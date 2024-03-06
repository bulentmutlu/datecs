package com.blk.sdk.olib;

class ObjectWriteDB  {

//    SQLiteDatabase db;
//    String primaryKeyColumnName;
//
//    public static byte[] ToBLOB(short[] la)
//    {
//        byte[] b = new byte[la.length * Short.BYTES];
//        for (int i = 0; i < la.length; ++i)
//        {
//            c.memcpy(b,i * Short.BYTES, Convert.ToArray(la[i]), 0, Short.BYTES);
//        }
//        return b;
//    }
//    public static void FromBLOB(byte[] b, short[] la)
//    {
//        for (int i = 0; i < la.length; ++i)
//        {
//            la[i] = Convert.ToShort(b, i * Short.BYTES);
//        }
//    }
//    public static byte[] ToBLOB(int[] la)
//    {
//        byte[] b = new byte[la.length * Integer.BYTES];
//        for (int i = 0; i < la.length; ++i)
//        {
//            c.memcpy(b,i * Integer.BYTES, Convert.ToArray(la[i]), 0, Integer.BYTES);
//        }
//        return b;
//    }
//    public static void FromBLOB(byte[] b, int[] la)
//    {
//        for (int i = 0; i < la.length; ++i)
//        {
//            la[i] = Convert.ToInt(b, i * Integer.BYTES);
//        }
//    }
//    public static byte[] ToBLOB(long[] la)
//    {
//        byte[] b = new byte[la.length * Long.BYTES];
//        for (int i = 0; i < la.length; ++i)
//        {
//            c.memcpy(b,i * Long.BYTES, Convert.ToArray(la[i]), 0, Long.BYTES);
//        }
//        return b;
//    }
//    public static void FromBLOB(byte[] b, long[] la)
//    {
//        for (int i = 0; i < la.length; ++i)
//        {
//            la[i] = Convert.ToLong(b, i * Long.BYTES);
//        }
//    }
//
//    @Override
//    void HandlePrimitiveArray(Object userObject, Object owner, Field f, Class arrayType, Object o, int len) throws Exception {
//        String column = f.getName();
//        ContentValues values = (ContentValues) userObject;
//
//        if (arrayType == byte.class) {
//            values.put(column, (byte[]) o);
//        }
//        else if (arrayType == short.class) {
//            values.put(column, ToBLOB((short[]) o));
//        }
//        else if (arrayType == int.class) {
//            values.put(column, ToBLOB((int[]) o));
//        }
//        else if (arrayType == long.class) {
//            values.put(column, ToBLOB((long[]) o));
//        }
//        else
//            throw new Exception("unhandled type : " + arrayType.getName());
//    }
//    @Override
//    void HandlePrimitive(Object userObject, Object owner, Field f, Object o) throws Exception{
//        Class<?> c = f.getType();
//        String column = f.getName();
//        ContentValues values = (ContentValues) userObject;
//
//        // dont write primaryKey into query if value 0
//        if (column.equals("id") && c == int.class && (int) o == 0 ||
//                column.equals("id") && c == long.class && (long) o == 0)
//            return;
//
//        if (c == byte.class) {
//            values.put(column, (byte) o);
//        }
//        else if (c == short.class) {
//            values.put(column, (short) o);
//        }
//        else if (c == int.class) {
//            values.put(column, (int) o);
//        }
//        else if (c == long.class) {
//            values.put(column, (long) o);
//        }
//        else
//            throw new Exception("unhandled type : " + c.getName());
//    }
//
//    @Override
//    void HandleClass(Object userObject, Field f, Object o) throws Exception {
//        Traverse(o, f.getType().getSimpleName());
//
//        throw  new Exception("Not implemented");
//
////        ContentValues values = (ContentValues) userObject;
////        values.put(f.getName(), GetFieldValue(o, getPrimaryKeyColumnName(db, o.getClass().getName())));
//    }
//    @Override
//    void HandleClassArray(Object userObject, Field f, Object o) throws Exception {
//        Object[] oFrom = (Object[]) o;
//        Object [] pks = new Object[oFrom.length];
//
//        throw  new Exception("Not implemented");
//
////
////        for (int i = 0; i < oFrom.length; ++i) {
////            Traverse(oFrom[i], f.getType().getComponentType().getSimpleName());
////            pks[i] = GetFieldValue(oFrom[i], getPrimaryKeyColumnName(db, oFrom[i].getClass().getName()));
////        }
////        ContentValues values = (ContentValues) userObject;
////        values.put(f.getName(), ToBLOB(pks));
//    }
//    @Override
//    <T> void Traverse(T o, Object userObject) throws Exception {
//        String table = (String) userObject;
//
//        ContentValues values = new ContentValues();
//        if (primaryKeyColumnName == null) {
//            // fill UserObject.values
//            super.Traverse(o, values);
//            db.insert(table, null, values);
//            return;
//        }
//
//        Object primaryKey = GetFieldValue(o, primaryKeyColumnName);
//
//        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + table + " WHERE " +
//                primaryKeyColumnName + "=" + primaryKey.toString() ,null);
//        if (cursor.getCount() == 0)
//            db.insert(table, null, values);
//        else
//            db.replace(table, null, values);
//    }
//
//    String getPrimaryKeyColumnName(SQLiteDatabase db, String tableName)
//    {
//        String query = "select C.COLUMN_NAME FROM  \n" +
//                "INFORMATION_SCHEMA.TABLE_CONSTRAINTS T  \n" +
//                "JOIN INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE C  \n" +
//                "ON C.CONSTRAINT_NAME=T.CONSTRAINT_NAME  \n" +
//                "WHERE  \n" +
//                "C.TABLE_NAME='" + tableName + "'  \n" +
//                "and T.CONSTRAINT_TYPE='PRIMARY KEY'   ";
//
//        Cursor cursor = db.rawQuery(query, null);
//        if (cursor.getCount() == 0) return null;
//        cursor.moveToNext();
//        String pKey = cursor.getString(0);
//        Log.i("owritedb", "Table " + tableName + " pkey " + pKey);
//        return pKey;
//    }
//
//    public <T> void WriteDB(T o, SQLiteDatabase db, String table) throws Exception {
//        this.db = db;
//        this.primaryKeyColumnName = getPrimaryKeyColumnName(db, table);
//        Traverse(o, table);
//    }
}
