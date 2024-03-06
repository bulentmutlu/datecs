package com.blk.techpos.tran;

import static com.blk.sdk.c.memcmp;
import static com.blk.sdk.c.memcpy;
import static com.blk.sdk.c.memset;
import static com.blk.sdk.c.strcpy;
import static com.blk.sdk.c.strncpy;
import static com.blk.techpos.Bkm.TranStruct.EM_CHIP;
import static com.blk.techpos.Bkm.TranStruct.EM_CONTACTLESS;
import static com.blk.techpos.Bkm.TranStruct.EM_FALLBACK;
import static com.blk.techpos.Bkm.TranStruct.EM_QR;
import static com.blk.techpos.Bkm.TranStruct.EM_SWIPE;
import static com.blk.techpos.Bkm.TranStruct.T_LOYALTYBONUSINQUIRY;
import static com.blk.techpos.Bkm.TranStruct.T_REFUND;
import static com.blk.techpos.Bkm.TranStruct.T_VOID;
import static com.blk.techpos.PrmStruct.params;

import android.util.Log;

import com.blk.platform.ICard;
import com.blk.platform.IPlatform;
import com.blk.sdk.CardReader;
import com.blk.sdk.UI;
import com.blk.sdk.Rtc;
import com.blk.sdk.Utility;
import com.blk.sdk.Emv.RetCode;
import com.blk.sdk.c;
import com.blk.sdk.activity.BaseActivity;
import com.blk.techpos.Bkm.Batch;
import com.blk.techpos.Bkm.Bkm;
import com.blk.techpos.Bkm.Messages.Msgs;
import com.blk.techpos.Bkm.Messages.QR;
import com.blk.techpos.Bkm.Messages.TcUpload;
import com.blk.techpos.Bkm.TranStruct;
import com.blk.techpos.Bkm.TranUtils;
import com.blk.techpos.Bkm.VParams.VBin;
import com.blk.techpos.Bkm.VParams.VTerm;
import com.blk.techpos.Print;
import com.blk.techpos.d9.D9;

import java.util.Arrays;

public class Tran {
    static final String TAG = CardReader.class.getSimpleName();


    public static Tran currentTranObject;

    TranStruct currentTran;
    public QR     qr;

    //        0x01 – Host responds to "APPROVED ".
//        0x02 – Host responds to "DECLINED".
//        0x03 – Unable connect to Host
    public enum OnlineResult {
        NONE((byte) 0),
        APPROVED((byte) 1),
        DECLINED((byte) 2),
        UNABLE((byte) 3);

        private final byte id;

        OnlineResult(byte id) {
            this.id = id;
        }

        public byte getValue() {
            return id;
        }
    }

    public Tran(TranStruct tranStruct, QR qr) {
        this.currentTran = tranStruct;
        this.qr = qr;

        currentTranObject = this;
    }


    public static void StartTran(boolean fClearTranData)
    {
        try {

            if (fClearTranData) TranStruct.ClearTranData();

            if (params.D9(null) && !D9.IsConnected() && !D9.Connect())
                return;

            int entryMode = TranStruct.EM_CONTACTLESS, rv;
            do {
                Tran tran = Tran.NewTran(TranStruct.currentTran, entryMode);
                if (tran == null)
                    break;

                rv = tran.DoTran();
                if (rv != RetCode.CLSS_USE_CONTACT)
                    break;
                TranStruct.currentTran.EntryMode = TranStruct.EM_NULL;
                memset(TranStruct.currentTran.Pan, (byte) 0, TranStruct.currentTran.Pan.length);
                entryMode = TranStruct.EM_CHIP;
            } while (true);

        } catch (Exception ex)
        {
            ex.printStackTrace();
            UI.ShowMessage(15000, ex.getMessage());
        }
    }

