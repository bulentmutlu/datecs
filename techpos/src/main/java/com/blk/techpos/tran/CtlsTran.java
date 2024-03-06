package com.blk.techpos.tran;


import static com.blk.sdk.Convert.EP_BfAscii;
import static com.blk.sdk.c.memcmp;
import static com.blk.sdk.c.memcpy;
import static com.blk.sdk.c.memset;
import static com.blk.sdk.c.sizeof;
import static com.blk.sdk.c.sprintf;
import static com.blk.sdk.c.strcpy;
import static com.blk.sdk.c.strlen;
import static com.blk.techpos.Bkm.TranStruct.EM_CONTACTLESS_SWIPE;
import static com.blk.techpos.Bkm.TranStruct.T_LOYALTYINSTALLMENT;
import static com.blk.techpos.Bkm.TranStruct.T_PREAUTHOPEN;
import static com.blk.techpos.Bkm.TranStruct.T_REFUND;
import static com.blk.techpos.Bkm.TranStruct.T_SALE;
import static com.blk.techpos.Bkm.TranStruct.T_VOID;
import static com.blk.techpos.PrmStruct.params;

import android.util.Log;

import com.blk.platform.Emv.IEmv;
import com.blk.platform.IEmvcl;
import com.blk.platform.IPlatform;
import com.blk.sdk.Convert;
import com.blk.sdk.UI;
import com.blk.sdk.Utility;
import com.blk.sdk.Emv.RetCode;
import com.blk.sdk.activity.ActivitySwipeCard;
import com.blk.sdk.activity.BaseActivity;
import com.blk.sdk.c;
import com.blk.techpos.Bkm.Batch;
import com.blk.techpos.Bkm.Bkm;
import com.blk.techpos.Bkm.Reversal;
import com.blk.techpos.Bkm.TranStruct;
import com.blk.techpos.Bkm.TranUtils;
import com.blk.techpos.Bkm.VParams.VTerm;
import com.blk.techpos.Print;

public class CtlsTran extends Tran{

    public static int ctlssTranLimit = 3500;
    public static byte[] clessLimit = new byte[32];
    static long visaTermFLmt = 3501;

    IEmvcl emvcl = IPlatform.get().emvcl;
    private static final String TAG = CtlsTran.class.getSimpleName();

    public CtlsTran(TranStruct tranStruct) {
        super(tranStruct, null);
    }

    public static void CtlssInitScreen() {
//        byte[] tmpStr = new byte[32];
//
//        //font = XuiCreateFont("./res/tema/courbd.ttf", 0, 0);
//        CtlssInitBgScreen();
//
//        if (!Utility.EP_IsNulls(currentTran.Amount, sizeof(currentTran.Amount))) {
//            Utility.EP_DispFrmtAmt(tmpStr, currentTran.Amount, "TL".getBytes());
//            Utils.Trim(tmpStr, (byte) ' ');
//            int x = (240 - strlen(tmpStr) * 6) / 2;
//            //EP_DispFormat(2, tmpStr, EP_ALIGN_CENTER);
//            //XuiCanvasDrawText(XuiRootCanvas(), x, 85, 14, font, XUI_TEXT_NORMAL, cStr, ConvertChar(tmpStr));
//        }
//        //XuiCanvasDrawText(XuiRootCanvas(), 50, 70, 14, font, XUI_TEXT_NORMAL, cStr, ConvertChar("KARTINIZI OKUTUNUZ"));
//        //EP_DispFormat(1, "KARTINIZI OKUTUNUZ", EP_ALIGN_CENTER);
    }

    private static void CtlssInitBgScreen() {
        CtlssSetLeds(1, 0, 0, 0);
    }

