package com.blk.platform_castle.emvcl;

import static com.blk.sdk.c.ToString;
import static com.blk.sdk.c.memcpy;
import static com.blk.sdk.c.strcpy;

import android.util.Log;

import com.blk.platform_castle.PlatfromCtos;
import com.blk.platform_castle.emv.Security;
import com.blk.platform.Emv.CAPublicKey;
import com.blk.platform.Emv.EmvApp;
import com.blk.platform.Emv.IEmv;
import com.blk.platform.IEmvcl;
import com.blk.sdk.Convert;
import com.blk.sdk.Emv.Config;
import com.blk.platform_castle.TLVUtility;
import com.blk.sdk.Emv.RetCode;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

import CTOS.CtEMVCL;
import CTOS.emvcl.EMVCLActData;
import CTOS.emvcl.EMVCLDetectTxnData;
import CTOS.emvcl.EMVCLGetCardData;
import CTOS.emvcl.EMVCLRcDataAnalyze;
import CTOS.emvcl.EMVCLRcDataEx;

//    Visa AIDs (Kernels 1 & 3)
//    MasterCard PayPass AIDs (Kernel 2)
//    American Express AIDs (Kernel 4)
//    DPAS & Zip AIDs (Kernel 6) (Amadis Kernel)
//    Interac AIDs (Kernel 3E)
//    Bancomat AIDs (Kernel 3D)
//    JCB AIDs (Kernel 5)
//    CUP AIDs (Kernel 7)

public class EmvclCtos extends IEmvcl {
    private static final String TAG = EmvclCtos.class.getSimpleName();

    public static CtEMVCL emvcl = PlatfromCtos._emvcl;
    private final MyEMVCLSPEvent myCLSpEvent = new MyEMVCLSPEvent();
    IEmv.TransactionData tranData;
    EMVCLRcDataEx rcData;
    TLVUtility chpData;
    boolean fInit = false;
    @Override
    public  int Init(final HashMap<String, List<CAPublicKey>> keys, final EmvApp[] appList) throws Exception
    {
        fInit = true;
        //emvcl.setDebug((byte) 1, (byte)-12);
        int intRtn = emvcl.initialize();
        if (intRtn != 0) throw new Exception( String.format("emvcl.initialize fail : %08X", intRtn));

        intRtn = emvcl.specialEventRegister(CtEMVCL.d_EMVCL_EVENTID_LED_PIC_SHOW, myCLSpEvent);
        if (intRtn != 0) throw new Exception( String.format("emvcl.specialEventRegister fail : %08X", intRtn));
        intRtn = emvcl.specialEventRegister(CtEMVCL.d_EMVCL_EVENTID_AUDIO_INDICATION, myCLSpEvent);
        if (intRtn != 0) throw new Exception( String.format("emvcl.specialEventRegister fail : %08X", intRtn));
        intRtn = emvcl.specialEventRegister(CtEMVCL.d_EMVCL_EVENTID_SHOW_MESSAGE, myCLSpEvent);
        if (intRtn != 0) throw new Exception( String.format("emvcl.specialEventRegister fail : %08X", intRtn));
//        intRtn = emvcl.specialEventRegister(CtEMVCL.d_EMVCL_EVENTID_APP_LIST_V4, myCLSpEvent);
//        if (intRtn != 0) throw new Exception( String.format("emvcl.specialEventRegister fail : %08X", intRtn));

        String emvConfigFile = Config.CreateEmvCLConfigFile(keys, appList);
        InputStream stream = new ByteArrayInputStream(emvConfigFile.getBytes(StandardCharsets.UTF_8));

        intRtn = emvcl.setConfiguration(stream);
        if (intRtn != 0) throw new Exception(String.format("emvcl.setConfiguration fail : %08X", intRtn));
        int r  = emvcl.secureDataEncryptInfoSet(Security.GetEMVCLSecureDataInfo());
        intRtn = emvcl.secureDataWhitelistSet((byte) 0, Security.whiteList(), 24); // 0 set 1 delete
        if (intRtn != 0) throw new Exception(String.format("emvcl.secureDataWhitelistSet fail : %08X", intRtn));

        return 0;
    }

