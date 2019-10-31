package dev.fervento.assertbydebug.serializer;

import dev.fervento.assertbydebug.entity.FieldNode;

import java.util.LinkedList;
import java.util.Optional;

public class CodeGenerationContext {

    private final LinkedList<CodeGenerationCallback> codeGenerationCallbacks = new LinkedList<>();
    private final LinkedList<CodeScope> scopeStack = new LinkedList<>();

    public CodeGenerationContext() {
        scopeStack.add(new CodeScope(null));
    }

    public void removeCodeGenerationCallback(CodeGenerationCallback codeGenerationCallback) {
        codeGenerationCallbacks.remove(codeGenerationCallback);
    }

    public String toCode() {
        StringBuilder stringBuilder = new StringBuilder();
        scopeStack.getFirst().toCode(stringBuilder, "");
        return stringBuilder.toString();
    }

    public static interface CodeGenerationCallback {
        Optional<String> getPath(CodeGenerationContext context, FieldNode fieldNode);
    }

    public static interface CodeGenerator {
        CodeGenerationContext getCodeGenerationContext();
    }

    public String getPath(FieldNode fieldNode) {
        return getCurrentScope().resolveVarName(fieldNode);
    }

    public CodeScope newScope() {
        CodeScope newScope = scopeStack.getLast().newInnerCodeScope();
        scopeStack.add(newScope);
        return newScope;
    }

    public void closeScope() {
        scopeStack.getLast().getFather().closeInnerCodeScope();
        scopeStack.removeLast();
    }

    public CodeScope getCurrentScope() {
        return scopeStack.getLast();
    }

    public void push(CodeGenerationCallback node) {
        codeGenerationCallbacks.addLast(node);
    }

}
