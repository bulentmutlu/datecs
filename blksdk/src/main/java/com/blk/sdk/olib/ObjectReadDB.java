package com.blk.sdk.olib;

class ObjectReadDB  {

//    SQLiteDatabase db;
//
//    @Override
//    void HandlePrimitiveArray(Object userObject, Object owner, Field f, Class arrayType, Object o, int len) throws Exception {
//        Cursor cursor = (Cursor)userObject;
//        String column = f.getName();
//        int idx = cursor.getColumnIndex(column);
//
//        if (arrayType == byte.class)
//            ISystem.arraycopy(cursor.getBlob(idx), 0, (byte[])o, 0, len);
//        else if (arrayType == short.class)
//            ObjectWriteDB.FromBLOB(cursor.getBlob(idx), (short[]) o);
//        else if (arrayType == int.class)
//            ObjectWriteDB.FromBLOB(cursor.getBlob(idx), (int[]) o);
//        else if (arrayType == long.class)
//            ObjectWriteDB.FromBLOB(cursor.getBlob(idx), (long[]) o);
//        else
//            throw new Exception("unhandled type : " + arrayType.getName());
//    }
//    @Override
//    void HandlePrimitive(Object userObject, Object owner, Field f, Object o) throws Exception{
//        Cursor cursor = (Cursor)userObject;
//        Class<?> c = f.getType();
//        String column = f.getName();
//        int idx = cursor.getColumnIndex(column);
//
//        if (c == byte.class)
//            f.set(owner, (byte) cursor.getInt(idx));
//        else if (c == short.class)
//            f.set(owner, cursor.getShort(idx));
//        else if (c == int.class)
//            f.set(owner, cursor.getInt(idx));
//        else if (c == long.class)
//            f.set(owner, cursor.getLong(idx));
//        else
//            throw new Exception("unhandled type : " + c.getName());
//    }
//    @Override
//    void HandleClass(Object userObject, Field f, Object o) throws Exception {
//        Cursor cursor = (Cursor)userObject;
//        String column = f.getName();
//        int idx = cursor.getColumnIndex(column);
//        long pk = cursor.getInt(idx);
//
//        Traverse(o, new Object[]{f.getType().getSimpleName(), pk});
//    }
//    @Override
//    void HandleClassArray(Object userObject, Field f, Object o) throws Exception {
//        Cursor cursor = (Cursor)userObject;
//        String column = f.getName();
//        int idx = cursor.getColumnIndex(column);
//
//        Object[] oFrom = (Object[]) o;
//        long [] pks = new long[oFrom.length]; // class objects primary keys
//        ObjectWriteDB.FromBLOB(cursor.getBlob(idx), pks);
//
//        for (int i = 0; i < oFrom.length; ++i) {
//            Traverse(oFrom[i], new Object[]{f.getType().getComponentType().getSimpleName(), pks[i]});
//        }
//    }
//
//    @Override
//    <T> void Traverse(T o, Object userObject) throws Exception {
//        String table = (String) ((Object[]) userObject)[0];
//        long primaryKey = (long) ((Object[]) userObject)[1];
//
//        // if primaryKey == 0 get first row
//        Cursor cursor = db.rawQuery("SELECT * FROM " + table + " WHERE id=" + primaryKey,null);
//        if (cursor.getCount() == 0) return;
//        cursor.moveToNext();
//
//        // dump cursor to class o
//        super.Traverse(o, cursor);
//    }
//
//    public <T> void ReadDB(T o, SQLiteDatabase db, String table, long primaryKey) throws Exception {
//        this.db = db;
//        Traverse(o, new Object[] {table, primaryKey});
//    }
}
