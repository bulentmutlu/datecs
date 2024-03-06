package com.blk.sdk.olib;

import android.database.sqlite.SQLiteDatabase;

import com.blk.sdk.Utility;

import java.lang.reflect.Field;

class ObjectCreateTableDB
//        extends ObjectDeserialize
{
//    String query, pk;
//
//    @Override
//    void HandlePrimitiveArray(Object userObject, Object owner, Field f, Class arrayType, Object o, int len) throws Exception {
//        query += ", " + f.getName() + " BLOB";
//    }
//
//    @Override
//    void HandlePrimitive(Object userObject, Object owner, Field f, Object o) throws Exception {
//        if (f.getName().equals(pk)) return; // dont add pk again.
//
//        query += ", " + f.getName() + " INTEGER";
//    }
//
//    @Override
//    void HandleClass(Object userObject, Field f, Object o) throws Exception {
//        query += ", " + f.getName() + " INTEGER"; // holds primary key for class members
//    }
//
//    @Override
//    void HandleClassArray(Object userObject, Field f, Object o) throws Exception {
//        query += ", " + f.getName() + " BLOB"; // holds primary key array for class array members
//    }
//
//    public <T> void CreateTableDB(T o, SQLiteDatabase db, String table, String pk) throws Exception {
//        if (Utility.IsTableExist(db, table)) return;
//
//        query = "CREATE TABLE " + table + " (" + pk + " INTEGER PRIMARY KEY";
//        this.pk = pk;
//        Traverse(o, null);
//        query += ")";
//
//        db.execSQL(query);
//    }
}
