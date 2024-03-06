package com.blk.techpos.Bkm.Messages;

import static com.blk.sdk.Convert.EP_ascii2bcd;
import static com.blk.sdk.Convert.SWAP_UINT16;
import static com.blk.sdk.c.memcpy;
import static com.blk.sdk.c.sprintf;
import static com.blk.sdk.c.strcpy;
import static com.blk.sdk.c.strlen;



import android.util.Log;

import com.blk.sdk.Convert;
import com.blk.sdk.UI;
import com.blk.sdk.Iso8583;
import com.blk.sdk.Utility;
import com.blk.sdk.c;
import com.blk.techpos.Bkm.Batch;
import com.blk.techpos.Print;
import com.blk.techpos.PrmStruct;
import com.blk.techpos.Bkm.TranStruct;
import com.blk.techpos.techpos;
import com.blk.techpos.Bkm.VParams.VTerm;

import java.util.Arrays;
import java.util.HashMap;
import static com.blk.techpos.PrmStruct.params;
import static com.blk.techpos.Bkm.TranStruct.currentTran;
public class EndOfDay {
    private static final String TAG = EndOfDay.class.getSimpleName();
    static int[] images = new int[16];

    public static int ProcessEndOfDay(boolean dontProcessSignals) throws Exception {
        int rv = -1;

        if ((VTerm.GetVTermPrms().AcqInfoLen <= 0) || !VTerm.IsVTermExist()) {
            UI.ShowMessage(1000, "PARAMETRE YÜKLEYİNİZ");
            return rv;
        }
        OfflineAdvice.ProcessOfflineAdvice(0);

        Log.i(TAG, "ProcessEndOfDay Started, dont process signal: " + dontProcessSignals);

        TranStruct.ClearTranData();

        currentTran.MsgTypeId = 500;
        currentTran.ProcessingCode = 910000;
        Batch.CalcBatchTotals();

        rv = Msgs.ProcessMsg(Msgs.MessageType.M_ENDOFDAY);
        if (rv == 0 && !currentTran.f39OK()) {
            rv = -1;
        }
        if (rv == 0) {
            UI.ShowMessage(2000, "İŞLEM BAŞARILI");
            Utility.SetEnvironmentVariable("BATCHON", "0");

//           sendOdealBatchReport();

        } else {
            if (!c.strcmp(currentTran.RspCode, "95".getBytes())) {
                rv = BatchUpload.ProcessBatchUpload();
            } else {
                UI.ShowMessage(2000, "İŞLEM BAŞARISIZ\nTEKRAR DENEYİNİZ");
            }
        }

        Log.i(TAG, "ProcessEndOfDay Ended:" + rv);

        if (rv == 0) {
            if (dontProcessSignals) {
                Print.PrintEndOfDay(0, 0, 0);
            } else {
                String[] items = new String[8];
                int reportOrder = Utility.GetEnvironmentVariableInt("BkmDailyReportOrder");
                if (reportOrder > 0) {
                    items[0] = "DETAY RAPOR";
                    items[1] = "ÖZET RAPOR";
                    rv = UI.ShowList("GÜNSONU RAPORU", items, images);
                    Utility.log("MenuSelectTo: %d", rv);
                    if (rv == 1)
                        Print.PrintEndOfDay(0, 0, 0);
                    else
                        Print.PrintEndOfDay(1, 0, 0);
                } else {
                    items[0] = "ÖZET RAPOR";
                    items[1] = "DETAY RAPOR";
                    rv = UI.ShowList("GÜNSONU RAPORU", items, images);
                    if (rv == 1)
                        Print.PrintEndOfDay(1, 0, 0);
                    else
                        Print.PrintEndOfDay(0, 0, 0);
                }
            }

            Batch.CloseBatch();
            if (params.KeyExchangeFlag == 1)
                Msgs.ProcessSignals(1);
            else if (!dontProcessSignals)
                Msgs.ProcessSignals(1);

            rv = 0;
        }

        PrmStruct.Save();
        return rv;
    }


