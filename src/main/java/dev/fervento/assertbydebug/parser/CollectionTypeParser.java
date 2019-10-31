package dev.fervento.assertbydebug.parser;

import com.intellij.debugger.engine.evaluation.EvaluateException;
import com.sun.jdi.*;
import dev.fervento.assertbydebug.BeanParser;
import dev.fervento.assertbydebug.ParserUtils;
import dev.fervento.assertbydebug.TypeParser;
import dev.fervento.assertbydebug.serializer.CodeGenerationContext;
import dev.fervento.assertbydebug.serializer.CodeScope;
import dev.fervento.assertbydebug.serializer.JUnitSerializer;
import dev.fervento.assertbydebug.entity.ArrayFieldNode;
import dev.fervento.assertbydebug.entity.FieldNode;
import dev.fervento.assertbydebug.entity.ReferencedNode;
import dev.fervento.assertbydebug.serializer.JsonSerializer;

import java.util.Collections;

public class CollectionTypeParser implements TypeParser {

    public static final String NAME_COLLECTION = "collection";

    private BeanParser beanParser;
    private CollectionFieldNode collectionFieldNode;
    private CollectionToArrayRelation overridenFatherChildRelation;

    public CollectionTypeParser(BeanParser beanParser) {
        this.beanParser = beanParser;
    }

    @Override
    public void parse(FieldNode father, FieldNode.Relation relationWithChild, String fieldName, ObjectReference objRef) throws EvaluateException, ClassNotLoadedException {
        Method toArray = objRef.referenceType().methodsByName("toArray", "()[Ljava/lang/Object;").get(0);
        ArrayReference arrayReference = (ArrayReference)beanParser.getDebugProcess()
                .invokeInstanceMethod(beanParser.getEvaluationContext(),
                    objRef, toArray, Collections.EMPTY_LIST, 0);
        beanParser.disableCollection(arrayReference);

        //Type type = getTypeFromGenerics(objRef);
        this.collectionFieldNode = new CollectionFieldNode(father, fieldName, objRef, arrayReference);
        this.overridenFatherChildRelation = new CollectionToArrayRelation(relationWithChild);
        ReferenceType objectType = objRef.virtualMachine().classesByName("java.lang.Object").get(0);
        ReferenceType objectArrayType = objRef.virtualMachine().classesByName("java.lang.Object[]").get(0);

        // Handled as .toArray()
        collectionFieldNode.setType(objectArrayType);
        overridenFatherChildRelation.setType(objectArrayType);

        int capSize = Math.min(arrayReference.length(), beanParser.getMaxArrayLength());
        for (int i = 0; i < capSize; i++) {
            Value elem = arrayReference.getValue(i);
            beanParser.parse(collectionFieldNode, new FieldNode.RelationByIndex(i).withType(objectType), "", elem);
        }
    }

    @Override
    public FieldNode getFieldNode() {
        return collectionFieldNode;
    }

    @Override
    public FieldNode.Relation getRelationWithChild() {
        return overridenFatherChildRelation;
    }

    private Type getTypeFromGenerics(ObjectReference objRef) throws ClassNotLoadedException {
        String genericSignature = ((InterfaceType)(objRef.referenceType()
                .methodsByName("iterator").get(0).returnType())).genericSignature();

        return new ParserUtils.GenericsHelper(objRef.virtualMachine(), genericSignature)
            .getGenericMap().entrySet().iterator().next().getValue();
    }

    public static class CollectionToArrayRelation extends FieldNode.Relation {
        private FieldNode.Relation relation;
        private Type type;
        public CollectionToArrayRelation(FieldNode.Relation relation) {
            this.relation = relation;
        }

        @Override
        public FieldNode getFieldNode() {
            return relation.getFieldNode();
        }

        @Override
        public void setFieldNode(FieldNode fieldNode) {
            relation.setFieldNode(fieldNode);
        }

        @Override
        public Type getChildType(FieldNode father) {
            if (type != null) {
                return type;
            }
            return relation.getChildType(father);
        }

        public void setType(Type type) {
            this.type = type;
        }

        @Override
        public void appendToPath(StringBuilder stringBuilder) {
            relation.appendToPath(stringBuilder);
            stringBuilder.append(".toArray()");
        }
    }

    public static class CollectionFieldNode extends ArrayFieldNode {
        private static final String NAME_ITEM = "item";
        private ObjectReference collectionReference;
        private ReferenceType type;

        public CollectionFieldNode(FieldNode father, String fieldName, ObjectReference collectionReference, ArrayReference arrayReference) {
            super(father, fieldName, arrayReference);
            this.collectionReference = collectionReference;
        }

        @Override
        public ReferenceType getReferenceType() {
            if (type != null) {
                return type;
            }
            return collectionReference.referenceType();
        }

        public void setType(ReferenceType type) {
            this.type = type;
        }

        @Override
        public void toJUnit(JUnitSerializer jUnitSerializer) {
            CodeGenerationContext codeGenerationContext = jUnitSerializer.getCodeGenerationContext();
            CodeScope currentScope = codeGenerationContext.getCurrentScope();

            String originalVarName = currentScope.resolveVarName(this);
            String arrayVarName = currentScope.getOrCreateVarName(this, NAME_COLLECTION);

            currentScope.writeLine();
            currentScope.writeLine(ParserUtils.format("Object[] %s = %s;", arrayVarName, originalVarName));

            this.getLength().getFieldNode().toJUnit(jUnitSerializer);
            for (Relation child : getChildren()) {
                FieldNode fieldNode = child.getFieldNode();
                if (fieldNode instanceof ReferencedNode) {
                    String originalItemVarName = currentScope.resolveVarName(fieldNode);
                    String itemName = currentScope.getOrCreateVarName((ReferencedNode) fieldNode, NAME_ITEM);
                    String typeName = ParserUtils.JNITypeResolver.toJavaName(((ReferencedNode) fieldNode).getReferenceType().name());
                    currentScope.writeLine(ParserUtils.format(
                            "%s %s = %s;",
                            typeName, itemName,
                            //typeName,
                            originalItemVarName));

                }
                fieldNode.toJUnit(jUnitSerializer);
            }

            currentScope.writeLine();
        }

        @Override
        public void toJson(JsonSerializer jsonWriter) {
            jsonWriter.startArray(this);
            for (Relation child : getChildren()) {
                child.getFieldNode().toJson(jsonWriter);
            }
            jsonWriter.endArray();
        }

        @Override
        public long getUniqueId() {
            return collectionReference.uniqueID();
        }

    }

}
