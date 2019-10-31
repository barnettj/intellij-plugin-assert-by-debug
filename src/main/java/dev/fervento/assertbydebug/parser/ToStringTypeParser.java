package dev.fervento.assertbydebug.parser;

import com.intellij.debugger.engine.evaluation.EvaluateException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StringReference;
import dev.fervento.assertbydebug.BeanParser;
import dev.fervento.assertbydebug.TypeParser;
import dev.fervento.assertbydebug.entity.FieldNode;

import java.util.Arrays;

public class ToStringTypeParser implements TypeParser {

    private FieldNode fieldNode;
    private FieldNode.Relation relationWithChild;
    private BeanParser beanParser;

    public ToStringTypeParser(BeanParser beanParser) {
        this.beanParser = beanParser;
    }

    @Override
    public void parse(FieldNode father,
                      FieldNode.Relation relationWithChild, String fieldName, ObjectReference objRef) throws EvaluateException {

        ClassType objectsClass = (ClassType)beanParser.getDebugProcess().getVirtualMachineProxy().classesByName("java.util.Objects").get(0);
        Method toStringMethod = objectsClass.methodsByName("toString", "(Ljava/lang/Object;)Ljava/lang/String;").get(0);
        StringReference stringReference = (StringReference) beanParser.getDebugProcess().invokeMethod(
                                            beanParser.getEvaluationContext(),
                                            objectsClass, toStringMethod, Arrays.asList(objRef));
        beanParser.disableCollection(stringReference);

        this.fieldNode = new StringTypeParser.StringFieldNode(father, fieldName, stringReference);
        this.relationWithChild = relationWithChild;
    }

    @Override
    public FieldNode getFieldNode() {
        return fieldNode;
    }

    @Override
    public FieldNode.Relation getRelationWithChild() {
        return relationWithChild;
    }

}
