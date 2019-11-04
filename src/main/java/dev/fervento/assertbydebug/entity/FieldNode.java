package dev.fervento.assertbydebug.entity;

import com.sun.jdi.*;
import dev.fervento.assertbydebug.serializer.JUnitSerializer;
import dev.fervento.assertbydebug.serializer.JsonSerializer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FieldNode {
    private final String fieldName;
    private final FieldNode father;
    private final List<Relation> children = new ArrayList<>();
    private Relation fatherChildRelation;

    public FieldNode(FieldNode father, String fieldName) {
        this.fieldName = fieldName;
        this.father = father;
    }

    public void addChild(Relation child) {
        children.add(child);
        child.getFieldNode().setFatherChildRelation(child);
    }

    public void toJUnit(JUnitSerializer jUnitSerializer) {
        for (Relation child : getChildren()) {
            child.getFieldNode().toJUnit(jUnitSerializer);
        }
    }

    public void toJson(JsonSerializer jsonWriter) {
        throw new UnsupportedOperationException();
    }

    public LinkedList<FieldNode> getFieldNodeTree() {
        LinkedList<FieldNode> tree = new LinkedList<>();
        for (FieldNode node = this; node != null; node = node.father) {
            tree.addFirst(node);
        }
        return tree;
    }

    public void setFatherChildRelation(Relation fatherChildRelation) {
        this.fatherChildRelation = fatherChildRelation;
    }

    public Relation getFatherChildRelation() {
        return fatherChildRelation;
    }

    public String getFieldName() {
        return fieldName;
    }

    public FieldNode getFather() {
        return father;
    }

    public List<Relation> getChildren() {
        return children;
    }

    public static abstract class Relation {
        protected FieldNode fieldNode;

        public FieldNode getFieldNode() {
            return fieldNode;
        }

        public void setFieldNode(FieldNode fieldNode) {
            this.fieldNode = fieldNode;
        }

        public abstract void appendToPath(StringBuilder stringBuilder);

        public abstract Type getChildType(FieldNode father);
    }

    public static class RelationByIndex extends Relation {
        private int id;
        private Type type;

        public RelationByIndex(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        @Override
        public void appendToPath(StringBuilder stringBuilder) {
            stringBuilder.append('[').append(id).append(']');
        }

        @Override
        public Type getChildType(FieldNode father) {
            if (type != null) {
                return type;
            }
            try {
                ReferenceType referenceType = ((ReferencedNode) father).getReferenceType();
                return ((ArrayType) referenceType).componentType();
            } catch (ClassNotLoadedException e) {
                throw new RuntimeException(e);
            }
        }

        public RelationByIndex withType(Type type) {
            this.type = type;
            return this;
        }
    }

    public static class RelationByGetter extends Relation {
        private Method getterMethod;

        public RelationByGetter(Method getterMethod) {
            this.getterMethod = getterMethod;
        }

        public String getGetterName() {
            return getterMethod.name();
        }

        @Override
        public void appendToPath(StringBuilder stringBuilder) {
            stringBuilder.append('.').append(getGetterName()).append("()");
        }

        @Override
        public Type getChildType(FieldNode father) {
            try {
                return getterMethod.returnType();
            } catch (ClassNotLoadedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class RelationByMethodCall extends Relation {
        private Method method;
        private String[] arguments;

        public RelationByMethodCall(Method method, String... args) {
            this.method = method;
            this.arguments = args;
        }

        public String getMethodName() {
            return method.name();
        }

        @Override
        public void appendToPath(StringBuilder stringBuilder) {
            stringBuilder.append('.').append(getMethodName())
                    .append("(");

            for (int i = 0; i < arguments.length; i++) {
                if (i > 0) stringBuilder.append(", ");
                stringBuilder.append(arguments[i]);
            }

            stringBuilder.append(")");
        }

        @Override
        public Type getChildType(FieldNode father) {
            try {
                return method.returnType();
            } catch (ClassNotLoadedException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public static class RelationByField extends Relation {
        private Field field;

        public RelationByField(Field field) {
            this.field = field;
        }

        public String getFieldName() {
            return field.name();
        }

        @Override
        public void appendToPath(StringBuilder stringBuilder) {
            stringBuilder.append('.').append(getFieldName());
        }

        @Override
        public Type getChildType(FieldNode father) {
            try {
                return field.type();
            } catch (ClassNotLoadedException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public static class RelationByVirtualField extends Relation {
        private String field;
        private Type type;

        public RelationByVirtualField(String field, Type type) {
            this.field = field;
            this.type = type;
        }

        public String getFieldName() {
            return field;
        }

        @Override
        public void appendToPath(StringBuilder stringBuilder) {
            stringBuilder.append('.').append(getFieldName());
        }

        @Override
        public Type getChildType(FieldNode father) {
            return type;
        }
    }

    public static class CompoundRelation extends Relation {
        private Relation[] relations;

        public CompoundRelation(Relation... relations) {
            this.relations = relations;
        }

        @Override
        public void appendToPath(StringBuilder stringBuilder) {
            for (Relation relation : relations) {
                relation.appendToPath(stringBuilder);
            }
        }

        @Override
        public Type getChildType(FieldNode father) {
            return relations[relations.length-1].getChildType(father);
        }
    }

}