    public static Tran NewTran(TranStruct currentTran, int entryMode) throws Exception {
        if (Utility.IsNullorEmpty(currentTran.DateTime))
            memcpy(currentTran.DateTime, Rtc.GetDateTime(), currentTran.DateTime.length);

        if (TranUtils.SelectTran() < 0)
            return null;
        if (Utility.IsNullorEmpty(currentTran.Amount) && TranUtils.PerformAmountEntry() != 0)
            return null;
        if (currentTran.EntryMode == TranStruct.EM_NULL && SelectEntryMode(currentTran, entryMode) != 0)
            return null;
        if (currentTran.EntryMode != EM_CONTACTLESS && TranUtils.SelectAcq() != 0)
                return null;

        QR qr = null;
        if (currentTran.EntryMode == EM_QR) {
            currentTran.MsgTypeId = 800;
            qr = new QR();
            qr.originalProcessingCode = currentTran.ProcessingCode;
            currentTran.ProcessingCode = 400001; // QR Create

        }
        if (currentTran.EntryMode == TranStruct.EM_CHIP && c.strlen(currentTran.EMVAID) > 0) {
            if (!VTerm.IsAcqSupportAID(currentTran.EMVAID, null)) {
                UI.ShowMessage(2000, "KART DESTEKLENMİYOR");
                return null;
            }
        }

        if (TranUtils.PerformEntryModePermissions() != 0) return null;
        if (TranUtils.PerformLoyaltyPermissions() != 0) return null;
        if (TranUtils.PerformPermissions() != 0) {
            UI.ShowMessage(2000, "İŞLEM İZNİ YOK");
            return null;
        }
        if (TranUtils.PerformInputs() != 0)            return null;

        if (currentTran.EntryMode == TranStruct.EM_CHIP)
            return new EmvTran(currentTran);
        if (currentTran.EntryMode == TranStruct.EM_CONTACTLESS)
            return new CtlsTran(currentTran);

        return new Tran(currentTran, qr);
    }
    public int DoTran() throws Exception {
        Msgs.ProcessSignals(3);

        if (params.D9(null) && !D9.info(currentTran)) return -1;

        int rv = doTran();

        if (params.D9(null) && !D9.authorization_response(rv)) return  -1;

        if (rv == 0) {
            Batch.SaveTran();
            Batch.SaveLastTran();

            if (currentTran.EntryMode == TranStruct.EM_CHIP && currentTran.onlineResult == OnlineResult.APPROVED.getValue()) {
                TcUpload.DoTcUpload(EmvTran.APP_EMV_ONLINE);
            }

            IPlatform.get().system.beepOk();
            UI.ShowMessage(0, "İŞLEM ONAYLANDI");

            Print.PrintTran(0);
        }

        Msgs.ProcessSignals(2);

        return rv;
    }
    OnlineResult GoOnline() throws Exception {
        UI.ShowMessage(0, "İŞLEM ONAYLANIYOR");
        int rv =  Bkm.DoTranOnl();
        OnlineResult or;

        if (rv != 0)
            or = OnlineResult.UNABLE;
        else if (currentTran.f39OK())
            or =  OnlineResult.APPROVED;
        else
            or = OnlineResult.DECLINED;

        currentTran.onlineResult = or.getValue();
        return or;
    }
    int doTran() throws Exception
    {
        OnlineResult or = GoOnline();
        if (or== OnlineResult.UNABLE) return -1;
        if (or == OnlineResult.APPROVED) return 0;
        return or.getValue();
    }

