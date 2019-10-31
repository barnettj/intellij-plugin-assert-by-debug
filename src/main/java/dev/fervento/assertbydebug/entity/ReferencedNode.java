package dev.fervento.assertbydebug.entity;

import com.sun.jdi.ReferenceType;

public interface ReferencedNode {
    public long getUniqueId();
    public ReferenceType getReferenceType();
}
