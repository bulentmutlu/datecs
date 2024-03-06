package com.blk.sdk;

import java.util.HashMap;

public class Iso8583 {

    private static final String TAG = Iso8583.class.getSimpleName();

    public static final int kDL_ISO8583_MAX_FIELD_IDX                = 128;
    public long handle;

    public Iso8583()
    {
        handle = newHandle();
    }
    public void Free() {
        freeHandle(handle);
        handle = 0;
    }
    public void setFieldValue(String field, String value)
    {
        setField(handle, Integer.parseInt(field), value);
    }
    public void setFieldValue(String field, byte[] value)
    {
        setField(handle, Integer.parseInt(field), new String(value));
    }
    public void setFieldBin(String field, byte[] value)
    {
        setFieldBin(handle, Integer.parseInt(field), value, value.length);
    }
    public byte[] pack() throws Exception {
        //String msg = "080020380000002000028100000000051405050321454441544130303032373931303030304452503930303230363932343837303030303030303030300011010008B6FCE1DAD8F16FDE";
        //return Convert.Hex2Buffer(msg.getBytes(), 0, msg.length());
        byte[] packed = pack(handle);
        if (packed == null)
            throw new Exception("Iso pack error");
        return packed;
    }
    public static HashMap<String, byte[]> unpack(byte[] packed, boolean b)
    {
        HashMap<String, byte[]> fields = new HashMap<String, byte[]>();

        Iso8583 iso = new Iso8583();
        try {
            unpack(iso.handle, packed, packed.length);
            iso.Dump();

            for (int i = 0; i < kDL_ISO8583_MAX_FIELD_IDX; ++i) {
                byte[] value = null;
                if (i == 48 || i == 63 || i == 55 || i == 62)
                    value = iso.getFieldBin(iso.handle, i);
                 else
                     value = iso.getFieldString(iso.handle, i);

                if (value != null) {
                    fields.put("" + i, value);
                }
            }
        }
        finally {
            iso.Free();
        }
        return fields;
    }
    public void Dump()
    {
        dump(handle);
//        HashMap<Integer, String> fields = Dump(handle);
//
//        for(Map.Entry m : fields.entrySet()){
//            ISystem.out.println(m.getKey()+" "+m.getValue());
//            Log.i(TAG, "F[" + m.getKey() + "] : " + m.getValue());
//        }
    }

//    public static native int HelloNdk();
    static native long newHandle();
    static native void freeHandle(long handle);

    native void setField(long handle, int field, String value);
    native void setFieldBin(long handle, int field, byte[] value, int length);
    native byte[] getFieldString(long handle, int field);
    native byte[] getFieldBin(long handle, int field);

    native HashMap<Integer, String> dump(long handle);
    native byte[] pack(long handle);
    static native void unpack(long handle, byte[] isoData, int length);
    //public native HashMap<String, byte[]> unpack(long handle, byte[] isoData);
}
