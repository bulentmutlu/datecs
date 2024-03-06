package com.blk.techpos;

import static com.blk.sdk.c.memcpy;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import com.blk.sdk.file;
import com.blk.sdk.Utility;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;


public class PrmStruct {
    public static class TcpipPrm
    {
        public String destIp;
        public int destPort;
        public int recvTO;
        public int connTO;

        public TcpipPrm(String destIp, int destPort, int recvTO, int connTO)
        {
            this.destIp = destIp;
            this.destPort = destPort;
            this.recvTO = recvTO;
            this.connTO = connTO;
        }
    }

    private static final String TAG = PrmStruct.class.getSimpleName();
    private static final String  tableName = "Parameters";


    public TcpipPrm			sTcpip = new TcpipPrm("31.145.171.94", 12121, 30000, 10000);
    public TcpipPrm			sBackupTcpip = new TcpipPrm("62.244.244.111", 23232, 30000, 10000);

    //public TcpipPrm			debugTcpip = new TcpipPrm("213.159.6.147", 5050, 10000, 10000);

    public String  MercPwd = "0000";//[5];
    public int		BatchNo = 0;
    public int		TranNo = 0;
    public int		Stan = 0;
    public int BkmParamStatus = 0;
    public int		AcqSelMode;
    public byte[]		DefAcq = new byte[2];
    public int		    LastCommTime;
    public int		DownloadParamsFlag;
    public int		KeyExchangeFlag;
    public byte[]		CommType = new byte[] {2, 0, 0};//[3];
    public int		GprsNetType = 1;
    public String				TCKN = "19484090926"; //[16];
    public String				VN = "0006000625"; //[16];

    public byte[]      IMMK = new byte[16];//Initial Message Master Key
    public byte[]      MSK = new byte[16];//Message Session Key
    public byte[]      HPK = new byte[128];//Host Public Key
    public byte[]      TMK = new byte[16];//Terminal Master Key
    public byte[]      TPK = new byte[16];//Terminal Pin Key
    public byte[]      OKK = new byte[16];//Offline Kart Karýþtýrma

    public int		TranNoLOnT;
    public int		BatchNoLOnT;
    public int		TranNoLOffT;
    public int		BatchNoLOffT;

    public int		OffAdvTryCount;
    public int		LastOffAdvTryTime;

    public int		AutoEodTryCount;
    public int		LastManuelEodTime;
    public int		LastAutoEodTryTime;
    public int		LastAutoEodDay;

    public String				MerchName = "BLK BILGI";//[64];
    public String				TaxpayerName = "BLK BILGI SITEMLERI";//[64];
    public String				MerchAddr1 = "Avrupa Konutlari 1";//[64];
    public String				MerchAddr2 = "Blok 10A, Daire:29";//[64];
    public String				MerchAddr3 = "KucukCekmece / Istanbul";//[64];
    public String				TaxofficeInfo = "Kucukcekmece VD. 19484090926";//[64];

    public byte[]				DefTermId = new byte[8];//[8 + 1];

    public String serialStr;
    public int techposMode; // 1 : D9

//    public static byte[] testIMMK = new byte[] { (byte)0x87, (byte)0xA7, (byte)0xB2, 0x5B, 0x4D, 0x5B, 0x73, (byte)0xA3, 0x69, (byte)0x99, 0x66, (byte)0xC7, 0x77, (byte)0xC9, (byte)0x98, 0x65};
//    public static byte[] testHPK = new byte[] {
//            (byte)0xdb, (byte)0xe5,       0x41,       0x60,       0x07,       0x67, (byte)0x9e, (byte)0xe2,(byte)0x81, (byte)0xb8,       0x09, (byte)0xef, (byte)0xf0, (byte)0xfa,       0x73, 0x42,
//            (byte)0x93,       0x61,       0x50, (byte)0xa1,       0x50, (byte)0x9a, (byte)0xa0, (byte)0xd6,0x6d,             0x12, (byte)0xa3,       0x29, (byte)0xc4, (byte)0xf9, (byte)0x8e, 0x50,
//            (byte)0xee, (byte)0xf0, (byte)0xdb,       0x44, (byte)0x98,       0x36, (byte)0xfe, (byte)0xe5,0x4e,       (byte)0x82, (byte)0xff,       0x3e, (byte)0x9e, (byte)0xcc, (byte)0xfc, (byte)0x9e,
//            (byte)0xe0, (byte)0xf0,       0x66,       0x48,       0x61, (byte)0x87,       0x5c, 0x31,(byte)0x8d,       0x25, (byte)0xd8,       0x59, (byte)0xbe, (byte)0xc3,       0x68, (byte)0x92,
//            0x78,             0x3a, (byte)0x9a,       0x73,       0x3d,       0x0d, (byte)0x94, (byte)0xad, 0x2f,0x7a,       (byte)0xdf,       0x6b,       0x22, (byte)0xe6,       0x73,       0x43,
//            (byte)0xff,       0x40, (byte)0xcd, (byte)0x9f,       0x47, (byte)0xba, (byte)0x8b,       0x70, (byte)0x8a,(byte)0xc5, (byte)0xbd,       0x35, (byte)0xf1, (byte)0xcb,       0x6d, (byte)0x93,
//            0x11,       (byte)0xea, (byte)0xad, (byte)0xce,       0x26, (byte)0xb8,       0x71, (byte)0xf2, (byte)0x88,0x7a,             0x07, (byte)0xa1,       0x16, (byte)0xad,       0x1d,       0x2d,
//            (byte)0x8c, (byte)0xcd, (byte)0xb5, (byte)0xc1, (byte)0xd2, (byte)0xb4,       0x61,       0x37, 0x21,(byte)0x95, (byte)0xe5,       0x7f, (byte)0xb6, (byte)0xc7, (byte)0x9d,       0x7f
//      };
    

