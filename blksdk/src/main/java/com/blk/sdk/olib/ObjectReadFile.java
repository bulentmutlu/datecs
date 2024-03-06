package com.blk.sdk.olib;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

/**
 * Created by id on 21.03.2018.
 */

class ObjectReadFile extends ObjectReader {
    com.blk.sdk.file file;

    @Override
    void PrivmitiveeArray(Object owner, Field f, Class arrayType, Object o, int len) throws Exception {
        if (arrayType == byte.class)
            file.Read((byte[]) o);
        else if (arrayType == short.class) {
            for (int i = 0; i < len; ++i)
                Array.set(o, i, file.ReadShort());
        }
        else if (arrayType == int.class) {
            for (int i = 0; i < len; ++i)
                Array.set(o, i, file.ReadInt());
        }
        else if (arrayType == long.class) {
            for (int i = 0; i < len; ++i)
                Array.set(o, i, file.ReadLong());
        }
        else
            throw new Exception("unhandled type : " + arrayType.getName());
    }

    @Override
    void Primitive(Object owner, Field f, Object o) throws Exception{
        Class<?> c = f.getType();
        if (c == byte.class)
            f.set(owner, file.ReadByte());
        else if (c == short.class)
            f.set(owner, file.ReadShort());
        else if (c == int.class)
            f.set(owner, file.ReadInt());
        else if (c == long.class)
            f.set(owner, file.ReadLong());
        else
            throw new Exception("unhandled type : " + c.getName());
    }
    public <T> void ReadFile(T o, com.blk.sdk.file f) throws Exception {
        file = f;
        Traverse(o);
    }
}
