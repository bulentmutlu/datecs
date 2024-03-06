package com.blk.techpos.Bkm;

import static com.blk.sdk.c.memcmp;
import static com.blk.sdk.c.strcmp;

import com.blk.sdk.c;

/**
 * Created by id on 23.02.2018.
 */

public class TranStruct {

    // Transaction Types
    public static final int T_NULL = 0;
    public static final int T_SALE = 1;
    public static final int T_REFUND = 2; // İADE
    public static final int T_REFUNDCONTROLED = 3; // EŞLENİKLİ İADE
    public static final int T_PREAUTHOPEN = 4; // ÖN PROVİZYON AÇMA
    public static final int T_PREAUTHCLOSE = 5; // ÖN PROVİZYON KAPAMA
    public static final int T_LOYALTYINSTALLMENT = 6; // TAKSİTLİ SATIŞ
    public static final int T_LOYALTYBONUSINQUIRY = 7; // PUAN SORGU
    public static final int T_LOYALTYBONUSSPEND = 8; // PUAN HARCAMA
    public static final int T_ONLINEIMPRINTER = 9;
    public static final int T_VOID = 10; // İPTAL
    public static final int T_VADE_FARKLI_TAKSIT = 11;
    public static final int T_VADE_FARKLI_TAKSIT_SORGU = 12;
    public static final int T_KAREKOD = 13;

//    public static final int O_INIT = 11;
//    public static final int O_FINISH = 12;
//    public static final int O_ENDOFDAY = 13;
//    public static final int O_OFFLINE=14;

    // Entry Modes
    public static final byte EM_NULL = 0x00;
    public static final byte EM_MANUAL = 0x01;
    public static final byte EM_QR   = 0x03;
    public static final byte EM_CHIP = 0x05;
    public static final byte EM_CONTACTLESS = 0x07;
    public static final byte EM_FALLBACK = (byte) 0x80;
    public static final byte EM_SWIPE = (byte) 0x90;
    public static final byte EM_CONTACTLESS_SWIPE = (byte) 0x91;

    public static class TranTotals {
        public byte[] CurrencyCode = new byte[4];
        public short POnlTCnt;
        public long POnlTAmt;
        public short NOnlTCnt;
        public long NOnlTAmt;
        public short POffTCnt;
        public long POffTAmt;
        public short NOffTCnt;
        public long NOffTAmt;
    }

    public static TranStruct currentTran = new TranStruct();

    //Auth Tran Data
    public int MsgTypeId;
    public int OrgMsgTypeId;
    public int ProcessingCode;
    public int OrgProcessingCode;
    public byte[] DateTime = new byte[8];
    public byte[] OrgDateTime = new byte[8];
    public byte Offline;
    public byte PinEntered;
    public byte NoPrntSign;
    public byte TranType;
    public byte OrgTranType;
    public byte[] AcqId = new byte[2];
    public byte[] IssId = new byte[2];
    public byte[] TermId = new byte[8];
    public byte[] MercId = new byte[15];
    public byte[] CardHolderName = new byte[64];
    public byte[] Pan = new byte[20];
    public byte[] Track2 = new byte[64];
    public byte[] ExpDate = new byte[4];
    public byte[] Cvv2 = new byte[4];
    public byte[] Amount = new byte[12];
    public byte[] OrgAmount = new byte[12];
    public byte[] AmountCB = new byte[12];
    public byte[] OrgAmountCB = new byte[12];
    public byte[] BonusAmount = new byte[12];
    public byte[] EarnedBonusAmount = new byte[12];
    public byte[] InstantDiscountAmount = new byte[12];
    public byte[] SurchargeAmount = new byte[12];
    public int Stan;
    public int BatchNo;
    public int TranNo;
    public int TranNoLOnT;
    public int BatchNoLOnT;
    public int TranNoLOffT;
    public int BatchNoLOffT;
    public int OrgTranNo;
    public byte EntryMode;
    public byte ConditionCode;
    public byte[] BankRefNo = new byte[16];
    public byte[] OrgBankRefNo = new byte[16];
    public byte[] RRN = new byte[12];
    public byte[] RspCode = new byte[2];
    public byte[] AuthCode = new byte[6];
    public byte InsCount;
    public byte[] CurrencyCode = new byte[2];
    public byte[] CountryCode = new byte[2];
    public byte CtlssKernelType;
    public byte[] EMVAID = new byte[32];
    public byte[] EMVAppPreferredName = new byte[16];
    public byte[] EMVAC = new byte[16];
    public byte[] EMVTC = new byte[24];
    public short DE55Len;
    public byte[] DE55 = new byte[256];
    public byte[] PinBlock = new byte[8];
    public byte[] VoidPan = new byte[20];

