package com.blk.sdk.olib;

import java.lang.reflect.Field;

/**
 * Created by id on 21.03.2018.
 */

class ObjectSize extends ObjectReader {

    int size = 0;
    @Override
    void PrivmitiveeArray(Object owner, Field f, Class arrayType, Object o, int len) throws Exception {
            size += getPrimitiveSize(arrayType) * len;
    }

    @Override
    void Primitive(Object owner, Field f, Object o) throws Exception{
            size += getPrimitiveSize(f.getType());
    }

    int getPrimitiveSize(Class c) throws Exception {
        if (c == byte.class)
            return 1;
        else if (c == short.class)
            return 2;
        else if (c == int.class)
            return 4;
        else if (c == long.class)
            return 8;
        throw new Exception("unhandled type : " + c.getName());
    }

    public <T> int Size(T o) throws Exception {
        Traverse(o);
        return size;
    }
}