    public static byte[] testIMMK = new byte[] {(byte)0x64, (byte)0x88, (byte)0x57, (byte)0x67, (byte)0x86, (byte)0x9A
                    , (byte)0xA3, (byte)0x99, (byte)0xB6, (byte)0xC7, (byte)0x6A, (byte)0x97, (byte)0xC7, (byte)0x6A
                    , (byte)0x96, (byte)0x7B};      //Blk

    public static byte[] testHPK = new byte[] {(byte) 0xE0, (byte) 0x22, (byte) 0x18, (byte) 0xCE, (byte) 0x4B, (byte) 0x96, (byte) 0x4B, (byte) 0xFB, (byte) 0x29, (byte) 0x16, (byte) 0x80, (byte) 0xB8, (byte) 0xA1, (byte) 0x04, (byte) 0xA9, (byte) 0x38,
            (byte) 0x10, (byte) 0x78, (byte) 0x59, (byte) 0x16, (byte) 0xF2, (byte) 0x81, (byte) 0x5B, (byte) 0x9F, (byte) 0x61, (byte) 0x92, (byte) 0x7E, (byte) 0x61, (byte) 0x01, (byte) 0x8F, (byte) 0x23, (byte) 0x1C,
            (byte) 0xE7, (byte) 0xED, (byte) 0xC7, (byte) 0x25, (byte) 0x58, (byte) 0xC0, (byte) 0xAD, (byte) 0x4B, (byte) 0x54, (byte) 0xF6, (byte) 0x48, (byte) 0xF2, (byte) 0xA8, (byte) 0x26, (byte) 0x66, (byte) 0xAD,
            (byte) 0x2A, (byte) 0xC8, (byte) 0x6C, (byte) 0xAD, (byte) 0x34, (byte) 0x60, (byte) 0x08, (byte) 0xEE, (byte) 0xDE, (byte) 0xAA, (byte) 0x02, (byte) 0x4A, (byte) 0x0C, (byte) 0x51, (byte) 0x02, (byte) 0x11,
            (byte) 0x7C, (byte) 0xCC, (byte) 0x0F, (byte) 0x53, (byte) 0xA8, (byte) 0x3C, (byte) 0xCB, (byte) 0x3C, (byte) 0x8C, (byte) 0xEE, (byte) 0x46, (byte) 0xB5, (byte) 0x4D, (byte) 0x62, (byte) 0x8D, (byte) 0x00,
            (byte) 0x21, (byte) 0x94, (byte) 0xEB, (byte) 0x46, (byte) 0x0A, (byte) 0x82, (byte) 0xF8, (byte) 0xA5, (byte) 0xE9, (byte) 0x2D, (byte) 0xC8, (byte) 0x75, (byte) 0x89, (byte) 0xFB, (byte) 0x53, (byte) 0xE1,
            (byte) 0xF5, (byte) 0xE2, (byte) 0xDF, (byte) 0x45, (byte) 0x6C, (byte) 0xCC, (byte) 0x60, (byte) 0x9A, (byte) 0x6C, (byte) 0x75, (byte) 0x54, (byte) 0xF0, (byte) 0x97, (byte) 0xF4, (byte) 0xEB, (byte) 0x41,
            (byte) 0x19, (byte) 0xF9, (byte) 0x62, (byte) 0x55, (byte) 0xF9, (byte) 0x5D, (byte) 0xB8, (byte) 0x7F, (byte) 0xEB, (byte) 0x32, (byte) 0xCB, (byte) 0x44, (byte) 0x44, (byte) 0x39, (byte) 0x28, (byte) 0xAB};

