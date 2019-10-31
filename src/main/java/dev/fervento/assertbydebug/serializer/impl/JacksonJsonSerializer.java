package dev.fervento.assertbydebug.serializer.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import dev.fervento.assertbydebug.entity.ArrayFieldNode;
import dev.fervento.assertbydebug.entity.FieldNode;
import dev.fervento.assertbydebug.entity.ReferencedNode;
import dev.fervento.assertbydebug.entity.SameInstanceFieldNode;
import dev.fervento.assertbydebug.parser.CollectionTypeParser;
import dev.fervento.assertbydebug.serializer.CodeGenerationContext;
import dev.fervento.assertbydebug.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class JacksonJsonSerializer implements JsonSerializer {

    public static final String FIELD_JSOG_ID = "@id";
    public static final String FIELD_JSOG_REF = "@ref";
    private final ObjectMapper objectMapper;
    private final LinkedList<JsonNode> stack = new LinkedList<>();
    private final JsonNode rootNode;
    private final Map<Long, JsonNode> jsonNodesById = new HashMap<>();
    private final CodeGenerationContext codeGenerationContext = new CodeGenerationContext();
    private boolean jsog;

    public JacksonJsonSerializer(boolean jsog) {
        this.jsog = jsog;
        objectMapper = new ObjectMapper();
        rootNode = objectMapper.createObjectNode();
        push(rootNode);
    }

    private void push(JsonNode node) {
        stack.addLast(node);
    }

    private <T extends JsonNode> T pop() {
        return (T)stack.removeLast();
    }

    private JsonNode top() {
        return stack.getLast();
    }

    private boolean isTopArrayNode() {
        return top() instanceof ArrayNode;
    }

    private boolean isTopObjectNode() {
        return top() instanceof ObjectNode;
    }

    private ArrayNode getTopAsArrayNode() {
        return (ArrayNode)top();
    }

    private ObjectNode getTopAsObjectNode() {
        return (ObjectNode)top();
    }

    @Override
    public CodeGenerationContext getCodeGenerationContext() {
        return codeGenerationContext;
    }

    @Override
    public void startArray(FieldNode fieldNode) {
        ArrayNode arrayNode = null;
        if (isTopArrayNode()) {
            arrayNode = getTopAsArrayNode().addArray();
        } else if (isTopObjectNode()) {
            arrayNode = getTopAsObjectNode().putArray(fieldNode.getFieldName());
        } else {
            throwInvalidTopType();
        }

        long uniqueId = ((ReferencedNode) fieldNode).getUniqueId();
        jsonNodesById.put(uniqueId, arrayNode);
        push(arrayNode);
    }

    private void throwInvalidTopType() {
        throw new RuntimeException("not expected type: " + top().getClass().getCanonicalName());
    }

    @Override
    public void endArray() {
        pop();
    }

    @Override
    public void startObject(FieldNode fieldNode) {
        ObjectNode objectNode = null;
        if (isTopArrayNode()) {
            objectNode = getTopAsArrayNode().addObject();
        } else if (isTopObjectNode()) {
            objectNode = getTopAsObjectNode().putObject(fieldNode.getFieldName());
        } else {
            throwInvalidTopType();
        }

        long uniqueId = ((ReferencedNode) fieldNode).getUniqueId();
        if (isJsog()) {
            objectNode.put(FIELD_JSOG_ID, String.valueOf(uniqueId));
        }
        jsonNodesById.put(uniqueId, objectNode);
        push(objectNode);
    }

    @Override
    public void endObject() {
        pop();
    }

    @Override
    public void writeValueNull(FieldNode fieldNode) {
        if (isTopArrayNode()) {
            getTopAsArrayNode().addNull();
        } else if (isTopObjectNode()) {
            getTopAsObjectNode().putNull(fieldNode.getFieldName());
        } else {
            throwInvalidTopType();
        }
    }

    @Override
    public void writeBoolean(FieldNode fieldNode, boolean value) {
        if (isTopArrayNode()) {
            getTopAsArrayNode().add(value);
        } else if (isTopObjectNode()) {
            getTopAsObjectNode().put(fieldNode.getFieldName(), value);
        } else {
            throwInvalidTopType();
        }
    }

    @Override
    public void writeLong(FieldNode fieldNode, long longValue) {
        if (isTopArrayNode()) {
            getTopAsArrayNode().add(longValue);
        } else if (isTopObjectNode()) {
            getTopAsObjectNode().put(fieldNode.getFieldName(), longValue);
        } else {
            throwInvalidTopType();
        }
    }

    @Override
    public void writeChar(FieldNode fieldNode, char value) {
        if (isTopArrayNode()) {
            getTopAsArrayNode().add(value);
        } else if (isTopObjectNode()) {
            getTopAsObjectNode().put(fieldNode.getFieldName(), value);
        } else {
            throwInvalidTopType();
        }
    }

    @Override
    public void writeDouble(FieldNode fieldNode, double doubleValue) {
        if (isTopArrayNode()) {
            getTopAsArrayNode().add(doubleValue);
        } else if (isTopObjectNode()) {
            getTopAsObjectNode().put(fieldNode.getFieldName(), doubleValue);
        } else {
            throwInvalidTopType();
        }
    }

    @Override
    public void writeString(FieldNode fieldNode, String value) {
        if (isTopArrayNode()) {
            getTopAsArrayNode().add(value);
        } else if (isTopObjectNode()) {
            getTopAsObjectNode().put(fieldNode.getFieldName(), value);
        } else {
            throwInvalidTopType();
        }
    }

    @Override
    public void sameObject(SameInstanceFieldNode sameInstanceFieldNode) {
        //boolean isPojo = isWithFieldName(sameInstanceFieldNode);
        ObjectNode objNode = null;

        if (isJsogType(sameInstanceFieldNode.getOther()) && isJsog()) {
            // Deve aggiungere REF
            if (isTopArrayNode()) {
                objNode = getTopAsArrayNode().addObject();
            } else if (isTopObjectNode()) {
                objNode = getTopAsObjectNode().putObject(sameInstanceFieldNode.getFieldName());
            } else {
                throwInvalidTopType();
            }

            objNode.put(FIELD_JSOG_REF, String.valueOf(sameInstanceFieldNode.getUniqueId()));
        } else {
            // Deve sostituire, se possibile
            JsonNode previousValue = jsonNodesById.get(sameInstanceFieldNode.getUniqueId()) ;
            if (previousValue == null) {
                throw new RuntimeException("Expected values already in the map!");
            }

            if (isTopArrayNode()) {
                getTopAsArrayNode().add(previousValue);
            } else if (isTopObjectNode()) {
                getTopAsObjectNode().replace(sameInstanceFieldNode.getFieldName(), previousValue);
            } else {
                throwInvalidTopType();
            }
        }
    }

    private boolean isWithFieldName(SameInstanceFieldNode sameInstanceFieldNode) {
        return Strings.isNullOrEmpty(sameInstanceFieldNode.getFieldName()) == false;
    }

    private boolean isJsogType(FieldNode other) {
        return !(other instanceof ArrayFieldNode) &&
                !(other instanceof CollectionTypeParser.CollectionFieldNode);
    }

    private boolean isJsog() {
        return jsog;
    }

    public String toJsonString() {
        try {
            return objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(rootNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
//        return rootNode.toString();
    }

}
