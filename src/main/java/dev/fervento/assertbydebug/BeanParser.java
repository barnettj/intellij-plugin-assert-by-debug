package dev.fervento.assertbydebug;

import com.intellij.debugger.engine.DebugProcess;
import com.intellij.debugger.engine.JavaValue;
import com.intellij.debugger.engine.evaluation.EvaluateException;
import com.intellij.debugger.engine.evaluation.EvaluationContext;
import com.intellij.debugger.engine.managerThread.DebuggerCommand;
import com.intellij.debugger.engine.managerThread.DebuggerManagerThread;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.sun.jdi.*;
import dev.fervento.assertbydebug.entity.ArrayFieldNode;
import dev.fervento.assertbydebug.entity.FieldNode;
import dev.fervento.assertbydebug.entity.NullFieldNode;
import dev.fervento.assertbydebug.entity.PrimitiveFieldNode;
import dev.fervento.assertbydebug.parser.*;
import dev.fervento.assertbydebug.serializer.CodeGenerationContext;
import dev.fervento.assertbydebug.serializer.impl.JUnitFlatSerializer;
import dev.fervento.assertbydebug.serializer.impl.JacksonJsonSerializer;

import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.nio.file.Path;
import java.time.temporal.Temporal;
import java.util.*;

public class BeanParser implements DebuggerCommand {

    private static final Logger LOG = Logger.getInstance(BeanParser.class);

    public static enum CopyAs {
        JUNIT,
        JSON,
        JSOG;
    }

    private static String TITLE_DEFAULT = "Assert By Debug Plugin";

    private final Project project;
    private DebugProcess debugProcess;
    private DebuggerManagerThread managerThread;
    private List<JavaValue> javaValueList = new ArrayList<>();
    private List<FieldNode> fieldNodeList = new ArrayList<>();
    private EvaluationContext evaluationContext;
    private SameInstanceFutureSetter sameInstanceFutureSetter;
    private List<ObjectReference> lockedReferences = new LinkedList<>();
    private CopyAs copyAs;
    private boolean commandCancelled;
    private Notification lastNotification;

    public BeanParser(Project project, DebugProcess debugProcess, CopyAs copyAs) {
        this.project = project;
        this.debugProcess = debugProcess;
        this.managerThread = debugProcess.getManagerThread();
        this.sameInstanceFutureSetter = new SameInstanceFutureSetter(this);
        this.copyAs = copyAs;
    }

    public void addValue(JavaValue javaValue) {
        javaValueList.add(javaValue);
    }

    public DebugProcess getDebugProcess() {
        return debugProcess;
    }

    public EvaluationContext getEvaluationContext() {
        return evaluationContext;
    }

    public void disableCollection(ObjectReference objectReference) {
        lockedReferences.add(objectReference);
        objectReference.disableCollection();
    }

    private void unlockCollection() {
        for (ObjectReference lockedReference : lockedReferences) {
            lockedReference.enableCollection();
        }
    }

    private void notify(boolean error, String message) {
        NotificationType information = (error ? NotificationType.ERROR : NotificationType.INFORMATION);
        final Notification notification = new Notification("AssertByDebugBeanParserNotification", TITLE_DEFAULT,
                message, information);

        if (lastNotification != null && !lastNotification.isExpired()) {
            lastNotification.expire();
        }
        lastNotification = notification;
        notification.notify(project);
    }

    @Override
    public void action() {
        try {
            notify(false, "Object dump started, please wait...");

            for (JavaValue javaValue : javaValueList) {
                if (commandCancelled) return;
                evaluationContext = javaValue.getEvaluationContext();
                parse(null, null, javaValue.getName(), javaValue.getDescriptor().getValue());
            }

            String textToCopy = null;
            if (copyAs == CopyAs.JUNIT) {
                JUnitFlatSerializer jUnitFlatSerializer = new JUnitFlatSerializer();
                for (FieldNode fieldNode : fieldNodeList) {
                    fieldNode.toJUnit(jUnitFlatSerializer);
                }
                textToCopy = jUnitFlatSerializer.toCode();
            } else if (copyAs == CopyAs.JSON || copyAs == CopyAs.JSOG) {
                JacksonJsonSerializer jsonSerializer = new JacksonJsonSerializer(copyAs == CopyAs.JSOG);
                for (FieldNode fieldNode : fieldNodeList) {
                    fieldNode.toJson(jsonSerializer);
                }
                textToCopy = jsonSerializer.toJsonString();
            }

            if (textToCopy != null) {
                StringSelection stringSelection = new StringSelection(textToCopy);
                CopyPasteManager.getInstance().setContents(stringSelection);
                notify(false, "Your objects are now in the clipboard");
            }

        } catch (CodeGenerationContext.UnsupportedLoopException e) {
            notify(true, "Cannot export in this format: a loop has been detected for the field \"" + e.getMessage() + "\"");
        } catch (Exception e) {
            notify(true, "Sorry, something wrong happened: are you exporting supporting types?\n"
                                        + "Check the logs and contribute to the project submitting an issue on <a href=\"https://github.com/fervento/intellij-plugin-assert-by-debug\">https://github.com/fervento/intellij-plugin-assert-by-debug</a>");
            LOG.warn("Cannot save as: " + copyAs.toString(), e);
        } finally {
            unlockCollection();
        }
    }

