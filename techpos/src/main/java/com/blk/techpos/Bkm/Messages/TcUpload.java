package com.blk.techpos.Bkm.Messages;

import static com.blk.techpos.Bkm.TranStruct.currentTran;

import android.util.Log;

import com.blk.sdk.olib.olib;
import com.blk.techpos.Bkm.Batch;
import com.blk.techpos.Bkm.TranStruct;
import com.blk.techpos.PrmStruct;
import com.blk.techpos.tran.EmvTran;

public class TcUpload {
    private static final String TAG = TcUpload.class.getSimpleName();
    public static int DoTcUpload(int op) throws Exception {
        int rv = -1;

        if (currentTran.EntryMode != TranStruct.EM_CHIP || op != EmvTran.APP_EMV_ONLINE)
            return rv;

        if (!(currentTran.TranType == TranStruct.T_SALE || currentTran.TranType == TranStruct.T_PREAUTHOPEN || currentTran.TranType == TranStruct.T_LOYALTYINSTALLMENT))
            return rv;

        TranStruct tDataBck = new TranStruct();
        olib.Copy(tDataBck, currentTran);

        currentTran.MsgTypeId += 10;
        currentTran.ProcessingCode = 950000;
        //Batch.SaveTran();

        Log.i(TAG, String.format("DoTcUpload Start - %d ---------------------", currentTran.MsgTypeId));
        rv = Msgs.ProcessMsg(Msgs.MessageType.M_OFFLINEADVICE);
        if (rv == 0 && !currentTran.f39OK()) {
            rv = -1;
        }
        Log.i(TAG, String.format("DoTcUpload Ended:%d - %d -------------------", rv, currentTran.MsgTypeId));
//            if (rv == 0) {
//                currentTran.OrgTranNo = currentTran.TranNo;
//                c.memcpy(currentTran.RspCode, "00".getBytes(), 2);
//                Batch.SaveTran();
//            }
        if(rv != 0)
        {
            Batch.SaveTran();
        }

        olib.Copy(currentTran, tDataBck);
        PrmStruct.Save();
        return rv;
    }
}
