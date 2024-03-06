package com.blk.techpos;


import static com.blk.techpos.Bkm.TranStruct.currentTran;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import com.blk.sdk.UI;
import com.blk.sdk.Utility;
import com.blk.sdk.activity.BaseActivity;
import com.blk.sdk.string;
import com.blk.techpos.tran.Tran;
import com.google.gson.Gson;
import com.blk.sdk.c;
import com.blk.techpos.Bkm.TranStruct;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class MyReceiver extends BroadcastReceiver {
    public static class MakeTranData {
        int msgType;
        int tranType;
        String amount;
        int installmentcount;
        String cardno;
        String acqid;
        String issid;
        String trandate;
        String trantime;
        String merchantid;
        String terminalid;
        int batchno;
        int transtan;
        String respcode;
        String authcode;
        String rrn;
        String refno;
        String ReplyDescription;
        short SubReplyCode;
        String SubReplyDescription;
    }

    public static Bitmap[] slips = new Bitmap[2];
    public static boolean fStartTran = false;
    public static Thread thread1;

    @Override
    public void onReceive(Context context, Intent intent) {

        thread1 = Thread.currentThread();

        Utility.log("broadcast: " + intent.getAction());

//        if (MainActivity.myThread != null && MainActivity.myThread.isAlive()) {
//            MainActivity.myThread.interrupt();
//            try {
//                MainActivity.myThread.join();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }




        if (intent.getAction().equals("com.blk.techpos.USER_ACTION"))
            user_actions(context, intent);
        thread1 = null;
    }

    static MakeTranData EcrTranInternal(TranStruct t) {
        MakeTranData resp = new MakeTranData();

        //resp.msgType = MIDLE_TRAN_MSG_SAVE;
        resp.tranType = t.TranType;
        resp.installmentcount = t.InsCount;
        resp.authcode = c.ToString(t.AuthCode);
        resp.rrn = c.ToString(t.RRN);
        resp.refno = c.ToString(t.OrgBankRefNo);
        resp.acqid = String.format("%02X%02X", t.AcqId[0], t.AcqId[1]);
        resp.issid = String.format("%02X%02X", t.IssId[0], t.IssId[1]);
        resp.merchantid = c.ToString(t.MercId);
        resp.terminalid = c.ToString(t.TermId);
        resp.batchno = t.BatchNo;
        resp.transtan = t.TranNo;
        resp.amount = c.ToString(t.Amount);
        if (c.strlen(t.Pan) > 0)
            resp.cardno = c.ToString(Arrays.copyOfRange(t.Pan, 0, 6)) + "******" + c.ToString(Arrays.copyOfRange(t.Pan, c.strlen(t.Pan) - 4, c.strlen(t.Pan)));
        resp.trandate = String.format("%02d%02d%02d", t.DateTime[0], t.DateTime[1], t.DateTime[2]);
        resp.trantime = String.format("%02d%02d%02d", t.DateTime[3], t.DateTime[4], t.DateTime[5]);
        resp.respcode = c.ToString(t.RspCode);
        resp.ReplyDescription = c.ToString(t.ReplyDescription);
        resp.SubReplyCode = t.SubReplyCode;
        resp.SubReplyDescription = c.ToString(t.SubReplyDescription);

        return resp;
    }

    private void user_actions(Context context, Intent intent) {
        fStartTran = true;
        final String amount = intent.getStringExtra("Amount");
        final int tranType = intent.getIntExtra("TranType", 0);
        final int tranNo = intent.getIntExtra("TranNo", 0);
        final String bankRefNo = intent.getStringExtra("BankRefNo");
        final String insCount = intent.getStringExtra("InsCount");
        final Bundle results = getResultExtras(true);

        final PendingResult pendingResult = goAsync();
        Thread t = new Thread() {
            public void run() {

                try {
//                Utility.EP_Init((Build.DEVICE.startsWith("generic") ? new Emulator() :
//                                PlatfromCtos.Get()), "techpos");
                    techpos.Init();
                } catch (Exception e) {
                    e.printStackTrace();
                    thread1 = null;
                    return;
                }

                if (BaseActivity.GetTopActivity() == null) {
                    Intent intent1 = new Intent();
                    intent1.setClassName(context.getPackageName(), MainActivity.class.getName());
                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent1);

                    while (BaseActivity.GetTopActivity() == null) {
                        Log.i("", "onReceive: wait");
                        Utility.sleep(100);
                    }
                }

                TranStruct.ClearTranData();

//                if (VTerm.GetVTermPrms().AcqInfoLen <= 0) {
//                    try {
//                        ParameterDownload.Download();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    if (params.BkmParamStatus != 1) {
//                        sendData("ERROR_CODE", "errorCode", "9999");
//                    }
//                }

                if (bankRefNo != null) {
                    c.memcpy(currentTran.OrgBankRefNo, bankRefNo.getBytes(), bankRefNo.length());
                }

                if (amount != null) {
                    c.memcpy(TranStruct.currentTran.Amount, string.PadLeft(amount, 12, '0').getBytes(), 12);

                    if (tranType == TranStruct.T_LOYALTYBONUSSPEND) {
                        c.memcpy(TranStruct.currentTran.BonusAmount, string.PadLeft(amount, 12, '0').getBytes(), 12);
                    }
                }

                if (tranNo > 0) {
                    currentTran.TranNo = tranNo;
                }

                if (insCount != null) {
                    currentTran.InsCount = (byte) c.atoi(insCount.getBytes());
                }

                TranStruct.currentTran.TranType = (byte) tranType;

                try {
                    Tran.StartTran(false);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    fStartTran = false;
                }


                TranStruct t = TranStruct.currentTran;
                MakeTranData tData = EcrTranInternal(t);
                String json = new Gson().toJson(tData);

                results.putString("TranData", json);

                for (int i = 0; i < 2; ++i) {
                    if (slips[i] != null) {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        slips[i].compress(Bitmap.CompressFormat.JPEG, 0, stream);
                        byte[] bytes = stream.toByteArray();
                        results.putByteArray("slip" + i, bytes);
                    }
                }
                slips[0] = null;
                slips[1] = null;

                pendingResult.setResultCode(0);
                pendingResult.finish();
                BaseActivity.GetTopActivity().moveTaskToBack(true);
            }
        };
        t.start();

    }

    public static void sendData(String actionName, String extraName, String value) {

        Intent intent = new Intent(actionName);
        intent.putExtra(extraName, value);

        UI.UiUtil.getApplicationContext().sendOrderedBroadcast(intent, null, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            }
        }, null, Activity.RESULT_OK, null, null);
    }
}