    @Override
    public int StartTransaction(final IEmv.TransactionData tranData, Result result)
    {
        this.tranData = tranData;
        myCLSpEvent.tranData = tranData;

        int rv = initTransaction(result);
        if (rv == 0) {
            rv = performTransaction(result);
        }
        if (rv == CtEMVCL.d_EMVCL_RC_FALLBACK)            return RetCode.CLSS_USE_CONTACT;
        if (rv  == CtEMVCL.d_EMVCL_TX_CANCEL)             return RetCode.EMV_USER_CANCEL;

        if (result.odaFail) { // visa sertificasyon test 6.07.01
            Log.i(TAG, "odaFail CTLSS_USECONTACT");
            return RetCode.CLSS_USE_CONTACT;
        }
        return rv;
    }

    @Override
    public byte[] chipData() {
        byte[] data = new byte[rcData.chipDataLen];
        memcpy(data, rcData.chipData, data.length);
        return data;
    }

    @Override
    public int CompleteTransaction(IEmv.OnlineResponse onlineResponse) {

        int itrn = emvcl.completeEx(onlineResponse.onlineResult, onlineResponse.authorizationCode,
                onlineResponse.issuerAuthenticationDataLen, onlineResponse.issuerAuthenticationData,
                onlineResponse.issuerScriptLen, onlineResponse.issuerScript, rcData);
        Log.i(TAG, "Complete transacction Rtn: " + String.format("0x%08X", itrn) + "\n");
        return (itrn == CtEMVCL.d_EMVCL_RC_DATA || itrn == 0) ? 0: itrn;
    }

    @Override
    public int CancelTransaction()
    {
        return emvcl.cancelTransaction();
    }


    @Override
    public IEmv.TlvData dataGet(int tag) {
        CTOS.emv.TlvData t = new CTOS.emv.TlvData();
        t.version = 1;
        t.tag = tag;
        t.len = 256;
        t.value = new byte[256];

        // Masked PAN 0xDF32 Masked track2 0xDF35
        // PAN encrypt 0xDF30 TRACK2 encrypt 0xDF33
//        if (tag == 0x5A) t.tag = 0xDF32;
//        else if (tag == 0x57) t.tag = 0xDF35;

        if (chpData == null) {
            chpData = new TLVUtility();
            chpData.TLVDataParse(rcData.chipData, rcData.chipDataLen);
        }

        if (chpData.TLVDataGet(t)) {

//            if (t.tag == 0xDF30 || t.tag == 0xDF33) {
//                byte[] output = Security.Decrypt(t.value);
//                return new IEmv.TlvData(tag, output.length, output);
//            }
            return new IEmv.TlvData(tag, t.len, t.value);
        }
        else {
            Log.i(TAG, String.format("Emvcl.dataGet(%X) tagNotFound", tag));
        }
        return new IEmv.TlvData(tag, 0, null);
    }


    int appSelect(EMVCLDetectTxnData detectTxnData, final EMVCLActData actData)
    {
        //EMVCLDetectTxnData detectTxnData = new EMVCLDetectTxnData();
        detectTxnData.tagNum = actData.tagNum;
        detectTxnData.transactionDataLen = actData.transactionDataLen;
        detectTxnData.transactionData = actData.transactionData;
        detectTxnData.selectedAid = new byte[10];
        detectTxnData.selectAidRsp = new byte[300];
        detectTxnData.selectPpseRsp = new byte[300];
        detectTxnData.selectedAidLen = 10;
        detectTxnData.selectAidRspLen = 300;
        detectTxnData.selectPpseRspLen = 300;
        int itrn = emvcl.detectTransactionEx(detectTxnData);
        Log.i(TAG, "Detect Transaction Rtn: " + String.format("0x%08X", itrn) + "\n");

        Log.d(TAG, "PPSE RSP: " + Convert.byteArray2HexString(detectTxnData.selectPpseRsp,detectTxnData.selectPpseRspLen) + "\n");
        Log.d(TAG, "AID RSP: " + Convert.byteArray2HexString(detectTxnData.selectAidRsp,detectTxnData.selectAidRspLen) + "\n");
        Log.i(TAG, "SEL AID: " + Convert.byteArray2HexString(detectTxnData.selectedAid,detectTxnData.selectedAidLen) + "\n");
        return itrn;
    }

