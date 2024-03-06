package com.blk.techpos.Bkm.Messages;

import static com.blk.sdk.Convert.EP_ascii2bcd;
import static com.blk.sdk.c.memcmp;
import static com.blk.sdk.c.memcpy;
import static com.blk.sdk.c.sprintf;
import static com.blk.sdk.c.strlen;



import android.util.Log;

import com.blk.sdk.Rtc;
import com.blk.sdk.UI;
import com.blk.sdk.Iso8583;
import com.blk.techpos.Bkm.Batch;
import com.blk.techpos.Bkm.BatchRec;
import com.blk.techpos.PrmStruct;
import com.blk.techpos.Bkm.TranStruct;

import java.util.Arrays;
import java.util.HashMap;
import static com.blk.techpos.PrmStruct.params;
import static com.blk.techpos.Bkm.TranStruct.currentTran;
/**
 * Created by id on 14.03.2018.
 */

public class BatchUpload {
    private static final String TAG = BatchUpload.class.getSimpleName();

    public static int ProcessBatchUpload() throws Exception {
        int rv = 0, rv1 = 0, idx = 0;

        do
        {
            BatchRec rec = new BatchRec();
            rv = Batch.GetTran(rec, idx);
            idx++;
            if(rv > 0)
            {
                if(
                        (
                                rec.MsgTypeId == 210 ||
                                        (rec.MsgTypeId == 230 && rec.ProcessingCode != 950000) ||
                                        (rec.MsgTypeId == 110 && (rec.ProcessingCode == 300000 || rec.ProcessingCode == 320000))
                        )
                                && !TranStruct.IsReverse(rec.ProcessingCode) && memcmp(rec.RspCode, "Z1".getBytes(), 2) && memcmp(rec.RspCode, "Z3".getBytes(), 2)
                        )
                {
                    TranStruct.ClearTranData();
                    memcpy(currentTran.DateTime, Rtc.GetDateTime(), currentTran.DateTime.length);

                    currentTran.MsgTypeId = 320;
                    memcpy(currentTran.Pan, rec.Pan, rec.Pan.length);
                    currentTran.ProcessingCode = rec.ProcessingCode;
                    currentTran.Stan = rec.Stan;
                    currentTran.TranNo = rec.TranNo;
                    currentTran.TranNoLOnT = rec.TranNoLOnT;
                    currentTran.BatchNoLOnT = rec.BatchNoLOnT;
                    currentTran.TranNoLOffT = rec.TranNoLOffT;
                    currentTran.BatchNoLOffT = rec.BatchNoLOffT;
                    memcpy(currentTran.Amount, rec.Amount, rec.Amount.length);
                    memcpy(currentTran.ExpDate, rec.ExpDate, rec.ExpDate.length);
                    currentTran.EntryMode = rec.EntryMode;
                    currentTran.ConditionCode = rec.ConditionCode;
                    memcpy(currentTran.AcqId, rec.AcqId, 2);
                    memcpy(currentTran.RRN, rec.RRN, rec.RRN.length);
                    memcpy(currentTran.AuthCode, rec.AuthCode, rec.AuthCode.length);
                    memcpy(currentTran.RspCode, rec.RspCode, rec.RspCode.length);
                    memcpy(currentTran.TermId, rec.TermId, rec.TermId.length);
                    memcpy(currentTran.MercId, rec.MercId, rec.MercId.length);
                    memcpy(currentTran.CurrencyCode, rec.CurrencyCode, rec.CurrencyCode.length);

                    Log.i(TAG, "BatchUpload Started");
                    rv1 = Msgs.ProcessMsg(Msgs.MessageType.M_BATCHUPLOAD);
                    if (rv1 == 0 && !currentTran.f39OK()) {
                        rv1 = -1;
                    }
                    Log.i(TAG, "BatchUpload Ended:" + rv1);

                    TranStruct.ClearTranData();
                }
            }
        }while(rv > 0);


        Log.i(TAG, "ProcessEndOfDay Started");

        TranStruct.ClearTranData();

        currentTran.MsgTypeId = 500;
        currentTran.ProcessingCode = 920000;
        Batch.CalcBatchTotals();

        rv = Msgs.ProcessMsg(Msgs.MessageType.M_ENDOFDAY);
        if (rv == 0 && !currentTran.f39OK()) {
            rv = -1;
        }
        if(rv == 0)
        {
            UI.ShowMessage(2000, "İŞLEM BAŞARILI");
        }
        else
        {
            UI.ShowMessage(2000, "İŞLEM BAŞARISIZ\nTEKRAR DENEYİNİZ");
        }

        PrmStruct.Save();

        Log.i(TAG, "ProcessEndOfDay Ended:" + rv);

        return rv;
    }