    public static void CtlssSetLeds(final int l1, final int l2, final int l3, final int l4) {

        if (BaseActivity.lastActivity.getClass() != ActivitySwipeCard.class) return;

        final ActivitySwipeCard sca = (ActivitySwipeCard) BaseActivity.lastActivity;

        if (sca.led1 == null) return;

        sca.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (l1 == 0)
                    sca.led1.setImageResource(com.blk.sdk.R.drawable.progress0);
                else{
                    sca.led1.setImageResource(com.blk.sdk.R.drawable.progress1);
                    Log.i("LEDX","1");
                }
                if (l2 == 0)
                    sca.led2.setImageResource(com.blk.sdk.R.drawable.progress0);
                else{
                    sca.led2.setImageResource(com.blk.sdk.R.drawable.progress1);
                    Log.i("LEDX","2");
                }
                if (l3 == 0)
                    sca.led3.setImageResource(com.blk.sdk.R.drawable.progress0);
                else{
                    sca.led3.setImageResource(com.blk.sdk.R.drawable.progress1);
                    Log.i("LEDX","3");

                }
                if (l4 == 0)
                    sca.led4.setImageResource(com.blk.sdk.R.drawable.progress0);
                else{
                    sca.led4.setImageResource(com.blk.sdk.R.drawable.progress1);
                    Log.i("LEDX","4");

                }
            }
        });
    }

    //