    public byte onlineResult;

    // QR
/*    public int  QRPaymentTransactionProcessingCode; // Tag 0x2A
    public byte[] QRReferansData = new byte[1024]; // Create requestin cevabında Tag 0x1A alınır. Tag 0x2C de gönderilir.
    public byte[] QRRelatedData = new byte[16]; // Field 57 an-16 Sadece karekod yöntemi ile alınacak otorizasyon request mesajlarında kullanılacaktır.
    public byte[] QrCardData = new byte[128];  // Autorizasion reply Field 48 Tag 0x18 Sadece “QR Get Card/FAST Data: 400002” cevabında gönderilecektir
    public byte[] QrFastData = new byte[128];  // Autorizasion reply Field 48 Tag 0x19 Sadece “QR Get Card/FAST Data: 400002” cevabında gönderilecektir
*/

    public byte EkstrePostPonedMountCount;
    public byte[] EkstrePostPonedDate = new byte[8];
    public byte[] CardDescription = new byte[3];
    public byte[] ReplyDescription = new byte[80];
    public short SubReplyCode;
    public byte[] SubReplyDescription = new byte[40];
    public byte[] SlipFormat = new byte[64];
    public byte[][] FreeFrmtPrntData = new byte[6][160];

    //Key Exchannge
    public byte[] MSK = new byte[16];            //Message Session Key
    public byte[] TCN = new byte[8];                //Terminal Challenge Number
    public byte[] HCN = new byte[8];                //Host Challenge Number
    public short RSAKeyBlkLen;
    public byte[] RSAKeyBlk = new byte[128];        //MSK(RSA Key Block)
    public byte[] TMK = new byte[16];            //Terminal Master Key
    public byte TMKInd;                //0: Current Terminal Master Key 1: New Terminal Master Key
    public byte[] TPK = new byte[16];            //Terminal Pin Encryption Key
    public byte[] OKK = new byte[16];            //Offline Card No Encryption Key

    //Prm Data
    public byte[] PrmPack = new byte[1024];
    public int PrmPackLen;
    public byte CompressionType;
    public int Offset;
    public int PackSize;
    public byte[] PackCrc = new byte[4];

    // Settle Data
    public byte TotalsLen;
    public TranTotals[] Totals = new TranTotals[10];

    public int emvOnlineFlow;
    public int unableToGoOnline;


    public TranStruct() {
        for (int i = 0; i < Totals.length; ++i)
            Totals[i] = new TranTotals();
        c.strcpy(ExpDate, "0000");
    }

    public boolean f39OK()
    {
        return !strcmp(currentTran.RspCode, "00");
    }

    public static void ClearTranData() {
        currentTran = new TranStruct();
    }

    public static boolean IsReverse(int proCode) {
        String pCodeStr = String.format("%06d", proCode);

        if (pCodeStr.charAt(1) == '2')
            return true;
        return false;
    }

    public static String GetTranNameForReceipt(int proCode) {
        int code = proCode;
        if (proCode < 0)
            code = currentTran.ProcessingCode;

        switch (code) {
            case 0:
                return "SATIŞ";
            case 1:
                return "ONLINE IMPRINTER";
            case 2:
                return "TAKSİTLİ SATIŞ";
            case 3:
                return "PUAN KULLANIM";
            case 4:
                return "PUAN SORGU";
            case 200000:
                return "EŞLENİKLİ İADE";
            case 200001:
                return "EŞLENİKSİZ İADE";
            case 300000:
                return "ÖN PROVİZYON AÇMA";
            case 300001:
                return "ÖN PROVİZYON KAPAMA";
            case 20000:
            case 20001:
            case 20002:
            case 20003:
            case 220000:
            case 220001:
            case 320000:
            case 320001:
                return "İPTAL";
            case 910000:
            case 920000:
                return "GÜNSONU";
            case 810000:
            case 810001:
            case 900000:
            case 900001:
                return "PARAMETRE YÜKLEME";
            default:
                return "İŞLEM";
        }
    }

    public static String GetTranNameForSettleReceipt(int proCode) {
        int code = proCode;
        if (proCode < 0)
            code = currentTran.ProcessingCode;

        switch (code) {
            case 0:
                return "SATIŞ";
            case 1:
                return "ONLINE IMPRINTER";
            case 2:
                return "TAKSİTLİ SATIŞ";
            case 3:
                return "PUAN KULLANIM";
            case 4:
                return "PUAN SORGU";
            case 200000:
                return "EŞLENİKLİ İADE";
            case 200001:
                return "EŞLENİKSİZ İADE";
            case 300000:
                return "ÖN PROV AÇMA";
            case 300001:
                return "ÖN PROV KAPAMA";
            case 20000:
                return "İPTAL";
            case 20001:
                return "O.IMPRNTR İPT.";
            case 20002:
                return "TKST.STS İPT.";
            case 20003:
                return "PUAN KUL. İPT.";
            case 220000:
                return "EŞNKLİ İADE İPT.";
            case 220001:
                return "EŞNKSZ İADE İPT.";
            case 320000:
                return "ÖN PROV.A İPT.";
            case 320001:
                return "ÖN PROV.K. İPT.";
            case 910000:
            case 920000:
                return "GÜNSONU";
            case 810000:
            case 810001:
            case 900000:
            case 900001:
                return "PARAMETRE YÜKLEME";
            default:
                return "İŞLEM";
        }
    }

