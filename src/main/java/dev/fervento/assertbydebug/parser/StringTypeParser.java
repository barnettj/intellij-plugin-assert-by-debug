package dev.fervento.assertbydebug.parser;

import com.intellij.debugger.engine.evaluation.EvaluateException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import dev.fervento.assertbydebug.TypeParser;
import dev.fervento.assertbydebug.entity.FieldNode;
import dev.fervento.assertbydebug.entity.ReferencedNode;
import dev.fervento.assertbydebug.serializer.JUnitSerializer;
import dev.fervento.assertbydebug.serializer.JsonSerializer;

public class StringTypeParser implements TypeParser {

    private FieldNode fieldNode;
    private FieldNode.Relation relationWithChild;

    @Override
    public void parse(FieldNode father,
                           FieldNode.Relation relationWithChild, String fieldName, ObjectReference objRef) throws EvaluateException, ClassNotLoadedException {
        this.fieldNode = new StringFieldNode(father, fieldName, (StringReference)objRef);
        this.relationWithChild = relationWithChild;
    }

    @Override
    public FieldNode getFieldNode() {
        return fieldNode;
    }

    @Override
    public FieldNode.Relation getRelationWithChild() {
        return relationWithChild;
    }

    public static class StringFieldNode extends FieldNode implements ReferencedNode {
        private StringReference value;

        public StringFieldNode(FieldNode father, String fieldName, StringReference value) {
            super(father, fieldName);
            this.value = value;
        }

        @Override
        public void toJUnit(JUnitSerializer jUnitSerializer) {
            jUnitSerializer.assertEquals(value.toString(), this);
        }

        @Override
        public void toJson(JsonSerializer jsonWriter) {
            jsonWriter.writeString(this, value.value());
        }

        @Override
        public long getUniqueId() {
            return value.uniqueID();
        }

        @Override
        public ReferenceType getReferenceType() {
            return value.referenceType();
        }
    }

}
