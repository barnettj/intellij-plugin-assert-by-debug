package dev.fervento.assertbydebug.entity;

import dev.fervento.assertbydebug.serializer.JUnitSerializer;
import dev.fervento.assertbydebug.serializer.JsonSerializer;

public class NullFieldNode extends FieldNode {

    public NullFieldNode(FieldNode father, String fieldName) {
        super(father, fieldName);
    }

    @Override
    public void toJUnit(JUnitSerializer jUnitSerializer) {
        jUnitSerializer.assertNull(this);
    }

    @Override
    public void toJson(JsonSerializer jsonWriter) {
        jsonWriter.writeValueNull(this);
    }

}
