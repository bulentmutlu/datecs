package com.blk.techpos.Bkm.Messages;
import static com.blk.techpos.PrmStruct.params;
import static com.blk.techpos.Bkm.TranStruct.currentTran;
import static com.blk.sdk.Convert.EP_ascii2bcd;
import static com.blk.sdk.c.memcpy;
import static com.blk.sdk.c.sprintf;
import static com.blk.sdk.c.strlen;



import android.util.Log;

import com.blk.sdk.Iso8583;
import com.blk.sdk.Rtc;
import com.blk.sdk.olib.olib;
import com.blk.techpos.Bkm.Batch;
import com.blk.techpos.Bkm.BatchRec;
import com.blk.techpos.PrmStruct;
import com.blk.techpos.Bkm.TranStruct;
import com.blk.techpos.Bkm.VParams.VTerm;

import java.util.Arrays;
import java.util.HashMap;
/**
 * Created by id on 14.03.2018.
 */

public class OfflineAdvice {
    private static final String TAG = OfflineAdvice.class.getSimpleName();

    public static int ProcessOfflineAdvice(int op) throws Exception {
        int rv = 0, rv1 = 0, idx = 0, suc = 0;

        if(op != 0)
        {
            if(params.OffAdvTryCount >= VTerm.GetVTermPrms().OffAdvMxSendTryCnt) return -1;
            if(Rtc.GetTimeSeconds() <= (params.LastOffAdvTryTime + (VTerm.GetVTermPrms().OffAdvTryPeriod * 60)))  return -1;
        }

        suc = 0;
        TranStruct bck = new TranStruct();
        olib.Copy(bck, currentTran); //memcpy(&bck, currentTran, sizeof(TranStruct));
        do
        {
            BatchRec rec = new BatchRec();
            rv = Batch.GetTran(rec, idx);
            idx++;
            if(rv > 0)
            {
                if(rec.MsgTypeId == 220 || rec.MsgTypeId == 120)
                {
                    TranStruct.ClearTranData();

                    currentTran.MsgTypeId = rec.MsgTypeId;
                    currentTran.Stan = rec.Stan;
                    currentTran.TranNo = rec.TranNo;
                    currentTran.TranNoLOnT = rec.TranNoLOnT;
                    currentTran.BatchNoLOnT = rec.BatchNoLOnT;
                    currentTran.TranNoLOffT = rec.TranNoLOffT;
                    currentTran.BatchNoLOffT = rec.BatchNoLOffT;
                    currentTran.OrgTranNo = rec.TranNo;
                    currentTran.ProcessingCode = rec.ProcessingCode;
                    memcpy(currentTran.DateTime, rec.DateTime, currentTran.DateTime.length);
                    memcpy(currentTran.Pan, rec.Pan, currentTran.Pan.length);
                    memcpy(currentTran.Amount, rec.Amount, currentTran.Amount.length);
                    memcpy(currentTran.ExpDate, rec.ExpDate, currentTran.ExpDate.length);
                    currentTran.EntryMode = rec.EntryMode;
                    currentTran.ConditionCode = rec.ConditionCode;
                    memcpy(currentTran.AcqId, rec.AcqId, 2);
                    memcpy(currentTran.RRN, rec.RRN, currentTran.RRN.length);
                    memcpy(currentTran.AuthCode, rec.AuthCode, currentTran.AuthCode.length);
                    memcpy(currentTran.RspCode, rec.RspCode, currentTran.RspCode.length);
                    memcpy(currentTran.TermId, rec.TermId, currentTran.TermId.length);
                    memcpy(currentTran.MercId, rec.MercId, currentTran.MercId.length);
                    memcpy(currentTran.CurrencyCode, rec.CurrencyCode, currentTran.CurrencyCode.length);
                    memcpy(currentTran.OrgBankRefNo, rec.OrgBankRefNo, currentTran.OrgBankRefNo.length);
                    currentTran.DE55Len = rec.DE55Len;
                    memcpy(currentTran.DE55, rec.DE55, currentTran.DE55.length);

                    currentTran.emvOnlineFlow = bck.emvOnlineFlow;
                    currentTran.unableToGoOnline = bck.unableToGoOnline;

                    Log.i(TAG, "DoAdvice Started");
                    rv1 = Msgs.ProcessMsg(Msgs.MessageType.M_OFFLINEADVICE);
                    if (rv1 == 0 && !currentTran.f39OK()) {
                        rv1 = -1;
                    }
                    Log.i(TAG, "DoAdvice Ended:" + rv1);

                    if(rv1 == 0)
                    {
                        memcpy(currentTran.RspCode, rec.RspCode, rec.RspCode.length);
                        currentTran.TranNo = rec.TranNo;
                        Batch.SaveTran();
                    }
                    else
                        suc = -1;

                    TranStruct.ClearTranData();
                }
            }
        }while(rv > 0);

        if(suc == 0)
        {
            params.OffAdvTryCount = 0;
        }
        else
        {
            params.OffAdvTryCount++;
        }

        params.LastOffAdvTryTime = Rtc.GetTimeSeconds();
        PrmStruct.Save();

        olib.Copy(currentTran, bck); //memcpy(currentTran, &bck , sizeof(TranStruct));

        return suc;
    }


    static int PrepareOfflineAdviceMsg(Iso8583 isoMsg) throws Exception {
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

        if(currentTran.ProcessingCode != 0)
            isoMsg.setFieldValue("37", currentTran.RRN);

        if(strlen(currentTran.AuthCode) > 0)
            isoMsg.setFieldValue("38", currentTran.AuthCode);


        isoMsg.setFieldValue("39", currentTran.RspCode);
        isoMsg.setFieldValue("41", currentTran.TermId);
        isoMsg.setFieldValue("42", currentTran.MercId);

        sprintf(tmpStr, "%02X%02X", currentTran.CurrencyCode[0], currentTran.CurrencyCode[1]);
        isoMsg.setFieldValue("49", Arrays.copyOf(tmpStr, strlen(tmpStr)));

        if(currentTran.DE55Len > 0)
            isoMsg.setFieldBin("55", Arrays.copyOf(currentTran.DE55, currentTran.DE55Len));

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

        if(currentTran.OrgBankRefNo[0] != 0)
        {
            buff[buffIdx++] = 0x0E;
            memcpy(buff, buffIdx, new byte[] {0x00, 0x10}, 0, 2);
            buffIdx += 2;
            memcpy(buff, buffIdx, currentTran.OrgBankRefNo, 0, 16);
            buffIdx += 16;
        }

        isoMsg.setFieldBin("63", Arrays.copyOf(buff, buffIdx));
        return rv;
    }
    static int ParseOfflineAdviceMsg(HashMap<String, byte[]> isoMsg)
    {
        int rv = 0;
        return rv;
    }

}