    int initTransaction(Result result)
    {
        EMVCLActData actData = new EMVCLActData();
        actData.transactionData = new byte[128];

        // amount
        System.arraycopy(Convert.hexString2ByteArray("9F0206"), 0, actData.transactionData , 0, 3);
        assert tranData.amount.length() == 12;
        System.arraycopy(Convert.hexString2ByteArray(tranData.amount), 0, actData.transactionData, 3, 6);

        // amountCB, tranType
        byte tempData[] = Convert.hexString2ByteArray("9F03060000000000009C01" + ((tranData.txnType == (byte) 0x20) ? "20" : "00"));
        System.arraycopy(tempData, 0, actData.transactionData, 9, tempData.length);

        actData.start = 0;
        actData.tagNum = 3;
        actData.transactionDataLen = 21;
        // Force online
        if (tranData.forceOnline) {
            tempData = Convert.hexString2ByteArray("DF9F010101");
            System.arraycopy(tempData, 0, actData.transactionData, actData.transactionDataLen, tempData.length);
            actData.tagNum++;
            actData.transactionDataLen += tempData.length;
        }
//        // DF8F4E Additional TLV response in Additional Data
//        // DF8F49 Upload DOL The Upload DOL tags are used to get the required data objects which are
//        //returned in the Chip (or Additional) data of transaction response after performing a transaction.
//        String dolData = "9800" + "8200" + "9500" + "9A00" + "9C00" + "5F2A00" + "9F0200" + "9F0300" + "9F3700" + "9F1A00"
//                + "9F3300" + "5F3400" + "9F0900" + "9F1E00" + "9F3400" + "9F3500" + "9F4100" + "9F5300" + "9F5000" + "9F3600"
//                + "9F1000" + "9F2600" + "9F2700" + "8E00" + "9F0600";
//        String dol = "FFC3" + ("" + dolData.length() / 2) + dolData;
//        String dolTlv = "DF8F49" + ("" + dol.length() / 2) + dol;
//
//        tempData = Convert.hexString2ByteArray(dolTlv);
//        ISystem.arraycopy(tempData, 0, actData.transactionData, actData.transactionDataLen, tempData.length);
//        actData.tagNum++;
//        actData.transactionDataLen += tempData.length;

        EMVCLDetectTxnData detectTxnData = new EMVCLDetectTxnData();
        int rv = appSelect(detectTxnData, actData);
        if (rv != CtEMVCL.d_EMVCL_RC_DATA) {
            Log.i(TAG, "EMVCLDetectTxnData rv(" + rv + ") error(" + emvcl.getLastError() + ")");
            return rv;
        }
        memcpy(result.aid, detectTxnData.selectedAid, (result.aidLen = detectTxnData.selectedAidLen));

        return emvcl.initTransactionEx(actData.tagNum,actData.transactionData,actData.transactionDataLen);
    }
    public int getCardData(byte[] pan)
    {
        if (!fInit) return -1;

        int itrn;
        EMVCLGetCardData rcCardData = new EMVCLGetCardData();

        //in
        rcCardData.version = (byte)0x01;

        rcCardData.requestedCardTag = new byte[16];
        rcCardData.transactionData = new byte[64];

        rcCardData.requestedCardTagLen = 1;
        rcCardData.requestedCardTag = Convert.hexString2ByteArray("5A");

        rcCardData.tagNum = 3;
        rcCardData.transactionDataLen =21;
        rcCardData.transactionData = Convert.hexString2ByteArray("9F02060000000000009F03060000000000009C0100");

        //inout
        rcCardData.selectedAidLen = 10;
        rcCardData.selectAidRspLen = 300;
        rcCardData.selectPpseRspLen = 300;
        rcCardData.gotCardDataLen = 1024;
        //out
        rcCardData.selectedAid = new byte[10];
        rcCardData.selectAidRsp = new byte[300];
        rcCardData.selectPpseRsp = new byte[300];
        rcCardData.gotCardData = new byte[1024];

        itrn = emvcl.getCardData(rcCardData);
        Log.i(TAG, "Get Card Data Rtn: " + String.format("0x%08X", itrn) + "\n");
        if(itrn == 0xA0000001)
        {
            if (rcCardData.gotCardDataLen > 0 && rcCardData.gotCardData[0] == 0x5A) {
                byte len = rcCardData.gotCardData[1];
                strcpy(pan, Convert.bcdToStr(rcCardData.gotCardData, 2, len));
                Log.i(TAG, "CTLSS PAN : " + ToString(pan));
            }
            Log.i(TAG, "Get Card Data Len: " + String.format("0x%08X", rcCardData.gotCardDataLen) + "\n");
            Log.i(TAG, "PPSE RSP: " + Convert.byteArray2HexString(rcCardData.gotCardData,rcCardData.gotCardDataLen) + "\n");

            Log.i(TAG, "PPSE RSP Len: " + String.format("%d", rcCardData.selectPpseRspLen) + "\n");
            Log.i(TAG, "PPSE RSP: " + Convert.byteArray2HexString(rcCardData.selectPpseRsp,rcCardData.selectPpseRspLen) + "\n");

            Log.i(TAG, "SEL AID RSP Len: " + String.format("%d", rcCardData.selectAidRspLen) + "\n");
            Log.i(TAG, "SEL AID RSP: " + Convert.byteArray2HexString(rcCardData.selectAidRsp, rcCardData.selectAidRspLen) + "\n");

            Log.i(TAG, "GOT CARD DATA Len: " + String.format("%d", rcCardData.gotCardDataLen) + "\n");
            Log.i(TAG, "CARD DATA: " + Convert.byteArray2HexString(rcCardData.gotCardData, rcCardData.gotCardDataLen) + "\n");
        }
        return itrn;
    }
    int performTransaction(Result result)
    {
        rcData = new EMVCLRcDataEx();
        chpData = null;

        EMVCLRcDataAnalyze rcDataAnalyze = new  EMVCLRcDataAnalyze();

        int rv;
        do {
            rv = emvcl.performTransactionEx(rcData);
        } while (rv == CtEMVCL.d_EMVCL_PENDING);

        if (rv != 0 && rv != CtEMVCL.d_EMVCL_RC_DATA) {
            Log.i(TAG, "EMVCL_PerformTransactionEx rv(" + rv + ") error(" + emvcl.getLastError() + ")");
            return rv;
        }
        result.sid = rcData.sid;

//        Log.i(TAG, "chipdata");
//        TLVUtility.Dump(rcData.chipData, rcData.chipDataLen);
//        Log.i(TAG, "additional data");
//        TLVUtility.Dump(rcData.additionalData, rcData.additionalDataLen);


        rv = emvcl.analyzeTransactionEx(rcData, rcDataAnalyze);
        if (rv != 0) {
            Log.i(TAG, "analyzeTransactionEx rv(" + rv + ") error(" + emvcl.getLastError() + ")");
            return rv;
        }
        result.transResult = rcDataAnalyze.transResult;
        result.cvmAnalysis = rcDataAnalyze.cvmAnalysis;
        memcpy(result.cvmResults, rcDataAnalyze.cvmResults, result.cvmResults.length);
        result.visaAOSAPresent = rcDataAnalyze.visaAOSAPresent;
        memcpy(result.visaAOSA, rcDataAnalyze.visaAOSA, result.visaAOSA.length);
        result.odaFail = rcDataAnalyze.odaFail;
        result.completeFunRequired = rcDataAnalyze.completeFunRequired;

        memcpy(result.track2Data, rcData.track2Data, rcData.track2Len);
        result.track2Len = rcData.track2Len;


        if(rcDataAnalyze.cvmAnalysis == 0x01)	//d_EMVCL_CVM_REQUIRED_SIGNATURE rcDataAnalyze.cvmAnalysis == 0x01
        {
            //CVMStr = "CVM->Signature";
            //GlobalPara.isNeedSignature = true;
        }
        else if(rcDataAnalyze.cvmAnalysis == 0x02)	//d_EMVCL_CVM_REQUIRED_ONLPIN
        {
            //CVMStr = "CVM->Online PIN";
        }
        else if(rcDataAnalyze.cvmAnalysis == 0x04)	//d_EMVCL_CVM_REQUIRED_NOCVM
        {
            //CVMStr = "CVM->No CVM Req";
        }

        if (rcDataAnalyze.transResult == 0x0004) // d_EMVCL_OUTCOME_ONL
        {

        }

        return rv;
    }
}
