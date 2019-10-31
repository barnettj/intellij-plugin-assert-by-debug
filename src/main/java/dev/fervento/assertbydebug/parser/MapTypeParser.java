package dev.fervento.assertbydebug.parser;

import com.intellij.debugger.engine.evaluation.EvaluateException;
import com.sun.jdi.*;
import dev.fervento.assertbydebug.BeanParser;
import dev.fervento.assertbydebug.TypeParser;
import dev.fervento.assertbydebug.entity.POJOFieldNode;
import dev.fervento.assertbydebug.entity.FieldNode;

import java.util.Arrays;
import java.util.Collections;

public class MapTypeParser implements TypeParser {

    private BeanParser beanParser;
    private POJOFieldNode pojoFieldNode;
    private FieldNode.Relation relationWithChild;

    public MapTypeParser(BeanParser beanParser) {
        this.beanParser = beanParser;
    }

    @Override
    public void parse(FieldNode father, FieldNode.Relation relationWithChild, String fieldName, ObjectReference objRef) throws EvaluateException, ClassNotLoadedException {
        ClassType objectsClass = (ClassType)beanParser.getDebugProcess().getVirtualMachineProxy().classesByName("java.util.Objects").get(0);
        Method toStringMethod = objectsClass.methodsByName("toString", "(Ljava/lang/Object;)Ljava/lang/String;").get(0);

        this.pojoFieldNode = new POJOFieldNode(father, fieldName, objRef);
        this.relationWithChild = relationWithChild;

        Method entrySetMethod = objRef.referenceType().methodsByName("entrySet", "()Ljava/util/Set;").get(0);
        ObjectReference entrySet = (ObjectReference)beanParser.getDebugProcess().invokeInstanceMethod(beanParser.getEvaluationContext(),
                objRef, entrySetMethod, Collections.EMPTY_LIST, 0);

        Method toArrayMethod = entrySet.referenceType().methodsByName("toArray", "()[Ljava/lang/Object;").get(0);
        ArrayReference arrayReference = (ArrayReference)beanParser.getDebugProcess()
                .invokeInstanceMethod(beanParser.getEvaluationContext(),
                        entrySet, toArrayMethod, Collections.EMPTY_LIST, 0);
        beanParser.disableCollection(arrayReference);

        Method mapGetMethod = getMapGetMethod(objRef);
        for (Value entryValue : arrayReference.getValues()) {
            ObjectReference entry = (ObjectReference)entryValue;
            ObjectReference key = getKey(beanParser, entry);
            ObjectReference value = getValue(beanParser, entry);
            StringReference keyToString = toString(beanParser, key, objectsClass, toStringMethod);

            beanParser.parse(pojoFieldNode,
                    new FieldNode.RelationByMethodCall(mapGetMethod, keyToString.toString()),
                    keyToString.value(), value);
        }
    }

    @Override
    public FieldNode getFieldNode() {
        return pojoFieldNode;
    }

    @Override
    public FieldNode.Relation getRelationWithChild() {
        return relationWithChild;
    }

    private Method getMapGetMethod(ObjectReference entry) {
        return entry.referenceType().methodsByName("get", "(Ljava/lang/Object;)Ljava/lang/Object;").get(0);
    }

    private ObjectReference getKey(BeanParser beanParser, ObjectReference entry) throws EvaluateException {
        Method entrySetMethod = entry.referenceType().methodsByName("getKey", "()Ljava/lang/Object;").get(0);
        return (ObjectReference)beanParser.getDebugProcess().invokeInstanceMethod(beanParser.getEvaluationContext(),
                entry, entrySetMethod, Collections.EMPTY_LIST, 0);
    }

    private ObjectReference getValue(BeanParser beanParser, ObjectReference entry) throws EvaluateException {
        Method entrySetMethod = entry.referenceType().methodsByName("getValue", "()Ljava/lang/Object;").get(0);
        return (ObjectReference)beanParser.getDebugProcess().invokeInstanceMethod(beanParser.getEvaluationContext(),
                entry, entrySetMethod, Collections.EMPTY_LIST, 0);
    }

    private StringReference toString(BeanParser beanParser, ObjectReference objRef, ClassType objectsClass, Method toStringMethod) throws EvaluateException {
        return (StringReference)beanParser.getDebugProcess().invokeMethod(
                beanParser.getEvaluationContext(),
                objectsClass, toStringMethod, Arrays.asList(objRef));
    }

}
