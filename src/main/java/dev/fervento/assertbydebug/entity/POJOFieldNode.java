package dev.fervento.assertbydebug.entity;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import dev.fervento.assertbydebug.serializer.JsonSerializer;

public class POJOFieldNode extends FieldNode implements ReferencedNode {

    private ObjectReference objectReference;

    public POJOFieldNode(FieldNode father, String fieldName, ObjectReference objectReference) {
        super(father, fieldName);
        this.objectReference = objectReference;
    }

    @Override
    public void toJson(JsonSerializer jsonWriter) {
        jsonWriter.startObject(this);
        for (Relation child : getChildren()) {
            child.getFieldNode().toJson(jsonWriter);
        }
        jsonWriter.endObject();
    }

    @Override
    public long getUniqueId() {
        return objectReference.uniqueID();
    }

    @Override
    public ReferenceType getReferenceType() {
        return objectReference.referenceType();
    }
}
