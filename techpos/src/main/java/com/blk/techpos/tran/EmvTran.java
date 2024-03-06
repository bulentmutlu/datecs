package com.blk.techpos.tran;

import static com.blk.sdk.c.ToString;
import static com.blk.sdk.c.memcpy;
import static com.blk.sdk.c.memset;
import static com.blk.sdk.c.sizeof;
import static com.blk.sdk.c.sprintf;
import static com.blk.sdk.c.strcpy;
import static com.blk.sdk.c.strlen;
import static com.blk.techpos.Bkm.TranStruct.T_REFUND;
import static com.blk.techpos.Bkm.TranStruct.T_SALE;
import static com.blk.techpos.PrmStruct.params;

import android.util.Log;

import com.blk.platform.Emv.IEmv;
import com.blk.platform.IPlatform;
import com.blk.sdk.Convert;
import com.blk.sdk.Tlv;
import com.blk.sdk.UI;
import com.blk.sdk.Emv.RetCode;
import com.blk.sdk.Utility;
import com.blk.sdk.string;
import com.blk.sdk.c;
import com.blk.techpos.Bkm.Batch;
import com.blk.techpos.Bkm.Bkm;
import com.blk.techpos.Bkm.Reversal;
import com.blk.techpos.Bkm.TranStruct;
import com.blk.techpos.Bkm.TranUtils;
import com.blk.techpos.Bkm.VParams.VTerm;
import com.blk.techpos.Print;

import java.util.Arrays;

public class EmvTran extends Tran {

    public static final int MAX_KEY_NUM = 7;
    public static final int MAX_APP_NUM = 100;
    public static final int PART_MATCH = 0x00;      //Application selection matching flag (partial matching).
    public static final int EMV_GOODS = 0x02;      //Goods
    public static final int APP_EMV_OK = 0;
    public static final int APP_EMV_FAILED = 1;
    public static final int APP_EMV_FALLBACK = 2;
    public static final int APP_EMV_APPROVED = 3;
    public static final int APP_EMV_DECLINED = 4;
    public static final int APP_EMV_DENIAL = 5;
    public static final int APP_EMV_ONLINE = 6;
    public static final int APP_EMV_USERCANCEL = 7;
    public static final int APP_EMV_EXIT = 8;
    public static int pinEntryTried = 0;
    public static int pinLastTry = 0;
    int op;
    IEmv emv = IPlatform.get().emv;
    private static final String TAG = EmvTran.class.getSimpleName();

    public EmvTran(TranStruct tranStruct) {
        super(tranStruct, null);
    }

