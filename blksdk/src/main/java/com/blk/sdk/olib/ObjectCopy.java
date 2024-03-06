package com.blk.sdk.olib;

import java.lang.reflect.Field;

/**
 * Created by id on 21.03.2018.
 */

class ObjectCopy extends ObjectDualReader {
    @Override
    void PrivmitiveeArray(Object owner1, Object owner2, Field f, Class arrayType, Object dest, Object src, int len) throws Exception {
        System.arraycopy(src, 0, dest, 0, len);
    }

    @Override
    void Primitive(Object ownerSrc, Object ownerDest, Field f, Object dest, Object src) throws Exception {
        f.set(ownerSrc, f.get(ownerDest));
    }
    public <T> void Copy(T dest, T src) throws Exception {
        Traverse(dest, src);
    }

//    <T> boolean setObject(Class c, Field f, Object from, Object to) throws IllegalAccessException {
//
//        if (c.isArray() && c.getComponentType().isPrimitive())
//        {
//            ISystem.arraycopy(from, 0, to, 0, Array.getLength(from));
//            return true;
//        }
//        else if (c.isArray())
//        {
//            Object[] oFrom = (Object[]) from;
//            Object[] oTo = (Object[]) to;
//
//            for (int i = 0; i < oFrom.length; ++i)
//                Copy(c.getComponentType(), oFrom[i], oTo[i]);
//            return true;
//        }
//        else if (!c.isPrimitive() &&  f != null)
//        {
//            Copy(c, from, to);
//            return true;
//        }
//        else if (f != null)
//        {
//            throw new IllegalAccessException("Unhandled field c(" + c.getName() + ") f(" + f.getName() + ")");
//        }
//        return false;
//    }
//
//    <T> void Copy(Class c, T from, T to) throws IllegalAccessException {
//
//        if (setObject(c, null, from, to))
//            return;
//
//        Field[] fields = c.getDeclaredFields();
//        for(Field f : fields){
//            if (Modifier.isStatic(f.getModifiers()))
//                continue;
//
//            String name = f.getName();
//
//            if (name.equals("this$0") || name.equals("$change") || name.equals("serialVersionUID"))
//                continue;
//            f.setAccessible(true);
//
//            if (f.getType().isPrimitive())
//            {
//                f.set(to, f.get(from));
//                continue;
//            }
//            setObject(f.getType(), f, f.get(from), f.get(to));
//        }
//    }
//    public <T> void Copy(T from, T to) throws IllegalAccessException {
//            Copy(from.getClass(), from, to);
//    }
}
