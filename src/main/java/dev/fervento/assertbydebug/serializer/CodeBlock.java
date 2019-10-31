package dev.fervento.assertbydebug.serializer;

public interface CodeBlock {

    default void writeLine() {
        writeLine("");
    }

    void writeLine(String line);

    void toCode(StringBuilder stringBuilder, String indentation);
}
