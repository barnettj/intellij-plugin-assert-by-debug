package dev.fervento.assertbydebug.serializer;

import dev.fervento.assertbydebug.entity.FieldNode;
import dev.fervento.assertbydebug.entity.SameInstanceFieldNode;

public interface JsonSerializer extends CodeGenerationContext.CodeGenerator {
    void startArray(FieldNode fieldNode);

    void endArray();

    void writeValueNull(FieldNode fieldNode);

    void startObject(FieldNode fieldNode);

    void endObject();

    void writeBoolean(FieldNode fieldNode, boolean value);

    void writeLong(FieldNode fieldNode, long longValue);

    void writeChar(FieldNode fieldNode, char value);

    void writeDouble(FieldNode fieldNode, double doubleValue);

    void writeString(FieldNode fieldNode, String value);

    void sameObject(SameInstanceFieldNode sameInstanceFieldNode);
}
