package com.blk.techpos.Bkm.Messages;

import static com.blk.sdk.c.memcpy;
import static com.blk.sdk.c.memset;
import static com.blk.sdk.c.sprintf;
import static com.blk.sdk.c.strcpy;
import static com.blk.sdk.c.strlen;
import static com.blk.techpos.Bkm.TranStruct.EM_QR;
import static com.blk.techpos.PrmStruct.params;
import static com.blk.techpos.Bkm.TranStruct.currentTran;


import android.util.Log;

import com.blk.sdk.Convert;
import com.blk.sdk.Iso8583;
import com.blk.sdk.c;
import com.blk.techpos.Bkm.TranStruct;
import com.blk.techpos.techpos;
import com.blk.techpos.tran.Tran;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by id on 20.03.2018.
 */


//--------------------------------AUTHORIZATION  REQUEST ISO8583 MSG ---------------------------------
//[00] [004] 0200
//[03] [006] 000000
//[04] [012] 000000010000
//[11] [006] 000029
//[12] [006] 101559
//[13] [004] 0324
//[22] [004] 0051
//[25] [002] 00
//[32] [004] 0001
//[35] [038] 5492087568040175D16102012501050900000F
//[41] [008] 10000547
//[42] [015] 000000000000002
//[43] [040] CA21717001600000BLKV30010036470000000000
//[49] [004] 0949
//[55] [183] 820238008407A0000000041010950500000080009A031403249C01005F2A0209499F02060000000100009F03060000000000009F37048EB9CDED9F1A0207929F3303E0F0C85F3401009F090200029F1E0837313730303136309F34034403029F3501259F
//[63] [021] 0C0012000004000002000000000000000004000001
//          Tag(0C) Len(18) Value 000004000002000000000000000004000001
//----------------------------------------------------------------------------------------------------
//CommsConnect(031.145.171.094:12121)
//CommsSend(363)
//CommRecv: 683  DM_REVERSAL(0)
//ParseMsg(AUTHORIZATION, 683)
//--------------------------------AUTHORIZATION  RESPONSE ISO8583 MSG --------------------------------
//[00] [004] 0210
//[03] [006] 000000
//[11] [006] 000029
//[12] [006] 101559
//[13] [004] 0324
//[37] [012] 117410565334
//[38] [006] 000030
//[39] [002] 00
//[41] [008] 10000547
//[42] [015] 000000000000002
//[48] [089] 0100060000000100000A000343444D0B000FDDDE4C454D204F4E41594C414E44490E002202010C48424B544F45495355444343001148424B543132333435364F4553554443430F001030303030313137343130353635333334
//          Tag(01) Len(06) Value 000000010000
//          Tag(0A) Len(03) Value 43444D
//          Tag(0B) Len(15) Value DDDE4C454D204F4E41594C414E4449
//          Tag(0E) Len(34) Value 02010C48424B544F45495355444343001148424B543132333435364F455355444343
//          Tag(0F) Len(16) Value 30303030313137343130353635333334
//[55] [016] 910A263854CABDA9C41700108A023030
//[62] [475] 03001B3C4B3E544553545055414E3A2020202020302C3030544C3C2F4B3E0400A03C533E2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A0A3C2F533E3C533E2020202020425520DDDE4C454D444520
//          Tag(03) Len(27) Value 3C4B3E544553545055414E3A2020202020302C3030544C3C2F4B3E
//          Tag(04) Len(160) Value 3C533E2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A0A3C2F533E3C533E2020202020425520DDDE4C454D444520574F524C4444454E2053DD5A4520D65A454C0A20202020202020203020454B2054
//          Tag(05) Len(160) Value 3C533E203020415920455254454C454E4DDDDE54DD522E0A2020202020414C49DE564552DDDE205455544152494E495A2031362F30332F323031380A20544152DD48DD4E444520454B535452454EDD5A452059414E534954494C4143414B5449522E0A20
//          Tag(06) Len(116) Value 3C533E2020DDDE4C454D20534F4E5543554E444120454B53545241203020574F524C445055414E0A20202020202020202028302E303020544C29204B415A414E44494E495A2E0A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A2A
//----------------------------------------------------------------------------------------------------


