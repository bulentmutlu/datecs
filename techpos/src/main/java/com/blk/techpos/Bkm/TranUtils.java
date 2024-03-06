package com.blk.techpos.Bkm;

import static com.blk.sdk.c.ToString;
import static com.blk.sdk.c.memcmp;
import static com.blk.sdk.c.memcpy;
import static com.blk.sdk.c.memset;
import static com.blk.sdk.c.sprintf;
import static com.blk.sdk.c.strcpy;
import static com.blk.techpos.Bkm.TranStruct.EM_CONTACTLESS;
import static com.blk.techpos.Bkm.TranStruct.EM_QR;
import static com.blk.techpos.Bkm.TranStruct.T_VOID;
import static com.blk.techpos.Bkm.TranStruct.currentTran;
import static com.blk.techpos.PrmStruct.params;

import android.util.Log;

import com.blk.sdk.Convert;
import com.blk.sdk.UI;
import com.blk.sdk.string;
import com.blk.sdk.Utility;
import com.blk.sdk.c;
import com.blk.techpos.Apps;
import com.blk.techpos.Bkm.Messages.OfflineAdvice;
import com.blk.techpos.Bkm.VParams.VBin;
import com.blk.techpos.Bkm.VParams.VSpecialBin;
import com.blk.techpos.Bkm.VParams.VTerm;
import com.blk.techpos.R;
import com.blk.techpos.techpos;
import com.blk.techpos.tran.CtlsTran;

import java.util.Arrays;

/**
 * Created by id on 15.03.2018.
 */

public class TranUtils {

    private static final String TAG = TranUtils.class.getSimpleName();

    public static final int AMOUNT_LENGTH = 12;


