package dev.fervento.assertbydebug.entity;

import com.sun.jdi.*;
import dev.fervento.assertbydebug.serializer.JUnitSerializer;
import dev.fervento.assertbydebug.serializer.JsonSerializer;

public class PrimitiveFieldNode extends FieldNode {
    private Value primitiveValue;

    public PrimitiveFieldNode(FieldNode father, String fieldName, Value primitiveValue) {
        super(father, fieldName);
        this.primitiveValue = primitiveValue;
    }

    public Value getPrimitiveValue() {
        return primitiveValue;
    }

    public void setPrimitiveValue(Value primitiveValue) {
        this.primitiveValue = primitiveValue;
    }

    @Override
    public void toJson(JsonSerializer jsonWriter) {
        Value primitiveValue = getPrimitiveValue();

        if (primitiveValue instanceof BooleanValue) {
            BooleanValue value = (BooleanValue) primitiveValue;
            jsonWriter.writeBoolean(this, value.value());
        } else if (isInstanceOfDiscreteNumber(primitiveValue)) {
            PrimitiveValue value = (PrimitiveValue) primitiveValue;
            jsonWriter.writeLong(this, value.longValue());

        } else if (primitiveValue instanceof CharValue) {
            CharValue value = (CharValue) primitiveValue;
            jsonWriter.writeChar(this, value.value());

        } else if (isInstanceOfFloatNumber(primitiveValue)) {
            PrimitiveValue value = (PrimitiveValue) primitiveValue;
            jsonWriter.writeDouble(this, value.doubleValue());
        } else if (primitiveValue instanceof VoidValue) {
            VoidValue value = (VoidValue) primitiveValue;
            jsonWriter.writeValueNull(this);
        }
    }

    @Override
    public void toJUnit(JUnitSerializer jUnitSerializer) {
        /*
            Subinterfaces of PrimitiveValue
            a boolean   true	    BooleanValue	BooleanType
            a byte      (byte)4	    ByteValue	    ByteType
            a char      'a'	        CharValue	    CharType
            a double    3.1415926	DoubleValue	    DoubleType
            a float     2.5f	    FloatValue	    FloatType
            an int      22	        IntegerValue	IntegerType
            a long      1024L	    LongValue	    LongType
            a short     (short)12	ShortValue	    ShortType
            a void	                VoidValue	    VoidType
         */
        Value primitiveValue = getPrimitiveValue();

        if (primitiveValue instanceof BooleanValue) {
            BooleanValue value = (BooleanValue) primitiveValue;
            if (value.value()) {
                jUnitSerializer.assertTrue(this);
            } else {
                jUnitSerializer.assertFalse(this);

            }
        } else if (isInstanceOfDiscreteNumber(primitiveValue)) {
            PrimitiveValue value = (PrimitiveValue) primitiveValue;
            jUnitSerializer.assertEquals(value.longValue(), this);

        } else if (primitiveValue instanceof CharValue) {
            CharValue value = (CharValue) primitiveValue;
            jUnitSerializer.assertEquals(value.value(), this);

        } else if (isInstanceOfFloatNumber(primitiveValue)) {
            PrimitiveValue value = (PrimitiveValue) primitiveValue;
            jUnitSerializer.assertEquals(value.doubleValue(), this);
        } else if (primitiveValue instanceof VoidValue) {
            VoidValue value = (VoidValue) primitiveValue;
            jUnitSerializer.assertNull(this);
        }
    }

    private boolean isInstanceOfDiscreteNumber(Value primitiveValue) {
        return primitiveValue instanceof ByteValue
                || primitiveValue instanceof IntegerValue
                || primitiveValue instanceof ShortValue
                || primitiveValue instanceof LongValue;
    }

    private boolean isInstanceOfFloatNumber(Value primitiveValue) {
        return primitiveValue instanceof DoubleValue
                || primitiveValue instanceof FloatValue;
    }

}
