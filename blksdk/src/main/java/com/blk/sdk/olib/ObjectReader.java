package com.blk.sdk.olib;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Created by id on 21.03.2018.
 */

abstract class ObjectReader {

    abstract void PrivmitiveeArray(Object owner, Field f, Class arrayType, Object o, int len) throws Exception;
    abstract void Primitive(Object owner, Field f, Object o) throws Exception;

    <T> boolean handleObject(Object owner, Class cObject, Field f, Object o) throws Exception {

        //ISystem.out.println("field : " + ((f == null) ? "null" : f.getName()));
        if (cObject.isPrimitive())
        {
            //f.set(to, f.get(from));
            Primitive(owner, f, o);
            return true;
        }
        else if (cObject.isArray() && cObject.getComponentType().isPrimitive())
        {
            //ISystem.arraycopy(from, 0, to, 0, Array.getLength(from));
            PrivmitiveeArray(owner, f, cObject.getComponentType(), o, Array.getLength(o));
            return true;
        }
        else if (cObject.isArray())
        {
            Object[] oFrom = (Object[]) o;

            for (int i = 0; i < oFrom.length; ++i)
                Traverse(owner, cObject.getComponentType(), oFrom[i]);
            return true;
        }
        else if (f != null)
        {
            Traverse(owner, cObject, o);
            return true;
        }
        return false; // root class and field class[]
    }

    <T> void Traverse(Object owner, Class c, T o) throws Exception {

        if (o == null || handleObject(owner, c, null, o))
            return;

        Field[] fields = c.getDeclaredFields();
        for(Field f : fields){
            if (Modifier.isStatic(f.getModifiers()))
                continue;

            String name = f.getName();

            if (name.equals("this$0") || name.equals("$change") || name.equals("serialVersionUID"))
                continue;
            f.setAccessible(true);

            handleObject(o, f.getType(), f, f.get(o));
        }
    }
    public <T> int Traverse(T o) throws Exception {
        Traverse(null, o.getClass(), o);
        return 0;
    }
}
