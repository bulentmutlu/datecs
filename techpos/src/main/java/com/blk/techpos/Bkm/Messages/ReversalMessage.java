package com.blk.techpos.Bkm.Messages;
import static com.blk.techpos.PrmStruct.params;
import static com.blk.techpos.Bkm.TranStruct.currentTran;
import static com.blk.sdk.Convert.EP_ascii2bcd;
import static com.blk.sdk.c.memcpy;
import static com.blk.sdk.c.sprintf;
import static com.blk.sdk.c.strcat;
import static com.blk.sdk.c.strcpy;
import static com.blk.sdk.c.strlen;



import android.util.Log;

import com.blk.sdk.UI;
import com.blk.sdk.Iso8583;
import com.blk.sdk.Rtc;
import com.blk.sdk.olib.olib;
import com.blk.sdk.c;
import com.blk.techpos.Comms;
import com.blk.techpos.PrmStruct;
import com.blk.techpos.Bkm.Reversal;
import com.blk.techpos.Bkm.TranStruct;

import java.util.Arrays;
import java.util.HashMap;
/**
 * Created by id on 14.03.2018.
 */

public class ReversalMessage {
    private static final String TAG = ReversalMessage.class.getSimpleName();

    public static int ProcessReversal(Msgs.MessageType mainMsgType) throws Exception {
        int rv = -1;
	    byte[] reqMsg;
        int reqMsgLen = 0;
	    byte[] respMsg ;
        TranStruct rec;

        if((rec = Reversal.GetReversalTran()) != null)
        {
            TranStruct bck = new TranStruct();
            olib.Copy(bck, currentTran);
            olib.Copy(currentTran, rec);

            try {
                currentTran.emvOnlineFlow = bck.emvOnlineFlow;
                currentTran.unableToGoOnline = bck.unableToGoOnline;

                memcpy(currentTran.DateTime, Rtc.GetDateTime(), currentTran.DateTime.length);

                Log.i(TAG, "ProcessReversal Started");

                currentTran.MsgTypeId = 400;

                reqMsg = Msgs.PrepareMsg(Msgs.MessageType.M_REVERSAL);
                respMsg = new Comms().CommsSendRecv(Msgs.MessageType.M_REVERSAL, reqMsg, reqMsg.length);

                if(respMsg != null)
                {
                    rv = Msgs.ParseMsg(Msgs.MessageType.M_REVERSAL, respMsg);
                }
                else
                {
                    if(mainMsgType != Msgs.MessageType.M_ENDOFDAY)
                    {
                        if(strlen(currentTran.RspCode) <= 0)
                        {
                            strcpy(currentTran.RspCode, "C1");
                            strcpy(currentTran.ReplyDescription, "   CEVAP ALINAMADI  ");
                            strcat(currentTran.ReplyDescription, "  TEKRAR  DENEYİNİZ ");
                        }

                        if(!(currentTran.emvOnlineFlow != 0 && currentTran.unableToGoOnline != 0))
                        {
                            Log.i(TAG, "------sub resp code");
                            UI.ShowMessage(2000, c.ToString(currentTran.ReplyDescription));
                        }
                    }
                }
            } finally {
                olib.Copy(currentTran, bck);
            }


        }
	else
        rv = 0;

        if(rv == 0)
            Reversal.RemoveReversalTran();

        PrmStruct.params.Save();

        return rv;
    }



    static int PrepareReversalMsg(Iso8583 isoMsg) throws Exception {
        byte[] tmpStr = new byte[64];
        int rv = 0;
        byte[] buff= new byte[1024];
        int buffIdx = 0;

        isoMsg.setFieldValue("2", Arrays.copyOf(currentTran.Pan, strlen(currentTran.Pan)));
        isoMsg.setFieldValue("4", currentTran.Amount);

        sprintf(tmpStr, "%03X1", currentTran.EntryMode);
        isoMsg.setFieldValue("22", Arrays.copyOf(tmpStr, strlen(tmpStr)));

        sprintf(tmpStr, "%02X", currentTran.ConditionCode);
        isoMsg.setFieldValue("25", Arrays.copyOf(tmpStr, strlen(tmpStr)));

        sprintf(tmpStr, "%02X%02X", currentTran.AcqId[0], currentTran.AcqId[1]);
        isoMsg.setFieldValue("32", Arrays.copyOf(tmpStr, strlen(tmpStr)));

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
    static int ParseReversalMsg(HashMap<String, byte[]> isoMsg)
    {
        int rv = 0;
        return rv;
    }

}