    public static String GetTranName(int proCode) {
        int code = proCode;
        if (proCode < 0)
            code = currentTran.ProcessingCode;

        switch (code) {
            case 0:
                return "SATIŞ";
            case 1:
                return "ONLINE IMPRINTER";
            case 2:
                return "LYL. TAKSİT";
            case 3:
                return "LYL. P. KULLANIM";
            case 4:
                return "LYL. P. SORGU";
            case 200000:
                return "EŞLENİKLİ İADE";
            case 200001:
                return "EŞLENİKSİZ İADE";
            case 300000:
                return "ÖN PROVİZYON";
            case 300001:
                return "ÖN PROV. KAPAMA";
            case 20000:
                return "İPTAL";
            case 20001:
                return "ONLN IMPRNTR İPT.";
            case 20002:
                return "LYL.TAKSİT İPT.";
            case 20003:
                return "LYL.P.KUL. İPT.";
            case 220000:
                return "EŞLNKLİ İADE İPT.";
            case 220001:
                return "EŞLNKSZ İADE İPT.";
            case 320000:
                return "ÖN PROVİZYON İPT.";
            case 320001:
                return "ÖN PROV.K. İPT.";
            case 910000:
            case 920000:
                return "GÜNSONU";
            case 810000:
            case 810001:
            case 900000:
            case 900001:
                return "PARAMETRE YÜKLEME";
            default:
                return "İŞLEM";
        }
    }

//    public void ToFile(file f) throws IOException {
//        f.Write(MsgTypeId);
//        f.Write(OrgMsgTypeId);
//        f.Write(ProcessingCode);
//        f.Write(OrgProcessingCode);
//        f.Write(DateTime);
//        f.Write(OrgDateTime);
//        f.Write(Offline);
//        f.Write(PinEntered);
//        f.Write(NoPrntSign);
//        f.Write(TranType);
//        f.Write(OrgTranType);
//        f.Write(AcqId);
//        f.Write(IssId);
//        f.Write(TermId);
//        f.Write(MercId);
//        f.Write(CardHolderName);
//        f.Write(Pan);
//        f.Write(Track2);
//        f.Write(ExpDate);
//        f.Write(Cvv2);
//        f.Write(Amount);
//        f.Write(OrgAmount);
//        f.Write(AmountCB);
//        f.Write(OrgAmountCB);
//        f.Write(BonusAmount);
//        f.Write(EarnedBonusAmount);
//        f.Write(InstantDiscountAmount);
//        f.Write(SurchargeAmount);
//        f.Write(Stan);
//        f.Write(BatchNo);
//        f.Write(TranNo);
//        f.Write(TranNoLOnT);
//        f.Write(BatchNoLOnT);
//        f.Write(TranNoLOffT);
//        f.Write(BatchNoLOffT);
//        f.Write(OrgTranNo);
//        f.Write(EntryMode);
//        f.Write(ConditionCode);
//        f.Write(BankRefNo);
//        f.Write(OrgBankRefNo);
//        f.Write(RRN);
//        f.Write(RspCode);
//        f.Write(AuthCode);
//        f.Write(InsCount);
//        f.Write(CurrencyCode);
//        f.Write(CountryCode);
//        f.Write(CtlssKernelType);
//        f.Write(EMVAID);
//        f.Write(EMVAppPreferredName);
//        f.Write(EMVAC);
//        f.Write(EMVTC);
//        f.Write(DE55Len);
//        f.Write(DE55);
//        f.Write(PinBlock);
//        f.Write(EkstrePostPonedMountCount);
//        f.Write(EkstrePostPonedDate);
//        f.Write(CardDescription);
//        f.Write(ReplyDescription);
//        f.Write(SubReplyCode);
//        f.Write(SubReplyDescription);
//        f.Write(SlipFormat);
//        f.Write(FreeFrmtPrntData);
//        f.Write(MSK);
//        f.Write(TCN);
//        f.Write(HCN);
//        f.Write(RSAKeyBlkLen);
//        f.Write(RSAKeyBlk);
//        f.Write(TMK);
//        f.Write(TMKInd);
//        f.Write(TPK);
//        f.Write(OKK);
//        f.Write(PrmPack);
//        f.Write(PrmPackLen);
//        f.Write(CompressionType);
//        f.Write(Offset);
//        f.Write(PackSize);
//        f.Write(PackCrc);
//        f.Write(TotalsLen);
//        for (int i = 0; i < Totals.length; ++i)
//            Totals[i].ToFile(f);
//
//        f.Write(emvOnlineFlow);
//        f.Write(unableToGoOnline);
//    }
//    public static void FromFile(file f, TranStruct ts) throws IOException {
//        ts.MsgTypeId = f.ReadInt();
//        ts.OrgMsgTypeId = f.ReadInt();
//        ts.ProcessingCode = f.ReadInt();
//        ts.OrgProcessingCode = f.ReadInt();
//        f.Read(ts.DateTime);
//        f.Read(ts.OrgDateTime);
//        ts.Offline = f.ReadByte();
//        ts.PinEntered = f.ReadByte();
//        ts.NoPrntSign = f.ReadByte();
//        ts.TranType = f.ReadByte();
//        ts.OrgTranType = f.ReadByte();
//        f.Read(ts.AcqId);
//        f.Read(ts.IssId);
//        f.Read(ts.TermId);
//        f.Read(ts.MercId);
//        f.Read(ts.CardHolderName);
//        f.Read(ts.Pan);
//        f.Read(ts.Track2);
//        f.Read(ts.ExpDate);
//        f.Read(ts.Cvv2);
//        f.Read(ts.Amount);
//        f.Read(ts.OrgAmount);
//        f.Read(ts.AmountCB);
//        f.Read(ts.OrgAmountCB);
//        f.Read(ts.BonusAmount);
//        f.Read(ts.EarnedBonusAmount);
//        f.Read(ts.InstantDiscountAmount);
//        f.Read(ts.SurchargeAmount);
//        ts.Stan = f.ReadInt();
//        ts.BatchNo = f.ReadInt();
//        ts.TranNo = f.ReadInt();
//        ts.TranNoLOnT = f.ReadInt();
//        ts.BatchNoLOnT = f.ReadInt();
//        ts.TranNoLOffT = f.ReadInt();
//        ts.BatchNoLOffT = f.ReadInt();
//        ts.OrgTranNo = f.ReadInt();
//        ts.EntryMode = f.ReadByte();
//        ts.ConditionCode = f.ReadByte();
//        f.Read(ts.BankRefNo);
//        f.Read(ts.OrgBankRefNo);
//        f.Read(ts.RRN);
//        f.Read(ts.RspCode);
//        f.Read(ts.AuthCode);
//        ts.InsCount = f.ReadByte();
//        f.Read(ts.CurrencyCode);
//        f.Read(ts.CountryCode);
//        ts.CtlssKernelType = f.ReadByte();
//        f.Read(ts.EMVAID);
//        f.Read(ts.EMVAppPreferredName);
//        f.Read(ts.EMVAC);
//        f.Read(ts.EMVTC);
//        ts.DE55Len = f.ReadShort();
//        f.Read(ts.DE55);
//        f.Read(ts.PinBlock);
//        ts.EkstrePostPonedMountCount = f.ReadByte();
//        f.Read(ts.EkstrePostPonedDate);
//        f.Read(ts.CardDescription);
//        f.Read(ts.ReplyDescription);
//        ts.SubReplyCode = f.ReadShort();
//        f.Read(ts.SubReplyDescription);
//        f.Read(ts.SlipFormat);
//        f.Read(ts.FreeFrmtPrntData);
//
//        //Key Exchannge
//        f.Read(ts.MSK);
//        f.Read(ts.TCN);
//        f.Read(ts.HCN);
//        ts.RSAKeyBlkLen = f.ReadShort();
//        f.Read(ts.RSAKeyBlk);
//        f.Read(ts.TMK);
//        ts.TMKInd = f.ReadByte();
//        f.Read(ts.TPK);
//        f.Read(ts.OKK);
//
//        //Prm Data
//        f.Read(ts.PrmPack);
//        ts.PrmPackLen = f.ReadInt();
//        ts.CompressionType = f.ReadByte();
//        ts.Offset = f.ReadInt();
//        ts.PackSize = f.ReadInt();
//        f.Read(ts.PackCrc);
//
//        // Settle Data
//        ts.TotalsLen = f.ReadByte();
//        for (int i = 0; i < ts.Totals.length; ++i)
//            TranTotals.FromFile(f, ts.Totals[i]);
//
//        ts.emvOnlineFlow = f.ReadInt();
//        ts.unableToGoOnline = f.ReadInt();
//    }

}
