package dev.fervento.assertbydebug;

import dev.fervento.assertbydebug.entity.FieldNode;
import dev.fervento.assertbydebug.entity.SameInstanceFieldNode;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;

public class SameInstanceFutureSetter {

    final private Map<Long, FieldNode> objectReferenceMap = new HashMap<>();
    final private Map<Long, LinkedList<Consumer<FieldNode>>> workingSet = new HashMap<>();
    final private BeanParser beanParser;

    public SameInstanceFutureSetter(BeanParser beanParser) {
        this.beanParser = beanParser;
    }

    public boolean onStartParsing(FieldNode father, FieldNode.Relation relationWithChild, String fieldName, long objectId) {
        // Se presente
        FieldNode sameInstance = objectReferenceMap.get(objectId);
        if (sameInstance != null) {
            consumer(father, relationWithChild, fieldName).accept(sameInstance);
            return false;
        } else {
            LinkedList<Consumer<FieldNode>> futureList = workingSet.get(objectId);
            if (futureList != null) {
                // Aggiunge future
                futureList.add(consumer(father, relationWithChild, fieldName));
                return false;
            } else {
                workingSet.put(objectId, new LinkedList<>());
                return true;
            }
        }
    }

    private Consumer<FieldNode> consumer(FieldNode father, FieldNode.Relation relationWithChild, String fieldName) {
        return (sameInstance) -> {
            SameInstanceFieldNode sameInstanceFieldNode = new SameInstanceFieldNode(father, fieldName, sameInstance);
            beanParser.addToFather(father, relationWithChild, sameInstanceFieldNode);
        };
    }

    public void onEndParsing(long objectId, FieldNode fieldNode) {
        if (fieldNode != null) {
            workingSet.remove(objectId).forEach(e -> e.accept(fieldNode));
            objectReferenceMap.put(objectId, fieldNode);
        } else {
            // Discard futures
            workingSet.remove(objectId);
        }
    }

}