    static int PrepareEndofDayMsg(Iso8583 isoMsg) throws Exception {
        byte[] tmpBuff = new byte[1024];
        int rv = 0, idx = 0, i = 0;
        short tmpLen = 0;
        byte BcdBuffLen = 0;
        byte[] BcdBuff = new byte[16];
        byte[] tmpStr = new byte[16];
        String tmpStr1;

        idx = 0;
        tmpBuff[idx++] = 0x11;
        idx += 2;

        sprintf(tmpStr, "%06d", params.BatchNo);
        EP_ascii2bcd(BcdBuff, 0, tmpStr, strlen(tmpStr));
        memcpy(tmpBuff, idx, BcdBuff, 0, 3);
        idx += 3;

        tmpBuff[idx++] = currentTran.TotalsLen;

        for (i = 0; i < currentTran.TotalsLen; i++) {
            memcpy(tmpBuff, idx, currentTran.Totals[i].CurrencyCode, 0, 3);
            idx += 3;

            tmpLen = SWAP_UINT16(currentTran.Totals[i].POnlTCnt);
            memcpy(tmpBuff, idx, Convert.ToArray(tmpLen), 0, 2);
            idx += 2;
            if (currentTran.Totals[i].POnlTCnt > 0) {
                tmpStr1 = Long.toString(currentTran.Totals[i].POnlTAmt);
                strcpy(tmpStr, tmpStr1);
                if (strlen(tmpStr) % 2 != 0) {
                    sprintf(tmpStr, "0%s", tmpStr1);
                }

                EP_ascii2bcd(BcdBuff, 0, tmpStr, strlen(tmpStr));
                BcdBuffLen = (byte) (strlen(tmpStr) / 2 + strlen(tmpStr) % 2);
                tmpBuff[idx++] = BcdBuffLen;
                memcpy(tmpBuff, idx, BcdBuff, 0, BcdBuffLen);
                idx += BcdBuffLen;
            } else
                tmpBuff[idx++] = 0x00;

            tmpLen = SWAP_UINT16(currentTran.Totals[i].NOnlTCnt);
            memcpy(tmpBuff, idx, Convert.ToArray(tmpLen), 0, 2);
            idx += 2;

            if (currentTran.Totals[i].NOnlTCnt > 0) {
                tmpStr1 = Long.toString(currentTran.Totals[i].NOnlTAmt);
                strcpy(tmpStr, tmpStr1);
                if (strlen(tmpStr) % 2 != 0) {
                    sprintf(tmpStr, "0%s", tmpStr1);
                }

                EP_ascii2bcd(BcdBuff, 0, tmpStr, strlen(tmpStr));
                BcdBuffLen = (byte) (strlen(tmpStr) / 2 + strlen(tmpStr) % 2);
                tmpBuff[idx++] = BcdBuffLen;
                memcpy(tmpBuff, idx, BcdBuff, 0, BcdBuffLen);
                idx += BcdBuffLen;
            } else
                tmpBuff[idx++] = 0x00;

            tmpLen = SWAP_UINT16(currentTran.Totals[i].POffTCnt);
            memcpy(tmpBuff, idx, Convert.ToArray(tmpLen), 0, 2);
            idx += 2;

            if (currentTran.Totals[i].POffTCnt > 0) {
                tmpStr1 = Long.toString(currentTran.Totals[i].POffTAmt);
                strcpy(tmpStr, tmpStr1);
                if (strlen(tmpStr) % 2 != 0) {
                    sprintf(tmpStr, "0%s", tmpStr1);
                }

                EP_ascii2bcd(BcdBuff, 0, tmpStr, strlen(tmpStr));
                BcdBuffLen = (byte) (strlen(tmpStr) / 2 + strlen(tmpStr) % 2);
                tmpBuff[idx++] = BcdBuffLen;
                memcpy(tmpBuff, idx, BcdBuff, 0, BcdBuffLen);
                idx += BcdBuffLen;
            } else
                tmpBuff[idx++] = 0x00;

            tmpLen = SWAP_UINT16(currentTran.Totals[i].NOffTCnt);
            memcpy(tmpBuff, idx, Convert.ToArray(tmpLen), 0, 2);
            idx += 2;

            if (currentTran.Totals[i].NOffTCnt > 0) {
                tmpStr1 = Long.toString(currentTran.Totals[i].NOffTAmt);

                strcpy(tmpStr, tmpStr1);
                if (strlen(tmpStr) % 2 != 0) {
                    sprintf(tmpStr, "0%s", tmpStr1);
                }

                EP_ascii2bcd(BcdBuff, 0, tmpStr, strlen(tmpStr));
                BcdBuffLen = (byte) (strlen(tmpStr) / 2 + strlen(tmpStr) % 2);
                tmpBuff[idx++] = BcdBuffLen;
                memcpy(tmpBuff, idx, BcdBuff, 0, BcdBuffLen);
                idx += BcdBuffLen;
            } else
                tmpBuff[idx++] = 0x00;
        }

        tmpLen = (short) (idx - 3);
        tmpLen = SWAP_UINT16(tmpLen);
        memcpy(tmpBuff, 1, Convert.ToArray(tmpLen), 0, 2);

        isoMsg.setFieldBin("63", Arrays.copyOf(tmpBuff, idx));
        return rv;
    }

