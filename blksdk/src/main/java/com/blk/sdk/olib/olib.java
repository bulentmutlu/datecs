package com.blk.sdk.olib;

import com.blk.sdk.file;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;

/**
 * Created by id on 23.03.2018.
 */

public class olib {

    public static Object GetStaticField(String classPath, String field) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
        Class<?> c = Class.forName(classPath);
        Field f = c.getDeclaredField(field);
        f.setAccessible(true);
        return f.get(null);
    }
    public static boolean isClassCollection(Class<?> c) {
        return Collection.class.isAssignableFrom(c); // || Map.class.isAssignableFrom(c);
    }
    public static Class getCollectionType(Field field)
    {
        ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
        Class<?> c = (Class<?>) stringListType.getActualTypeArguments()[0];
        return c;
    }

    public static <T> int Size(T o) throws Exception {
        return new ObjectSize().Size(o);
    }

    public static <T> void ReadFile(T o, file f) throws Exception {
        new ObjectReadFile().ReadFile(o, f);
    }

    public static <T> void WriteFile(T o, file f) throws Exception {
        new ObjectWriteFile().WriteFile(o, f);
    }

    public static <T> boolean Equal(T o1, T o2) throws Exception {
        return new ObjectEqual().Equal(o1, o2);
    }

    public static <T> void Copy(T dest, T src) throws Exception {
        new ObjectCopy().Copy(dest, src);
    }
    public static <T> void Clear(T o) throws Exception {
        new ObjectClear().Clear(o);
    }
    public static <T> void DeserializeJson(T o, String json) throws Exception {
        JSONObject jObject = new JSONObject(json);
        ObjectDeserialize.HandleObject(jObject, o, new JsonReader());
    }
    public static <T> void DeserializeJson(T o, JSONObject jObject) throws Exception {
        ObjectDeserialize.HandleObject(jObject, o, new JsonReader());
    }
    public static <T> String SerializeJson(T o) throws Exception {
        JSONObject jObject = new JSONObject();
        ObjectSerialize.HandleObject(jObject, o, new JsonWriter());
        return jObject.toString();
    }
    public static <T> void DeserializeXml(T o, String xml) throws Exception {
        XmlReader.r _r = new XmlReader.r(xml, o.getClass().getSimpleName());
        ObjectDeserialize.HandleObject(_r, o, new XmlReader());
    }
    public static <T> String SerializeXml(T o) throws Exception {
        XmlWriter.x _x = new XmlWriter.x(o.getClass().getSimpleName());
        ObjectSerialize.HandleObject(_x, o, new XmlWriter());
        return _x.ToString();
    }
//    public static <T> void WriteDB(T o, SQLiteDatabase db, String table) throws Exception {
//        new ObjectWriteDB().WriteDB(o, db, table);
//    }
//    public static <T> void ReadDB(T o, SQLiteDatabase db, String table, int pk) throws Exception {
//        new ObjectReadDB().ReadDB(o, db, table, pk);
//    }
//    public static <T> void CreateTableDB(T o, SQLiteDatabase db, String table, String pkField) throws Exception {
//        new ObjectCreateTableDB().CreateTableDB(o, db, table, pkField);
//    }

}
