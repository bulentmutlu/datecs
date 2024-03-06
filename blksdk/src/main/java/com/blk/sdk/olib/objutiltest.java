package com.blk.sdk.olib;

import android.database.sqlite.SQLiteDatabase;

import com.blk.sdk.file;
import com.blk.sdk.Utility;

import static com.blk.sdk.c.memcpy;

/**
 * Created by id on 21.03.2018.
 */

public class objutiltest {

    public static class AcqInfo2 {
        public int id;
        public int acq;
        public byte[] b = new byte[2];
    }
    public static class VTerm2 {

        //public AcqInfo2[] ai = new AcqInfo2[2];
        public int id, v;
        public AcqInfo2 ai = new AcqInfo2();
        public AcqInfo2[] aai = new AcqInfo2[3];
        public short[] s = new short[2];

        public VTerm2()
        {
            for (int i = 0; i < aai.length; ++i)
                aai[i] = new AcqInfo2();
        }
    }

    public static void test()
    {
        try {
            VTerm2 v1 = new VTerm2(), v2 = new VTerm2();
            v1.id = 1;
            v1.v = 2;
            v1.ai.id = 3;
            v1.ai.acq = 4;
            v1.ai.b = new byte [] {5, 6};
            v1.aai[0].acq = 7;
            v1.aai[0].b = new byte[] {8, 9};
            v1.aai[1].acq = 10;
            v1.aai[1].b = new byte[] {11, 12};
            v1.aai[2].acq = 13;
            v1.aai[2].b = new byte[] {14, 15};
            v1.s = new short[] {16, 17};


            String table = "AcqInfo2", vTable = "VTERM";
            if (!file.Exist(Utility.dbPath) || !Utility.IsTableExist(Utility.dbPath, table))
            {
                SQLiteDatabase db =  SQLiteDatabase.openOrCreateDatabase(Utility.dbPath, null);
                db.execSQL("CREATE TABLE " + table + " (id INTEGER PRIMARY KEY, acq INTEGER, b BLOB)");
                db.close();
            }
            if (!file.Exist(Utility.dbPath) || !Utility.IsTableExist(Utility.dbPath, vTable))
            {
                SQLiteDatabase db =  SQLiteDatabase.openOrCreateDatabase(Utility.dbPath, null);
                db.execSQL("CREATE TABLE " + vTable + " (id INTEGER PRIMARY KEY, ai INTEGER, v INTEGER, aai BLOB)");
                db.close();
            }

            SQLiteDatabase db = SQLiteDatabase.openDatabase(Utility.dbPath, null, SQLiteDatabase.OPEN_READWRITE);

//            AcqInfo2 ai = new AcqInfo2(), rAi = new AcqInfo2();
//            ai.id = 2;
//            ai.acq = 3;
//            ai.b = new byte[] {1, 2};
//            olib.CreateTableDB(v1, db,"v1", "id");
//            olib.WriteDB(v1, db, "v1");
//            olib.ReadDB(v2, db, "v1", 1);
            boolean e = olib.Equal(v1, v2);

            db.close();

            //Assert.assertTrue(e);





//            VTerm v1 = VTerm.GetVTermPrms();
//            VTerm v2 = new VTerm(), v3 = new VTerm();
            //new ObjectCopy().Copy(v2, v1);
            //new ObjectWriteFile().ToFile(new file(VTerm.PRMFILE), v2);
//            olib.ReadFile(v3, new file(VTerm.PRMFILE));
//            boolean b = new ObjectEqual().Equal(v1, v3);

//            v1.AcqInfos[1].CurList.CurList[1][1] = 1;
//            boolean c = new ObjectEqual().Equal(v1, v2);

//            TranStruct t1 = new TranStruct(), t2 = new TranStruct();
//            t1.MsgTypeId = 1;
//            t2.MsgTypeId = 2;
//            memcpy(t1.DateTime, Rtc.EP_GetDateTime(), t1.DateTime.length);
//            t1.Totals[0].NOffTAmt = 1;
//
//            for (int i = 0; i < t1.FreeFrmtPrntData.length; ++i)
//                memcpy(t1.FreeFrmtPrntData[i], new byte[] {0, 1, 2, 3}, 3);
//
//            int transize = new ObjectSize().Sizeof(t1);
//            new ObjectWriteFile().ToFile(new file("TMP"), t1);
//            int filesize = file.Size("TMP");
//
//            new ObjectReadFile().FromFile(new file("TMP"), t2);
//
            file.Remove("TMP");
//            Long i1 = (long) 5, i2 = (long) 1;
//            new ObjectCopy().Copy(i1, i2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
