package com.blk.sdk.olib;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;

abstract class ObjectSerialize {

    abstract void addArrayMember(Object encoded, Class cArrayMember, Object oMember) throws Exception;
    abstract Object newObject(Class c) throws Exception;
    abstract void setObject(Object owner, Object newObject, Field f) throws Exception;
    abstract boolean processField(Object userObject, Object owner, Field f) throws Exception;

    void HandlePrimitiveArray(Object userObject, Object owner, Field f, Class arrayType, Object o, int len) throws Exception {
    }
    void HandleCollection(Object userObject, Object owner, Field f, Class arrayType, Object o) throws Exception {

        String fieldName = (f!=null)? f.getName() : "null";

        Object userCollectionObject = newObject(f.getType());

        Collection collection = (Collection) o;
        Object[] a = collection.toArray();
        for (int i = 0; i < collection.size(); ++i) {

            if (arrayType.isPrimitive() || arrayType == String.class)
                addArrayMember(userCollectionObject, arrayType, a[i]);
            else {
                Object newUserObject = userObject.getClass().newInstance();
                HandleObject(newUserObject, a[i], this.getClass().newInstance());
                addArrayMember(userCollectionObject, arrayType, newUserObject);
            }
        }
        setObject(userObject, userCollectionObject, f);
    }

    <T> void handleField(Object userObject, Field f) throws Exception {

        Class cFielc = f.getType();
        Object oField = f.get(o);

        String fieldName = f.getName();
        if (processField(userObject,  o, f))
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

//            for (int i = 0; i < oFieldArray.length; ++i)
//                HandleObject(oFieldArray[i], getArrayMember(userObject, f, i), this);
        }
        else if (olib.isClassCollection(cFielc))
        {
            HandleCollection(userObject, o, f, olib.getCollectionType(f), oField);
        }
        else
        {
            Object newUserObject = newObject(cFielc);
            HandleObject(newUserObject, oField, this.getClass().newInstance());
            setObject(userObject, newUserObject, f);
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
    public static void HandleObject(Object userObject, Object o, ObjectSerialize d) throws Exception {
        d.o = o;
        d.Traverse(userObject);
    }
}
