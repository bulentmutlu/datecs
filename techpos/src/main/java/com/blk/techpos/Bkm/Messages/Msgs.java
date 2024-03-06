package com.blk.techpos.Bkm.Messages;

import static com.blk.sdk.c.atoi;
import static com.blk.sdk.c.memcpy;
import static com.blk.sdk.c.memset;
import static com.blk.sdk.c.sprintf;
import static com.blk.sdk.c.strcat;
import static com.blk.sdk.c.strcmp;
import static com.blk.sdk.c.strcpy;
import static com.blk.sdk.c.strlen;
import static com.blk.sdk.c.strncpy;
import static com.blk.techpos.Bkm.Messages.Msgs.MessageType.M_ENDOFDAY;
import static com.blk.techpos.Bkm.Messages.Msgs.MessageType.M_PRMDOWNLOAD;


import android.util.Log;

import com.blk.platform.IPlatform;
import com.blk.sdk.Convert;
import com.blk.sdk.Openssl;
import com.blk.sdk.Rtc;
import com.blk.sdk.UI;
import com.blk.sdk.file;
import com.blk.sdk.Iso8583;
import com.blk.sdk.Utility;
import com.blk.sdk.olib.olib;
import com.blk.sdk.c;
import com.blk.techpos.Bkm.Batch;
import com.blk.techpos.Bkm.Bkm;
import com.blk.techpos.Comms;
import com.blk.techpos.Print;
import com.blk.techpos.PrmStruct;
import com.blk.techpos.Bkm.Reversal;
import com.blk.techpos.Bkm.TranStruct;
import com.blk.techpos.techpos;
import com.blk.techpos.Bkm.VParams.PrmFileHeader;
import com.blk.techpos.Bkm.VParams.VBin;
import com.blk.techpos.Bkm.VParams.VEod;
import com.blk.techpos.Bkm.VParams.VTerm;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static com.blk.techpos.PrmStruct.params;
import static com.blk.techpos.Bkm.TranStruct.currentTran;
/**
 * Created by id on 27.02.2018.
 */
/*
isoMsg.setFieldValue("2", Arrays.copyOf(currentTran.Pan, strlen(currentTran.Pan)));
isoMsg.setFieldValue("4", currentTran.Amount);
isoMsg.setFieldValue("14", currentTran.ExpDate);

isoMsg.setFieldValue("22", Arrays.copyOf(tmpStr, strlen(tmpStr)));
isoMsg.setFieldValue("25", Arrays.copyOf(tmpStr, strlen(tmpStr)));

isoMsg.setFieldValue("32", Arrays.copyOf(tmpStr, strlen(tmpStr)));
isoMsg.setFieldValue("35", Arrays.copyOf(currentTran.Track2, Math.min(37, strlen(currentTran.Track2))));
isoMsg.setFieldValue("37", currentTran.RRN);
isoMsg.setFieldValue("38", currentTran.AuthCode);
isoMsg.setFieldValue("39", currentTran.RspCode);
isoMsg.setFieldValue("41", currentTran.TermId);
isoMsg.setFieldValue("42", currentTran.MercId);
isoMsg.setFieldValue("49", Arrays.copyOf(tmpStr, strlen(tmpStr)));
isoMsg.setFieldBin("55", Arrays.copyOf(currentTran.DE55, currentTran.DE55Len));
isoMsg.setFieldBin("63", Arrays.copyOf(buff, buffIdx));

*/

public class Msgs {

    private static final String TAG = Msgs.class.getSimpleName();

    public enum MessageType {
        M_NULL(0),
        M_AUTHORIZATION(1),
        M_OFFLINEADVICE(2),
        M_REVERSAL(3),
        M_ENDOFDAY(4),
        M_BATCHUPLOAD(5),
        M_PRMDOWNLOAD(6),
        M_HANDSHAKE(7),
        M_KEYEXCHANGE(8),

        //odeal
        M_INIT(9),
        M_FINISH(10),
        M_BATCHREPORT(11);

        private final int id;

        MessageType(int id) {
            this.id = id;
        }

        public int getValue() {
            return id;
        }

