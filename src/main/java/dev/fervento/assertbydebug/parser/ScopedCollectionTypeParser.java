package dev.fervento.assertbydebug.parser;

public class ScopedCollectionTypeParser
//        implements TypeParser
{

//    public static final String NAME_ITERATOR = "it";
//    public static final String VARNAME_ELEM = "elem";
//
//    @Override
//    public FieldNode parse(BeanParser beanParser, FieldNode father, FieldNode.Relation relationWithChild, String fieldName, ObjectReference objRef) throws EvaluateException, ClassNotLoadedException {
//        Method toArray = objRef.referenceType().methodsByName("toArray", "()[Ljava/lang/Object;").get(0);
//        ArrayReference arrayReference = (ArrayReference)beanParser.getDebugProcess()
//                .invokeInstanceMethod(beanParser.getEvaluationContext(),
//                    objRef, toArray, Collections.EMPTY_LIST, 0);
//
//        CollectionFieldNode collectionFieldNode = new CollectionFieldNode(father, fieldName, objRef, arrayReference);
//        int capSize = Math.min(arrayReference.length(), beanParser.getMaxArrayLength());
//        for (int i = 0; i < capSize; i++) {
//            Value elem = arrayReference.getValue(i);
//            beanParser.parse(collectionFieldNode, new RelationByIteratorNext(NAME_ITERATOR), "", elem);
//        }
//        return collectionFieldNode;
//    }
//
//    public static class CollectionFieldNode extends FieldNode implements ReferencedNode {
//        private static final String SIZE = "size";
//        private ObjectReference collectionReference;
//        private ArrayReference arrayReference;
//        private RelationByMethodCall size;
//
//        public CollectionFieldNode(FieldNode father, String fieldName, ObjectReference collectionReference, ArrayReference arrayReference) {
//            super(father, fieldName);
//            this.collectionReference = collectionReference;
//            this.arrayReference = arrayReference;
//
//            this.size = new RelationByMethodCall(SIZE);
//            this.size.setFieldNode(new PrimitiveFieldNode(
//                            this, SIZE,
//                            new ArrayFieldNode.LengthValue(arrayReference.length())));
//            this.size.getFieldNode().setFatherChildRelation(this.size);
//        }
//
//        @Override
//        public ReferenceType getReferenceType() {
//            return collectionReference.referenceType();
//        }
//
//        @Override
//        public void toJUnit(JUnitSerializer jUnitSerializer) {
//            CodeGenerationContext codeGenerationContext = jUnitSerializer.getCodeGenerationContext();
//
//            CodeScope collectionCodeScope = codeGenerationContext.newScope();
//            String itVarname = collectionCodeScope.newVarName(null, NAME_ITERATOR);
//
//            this.size.getFieldNode().toJUnit(jUnitSerializer);
//            collectionCodeScope.writeLine(ParserUtils.format("Iterator %s = %s.iterator();", itVarname, collectionCodeScope.resolveVarName(this)));
//            for (Relation child : getChildren()) {
//                FieldNode fieldNode = child.getFieldNode();
//                if (fieldNode instanceof ReferencedNode) {
//                    CodeScope childScope = codeGenerationContext.newScope();
//                    String fieldNodeType = JNITypeResolver.toJavaName(((ReferencedNode) fieldNode).getReferenceType().name());
//                    childScope.writeLine(ParserUtils.format("%s %s = (%s)%s.next();", fieldNodeType, VARNAME_ELEM, fieldNodeType, NAME_ITERATOR));
//                    childScope.newVarName((ReferencedNode)fieldNode, VARNAME_ELEM);
//
//                    fieldNode.toJUnit(jUnitSerializer);
//                    codeGenerationContext.closeScope();
//                } else {
//                    fieldNode.toJUnit(jUnitSerializer);
//                }
//            }
//            codeGenerationContext.closeScope();
//        }
//
//        @Override
//        public void toJson(JsonSerializer jsonWriter) {
//            jsonWriter.startArray(this);
//            for (Relation child : getChildren()) {
//                child.getFieldNode().toJson(jsonWriter);
//            }
//            jsonWriter.endArray();
//        }
//
//        @Override
//        public long getUniqueId() {
//            return collectionReference.uniqueID();
//        }
//
//    }
//
//    public static class RelationByIteratorNext extends FieldNode.Relation {
//
//        private final String iteratorName;
//
//        public RelationByIteratorNext(String iteratorName) {
//            this.iteratorName = iteratorName;
//        }
//
//        public String getIteratorName() {
//            return iteratorName;
//        }
//
//        @Override
//        public void appendToPath(StringBuilder stringBuilder) {
//            stringBuilder.delete(0, stringBuilder.length());
//            stringBuilder.append(iteratorName).append(".next()");
//        }
//
//        @Override
//        public String appendToPath(String father) {
//            return iteratorName + ".next()";
//        }
//    }

}