    static int SelectEntryMode(TranStruct currentTran, int mode)
    {
        int rv;
        if (params.AcqSelMode == 0) {
            return ReadCard(currentTran, mode);
        }
        return 0;
    }
    // EntryMode, cardInfo[Pan, Track2, EMVAID, ExpDate, CardHolderName, EMVAppPreferredName]
    static int ReadCard(TranStruct currentTran, int mode) {

        int rv = 0, i = 0, ret = -1;
        byte[] srvCode;

        if (currentTran.Pan[0] != 0 || currentTran.EntryMode == EM_QR)
            return 0;

        if (mode == EM_CONTACTLESS) {
            mode = EM_CHIP;
            if ((currentTran.TranType == TranStruct.T_SALE || currentTran.TranType == TranStruct.T_VOID || currentTran.TranType == TranStruct.T_REFUNDCONTROLED)) {

                if (VTerm.IsAnyAcqSupportClssAnyAID()) {
                    if (Utility.IsNullorEmpty(currentTran.AcqId) || VTerm.IsAcqSupportClssAnyAID(null))
                        mode = TranStruct.EM_CONTACTLESS;
                }
            }
        }

        while (true) {
            if (mode == TranStruct.EM_CONTACTLESS)
                CtlsTran.CtlssInitScreen();
            else if (mode == TranStruct.EM_FALLBACK)
                UI.ShowMessage(0, "MANYETİK OKUYUCUYU\nKULLANINIZ");
            else
                UI.ShowMessage(0, "ÇİPLİ VEYA MANYETİK\nKARTINIZI OKUTUNUZ");

            CardReader card = new CardReader();
            String amount = null;
            if(currentTran.TranType!=T_VOID && currentTran.TranType!=T_LOYALTYBONUSINQUIRY &&currentTran.TranType!= T_REFUND){
                amount = c.ToString(currentTran.Amount);
            }

            if (mode == TranStruct.EM_CONTACTLESS)
                card.Read(ICard.MAG_ICC_PICC, amount, QR.allowQR(currentTran.TranType));
            else if (mode == TranStruct.EM_FALLBACK)
                card.Read(ICard.MAGNETIC, amount, QR.allowQR(currentTran.TranType));
            else
                card.Read(ICard.MAG_ICC, amount, QR.allowQR(currentTran.TranType));

            BaseActivity.fForward = (card.result.status == ICard.Status.OK || card.result.status == ICard.Status.MANUEL);

            if (card.result.cardType == ICard.ICC)
            {
                UI.ShowMessage(0, "CHİP KART OKUNUYOR\nLÜTFEN BEKLEYİNİZ");

                rv = IPlatform.get().emv.AppSelect(0);
                if (rv == RetCode.EMV_APP_BLOCK) {
                    ret = EmvTran.APP_EMV_EXIT;
                    break;
                }
                if (rv != 0) {
                    memset(currentTran.Pan, (byte) 0, c.sizeof(currentTran.Pan));
                    return ReadCard(currentTran, EM_FALLBACK);
                    // ret = -2;                    break;
                }

                EmvTran.ReadCardInfo(IPlatform.get().emv, currentTran.Pan, currentTran.Track2,
                        currentTran.ExpDate, currentTran.EMVAID, currentTran.CardHolderName,
                        currentTran.EMVAppPreferredName);

//                if (BuildConfig.DEBUG) {
//                    memcmp(currentTran.Pan, "4355093000658409D24012010000045700000F".getBytes(), 16);
//                    memcpy(currentTran.Track2, "4355093000658409D24012010000045700000F".getBytes(), 38);
//                    strcpy(currentTran.ExpDate, "0000");
//                }

                DumpCardInfo(currentTran);

                currentTran.EntryMode = TranStruct.EM_CHIP;

                VBin.SelVBinBinInfoByBin(currentTran.Pan);
                ret = 0;
                break;
            }
            if (card.result.cardType == ICard.PICC)
            {
                currentTran.EntryMode = TranStruct.EM_CONTACTLESS;
                ret = 0;
                break;
            }
            if (card.result.cardType == ICard.MAGNETIC)
            {
                if (card.result.track2 != null && card.result.track2.length > 0)
                {
                    strcpy(currentTran.Track2, card.result.track2);
                    strcpy(currentTran.Pan, card.pan);
                    if (card.cardHolderName != null)
                    {
                        strncpy(currentTran.CardHolderName, card.cardHolderName.getBytes(), currentTran.CardHolderName.length - 1);
                    }

                    if (mode != TranStruct.EM_FALLBACK) {
                        srvCode = Bkm.GetServiceCode(currentTran.Track2);
                        if (srvCode[0] == '2' || srvCode[0] == '6') {
                            UI.ShowMessage(2000, "ÇİPİ KULLANIN");
                            continue; //goto StartTran;
                        }

                        currentTran.EntryMode = EM_SWIPE;
                    } else {
                        currentTran.EntryMode = EM_FALLBACK;

                        srvCode = Bkm.GetServiceCode(currentTran.Track2);
                        if (!(srvCode[0] == '2' || srvCode[0] == '6'))
                            currentTran.EntryMode = TranStruct.EM_SWIPE;
                    }

                    VBin.SelVBinBinInfoByBin(currentTran.Pan);
                    ret = 0;
                    break;
                } else {
                    UI.ShowMessage(1000, "TEKRAR DENEYİNİZ");
                    continue;
                }
            }

            if (card.result.status == ICard.Status.QR)
            {
                currentTran.EntryMode = EM_QR;
                ret = 0;
                break;
            }
            if (card.result.status == ICard.Status.CANCEL) {
                //UI.ShowMessage("İŞLEMDEN VAZGEÇİLDİ", 2000);
                break;
            }
            if (card.result.status == ICard.Status.MANUEL && (mode != TranStruct.EM_FALLBACK)) {

                String pan= UI.GetPan("KART NO GİRİNİZ", 60);
                //TODO:3 KERE GİRİŞ YAPMASINA İZİN VER SONRASINDA ÇIK
//                if (!Utility.isCreditCardNumberValid(pan)) {
//
//                    UI.ShowMessage("GEÇERSİZ KREDİ KARTI NUMARASI GİRDİNİZ", 2000);
//                    break;
//                }

//                for(int k=0;k<3;k++) {
//                    if (!Utility.isCreditCardNumberValid(pan)) {
//
//                        UI.ShowMessage("GEÇERSİZ KREDİ KARTI NUMARASI GİRDİNİZ", 2000);
//                        break;
//                    }
//                }
                if (pan != null) {
                    memcpy(currentTran.Pan, pan.getBytes(), pan.length());

                    if (VBin.IsDebit(VBin.GetVBinBinInfoByBin(currentTran.Pan))) {
                        UI.ShowMessage(2000, "BANKA KARTLARI\nİÇİN ELLE GİRİŞ\nİZNİ YOK");
                        break;
                    }

                    currentTran.EntryMode = TranStruct.EM_MANUAL;
                    VBin.SelVBinBinInfoByBin(currentTran.Pan);
                    ret = 0;
                    break;
                }
            }

            if (card.result.status == ICard.Status.TIMEOUT) {
                //UI.ShowMessage("TEKRAR DENEYİNİZ", 3000);
                // continue;
                break;
            }

            if (card.result.status != ICard.Status.OK) {
                UI.ShowMessage(3000, "TEKRAR DENEYİNİZ");
                continue;
                //break;
            }
        }

        return ret;
    }
    public static void DumpCardInfo(TranStruct currentTran)
    {
        Utility.log("----- CARD INFO --------");
        Utility.log("pan : " + c.ToString(currentTran.Pan));
        Utility.log("Track2 : " + c.ToString(currentTran.Track2));
        Utility.log("EMVAID : " + c.ToString(currentTran.EMVAID));
        Utility.log("CardHolderName : " + c.ToString(currentTran.CardHolderName));
        Utility.log("EMVAppPreferredName : " + c.ToString(currentTran.EMVAppPreferredName));
        Utility.log("------------------------");
    }
    static int FixTrack2(byte[] track2, int Tk2Len)
    {
        Log.i(TAG, "FixTrack2 : " + new String(track2, 0, Tk2Len));

        int i, track2Start = 0, track2End = Tk2Len;

        if ("0123456789".indexOf(track2[0]) == -1)
            track2Start = 1;
        if (track2[Tk2Len - 2] == '?')
            track2End--;
        if (track2[Tk2Len - 1] != '?')
            track2End++;

        byte[] fixedtrack2 = Arrays.copyOfRange(track2, track2Start, track2End);

        if (fixedtrack2[fixedtrack2.length - 1] != '?')
            fixedtrack2[fixedtrack2.length - 1] = '?';

        memset(track2, (byte) 0, Tk2Len);
        memcpy(track2, fixedtrack2, fixedtrack2.length);
        Log.i(TAG, "Fixed track2 : " + new String(fixedtrack2));

        return fixedtrack2.length;
    }
}
