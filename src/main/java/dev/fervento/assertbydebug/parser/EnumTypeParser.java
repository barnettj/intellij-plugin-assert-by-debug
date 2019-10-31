package dev.fervento.assertbydebug.parser;

import com.intellij.debugger.engine.evaluation.EvaluateException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import dev.fervento.assertbydebug.BeanParser;
import dev.fervento.assertbydebug.ParserUtils;
import dev.fervento.assertbydebug.TypeParser;
import dev.fervento.assertbydebug.serializer.JUnitSerializer;
import dev.fervento.assertbydebug.entity.FieldNode;
import dev.fervento.assertbydebug.entity.ReferencedNode;
import dev.fervento.assertbydebug.serializer.JsonSerializer;

import java.util.Collections;

public class EnumTypeParser implements TypeParser {

    private BeanParser beanParser;
    private FieldNode fieldNode;
    private FieldNode.Relation relationWithChild;

    public EnumTypeParser(BeanParser beanParser) {
        this.beanParser = beanParser;
    }

    @Override
    public void parse(FieldNode father,
                      FieldNode.Relation relationWithChild, String fieldName, ObjectReference objRef) throws EvaluateException {
        StringReference name = getName(beanParser, objRef);
        this.fieldNode = new EnumFieldNode(father, fieldName, objRef, name);
        this.relationWithChild = relationWithChild;
    }

    private StringReference getName(BeanParser beanParser, ObjectReference entry) throws EvaluateException {
        Method entrySetMethod = entry.referenceType().methodsByName("name", "()Ljava/lang/String;").get(0);
        return (StringReference)beanParser.getDebugProcess().invokeInstanceMethod(beanParser.getEvaluationContext(),
                entry, entrySetMethod, Collections.EMPTY_LIST, 0);
    }

    @Override
    public FieldNode getFieldNode() {
        return fieldNode;
    }

    @Override
    public FieldNode.Relation getRelationWithChild() {
        return relationWithChild;
    }

    public static class EnumFieldNode extends FieldNode implements ReferencedNode {
        private ObjectReference enumRef;
        private StringReference value;

        public EnumFieldNode(FieldNode father, String fieldName, ObjectReference enumRef, StringReference value) {
            super(father, fieldName);
            this.enumRef = enumRef;
            this.value = value;
        }

        @Override
        public void toJUnit(JUnitSerializer jUnitSerializer) {
            String enumType = ParserUtils.JNITypeResolver.toJavaName(enumRef.type().name());
            String enumValue = value.value();
            jUnitSerializer.assertEquals(String.format("%s.%s", enumType, enumValue), this);
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