    @Override
    public void commandCancelled() {
        commandCancelled = true;
    }

    protected boolean isClass(ReferenceType referenceType, String claszName) {
        return claszName.equals(referenceType.name());
    }

    protected boolean isClassOrSubclassOf(ReferenceType referenceType, String claszName) {
        if (referenceType instanceof ClassType) {
            ClassType classType = (ClassType) referenceType;
            while (classType != null) {
                if (isClass(classType, claszName)) {
                    return true;
                }
                classType = classType.superclass();
            }
        }
        return false;
    }

    protected boolean implementsInterface(ReferenceType referenceType, String interfaceName) {
        if (referenceType instanceof ClassType) {
            return ((ClassType)referenceType).allInterfaces().stream()
                    .map(InterfaceType::name)
                    .anyMatch(name -> Objects.equals(name, interfaceName));
        }
        return false;
    }

    protected TypeParser newTypeParser(ReferenceType typename) {
        if (isEnum(typename)) {
            return new EnumTypeParser(this);
        } else if (isClass(typename, String.class.getCanonicalName())) {
            return new StringTypeParser();
        } else if (isClass(typename, File.class.getCanonicalName()) || isClass(typename, Path.class.getCanonicalName())
                || isClass(typename, UUID.class.getCanonicalName())
                || isClass(typename, Date.class.getCanonicalName()) || implementsInterface(typename, Temporal.class.getCanonicalName())) {
            return new ToStringTypeParser(this);
        } else if (isClassOrSubclassOf(typename, java.lang.Number.class.getCanonicalName())) {
            return new NumberTypeParser(this);
        } else if (implementsInterface(typename, java.util.Collection.class.getCanonicalName())) {
            return new CollectionTypeParser(this);
        } else if (implementsInterface(typename, java.util.Map.class.getCanonicalName())) {
            return new MapTypeParser(this);
        } else {
            return new POJOTypeParser(this);
        }
    }

    private boolean isEnum(ReferenceType typename) {
        return typename instanceof ClassType && ((ClassType)typename).isEnum();
    }

    public void parse(FieldNode father, FieldNode.Relation relationWithChild, String fieldName, Value value) throws EvaluateException, ClassNotLoadedException {
        if (value == null) {
            NullFieldNode child = new NullFieldNode(father, fieldName);
            addToFather(father, relationWithChild, child);
        } else if (value instanceof PrimitiveValue) {
            PrimitiveFieldNode child = new PrimitiveFieldNode(father, fieldName, value);
            addToFather(father, relationWithChild, child);
        } else if (value instanceof ObjectReference) {
            long uniqueId = ((ObjectReference) value).uniqueID();
            boolean continueParsing = sameInstanceFutureSetter.onStartParsing(father, relationWithChild, fieldName, uniqueId);

            if (continueParsing) {
                if (value instanceof ArrayReference) {
                    ArrayReference arrayReference = (ArrayReference)value;
                    ArrayFieldNode arrayFieldNode = new ArrayFieldNode(father, fieldName, arrayReference);
                    addToFather(father, relationWithChild, arrayFieldNode);
                    sameInstanceFutureSetter.onEndParsing(uniqueId, arrayFieldNode);

                    int capSize = Math.min(arrayReference.length(), getMaxArrayLength());
                    for (int i = 0; i < capSize; i++) {
                        Value elem = arrayReference.getValue(i);
                        parse(arrayFieldNode, new FieldNode.RelationByIndex(i), "", elem);
                    }
                } else {
                    ObjectReference objRef = (ObjectReference)value;
                    ReferenceType referenceType = objRef.referenceType();

                    TypeParser typeParser = newTypeParser(referenceType);
                    FieldNode fieldNode = null;
                    if (typeParser != null) {
                        typeParser.parse(father, relationWithChild, fieldName, objRef);
                        fieldNode = typeParser.getFieldNode();
                        if (fieldNode != null) {
                            addToFather(father, typeParser.getRelationWithChild(), fieldNode);
                        }
                    }
                    sameInstanceFutureSetter.onEndParsing(uniqueId, fieldNode);
                }
            }
        }
    }

    public int getMaxArrayLength() {
        return 100;
    }

    void addToFather(FieldNode father, FieldNode.Relation relationWithChild, FieldNode child) {
        if (father == null) {
            fieldNodeList.add(child);
        } else {
            relationWithChild.setFieldNode(child);
            father.addChild(relationWithChild);
        }
    }

    public void submit() {
        managerThread.invokeCommand(this);
    }

}