    //    private static EMVCallback emvCallback = EMVCallback.getInstance();
//
//
//    public static int AppEMVInit()
//    {
//        int rv = 0;
//
//        rv = EMVCallback.EMVCoreInit();
//        Log.i(TAG, String.valueOf(rv));
//
//        Assert.assertTrue(rv == RetCode.EMV_OK);
//
//        emvCallback.setCallbackListener(emvCallbackListener);
//        DeviceImplNeptune.getInstance(DeviceInfo.idal).verifyPlainPin = new VerifyPlainPin();
//
//        return rv;
//
//    }
//    public static int AppLoadEmvParams()
//    {
//        AppRemoveEmvAppCapk();
//        EmvSetAIDPrms();
//        AppInitApps();
//        return 0;
//    }
//    public static int AppRemoveEmvAppCapk()
//    {
//        int				iCnt;
//        int				iRet;
//
//        for(iCnt=0; iCnt<MAX_KEY_NUM; iCnt++)
//        {
//            EMV_CAPK stEmvCapk = new EMV_CAPK();
//            iRet = EMVCallback.EMVGetCAPK(iCnt, stEmvCapk);
//            if( iRet== RetCode.EMV_OK )
//            {
//                EMVCallback.EMVDelCAPK(stEmvCapk.keyID, stEmvCapk.rID);
//            }
//        }
//
//        for(iCnt=0; iCnt<MAX_APP_NUM; iCnt++)
//        {
//            EMV_APPLIST stEmvApp = new EMV_APPLIST();
//
//            iRet = EMVCallback.EMVGetApp(iCnt, stEmvApp);
//            if( iRet==RetCode.EMV_OK )
//            {
//                EMVCallback.EMVDelApp(stEmvApp.aid, (int)stEmvApp.aidLen);
//            }
//        }
//
//        return 0;
//    }
//    public static void AppInitApps()
//    {
//        int iRet, i = 0, idx = PrmFileHeader.sizeof;
//
//        if (!file.Exist(file_VEMVCONFIG)) return;
//
//        byte[] pBuff = new byte[0];
//        try {
//            pBuff = file.ReadAllBytes(file_VEMVCONFIG);
//        } catch (IOException e) {
//            return;
//        }
//
//        while(true)
//        {
//            EmvUtil.TlvData aData = EmvUtil.GetBerTlvData(pBuff, idx, pBuff.length, 0xBF8B02, i);
//            if (aData == null) break;
//
//            EmvUtil.TlvData tData = EmvUtil.GetBerTlvData(aData.Val, aData.Len, 0x9F06, 0);
//
//            if(tData != null)
//            {
//                EMV_APPLIST emvApp = new EMV_APPLIST();
//
//                memcpy(emvApp.aid, tData.Val, tData.Len);
//                emvApp.aidLen = (byte) tData.Len;
//
//                byte[] appName = EmvUtil.GetAppNameFromAID(emvApp.aid);
//
//                memcpy(emvApp.appName, appName, appName.length);
//                emvApp.selFlag = PART_MATCH;
//                emvApp.floorLimitCheck= 1;
//                emvApp.randTransSel= 1;
//                emvApp.velocityCheck= 1;
//
//                if((tData = EmvUtil.GetBerTlvData(aData.Val, aData.Len, 0xDF8B11, 0)) != null)
//                {
//                    emvApp.threshold = Convert.SWAP_UINT32(Convert.ToInt(tData.Val, 0));
//                }
//
//                if((tData = EmvUtil.GetBerTlvData(aData.Val, aData.Len, 0x9F1B, 0)) != null)
//                {
//                    emvApp.floorLimit = Convert.SWAP_UINT32(Convert.ToInt(tData.Val, 0));
//                    //Log.i(TAG, String.format("floorlimit:%d", emvApp.FloorLimit);
//                }
//
//                if((tData = EmvUtil.GetBerTlvData(aData.Val, aData.Len, 0xDF8B12, 0)) != null)
//                {
//                    emvApp.dDOL[0] = (byte) tData.Len;
//                    memcpy(emvApp.dDOL, 1, tData.Val, 0, tData.Len);
//                }
//
//                if((tData = EmvUtil.GetBerTlvData(aData.Val, aData.Len, 0xDF8B13, 0)) != null)
//                {
//                    emvApp.tDOL[0] = (byte) tData.Len;
//                    memcpy(emvApp.tDOL, 1, tData.Val, 0, tData.Len);
//                }
//
//                if((tData = EmvUtil.GetBerTlvData(aData.Val, aData.Len, 0xDF8B14, 0)) != null)
//                {
//                    emvApp.maxTargetPer = tData.Val[0];
//                }
//
//                if((tData = EmvUtil.GetBerTlvData(aData.Val, aData.Len, 0xDF8B15, 0)) != null)
//                {
//                    emvApp.targetPer = tData.Val[0];
//                }
//
//                if((tData = EmvUtil.GetBerTlvData(aData.Val, aData.Len, 0xDF8120, 0)) != null)
//                {
//                    memcpy(emvApp.tacDefault, tData.Val, tData.Len);
//                    //Log.i(TAG, String.format("TACDefault:%02X%02X%02X%02X%02X", emvApp.TACDefault[0], emvApp.TACDefault[1], emvApp.TACDefault[2], emvApp.TACDefault[3], emvApp.TACDefault[4]);
//                }
//
//                if((tData = EmvUtil.GetBerTlvData(aData.Val, aData.Len, 0xDF8121, 0)) != null)
//                {
//                    memcpy(emvApp.tacDenial, tData.Val, tData.Len);
//                    //Log.i(TAG, String.format("TACDenial:%02X%02X%02X%02X%02X", emvApp.TACDenial[0], emvApp.TACDenial[1], emvApp.TACDenial[2], emvApp.TACDenial[3], emvApp.TACDenial[4]);
//                }
//
//                if((tData = EmvUtil.GetBerTlvData(aData.Val, aData.Len, 0xDF8122, 0)) != null)
//                {
//                    memcpy(emvApp.tacOnline, tData.Val, tData.Len);
//                    //Log.i(TAG, String.format("TACOnline:%02X%02X%02X%02X%02X", emvApp.TACOnline[0], emvApp.TACOnline[1], emvApp.TACOnline[2], emvApp.TACOnline[3], emvApp.TACOnline[4]);
//                }
//
//                if((tData = EmvUtil.GetBerTlvData(aData.Val, aData.Len, 0x9F09, 0)) != null)
//                {
//                    memcpy(emvApp.version, tData.Val, tData.Len);
//                }
//
//                iRet = EMVCallback.EMVAddApp(emvApp);
//                Assert.assertTrue(iRet == RetCode.EMV_OK);
//
//                Log.i(TAG, String.format("EMVAddAPP(%s) : %d", c.ToString(emvApp.appName), iRet));
//            }
//            i++;
//        }
//    }
//
//    public static int AppInitKeys(byte keyid, byte[] rid)
//    {
//        int fd = 0, size = 0, i, rv, idx = 0;
//        byte[] pBuff = null;
//        EmvUtil.TlvData aData, tData;
//
//        Log.i(TAG, String.format("AppInitKeys(%02X)", keyid));
//        //EP_HexDump(rid, 5);
//
//        for(i=0; i<MAX_KEY_NUM; i++)
//        {
//            EMV_CAPK emvCak = new EMV_CAPK();
//            rv = EMVCallback.EMVGetCAPK(i, emvCak);
//            if(rv == RetCode.EMV_OK )
//            {
//                EMVCallback.EMVDelCAPK(emvCak.keyID, emvCak.rID);
//            }
//        }
//
//        size = file.Size(Bkm.file_VCAPK);
//        if (size <= 0) return 0;
//
//        try {
//            pBuff = file.ReadAllBytes(Bkm.file_VCAPK");
//        } catch (IOException e) {
//            //e.printStackTrace();
//        }
//        if (pBuff == null) return 0;
//
//
//        idx = PrmFileHeader.sizeof;
//        size -= idx;
//
//        i = 0;
//        while(true)
//        {
//            if((aData = EmvUtil.GetBerTlvData(pBuff, idx, size, 0xBF8B01, i)) == null)
//                     break;
//            EMV_CAPK emvCak = new EMV_CAPK();
//            if((tData = EmvUtil.GetBerTlvData(aData.Val, aData.Len, 0xDF8B01, 0)) != null)
//            {
//                memcpy(emvCak.rID, tData.Val, 5);
//                memcpy(emvCak.expDate, new byte[] {0x12,0x12,0x31}, 3);
//
//                if((tData = EmvUtil.GetBerTlvData(aData.Val, aData.Len, 0xDF8B02, 0)) != null)
//                {
//                    emvCak.keyID = tData.Val[0];
//                }
//
//                if(rid != null && !memcmp(rid, emvCak.rID, 5) && emvCak.keyID == keyid)
//                {
//                    if((tData = EmvUtil.GetBerTlvData(aData.Val, aData.Len, 0xDF8B03, 0)) != null)
//                    {
//                        emvCak.hashInd = tData.Val[0];
//                    }
//
//                    if((tData = EmvUtil.GetBerTlvData(aData.Val, aData.Len, 0xDF8B04, 0)) != null)
//                    {
//                        emvCak.arithInd = tData.Val[0];
//                    }
//
//                    if((tData = EmvUtil.GetBerTlvData(aData.Val, aData.Len, 0xDF8B05, 0)) != null)
//                    {
//                        if(tData.Len > c.sizeof(emvCak.modul))
//                            tData.Len = c.sizeof(emvCak.modul);
//
//                        emvCak.modulLen = (short) tData.Len;
//                        memcpy(emvCak.modul, tData.Val, tData.Len);
//                    }
//
//                    if((tData = EmvUtil.GetBerTlvData(aData.Val, aData.Len, 0xDF8B06, 0)) != null)
//                    {
//                        if(tData.Len > c.sizeof(emvCak.exponent))
//                            tData.Len = c.sizeof(emvCak.exponent);
//
//                        emvCak.exponentLen = (byte) tData.Len;
//                        memcpy(emvCak.exponent, tData.Val, tData.Len);
//                    }
//
//                    if((tData = EmvUtil.GetBerTlvData(aData.Val, aData.Len, 0xDF8B07, 0)) != null)
//                    {
//                        if(tData.Len > c.sizeof(emvCak.checkSum))
//                            tData.Len = c.sizeof(emvCak.checkSum);
//
//                        memcpy(emvCak.checkSum, tData.Val, tData.Len);
//                    }
//
//                    rv = EMVCallback.EMVAddCAPK(emvCak);
//                    Assert.assertTrue(rv == RetCode.EMV_OK);
//                    Log.i(TAG, String.format("EMVAddCAPK(%02X):%d", emvCak.keyID, rv));
//                    //EP_HexDump(&emvCak, sizeof(EMV_CAPK));
//
//                    break;
//                }
//            }
//
//            i++;
//        }
//
//        return 0;
//    }
//    public static int EmvSetAIDPrms()
//    {
//        int iRet, i = 0, idx = 0;
//        int fd = 0, size = 0;
//        int rv = -1;
//
//
//        if (!file.Exist(file_VEMVCONFIG)) return -1;
//
//        byte[] pBuff = new byte[0];
//        try {
//            pBuff = file.ReadAllBytes(file_VEMVCONFIG);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return -1;
//        }
//
//        idx = PrmFileHeader.sizeof;
//        i = 0;
//        while(true)
//        {
//            EmvUtil.TlvData aData = EmvUtil.GetBerTlvData(pBuff, idx, pBuff.length, 0xBF8B02, i);
//            if (aData == null) break;
//
//            EmvUtil.TlvData tData = EmvUtil.GetBerTlvData(aData.Val, aData.Len, 0x9F06, 0);
//            if(tData != null)
//            {
//                byte[] tmpAID = new byte[32];
//                Convert.EP_BfAscii(tmpAID, tData.Val, 0, tData.Len);
//
//                if(c.strstr(currentTran.EMVAID, tmpAID))
//                {
//                    Log.i(TAG, String.format("EmvSetAIDPrms:%s - %s", c.ToString(currentTran.EMVAID), c.ToString(tmpAID)));
//
//                    EmvParam emvPrm = new EmvParam();
//                    EMVCallback.EMVGetParameter(emvPrm);
//
//                    memcpy(emvPrm.merchCateCode, new byte[] {0x07, 0x42}, 2);
//                    emvPrm.getDataPIN= 1;
//                    emvPrm.surportPSESel= 1;
//
//                    memcpy(emvPrm.merchId, VTerm.GetVTermAcqInfo().MercId, 15);
//                    memcpy(emvPrm.termId, VTerm.GetVTermAcqInfo().TermId, 8);
//                    emvPrm.transType = EMV_GOODS;
//
//                    emvPrm.forceOnline = 1;
//                    if(VTerm.IsContactEMVOffline(null))
//                        emvPrm.forceOnline = (byte) ((currentTran.TranType == (byte) TranStruct.T_SALE) ? 0 : 1);
//
//                    Log.i(TAG,"ForceOnline:" + emvPrm.forceOnline);
//
//                    if((tData = EmvUtil.GetBerTlvData(aData.Val, aData.Len, 0x9F35, 0)) != null)
//                    {
//                        emvPrm.terminalType = tData.Val[0];
//                        Log.i(TAG, String.format("TerminalType:%02X", emvPrm.terminalType));
//                    }
//
//                    if((tData = EmvUtil.GetBerTlvData(aData.Val, aData.Len, 0x9F33, 0)) != null)
//                    {
//                        memcpy(emvPrm.capability, tData.Val, 3);
//                        Log.i(TAG, String.format("Capability:%02X%02X%02X", emvPrm.capability[0], emvPrm.capability[1], emvPrm.capability[2]));
//                    }
//
//                    if((tData = EmvUtil.GetBerTlvData(aData.Val, aData.Len, 0x9F40, 0)) != null)
//                    {
//                        memcpy(emvPrm.exCapability, tData.Val, 5);
//                        Log.i(TAG, String.format("ExCapability:%02X%02X%02X%02X%02X", emvPrm.exCapability[0], emvPrm.exCapability[1], emvPrm.exCapability[2], emvPrm.exCapability[3], emvPrm.exCapability[4]));
//                    }
//
//                    if((tData = EmvUtil.GetBerTlvData(aData.Val, aData.Len, 0x9F1A, 0)) != null)
//                    {
//                        memcpy(emvPrm.countryCode, tData.Val, 2);
//                        Log.i(TAG, String.format("CountryCode:%02X%02X", emvPrm.countryCode[0], emvPrm.countryCode[1]));
//                    }
//
//                    if((tData = EmvUtil.GetBerTlvData(aData.Val, aData.Len, 0x5F2A, 0)) != null)
//                    {
//                        memcpy(emvPrm.transCurrCode, tData.Val, 2);
//                        memcpy(emvPrm.referCurrCode, tData.Val, 2);
//                        Log.i(TAG, String.format("TransCurrCode:%02X%02X", emvPrm.transCurrCode[0], emvPrm.transCurrCode[1]));
//                    }
//
//                    if((tData = EmvUtil.GetBerTlvData(aData.Val, aData.Len, 0x5F36, 0)) != null)
//                    {
//                        emvPrm.transCurrExp = tData.Val[0];
//                        emvPrm.referCurrExp = tData.Val[0];
//                        Log.i(TAG, String.format("TransCurrExp:%02X", emvPrm.transCurrExp));
//                    }
//
//                    EMVCallback.EMVSetParameter(emvPrm);
//                    rv = 0;
//                    break;
//                }
//            }
//
//            i++;
//        }
//
//        return rv;
//    }
    public static int EMVFillDE55(IEmv emv)
    {
        int i = 0;
        IEmv.TlvData tlvData;
        int idx = 0;

        // 98 Transaction Certificate (TC) Hash Value
        if( (tlvData = emv.dataGet(0x98)).Len > 0)
        {
            memcpy(TranStruct.currentTran.EMVTC, tlvData.Val, Math.min(tlvData.Len, 24));
        }

        //	82		Application Interchange Profile (AIP)	B2			Mandatory									//
        if( (tlvData = emv.dataGet(0x82)).Len > 0)
        {
            TranStruct.currentTran.DE55[idx++] = (byte) 0x82;
            TranStruct.currentTran.DE55[idx++] = (byte)tlvData.Len;
            memcpy(TranStruct.currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
            idx += tlvData.Len;
        }

        //	95		Terminal Verification Results (TVR)		B5			Mandatory									//
        if( (tlvData = emv.dataGet(0x95)).Len > 0)
        {
            TranStruct.currentTran.DE55[idx++] = (byte) 0x95;
            TranStruct.currentTran.DE55[idx++] = (byte)tlvData.Len;
            memcpy(TranStruct.currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
            idx += tlvData.Len;
        }

        //	9A		Transaction Date						B3			Mandatory									//
        if( (tlvData = emv.dataGet(0x9A)).Len > 0)
        {
            TranStruct.currentTran.DE55[idx++] = (byte) 0x9A;
            TranStruct.currentTran.DE55[idx++] = (byte)tlvData.Len;
            memcpy(TranStruct.currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
            idx += tlvData.Len;
        }

        //	9C		Transaction Type						B1			Mandatory									//
        if( (tlvData = emv.dataGet(0x9C)).Len > 0)
        {
            TranStruct.currentTran.DE55[idx++] =(byte)  0x9C;
            TranStruct.currentTran.DE55[idx++] = (byte)tlvData.Len;
            memcpy(TranStruct.currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
            idx += tlvData.Len;
        }

        //	5F2A	Transaction Currency Code				B2			Mandatory									//
        if( (tlvData = emv.dataGet(0x5F2A)).Len > 0)
        {
            TranStruct.currentTran.DE55[idx++] = 0x5F;
            TranStruct.currentTran.DE55[idx++] = 0x2A;
            TranStruct.currentTran.DE55[idx++] = (byte)tlvData.Len;
            memcpy(TranStruct.currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
            idx += tlvData.Len;
        }

        //	9F02	Amount Authorised						B6			Mandatory									//
        if( (tlvData = emv.dataGet(0x9F02)).Len > 0)
        {
            TranStruct.currentTran.DE55[idx++] = (byte) 0x9F;
            TranStruct.currentTran.DE55[idx++] = 0x02;
            TranStruct.currentTran.DE55[idx++] = (byte)tlvData.Len;
            memcpy(TranStruct.currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
            idx += tlvData.Len;
        }

        //	9F03	Amount Other							B6			Mandatory (For cashback)					//
        if( (tlvData = emv.dataGet(0x9F03)).Len > 0)
        {
            TranStruct.currentTran.DE55[idx++] = (byte) 0x9F;
            TranStruct.currentTran.DE55[idx++] = 0x03;
            TranStruct.currentTran.DE55[idx++] = (byte)tlvData.Len;
            memcpy(TranStruct.currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
            idx += tlvData.Len;
        }

        //	9F37	Unpredictable Number					B4			Mandatory									//
        if( (tlvData = emv.dataGet(0x9F37)).Len > 0)
        {
            TranStruct.currentTran.DE55[idx++] = (byte) 0x9F;
            TranStruct.currentTran.DE55[idx++] = 0x37;
            TranStruct.currentTran.DE55[idx++] = (byte)tlvData.Len;
            memcpy(TranStruct.currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
            idx += tlvData.Len;
        }

        //	9F1A	Terminal Country Code					B2			Mandatory									//
        if( (tlvData = emv.dataGet(0x9F1A)).Len > 0)
        {
            TranStruct.currentTran.DE55[idx++] =(byte)  0x9F;
            TranStruct.currentTran.DE55[idx++] = 0x1A;
            TranStruct.currentTran.DE55[idx++] = (byte)tlvData.Len;
            memcpy(TranStruct.currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
            idx += tlvData.Len;
        }

        //	9F33	Terminal Capabilities					B3			Optional									//
        if( (tlvData = emv.dataGet(0x9F33)).Len > 0)
        {
            TranStruct.currentTran.DE55[idx++] = (byte) 0x9F;
            TranStruct.currentTran.DE55[idx++] = 0x33;
            TranStruct.currentTran.DE55[idx++] = (byte)tlvData.Len;
            TranStruct.currentTran.DE55[idx++] = (byte) 0xE0;
            TranStruct.currentTran.DE55[idx++] = (byte) 0xF0;
            TranStruct.currentTran.DE55[idx++] = (byte) 0xC8;
        }

        //	5F34	Card seq number
        if( (tlvData = emv.dataGet(0x5F34)).Len > 0)
        {
            TranStruct.currentTran.DE55[idx++] = 0x5F;
            TranStruct.currentTran.DE55[idx++] = 0x34;
            TranStruct.currentTran.DE55[idx++] = (byte)tlvData.Len;
            memcpy(TranStruct.currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
            idx += tlvData.Len;
        }

        //	9F09	Terminal app version number
        if( (tlvData = emv.dataGet(0x9F09)).Len > 0)
        {
            TranStruct.currentTran.DE55[idx++] = (byte) 0x9F;
            TranStruct.currentTran.DE55[idx++] = 0x09;
            TranStruct.currentTran.DE55[idx++] = (byte)tlvData.Len;
            memcpy(TranStruct.currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
            idx += tlvData.Len;
        }

        //	9F1E	IFD serial number
        if( (tlvData = emv.dataGet(0x9F1E)).Len > 0)
        {
            TranStruct.currentTran.DE55[idx++] = (byte) 0x9F;
            TranStruct.currentTran.DE55[idx++] = 0x1E;
            TranStruct.currentTran.DE55[idx++] = (byte)tlvData.Len;
            memcpy(TranStruct.currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
            idx += tlvData.Len;
        }

        //	9F34	Cardholder verification method result
        if( (tlvData = emv.dataGet(0x9F34)).Len > 0)
        {
            TranStruct.currentTran.DE55[idx++] = (byte) 0x9F;
            TranStruct.currentTran.DE55[idx++] = 0x34;
            TranStruct.currentTran.DE55[idx++] = (byte)tlvData.Len;
            memcpy(TranStruct.currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
            idx += tlvData.Len;
        }

        //	9F35	Terminal type
        if( (tlvData = emv.dataGet(0x9F35)).Len > 0)
        {
            TranStruct.currentTran.DE55[idx++] = (byte) 0x9F;
            TranStruct.currentTran.DE55[idx++] = 0x35;
            TranStruct.currentTran.DE55[idx++] = (byte)tlvData.Len;
            memcpy(TranStruct.currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
            idx += tlvData.Len;
        }

        //	9F41	Trx sequence counter
        if( (tlvData = emv.dataGet(0x9F41)).Len > 0)
        {
            TranStruct.currentTran.DE55[idx++] = (byte) 0x9F;
            TranStruct.currentTran.DE55[idx++] = 0x41;
            TranStruct.currentTran.DE55[idx++] = (byte)tlvData.Len;
            memcpy(TranStruct.currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
            idx += tlvData.Len;
        }

        //	9F53	Trx category code
        if( (tlvData = emv.dataGet(0x9F53)).Len > 0)
        {
            TranStruct.currentTran.DE55[idx++] = (byte) 0x9F;
            TranStruct.currentTran.DE55[idx++] = 0x53;
            TranStruct.currentTran.DE55[idx++] = (byte)tlvData.Len;
            memcpy(TranStruct.currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
            idx += tlvData.Len;
        }

        //	9F50	Prepaid Balance
        if( (tlvData = emv.dataGet(0x9F50)).Len > 0)
        {
            TranStruct.currentTran.DE55[idx++] =(byte)  0x9F;
            TranStruct.currentTran.DE55[idx++] = 0x50;
            TranStruct.currentTran.DE55[idx++] = (byte)tlvData.Len;
            memcpy(TranStruct.currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
            idx += tlvData.Len;
        }

        if(TranStruct.currentTran.TranType == TranStruct.T_SALE || TranStruct.currentTran.TranType == TranStruct.T_PREAUTHOPEN || TranStruct.currentTran.TranType == TranStruct.T_LOYALTYINSTALLMENT)
        {
            //	9F36	Application Transaction Counter (ATC)	B2			Mandatory									//
            if( (tlvData = emv.dataGet(0x9F36)).Len > 0)
            {
                TranStruct.currentTran.DE55[idx++] = (byte) 0x9F;
                TranStruct.currentTran.DE55[idx++] = 0x36;
                TranStruct.currentTran.DE55[idx++] = (byte)tlvData.Len;
                memcpy(TranStruct.currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
                idx += tlvData.Len;
            }

            //	9F10	Issuer Application Data					B32 (Var)	Mandatory									//
            if( (tlvData = emv.dataGet(0x9F10)).Len > 0)
            {
                TranStruct.currentTran.DE55[idx++] = (byte) 0x9F;
                TranStruct.currentTran.DE55[idx++] = 0x10;
                TranStruct.currentTran.DE55[idx++] = (byte)tlvData.Len;
                memcpy(TranStruct.currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
                idx += tlvData.Len;
            }

            //	9F26	Application Cryptogram					B8 			Mandatory									//
            if( (tlvData = emv.dataGet(0x9F26)).Len > 0)
            {
                for(i = 0; i < (Math.min(tlvData.Len, 8)); i++)
                    sprintf(TranStruct.currentTran.EMVAC, i*2, "%02X", tlvData.Val[i]);

                TranStruct.currentTran.DE55[idx++] = (byte) 0x9F;
                TranStruct.currentTran.DE55[idx++] = 0x26;
                TranStruct.currentTran.DE55[idx++] = (byte)tlvData.Len;
                memcpy(TranStruct.currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
                idx += tlvData.Len;
            }

            //	9F27	Cryptogram Information Data  (CID)		B1			Mandatory									//
            if( (tlvData = emv.dataGet(0x9F27)).Len > 0)
            {
                TranStruct.currentTran.DE55[idx++] = (byte) 0x9F;
                TranStruct.currentTran.DE55[idx++] = 0x27;
                TranStruct.currentTran.DE55[idx++] = (byte)tlvData.Len;
                memcpy(TranStruct.currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
                idx += tlvData.Len;
            }

            //	8E	CVM List
            if( (tlvData = emv.dataGet(0x8E)).Len > 0)
            {
                TranStruct.currentTran.DE55[idx++] =(byte)  0x8E;
                TranStruct.currentTran.DE55[idx++] = (byte)tlvData.Len;
                memcpy(TranStruct.currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
                idx += tlvData.Len;
            }

            //	9F06	EMV Application Id
            if( (tlvData = emv.dataGet(0x9F06)).Len > 0)
            {
                TranStruct.currentTran.DE55[idx++] =(byte)  0x9F;
                TranStruct.currentTran.DE55[idx++] = 0x06;
                TranStruct.currentTran.DE55[idx++] = (byte)tlvData.Len;
                memcpy(TranStruct.currentTran.DE55, idx, tlvData.Val, 0, tlvData.Len);
                idx += tlvData.Len;
            }
        }
        TranStruct.currentTran.DE55Len = (short) idx;
        return 0;
    }

    public static void ReadCardInfo(IEmv emv, byte[] pan, byte[] trk2, byte[] ExpDate, byte[] aid, byte[] name, byte[] preName)
    {
        IEmv.TlvData tlvData;
        boolean isPanOk = false;

        if( (tlvData = emv.dataGet(0x5A)).Len > 0) {
            isPanOk = true;
            //Convert.EP_bcd2str(panClear,tlvData.Val,tlvData.Len);
            String sPan = Convert.Buffer2Hex(tlvData.Val, 0, tlvData.Len);
            memcpy(pan, sPan.getBytes(), sPan.length());

            if(pan[strlen(pan) - 1] == 0x3F)
                pan[strlen(pan) - 1] = 'F';

            if(pan[strlen(pan)-1] == '?')
                pan[strlen(pan) - 1] = 0;
        }

        if( (tlvData = emv.dataGet(0x57)).Len > 0) {
            //Convert.EP_bcd2str(trk2,tlvData.Val,tlvData.Len);
            String sTrk2 = Convert.Buffer2Hex(tlvData.Val, 0, tlvData.Len);
            memcpy(trk2, sTrk2.getBytes(), sTrk2.length());

            Utility.log("TRACK2");
            Utility.logDump(tlvData.Val, tlvData.Len);
            Utility.logDump(trk2, strlen(trk2));
            Utility.log("Track2:%s", ToString(trk2));
            for(int i = 0; i < strlen(trk2); i++)
            {
                if(trk2[i] == '=' || trk2[i] == 'D')
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

    public static void EMVSetDE55(IEmv.OnlineResponse onlineResponse)
    {
        int idx = 0;
        Tlv tData;

        Log.i(TAG, "EMVSetDE55");

        if((tData = Tlv.GetBerTlvData(TranStruct.currentTran.DE55, TranStruct.currentTran.DE55Len, 0x8A, 0)) != null)
        {
//            EMVCallback.EMVSetTLVData((short) 0x8A, tData.Val, tData.Len);
            onlineResponse.authorizationCode[0] = tData.val[0];
            onlineResponse.authorizationCode[1] = tData.val[1];
        }
	    else {
            onlineResponse.authorizationCode[0] = (byte) 0x30;
            onlineResponse.authorizationCode[1] = (byte) 0x30;
//            EMVCallback.EMVSetTLVData((short) 0x8A, new byte[] {0x30, 0x30}, 2);
        }

//        if((tData = EmvUtil.GetBerTlvData(currentTran.DE55, currentTran.DE55Len, 0x89, 0)) != null)
//        {
//            EMVCallback.EMVSetTLVData((short) 0x89, tData.Val, tData.Len);
//        }

        // ISSUER AUTHENTICATION DATA
        if((tData = Tlv.GetBerTlvData(TranStruct.currentTran.DE55, TranStruct.currentTran.DE55Len, 0x91, 0)) != null)
        {
            //EMVCallback.EMVSetTLVData((short) 0x91, tData.Val, tData.Len);
            onlineResponse.issuerAuthenticationData = Arrays.copyOfRange(tData.val, 0, tData.len);
            onlineResponse.issuerAuthenticationDataLen = tData.len;
        }

        //if(script != null)
        {
            // ISSUER SCRIPT TEMPLATE 1
            if((tData = Tlv.GetBerTlvData(TranStruct.currentTran.DE55, TranStruct.currentTran.DE55Len, 0x71, 0)) != null)
            {
                onlineResponse.issuerScript[idx++] = 0x71;
                onlineResponse.issuerScript[idx++] = (byte) tData.len;
                memcpy(onlineResponse.issuerScript, idx, tData.val, 0, tData.len);
                idx += tData.len;
            }

            // ISSUER SCRIPT TEMPLATE 2
            if((tData = Tlv.GetBerTlvData(TranStruct.currentTran.DE55, TranStruct.currentTran.DE55Len, 0x72, 0)) != null)
            {
                onlineResponse.issuerScript[idx++] = 0x72;
                onlineResponse.issuerScript[idx++] = (byte) tData.len;
                memcpy(onlineResponse.issuerScript, idx, tData.val, 0, tData.len);
                idx += tData.len;
            }
        }
        onlineResponse.issuerScriptLen = idx;
    }

    //    public static int EmvTran() throws Exception {
//        int rv = Emv.APP_EMV_OK;
//
//        if(params.BkmParamStatus == 0)
//        {
//            UI.ShowMessage("PARAMETRE YÜKLEYİNİZ", 2000);
//            return -600;
//        }
//
//        if(Batch.IsSettleRequired())
//        {
//            UI.ShowMessage("GÜNSONU YAPINIZ", 2000);
//            return -700;
//        }
//
//        Msgs.ProcessSignals(3);
//
//        TranStruct.ClearTranData();
//
//        memcpy(currentTran.DateTime, Rtc.EP_GetDateTime(), currentTran.DateTime.length);
//
//        rv = EmvTranStep1();
//        if(rv == 0)
//        {
//            if(TranProc.DoTran() < 0)
//                rv = APP_EMV_USERCANCEL;
//        }
//        else if(rv == APP_EMV_FALLBACK)
//        {
//            return TranProc.FallbackTran();
//        }
//
//        Msgs.ProcessSignals(2);
//
//        return rv;
//    }
//
//    public static int EmvTranStep1()
//    {
//        int rv = 0;
//        Log.i(TAG, "EmvTranStep1");
//
//        if(currentTran.EMVAID[0] == 0)
//        {
//            UI.ShowMessage("CHİP KART OKUNUYOR\nLÜTFEN BEKLEYİNİZ", 0);
//            rv = EmvGetCardInfo(currentTran.Pan, currentTran.Track2, currentTran.EMVAID, currentTran.CardHolderName, currentTran.EMVAppPreferredName);
//            if(rv == RetCode.EMV_APP_BLOCK)
//                return APP_EMV_EXIT;
//
//            if(rv != 0)
//                return APP_EMV_FALLBACK;
//
//        }
//
//        currentTran.EntryMode = TranStruct.EM_CHIP;
//
//        if(TranUtils.SelectTran() <= 0)
//            return APP_EMV_USERCANCEL;
//
//        if(TranUtils.SelectAcq() != 0)
//            return APP_EMV_USERCANCEL;
//
//        return APP_EMV_OK;
//    }
//
    public static int EmvTranStep2() throws Exception {

        Log.i(TAG, "EmvTranStep2");

        IEmv emv = IPlatform.get().emv;

        if(TranStruct.currentTran.EntryMode != TranStruct.EM_CHIP)
            return APP_EMV_ONLINE;

        if(emv.IsCardPINBlocked())
        {
            TranStruct.currentTran.PinEntered = 3;
            if(VTerm.IsEMVPinBlock(null))
            {
                UI.ShowMessage("PIN BLOKE\nIZNI YOK");
                return APP_EMV_EXIT;
            }
        }
        IEmv.TransactionData tranData = new IEmv.TransactionData();
        tranData.amount = new String(TranStruct.currentTran.Amount);
        tranData.amountCB = new String(TranStruct.currentTran.AmountCB);
        tranData.txnType = (TranStruct.currentTran.TranType == T_REFUND) ? 0x20: (byte)0x00;
        tranData.forceOnline = true;
        if (VTerm.IsContactEMVOffline(null))
            tranData.forceOnline = TranStruct.currentTran.TranType != T_SALE;

        tranData.emvEvent = () -> {
            if (!VTerm.IsPinBypass(null))
                return false;
            TranStruct.currentTran.PinEntered = 4;
            return true;
        };
        IEmv.ACType aCType = new IEmv.ACType();

        int rv = emv.StartTransaction(tranData, aCType);

        if(emv.IsCardPINBlocked())
        {
            TranStruct.currentTran.PinEntered = 3;
            if(VTerm.IsEMVPinBlock(null))
            {
                UI.ShowMessage("PIN BLOKE\nIZNI YOK");
                return APP_EMV_EXIT;
            }
            UI.ShowMessage("KARTIN SIFRESI\nKULLANIMA KAPALIDIR\nISLEME DEVAM\nEDILILYOR");
        }

        if(emv.IsPerformOnlinePin())
        {
            Log.i(TAG, "PerformPinEntry 2");
            if(TranUtils.PerformPinEntry(1)==-2)
            {
                UI.ShowMessage("ZAMAN ASIMI");
                return APP_EMV_EXIT;
            }
        }

        EMVFillDE55(emv);
        if(rv == RetCode.EMV_OK)
        {
            switch(aCType.type)
            {
                case IEmv.ACType.AC_TC:
                    if(TranStruct.currentTran.TranType == TranStruct.T_SALE)
                            rv = APP_EMV_APPROVED;
                    else
                            rv = APP_EMV_FAILED;
                    break;
                case IEmv.ACType.AC_ARQC:
                    rv = APP_EMV_ONLINE;
                    if(TranStruct.currentTran.TranType == TranStruct.T_SALE)
                    {
                        TranStruct.currentTran.emvOnlineFlow = 1;
                        if(VTerm.IsContactEMVOffline(null))
                            TranStruct.currentTran.unableToGoOnline = 1;
                    }
                break;
                default:
                    rv = APP_EMV_DECLINED;
                    break;
            }
        }
        else if(rv == RetCode.EMV_DENIAL || rv == RetCode.EMV_NOT_ACCEPT)
        {
            rv = APP_EMV_DENIAL;
        }
        else if(rv == RetCode.EMV_USER_CANCEL)
        {
            rv = APP_EMV_USERCANCEL;
        }
        else
        {
            rv = APP_EMV_FAILED;
        }

        return rv;
    }

    public static int EmvTranStep3(int op, OnlineResult or)
    {
        int iCommuStatus = 0, iScriptLen = 0, rv = 0;
        IEmv.ACType aCType = new IEmv.ACType();
        byte[] Script = new byte[256];

        Log.i(TAG, "EmvTranStep3");

        if(TranStruct.currentTran.EntryMode != TranStruct.EM_CHIP || op != APP_EMV_ONLINE)
            return APP_EMV_OK;

        if(!(TranStruct.currentTran.TranType == TranStruct.T_SALE || TranStruct.currentTran.TranType == TranStruct.T_PREAUTHOPEN || TranStruct.currentTran.TranType == TranStruct.T_LOYALTYINSTALLMENT))
            return APP_EMV_OK;

        IEmv.OnlineResponse onlineResponse = new IEmv.OnlineResponse();
//        public class OnlineResult {
//            public static final int REFER_APPROVE = 1;
//            public static final int REFER_DENIAL = 2;
//            public static final int ONLINE_APPROVE = 0;
//            public static final int ONLINE_FAILED = 1;
//            public static final int ONLINE_REFER = 2;
//            public static final int ONLINE_DENIAL = 3;
//            public static final int ONLINE_ABORT = 4;
//
//            public OnlineResult() {
//            }
//        }
        onlineResponse.onlineResult = or.getValue();
        if (onlineResponse.onlineResult != OnlineResult.UNABLE.getValue())
            EMVSetDE55(onlineResponse);

        rv = IPlatform.get().emv.CompleteTransaction(onlineResponse);
        Log.i(TAG, "emv.CompleteTransaction:" + rv);
        EMVFillDE55(IPlatform.get().emv);
        return rv;
    }

    @Override
    public int doTran() throws Exception {
        op = EmvTranStep2();
        int rv;

        switch (op) {
            case APP_EMV_ONLINE:
                UI.ShowMessage(0, "İŞLEM ONAYLANIYOR");
                OnlineResult or = GoOnline();
                rv = EmvTranStep3(op, or);
                EMVFillDE55(emv);

                if (rv == 0 && or == OnlineResult.APPROVED) {
                    break;
                }

                // TODO: kart çıkarıldı. unbale completion reverse.
                if (rv != 0) {
                    Reversal.ReverseTran();

                    c.strcpy(currentTran.RspCode, "C3");
                    c.strcpy(currentTran.ReplyDescription, "İŞLEM BAŞARISIZ");
                    UI.ShowMessage(1500, c.ToString(currentTran.ReplyDescription));
                    Print.PrintTran(0);
                    rv = -1;
                    break;
                }

                // TODO: check offline completion result.
                if (or == OnlineResult.UNABLE) {

                    if (rv == RetCode.EMV_OK) {
                        op = APP_EMV_APPROVED;
                        //UI.ShowMessage("İŞLEM ONAYLANDI", 15000);

                        rv = 0;
                        currentTran.Offline = 1;
                        currentTran.MsgTypeId = 220;
                        Batch.GenerateTranNos();
                        Bkm.GenerateOffAuthCode();
                        c.strcpy(currentTran.RspCode, "Y3");
                        params.Stan++;
                        currentTran.Stan = params.Stan;
                    } else {
                        op = APP_EMV_DENIAL;
                        UI.ShowMessage(1500, "İŞLEM ÇİP\nTARAFINDAN\nREDDEDİLDİ");
                        if (rv == RetCode.EMV_DENIAL || rv == RetCode.EMV_NOT_ACCEPT) {
                            currentTran.Offline = 1;
                            currentTran.MsgTypeId = 220;
                            Batch.GenerateTranNos();
                            c.strcpy(currentTran.RspCode, "Z3");
                            c.strcpy(currentTran.ReplyDescription, "KART REDDETTİ");
                            params.Stan++;
                            currentTran.Stan = params.Stan;

                            Batch.SaveTran();
                            Print.PrintTran(0);
                        } else {
                            c.strcpy(currentTran.RspCode, "C5");
                            c.strcpy(currentTran.ReplyDescription, "KART REDDETTİ");
                            Print.PrintTran(0);
                        }
                        // TranStruct.ClearTranData();
                        rv = -1;
                    }
                }

                break;
            case APP_EMV_APPROVED:
                UI.ShowMessage(1500, "İŞLEM ONAYLANDI");
                rv = 0;
                currentTran.Offline = 1;
                currentTran.MsgTypeId = 220;
                Batch.GenerateTranNos();
                Bkm.GenerateOffAuthCode();
                c.strcpy(currentTran.RspCode, "Y1");
                params.Stan++;
                currentTran.Stan = params.Stan;
                break;
            case APP_EMV_DENIAL:
                UI.ShowMessage(1500, "İŞLEM ÇİP\nTARAFINDAN\nREDDEDİLDİ");
                //if(IsEMVAdviceFromCID())
                if (currentTran.TranType == TranStruct.T_SALE) {
                    currentTran.Offline = 1;
                    currentTran.MsgTypeId = 220;
                    Batch.GenerateTranNos();
                    c.strcpy(currentTran.RspCode, "Z1");
                    c.strcpy(currentTran.ReplyDescription, "KART REDDETTİ");
                    params.Stan++;
                    currentTran.Stan = params.Stan;

                    Batch.SaveTran();
                    Print.PrintTran(0);
                } else {
                    c.strcpy(currentTran.RspCode, "CD");
                    c.strcpy(currentTran.ReplyDescription, "KART REDDETTİ");
                    UI.ShowMessage(1500, c.ToString(currentTran.ReplyDescription));
                    Print.PrintTran(0);
                }
                //TranStruct.ClearTranData();
                rv = -1;
                break;
            case APP_EMV_DECLINED:
                c.strcpy(currentTran.RspCode, "C2");
                c.strcpy(currentTran.ReplyDescription, "KART REDDETTİ");
                UI.ShowMessage(1500, c.ToString(currentTran.ReplyDescription));
                Print.PrintTran(0);
                rv = -1;
                break;
//            case Emv.APP_EMV_FALLBACK:
//                return FallbackTran();
            case APP_EMV_USERCANCEL:
                UI.ShowMessage(2000, "İŞLEMDEN VAZGEÇİLDİ");
                rv = -1;
                break;
            case APP_EMV_EXIT:
                rv = -1;
                break;
            default:
                c.strcpy(currentTran.RspCode, "C3");
                c.strcpy(currentTran.ReplyDescription, "İŞLEM BAŞARISIZ");
                UI.ShowMessage(1500, c.ToString(currentTran.ReplyDescription));
                Print.PrintTran(0);
                rv = -1;
                break;
        }
        return rv;
    }

}
