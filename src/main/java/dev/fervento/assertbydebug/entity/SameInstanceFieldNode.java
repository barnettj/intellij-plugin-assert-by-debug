package dev.fervento.assertbydebug.entity;

import com.sun.jdi.ReferenceType;
import dev.fervento.assertbydebug.serializer.JUnitSerializer;
import dev.fervento.assertbydebug.serializer.JsonSerializer;

public class SameInstanceFieldNode<T extends FieldNode & ReferencedNode> extends FieldNode implements ReferencedNode {

    private T other;

    public SameInstanceFieldNode(FieldNode father, String fieldName, T other) {
        super(father, fieldName);
        this.other = other;
    }

    public T getOther() {
        return other;
    }

    @Override
    public void toJUnit(JUnitSerializer jUnitSerializer) {
        jUnitSerializer.assertSame(other, this);
    }

    @Override
    public void toJson(JsonSerializer jsonWriter) {
        jsonWriter.sameObject(this);
    }

    @Override
    public long getUniqueId() {
        return other.getUniqueId();
    }

    @Override
    public ReferenceType getReferenceType() {
        return other.getReferenceType();
    }
}

