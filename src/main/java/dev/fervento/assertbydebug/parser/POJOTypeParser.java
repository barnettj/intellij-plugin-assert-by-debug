package dev.fervento.assertbydebug.parser;

import com.intellij.debugger.engine.evaluation.EvaluateException;
import com.sun.jdi.*;
import dev.fervento.assertbydebug.BeanParser;
import dev.fervento.assertbydebug.TypeParser;
import dev.fervento.assertbydebug.entity.POJOFieldNode;
import dev.fervento.assertbydebug.entity.FieldNode;

import java.util.Collections;

public class POJOTypeParser implements TypeParser {

    public static final String METHOD_PREFIX_GETTER = "get";

    private BeanParser beanParser;
    private POJOFieldNode pojoFieldNode;
    private FieldNode.Relation relationWithChild;

    public POJOTypeParser(BeanParser beanParser) {
        this.beanParser = beanParser;
    }

    @Override
    public void parse(FieldNode father, FieldNode.Relation relationWithChild, String fieldName, ObjectReference objRef) throws EvaluateException, ClassNotLoadedException {
        this.pojoFieldNode = new POJOFieldNode(father, fieldName, objRef);
        this.relationWithChild = relationWithChild;

        for (Field visibleField : objRef.referenceType().visibleFields()) {
            if (visibleField.isPublic()) {
                Value returnValue = objRef.getValue(visibleField);
                if (returnValue == null || isParsableType(returnValue.type())) {
                    beanParser.parse(pojoFieldNode, new FieldNode.RelationByField(visibleField), visibleField.name(), returnValue);
                }
            }
        }
        for (Method method : objRef.referenceType().visibleMethods()) {
            try {
                if (isMethodAllowed(method)
                        && method.name().startsWith(METHOD_PREFIX_GETTER)
                        && method.argumentTypes().isEmpty()
                        && method.isPublic()) {

                    String attributeName = toAttributeName(method.name());
                    Value returnValue = getReturnValue(objRef, method);
                    if (returnValue == null) {
                        beanParser.parse(pojoFieldNode, new FieldNode.RelationByGetter(method), attributeName, returnValue);
                    } else {

                        /*
                            Subinterfaces of PrimitiveValue
                            a boolean   true	    BooleanValue	BooleanType
                            a byte      (byte)4	    ByteValue	    ByteType
                            a char      'a'	        CharValue	    CharType
                            a double    3.1415926	DoubleValue	    DoubleType
                            a float     2.5f	    FloatValue	    FloatType
                            an int      22	        IntegerValue	IntegerType
                            a long      1024L	    LongValue	    LongType
                            a short     (short)12	ShortValue	    ShortType
                            a void	                VoidValue	    VoidType

                            Subinterfaces of ObjectReference
                            a class instance	this	                ObjectReference	        ClassType
                            an array	        new int[5]	            ArrayReference	        ArrayType
                            a string	        "hello"	                StringReference	        ClassType
                            a thread	        Thread.currentThread()	ThreadReference	        ClassType
                            a thread group	    Thread.currentThread()  ThreadGroupReference	ClassType
                                                    .getThreadGroup()
                            a java.lang.Class   this.getClass()	        ClassObjectReference	ClassType
                            instance
                            a class loader	    this.getClass()         ClassLoaderReference	ClassType
                                                    .getClassLoader()
                            Other
                            null	            null	                null	                Sn/a

                         */

                        Type type = returnValue.type();
                        if (isParsableType(type)) {
                            beanParser.parse(pojoFieldNode, new FieldNode.RelationByGetter(method), attributeName, returnValue);
                            //                        ReferenceType referenceType = (ReferenceType)returnValue;
                            //                        fieldNodeList.add(new FieldNode(path, javaReturnValue));
                            //
                            //                        ValueDescriptorImpl descriptor = javaReturnValue.getDescriptor();
                            //                        if (descriptor.isPrimitive() || descriptor.isNull() || descriptor.isEnumConstant()) {
                            //                            // Nothing to do
                            //
                            //                        } else if (descriptor.isArray()) {
                            //                            // Array
                            //                        } else {
                            //                            // Object
                            //                            parse(javaReturnValue, path);
                            //                        }
                        }
                    }
                }
            } catch (ClassNotLoadedException e) {
                e.printStackTrace();
            }
        }
    }

    private Value getReturnValue(ObjectReference objRef, Method method) throws EvaluateException {
        try {
            return beanParser.getDebugProcess().invokeInstanceMethod(beanParser.getEvaluationContext(),
                    objRef, method, Collections.EMPTY_LIST, 0);
        } catch (EvaluateException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public FieldNode getFieldNode() {
        return pojoFieldNode;
    }

    @Override
    public FieldNode.Relation getRelationWithChild() {
        return relationWithChild;
    }

    private boolean isParsableType(Type type) {
        return type instanceof ReferenceType || type instanceof PrimitiveType || type instanceof ArrayType;
    }

    private static String toAttributeName(String getter) {
        if (getter.startsWith("is")) {
            getter = getter.substring(2);
        } else if (getter.startsWith("get")) {
            getter = getter.substring(3);
        } else {
            return null;
        }
        return getter.substring(0, 1).toLowerCase() + getter.substring(1);
    }

    private boolean isMethodAllowed(Method method) {
        return method.declaringType().name().startsWith("java.lang.") == false;
    }

}
