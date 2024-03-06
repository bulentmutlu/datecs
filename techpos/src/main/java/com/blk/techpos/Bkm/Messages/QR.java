package com.blk.techpos.Bkm.Messages;

import static com.blk.sdk.c.sprintf;
import static com.blk.techpos.Bkm.TranStruct.T_KAREKOD;
import static com.blk.techpos.Bkm.TranStruct.T_LOYALTYBONUSSPEND;
import static com.blk.techpos.Bkm.TranStruct.T_LOYALTYINSTALLMENT;
import static com.blk.techpos.Bkm.TranStruct.T_REFUND;
import static com.blk.techpos.Bkm.TranStruct.T_REFUNDCONTROLED;
import static com.blk.techpos.Bkm.TranStruct.T_SALE;
import static com.blk.techpos.Bkm.TranStruct.T_VOID;
import static com.blk.techpos.Bkm.TranStruct.currentTran;

import com.blk.sdk.Convert;
import com.blk.sdk.UI;
import com.blk.techpos.Bkm.TranUtils;
import com.blk.techpos.Bkm.VParams.VTerm;

import java.util.Arrays;

public class QR {
    // finansbak İlgili cihaza PS011836 nolu terminal tanımlanmıştır.

    public static final int qrShowTimeout = 300;

    //  QR Card Data Sadece “QR Get Card/FAST  Data: 400002” cevabında gönderilecektir.
    public byte[] QrCardData;
    public boolean isFastQr;
    //    Tag 0x2C: QR Reference Data
//    Uzunluk (Len): n byte
//    QR Referans Numarası	12	ASCII
//    QR Verisi Uzunluğu	2	Binary
//    QR Verisi	n	ASCII	Ekranda gösterilecek QR verisi
    public byte[] QRReferansData;
    public int originalProcessingCode;

    // qr fields
    public String CVM;
    public byte[] ECI;
    public byte[] WalletProgramData;

    // fast qr fields
    public String iban = "";
    public String GonderenAdSoyad = "";
    public  String GonderenKatilimciKodu = "";
    public  String AlanKatilimciKodu = "";
    public  String FASTReferansKodu = "";

    // Tag 0x23: Terminal Yetenekleri (Terminalin Faz 2 Kapsamında Desteklediği Özellikler) Uzunluk (Len): 5 byte
    static byte[] Field23()
    {
        byte[] f23 = new byte[5];

        // Tag 0x23: Terminal Yetenekleri (Terminalin Faz 2 Kapsamında Desteklediği Özellikler) Uzunluk (Len): 5 byte
        byte TermCaps = 0, TermCaps2 = 0;
//        if (VTerm.IsVadeFarkliTaksit(null)) TermCaps |= 0x01;
//        if (VTerm.IsOnlineImprinter(null))  TermCaps |= 0x02;
//        if (VTerm.IsEmvPuanKullanim(null))  TermCaps |= 0x04;
//        if (VTerm.IsPuanSatisTaksit(null))  TermCaps |= 0x08;
//        if (IsBolumluIslemDestegi(NULL)) TermCaps |= 0x10;
//        if (IsAnindaIndirim(NULL))    TermCaps |=  0x20;
//        if (IsMerkezdenTarihSaatGuncelleme(NULL)) TermCaps |= 0x40;
//        if (IkinciFazOnTanimliSlipDestegi(NULL)) TermCaps |= 0x80;

        //if (VTerm.IsKareKodSupport(null))
        TermCaps2 |= 0x01;
        //if (VTerm.KareKodTipi(null))
        TermCaps2 |= 0x02;
        if (VTerm._8haneBINdestegi()) TermCaps2 |= 0x04;

        TermCaps = 1;
        f23[0] = TermCaps;
        f23[1] = TermCaps2;
        return f23;
    }

    public boolean isTranAllowed()
    {
        if (currentTran.ProcessingCode == 400002 && isFastQr && currentTran.TranType != T_SALE && currentTran.TranType != T_REFUNDCONTROLED)
        {
            UI.ShowErrorMessage("İlgili İşlem Tipi ile Karekod FAST İşlem Yapılamaz");
            return false;
        }
        if (currentTran.ProcessingCode == 400002 && !isFastQr && TranUtils.PerformLoyaltyPermissions() != 0) {
            return false;
        }
        return true;
    }

    static int qrTranType(int tranType) throws Exception {
        if (tranType == T_SALE) return 1;
        if (tranType == T_LOYALTYINSTALLMENT) return 2;
        if (tranType == T_VOID) return 3;
        if (tranType == T_REFUND) return 4;

        throw  new Exception("qrTranType unhandled trantype : " + tranType);
    }
    public static boolean isTranSupport(int tranType)
    {
        switch (tranType) {
            case T_SALE:
            case T_LOYALTYINSTALLMENT:
            case T_LOYALTYBONUSSPEND:
            case T_REFUNDCONTROLED:
                return true;
        }
        return false;
    }
    public static boolean allowQR(int tranType)
    {
        return isTranSupport(tranType) && VTerm.IsAnyAcqSupportTran(T_KAREKOD) && VTerm.GetVTermPrms().qrParams.IslemIzni != 0;
    }
    public byte[] F63_T2A() throws Exception {
        byte[] tmpStr = new byte[6];
        sprintf(tmpStr, "%06d", originalProcessingCode);
        return Convert.Ascii2Bcd(tmpStr);
    }
    // QR Get Card/Fast Data işleminde gönderilecektir.
    public byte[] F63_T2C()
    {
        int len = 12 + 2;
        len += Msgs.Get2ByteLen(QRReferansData, 12);
        return Arrays.copyOfRange(QRReferansData, 0, len);
    }

    public byte[] GetQr()
    {
        int len = Msgs.Get2ByteLen(QRReferansData, 12);
        return Arrays.copyOfRange(QRReferansData, 14, 14 + len);
    }
}
