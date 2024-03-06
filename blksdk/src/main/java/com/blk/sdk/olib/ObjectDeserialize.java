package com.blk.sdk.olib;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

abstract class ObjectDeserialize {

    abstract int getArrayLength(Object encoded, Field f) throws Exception;
    abstract Object getArrayMember(Object encoded, Field f, int i) throws Exception;
    abstract Object getCollection(Object encoded, Field f) throws Exception;

    abstract Object getObject(Object encoded, Field f) throws Exception;

    abstract boolean processField(Object userObject, Object owner, Field f) throws Exception;


    void HandlePrimitiveArray(Object userObject, Object owner, Field f, Class arrayType, Object o, int len) throws Exception {
    }
    void HandleCollection(Object userObject, Object owner, Field f, Class arrayType, Object o) throws Exception {

        String fieldName = (f!=null)? f.getName() : "null";

        Object userCollectionObject =getCollection(userObject, f);

        Collection collection = (Collection) o;
        for (int i = 0; i < getArrayLength(userCollectionObject, f); ++i) {
            Object userCollectionMember = getArrayMember(userCollectionObject, f, i);

            if (arrayType == String.class) {
                collection.add(userCollectionMember);
                continue;
            }
            Object collectionMember = arrayType.newInstance();
            HandleObject(userCollectionMember, collectionMember, this.getClass().newInstance());
            collection.add(collectionMember);
        }
    }

    <T> void handleField(Object userObject, Field f) throws Exception {

        Class cFielc = f.getType();
        Object oField = f.get(o);

        String fieldName = f.getName();
        if (processField(userObject, o, f))
            return;
//        if (cFielc.isPrimitive() || cFielc == String.class || cFielc == Date.class)
//        {
//            processValue(userObject, o, f);
//        }
//        else
            if (cFielc.isArray() && cFielc.getComponentType().isPrimitive())
        {
            HandlePrimitiveArray(userObject, o, f, cFielc.getComponentType(), oField, Array.getLength(o));
        }
        else if (cFielc.isArray())
        {
            Object[] oFieldArray = (Object[]) oField;

            for (int i = 0; i < oFieldArray.length; ++i)
                HandleObject(oFieldArray[i], getArrayMember(userObject, f, i), this.getClass().newInstance());
        }
        else if (olib.isClassCollection(cFielc))
        {
            HandleCollection(userObject, o, f, olib.getCollectionType(f), oField);
        }
        else
        {
            Object fUserObject = getObject(userObject, f);
            // xmlde gelmemişse
            if (fUserObject == null)
                return;
            HandleObject(fUserObject, oField, this.getClass().newInstance());
        }
    }

    <T> void Traverse(Object userObject) throws Exception {

        Class c = o.getClass();
        Field[] fields = c.getDeclaredFields();
        for(Field f : fields){
            if (Modifier.isStatic(f.getModifiers()))
                continue;

            String name = f.getName();

            if (name.equals("this$0") || name.equals("$change") || name.equals("serialVersionUID"))
                continue;
            f.setAccessible(true);

            try {
                handleField(userObject, f);
            } catch (Exception ex)
            {
                ex.printStackTrace();
                System.out.println("field type : " + f.getType() + " name : " + name);
                throw ex;
            }
        }
    }

    Object o;
    public static void HandleObject(Object userObject, Object o, ObjectDeserialize d) throws Exception {
        d.o = o;
        d.Traverse(userObject);
    }
}
