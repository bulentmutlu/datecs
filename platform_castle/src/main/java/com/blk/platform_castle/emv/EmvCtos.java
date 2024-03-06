package com.blk.platform_castle.emv;

import static com.blk.sdk.c.ToString;
import static com.blk.sdk.c.memcpy;
import static com.blk.sdk.c.memset;
import static com.blk.sdk.c.sizeof;
import static com.blk.sdk.c.strcpy;
import static com.blk.sdk.c.strlen;

import android.util.Log;

import com.blk.platform_castle.PlatfromCtos;
import com.blk.platform.Emv.CAPublicKey;
import com.blk.platform.Emv.EmvApp;
import com.blk.platform.Emv.IEmv;
import com.blk.sdk.Convert;
import com.blk.sdk.string;
import com.blk.sdk.Utility;
import com.blk.sdk.Emv.Config;
import com.blk.sdk.activity.BaseActivity;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

import CTOS.CtEMV;
import CTOS.emv.EMVAppInfo;
import CTOS.emv.EMVOnlineResponseData;

public class EmvCtos extends IEmv {

    private static final String TAG = EmvCtos.class.getSimpleName();

    CtEMV emv = PlatfromCtos._emv;
    MyEMVEvent emvEvent = new MyEMVEvent(emv);
    TransactionData tranData;

    @Override
    public int Init(final HashMap<String, List<CAPublicKey>> keys, final EmvApp[] appList) throws Exception {

        //emv.setDebug((byte) 1, (byte)-12);
        int intRtn = emv.initialize(emvEvent);
        if (intRtn != 0) throw new Exception( String.format("emv.initialize fail : %08X", intRtn));

        //intRtn = emv.specialEventRegister(CtEMV.d_EVENTID_DEBUG_MSG, emvEvent);
        intRtn = emv.specialEventRegister(CtEMV.d_EVENTID_TXN_FORCED_ONLINE, emvEvent);
        intRtn = emv.specialEventRegister(CtEMV.d_EVENTID_SHOW_VIRTUAL_PIN_EX, emvEvent);
        intRtn = emv.specialEventRegister(CtEMV.d_EVENTID_GET_PIN_DONE, emvEvent);
        intRtn = emv.specialEventRegister(CtEMV.d_EVENTID_OFFLINE_PIN_VERIFY_RESULT, emvEvent);
        intRtn = emv.specialEventRegister(CtEMV.d_EVENTID_PIN_BYPASS, emvEvent);

        String emvConfigFile = Config.CreateEmvConfigFile(keys, appList);
        InputStream stream = new ByteArrayInputStream(emvConfigFile.getBytes(StandardCharsets.UTF_8));

        intRtn = emv.setConfiguration(stream);
        if (intRtn != 0) throw new Exception(String.format("emv.setConfiguration fail : %08X", intRtn));
        int r  = emv.secureDataEncryptInfoSet(Security.GetEMVSecureDataInfo());

        intRtn = emv.secureDataWhitelistSet((byte) 0, Security.whiteList(), 24); // 0 set 1 delete
        if (intRtn != 0) throw new Exception(String.format("emv.secureDataWhitelistSet fail : %08X", intRtn));
//        r = emv.setMaskChar((byte)'0');
//        //PAN Mask in 5A & Track2
//        r = emv.maskPANDigits1To6((byte)0x00);
//        r = emv.maskPANDigits13To16((byte)0x00);
//        //Expiration date Mask in Track2
//        r = emv.maskEXPDigits1To4((byte)0x00);
//        //Service code Mask in Track2
//        r = emv.maskCSCDigits1To3((byte)0x00);

        return intRtn;
    }

    @Override
    public int AppSelect(int slot) {
        EMVAppInfo selectedAppInfo = new EMVAppInfo();
        selectedAppInfo.version = 1;
        selectedAppInfo.aid = new byte[16];
        selectedAppInfo.aidLen = 0;
        selectedAppInfo.appLabel = new byte[33];
        selectedAppInfo.appLabelLen = 0;
        int rv =  emv.txnAppSelect(selectedAppInfo);
        Log.i(TAG, String.format("AppSelect AID(%s) Label(%s) rv(0x%08X)",
                Convert.Buffer2Hex(selectedAppInfo.aid, 0, selectedAppInfo.aidLen),
                new String(selectedAppInfo.appLabel, 0, selectedAppInfo.appLabelLen), rv));
        return rv;
    }

    @Override
    public int StartTransaction(TransactionData tranData, ACType acType) {
        this.tranData = tranData;
        emvEvent.tranData = tranData;
        final int[] rv = new int[1];

        emvEvent.activity = BaseActivity.GetTopActivity();
        Log.i(TAG, "Emv activity : " + emvEvent.activity);

        rv[0] = emv.txnPerform();
        Log.i(TAG, "emv.txnPerform rv(" + rv[0] + ") txnResult(" + emvEvent.txnResult + ")");

        emvEvent.activity = null;

        if (rv[0] == CtEMV.d_EMVAPLIB_ERR_EVENT_ONLINE) {
            acType.type = ACType.AC_ARQC;
            return 0;
        }
        if (emvEvent.txnResult == 1) acType.type = ACType.AC_TC; // d_TXN_RESULT_APPROVAL
        else if (emvEvent.txnResult == 2) acType.type = ACType.AC_AAC; // d_TXN_RESULT_DECLINE
        else if (emvEvent.txnResult == 3) acType.type = ACType.AC_ARQC; // d_TXN_RESULT_GO_ONLINE
        // d_TXN_RESULT_FORCED_APPROVAL 4
        return rv[0];
    }

