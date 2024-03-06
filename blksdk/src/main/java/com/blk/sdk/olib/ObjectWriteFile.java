package com.blk.sdk.olib;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

/**
 * Created by id on 21.03.2018.
 */

class ObjectWriteFile extends ObjectReader {

    com.blk.sdk.file file;
    @Override
    void PrivmitiveeArray(Object owner, Field f, Class arrayType, Object o, int len) throws Exception {
        if (arrayType == byte.class)
            file.Write((byte[]) o);
        else if (arrayType == short.class) {
            for (int i = 0; i < len; ++i)
                file.Write((short) Array.get(o, i));
        }
        else if (arrayType == int.class) {
            for (int i = 0; i < len; ++i)
                file.Write((int) Array.get(o, i));
        }
        else if (arrayType == long.class) {
            for (int i = 0; i < len; ++i)
                file.Write((long) Array.get(o, i));
        }
        else
            throw new Exception("unhandled type : " + arrayType.getName());
    }

    @Override
    void Primitive(Object owner, Field f, Object o) throws Exception{
        Class<?> c = f.getType();
        if (c == byte.class)
            file.Write((byte) o);
        else if (c == short.class)
            file.Write((short) o);
        else if (c == int.class)
            file.Write((int) o);
        else if (c == long.class)
            file.Write((long) o);
        else
            throw new Exception("unhandled type : " + c.getName());
    }

    public <T> void WriteFile(T o, com.blk.sdk.file f) throws Exception {
        file = f;
        Traverse(o);
    }
}