    public static PrmStruct params = new PrmStruct();

    public PrmStruct()
    {
//        byte[] testIMMK = new byte[]
//                {
//                        0x73, (byte)0xD6, 0x59, 0x79, (byte)0x98, (byte)0x88, 0x77, 0x75, 0x69, (byte)0xBD,
//                        (byte)0xA5, (byte)0xB6, 0x74, (byte)0xA3, (byte)0xB9, (byte)0xA8
//                };
//        byte[] testHPK = new byte[]
//                {
//                        (byte)0xD2, 0x07, (byte)0xE4, (byte)0xA8, 0x26, (byte)0xF7, (byte)0x9E, (byte)0xC3, (byte)0xFC, 0x0C, 0x4E, 0x7E, 0x66, 0x60, (byte)0xFF, (byte)0x9B
//                        , 0x69, (byte)0x97, 0x42, 0x3D, 0x71, (byte)0xF0, 0x51, 0x22, 0x61, 0x7B, 0x0F, 0x73, 0x23, (byte)0xAD, (byte)0xC3, (byte)0xA9
//                        , (byte)0xEC, (byte)0xBE, 0x12, (byte)0xA3, (byte)0xA7, 0x17, (byte)0xA4, 0x79, 0x5C, (byte)0xD6, 0x14, (byte)0xBE, 0x33, 0x35, 0x48, (byte)0xEE
//                        , (byte)0xDD, (byte)0xCF, (byte)0xE6, (byte)0x98, (byte)0xAF, (byte)0x94, (byte)0xD9, (byte)0xC8, (byte)0xFF, 0x35, 0x6D, (byte)0xE6, 0x06, (byte)0xFF, 0x67, (byte)0xBF
//                        , 0x76, (byte)0xAE, 0x65, 0x2C, (byte)0xEA, 0x67, 0x79, 0x45, 0x35, 0x27, (byte)0xFC, (byte)0xA7, 0x74, 0x5E, (byte)0xCA, 0x78
//                        , 0x73, (byte)0xCF, 0x0A, 0x66, 0x0D, 0x44, 0x16, 0x6E, 0x6D, (byte)0xBA, 0x67, 0x37, (byte)0xE2, 0x2A, (byte)0xF5, (byte)0x82
//                        , 0x27, 0x06, (byte)0x9E, (byte)0x8A, (byte)0xCB, (byte)0xF6, (byte)0xEF, 0x79, (byte)0xAA, (byte)0xF1, 0x3B, (byte)0xCF, (byte)0xD0, (byte)0xBB, (byte)0x92, 0x7E
//                        , (byte)0x89, (byte)0xA0, 0x10, (byte)0x98, 0x50, (byte)0xD7, 0x02, 0x77, 0x27, (byte)0xE2, 0x62, 0x0E, 0x60, (byte)0xBB, 0x35, 0x73
//                };

        memcpy(IMMK, testIMMK, 16);
        memcpy(HPK, testHPK, 128);
    }


    private static String getParamValue(Object o, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Class c = o.getClass();
        Field f = c.getField(fieldName);
        Class<?> t = f.getType();
        Object fo = f.get(o);

        if (fo == null)
            return "";

        if (t == String.class)
            return (String) fo;
        else if (t.isArray())
            return Base64.encodeToString((byte[]) fo, Base64.DEFAULT);
        else if (t.isPrimitive()) {
//            if (t == Byte.class)
//                return Integer.toString(Convert.unsignedByteToInt((byte)fo));
            return Integer.toString((int) fo);
        }

        Log.e(TAG, "field not handled : " + fieldName);
        return "";
    }
    private String getParamValue(String name) throws NoSuchFieldException, IllegalAccessException {

        Class c = this.getClass();

        if (name.indexOf('.') >= 0) {
            Object o = c.getField(name.substring(0, name.indexOf('.'))).get(this);
            return getParamValue(o, name.substring(name.indexOf('.') + 1));
        } else
            return getParamValue(this, name);
    }
    private static void setParamValue(Object o, String fieldName, String value) throws NoSuchFieldException, IllegalAccessException {
        Class c = o.getClass();
        Field f = c.getField(fieldName);
        Class<?> t = f.getType();
        Object fieldValue = null;

        if (t == String.class)
            fieldValue = value;
        else if (t.isArray())
            fieldValue = Base64.decode(value, Base64.DEFAULT);
        else if (t.isPrimitive())
            fieldValue = Integer.parseInt(value);

        if (fieldValue != null)
            f.set(o, fieldValue);
    }