//    public static void CtlssDisplayBalanceAmount() {
//        Log.i("CTLSS", "CtlssDisplayBalanceAmount");
//        int rv = 0;
//        ByteArray tmpAmt = new ByteArray();
//        byte[] tmpFAmt = new byte[32];
//
//        memset(clessLimit, (byte) 0, sizeof(clessLimit));
//
//        if (currentTran.CtlssKernelType == KernType.KERNTYPE_MC) {
//            rv = ClssPassApi.Clss_GetTLVDataList_MC(new byte[]{(byte) 0xDF, (byte) 0x81, 0x05}, (byte) 3, 64, tmpAmt);
//            Utility.EP_printf("Clss_GetTLVDataList_MC(0xDF8105, %d):%d", tmpAmt.length, rv);
//            if (rv == RetCode.EMV_OK) {
//                Utility.EP_HexDump(tmpAmt.data, tmpAmt.length);
//                Convert.EP_BfAscii(tmpFAmt, tmpAmt.data, 0, 6);
//                Utility.EP_DispFrmtAmt(tmpFAmt, tmpFAmt, "TL".getBytes());
//
//                //EP_PutIMG( 0, 0, "contactless_bg.png");
//                //EP_PutIMG( 25, 15, "progress1.png");
//                CtlssSetLeds(1, 1, 1, 1);
//                int x = (240 - strlen(tmpFAmt) * 6) / 2;
//                //XuiCanvasDrawText(XuiRootCanvas(), 50, 70, 14, font, XUI_TEXT_NORMAL, cStr, ConvertChar("Temassız Bakiye:"));
//                //XuiCanvasDrawText(XuiRootCanvas(), x, 85, 14, font, XUI_TEXT_NORMAL, cStr, ConvertChar(tmpAmt));
//                //Screen
//                //EP_DispFormat(3, "Temassız Bakiye:", EP_ALIGN_CENTER);
//                //EP_DispFormat(4, tmpAmt, EP_ALIGN_CENTER);
//                memcpy(clessLimit, tmpFAmt, tmpFAmt.length);
//            }
//        } else {
//            rv = CtlssLib.Clss_GetTLVData((short) 0x9F5D, tmpAmt);
//            Utility.EP_printf("Clss_GetTLVData(0x9F5D, %d):%d", tmpAmt.length, rv);
//            if (rv == RetCode.EMV_OK) {
//                Utility.EP_HexDump(tmpAmt.data, tmpAmt.length);
//                Convert.EP_BfAscii(tmpFAmt, tmpAmt.data, 0, 6);
//                Utility.EP_DispFrmtAmt(tmpFAmt, tmpFAmt, "TL".getBytes());
//                int x = (240 - strlen(tmpFAmt) * 6) / 2;
//                //EP_PutIMG( 0, 0, "contactless_bg.png");
//                //EP_PutIMG( 25, 15, "progress1.png");
//                CtlssSetLeds(1, 1, 1, 1);
//                //XuiCanvasDrawText(XuiRootCanvas(), 51, 70, 14, font, XUI_TEXT_NORMAL, cStr, ConvertChar("Temassız Bakiye:"));
//                //XuiCanvasDrawText(XuiRootCanvas(), x, 85, 14, font, XUI_TEXT_NORMAL, cStr, ConvertChar(tmpAmt));
//                //Screen
//                //EP_DispFormat(3, "Temassız Bakiye:", EP_ALIGN_CENTER);
//                //EP_DispFormat(4, tmpAmt, EP_ALIGN_CENTER);
//                memcpy(clessLimit, tmpFAmt, tmpFAmt.length);
//            }
//        }
//        Utility.EP_printf("clessLimit: %s", c.ToString(clessLimit));
////        if(strlen(clessLimit)>0)
////            EP_WaitAKey(2000);
//    }
    @Override
    public int doTran() throws Exception {
        IEmv.TransactionData tranData = new IEmv.TransactionData();
        IEmvcl.Result result = new IEmvcl.Result();

        tranData.amount = new String(currentTran.Amount);
        tranData.amountCB = new String(currentTran.AmountCB);
        tranData.txnType = (currentTran.TranType == T_REFUND) ? 0x20: (byte)0x00;
        tranData.forceOnline = currentTran.TranType == T_REFUND || currentTran.TranType == T_VOID;

        int rv = emvcl.StartTransaction(tranData, result);
        if (rv != 0) return rv;

        boolean fOnline =  (result.transResult == 0x0004); // d_EMVCL_OUTCOME_ONL

        if ((currentTran.TranType == T_REFUND || currentTran.TranType == T_VOID) && !fOnline)
            throw new Exception("refund/void force online");

        EP_BfAscii(currentTran.EMVAID, result.aid, 0, result.aidLen);

        //EP_BfAscii(GetTranData().EMVAID, aid.data, 0, aid.length);
        memcpy(currentTran.Track2, result.track2Data, result.track2Len);
        FixTrack2(currentTran.Track2, result.track2Len);
        for (int i = 0; i < strlen(currentTran.Track2); i++) {
            if (currentTran.Track2[i] == '=') {
                memset(currentTran.ExpDate, (byte) 0, sizeof(currentTran.ExpDate));
                memcpy(currentTran.ExpDate, 0, currentTran.Track2, i + 1, 4);


                memcpy(currentTran.Pan, currentTran.Track2, i);
                currentTran.Pan[i] = 0;
                break;
            }
        }
        DumpCardInfo(currentTran);

        //visa sertifikasyon  5.22.25
        if (!CtlessCheckPanMismatch(currentTran.VoidPan)) {
            Log.i(TAG, "pan mismatch");
            UI.ShowMessage("İşlem karta ait değil");
            return RetCode.CLSS_FAILED;
        }


//        if(currentTran.TranType == T_VOID || currentTran.TranType == T_REFUNDCONTROLED)
//            rv = APP_CTLSS_CARDREADED; // goes online

        if (params.AcqSelMode == 0) {
            if (TranUtils.SelectAcq() != 0) {
                memcpy(currentTran.AcqId, VTerm.GetVTermPrms().AcqInfos[0].AcqId, 2);
                memcpy(currentTran.TermId, VTerm.GetVTermPrms().AcqInfos[0].TermId, 8);
                memcpy(currentTran.MercId, VTerm.GetVTermPrms().AcqInfos[0].MercId, 15);
                VTerm.SelVTermAcqInfoByAcqId(currentTran.AcqId, currentTran.TermId);
            }
        }
        if (result.sid == 0x18 || result.sid == 0x20 || result.sid == 0x52 || result.sid == 0x64 || result.sid == 0x43 || Convert.unsignedByteToInt(result.sid) == 0x8B)
            currentTran.EntryMode = EM_CONTACTLESS_SWIPE;

        if(currentTran.EntryMode != EM_CONTACTLESS_SWIPE)
            CtlssFillDE55();
//        if(!fOnline || (currentTran.PinEntered == 0))
//            OsBeep(3,100);

        currentTran.NoPrntSign = 1;
        if(result.cvmAnalysis == 0x01)	//d_EMVCL_CVM_REQUIRED_SIGNATURE rcDataAnalyze.cvmAnalysis == 0x01
        {
            //CVMStr = "CVM->Signature";
            //GlobalPara.isNeedSignature = true;
            currentTran.NoPrntSign = 0;
        }
        else if(result.cvmAnalysis == 0x02)	//d_EMVCL_CVM_REQUIRED_ONLPIN
        {
            //CVMStr = "CVM->Online PIN";
            if (TranUtils.PerformPinEntry(1) != 0) {
                emvcl.CancelTransaction();
                return RetCode.EMV_USER_CANCEL;
            }
        }
        else if(result.cvmAnalysis == 0x04)	//d_EMVCL_CVM_REQUIRED_NOCVM
        {
            //CVMStr = "CVM->No CVM Req";
        }
        //CtlssSetLeds(1,1,1,1);

        if (fOnline)
        {
            IEmv.OnlineResponse onlineResponse = new IEmv.OnlineResponse();

            onlineResponse.onlineResult = GoOnline().getValue();
            if (onlineResponse.onlineResult != OnlineResult.UNABLE.getValue())
                EmvTran.EMVSetDE55(onlineResponse);

            rv = emvcl.CompleteTransaction(onlineResponse);
            Log.i(TAG, "emvcl.CompleteTransaction:" + rv);
            if (rv != 0) {
                Reversal.ReverseTran();

                c.strcpy(currentTran.RspCode, "C3");
                c.strcpy(currentTran.ReplyDescription, "İŞLEM BAŞARISIZ..");
                UI.ShowMessage(1500, c.ToString(currentTran.ReplyDescription));
                Print.PrintTran(0);
            }
            return rv;
        }

        if (result.transResult == 0x0002) { // d_EMVCL_OUTCOME_APPROVAL
            //visa sertifikasyon time problem

            //screen
            //EP_DispFormat(2, "İŞLEM ONAYLANDI", EP_ALIGN_CENTER);
            //CtlssDisplayBalanceAmount();

            currentTran.Offline = 1;
            currentTran.MsgTypeId = 220;
            Batch.GenerateTranNos();
            Bkm.GenerateOffAuthCode();
            strcpy(currentTran.RspCode, "Y1");
            params.Stan++;
            currentTran.Stan = params.Stan;
        }
        else if (result.transResult == 0x0003) // d_EMVCL_OUTCOME_DECLINED
        {
            if (result.sid == 0x21 || result.sid == 0x20) { // MC
                return RetCode.CLSS_USE_CONTACT;
            }
            //CtlssDisplayBalanceAmount();
            //EP_WaitAKey(2000);
            Batch.SaveTran();
            Batch.SaveLastTran();
            UI.ShowMessage("İŞLEM REDDEDİLDİ");
            Print.PrintTran(0);
            return RetCode.CLSS_DECLINE;
        }

        return rv;
    }

    // chipdata