    @Override
    public int CompleteTransaction(OnlineResponse onlineResponse) {

        EMVOnlineResponseData ord = new EMVOnlineResponseData();
        ord.version = 1;
        ord.action = onlineResponse.onlineResult;
        ord.authorizationCode = onlineResponse.authorizationCode;
        ord.issuerAuthenticationData = onlineResponse.issuerAuthenticationData;
        ord.issuerAuthenticationDataLen = onlineResponse.issuerAuthenticationDataLen;
        ord.issuerScript = onlineResponse.issuerScript;
        ord.issuerScriptLen = onlineResponse.issuerScriptLen;

        return emv.txnCompletion(ord);
    }

    @Override
    public TlvData dataGet(int tag) {
        CTOS.emv.TlvData t = new CTOS.emv.TlvData();
        t.version = 1;
        t.tag = tag;
        t.len = 256;
        t.value = new byte[256];

        // Masked PAN 0xDF32 Masked track2 0xDF35
        // PAN encrypt 0xDF30 TRACK2 encrypt 0xDF33
//        if (tag == 0x5A) t.tag = 0xDF32;
//        else if (tag == 0x57) t.tag = 0xDF35;

        int rv = emv.dataGet(t);
        if (rv == 0) {

            if (t.tag == 0xDF30 || t.tag == 0xDF33) {
                byte[] output = Security.Decrypt(t.value);
                return new TlvData(tag, output.length, output);
            }
            return new TlvData(tag, t.len, t.value);
        }
        else {
            Log.i(TAG, String.format("Emv.dataGet(%X) fail : %d", tag, rv));
        }
        return new TlvData(tag, 0, null);
    }

    @Override
    public void Test(final HashMap<String, List<CAPublicKey>> keys, final EmvApp[] appList) throws Exception {
        if (false) {
            Init(keys, appList);
            AppSelect(0);
            TransactionData data = new TransactionData();
            data.amount = "000000000001";
            data.amountCB = "000000000000";
            ACType acType = new ACType();
            StartTransaction(data, acType);

            byte[] Pan = new byte[20];
            byte[] Track2 = new byte[64];
            byte[] ExpDate = new byte[4];
            byte[] EMVAID = new byte[32];
            byte[] CardHolderName = new byte[64];
            byte[] EMVAppPreferredName = new byte[16];
            ReadCardInfo(this, Pan, Track2, ExpDate, EMVAID, CardHolderName, EMVAppPreferredName);
        }

    }
    public static void ReadCardInfo(IEmv emv, byte[] pan, byte[] trk2, byte[] ExpDate, byte[] aid,
                                    byte[] name, byte[] preName)
    {
        IEmv.TlvData tlvData;
        boolean isPanOk = false;
        byte[] panClear= new byte[21];

        if( (tlvData = emv.dataGet(0x5A)).Len > 0) {
            isPanOk = true;
            Convert.EP_bcd2str(panClear,tlvData.Val,tlvData.Len);
            if(pan[strlen(panClear) - 1] == 0x3F)
                panClear[strlen(panClear) - 1] = 'F';

            if(panClear[strlen(panClear)-1] == '?')
                memcpy(pan, panClear, strlen(panClear)>19 ? 19 : strlen(panClear)-1);
            else
                memcpy(pan, panClear, strlen(panClear)>19 ? 19 : strlen(panClear));
        }

        if( (tlvData = emv.dataGet(0x57)).Len > 0) {
            Convert.EP_bcd2str(trk2,tlvData.Val,tlvData.Len);
            Utility.log("TRACK2");
            Utility.logDump(tlvData.Val, tlvData.Len);
            Utility.logDump(trk2, strlen(trk2));
            Utility.log("Track2:%s", ToString(trk2));
            for(int i = 0; i < strlen(trk2); i++)
            {
                if(trk2[i] == '=')
                {
                    memset(ExpDate, (byte) 0, sizeof(ExpDate));
                    memcpy(ExpDate, 0, trk2, i + 1, 4);
                    if(!isPanOk && pan != null)
                    {
                        memcpy(pan, trk2, i);
                        pan[i] = 0;
                        isPanOk = true;
                    }
                    break;
                }
            }
        }

        if( (tlvData = emv.dataGet(0x9F06)).Len > 0) {
            Convert.EP_BfAscii(aid,tlvData.Val, 0, tlvData.Len);
        }
        if( (tlvData = emv.dataGet(0x5F20)).Len > 0) {
            memcpy(name,tlvData.Val,tlvData.Len);
            string.Trim(name, (byte) ' ');
        }

        if( (tlvData = emv.dataGet(0x9F12)).Len > 0) {
            memcpy(preName,tlvData.Val,tlvData.Len);
            string.Trim(preName, (byte) ' ');
        }
        else {
            strcpy(preName, IEmv.GetPreNameFromAID(ToString(aid)));
        }
//
//            // Public Key Index, Certification Authority, Card
//        ba = new ByteArray();
//        rv2 = EMVCallback.EMVGetTLVData((short)0x8F, ba);
//        if(rv2 == RetCode.EMV_OK)
//        {
//            keyid = tlvData.Val[0];
//        }
//
//            // Registered Application Provider Identifier
//        ba = new ByteArray();
//        rv2 = EMVCallback.EMVGetTLVData((short)0x4F, ba);
//        if(rv2 == RetCode.EMV_OK)
//        {
//            EmvLib.AppInitKeys(keyid, tlvData.Val);
//        }
    }

}