    private void setParam(String name, String value) throws NoSuchFieldException, IllegalAccessException {
        if (value == null) return;

        Class c = this.getClass();

        if (name.indexOf('.') >= 0)
        {
            Object o = c.getField(name.substring(0, name.indexOf('.'))).get(this);
            setParamValue(o, name.substring(name.indexOf('.') + 1), value);
        }
        else
            setParamValue(this, name, value);
    }
    public static void Read() throws NoSuchFieldException, IllegalAccessException {
        Log.i(TAG, "Read Parameters");

        if (!file.Exist(Utility.dbPath) || !Utility.IsTableExist(Utility.dbPath, tableName))
        {
            SQLiteDatabase db =  SQLiteDatabase.openOrCreateDatabase(Utility.dbPath, null);
            db.execSQL("CREATE TABLE " + tableName + " (Name TEXT PRIMARY KEY, Value TEXT)");
            db.close();
            Save();
        }

        SQLiteDatabase db = SQLiteDatabase.openDatabase(Utility.dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        Cursor cursor = db.rawQuery("SELECT * FROM " + tableName ,null);

        while(cursor.moveToNext()){

            String paramName = cursor.getString(0);
            String paramValue = cursor.getString(1);
            Log.i(TAG, paramName + " = " + paramValue);
            params.setParam(paramName, paramValue);
        }
        db.close();
    }
    public static void Save(String paramName) throws NoSuchFieldException, IllegalAccessException {
        String paramValue = params.getParamValue(paramName);
        SQLiteDatabase db = SQLiteDatabase.openDatabase(Utility.dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        db.execSQL("INSERT OR REPLACE INTO " + tableName + " Values('" + paramName + "', '" + paramValue + "')");
        db.close();
    }
    // without begin - end trans
//04-05 11:34:12.129 7976-7998/com.blk.techpos D/PrmStruct: Save Start
//04-05 11:34:12.505 7976-7998/com.blk.techpos D/PrmStruct: Save End
//04-05 11:34:59.440 7976-7998/com.blk.techpos D/PrmStruct: Save Start
//04-05 11:34:59.810 7976-7998/com.blk.techpos D/PrmStruct: Save End
    // use begin - end trans
//04-05 11:46:30.686 8918-8941/com.blk.techpos D/PrmStruct: Save Start
//04-05 11:46:30.716 8918-8941/com.blk.techpos D/PrmStruct: Save End

    public static void Save() throws NoSuchFieldException, IllegalAccessException {
        SQLiteDatabase db =  SQLiteDatabase.openOrCreateDatabase(Utility.dbPath, null);
        db.beginTransaction();

        //Log.i(TAG, "Save Start");
        try {

            Class c = params.getClass();
            Field[] fields = c.getDeclaredFields();
            for(Field f : fields){
                String name = f.getName();
                String value = "";
                Class<?> t = f.getType();

                if (Modifier.isStatic(f.getModifiers()))
                    continue;

                if (name.equals("this$0") || name.equals("$change") || name.equals("serialVersionUID"))
                    continue;

                if (t.isPrimitive() || (t == String.class) || t.isArray()) {
                    //Log.i(TAG, "FIELD : " + name);

                    value = params.getParamValue(f.getName());

                    //Log.i(TAG, name + " : " + value);
                    db.execSQL("INSERT OR REPLACE INTO "+tableName+" VALUES ('" + name + "','" + value + "')");
                }
                else {
                    Object o = f.get(params);
                    Class oc = o.getClass();
                    for (Field fc : oc.getDeclaredFields())
                    {
                        if (fc.getName().equals("this$0") || fc.getName().equals("$change") || fc.getName().equals("serialVersionUID"))
                            continue;

                        String name2 = name + "." + fc.getName();
                        //Log.i(TAG, "FIELD : " + name2);

                        value = getParamValue(o, fc.getName());

                        //Log.i(TAG, name2 + " : " + value);
                        db.execSQL("INSERT OR REPLACE INTO "+tableName+" VALUES ('" + name2 + "','" + value + "')");
                    }
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
        //Log.i(TAG, "Save End");
    }

    public boolean D9(@Nullable Boolean open) {
        if (open != null) {
            params.techposMode = open ? (byte) 1 : (byte) 0;
            try {
                Save("techposMode");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return params.techposMode ==  1;
    }
}