        public String getString() {
            switch (id) {
                case 0:
                    return "M_NULL";
                case 1:
                    return "M_AUTHORIZATION";
                case 2:
                    return "M_OFFLINEADVICE";
                case 3:
                    return "M_REVERSAL";
                case 4:
                    return "M_ENDOFDAY";
                case 5:
                    return "M_BATCHUPLOAD";
                case 6:
                    return "M_PRMDOWNLOAD";
                case 7:
                    return "M_HANDSHAKE";
                case 8:
                    return "M_KEYEXCHANGE";
            }
            return "";
        }
    }

    static byte[] MsgBuff = new byte[4096];
    static final int bkmHeaderLen = 27;

    public static boolean IsPrintRED()
    {
        if (strcmp(currentTran.RspCode, "00") && strcmp(currentTran.RspCode, "55") && strcmp(currentTran.RspCode, "95"))
            return true;
        return false;
    }

    public static int ProcessMsg(MessageType msgType) throws Exception {
        int rv = -1;
        byte[] reqMsg;
        byte[] respMsg;

        try {

            if (Utility.IsNullorEmpty(currentTran.DateTime))
                memcpy(currentTran.DateTime, Rtc.GetDateTime(), currentTran.DateTime.length);

            Log.i(TAG, String.format("ProcessMsg %s %d %d", msgType.getString(), currentTran.MsgTypeId, currentTran.ProcessingCode));

//            if (msgType == M_INIT || msgType == M_FINISH || msgType == M_BATCHREPORT) {
//                String initms = Msgs.PrepareInitMsg();
//                respMsg = new Comms().SendRecvOdeAl(reqMsg, reqMsg.length);
//                if (respMsg != null) {
//                    rv = Msgs.ParseMsg(msgType, respMsg);
//                }
//                return 0;
//            }

            if (ReversalMessage.ProcessReversal(msgType) == 0 || (msgType == M_ENDOFDAY)) {
                reqMsg = Msgs.PrepareMsg(msgType);

                respMsg = new Comms().CommsSendRecv(msgType, reqMsg, reqMsg.length);
                if (respMsg != null) {
                    rv = Msgs.ParseMsg(msgType, respMsg);
                    if (strcmp(currentTran.RspCode, "00"))
                        Reversal.RemoveReversalTran();

                    if (IsPrintRED()) {
                        Print.PrintRED();
                    }
                } else {
                    if (currentTran.ProcessingCode == 400002) {
                        return -999;
                    }
                    if (currentTran.ProcessingCode != 950000) {
                        if (strlen(currentTran.RspCode) <= 0) {
                            strcpy(currentTran.RspCode, "C1");
                            strcpy(currentTran.ReplyDescription, "   CEVAP ALINAMADI  ");
                            strcat(currentTran.ReplyDescription, "  TEKRAR  DENEYİNİZ ");
                        }

                        if (!(currentTran.emvOnlineFlow != 0 && currentTran.unableToGoOnline != 0)) {
                            if (!((currentTran.MsgTypeId == 220) || (currentTran.MsgTypeId == 120) || (currentTran.MsgTypeId == 800)))
                                Print.PrintTran(0);

                            Log.i(TAG, "------sub resp code 3");//***
                            Utility.logDump(currentTran.ReplyDescription, 80);
                            UI.ShowMessage(2000, c.ToString(currentTran.ReplyDescription));
                        }
                    }
                    rv = -999;
                }
            } else
                rv = -998;

        } catch (Exception e) {
            e.printStackTrace();
            rv = -988;
        }

        return rv;
    }
    public static byte[] PrepareMsg(MessageType MsgType) throws Exception {
        byte[] tmpStr = new byte[64];
        Rtc now = Rtc.RtcGet();
        int rv = 0, len = 0;
        short msgLen = 0;
        byte encryptMsg = 1;

        Iso8583 isoMsg = new Iso8583();

        try {
            memset(MsgBuff, (byte) 0, MsgBuff.length);

        if (MsgType != Msgs.MessageType.M_OFFLINEADVICE && MsgType != Msgs.MessageType.M_REVERSAL && MsgType != Msgs.MessageType.M_BATCHUPLOAD) {
            params.Stan++;
            PrmStruct.Save("Stan");
            currentTran.Stan = params.Stan;
        }

        if (MsgType == Msgs.MessageType.M_AUTHORIZATION) {
            Batch.GenerateTranNos();
        }

        sprintf(tmpStr, "%04d", currentTran.MsgTypeId);
            isoMsg.setFieldValue("0", new String(tmpStr, 0, 4));

        sprintf(tmpStr, "%06d", currentTran.ProcessingCode);
        isoMsg.setFieldValue("3", new String(tmpStr, 0, 6));
        sprintf(tmpStr, "%06d", currentTran.Stan);
        isoMsg.setFieldValue("11", new String(tmpStr, 0, 6));

        if (currentTran.DateTime[0] != 0) {
            sprintf(tmpStr, "%02d%02d%02d", (int) currentTran.DateTime[3], (int) currentTran.DateTime[4], (int) currentTran.DateTime[5]);
            isoMsg.setFieldValue("12", new String(tmpStr, 0, 6));
            sprintf(tmpStr, "%02d%02d", (int) currentTran.DateTime[1], (int) currentTran.DateTime[2]);
            isoMsg.setFieldValue("13", new String(tmpStr, 0, 4));
        } else {
            sprintf(tmpStr, "%02d%02d%02d", now.hour, now.min, now.sec);
            isoMsg.setFieldValue("12", new String(tmpStr, 0, 6));
            sprintf(tmpStr, "%02d%02d", now.mon, now.day);
            isoMsg.setFieldValue("13", new String(tmpStr, 0, 4));
        }

            isoMsg.setFieldValue("43", Bkm.GetDeviceInfo());

        switch (MsgType) {
            case M_AUTHORIZATION:
                rv = Authorization.PrepareAuthorizationMsg(isoMsg);
                break;
            case M_OFFLINEADVICE:
                rv = OfflineAdvice.PrepareOfflineAdviceMsg(isoMsg);
                break;
            case M_REVERSAL:
                rv = ReversalMessage.PrepareReversalMsg(isoMsg);
                break;
            case M_ENDOFDAY:
                rv = EndOfDay.PrepareEndofDayMsg(isoMsg);
                break;
            case M_BATCHUPLOAD:
                rv = BatchUpload.PrepareBatchUploadMsg(isoMsg);
                break;
            case M_PRMDOWNLOAD:
                rv = ParameterDownload.PreparePrmDownloadMsg(isoMsg);
                break;
            case M_HANDSHAKE:
                rv = HandShake.PrepareHandShakeMsg(isoMsg);
                break;
            case M_KEYEXCHANGE:
                rv = KeyExchange.PrepareKeyExchangeMsg(isoMsg);
                encryptMsg = 0;
                break;
            default:
                rv = -1;
                break;
        }

            isoMsg.Dump();
            byte[] packed = isoMsg.pack();
        memcpy(MsgBuff, bkmHeaderLen, packed, 0, packed.length);
        len = packed.length;
        techpos.CRC32(MsgBuff, bkmHeaderLen, len, MsgBuff, len + bkmHeaderLen);
        len += 4;

        Log.i(TAG, "ISO : " + Convert.Buffer2Hex(packed));
            //Utils.DumpISO(TAG, "REQUEST", isoMsg.unpack(packed, false));

        if (encryptMsg != 0) {
            len = EncryptMsg(MsgBuff, bkmHeaderLen, len, params.MSK);
        }

        // Add Bkm header
        len += bkmHeaderLen;

        msgLen = (short) (len - 2);
        msgLen = Convert.SWAP_UINT16(msgLen);
        memcpy(MsgBuff, Convert.ToArray(msgLen), 2);
        MsgBuff[2] = 1;
        MsgBuff[3] = 23;
        MsgBuff[4] = encryptMsg;
            MsgBuff[5] = Bkm.VendorID;

            sprintf(tmpStr, "%12s", Bkm.GetDeviceInfo().substring(0, 12));
        memcpy(MsgBuff, 6, tmpStr, 0, 12);
        //memcpy(MsgBuff, 18,  "    ".getBytes(), 0, 4);

        sprintf(tmpStr, "%04d", currentTran.MsgTypeId);
        Convert.EP_ascii2bcd(MsgBuff, 22, tmpStr, 4);
        sprintf(tmpStr, "%06d", currentTran.ProcessingCode);
        Convert.EP_ascii2bcd(MsgBuff, 24, tmpStr, 6);
        }
        finally {
            isoMsg.Free();
        }


        //len(0067) 01 17 00 06 454441544130303032373931 00000000 0800 810000 iso(080020380000002000028100000000051405050321454441544130303032373931303030304452503930303230363932343837303030303030303030300011010008B6FCE1DAD8F16FDE CRC(E33F8F7E)
        //0067      01 17 00 06 454441544130393031393233 00000000 0800 810000     0800203800000020000281000000000114375003214544415441303930313932333030303044525039303032303639323438373030303030303030303000110100087D28E3D66EE0557C     2FB8FA03
        if (rv != 0) return null;

        return Arrays.copyOf(MsgBuff, len);
    }
    public static int ParseMsg(MessageType MsgType, byte[] resp) throws Exception {
        int i = 0;
        byte[] respMsgId = new byte[16];
        byte[] tmpStr;
        byte[] tmpB = new byte[32];
        byte[] tmpBuff;
        int tmpBuffLen = 0;
        byte[] tlvData = new byte[1024];
        short tlvDataLen = 0;
        Rtc now = Rtc.RtcGet();
        final int crcLen = 4; // ?
        byte[] isoData = null; // new byte[resp.length - bkmHeaderLen - 4];


// 003D011700064544415441303030323839322020202000000000000000003800000A00000048496511025903053830363431313837383638353330144EBFA9
        if(resp[4] != 0)
        {
            byte[] encrypted = Arrays.copyOfRange(resp, bkmHeaderLen, resp.length);
            byte[] iso_crc = DecryptMsg2(params.MSK, encrypted, 0, encrypted.length);
            isoData = Arrays.copyOfRange(iso_crc, 0, iso_crc.length - crcLen);

        }
        else {
            isoData = Arrays.copyOfRange(resp, bkmHeaderLen, resp.length - crcLen);
        }
        //Log.i(TAG, "ParseMsg : " + Convert.Buffer2Hex(resp));
        Log.i(TAG, "ISO : " + Convert.Buffer2Hex(isoData));

        HashMap<String, byte[]> isoMsg = null;
        try {
            isoMsg = Iso8583.unpack(isoData, false);
            //Utils.DumpISO(TAG, "RESPONSE", isoMsg);

        } catch (Exception e) {
            e.printStackTrace();
            strcpy(currentTran.RspCode, "D0");
            strcpy(currentTran.ReplyDescription, "HATALI CEVAP");
            return -1;
        }

        sprintf(respMsgId, "%04d", (currentTran.MsgTypeId + 10));
        if (!isoMsg.containsKey("0") || !new String(isoMsg.get("0")).equals(new String(respMsgId, 0, 4)))
        {
            strcpy(currentTran.RspCode, "D0");
            strcpy(currentTran.ReplyDescription, "HATALI CEVAP");
            return -2;
        }
        currentTran.MsgTypeId = atoi(respMsgId);

        if (isoMsg.containsKey("3"))
            currentTran.ProcessingCode = atoi(isoMsg.get("3"));

        if (isoMsg.containsKey("12")) {
            tmpStr = isoMsg.get("12");

            memcpy(tmpB, 0, tmpStr, 0, 2);
            currentTran.DateTime[3] = (byte) atoi(tmpB);
            memcpy(tmpB, 0, tmpStr, 2, 2);
            currentTran.DateTime[4] = (byte) atoi(tmpB);
            memcpy(tmpB, 0, tmpStr, 4, 2);
            currentTran.DateTime[5] = (byte) atoi(tmpB);
        }

        if (isoMsg.containsKey("13")) {
            tmpStr = isoMsg.get("13");
            memset(tmpB, (byte) 0, tmpB.length);
            memcpy(tmpB, 0, tmpStr, 0, 2);
            currentTran.DateTime[1] = (byte) atoi(tmpB);
            memcpy(tmpB, 0, tmpStr, 2, 2);
            currentTran.DateTime[0] = (byte) now.year;
            currentTran.DateTime[2] = (byte) atoi(tmpB);
        }

//		if((now.year != currentTran.DateTime[0]) || (now.mon != currentTran.DateTime[1]) || (now.hour != currentTran.DateTime[3]) || (now.min != currentTran.DateTime[4]) || (now.sec != currentTran.DateTime[5]))
//		{
//			//memcpy(&now, currentTran.DateTime, sizeof(Rtc));
//            IPlatform.get().system.setSystemTime(
//                    currentTran.DateTime[0] + 2000, currentTran.DateTime[1], currentTran.DateTime[2],
//                    currentTran.DateTime[3], currentTran.DateTime[4], currentTran.DateTime[5]);
//            Utility.EP_printf("Date Time Setted");
//		}

        if (isoMsg.containsKey("37")) {
            tmpStr = isoMsg.get("37");
            strncpy(currentTran.RRN, tmpStr, 12);
        }

        if (isoMsg.containsKey("38")) {
            tmpStr = isoMsg.get("38");
            strncpy(currentTran.AuthCode, tmpStr, 6);
        }


        if (isoMsg.containsKey("39")) {
            tmpStr = isoMsg.get("39");
            strncpy(currentTran.RspCode, tmpStr, 2);
        }

        if (isoMsg.containsKey("55")) {
            tmpBuffLen = isoMsg.get("55").length;
            tmpBuff = isoMsg.get("55");

            if (!strcmp(currentTran.RspCode, "55"))
                ;//EP_printf("----return 55");
            else {
                currentTran.DE55Len = (short) tmpBuffLen;
                memset(currentTran.DE55, (byte) 0, currentTran.DE55.length);
                memcpy(currentTran.DE55, tmpBuff, tmpBuffLen);
            }
        }

        if (MsgType == Msgs.MessageType.M_AUTHORIZATION && isoMsg.containsKey("62")) {
            tmpBuffLen = isoMsg.get("62").length;
            tmpBuff = isoMsg.get("62");

            for (i = 1; i <= 6; i++) {
                tlvDataLen = techpos.GetTLVData(tmpBuff, tmpBuffLen, i, tlvData, (short) tlvData.length);
                if (tlvDataLen > 0) {
                    if (tlvDataLen > 160)
                        tlvDataLen = 160;


                    memset(currentTran.FreeFrmtPrntData[i - 1], (byte) 0, 160);
                    memcpy(currentTran.FreeFrmtPrntData[i - 1], tlvData, tlvDataLen);
                }
            }
        }

        if (isoMsg.containsKey("48")) {
            tmpBuffLen = isoMsg.get("48").length;
            tmpBuff = isoMsg.get("48");

            //Reply Description
            tlvDataLen = techpos.GetTLVData(tmpBuff, tmpBuffLen, 0x0B, tlvData, (short) tlvData.length);
            if (tlvDataLen > 0) {
                if (tlvDataLen > 80)
                    tlvDataLen = 80;

                memcpy(currentTran.ReplyDescription, tlvData, tlvDataLen);
                // EP_printf("Reply Description:%s", currentTran.ReplyDescription);
            }

            //Sub Reply Description
            if (techpos.GetTLVData(tmpBuff, tmpBuffLen, 0x0C, tlvData, (short) tlvData.length) > 0) {
                currentTran.SubReplyCode = Convert.ToShort(tlvData, 0);
                currentTran.SubReplyCode = Convert.SWAP_UINT16(currentTran.SubReplyCode);
                //EP_printf("Sub Reply Code:%d", currentTran.SubReplyCode);
                tlvDataLen = tlvData[2];
                if (tlvDataLen > 40)
                    tlvDataLen = 40;
                memcpy(currentTran.SubReplyDescription, 0, tlvData, 3, tlvDataLen);
                //EP_printf("Sub Reply Description:%s", currentTran.SubReplyDescription);
            }

            if (techpos.GetTLVData(tmpBuff, tmpBuffLen, 0x0D, tlvData, (short) tlvData.length) > 0) {
                params.DownloadParamsFlag = (tlvData[0] + 1) % 4;
                params.KeyExchangeFlag = (tlvData[1] + 1) % 4;
                PrmStruct.Save("DownloadParamsFlag");
                PrmStruct.Save("KeyExchangeFlag");
            }

            //Slip Format
            tlvDataLen = techpos.GetTLVData(tmpBuff, tmpBuffLen, 0x0E, tlvData, (short) tlvData.length);
            if (tlvDataLen > 0) {
                if (tlvDataLen > 64)
                    tlvDataLen = 64;

                memcpy(currentTran.SlipFormat, tlvData, tlvDataLen);
                //EP_printf("Slip Format");
                //EP_HexDump(currentTran.SlipFormat, tlvDataLen);
            }

            //Banka Referans Bilgisi
            tlvDataLen = techpos.GetTLVData(tmpBuff, tmpBuffLen, 0x0F, tlvData, (short) tlvData.length);
            if (tlvDataLen > 0) {
                if (tlvDataLen > 16)
                    tlvDataLen = 16;

                memcpy(currentTran.BankRefNo, tlvData, tlvDataLen);
                //EP_printf("BankRefNo:%s", currentTran.BankRefNo);
            }

            if (MsgType == M_ENDOFDAY || MsgType == M_PRMDOWNLOAD) {
                tlvDataLen = techpos.GetTLVData(tmpBuff, tmpBuffLen, 0x16, tlvData, (short) tlvData.length);
                byte[] hostdatetime = new byte[7];
                memcpy(hostdatetime, tlvData, tlvDataLen);
                setDeviceDateTime(hostdatetime);
            }

        }

        int rv = 0;
        if (currentTran.f39OK()) {
            //rv = 0;
            switch (MsgType) {
                case M_AUTHORIZATION:
                    rv = Authorization.ParseAuthorizationMsg(isoMsg);
                    break;
                case M_OFFLINEADVICE:
                    rv = OfflineAdvice.ParseOfflineAdviceMsg(isoMsg);
                    break;
                case M_REVERSAL:
                    rv = ReversalMessage.ParseReversalMsg(isoMsg);
                    break;
                case M_ENDOFDAY:
                    rv = EndOfDay.ParseEndofDayMsg(isoMsg);
                    break;
                case M_BATCHUPLOAD:
                    rv = BatchUpload.ParseBatchUploadMsg(isoMsg);
                    break;
                case M_PRMDOWNLOAD:
                    rv = ParameterDownload.ParsePrmDownloadMsg(isoMsg);
                    break;
                case M_HANDSHAKE:
                    rv = HandShake.ParseHandShakeMsg(isoMsg);
                    break;
                case M_KEYEXCHANGE:
                    rv = KeyExchange.ParseKeyExchangeMsg(isoMsg);
                    break;
                default:
                    break;
            }
        } else {
            if (currentTran.ReplyDescription[0] == 0)
                strcpy(currentTran.ReplyDescription, "İŞLEM BAŞARISIZ");
        }

        return rv;
    }
    public static int ProcessSignals(int signal) throws Exception {
        int rv = 0;
        TranStruct tBck;
        boolean fHandled = false;

        Log.i(TAG, String.format("ProcessSignals(%d) %d %d", signal, params.KeyExchangeFlag, params.DownloadParamsFlag));

        if (params.KeyExchangeFlag == signal) {
            tBck = TranStruct.currentTran;
            TranStruct.currentTran = new TranStruct();

            rv = KeyExchange.ProcessKeyExchange();
            if (rv == 0) {
                params.KeyExchangeFlag = 0;
            }

            fHandled = true;
            TranStruct.currentTran = tBck;
        }

        if (params.DownloadParamsFlag == signal) {
            tBck = TranStruct.currentTran;
            TranStruct.currentTran = new TranStruct();

            rv = ParameterDownload.ProcessDownloadPrms();
            Print.PrintAppPrms(params.BkmParamStatus);
            if (rv == 0) {
                params.DownloadParamsFlag = 0;
            }

            fHandled = true;
            TranStruct.currentTran = tBck;
        }
        if (fHandled)
            PrmStruct.Save();
        return rv;
    }

