package dev.fervento.assertbydebug;

import com.intellij.debugger.engine.evaluation.EvaluateException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ObjectReference;
import dev.fervento.assertbydebug.entity.FieldNode;

public interface TypeParser {
    public void parse(FieldNode father, FieldNode.Relation relationWithChild, String fieldName, ObjectReference objRef) throws EvaluateException, ClassNotLoadedException;
    public FieldNode getFieldNode();
    public FieldNode.Relation getRelationWithChild();
}
