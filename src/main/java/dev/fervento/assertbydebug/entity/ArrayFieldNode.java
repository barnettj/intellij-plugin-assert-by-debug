package dev.fervento.assertbydebug.entity;

import com.sun.jdi.*;
import dev.fervento.assertbydebug.serializer.JUnitSerializer;
import dev.fervento.assertbydebug.serializer.JsonSerializer;
import org.jetbrains.annotations.NotNull;

public class ArrayFieldNode extends FieldNode implements ReferencedNode {

    private static final String LENGTH = "length";
    private ArrayReference arrayReference;
    private RelationByVirtualField length;

    public ArrayFieldNode(FieldNode father, String fieldName, ArrayReference arrayReference) {
        super(father, fieldName);
        this.arrayReference = arrayReference;

        this.length = new RelationByVirtualField(LENGTH, arrayReference.virtualMachine().mirrorOf(0).type());
        this.length.setFieldNode(new PrimitiveFieldNode(
                        this, LENGTH,
                        new LengthValue(arrayReference.length())));
        this.length.getFieldNode().setFatherChildRelation(this.length);
    }

    @Override
    public void toJUnit(JUnitSerializer jUnitSerializer) {
        this.length.getFieldNode().toJUnit(jUnitSerializer);
        super.toJUnit(jUnitSerializer);
    }

    @Override
    public void toJson(JsonSerializer jsonWriter) {
        jsonWriter.startArray(this);
        for (Relation child : getChildren()) {
            child.getFieldNode().toJson(jsonWriter);
        }
        jsonWriter.endArray();
    }

    public RelationByVirtualField getLength() {
        return length;
    }

    @Override
    public long getUniqueId() {
        return arrayReference.uniqueID();
    }

    @Override
    public ReferenceType getReferenceType() {
        return arrayReference.referenceType();
    }

    public static class LengthValue implements IntegerValue {
        private int value;

        public LengthValue(int value) {
            this.value = value;
        }

        @Override
        public int value() {
            return value;
        }

        @Override
        public boolean booleanValue() {
            return false;
        }

        @Override
        public byte byteValue() {
            return (byte)value;
        }

        @Override
        public char charValue() {
            return (char)value;
        }

        @Override
        public short shortValue() {
            return (short)value;
        }

        @Override
        public int intValue() {
            return value;
        }

        @Override
        public long longValue() {
            return value;
        }

        @Override
        public float floatValue() {
            return (float)value;
        }

        @Override
        public double doubleValue() {
            return (double)value;
        }

        @Override
        public Type type() {
            return null;
        }

        @Override
        public VirtualMachine virtualMachine() {
            return null;
        }

        @Override
        public int compareTo(@NotNull IntegerValue o) {
            return Integer.compare(this.value, o.value());
        }
    }
}
