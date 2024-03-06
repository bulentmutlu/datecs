package com.blk.sdk.olib;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Created by id on 22.03.2018.
 */

class ObjectClear extends ObjectReader {
    @Override
    void PrivmitiveeArray(Object owner, Field f, Class arrayType, Object o, int len) throws Exception {
        if (arrayType == byte.class)
            Arrays.fill((byte[]) o, 0, len, (byte) 0);
        else if (arrayType == short.class)
            Arrays.fill((short[]) o, 0, len, (short) 0);
        else if (arrayType == int.class)
            Arrays.fill((int[]) o, 0, len, (int) 0);
        else if (arrayType == long.class)
            Arrays.fill((long[]) o, 0, len, (long) 0);
        else
            throw new Exception("unhandled type : " + arrayType.getName());
    }

    @Override
    void Primitive(Object owner, Field f, Object o) throws Exception {
        Class<?> c = f.getType();
        if (c == byte.class)
            f.set(owner, (byte)0);
        else if (c == short.class)
            f.set(owner, (short) 0);
        else if (c == int.class)
            f.set(owner, (int) 0);
        else if (c == long.class)
            f.set(owner, (long) 0);
        else
            throw new Exception("unhandled type : " + c.getName());
    }
    public <T> void Clear(T o) throws Exception {
        Traverse(o);
    }
}
