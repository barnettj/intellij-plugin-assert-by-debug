package dev.fervento.assertbydebug.serializer;

import com.google.common.collect.HashMultimap;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Type;
import dev.fervento.assertbydebug.ParserUtils;
import dev.fervento.assertbydebug.entity.FieldNode;
import dev.fervento.assertbydebug.entity.ReferencedNode;

import java.util.*;

public class CodeScope implements CodeBlock {
    private static final String CHILD_INDENTATION = "   ";
    private CodeScope father;
    private LinkedList<CodeBlock> sections = new LinkedList<>();
    private Map<String, Integer> varnameCounter = new HashMap<>();
    private HashMultimap<Long, String> varnameById = HashMultimap.create();

    public CodeScope(CodeScope father) {
        this.father = father;
        newCodeSnippetSection();
    }

    private void newCodeSnippetSection() {
        this.sections.add(new CodeSnippet());
    }

    public CodeScope getFather() {
        return father;
    }

    @Override
    public void toCode(StringBuilder stringBuilder, String indentation) {
        List<String> code = new ArrayList<>();
        for (CodeBlock section : sections) {
            boolean isScope = section instanceof CodeScope;
            String childIndentation;
            if (isScope) {
                stringBuilder.append(indentation).append("{\n");
                childIndentation = indentation + CHILD_INDENTATION;
            } else {
                childIndentation = indentation;
            }
            section.toCode(stringBuilder, childIndentation);
            if (isScope) {
                stringBuilder.append(indentation).append("}\n");
            }
        }
    }

    public Set<String> getVarName(ReferencedNode referencedNode) {
        CodeScope codeScope = this;
        while (true) {
            Set<String> values = codeScope.varnameById.get(referencedNode.getUniqueId());
            if (values != null) {
                return values;
            } else {
                if (codeScope.father != null) {
                    codeScope = codeScope.father;
                } else {
                    return Collections.EMPTY_SET;
                }
            }
        }
    }

    public String getOrCreateVarName(ReferencedNode referencedNode, String varname) {
        Set<String> varNameSet = getVarName(referencedNode);
        if (varNameSet.isEmpty()) {
            return newVarName(referencedNode, varname);
        }
        return varNameSet.iterator().next();
    }

    public String newVarName(ReferencedNode referencedNode, String varname) {
        Integer id = varnameCounter.get(varname);
        if (id == null) {
            varnameCounter.put(varname, 1);
        } else {
            id = id + 1;
            varnameCounter.put(varname, id);
            varname = varname + "_" + id;
        }

        if (referencedNode != null) {
            varnameById.put(referencedNode.getUniqueId(), varname);
        }
        return varname;
    }

    @Override
    public void writeLine(String line) {
        sections.getLast().writeLine(line);
    }

    public CodeScope newInnerCodeScope() {
        CodeScope innerCodeScope = new CodeScope(this);
        sections.addLast(innerCodeScope);
        return innerCodeScope;
    }

    public void closeInnerCodeScope() {
        if (sections.getLast() instanceof CodeScope) {
            //sections.removeLast();
            newCodeSnippetSection();
        }
    }

    public String getPath(FieldNode fieldNode) {
        return getPath(fieldNode, false);
    }

    public String resolveVarName(FieldNode fieldNode) {
        return getPath(fieldNode, true);
    }

    private String getPath(FieldNode fieldNode, boolean resolveVarnames) {
//        if (father == null) {
//            return getFieldName();
//        } else {
//            String toFather = father.getFullPath();
//            return fatherChildRelation.appendToPath(toFather);
//        }

        StringBuilder stringBuilder = new StringBuilder();
        Iterator<FieldNode> it = fieldNode.getFieldNodeTree().iterator();

        FieldNode root = it.next();
        stringBuilder.append(root.getFieldName());
        while (it.hasNext()) {
            FieldNode child = it.next();
            if (resolveVarnames && child instanceof ReferencedNode) {
                Set<String> varName = getVarName((ReferencedNode) child);
                if (varName != null && varName.isEmpty() == false) {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append(varName.iterator().next());
                    continue;
                }
            }
            child.getFatherChildRelation().appendToPath(stringBuilder);
            appendCastingIfRequired(stringBuilder, child);
        }
        return stringBuilder.toString();
    }

    private void appendCastingIfRequired(StringBuilder stringBuilder, FieldNode child) {
        if (child instanceof ReferencedNode) {
            ReferenceType requiredChildType = ((ReferencedNode) child).getReferenceType();
            Type childTypeByFather = child.getFatherChildRelation().getChildType(child.getFather());

            if (childTypeByFather.equals(requiredChildType) == false) {
                stringBuilder
                        .insert(0, ParserUtils.format("((%s)",
                            ParserUtils.JNITypeResolver.toJavaName(requiredChildType.name())))
                        .append(")");
            }
        }
    }

}