    // TranType, MsgTypeId, ProcessingCode, PerformAmountEntry()
    public static int SelectTran() throws Exception {
        int rv = 0, idx = 0;
        String[] items = new String[16];

        int[] idxs = new int[16];

        if (params.BkmParamStatus == 0) {
            UI.ShowMessage(2000, "PARAMETRE YÜKLEYİNİZ");

            //sendData("ERROR_CODE", "errorCode", "9999");
            return -6;
        }

        if (currentTran.TranType == TranStruct.T_NULL) {
            if (!VTerm.IsAnyAcqSupportAnyTran()) {
                UI.ShowMessage(2000, "İŞLEM YETKİSİ YOK");
                return -6;
            }

            idx = 0;

            if (VTerm.IsAnyAcqSupportTran(TranStruct.T_SALE)) {
                items[idx] = "SATIŞ";
                idxs[idx++] = 0;
            }
            if (VTerm.IsAnyAcqSupportTran(TranStruct.T_LOYALTYINSTALLMENT)) {
                items[idx] = "TAKSİTLİ SATIŞ";

                idxs[idx++] = 1;
            }
            if (true) {
                items[idx] = "İPTAL";

                idxs[idx++] = 2;
            }
            if (VTerm.IsAnyAcqSupportTran(TranStruct.T_REFUNDCONTROLED)) {
                items[idx] = "EŞLENİKLİ İADE";

                idxs[idx++] = 3;
            }
            if (VTerm.IsAnyAcqSupportTran(TranStruct.T_REFUND)) {
                items[idx] = "EŞLENİKSİZ İADE";

                idxs[idx++] = 4;
            }
            if (VTerm.IsAnyAcqSupportTran(TranStruct.T_LOYALTYBONUSINQUIRY)) {
                items[idx] = "PUAN SORGU";

                idxs[idx++] = 5;
            }
            if (VTerm.IsAnyAcqSupportTran(TranStruct.T_LOYALTYBONUSSPEND)) {
                items[idx] = "PUAN KULLANIM";

                idxs[idx++] = 6;
            }

            if (VTerm.IsAnyAcqSupportTran(TranStruct.T_PREAUTHOPEN)) {
                items[idx] = "ÖN PROVİZYON AÇMA";

                idxs[idx++] = 7;
            }
            if (VTerm.IsAnyAcqSupportTran(TranStruct.T_PREAUTHCLOSE)) {
                items[idx] = "ÖN PROVİZYON KAPAMA";

                idxs[idx++] = 8;
            }
            if (VTerm.IsAnyAcqSupportTran(TranStruct.T_ONLINEIMPRINTER)) {
                items[idx] = "ONLINE IMPRINTER";
                idxs[idx++] = 9;
            }


            rv = UI.ShowList("İŞLEMLER", items, new int[]{R.drawable.satis,
                    R.drawable.taksitli_satis, R.drawable.puan_kullanim,
                    R.drawable.puan_sorgu, R.drawable.provision_acma, R.drawable.provision_kapama,
                    R.drawable.online_imprinter,
                    R.drawable.esnelikli_iade,
                    R.drawable.esneliksiz_iade,
                    R.drawable.iptal,
            });


            if (rv < 0) return -6;

            if (rv >= 0) {
                switch (idxs[rv]) {
                    case 0:
                        currentTran.TranType = TranStruct.T_SALE;
                        break;
                    case 1:
                        currentTran.TranType = TranStruct.T_LOYALTYINSTALLMENT;
                        break;
                    case 2:
                        currentTran.TranType = TranStruct.T_VOID;
                        break;
                    case 3:
                        currentTran.TranType = TranStruct.T_REFUNDCONTROLED;
                        break;
                    case 4:
                        currentTran.TranType = TranStruct.T_REFUND;
                        break;
                    case 5:
                        currentTran.TranType = TranStruct.T_LOYALTYBONUSINQUIRY;
                        break;
                    case 6:
                        currentTran.TranType = TranStruct.T_LOYALTYBONUSSPEND;
                        break;
                    case 7:
                        currentTran.TranType = TranStruct.T_PREAUTHOPEN;
                        break;
                    case 8:
                        currentTran.TranType = TranStruct.T_PREAUTHCLOSE;
                        break;
                    case 9:
                        currentTran.TranType = TranStruct.T_ONLINEIMPRINTER;
                        break;

                }
            }

            if (currentTran.TranType != TranStruct.T_NULL && Apps.MerchantPwd(0) != 0)
                return -1;
        }

        switch (currentTran.TranType) {
            case TranStruct.T_SALE:
                currentTran.MsgTypeId = 200;
                currentTran.ProcessingCode = 0;
                break;
            case TranStruct.T_REFUNDCONTROLED:
                currentTran.MsgTypeId = 200;
                currentTran.ProcessingCode = 200000;
                break;
            case TranStruct.T_REFUND:
                currentTran.MsgTypeId = 200;
                currentTran.ProcessingCode = 200001;
                break;
            case TranStruct.T_PREAUTHOPEN:
                currentTran.MsgTypeId = 100;
                currentTran.ProcessingCode = 300000;
                currentTran.ConditionCode = 0x06;
                break;
            case TranStruct.T_PREAUTHCLOSE:
                currentTran.MsgTypeId = 200;
                currentTran.ProcessingCode = 300001;
                break;
            case TranStruct.T_LOYALTYINSTALLMENT:
                currentTran.MsgTypeId = 200;
                currentTran.ProcessingCode = 000002;
                break;
            case TranStruct.T_LOYALTYBONUSINQUIRY:
                currentTran.MsgTypeId = 800;
                currentTran.ProcessingCode = 000004;
                break;
            case TranStruct.T_LOYALTYBONUSSPEND:
                currentTran.MsgTypeId = 200;
                currentTran.ProcessingCode = 000003;
                break;
            case TranStruct.T_ONLINEIMPRINTER:
                currentTran.MsgTypeId = 200;
                currentTran.ProcessingCode = 000001;
                break;
            case TranStruct.T_VOID:
                currentTran.MsgTypeId = 200;
                currentTran.ProcessingCode = 0;
                break;
            default:
                currentTran.TranType = TranStruct.T_NULL;
                break;
        }

        switch (currentTran.TranType) {
            case TranStruct.T_SALE:
            case TranStruct.T_PREAUTHOPEN:
            case TranStruct.T_ONLINEIMPRINTER:
            case TranStruct.T_PREAUTHCLOSE:
            case TranStruct.T_LOYALTYINSTALLMENT:
            case TranStruct.T_LOYALTYBONUSSPEND:
            case TranStruct.T_REFUNDCONTROLED:
                if (PerformAmountEntry() != 0)
                    return -1;
                break;
            case T_VOID:
                if (PerformTranNoEntry() != 0)
                    return -1;
                break;
        }

        return currentTran.TranType;
    }