    static int ParseEndofDayMsg(HashMap<String, byte[]> isoMsg) {
        int rv = 0;
        byte[] tlvData = new byte[1024];

        if (!isoMsg.containsKey("48"))
            return rv;

        int len = isoMsg.get("48").length;
        byte[] tmpBuff = isoMsg.get("48");

        if (techpos.GetTLVData(tmpBuff, len, 0x0D, tlvData, (short) c.sizeof(tlvData)) <= 0)
            return rv;

        params.DownloadParamsFlag = (tlvData[0] + 1) % 5;
        params.KeyExchangeFlag = (tlvData[1] + 1) % 5;


        return rv;
    }

//    private static void sendOdealBatchReport() throws Exception {
//
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(UI.getCurrentActivity());
//        SharedPreferences.Editor editor = prefs.edit();
//
//        editor.clear().apply();
//        byte[] tmpStr = new byte[32];
//        Batch.BatchTotals bTots = Batch.CalcBatchTotalsForPrint();
//        for (int i = 0; i < bTots.len; i++) {
//            editor.clear().apply();
//
//            int acqId = Integer.parseInt(String.format("%02X%02X", VTerm.GetVTermPrms().AcqInfos[i].AcqId[0], VTerm.GetVTermPrms().AcqInfos[i].AcqId[1]));
//            editor.putInt("AcqId", acqId).apply();
//
//            for (int j = 0; j < bTots.acqTots[i].len; j++) {
//
//                Utility.EP_DispFrmtAmt(tmpStr, Long.toString(bTots.acqTots[i].totAmts[j]).getBytes(), "TL".getBytes());
//
//                if (bTots.acqTots[i].tots[j][1] != 0) {
//                    // String tranName = GetTranNameForSettleReceipt(bTots.acqTots[i].tots[j][0]);
//
//                    int proCode = bTots.acqTots[i].tots[j][0];
//                    if (proCode == 0) {
//                        editor.putInt("paymentCount", bTots.acqTots[i].tots[j][1]);
//                        editor.putString("paymentAmount", Long.toString(bTots.acqTots[i].totAmts[j]));
//                    }
//                    if (proCode == 20000) {
//                        editor.putInt("cancelCount", bTots.acqTots[i].tots[j][1]);
//                        editor.putString("cancelAmount", Long.toString(bTots.acqTots[i].totAmts[j]));
//                    }
//                    if (proCode == 200000) {
//                        editor.putInt("refundControledCount", bTots.acqTots[i].tots[j][1]);
//                        editor.putString("refundControledAmount", c.ToString(tmpStr));
//                    }
//                    if (proCode == 200001) {
//                        editor.putInt("refundCount", bTots.acqTots[i].tots[j][1]);
//                        editor.putString("refundAmount", Long.toString(bTots.acqTots[i].totAmts[j]));
//                    }
//
//                    editor.commit();
//                }
//            }
//
//            //ODEALSERVİS
//            Odeal.HandleEndOfDay();
//        }
//    }
}

