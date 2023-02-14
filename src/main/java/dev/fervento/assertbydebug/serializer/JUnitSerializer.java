package dev.fervento.assertbydebug.serializer;

import dev.fervento.assertbydebug.entity.FieldNode;

import java.math.BigDecimal;

public interface JUnitSerializer extends CodeGenerationContext.CodeGenerator {

    public void assertTrue(FieldNode fieldNode);
    public void assertFalse(FieldNode fieldNode);
    public void assertNull(FieldNode fieldNode);
    public void assertSame(FieldNode expectation, FieldNode fieldNode);
    public void assertEquals(long value, FieldNode fieldNode);
    public void assertEquals(String value, FieldNode fieldNode);
    public void assertEquals(char value, FieldNode fieldNode);
    public void assertEquals(double value, FieldNode fieldNode);
    public void assertBigDecimalEqual(BigDecimal value, FieldNode fieldNode);

}