    public static int SelectAcq() {
        int AcqCnt = 0;
        VTerm.AcqInfo[] AcqList = new VTerm.AcqInfo[32];
        VBin.BinInfo pBinInfo;
        VSpecialBin.SpecialBinInfo pSpecialBinInfo;
        int rv = 0, i = 0, tmpFlag = 0, retVal = -1, j = 0, forceManuel = 1, k;
        String[] items = new String[16];
        int[] images = new int[16];
        String errMsg = "KARTI DESTEKLEYEN\nBANKA BULUNAMADI";

        if (!Utility.IsNullorEmpty(TranStruct.currentTran.AcqId) ||
            (TranStruct.currentTran.TranType == TranStruct.T_VOID)) {
            retVal = 0;
        } else {
            if (VBin.SelVBinBinInfoByBin(currentTran.Pan) < 0)
                Log.i(TAG, "UNSUPPORTED BIN:" + c.ToString(currentTran.Pan));

            AcqCnt = VTerm.GetVTermPrms().AcqInfoLen;
            for (i = 0; i < AcqCnt; i++) {
                AcqList[i] = VTerm.GetVTermPrms().AcqInfos[i];
            }

            if (currentTran.TranType > 0) {
                for (i = 0; i < AcqCnt; i++) {
                    if (!VTerm.IsAcqSupportTran(currentTran.TranType, AcqList[i])) {
                        AcqCnt--;
                        //memmove(&AcqList[i], &AcqList[i + 1], (AcqCnt - i)*sizeof(AcqInfo *));
                        for (k = 0; k < (AcqCnt - i); ++k) {
                            AcqList[k + i] = AcqList[k + i + 1];
                        }
                        i--;
                    }
                }
            }

            if ((currentTran.TranType == TranStruct.T_SALE) || (currentTran.TranType == TranStruct.T_PREAUTHOPEN)
                    || (currentTran.TranType == TranStruct.T_LOYALTYINSTALLMENT) || (currentTran.TranType == TranStruct.T_LOYALTYBONUSINQUIRY)
                    || (currentTran.TranType == TranStruct.T_LOYALTYBONUSSPEND)) {
                forceManuel = 0;

                if (c.strlen(currentTran.EMVAID) > 0) {
                    for (i = 0; i < AcqCnt; i++) {
                        if (currentTran.EntryMode == TranStruct.EM_CHIP) {
                            if (!VTerm.IsAcqSupportAID(currentTran.EMVAID, AcqList[i])) {
                                AcqCnt--;
                                //memmove(&AcqList[i], &AcqList[i + 1], (AcqCnt - i)*sizeof(AcqInfo *));
                                for (k = 0; k < (AcqCnt - i); ++k) {
                                    AcqList[k + i] = AcqList[k + i + 1];
                                }
                                i--;
                            }
                        } else {
                            if (!VTerm.IsAcqSupportClssAID(currentTran.EMVAID, AcqList[i])) {
                                AcqCnt--;
                                //memmove(&AcqList[i], &AcqList[i + 1], (AcqCnt - i)*sizeof(AcqInfo *));
                                for (k = 0; k < (AcqCnt - i); ++k) {
                                    AcqList[k + i] = AcqList[k + i + 1];
                                }
                                i--;
                            }
                        }
                    }
                }

                if ((AcqCnt > 0) && (currentTran.EntryMode != 0)) {
                    if (currentTran.EntryMode == TranStruct.EM_MANUAL)
                    {
                        for (i = 0; i < AcqCnt; i++) {
                            if (!VTerm.IsManuelKeyEntry(AcqList[i])) {
                                AcqCnt--;
                                //memmove(&AcqList[i], &AcqList[i + 1], (AcqCnt - i)*sizeof(AcqInfo *));
                                for (k = 0; k < (AcqCnt - i); ++k) {
                                    AcqList[k + i] = AcqList[k + i + 1];
                                }
                                i--;
                            }
                        }
                        errMsg = "ELLE KART NO\nGİRİŞ İZNİ YOK";
                    }
                    else if (currentTran.EntryMode == TranStruct.EM_FALLBACK)
                    {
                        for (i = 0; i < AcqCnt; i++) {
                            if (!VTerm.IsFallbackTransaction(AcqList[i])) {
                                AcqCnt--;
                                //memmove(&AcqList[i], &AcqList[i + 1], (AcqCnt - i)*sizeof(AcqInfo *));
                                for (k = 0; k < (AcqCnt - i); ++k) {
                                    AcqList[k + i] = AcqList[k + i + 1];
                                }
                                i--;
                            }
                        }

                        errMsg = "FALLBACK İŞLEM\nİZNİ YOK";
                    } else if (currentTran.EntryMode == EM_QR) {
                        for (i = 0; i < AcqCnt; i++) {
                            if (!VTerm.IsKareKodSupport(AcqList[i])) {
                                AcqCnt--;
                                //memmove(&AcqList[i], &AcqList[i + 1], (AcqCnt - i)*sizeof(AcqInfo *));
                                for (k = 0; k < (AcqCnt - i); ++k) {
                                    AcqList[k + i] = AcqList[k + i + 1];
                                }
                                i--;
                            }
                        }

                        errMsg = "QR İŞLEM\nİZNİ YOK";
                    }
                }

                if (params.AcqSelMode == 0 && currentTran.EntryMode != EM_QR) {
                    pBinInfo = VBin.GetVBinBinInfoByBin(currentTran.Pan);
                    if (pBinInfo != null) {
                        Log.i(TAG, String.format("BinIssId:%02X%02X - BinAcqId:%02X%02X", pBinInfo.IssId[0], pBinInfo.IssId[1], pBinInfo.AcqId[0], pBinInfo.AcqId[1]));
                        for (i = 0; i < AcqCnt; i++) {
                            if (!memcmp(pBinInfo.AcqId, AcqList[i].AcqId, 2) || !memcmp(pBinInfo.IssId, AcqList[i].AcqId, 2)) {
                                tmpFlag = 1;
                                break;
                            }
                        }

                        if (tmpFlag != 0) {
                            for (i = 0; i < AcqCnt; i++) {
                                Log.i(TAG, String.format("AcqId:%02X%02X", AcqList[i].AcqId[0], AcqList[i].AcqId[1]));
                                if (memcmp(pBinInfo.AcqId, AcqList[i].AcqId, 2) && memcmp(pBinInfo.IssId, AcqList[i].AcqId, 2)) {
                                    AcqCnt--;
                                    //memmove(&AcqList[i], &AcqList[i + 1], (AcqCnt - i)*sizeof(AcqInfo *));
                                    for (k = 0; k < (AcqCnt - i); ++k) {
                                        AcqList[k + i] = AcqList[k + i + 1];
                                    }
                                    i--;
                                }
                            }
                        }
                    }
                }

                if (params.AcqSelMode == 0  && currentTran.EntryMode != EM_QR ) {
                    tmpFlag = 0;
                    pSpecialBinInfo = VSpecialBin.GetVSpecialBinSpecialBinInfoByBin(currentTran.Pan);
                    if (pSpecialBinInfo != null) {
                        for (i = 0; i < AcqCnt; i++) {
                            for (j = 0; j < pSpecialBinInfo.AcqsLen; j++) {
                                if (!memcmp(pSpecialBinInfo.Acqs[j], AcqList[i].AcqId, 2) || !memcmp(pSpecialBinInfo.Acqs[j], new byte[]{0x00, 0x00}, 2)) {
                                    tmpFlag = 1;
                                    break;
                                }
                            }

                            if (tmpFlag != 0)
                                break;
                        }

                        if (tmpFlag != 0) {
                            for (i = 0; i < AcqCnt; i++) {
                                tmpFlag = 0;
                                for (j = 0; j < pSpecialBinInfo.AcqsLen; j++) {
                                    if (!memcmp(pSpecialBinInfo.Acqs[j], AcqList[i].AcqId, 2) || !memcmp(pSpecialBinInfo.Acqs[j], new byte[]{0x00, 0x00}, 2)) {
                                        tmpFlag = 1;
                                        break;
                                    }
                                }

                                if (tmpFlag == 0) {
                                    AcqCnt--;
                                    //memmove(&AcqList[i], &AcqList[i + 1], (AcqCnt - i)*sizeof(AcqInfo *));
                                    for (k = 0; k < (AcqCnt - i); ++k) {
                                        AcqList[k + i] = AcqList[k + i + 1];
                                    }
                                    i--;
                                }
                            }
                        }
                    }
                }

                if (params.AcqSelMode == 0 && currentTran.EntryMode != EM_QR) {
                    pBinInfo = VBin.GetVBinBinInfoByBin(currentTran.Pan);
                    if (pBinInfo != null) {
                        if (pBinInfo.ShareBrand > 0) {
                            for (i = 0; i < AcqCnt; i++) {
                                if (pBinInfo.ShareBrand == AcqList[i].ShareBrand) {
                                    tmpFlag = 1;
                                    break;
                                }
                            }

                            if (tmpFlag != 0) {
                                for (i = 0; i < AcqCnt; i++) {
                                    if (pBinInfo.ShareBrand != AcqList[i].ShareBrand) {
                                        AcqCnt--;
                                        //memmove(&AcqList[i], &AcqList[i + 1], (AcqCnt - i)*sizeof(AcqInfo *));
                                        for (k = 0; k < (AcqCnt - i); ++k) {
                                            AcqList[k + i] = AcqList[k + i + 1];
                                        }
                                        i--;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (AcqCnt == 1) {
                memcpy(currentTran.AcqId, AcqList[0].AcqId, 2);
                memcpy(currentTran.TermId, AcqList[0].TermId, 8);
                retVal = 0;
            } else if (AcqCnt > 1) {
                for (i = 0; i < AcqCnt; i++) {
                    items[i] = c.ToString(AcqList[i].MenuStr);
                    images[i] = Bkm.returnBmpImage(AcqList[i].AcqId);

                    if ((forceManuel == 0) && (params.AcqSelMode == 0) &&
                            !memcmp(params.DefAcq, AcqList[i].AcqId, 2) &&
                            !memcmp(params.DefTermId, AcqList[i].TermId, 8) &&
                            c.strlen(currentTran.Pan) > 0) {
                        memcpy(currentTran.AcqId, AcqList[i].AcqId, 2);
                        memcpy(currentTran.TermId, AcqList[i].TermId, 8);
                        retVal = 0;
                    }
                }

                if (retVal != 0) {
                    if (currentTran.EntryMode == TranStruct.EM_CONTACTLESS) {
                        if (currentTran.TranType == TranStruct.T_REFUNDCONTROLED) {
                            rv = UI.ShowList("BANKA SEÇİMİ", items, images);

                            if (rv >= 0) {
                                memcpy(currentTran.AcqId, AcqList[rv].AcqId, 2);
                                memcpy(currentTran.TermId, AcqList[rv].TermId, 8);
                                retVal = 0;
                            } else
                                retVal = -99;
                        } else {
                            memcpy(currentTran.AcqId, AcqList[0].AcqId, 2);
                            memcpy(currentTran.TermId, AcqList[0].TermId, 8);
                            retVal = 0;
                        }
                    } else {
                        rv = UI.ShowList("BANKA SEÇİNİZ", items, images);
                        if (rv >= 0) {
                            memcpy(currentTran.AcqId, AcqList[rv].AcqId, 2);
                            memcpy(currentTran.TermId, AcqList[rv].TermId, 8);
                            retVal = 0;
                        } else
                            retVal = -99;
                    }
                }
            }
        }

        if (retVal == 0) {
            if (currentTran.TranType != TranStruct.T_VOID) {
                VTerm.SelVTermAcqInfoByAcqId(currentTran.AcqId, currentTran.TermId);

                if (c.strlen(currentTran.EMVAID) > 0) {
                    if (currentTran.EntryMode == TranStruct.EM_CHIP) {
                        if (!VTerm.IsAcqSupportAID(currentTran.EMVAID, null)) {
                            UI.ShowMessage(2000, "KART DESTEKLENMİYOR");
                            return -99;
                        }
                    } else {
                        if (!VTerm.IsAcqSupportClssAID(currentTran.EMVAID, null)) {
                            UI.ShowMessage(2000, "KART DESTEKLENMİYOR");
                            return -99;
                        }
                    }
                }

                memcpy(currentTran.TermId, VTerm.GetVTermAcqInfo().TermId, 8);
                memcpy(currentTran.MercId, VTerm.GetVTermAcqInfo().MercId, 15);

                Log.i(TAG, String.format("%s SELECTED", c.ToString(VTerm.GetVTermAcqInfo().AcqName)));
            }
        } else if (retVal != -99) {
            UI.ShowMessage(2000, errMsg);
        }

        return retVal;
    }

    public static int PerformLoyaltyPermissions() {
        if (currentTran.TranType <= 0)
            return 0;

        if (currentTran.Pan[0] == 0)
            return 0;

        if ((currentTran.TranType == TranStruct.T_LOYALTYINSTALLMENT) && !VBin.IsInstallment(null)) {
            UI.ShowMessage(2000, "KARTIN\nİŞLEM İZNİ YOK");
            return -1;
        } else if (((currentTran.TranType == TranStruct.T_LOYALTYBONUSINQUIRY) || (currentTran.TranType == TranStruct.T_LOYALTYBONUSSPEND)) && !VBin.IsInquiryAndSpentBonus(null)) {
            UI.ShowMessage(2000, "KARTIN\nİŞLEM İZNİ YOK");
            return -1;
        }
        return 0;
    }

    public static int PerformEntryModePermissions() {
        Log.i(TAG, String.format("PerformEntryModePermissions Acq:%02X%02X", currentTran.AcqId[0], currentTran.AcqId[1]));

        if (!Utility.IsNullorEmpty(currentTran.AcqId)) {
            if (currentTran.EntryMode == TranStruct.EM_MANUAL) {
                if (!VTerm.IsManuelKeyEntry(null)) {
                    UI.ShowMessage(2000, "ELLE KART NO\nGİRİŞ İZNİ YOK");
                    return -1;
                }
            }
            else if (currentTran.EntryMode == TranStruct.EM_FALLBACK) {
                if (!VTerm.IsFallbackTransaction(null)) {
                    UI.ShowMessage(2000, "FALLBACK İŞLEM\nİZNİ YOK");
                    return -2;
                }
            }
            else if (currentTran.EntryMode == EM_QR) {
                if (!VTerm.IsKareKodSupport(null)) {
                    UI.ShowMessage(2000, "QR İŞLEM\nİZNİ YOK");
                    return -3;
                }
            }
        }

        return 0;
    }

    public static int PerformPermissions() {
        VTerm.AcqInfo acqInfo;
        VBin.BinInfo binInfo;

        if (currentTran.TranType <= 0) {
            Log.i(TAG, "PerformPermissions TranType Not Selected");
            return -1;
        }
        Log.i(TAG, "PerformPermissions TranType " + currentTran.TranType);

        if (currentTran.TranType == TranStruct.T_VOID || currentTran.EntryMode == EM_QR
                || currentTran.EntryMode == EM_CONTACTLESS)
            return 0;

        if (currentTran.Pan[0] == 0) {
            Log.i(TAG, "PerformPermissions Card Info Empty");
            return -2;
        }
        //EP_printf("PerformPermissions Pan %s", c.ToString(currentTran.Pan));

        if (!memcmp(currentTran.AcqId, new byte[]{0x00, 0x00}, 2)) {
            Log.i(TAG, "PerformPermissions Acq Not Selected");
            return -3;
        }

        acqInfo = VTerm.GetVTermAcqInfoByAcqId(currentTran.AcqId, currentTran.TermId);
        if (acqInfo == null) {
            Log.i(TAG, "PerformPermissions Acq Info Not Found");
            return -4;
        }
        Log.i(TAG, "PerformPermissions Acq " + ToString(acqInfo.AcqName));

        if (currentTran.TranType == TranStruct.T_LOYALTYINSTALLMENT || currentTran.TranType == TranStruct.T_LOYALTYBONUSINQUIRY || currentTran.TranType == TranStruct.T_LOYALTYBONUSSPEND) {
            binInfo = VBin.GetVBinBinInfoByBin(currentTran.Pan);
            if (binInfo == null) {
                Log.i(TAG, "PerformPermissions Bin Info Not Found");
                return -5;
            }
            Log.i(TAG, String.format("PerformPermissions IssId %02X%02X AcqId %02X%02X AcqSBrand %d BinSBrand %d", binInfo.IssId[0], binInfo.IssId[1], binInfo.AcqId[0], binInfo.AcqId[1], acqInfo.ShareBrand, binInfo.ShareBrand));

            if (!VTerm.IsAcqSupportTran(currentTran.TranType, acqInfo)) {
                Log.i(TAG, "PerformPermissions Acq Not Supprot Tran");
                return -6;
            }

            if (!memcmp(binInfo.IssId, currentTran.AcqId, 2))
                return 0;

            if (acqInfo.ShareBrand <= 0) {
                Log.i(TAG, "PerformPermissions Acq Not Supprot Share Brand");
                return -7;
            }

            if (acqInfo.ShareBrand != binInfo.ShareBrand) {
                Log.i(TAG, "PerformPermissions ShareBrand mismatch");
                return -8;
            }
        } else {
            if (!VTerm.IsAcqSupportTran(currentTran.TranType, acqInfo)) {
                Log.i(TAG, "PerformPermissions Acq Not Supprot Tran");
                return -9;
            }
        }

        return 0;
    }

    public static int PerformInputs() throws Exception {
        int rv = -1;

        if (PerformManuelEntryInputs() != 0)
            return rv;

//        if (PerformTranNoEntry() != 0)
//            return rv;

        if (PerformAmountEntry() != 0)
            return rv;

        if (PerformInsCountEntry() != 0)
            return rv;

        if (PerformOrgRRNEntry() != 0)
            return rv;
        if (currentTran.EntryMode != TranStruct.EM_MANUAL && currentTran.EntryMode != EM_QR ) {
            if (PerformPinEntry(0) != 0)
                return rv;
        }

        return 0;
    }

    static int PerformManuelEntryInputs() {
        int m = 0;
        if (currentTran.EntryMode == TranStruct.EM_MANUAL) {
            while (true) {
                memset(currentTran.ExpDate, (byte) 0, c.sizeof(currentTran.ExpDate));
                String expDate = UI.GetExpDate("SON KULLANMA TARİHİ?", 30);
                if (expDate == null)
                    return -1;
                memcpy(currentTran.ExpDate, 0, expDate.getBytes(), 2, 2);
                memcpy(currentTran.ExpDate, 2, expDate.getBytes(), 0, 2);
                Log.i(TAG, String.format("ExpDate:%s", c.ToString(currentTran.ExpDate)));

                m = c.atoi(Arrays.copyOfRange(currentTran.ExpDate, 2, 4));
                if (m > 12 || m <= 0) {
                    UI.ShowMessage(2000, "GEÇERSİZ TARİH\nTEKRAR DENEYİNİZ");
                    continue;
                }
                break;
            }

            if (currentTran.TranType != TranStruct.T_VOID) {
                String cvv2 = UI.GetNumber("CVV2/4CSC", "", 3, 4, false, 30, false);
                if (cvv2 == null) return -1;

                memcpy(currentTran.Cvv2, cvv2.getBytes(), cvv2.length());
                Log.i(TAG, String.format("Cvv2:%s", c.ToString(currentTran.Cvv2)));
            }
        }

        return 0;
    }

    public static int PerformTranNoEntry() throws Exception {
        int no = 0;
        byte[] tmpStr = new byte[32];
        BatchRec rec = new BatchRec();

        if (currentTran.TranType == TranStruct.T_VOID) {
            Log.i(TAG, String.format("Tran No:%d", currentTran.TranNo));
            no = currentTran.TranNo;
            if (no <= 0) {
                String strTranNo = UI.GetNumber("İŞLEM NO?", null, 1, 6, false, 30, false);
                if (strTranNo == null)
                    return -1;
                no = c.atoi(strTranNo.getBytes());
            }

            if (Batch.GetTranByNo(rec, no) == 0) {
                if (!memcmp(rec.RspCode, "00".getBytes(), 2) || !memcmp(rec.RspCode, "Y1".getBytes(), 2) || !memcmp(rec.RspCode, "Y3".getBytes(), 2)) {
                    sprintf(tmpStr, "%06d", rec.ProcessingCode);
                    if (tmpStr[1] != '2') {
                        if (!c.strcmp(currentTran.Pan, rec.Pan)
                                || currentTran.EntryMode == TranStruct.EM_CONTACTLESS // @idris
                                || true
                        ) {
                            if (rec.MsgTypeId == 220) {
                                if (OfflineAdvice.ProcessOfflineAdvice(0) != 0) {
                                    UI.ShowMessage(2000, "OFFLİNE İŞLEM\nBİLDİRİLEMEDİ");
                                    return -1;
                                }

                                if (Batch.GetTranByNo(rec, no) != 0) {
                                    UI.ShowMessage(2000, "İŞLEM BULUNAMADI");
                                    return -1;
                                }
                            }

                            if (currentTran.EntryMode == TranStruct.EM_CONTACTLESS && rec.ProcessingCode != 0) {
                                UI.ShowMessage(2000, "BU İŞLEM TEMASSIZ\nARAYÜZDEN İPTAL\nEDİLEMEZ");
                                return -1;
                            } else if (currentTran.EntryMode == TranStruct.EM_CONTACTLESS && (c.atoi(rec.Amount) > CtlsTran.ctlssTranLimit)) {
                                UI.ShowMessage(2000, "TEMASSIZ İŞLEM\nLİMİTİ AŞILMIŞTIR");
                                return -1;
                            } else {
                                currentTran.OrgMsgTypeId = rec.MsgTypeId - 10;

                                if (rec.MsgTypeId == 230)
                                    rec.MsgTypeId = 210;

                                currentTran.MsgTypeId = rec.MsgTypeId - 10;

                                currentTran.OrgProcessingCode = rec.ProcessingCode;
                                currentTran.OrgTranNo = rec.TranNo;
                                currentTran.ProcessingCode = rec.ProcessingCode + 20000;

                                memcpy(currentTran.OrgDateTime, rec.DateTime, c.sizeof(currentTran.OrgDateTime));
                                memcpy(currentTran.Amount, rec.Amount, c.sizeof(currentTran.Amount));
                                memcpy(currentTran.VoidPan, rec.Pan, c.sizeof(currentTran.Pan));
                                memcpy(currentTran.ExpDate, rec.ExpDate, c.sizeof(currentTran.ExpDate));
                                memcpy(currentTran.RRN, rec.RRN, c.sizeof(currentTran.RRN));
                                memcpy(currentTran.AuthCode, rec.AuthCode, c.sizeof(currentTran.AuthCode));
                                memcpy(currentTran.RspCode, rec.RspCode, c.sizeof(currentTran.RspCode));
                                memcpy(currentTran.CurrencyCode, rec.CurrencyCode, c.sizeof(currentTran.CurrencyCode));
                                memcpy(currentTran.AcqId, rec.AcqId, c.sizeof(currentTran.AcqId));
                                memcpy(currentTran.TermId, rec.TermId, rec.TermId.length);
                                memcpy(currentTran.MercId, rec.MercId, rec.MercId.length);
                                VTerm.SelVTermAcqInfoByAcqId(currentTran.AcqId, currentTran.TermId);

                                if (PerformEntryModePermissions() != 0)
                                    return -1;
                            }
                        } else {
                            UI.ShowMessage(2000, "İŞLEM İLGİLİ\nKARTA AİT\nDEĞİL");
                            return -1;
                        }
                    } else {
                        UI.ShowMessage(2000, "İŞLEM İPTAL EDİLMİŞ");
                        return -1;
                    }
                } else {
                    UI.ShowMessage(2000, "İŞLEM BULUNAMADI");
                    return -1;
                }
            } else {
                UI.ShowMessage(2000, "İŞLEM BULUNAMADI");
                return -1;
            }
        }
        return 0;
    }

    public static int PerformAmountEntry() {
        int bonusAmount = 0;
        byte[] amountBcd = new byte[6];

        memcpy(currentTran.CurrencyCode, new byte[]{0x09, 0x49}, 2);

        if (params.D9(null) || currentTran.TranType == TranStruct.T_LOYALTYBONUSINQUIRY || currentTran.TranType == TranStruct.T_VOID)
            return 0;

        if (currentTran.TranType == TranStruct.T_LOYALTYBONUSSPEND) {

            if (Utility.IsNullorEmpty(currentTran.BonusAmount)) {
                String amount = UI.GetAmount("PUAN TUTARI?", AMOUNT_LENGTH);
                if (amount == null) return -1;

                amount = string.PadLeft(amount, AMOUNT_LENGTH, '0');
                memcpy(currentTran.BonusAmount, amount.getBytes(), AMOUNT_LENGTH);
                memcpy(currentTran.Amount, currentTran.BonusAmount, AMOUNT_LENGTH);
            }
        } else {
            if (Utility.IsNullorEmpty(currentTran.Amount)) {

                String amount = UI.GetAmount("TUTAR?", AMOUNT_LENGTH);
                if (amount == null) return -1;

                amount = string.PadLeft(amount, AMOUNT_LENGTH, '0');

                memcpy(currentTran.Amount, amount.getBytes(), AMOUNT_LENGTH);
                Log.i(TAG, "Amount : " + c.ToString(currentTran.Amount));
            }
        }

        return 0;
    }

    static int PerformInsCountEntry() {
        byte[] tmpStr = new byte[8];
        if ((currentTran.TranType == TranStruct.T_LOYALTYINSTALLMENT) && (currentTran.InsCount <= 0)) {
            String insCount = UI.GetNumber("TAKSİT SAYISI?", null, 1, 2, false, 30, false);
            if (insCount == null || c.atoi(insCount.getBytes()) == 0)
                return -1;

            currentTran.InsCount = (byte) c.atoi(insCount.getBytes());
        }
        return 0;
    }

    static int PerformOrgRRNEntry() {
        if (currentTran.TranType == TranStruct.T_ONLINEIMPRINTER || currentTran.TranType == TranStruct.T_PREAUTHCLOSE || currentTran.TranType == TranStruct.T_REFUNDCONTROLED) {
            if (c.strlen(currentTran.OrgBankRefNo) <= 0) {

                String bankRefNo = UI.GetNumber("BANKA REFERANS NO", null, 16, 16, false, 30, false, null, true);
                if (bankRefNo == null)
                    return -1;

                c.memcpy(currentTran.OrgBankRefNo, bankRefNo.getBytes(), bankRefNo.getBytes().length);
            }
        }
        return 0;
    }

    public static int PerformPinEntry(int force) throws Exception {
        byte[] tmpStr = new byte[32];
        byte[] amtStr = new byte[32];
        byte[] pinPartStr = new byte[32];
        byte[] panPartStr = new byte[32];
        byte[] pinPartBcd = new byte[8];
        byte[] panPartBcd = new byte[8];
        byte[] pinBlock = new byte[8];
        int i = 0;
        byte[] srvCode;

        if ((force == 0) && (currentTran.EntryMode == TranStruct.EM_CHIP || currentTran.EntryMode == TranStruct.EM_CONTACTLESS)) {
            return 0;
        }

        srvCode = Bkm.GetServiceCode(currentTran.Track2);
        if (VBin.IsDebit(null) || VBin.IsOnlinePin(null) || (force != 0) || (srvCode[0] != '2' && srvCode[0] != '6' && (srvCode[2] == '0') || (srvCode[2] == '5') || (srvCode[2] == '6') || (srvCode[2] == '7'))) {
            String dspStr;

            if (!Utility.IsNullorEmpty(currentTran.Amount)) {
                memset(amtStr, (byte) 0, c.sizeof(amtStr));
                techpos.FormatAmount(amtStr, currentTran.Amount, new byte[]{'T', 'L', 0});
                string.Trim(amtStr, (byte) ' ');
                dspStr = "ŞİFRE(PİN) GİRİNİZ\nTUTAR " + c.ToString(amtStr);
            } else
                dspStr = "ŞİFRE(PİN) GİRİNİZ";

            int[] endStatuses = new int[1];

            String pin = UI.GetNumber(dspStr, "", 4, 6, false, 30, true, endStatuses, false);
            if (endStatuses[0] == UI.TIMEOUT) {
                Log.i(TAG, "ret timeout");
                return -2;
            } else if (endStatuses[0] != UI.OK && (!c.strcmp(currentTran.RspCode, "55"))) {
                Log.i(TAG, "ret 55");
                return -1;
            } else if (endStatuses[0] != UI.OK) {
                Log.i(TAG, "ret nok");
                return -1;
            }

            memcpy(tmpStr, pin.getBytes(), pin.length());

            sprintf(pinPartStr, "%02d%sFFFFFFFFFFFF", c.strlen(tmpStr), c.ToString(tmpStr));

            //EP_printf("pinPartStr: %s", c.ToString(pinPartStr));
            Convert.EP_ascii2bcd(pinPartBcd, 0, pinPartStr, 16);

            int index = c.strlen(currentTran.Pan) - 13;
            sprintf(panPartStr, "0000%s", new String(currentTran.Pan, index, currentTran.Pan.length - index));

            //EP_printf("panPartStr:%s %d", c.ToString(panPartStr), strlen(currentTran.Pan));
            //EP_HexDump(panPartStr, 32);
            Convert.EP_ascii2bcd(panPartBcd, 0, panPartStr, 16);

            for (i = 0; i < 8; i++)
                pinBlock[i] = (byte) (Convert.unsignedByteToInt(pinPartBcd[i]) ^ Convert.unsignedByteToInt(panPartBcd[i]));

            techpos.OsDES(pinBlock, currentTran.PinBlock, params.TPK, 16, 1);

            Utility.logDump(pinBlock, 8);
            //EP_HexDump(currentTran.PinBlock, 8);

            currentTran.PinEntered = 1;
        }

        return 0;
    }
}