//tag(9F02) len(6) val(000000000500)
//tag(9F03) len(6) val(000000000000)
//tag(9F26) len(8) val(450ED2364259081C)
//tag(82) len(2) val(0000)
//tag(9F36) len(2) val(01A1)
//tag(5F34) len(1) val(02)
//tag(9F6E) len(4) val(20700000)
//tag(9F10) len(23) val(06011203A000000F03000000500000000000004487FB3D)
//tag(9F39) len(1) val(07)
//tag(9F33) len(3) val(E00008)
//tag(DF20) len(1) val(05)
//tag(95) len(5) val(0000000000)
//tag(57) len(19) val(4355093000658409D24012010000045700000F)
//tag(9A) len(3) val(220504)
//tag(9C) len(1) val(00)
//tag(9F37) len(4) val(8FCB128E)
//tag(5F24) len(3) val(240131)
//tag(5A) len(8) val(4355093000658409)
//tag(5F20) len(2) val(202F)
//tag(9F27) len(1) val(80)
    public void CtlssFillDE55()
    {
//        byte[] chipData = emvcl.chipData();
//        memcpy(currentTran.DE55, chipData, chipData.length);
//        currentTran.DE55Len = (short) chipData.length;
//        return 0;

        int rv = 0, i = 0;
        short idx = 0;
        IEmv.TlvData tlvData;

        // 98 Transaction Certificate (TC) Hash Value
        if( (tlvData = emvcl.dataGet(0x98)).Len > 0)
        {
            memcpy(currentTran.EMVTC, tlvData.Val, Math.min(tlvData.Len, 24));
        }

        //	82		Application Interchange Profile (AIP)	B2			Mandatory									//
        if( (tlvData = emvcl.dataGet(0x82)).Len > 0)
        {
            currentTran.DE55[idx++] = (byte) 0x82;
            currentTran.DE55[idx++] = (byte)tlvData.Len;
            memcpy(currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
            idx += tlvData.Len;
        }

        //	95		Terminal Verification Results (TVR)		B5			Mandatory									//
        if( (tlvData = emvcl.dataGet(0x95)).Len > 0)
        {
            currentTran.DE55[idx++] = (byte) 0x95;
            currentTran.DE55[idx++] = (byte)tlvData.Len;
            memcpy(currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
            idx += tlvData.Len;
        }

        //	9A		Transaction Date						B3			Mandatory									//
        if( (tlvData = emvcl.dataGet(0x9A)).Len > 0)
        {
            currentTran.DE55[idx++] = (byte) 0x9A;
            currentTran.DE55[idx++] = (byte)tlvData.Len;
            memcpy(currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
            idx += tlvData.Len;
        }

        //	9C		Transaction Type						B1			Mandatory									//
        if( (tlvData = emvcl.dataGet(0x9C)).Len > 0)
        {
            currentTran.DE55[idx++] =(byte)  0x9C;
            currentTran.DE55[idx++] = (byte)tlvData.Len;
            memcpy(currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
            idx += tlvData.Len;
        }

        //	5F2A	Transaction Currency Code				B2			Mandatory									//
        if( (tlvData = emvcl.dataGet(0x5F2A)).Len > 0)
        {
            currentTran.DE55[idx++] = 0x5F;
            currentTran.DE55[idx++] = 0x2A;
            currentTran.DE55[idx++] = (byte)tlvData.Len;
            memcpy(currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
            idx += tlvData.Len;
        }
        else {
            byte[] b = Convert.hexString2ByteArray("5F2A020949");
            memcpy(currentTran.DE55, idx, b, 0, b.length);
            idx += b.length;
        }

        //	9F02	Amount Authorised						B6			Mandatory									//
        if( (tlvData = emvcl.dataGet(0x9F02)).Len > 0)
        {
            currentTran.DE55[idx++] = (byte) 0x9F;
            currentTran.DE55[idx++] = 0x02;
            currentTran.DE55[idx++] = (byte)tlvData.Len;
            memcpy(currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
            idx += tlvData.Len;
        }

        //	9F03	Amount Other							B6			Mandatory (For cashback)					//
        if( (tlvData = emvcl.dataGet(0x9F03)).Len > 0)
        {
            currentTran.DE55[idx++] = (byte) 0x9F;
            currentTran.DE55[idx++] = 0x03;
            currentTran.DE55[idx++] = (byte)tlvData.Len;
            memcpy(currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
            idx += tlvData.Len;
        }

        //	9F37	Unpredictable Number					B4			Mandatory									//
        if( (tlvData = emvcl.dataGet(0x9F37)).Len > 0)
        {
            currentTran.DE55[idx++] = (byte) 0x9F;
            currentTran.DE55[idx++] = 0x37;
            currentTran.DE55[idx++] = (byte)tlvData.Len;
            memcpy(currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
            idx += tlvData.Len;
        }

        //	9F1A	Terminal Country Code					B2			Mandatory									//
        if( (tlvData = emvcl.dataGet(0x9F1A)).Len > 0)
        {
            currentTran.DE55[idx++] =(byte)  0x9F;
            currentTran.DE55[idx++] = 0x1A;
            currentTran.DE55[idx++] = (byte)tlvData.Len;
            memcpy(currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
            idx += tlvData.Len;
        }
        else {
            byte[] b = Convert.hexString2ByteArray("9F1A020792");
            memcpy(currentTran.DE55, idx, b, 0, b.length);
            idx += b.length;
        }

        //	9F33	Terminal Capabilities					B3			Optional									//
        if( (tlvData = emvcl.dataGet(0x9F33)).Len > 0)
        {
            currentTran.DE55[idx++] = (byte) 0x9F;
            currentTran.DE55[idx++] = 0x33;
            currentTran.DE55[idx++] = (byte)tlvData.Len;
            currentTran.DE55[idx++] = (byte) 0xE0;
            currentTran.DE55[idx++] = (byte) 0xF0;
            currentTran.DE55[idx++] = (byte) 0xC8;
        }

        //	5F34	Card seq number
        if( (tlvData = emvcl.dataGet(0x5F34)).Len > 0)
        {
            currentTran.DE55[idx++] = 0x5F;
            currentTran.DE55[idx++] = 0x34;
            currentTran.DE55[idx++] = (byte)tlvData.Len;
            memcpy(currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
            idx += tlvData.Len;
        }

        //	9F09	Terminal app version number
        if( (tlvData = emvcl.dataGet(0x9F09)).Len > 0)
        {
            currentTran.DE55[idx++] = (byte) 0x9F;
            currentTran.DE55[idx++] = 0x09;
            currentTran.DE55[idx++] = (byte)tlvData.Len;
            memcpy(currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
            idx += tlvData.Len;
        }

        //	9F1E	IFD serial number
        if( (tlvData = emvcl.dataGet(0x9F1E)).Len > 0)
        {
            currentTran.DE55[idx++] = (byte) 0x9F;
            currentTran.DE55[idx++] = 0x1E;
            currentTran.DE55[idx++] = (byte)tlvData.Len;
            memcpy(currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
            idx += tlvData.Len;
        }
//        else {
//            currentTran.DE55[idx++] = (byte) 0x9F;
//            currentTran.DE55[idx++] = 0x1E;
//            currentTran.DE55[idx++] = (byte)8;
//            memcpy(currentTran.DE55, idx, Bkm.GetSerialStr().getBytes(), 2, 8);
//            idx += tlvData.Len;
//        }

        //	9F34	Cardholder verification method result
        if( (tlvData = emvcl.dataGet(0x9F34)).Len > 0)
        {
            currentTran.DE55[idx++] = (byte) 0x9F;
            currentTran.DE55[idx++] = 0x34;
            currentTran.DE55[idx++] = (byte)tlvData.Len;
            memcpy(currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
            idx += tlvData.Len;
        }

        //	9F35	Terminal type
        if( (tlvData = emvcl.dataGet(0x9F35)).Len > 0)
        {
            currentTran.DE55[idx++] = (byte) 0x9F;
            currentTran.DE55[idx++] = 0x35;
            currentTran.DE55[idx++] = (byte)tlvData.Len;
            memcpy(currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
            idx += tlvData.Len;
        }

        // EMVProc_9F41Fix(gstEmvTransData.pstTransReq.asChipData, &gstEmvTransData.pstTransReq.iChipDataLen, gstEmvTransData.pstTransReq.asTraceNO);
        //	9F41	Trx sequence counter
        if( (tlvData = emvcl.dataGet(0x9F41)).Len > 0)
        {
            currentTran.DE55[idx++] = (byte) 0x9F;
            currentTran.DE55[idx++] = 0x41;
            currentTran.DE55[idx++] = (byte)tlvData.Len;
            memcpy(currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
            idx += tlvData.Len;
        }
//        else {
//            string strTraceNo = new string("" + params.TranNo).PadLeft(8, '0');
//            byte[] traceNo = Convert.hexString2ByteArray(strTraceNo.toString());
//            currentTran.DE55[idx++] = (byte) 0x9F;
//            currentTran.DE55[idx++] = 0x41;
//            currentTran.DE55[idx++] = (byte)traceNo.length;
//            memcpy(currentTran.DE55, idx, traceNo, 0, traceNo.length);
//            idx += tlvData.Len;
//        }

        //	9F53	Trx category code
        if( (tlvData = emvcl.dataGet(0x9F53)).Len > 0)
        {
            currentTran.DE55[idx++] = (byte) 0x9F;
            currentTran.DE55[idx++] = 0x53;
            currentTran.DE55[idx++] = (byte)tlvData.Len;
            memcpy(currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
            idx += tlvData.Len;
        }

        //	9F50	Prepaid Balance
        if( (tlvData = emvcl.dataGet(0x9F50)).Len > 0)
        {
            currentTran.DE55[idx++] =(byte)  0x9F;
            currentTran.DE55[idx++] = 0x50;
            currentTran.DE55[idx++] = (byte)tlvData.Len;
            memcpy(currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
            idx += tlvData.Len;
        }

        if(currentTran.TranType == T_SALE || currentTran.TranType == T_PREAUTHOPEN || currentTran.TranType == T_LOYALTYINSTALLMENT)
        {
            //	9F36	Application Transaction Counter (ATC)	B2			Mandatory									//
            if( (tlvData = emvcl.dataGet(0x9F36)).Len > 0)
            {
                currentTran.DE55[idx++] = (byte) 0x9F;
                currentTran.DE55[idx++] = 0x36;
                currentTran.DE55[idx++] = (byte)tlvData.Len;
                memcpy(currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
                idx += tlvData.Len;
            }

            //	9F10	Issuer Application Data					B32 (Var)	Mandatory									//
            if( (tlvData = emvcl.dataGet(0x9F10)).Len > 0)
            {
                currentTran.DE55[idx++] = (byte) 0x9F;
                currentTran.DE55[idx++] = 0x10;
                currentTran.DE55[idx++] = (byte)tlvData.Len;
                memcpy(currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
                idx += tlvData.Len;
            }

            //	9F26	Application Cryptogram					B8 			Mandatory									//
            if( (tlvData = emvcl.dataGet(0x9F26)).Len > 0)
            {
                for(i = 0; i < (Math.min(tlvData.Len, 8)); i++)
                    sprintf(currentTran.EMVAC, i*2, "%02X", tlvData.Val[i]);

                currentTran.DE55[idx++] = (byte) 0x9F;
                currentTran.DE55[idx++] = 0x26;
                currentTran.DE55[idx++] = (byte)tlvData.Len;
                memcpy(currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
                idx += tlvData.Len;
            }

            //	9F27	Cryptogram Information Data  (CID)		B1			Mandatory									//
            if( (tlvData = emvcl.dataGet(0x9F27)).Len > 0)
            {
                currentTran.DE55[idx++] = (byte) 0x9F;
                currentTran.DE55[idx++] = 0x27;
                currentTran.DE55[idx++] = (byte)tlvData.Len;
                memcpy(currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
                idx += tlvData.Len;
            }

            //	8E	CVM List
            if( (tlvData = emvcl.dataGet(0x8E)).Len > 0)
            {
                currentTran.DE55[idx++] =(byte)  0x8E;
                currentTran.DE55[idx++] = (byte)tlvData.Len;
                memcpy(currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
                idx += tlvData.Len;
            }

            //	9F06	EMV Application Id
            if( (tlvData = emvcl.dataGet(0x9F06)).Len > 0)
            {
                currentTran.DE55[idx++] =(byte)  0x9F;
                currentTran.DE55[idx++] = 0x06;
                currentTran.DE55[idx++] = (byte)tlvData.Len;
                memcpy(currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
                idx += tlvData.Len;
            }

        }

        currentTran.DE55Len = idx;
        Log.i(TAG, "DE55 : " + Convert.Buffer2Hex(currentTran.DE55, 0, currentTran.DE55Len));

    }

    public boolean CtlessCheckPanMismatch(byte[] pan) {

        if (Utility.IsNullorEmpty(pan)) return true;
        return !memcmp(currentTran.Pan, pan, strlen(currentTran.Pan));
    }
}