    static int PrepareBatchUploadMsg(Iso8583 isoMsg) throws Exception {
        int rv = 0;
        byte[] tmpStr = new byte[64];
        byte[] buff = new byte[1024];
        int buffIdx = 0;

        isoMsg.setFieldValue("2", Arrays.copyOf(currentTran.Pan, strlen(currentTran.Pan)));
        isoMsg.setFieldValue("4", currentTran.Amount);
        isoMsg.setFieldValue("14", currentTran.ExpDate);

        sprintf(tmpStr, "%03X1", currentTran.EntryMode);
        isoMsg.setFieldValue("22", Arrays.copyOf(tmpStr, strlen(tmpStr)));

        sprintf(tmpStr, "%02X", currentTran.ConditionCode);
        isoMsg.setFieldValue("25", Arrays.copyOf(tmpStr, strlen(tmpStr)));

        sprintf(tmpStr, "%02X%02X", currentTran.AcqId[0], currentTran.AcqId[1]);
        isoMsg.setFieldValue("32", Arrays.copyOf(tmpStr, strlen(tmpStr)));


        isoMsg.setFieldValue("37", currentTran.RRN);
        isoMsg.setFieldValue("38", currentTran.AuthCode);
        isoMsg.setFieldValue("39", currentTran.RspCode);
        isoMsg.setFieldValue("41", currentTran.TermId);
        isoMsg.setFieldValue("42", currentTran.MercId);


        sprintf(tmpStr, "%02X%02X", currentTran.CurrencyCode[0], currentTran.CurrencyCode[1]);
        isoMsg.setFieldValue("49", Arrays.copyOf(tmpStr, strlen(tmpStr)));


        //Build Field 63
        buffIdx = 0;
        buff[buffIdx++] = 0x0C;
        memcpy(buff, buffIdx, new byte[] {0x00, 0x12}, 0, 2);
        buffIdx += 2;
        sprintf(tmpStr, "%06d", params.BatchNo);
        EP_ascii2bcd(buff, buffIdx, tmpStr, 6);
        buffIdx += 3;
        sprintf(tmpStr, "%06d", currentTran.TranNo);
        EP_ascii2bcd(buff, buffIdx, tmpStr, 6);
        buffIdx += 3;
        sprintf(tmpStr, "%06d", currentTran.BatchNoLOnT);
        EP_ascii2bcd(buff, buffIdx, tmpStr, 6);
        buffIdx += 3;
        sprintf(tmpStr, "%06d", currentTran.TranNoLOnT);
        EP_ascii2bcd(buff, buffIdx, tmpStr, 6);
        buffIdx += 3;
        sprintf(tmpStr, "%06d", currentTran.BatchNoLOffT);
        EP_ascii2bcd(buff, buffIdx, tmpStr, 6);
        buffIdx += 3;
        sprintf(tmpStr, "%06d", currentTran.TranNoLOffT);
        EP_ascii2bcd(buff, buffIdx, tmpStr, 6);
        buffIdx += 3;

        
        Log.i(TAG, String.format("Batch Tran No Infos"));
        Log.i(TAG, String.format("BatchNo:%d", params.BatchNo));
        Log.i(TAG, String.format("TranNo:%d", currentTran.TranNo));
        Log.i(TAG, String.format("BatchNoLOnT:%d", currentTran.BatchNoLOnT));
        Log.i(TAG, String.format("TranNoLOnT:%d", currentTran.TranNoLOnT));
        Log.i(TAG, String.format("BatchNoLOffT:%d", currentTran.BatchNoLOffT));
        Log.i(TAG, String.format("TranNoLOffT:%d", currentTran.TranNoLOffT));

        isoMsg.setFieldBin("63", Arrays.copyOf(buff, buffIdx));

        return rv;
    }
    static int ParseBatchUploadMsg(HashMap<String, byte[]> isoMsg)
    {
        int rv = 0;
        return rv;
    }

}
