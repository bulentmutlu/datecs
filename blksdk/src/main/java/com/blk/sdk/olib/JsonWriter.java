package com.blk.sdk.olib;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

class JsonWriter extends ObjectSerialize {

    @Override
    void addArrayMember(Object encoded, Class cArrayMember, Object oMember) throws Exception
    {
        JSONArray ja = (JSONArray) encoded;

        if (cArrayMember == int.class)
            ja.put((int) oMember);
        else if (cArrayMember == String.class)
            ja.put((String) oMember);
        else
            ja.put(oMember);
    }

    @Override
    Object newObject(Class c) throws Exception {
        if (olib.isClassCollection(c))
            return new JSONArray();
        return new JSONObject();
    }

    @Override
    void setObject(Object owner, Object newObject, Field f) throws Exception {
        JSONObject jo = (JSONObject) owner;
        jo.put(f.getName(), newObject);
    }

    @Override
    boolean processField(Object userObject, Object owner, Field f) throws Exception {
        JSONObject jo = (JSONObject) userObject;
        Class<?> c = f.getType();
        Object value = f.get(owner);
        String name = f.getName();

        if (c == int.class) {
            jo.put(name, (int) value);
            return true;
        }
        if (c == long.class) {
            jo.put(name, (long) value);
            return true;
        }
        if (c == double.class) {
            jo.put(name, (double) value);
            return true;
        }
        if (c == boolean.class) {
            jo.put(name, (boolean) value);
            return true;
        }
        if (c == String.class) {
            jo.put(name, (String) value);
            return true;
        }
        if (c == Date.class) {
            String sDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) f.get(owner));
            jo.put(name, sDate);
            return true;
        }
        return false;
    }

}