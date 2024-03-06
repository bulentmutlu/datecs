package com.blk.sdk.olib;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

class JsonReader extends ObjectDeserialize {

    @Override
    public int getArrayLength(Object encoded, Field f) {
        JSONArray jObject =  (JSONArray) encoded;
        return jObject.length();
    }

    @Override
    public Object getArrayMember(Object encoded, Field f, int i) throws JSONException {
        JSONArray jObject =  (JSONArray) encoded;
        return jObject.get(i);
    }

    @Override
    public Object getCollection(Object encoded, Field f) throws JSONException {
        JSONObject jObject =  (JSONObject) encoded;
        return jObject.getJSONArray(f.getName());
    }

    @Override
    Object getObject(Object encoded, Field f) throws Exception {
        JSONObject jo = (JSONObject) encoded;
        return jo.getJSONObject(f.getName());
    }

    @Override
    boolean processField(Object userObject, Object owner, Field f) throws Exception {
        JSONObject jo = (JSONObject) userObject;

        String name = f.getName();

        if (!jo.has(name)) return true;

        Class<?> c = f.getType();
        if (c == int.class) {
            f.set(owner, jo.getInt(name));
            return true;
        }
        if (c == long.class) {
            f.set(owner, jo.getLong(name));
            return true;
        }
        if (c == double.class) {
            f.set(owner, jo.getDouble(name));
            return true;
        }
        if (c == boolean.class) {
            f.set(owner, jo.getBoolean(name));
            return true;
        }
        if (c == String.class) {
            f.set(owner, jo.getString(name));
            return true;
        }
        if (c == Date.class) {
            String sDate = jo.getString(name);
            sDate = sDate.replace('T', '_').substring(0, 19);
            Date date = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").parse(sDate);
            f.set(owner, date);
            return true;
        }
        return false;
    }
}