public class Authorization {
    private static final String TAG = Authorization.class.getSimpleName();

    public static int PrepareAuthorizationMsg(Iso8583 isoMsg) throws Exception {
        int rv = 0;
        byte[] tmpStr = new byte[64];
        int tmpLen = 0;
        byte[] buff = new byte[1024];
        int buffIdx = 0;

        if(currentTran.Amount[0] != 0 || (currentTran.TranType == TranStruct.T_LOYALTYBONUSINQUIRY)
                || (currentTran.TranType == TranStruct.T_LOYALTYBONUSSPEND) || currentTran.EntryMode == EM_QR) {
            if (currentTran.Amount[0] == 0)
                memset(currentTran.Amount, (byte) '0', currentTran.Amount.length);
            isoMsg.setFieldValue("4", currentTran.Amount);
        }

        sprintf(tmpStr, "%03X1", currentTran.EntryMode);
        isoMsg.setFieldValue("22", Arrays.copyOf(tmpStr, strlen(tmpStr)));

        sprintf(tmpStr, "%02X", currentTran.ConditionCode);
        isoMsg.setFieldValue("25", Arrays.copyOf(tmpStr, strlen(tmpStr)));

        sprintf(tmpStr, "%02X%02X", currentTran.AcqId[0], currentTran.AcqId[1]);
        isoMsg.setFieldValue("32", Arrays.copyOf(tmpStr, strlen(tmpStr)));

        if(currentTran.TranType == TranStruct.T_VOID)
        {
            isoMsg.setFieldValue("37", currentTran.RRN);
            isoMsg.setFieldValue("38", currentTran.AuthCode);
            isoMsg.setFieldValue("39", currentTran.RspCode);
        }

        isoMsg.setFieldValue("41", currentTran.TermId);
        isoMsg.setFieldValue("42", currentTran.MercId);

        sprintf(tmpStr, "%02X%02X", currentTran.CurrencyCode[0], currentTran.CurrencyCode[1]);
        isoMsg.setFieldValue("49", Arrays.copyOf(tmpStr, strlen(tmpStr)));


        if(currentTran.EntryMode == TranStruct.EM_MANUAL || currentTran.EntryMode == EM_QR)
        {
            if (strlen(currentTran.Pan) > 0) {
                isoMsg.setFieldValue("2", Arrays.copyOf(currentTran.Pan, strlen(currentTran.Pan)));
                if (strlen(currentTran.ExpDate) == 0) strcpy(currentTran.ExpDate, "0000");
                isoMsg.setFieldValue("14", currentTran.ExpDate);
            }
        }
//        else if (currentTran.EntryMode == TranStruct.EM_QR) {
//            isoMsg.setFieldValue("57", currentTran.QRRelatedData);
//        }
	    else if (currentTran.EntryMode != EM_QR)
        {
            byte[] f35 = new byte[38];
            memset(f35, (byte) 0xFF, f35.length); // puan sorguda track bilgisi bos gidecek
            if (strlen(currentTran.Track2) > 0)
                memcpy(f35, currentTran.Track2, Math.min(f35.length, currentTran.Track2.length));

            for (int i = 0; i < f35.length; ++i) {
                if (f35[i] == (byte) 0x3D)
                    f35[i] = (byte) 0x44;
            }
            f35[37] = (byte) 0x46;

//            byte[] t = new byte[40];
//            memset(t, (byte) 0xFF, t.length);
//            int actLen = strlen(currentTran.Track2);
//            Convert.EP_ascii2bcd(t, 0, currentTran.Track2, actLen);
//            tmpLen = actLen/2;
//            if(actLen % 2 != 0)
//                tmpLen += 1;

            isoMsg.setFieldValue("35", Arrays.copyOf(f35, strlen(f35))); // Arrays.copyOf(thread1, tmpLen));
        }

        if(currentTran.PinBlock[0] != 0)
            isoMsg.setFieldBin("52", currentTran.PinBlock);

        if(currentTran.DE55Len > 0)
            isoMsg.setFieldBin("55", Arrays.copyOf(currentTran.DE55, currentTran.DE55Len));

        //Build Field 63
        buffIdx = 0;
        if(currentTran.InsCount > 0)
        {
            buff[buffIdx++] = 0x0A;
            memcpy(buff, buffIdx, new byte[] {0x00, 0x01},0,  2);
            buffIdx += 2;
            buff[buffIdx++] = currentTran.InsCount;
        }

        if(currentTran.BonusAmount[0] != 0)
        {
            buff[buffIdx++] = 0x0B;
            memcpy(buff, buffIdx, new byte[] {0x00, 0x06}, 0, 2);
            buffIdx += 2;
            Convert.EP_ascii2bcd(buff, buffIdx, currentTran.BonusAmount, 12);
            buffIdx += 6;
        }

        buff[buffIdx++] = 0x0C;
        memcpy(buff, buffIdx, new byte[] {0x00, 0x12}, 0, 2);
        buffIdx += 2;
        sprintf(tmpStr, "%06d", params.BatchNo);
        Convert.EP_ascii2bcd(buff, buffIdx, tmpStr, 6);
        buffIdx += 3;
        sprintf(tmpStr, "%06d", currentTran.TranNo);
        Convert.EP_ascii2bcd(buff, buffIdx, tmpStr, 6);
        buffIdx += 3;
        sprintf(tmpStr, "%06d", currentTran.BatchNoLOnT);
        Convert.EP_ascii2bcd(buff, buffIdx, tmpStr, 6);
        buffIdx += 3;
        sprintf(tmpStr, "%06d", currentTran.TranNoLOnT);
        Convert.EP_ascii2bcd(buff, buffIdx, tmpStr, 6);
        buffIdx += 3;
        sprintf(tmpStr, "%06d", currentTran.BatchNoLOffT);
        Convert.EP_ascii2bcd(buff, buffIdx, tmpStr, 6);
        buffIdx += 3;
        sprintf(tmpStr, "%06d", currentTran.TranNoLOffT);
        Convert.EP_ascii2bcd(buff, buffIdx, tmpStr, 6);
        buffIdx += 3;

        
        Log.i(TAG, String.format("Batch Tran No Infos"));
        Log.i(TAG, String.format("BatchNo:%d", params.BatchNo));
        Log.i(TAG, String.format("TranNo:%d", currentTran.TranNo));
        Log.i(TAG, String.format("BatchNoLOnT:%d", currentTran.BatchNoLOnT));
        Log.i(TAG, String.format("TranNoLOnT:%d", currentTran.TranNoLOnT));
        Log.i(TAG, String.format("BatchNoLOffT:%d", currentTran.BatchNoLOffT));
        Log.i(TAG, String.format("TranNoLOffT:%d", currentTran.TranNoLOffT));

        if(currentTran.OrgBankRefNo[0] != 0)
        {
            buff[buffIdx++] = 0x0E;
            memcpy(buff, buffIdx, new byte[] {0x00, 0x10}, 0, 2);
            buffIdx += 2;
            memcpy(buff, buffIdx, currentTran.OrgBankRefNo, 0, 16);
            buffIdx += 16;
        }

//        if(currentTran.Cvv2[0] != 0)
//        {
//            buff[buffIdx++] = 0x10;
//            buff[buffIdx++] = 0x00;
//            buff[buffIdx++] = (byte)c.strlen(currentTran.Cvv2);
//            memcpy(buff, buffIdx, currentTran.Cvv2, 0, c.strlen(currentTran.Cvv2));
//            buffIdx += c.strlen(currentTran.Cvv2);
//        }
        // Tag 0x23: Terminal Yetenekleri (Terminalin Faz 2 Kapsamında Desteklediği Özellikler) Uzunluk (Len): 5 byte
        buff[buffIdx++] = 0x23;
        buff[buffIdx++] = 0x00;
        buff[buffIdx++] = 0x05;
        memcpy(buff, buffIdx, QR.Field23(), 0, 5);
        buffIdx += 5;

        // Kare kod alma istegi
        if (currentTran.EntryMode == EM_QR) {
            QR qr = Tran.currentTranObject.qr;
            byte[] Tag2A = qr.F63_T2A();

            buff[buffIdx++] = 0x2A;
            buff[buffIdx++] = 0x00;
            buff[buffIdx++] = (byte) Tag2A.length;
            memcpy(buff, buffIdx, Tag2A, 0, Tag2A.length); buffIdx += Tag2A.length;

            // Tag 0x2C: QR Reference Data, QR Get Card/Fast Data işleminde gönderilecektir.
            //if (currentTran.ProcessingCode == 400002) {
            if (qr.QRReferansData != null) {
                byte[] Tag2C = qr.F63_T2C();

                buff[buffIdx++] = 0x2C;
                Msgs.Add2ByteLen(buff, buffIdx, (short) Tag2C.length); buffIdx += 2;
                memcpy(buff, buffIdx, Tag2C, 0, Tag2C.length); buffIdx += Tag2C.length;
            }

            // QR Card Authorization Request
            if (currentTran.MsgTypeId == 200 && qr.ECI != null && qr.WalletProgramData != null) {
                buff[buffIdx++] = 0x2D;
                buff[buffIdx++] = 0x00;
                buff[buffIdx++] = (byte) 6;
                memcpy(buff, buffIdx, qr.ECI, 0, 3); buffIdx += 3;
                memcpy(buff, buffIdx, qr.WalletProgramData, 0, 3); buffIdx += 3;
            }
        }

        isoMsg.setFieldBin("63", Arrays.copyOf(buff, buffIdx));


        return rv;
    }
    public static int ParseAuthorizationMsg(HashMap<String, byte[]> isoMsg)
    {
        int rv = 0;
        byte[] tlvData = new byte[4096];

        if(!isoMsg.containsKey("48"))
            return rv;

        int len = isoMsg.get("48").length;
        byte[] tmpBuff = isoMsg.get("48");

        //Auth Amount
        if(techpos.GetTLVData(tmpBuff, len, 0x01, tlvData, (short) c.sizeof(tlvData)) > 0)
        {
            Convert.EP_bcd2str(currentTran.Amount, tlvData, 6);
            Log.i(TAG, String.format("Auth Amount:%s", c.ToString(currentTran.Amount)));
        }

        //Earned Bonus
        if(techpos.GetTLVData(tmpBuff, len, 0x02, tlvData, (short) c.sizeof(tlvData)) > 0)
        {
            Convert.EP_bcd2str(currentTran.EarnedBonusAmount, tlvData, 6);
            Log.i(TAG, String.format("Earned Bonus:%s", c.ToString(currentTran.EarnedBonusAmount)));
        }

        //Installment Count
        if(techpos.GetTLVData(tmpBuff, len, 0x03, tlvData, (short) c.sizeof(tlvData)) > 0)
        {
            currentTran.InsCount = tlvData[0];
            Log.i(TAG, String.format("Installment Count:%d", currentTran.InsCount));
        }

        //Ekstre Postponing Mountly
        if(techpos.GetTLVData(tmpBuff, len, 0x04, tlvData, (short) c.sizeof(tlvData)) > 0)
        {
            currentTran.EkstrePostPonedMountCount = tlvData[0];
            Log.i(TAG, String.format("Ekstre Postponing Mountly:%d", currentTran.EkstrePostPonedMountCount));
        }

        //Ekstre Postponing by date
        if(techpos.GetTLVData(tmpBuff, len, 0x05, tlvData, (short) c.sizeof(tlvData)) > 0)
        {
            Convert.EP_bcd2str(currentTran.EkstrePostPonedDate, tlvData, 4);
            Log.i(TAG, String.format("Ekstre Postponing by date:%s", c.ToString(currentTran.EkstrePostPonedDate)));
        }

        //Instant Discount Amount
        if(techpos.GetTLVData(tmpBuff, len, 0x06, tlvData, (short) c.sizeof(tlvData)) > 0)
        {
            Convert.EP_bcd2str(currentTran.InstantDiscountAmount, tlvData, 6);
            Log.i(TAG, String.format("Instant Discount Amount:%s", c.ToString(currentTran.InstantDiscountAmount)));
        }

        //Surcharge Amount
        if(techpos.GetTLVData(tmpBuff, len, 0x07, tlvData, (short) c.sizeof(tlvData)) > 0)
        {
            Convert.EP_bcd2str(currentTran.SurchargeAmount, tlvData, 6);
            Log.i(TAG, String.format("Surcharge Amount:%s", c.ToString(currentTran.SurchargeAmount)));
        }

        //Card Description
        if(techpos.GetTLVData(tmpBuff, len, 0x0A, tlvData, (short) c.sizeof(tlvData)) > 0)
        {
            memcpy(currentTran.CardDescription, tlvData, 3);
            Log.i(TAG, String.format("Card Description:%s", c.ToString(currentTran.CardDescription)));
        }

        //Bank Ref No
        if(techpos.GetTLVData(tmpBuff, len, 0x0F, tlvData, (short) c.sizeof(tlvData)) > 0)
        {
            memcpy(currentTran.OrgBankRefNo, tlvData, 16);
            Log.i(TAG, String.format("Bank Ref No:%s", c.ToString(currentTran.OrgBankRefNo)));
        }
        //  QR Card Data Sadece “QR Get Card/FAST  Data: 400002” cevabında gönderilecektir.
        if(techpos.GetTLVData(tmpBuff, len, 0x18, tlvData, (short) c.sizeof(tlvData)) > 0)
        {
            Tran.currentTranObject.qr.isFastQr = false;
            Tran.currentTranObject.qr.QrCardData = Arrays.copyOf(tlvData, tlvData.length);
            //memcpy(Tran.currentTranObject.qr.QrCardData, tlvData, Math.min(tlvData.length, currentTran.QrCardData.length));
            Log.i(TAG, String.format("QrCardData :%s", c.ToString(Tran.currentTranObject.qr.QrCardData)));
        }
        int tlvLen;
        //  QR FAST Data Sadece “QR Get Card/FAST  Data: 400002” cevabında gönderilecektir.
        if((tlvLen = techpos.GetTLVData(tmpBuff, len, 0x19, tlvData, (short) c.sizeof(tlvData))) > 0)
        {
            Tran.currentTranObject.qr.isFastQr = true;
            Tran.currentTranObject.qr.QrCardData = Arrays.copyOf(tlvData, tlvData.length);
            //memcpy(currentTran.QrFastData, tlvData, Math.min(tlvLen, currentTran.QrFastData.length));
            Log.i(TAG, String.format("QrFastData :%s", c.ToString(Tran.currentTranObject.qr.QrCardData)));
        }
        //  QR Data Sadece “QR Create: 400001” cevabında gönderilecektir.
        if((tlvLen = techpos.GetTLVData(tmpBuff, len, 0x1A, tlvData, (short) c.sizeof(tlvData))) > 0)
        {
            Tran.currentTranObject.qr.QRReferansData = Arrays.copyOf(tlvData, tlvData.length);
            //memcpy(currentTran.QRReferansData, tlvData, Math.min(tlvLen, currentTran.QRReferansData.length));
                Log.i(TAG, String.format("QRData :%s", c.ToString(Tran.currentTranObject.qr.QRReferansData)));
//            memcpy(QR.ReferansNumarasi, tlvData, 12);
//            QR.qr = new byte[];

        }
        return rv;
    }

}