    public static void Add2ByteLen(byte[] dst, int dstOffset, short _2byteLen)
    {
        memcpy(dst, dstOffset, Convert.ToArray(Convert.SWAP_UINT16(_2byteLen)), 0, 2);
    }
    public static int Get2ByteLen(byte[] source, int offset)
    {
        return Convert.unsignedByteToInt(source[offset]) * 256 + Convert.unsignedByteToInt(source[offset + 1]);
    }
    static byte[] GetPrmListData() throws Exception {
        byte[] prmList = new byte[1024];
        int idx = 1, fd = 0;
        byte prmCnt = 0;
        PrmFileHeader hdr = new PrmFileHeader();
        short tmpType = 0;
        String[] paramFiles = new String[]{
                VTerm.PRMFILE, VBin.PRMFILE, VEod.PRMFILE, "VCOMM", "VEMVCON", "VEMVCLSPP3",
                "VEMVCLSPP2", "VEMVCLSPW2", Bkm.file_VCAPK, "dummyforprmtype10", "VSpecialBIN"};

        for (int i = 0; i < paramFiles.length; ++i) {
            if (!file.Exist(paramFiles[i])) continue;

            olib.ReadFile(hdr, new file(paramFiles[i])); // hdr.ReadFromFile(new file(paramFiles[i], file.OpenMode.EP_RDONLY));
            Log.i(TAG, String.format("prm type:%d - %02X%02X%02X%02X", hdr.PrmType, hdr.PrmVer[0], hdr.PrmVer[1], hdr.PrmVer[2], hdr.PrmVer[3]));
            if (hdr.PrmType == i + 1) {
                prmCnt++;
                tmpType = Convert.SWAP_UINT16(hdr.PrmType);
                memcpy(prmList, idx, Convert.ToArray(tmpType), 0, 2);
                idx += 2;

                memcpy(prmList, idx, hdr.PrmVer, 0, 4);
                idx += 4;
            }
        }


        prmList[0] = prmCnt;

        //EP_printf("GetPrmListData():%d", *len);
        //EP_HexDump(prmList, *len);

        return Arrays.copyOf(prmList, idx);
    }
    static void setDeviceDateTime(byte[] tlvData) {
        //tlvdata string değere dönüştürüldü
        byte[] ss = Convert.bcd2Str(tlvData);
        String s = c.ToString(ss);

        char year[] = new char[4];
        char mounth[] = new char[2];
        char day[] = new char[2];
        char hour[] = new char[2];
        char minute[] = new char[2];
        char second[] = new char[2];

        //string değerler ayıklanarak yıl,ay,gün... elde edildi
        s.getChars(0, 4, year, 0);
        s.getChars(4, 6, mounth, 0);
        s.getChars(6, 8, day, 0);
        s.getChars(8, 10, hour, 0);
        s.getChars(10, 12, minute, 0);
        s.getChars(12, 14, second, 0);

        //char değerler int değişkenlerine dönüştürüldü
        int yearx = Integer.parseInt(String.valueOf(year));
        int mounthx = Integer.parseInt(String.valueOf(mounth));
        int dayx = Integer.parseInt(String.valueOf(day));
        int hourx = Integer.parseInt(String.valueOf(hour));
        int minutex = Integer.parseInt(String.valueOf(minute));
        int secondx = Integer.parseInt(String.valueOf(second));

        if (checkDateTime(yearx, mounthx, dayx, hourx, minutex, secondx)) {
            //gelen değerler cihazın tarihi olarak ayarlandı.
            // java.lang.SecurityException: setTime: Neither user 10063 nor current process has android.permission.SET_TIME.
//            Calendar c = Calendar.getInstance();
//            c.set(yearx, mounthx-1, dayx, hourx, minutex, secondx);
//            AlarmManager am = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
//            am.setTime(c.getTimeInMillis());
            IPlatform.get().system.setSystemTime(yearx, mounthx, dayx, hourx, minutex, secondx);



        }
    }
    static boolean checkDateTime(int year, int mounth, int day, int hour, int minute, int second) {

        if (year < 2018 || year > 3000 || mounth > 12 || day > 31 || hour > 24 || minute > 59 || second > 59) {
            return false;
        } else {
            return true;
        }

    }
    static int EncryptMsg(byte[] in, int inOffset, int len, byte[] key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        byte[] cipherText = new byte[0];

//        in = Convert.Hex2Buffer("08002038000000200002710000000328112141032443413231373137303031363030303030424C4B5633303031303033363437303030303030303030300052070031080001000000010002000010600003000000350004000000170005000000E60006000001C20008000001C2000900000140FC74B6B5".getBytes());
//        key = Convert.Hex2Buffer("9C69978602DAD60F14FED169E2DE3E3B".getBytes());
//        inOffset = 0;
//        len = in.length;

        cipherText = Openssl.EncryptMsg(key, Arrays.copyOfRange(in, inOffset, inOffset + len), len);

        //////////////////////////////////////////////////////

//        byte[] myIV = new byte[] {0, 0, 0, 0, 0, 0, 0, 0};// initialization vector
//        Cipher c3des = Cipher.getInstance("DESede/CBC/PKCS5Padding");
//        SecretKeySpec myKey = new SecretKeySpec(key, "DESede");
//        IvParameterSpec ivspec = new IvParameterSpec(myIV);
//        c3des.init(Cipher.ENCRYPT_MODE, myKey, ivspec);
//        cipherText = c3des.doFinal(in, inOffset, len);

        /////////////////////////////////////////////////////////

        //cipherText = Utils.TDES.Encrypt(key, in, inOffset, len);

        memcpy(in, inOffset, cipherText, 0, cipherText.length);
        return cipherText.length;
    }
    public static byte[] DecryptMsg2(byte[] key, byte[] in, int inOffset, int len) throws Exception {
//        in = Convert.Hex2Buffer("E383264AB62A4C2162480C2CE9B3ABC5E6B50DEC34F0C4324B8B499CE0BD0E4E785784121327FBA0E95E5A42406806DE9303CF7C3747000E2425ED003F6286A51EBA62D2315942E7F609F5A41260E2DFAA0959ABEEE9CA9A04855FB316C27697AAA0DB27EF8E382AA2F16D53A98EB986FA4F32020443618AB352CDA8488F71B4BE2BC89013F0A2D9D21E93715C72F0A9".getBytes());
//        key = Convert.Hex2Buffer("70140F7064BB76CD449E1BA346922B21".getBytes());
//        inOffset = 0;
//        len = in.length;
        byte[] r =  Openssl.DecryptMsg(key, Arrays.copyOfRange(in, inOffset, inOffset + len), len);
        //return Utils.TDES.Decrypt(params.MSK, in, inOffset, len);
        //byte[] r = DecryptMsg(key, in, 0, in.length);
        return r;
    }
//    static byte[] DecryptMsg(byte[] key, byte[] in, int inOffset, int len) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
//
//        int i = 0;
//        byte[] out = new byte[len + 8];
//        byte[] DesKey;
//        byte[] cipherText = new byte[0];
//        byte[] tdesKeyData = key;// your encryption key
//        byte[] myIV = new byte[] {0, 0, 0, 0, 0, 0, 0, 0};// initialization vector
//
//        Cipher c3des = Cipher.getInstance("DESede/CBC/PKCS5Padding");
//        //Cipher c3des = Cipher.getInstance("DESede/CBC/NoPadding");
//        SecretKeySpec myKey = new SecretKeySpec(tdesKeyData, "DESede");
//        IvParameterSpec ivspec = new IvParameterSpec(myIV);
//
//        c3des.init(Cipher.DECRYPT_MODE, myKey, ivspec);
////        cipherText = c3des.doFinal(in, inOffset, len);
////
////        memcpy(in, inOffset, cipherText, 0, cipherText.length);
////
////        return cipherText.length;
//        return c3des.doFinal(in, inOffset, len);
//        //byte[] r = pad(in); return c3des.doFinal(r, 0, r.length);
//    }
}
