package com.blk.techpos.Bkm;



import android.util.Log;

import com.blk.sdk.file;
import com.blk.sdk.Utility;
import com.blk.sdk.olib.olib;
import com.blk.sdk.c;

import static com.blk.techpos.Bkm.TranStruct.EM_QR;
import static com.blk.techpos.Bkm.TranStruct.currentTran;
/**
 * Created by id on 2.03.2018.
 */

public class Reversal {
    private static final String TAG = Reversal.class.getSimpleName();

    static final String RVRSL_FN	= "RVRSL";

    public static void ReverseTran()
    {
        int rv = -1;

        if(currentTran.TranType == TranStruct.T_VOID || currentTran.TranType == TranStruct.T_LOYALTYBONUSINQUIRY || currentTran.EntryMode == EM_QR)
            return;

        RemoveReversalTran();

        Log.i(TAG, "CREATE REVERSAL");
        CreateReversalFile();

        try {
            file fd = new file(RVRSL_FN, file.OpenMode.RDWR);
            fd.Seek(0, file.SeekMode.SEEK_END);
            olib.WriteFile(currentTran, fd); // currentTran.ToFile(fd);
            fd.Close();

            rv = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static TranStruct GetReversalTran()
    {
        if (!file.Exist(RVRSL_FN)) return null;

        try {
            file fd = new file(RVRSL_FN, file.OpenMode.RDONLY);
            fd.Seek(0, file.SeekMode.SEEK_SET);

            TranStruct rec = new TranStruct();
            olib.ReadFile(rec, fd); // TranStruct.FromFile(fd, rec);

            return rec;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void RemoveReversalTran()
    {
        Log.i(TAG, "REMOVE REVERSAL");
        file.Remove(RVRSL_FN);
    }

    public static void PrintReversalTran()
    {
        int idx = 0;
        TranStruct rec;

        if((rec = GetReversalTran()) != null)
        {
            Utility.log("********************************");
            Utility.log("MsgTypeId:%d", rec.MsgTypeId);
            Utility.log("ProcessingCode:%d", rec.ProcessingCode);
            Utility.log("Pan:%s", c.ToString(rec.Pan));
            Utility.log("Amount:%s", c.ToString(rec.Amount));
            Utility.log("ExpDate:%s", c.ToString(rec.ExpDate));
            Utility.log("EntryMode:%d", rec.EntryMode);
            Utility.log("ConditionCode:%02X", rec.ConditionCode);
            Utility.log("AcqId:%02X%02X", rec.AcqId[0], rec.AcqId[1]);
            Utility.log("RRN:%s", c.ToString(rec.RRN));
            Utility.log("AuthCode:%s", c.ToString(rec.AuthCode));
            Utility.log("RspCode:%s", c.ToString(rec.RspCode));
            Utility.log("TermId:%s", c.ToString(rec.TermId));
            Utility.log("MercId:%s", c.ToString(rec.MercId));
            Utility.log("CurrencyCode:%02X%02X", rec.CurrencyCode[0], rec.CurrencyCode[1]);
            Utility.log("Stan:%d", rec.Stan);
        }
    }

    static void CreateReversalFile()
    {
        if (file.Exist(RVRSL_FN)) return;

        try {
            new file(RVRSL_FN, file.OpenMode.RDWR).Close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
