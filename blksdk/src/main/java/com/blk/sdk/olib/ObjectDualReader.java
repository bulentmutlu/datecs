package com.blk.sdk.olib;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Created by id on 21.03.2018.
 */

abstract class ObjectDualReader {
    abstract void PrivmitiveeArray(Object owner1, Object owner2, Field f, Class arrayType, Object o1, Object o2, int len) throws Exception;
    abstract void Primitive(Object owner1, Object owner2, Field f, Object o1, Object o2) throws Exception;

    <T> boolean handleObject(Object owner1, Object owner2, Class c, Field f, Object o1, Object o2) throws Exception {

        if (c.isPrimitive())
        {
            Primitive(owner1, owner2, f, o1, o2);
            //f.set(to, f.get(from));
            return true;
        }
        else if (c.isArray() && c.getComponentType().isPrimitive())
        {
            PrivmitiveeArray(owner1, owner2, f, c.getComponentType(), o1, o2, Array.getLength(o1));
            //ISystem.arraycopy(from, 0, to, 0, Array.getLength(from));
            return true;
        }
        else if (c.isArray())
        {
            Object[] a1 = (Object[]) o1;
            Object[] a2 = (Object[]) o2;

            for (int i = 0; i < a1.length; ++i)
                Traverse(c.getComponentType(), a1[i], a2[i]);
            return true;
        }
        else if (f != null)
        {
            Traverse(c, o1, o2);
            return true;
        }
        return false;
    }

    <T> void Traverse(Class c, T o1, T o2) throws Exception {

        if (handleObject(null, null, c, null, o1, o2))
            return;

        Field[] fields = c.getDeclaredFields();
        for(Field f : fields){
            if (Modifier.isStatic(f.getModifiers()))
                continue;

            String name = f.getName();

            if (name.equals("this$0") || name.equals("$change") || name.equals("serialVersionUID"))
                continue;
            f.setAccessible(true);
            handleObject(o1, o2, f.getType(), f, f.get(o1), f.get(o2));
        }
    }
    public <T> void Traverse(T o1, T o2) throws Exception {
            Traverse(o1.getClass(), o1, o2);
    }
}
