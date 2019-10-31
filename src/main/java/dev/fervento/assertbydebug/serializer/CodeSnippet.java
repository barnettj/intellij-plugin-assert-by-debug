package dev.fervento.assertbydebug.serializer;

import java.util.ArrayList;
import java.util.List;

public class CodeSnippet implements CodeBlock {
    public static final char ENDLINE = '\n';

    private List<String> code = new ArrayList<>();

    @Override
    public void writeLine(String line) {
        code.add(line);
    }


    @Override
    public void toCode(StringBuilder stringBuilder, String indentation) {
        for (String line : code) {
            stringBuilder.append(indentation).append(line).append(ENDLINE);
        }
    }
}
