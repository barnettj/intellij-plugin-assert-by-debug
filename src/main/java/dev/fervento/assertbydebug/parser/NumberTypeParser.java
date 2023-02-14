package dev.fervento.assertbydebug.parser;

import com.intellij.debugger.engine.evaluation.EvaluateException;
import com.sun.jdi.*;
import dev.fervento.assertbydebug.BeanParser;
import dev.fervento.assertbydebug.TypeParser;
import dev.fervento.assertbydebug.entity.FieldNode;
import dev.fervento.assertbydebug.entity.ReferencedNode;
import dev.fervento.assertbydebug.serializer.JUnitSerializer;
import dev.fervento.assertbydebug.serializer.JsonSerializer;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NumberTypeParser implements TypeParser {

    private BeanParser beanParser;
    private FieldNode fieldNode;
    private FieldNode.Relation relation;

    public NumberTypeParser(BeanParser beanParser) {
        this.beanParser = beanParser;
    }

    @Override
    public void parse(FieldNode father,
                           FieldNode.Relation relationWithChild, String fieldName, ObjectReference objRef) throws EvaluateException, ClassNotLoadedException {

        PrimitiveValue primitiveValue;
        if (isFloatType(objRef)) {
            primitiveValue = toDouble(beanParser, objRef);
        } else if (isIntegerType(objRef)) {
            primitiveValue = toLong(beanParser, objRef);
        } else if (isBigDecimalType(objRef)) {
            this.fieldNode = new NumberFieldNode(father, fieldName, objRef, toBigDecimal(beanParser, objRef));
            this.relation = relationWithChild;
            return;
        } else {
            throw new RuntimeException("Not expected type: " + objRef.referenceType().name());
        }

        this.fieldNode = new NumberFieldNode(father, fieldName, objRef, primitiveValue);
        this.relation = relationWithChild;
    }

    @Override
    public FieldNode getFieldNode() {
        return fieldNode;
    }

    @Override
    public FieldNode.Relation getRelationWithChild() {
        return relation;
    }

    private static boolean isFloatType(ObjectReference value) {
        return floatTypeNames.contains(value.referenceType().name());
    }

    private static boolean isIntegerType(ObjectReference value) {
        return integerTypeNames.contains(value.referenceType().name());
    }

    private static boolean isBigDecimalType(ObjectReference value) {
        return java.math.BigDecimal.class.getCanonicalName().equals(value.referenceType().name());
    }

    private BigDecimal toBigDecimal(BeanParser beanParser, ObjectReference value) throws EvaluateException {
        final Method toString = value.referenceType()
                .methodsByName("toString", "()Ljava/lang/String;").get(0);
        final StringReference stringRef = (StringReference) beanParser.getDebugProcess().invokeInstanceMethod(beanParser.getEvaluationContext(), value, toString,
                Collections.EMPTY_LIST, 0);
        final String val = stringRef.toString().replaceAll("\"", "");
        return new BigDecimal(val);
    }

    private PrimitiveValue toLong(BeanParser beanParser, ObjectReference value) throws EvaluateException {
        Method toString = value.referenceType().methodsByName("longValue", "()J").get(0);
        return (PrimitiveValue)beanParser.getDebugProcess()
                .invokeInstanceMethod(beanParser.getEvaluationContext(), value, toString, Collections.EMPTY_LIST, 0);
    }

    private PrimitiveValue toDouble(BeanParser beanParser, ObjectReference value) throws EvaluateException {
        Method toString = value.referenceType().methodsByName("doubleValue", "()D").get(0);
        return (PrimitiveValue)beanParser.getDebugProcess()
                .invokeInstanceMethod(beanParser.getEvaluationContext(), value, toString, Collections.EMPTY_LIST, 0);
    }

    private static final List<String> integerTypeNames =
            Arrays.asList(
                Byte.class.getCanonicalName(),
                Short.class.getCanonicalName(),
                Integer.class.getCanonicalName(),
                Long.class.getCanonicalName()
            );

    private static final List<String> floatTypeNames =
            Arrays.asList(
                Float.class.getCanonicalName(),
                Double.class.getCanonicalName()
            );

    public static class NumberFieldNode extends FieldNode implements ReferencedNode {
        private ObjectReference value;
        private PrimitiveValue primitiveValue;
        private BigDecimal bigDecimal;

        public NumberFieldNode(FieldNode father, String fieldName, ObjectReference value, PrimitiveValue primitiveValue) {
            super(father, fieldName);
            this.value = value;
            this.primitiveValue = primitiveValue;
        }

        public NumberFieldNode(FieldNode father, String fieldName, ObjectReference value, BigDecimal bigDecimal) {
            super(father, fieldName);
            this.value = value;
            this.bigDecimal = bigDecimal;
        }

        @Override
        public void toJUnit(JUnitSerializer jUnitSerializer) {
            if (isIntegerType(value)) {
                jUnitSerializer.assertEquals(primitiveValue.longValue(), this);
            } else if (isFloatType(value)) {
                jUnitSerializer.assertEquals(primitiveValue.doubleValue(), this);
            } else if (isBigDecimalType(value)) {
                jUnitSerializer.assertBigDecimalEqual(bigDecimal, this);
            } else {
                throw new RuntimeException("Not expected type: " + value.referenceType().name());
            }
        }

        @Override
        public void toJson(JsonSerializer jsonWriter) {
            if (isIntegerType(value)) {
                jsonWriter.writeLong(this, primitiveValue.longValue());
            } else if (isFloatType(value)) {
                jsonWriter.writeDouble(this, primitiveValue.doubleValue());
            } else {
                throw new RuntimeException("Not expected type: " + value.referenceType().name());
            }
        }

        @Override
        public long getUniqueId() {
            return value.uniqueID();
        }

        @Override
        public ReferenceType getReferenceType() {
            return value.referenceType();
        }
    }

}
