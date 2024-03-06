package com.blk.techpos.Bkm.Messages;
import static com.blk.techpos.PrmStruct.params;
import static com.blk.techpos.Bkm.TranStruct.currentTran;
import static com.blk.sdk.c.memcpy;



import android.util.Log;

import com.blk.sdk.Convert;
import com.blk.sdk.Iso8583;
import com.blk.sdk.Rtc;
import com.blk.sdk.c;
import com.blk.techpos.PrmStruct;
import com.blk.techpos.Bkm.TranStruct;
import com.blk.techpos.techpos;
import com.blk.techpos.Bkm.VParams.VTerm;

import java.util.HashMap;
/**
 * Created by id on 14.03.2018.
 */

public class HandShake {
    private static final String TAG = HandShake.class.getSimpleName();


    public static int ProcessHandShake(int type) throws Exception {
        int rv = -1;

        Log.i(TAG, "ProcessHandShake " +  params.BkmParamStatus);


        if(params.BkmParamStatus == 0)    return -600;

        TranStruct.ClearTranData();

        currentTran.MsgTypeId = 800;
        currentTran.ProcessingCode = 710000;
        if(type == 0)
        {
            Log.i(TAG, String.format("%d - %d - %d", Rtc.GetTimeSeconds(), VTerm.GetVTermPrms().HandShakeTryPeriod, (params.LastCommTime + (VTerm.GetVTermPrms().HandShakeTryPeriod * 3600))));
            if(Rtc.GetTimeSeconds() <= (params.LastCommTime + (VTerm.GetVTermPrms().HandShakeTryPeriod * 3600)))
                return -1;

            currentTran.ProcessingCode = 710001;

            
		/*
		{
			Rtc now = {0};
			EP_RtcGet(&now);

			EP_PrntOpen();
			EP_PrntFrmt("HANDSHAKE %02d.%02d.%02d %02d:%02d", now.day, now.mon, now.year, now.hour, now.min);
			EP_PrntFrmt("%d - %d - %d", EP_GetTimeSeconds(), GetVTermPrms().HandShakeTryPeriod, (params.LastCommTime + (GetVTermPrms().HandShakeTryPeriod * 3600)));
			EP_PrntFlush(0);
			EP_PrntClose();
		}
		*/
        }

        Msgs.ProcessMsg(Msgs.MessageType.M_HANDSHAKE);
        TranStruct.ClearTranData();
        PrmStruct.Save();

        return 0;
    }

    public static int PrepareHandShakeMsg(Iso8583 isoMsg) throws Exception {
        int rv = 0;
        byte[] prmListData = Msgs.GetPrmListData();
        short len = (short) prmListData.length, tmpLen = 0;
        byte[] buff = new byte[len + 3];

        buff[0] = 0x07;
        tmpLen = Convert.SWAP_UINT16(len);
        memcpy(buff, 1, Convert.ToArray(tmpLen), 0, 2);
        memcpy(buff, 3, prmListData, 0, len);

        isoMsg.setFieldBin("63", buff);
        return rv;
    }

    public static int ParseHandShakeMsg(HashMap<String, byte[]> isoMsg)
    {
        int rv = 0;
        byte[] tlvData = new byte[1024];

        if(isoMsg.containsKey("48")) {
            int len = isoMsg.get("48").length;
            byte[] tmpBuff = isoMsg.get("48");

            if(techpos.GetTLVData(tmpBuff, len, 0x0D, tlvData, (short) c.sizeof(tlvData)) > 0)
            {
                params.DownloadParamsFlag = (tlvData[0] + 1) % 5;
                params.KeyExchangeFlag = (tlvData[1] + 1) % 5;
            }
        }
        return rv;
    }
}
