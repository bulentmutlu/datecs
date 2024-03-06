package com.blk.sdk.olib;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Created by id on 22.03.2018.
 */

class ObjectEqual extends ObjectDualReader {
    boolean fEqual = true;
    @Override
    void PrivmitiveeArray(Object owner1, Object owner2, Field f, Class arrayType, Object o1, Object o2, int len) throws Exception {

        boolean b = true;
        if (arrayType == byte.class)
            b = Arrays.equals((byte[]) o1, (byte[]) o2);
        else if (arrayType == short.class)
            b = Arrays.equals((short[]) o1, (short[]) o2);
        else if (arrayType == int.class)
            b = Arrays.equals((int[]) o1, (int[]) o2);
        else if (arrayType == long.class)
            b = Arrays.equals((long[]) o1, (long[]) o2);
        else
            throw new Exception("unhandled type : " + arrayType.getName());

        if (b == false) fEqual = false;
    }

    @Override
    void Primitive(Object owner1, Object owner2, Field f, Object o1, Object o2) throws Exception {
        if (!o1.equals(o2))
            fEqual = false;
    }

    public <T> boolean Equal(T o1, T o2) throws Exception {
        Traverse(o1, o2);
        return fEqual;
    }
}
