package dev.fervento.assertbydebug.serializer.impl;

import dev.fervento.assertbydebug.ParserUtils;
import dev.fervento.assertbydebug.serializer.JUnitSerializer;
import dev.fervento.assertbydebug.entity.FieldNode;
import dev.fervento.assertbydebug.serializer.CodeGenerationContext;

import java.math.BigDecimal;

public class JUnitFlatSerializer implements JUnitSerializer {
    
    private CodeGenerationContext codeGenerationContext = new CodeGenerationContext();

    @Override
    public CodeGenerationContext getCodeGenerationContext() {
        return codeGenerationContext;
    }

    @Override
    public void assertTrue(FieldNode fieldNode) {
        
        codeGenerationContext.getCurrentScope().writeLine(ParserUtils.format("assertTrue(%s);", codeGenerationContext.getPath(fieldNode)));
    }

    @Override
    public void assertFalse(FieldNode fieldNode) {
        codeGenerationContext.getCurrentScope().writeLine(ParserUtils.format("assertFalse(%s);", codeGenerationContext.getPath(fieldNode)));
    }

    @Override
    public void assertNull(FieldNode fieldNode) {
        codeGenerationContext.getCurrentScope().writeLine(ParserUtils.format("assertNull(%s);", codeGenerationContext.getPath(fieldNode)));
    }

    @Override
    public void assertSame(FieldNode expectation, FieldNode fieldNode) {
        codeGenerationContext.getCurrentScope().writeLine(ParserUtils.format("assertSame(%s, %s);",
                codeGenerationContext.getCurrentScope().resolveVarName(expectation),
                codeGenerationContext.getCurrentScope().getPath(fieldNode)));
    }

    @Override
    public void assertEquals(long value, FieldNode fieldNode) {
        codeGenerationContext.getCurrentScope().writeLine(ParserUtils.format("assertEquals(%d, %s);", value, codeGenerationContext.getPath(fieldNode)));
    }

    @Override
    public void assertEquals(char value, FieldNode fieldNode) {
        codeGenerationContext.getCurrentScope().writeLine(ParserUtils.format("assertEquals(%c, %s);", value, codeGenerationContext.getPath(fieldNode)));
    }

    @Override
    public void assertEquals(double value, FieldNode fieldNode) {
        codeGenerationContext.getCurrentScope().writeLine(ParserUtils.format("assertEquals(%f, %s, ACCURACY);", value, codeGenerationContext.getPath(fieldNode)));
    }

    @Override
    public void assertEquals(String value, FieldNode fieldNode) {
        codeGenerationContext.getCurrentScope().writeLine(ParserUtils.format("assertEquals(%s, %s);", value, codeGenerationContext.getPath(fieldNode)));
    }

    @Override
    public void assertBigDecimalEqual(BigDecimal value, FieldNode fieldNode) {
        codeGenerationContext.getCurrentScope().writeLine(ParserUtils.format("assertEquals(0, new BigDecimal(\"%s\").compareTo(%s));", value,
                codeGenerationContext.getPath(fieldNode)));
    }

    public String toCode() {
        return codeGenerationContext.toCode();
    }

}
