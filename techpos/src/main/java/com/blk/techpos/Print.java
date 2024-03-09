package com.blk.techpos;

import static com.blk.sdk.MainPrinter.Reset;
import static com.blk.sdk.MainPrinter.boldWriter;
import static com.blk.sdk.MainPrinter.captionWriter;
import static com.blk.sdk.MainPrinter.lineWriter;
import static com.blk.sdk.c.ToString;
import static com.blk.sdk.c.atoi;
import static com.blk.sdk.c.memcmp;
import static com.blk.sdk.c.memcpy;
import static com.blk.sdk.c.memset;
import static com.blk.sdk.c.sizeof;
import static com.blk.sdk.c.sprintf;
import static com.blk.sdk.c.strcat;
import static com.blk.sdk.c.strcmp;
import static com.blk.sdk.c.strcpy;
import static com.blk.sdk.c.strlen;
import static com.blk.techpos.Bkm.TranStruct.EM_CHIP;
import static com.blk.techpos.Bkm.TranStruct.EM_CONTACTLESS;
import static com.blk.techpos.Bkm.TranStruct.EM_CONTACTLESS_SWIPE;
import static com.blk.techpos.Bkm.TranStruct.EM_FALLBACK;
import static com.blk.techpos.Bkm.TranStruct.EM_MANUAL;
import static com.blk.techpos.Bkm.TranStruct.EM_NULL;
import static com.blk.techpos.Bkm.TranStruct.EM_QR;
import static com.blk.techpos.Bkm.TranStruct.EM_SWIPE;
import static com.blk.techpos.Bkm.TranStruct.GetTranNameForReceipt;
import static com.blk.techpos.Bkm.TranStruct.GetTranNameForSettleReceipt;
import static com.blk.techpos.Bkm.TranStruct.IsReverse;
import static com.blk.techpos.Bkm.TranStruct.currentTran;
import static com.blk.techpos.PrmStruct.params;

import android.annotation.SuppressLint;
import android.util.Log;

import com.blk.platform.IPlatform;
import com.blk.platform.IPrinter;
import com.blk.sdk.MainPrinter;
import com.blk.sdk.Rtc;
import com.blk.sdk.UI;
import com.blk.sdk.Utility;
import com.blk.sdk.c;
import com.blk.sdk.file;
import com.blk.sdk.string;
import com.blk.techpos.Bkm.Batch;
import com.blk.techpos.Bkm.BatchRec;
import com.blk.techpos.Bkm.Bkm;
import com.blk.techpos.Bkm.VParams.VBin;
import com.blk.techpos.Bkm.VParams.VSpecialBin;
import com.blk.techpos.Bkm.VParams.VTerm;
import com.blk.techpos.tran.CtlsTran;
import com.blk.techpos.tran.Tran;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
/**
 * Created by id on 29.03.2018.
 */


public class Print {

    static byte[] freeFormatLine = new byte[256];
    static int freeFormatLineIdx = 0;
    static int[] images = new int[16];

    static IPrinter IPrinter = IPlatform.get().printer;

;

    public static void PrintFlush()   {
        MainPrinter.PrintFlush();
        /*
        int rv = 0;

        if (IPrinter.OutofPaper()) {
            do {
                UI.ShowMessage(0, "KAĞIT BİTTİ\nKAĞIT TAKINIZ");
                while (IPrinter.OutofPaper())
                    Utility.sleep(250);
                //EP_WaitAKey(10000);
            } while (IPrinter.OutofPaper());
        }

        IPrinter.Print();
        while (true) {
//            rv = EP_PrntStat();
//            if (rv == EP_PRN_STAT_BUSY)
//                Utility.EP_Wait(1);
//            else
                if (IPrinter.OutofPaper()) {
                do {
                    UI.ShowMessage(0, "KAĞIT BİTTİ\nKAĞIT TAKINIZ");
                    while (IPrinter.OutofPaper())
                        Utility.sleep(250);
                    //EP_WaitAKey(10000);
                } while (IPrinter.OutofPaper());
                IPrinter.Print();
            } else
                break;
        }

        Reset();
         */
    }

    public static void PrintRED() throws Exception {
        if (currentTran.f39OK())
            memset(currentTran.RspCode, (byte) 0, 2);

        Print.PrintTran(0);
        Log.i("", "------sub resp code 1");
        byte[] temp = new byte[170];
        strcpy(temp, currentTran.ReplyDescription);
        if (currentTran.SubReplyCode > 0) {
            byte[] subTemp = new byte[10];
            sprintf(subTemp, "\nRED: %d", currentTran.SubReplyCode);
            strcat(temp, subTemp);
        }

        if (strlen(currentTran.SubReplyDescription) > 0) {
            strcat(temp, "\n");
            strcat(temp, currentTran.SubReplyDescription);
        }
        UI.ShowMessage(2000, c.ToString(temp));
    }

    public static void PrintTran(int report) throws Exception {
        int i = 0, idx = 0, templateLen = 0, sliptype = 0;

        //EP_printf("PrintTran : %d : %s : %s", report, EcrcurrentTran.prntFile, currentTran.RspCode);

        memset(freeFormatLine, (byte) 0, sizeof(freeFormatLine));
        freeFormatLineIdx = 0;

        if (!strcmp(currentTran.RspCode, "00") || !strcmp(currentTran.RspCode, "Y1") || !strcmp(currentTran.RspCode, "Y3")) {
            if (currentTran.Offline != 0) {
                currentTran.SlipFormat[idx++] = 2;
                currentTran.SlipFormat[idx++] = 1;
                currentTran.SlipFormat[idx++] = 9;
                memcpy(currentTran.SlipFormat, idx, "HBKTOEUCS".getBytes(), 0, 9);
                idx += 9;
                currentTran.SlipFormat[idx++] = 0;
                currentTran.SlipFormat[idx++] = 9;
                memcpy(currentTran.SlipFormat, idx, "HBKTOEUCS".getBytes(), 0, 9);
            }

            //EP_printf("Slip Format");
            //EP_HexDump(currentTran.SlipFormat, strlen(currentTran.SlipFormat));

            if (report == 0)
                Batch.SaveLastTran();

            idx = 1;
            for (i = 0; i < currentTran.SlipFormat[0]; i++) {
                sliptype = currentTran.SlipFormat[idx++];
                templateLen = currentTran.SlipFormat[idx++];

                if (i > 0) {
                    if (sliptype == 1)
                        UI.ShowMessage(15000, "İŞYERİ NÜSHASI İÇİN\nEKRANA DOKUNUNUZ");
                    else
                        UI.ShowMessage(15000, "MÜŞTERİ NÜSHASI İÇİN\nEKRANA DOKUNUNUZ");

                }
                PrintStartReceipt((byte) sliptype);

                if (report != 0)
                    PrintFLineD(1, 0, "****FİŞ TEKRARI****");

                PrintTemplate(sliptype, report, currentTran.SlipFormat, idx, templateLen);
                idx += templateLen;

                Utility.log("slip 2");
                lineWriter.Write("\n\n\n\n\n");

                PrintEndReceipt((byte) sliptype);
                PrintFlush();
            }
        } else {
            byte[] tmpStr = new byte[128];

            PrintStartReceipt((byte) 1);

            PrintHeader(1, 0);

            PrintTranTitle(1);

            //visa sertification
            if (currentTran.EntryMode == EM_CONTACTLESS && strlen(CtlsTran.clessLimit) > 0)
                lineWriter.Write_Center("Temassız Bakiye: " + ToString(CtlsTran.clessLimit));

            if (currentTran.RspCode[0] != 0) {
                sprintf(tmpStr, "RED: %s", ToString(currentTran.RspCode));
                PrintFLineD(1, 0, tmpStr);
            }

            if (currentTran.ReplyDescription[0] != 0) {
                for (i = 0; i < 4; i++) {
                    memcpy(tmpStr, 0, currentTran.ReplyDescription, i * 20, 20);
                    tmpStr[20] = 0;
                    if (strlen(tmpStr) > 0)
                        PrintFLineD(1, 0, tmpStr);
                }
            }

            if (currentTran.SubReplyCode != 0) {
                lineWriter.Write_Center(String.format("ALT CEVAP KODU: %d", currentTran.SubReplyCode));
            }

            if (strlen(currentTran.SubReplyDescription) > 0)
                lineWriter.Write_Center(currentTran.SubReplyDescription);

            if (currentTran.CardDescription[1] == 'F')
                PrintFLineD(1, 0, "NOT APPROVED");

            if (strlen(currentTran.RRN) > 0)
                lineWriter.Write_Center("RRN: " + ToString(currentTran.RRN));

            Utility.log("ProcessingCode:%d", currentTran.ProcessingCode);

            lineWriter.Write("\n\n\n\n\n");
            lineWriter.Write("\n\n\n\n\n");
            if (currentTran.ProcessingCode == 910000) {
                lineWriter.Write("\n\n\n\n\n");
                lineWriter.Write("\n\n\n\n\n");
            }

            PrintEndReceipt((byte) 1);
        }

        PrintFlush();
    }

    public static void PrintEndOfDay(int detail, int report, int subreport) throws Exception {
        int i = 0, j = 0, k = 0, tmpCount = 0, rv = 0;
        byte[] tmpStr = new byte[32], tmpStr1 = new byte[32];
        Rtc now = Rtc.RtcGet();
        Batch.BatchTotals bTots;
        String[] items = new String[8];
        int lineCount = 0;

        memset(freeFormatLine, (byte) 0, sizeof(freeFormatLine));
        freeFormatLineIdx = 0;


        if (subreport != 0)
            PrintFLineD(1, 1, "ARA RAPOR");

        if (report != 0) {
            items[0] = "ÖZET RAPOR";
            items[1] = "DETAY RAPOR";

            rv = UI.ShowList("GÜNSONU RAPORU", items, images);//, 5000);
            if (rv == 1)
                detail = 1;
            else
                detail = 0;

            PrintFLineD(1, 1, "****FİŞ TEKRARI****");
            bTots = Batch.GetBatchTotalsForPrint();
        } else
            bTots = Batch.CalcBatchTotalsForPrint();

        PrintMerchantInfo();
        PrintSerial();
        lineWriter.Write_LeftRight("BELGE REF. NO:", String.format("%06d", bTots.BatchNo));
        lineWriter.Write_LeftRight(String.format("TARİH:%02d.%02d.%02d", now.day, now.mon, now.year), String.format("SAAT:%02d:%02d", now.hour, now.min));
        lineWriter.Write( "\n");

        if (detail == 0) {
            for (i = 0; i < bTots.len; i++) {
                lineWriter.Write_Center(VTerm.GetVTermPrms().AcqInfos[i].AcqName);
                MainPrinter.LineSpace();
                lineWriter.Write_Center( "--------------------------------");
                MainPrinter.LineSpace();
                lineWriter.Write("İŞYERİ NO: " + ToString(VTerm.GetVTermPrms().AcqInfos[i].MercId));
                lineWriter.Write("TERMİNAL NO: " + ToString(VTerm.GetVTermPrms().AcqInfos[i].TermId));
                lineWriter.Write("\n");
                lineWriter.Write_Center("TECHPOS GÜNSONU ÖZET");
                lineWriter.Write("\n");

                lineWriter.Write("İŞLEM             ADET             TUTAR");
                for (j = 0; j < bTots.acqTots[i].len; j++) {
                    if (bTots.acqTots[i].tots[j][1] != 0) {
                        Utility.FormatAmount(tmpStr, Long.toString(bTots.acqTots[i].totAmts[j]).getBytes(), "TL".getBytes());
                        lineWriter.Write_LeftRight(String.format("%-15s%4d", GetTranNameForSettleReceipt(bTots.acqTots[i].tots[j][0]), bTots.acqTots[i].tots[j][1]),
                                ToString(tmpStr));

                    }
                }

                Utility.FormatAmount(tmpStr, Long.toString(bTots.acqTots[i].trnTotAmt).getBytes(), "TL".getBytes());
                lineWriter.Write_LeftRight(String.format("%-15s%4d", "Toplam", bTots.acqTots[i].trnCnt), c.ToString(tmpStr));

                lineWriter.Write("\n");

                PrintSettleSlipMsg(i);
                PrintSettlementBankLogo(i);
                lineWriter.Write("\n");
            }
        } else {
            for (i = 0; i < VTerm.GetVTermPrms().AcqInfoLen; i++) {
                lineWriter.Write_Center(VTerm.GetVTermPrms().AcqInfos[i].AcqName);
                MainPrinter.LineSpace();
                lineWriter.Write_Center( "--------------------------------");
                MainPrinter.LineSpace();
                lineWriter.Write("İŞYERİ NO: " + ToString(VTerm.GetVTermPrms().AcqInfos[i].MercId));
                lineWriter.Write("TERMİNAL NO: " + ToString(VTerm.GetVTermPrms().AcqInfos[i].TermId));
                lineWriter.Write("PARA BİRİMİ: TL");

                lineWriter.Write("\n");
                lineWriter.Write_Center("TECHPOS SLİP BİLGİ");
                lineWriter.Write("\n");

                lineWriter.Write("İŞLEM NO          TİP         TARİH/SAAT");
                lineWriter.Write("KART NO                            TUTAR");
                lineWriter.Write("\n");

                j = 0;
                BatchRec rec = new BatchRec();
                do {
                    lineCount++;
                    rv = Batch.GetBatchTotalsTranByAcqForPrint(rec, VTerm.GetVTermPrms().AcqInfos[i].AcqId, VTerm.GetVTermPrms().AcqInfos[i].TermId, j++);
                    if (rv == 0 && (!memcmp(rec.RspCode, "00".getBytes(), 2) || !memcmp(rec.RspCode, "Y1".getBytes(), 2) || !memcmp(rec.RspCode, "Y3".getBytes(), 2)) && (rec.ProcessingCode != 950000)) {
                        if (IsReverse(rec.ProcessingCode)) {
                            lineWriter.Write(String.format("%-6d%15s   %02d/%02d/20%02d %02d:%02d", rec.TranNo, GetTranNameForSettleReceipt(rec.ProcessingCode), rec.DateTime[2], rec.DateTime[1], rec.DateTime[0], rec.DateTime[3], rec.DateTime[4]));
                            sprintf(tmpStr, "**** **** **** %s", new String(rec.Pan, 12, rec.Pan.length - 12));
                            memcpy(tmpStr, rec.Pan, 4);
                            Utility.FormatAmount(tmpStr1, rec.Amount, "TL".getBytes());

                            if (tmpStr[strlen(tmpStr) - 1] == 'F')
                                tmpStr[strlen(tmpStr) - 1] = 0;

                            lineWriter.Write(String.format("%-20s%20s", c.ToString(tmpStr), c.ToString(tmpStr1)));

                            lineWriter.Write("\n");

                            lineWriter.Write(String.format("%-6d%15s   %02d/%02d/20%02d %02d:%02d", rec.OrgTranNo, GetTranNameForSettleReceipt(rec.OrgProcessingCode), rec.OrgDateTime[2], rec.OrgDateTime[1], rec.OrgDateTime[0], rec.OrgDateTime[3], rec.OrgDateTime[4]));
                            sprintf(tmpStr, "**** **** **** %s", new String(rec.Pan, 12, rec.Pan.length - 12));
                            memcpy(tmpStr, rec.Pan, 4);
                            Utility.FormatAmount(tmpStr1, rec.Amount, "TL".getBytes());

                            if (tmpStr[strlen(tmpStr) - 1] == 'F')
                                tmpStr[strlen(tmpStr) - 1] = 0;

                            lineWriter.Write(String.format("%-20s%20s", c.ToString(tmpStr), c.ToString(tmpStr1)));
                        } else {
                            lineWriter.Write(String.format("%-6d%15s   %02d/%02d/20%02d %02d:%02d", rec.TranNo, GetTranNameForSettleReceipt(rec.ProcessingCode), rec.DateTime[2], rec.DateTime[1], rec.DateTime[0], rec.DateTime[3], rec.DateTime[4]));

                            sprintf(tmpStr, "**** **** **** %s", new String(rec.Pan, 12, rec.Pan.length - 12));
                            memcpy(tmpStr, rec.Pan, 4);
                            Utility.FormatAmount(tmpStr1, rec.Amount, "TL".getBytes());

                            if (tmpStr[strlen(tmpStr) - 1] == 'F')
                                tmpStr[strlen(tmpStr) - 1] = 0;

                            lineWriter.Write(String.format("%-20s%20s", c.ToString(tmpStr), c.ToString(tmpStr1)));
                        }
                        lineWriter.Write("\n");
                        if (lineCount > 30) {
                            PrintFlush();
                            lineCount = 0;
                        }

                    }
                } while (rv == 0);


                Utility.FormatAmount(tmpStr, Long.toString(bTots.acqTots[i].trnTotAmt).getBytes(), "TL".getBytes());
                lineWriter.Write_LeftRight(String.format("%-15s%4d", "Toplam", bTots.acqTots[i].trnCnt), ToString(tmpStr));

                lineWriter.Write("\n");

                PrintSettleSlipMsg(i);
                PrintSettlementBankLogo(i);
                lineWriter.Write("\n");
            }
        }


        Utility.FormatAmount(tmpStr, Long.toString(bTots.batchTotAmt).getBytes(), "TL".getBytes());

        lineWriter.Write_LeftRight("Genel Toplam", c.ToString(tmpStr));

//        if (fStartTran) {
//            float x = (int) bTots.batchTotAmt;
//            float y = x / 100;
//            //genel toplam bilgisi ödeal uygulamasına gönderiliyor.
//            sendData("END_OF_DAY_ACTION", "endOfDayTotalAmount", Float.toString(y));
//        }

        if (subreport == 0) {
            lineWriter.Write_Center( "RAPOR SONU");
            lineWriter.Write_Center("BU BELGEYİ SAKLAYINIZ");
            lineWriter.Write_Center("GÜNSONU İŞLEMİ BAŞARILI OLARAK");
            lineWriter.Write_Center("TAMAMLANMIŞTIR.");
        }

        if (subreport != 0) {
            lineWriter.Write("\n\n");
            PrintFLineD(1, 1, "ARA RAPOR SONU");
        }

        lineWriter.Write("\n\n\n\n\n");
        lineWriter.Write("\n\n\n\n\n");

        PrintFlush();

//        if(WebServicePrms.GetWSParams().isOdeal.equals("True")){
//            //Msgs.ProcessMsg(Msgs.MessageType.M_BATCHREPORT);
//
//            CallWebService.OdealSettlement();
//        }

    }

    public static void PrintAutoEndOfDay() throws InterruptedException, Exception {
        int i = 0, j = 0, k = 0;
        byte[] tmpStr = new byte[32], tmpStr1 = new byte[32];
        BatchRec rec;
        Rtc now = Rtc.RtcGet();
        Batch.BatchTotals bTots;
        String[] items = new String[8];


        memset(freeFormatLine, (byte) 0, sizeof(freeFormatLine));
        freeFormatLineIdx = 0;

        bTots = Batch.GetAutoBatchTotalsForPrint();

        PrintMerchantInfo();
        PrintSerial();
        lineWriter.Write_LeftRight("BELGE REF. NO:", String.format("%06d", bTots.BatchNo));
        lineWriter.Write_LeftRight(String.format("TARİH:%02d.%02d.%02d", now.day, now.mon, now.year), String.format("SAAT:%02d:%02d", now.hour, now.min));


        lineWriter.Write("\n");

        for (i = 0; i < bTots.len; i++) {
            lineWriter.Write_Center(VTerm.GetVTermPrms().AcqInfos[i].AcqName);
            MainPrinter.LineSpace();
            lineWriter.Write_Center( "--------------------------------");
            MainPrinter.LineSpace();
            lineWriter.Write("İŞYERİ NO: " + ToString(VTerm.GetVTermPrms().AcqInfos[i].MercId));
            lineWriter.Write("TERMİNAL NO: " + ToString(VTerm.GetVTermPrms().AcqInfos[i].TermId));
            lineWriter.Write("\n");
            lineWriter.Write_Center("TechPOS GÜNSONU ÖZET");
            lineWriter.Write("\n");

            lineWriter.Write("İŞLEM             ADET             TUTAR");
            for (j = 0; j < bTots.acqTots[i].len; j++) {
                if (bTots.acqTots[i].tots[j][1] != 0) {
                    Utility.FormatAmount(tmpStr, Long.toString(bTots.acqTots[i].totAmts[j]).getBytes(), "TL".getBytes());
                    lineWriter.Write_LeftRight(String.format("%-15s%4d", GetTranNameForSettleReceipt(bTots.acqTots[i].tots[j][0]), bTots.acqTots[i].tots[j][1]), c.ToString(tmpStr));
                }
            }

            Utility.FormatAmount(tmpStr, Long.toString(bTots.acqTots[i].trnTotAmt).getBytes(), "TL".getBytes());
            lineWriter.Write_LeftRight(String.format("%-15s%4d", "Toplam", bTots.acqTots[i].trnCnt), c.ToString(tmpStr));

            lineWriter.Write("\n");

            PrintSettleSlipMsg(i);
            PrintSettlementBankLogo(i);
            lineWriter.Write("\n");
        }

        //EP_PrntLgo(Logo, 60);

        Utility.FormatAmount(tmpStr, Long.toString(bTots.batchTotAmt).getBytes(), "TL".getBytes());
        lineWriter.Write_LeftRight("Genel Toplam", c.ToString(tmpStr));

        lineWriter.Write_Center("RAPOR SONU");
        lineWriter.Write_Center("BU BELGEYİ SAKLAYINIZ");
        lineWriter.Write_Center("GÜNSONU İŞLEMİ BAŞARILI OLARAK");
        lineWriter.Write_Center("TAMAMLANMIŞTIR.");

        lineWriter.Write("\n\n\n\n\n");
        lineWriter.Write("\n\n\n\n\n");

        SetPrinterMode(2);

        PrintFLineD(1, 1, "PARAMETRE YÜKLEME");
        lineWriter.Write("\n");
        PrintFLineD(1, 0, "BAŞARILI");

        lineWriter.Write_Center(String.format("%02d.%02d.%02d %02d:%02d", now.day, now.mon, now.year, now.hour, now.min));

        lineWriter.Write("\n\n\n\n\n");
        lineWriter.Write("\n\n\n\n\n");

        PrintFlush();

        file.Remove("AUTOBTCH");
    }

    public static void PrintAppPrms(int status) throws InterruptedException, Exception {
        //int i = 0, j = 0;

        Rtc now = Rtc.RtcGet();

        //PrintHeader(1);
        SetPrinterMode(1);

        PrintFLineD(1, 1, "PARAMETRE YÜKLEME");
        lineWriter.Write("\n");

        if (status == 1) {
            PrintFLineD(1, 0, "BAŞARILI");
        } else {
            byte[] tmpStr = new byte[128];

            PrintFLineD(1, 0, "BAŞARISIZ ");
            lineWriter.Write("\n");

            if (currentTran.RspCode[0] != 0) {
                sprintf(tmpStr, "RED: %s", ToString(currentTran.RspCode));
                PrintFLineD(1, 0, tmpStr);
            }

            if (currentTran.ReplyDescription[0] != 0) {
                for (int i = 0; i < 4; i++) {
                    memcpy(tmpStr, 0, currentTran.ReplyDescription, i * 20, 20);
                    tmpStr[20] = 0;
                    if (strlen(tmpStr) > 0)
                        PrintFLineD(1, 0, tmpStr);
                }
            }
        }

        lineWriter.Write_Center(String.format("%02d.%02d.%02d %02d:%02d", now.day, now.mon, now.year, now.hour, now.min));

        lineWriter.Write("\n\n\n\n\n");
        lineWriter.Write("\n\n\n\n\n");

        PrintFlush();

    }

    static void PrintMerchantInfo() throws Exception {
        VTerm.AcqInfo acqInfo = VTerm.GetVTermAcqInfoByIdx(0);
        if (acqInfo != null) {
            lineWriter.Write_Center(acqInfo.MercSlipName);
            PrintAddress(acqInfo.MercSlipAddr);
            lineWriter.Write_Center(acqInfo.MercCity);
            lineWriter.Write_Center(acqInfo.MercPhoneNo);
        }
    }
    static void PrintSerial()
    {
        lineWriter.Write_LeftRight(String.format("SN:%s%s  BLK/1000", Bkm.BkmDeviceModel(), Bkm.GetSerialStr().substring(2)),
                "VER:" + Utility.GetAppVersion(true));
    }

    public static void PrintBankConnInfos() throws InterruptedException, Exception {
        Rtc now = Rtc.RtcGet();
        int i = 0;
        if ((VTerm.GetVTermPrms().AcqInfoLen <= 0) || !VTerm.IsVTermExist()) {
            UI.ShowMessage(3000, "PARAMETRE YÜKLEYİNİZ");
            return;
        }

        UI.ShowMessage(0, "BANKA İRTİBAT BİLGİLERİ BASILIYOR");

        PrintMerchantInfo();
        PrintSerial();
        lineWriter.Write_LeftRight(String.format("TARİH:%02d.%02d.%02d", now.day, now.mon, now.year), String.format("SAAT:%02d:%02d", now.hour, now.min));


        lineWriter.Write("\n");

        for (i = 0; i < VTerm.GetVTermPrms().AcqInfoLen; i++) {
            PrintFLineD(1, 0, VTerm.GetVTermPrms().AcqInfos[i].AcqName);

            lineWriter.Write("İŞYERİ NO: " + ToString(VTerm.GetVTermPrms().AcqInfos[i].MercId));
            lineWriter.Write("TERMİNAL NO: " + ToString(VTerm.GetVTermPrms().AcqInfos[i].TermId));
            lineWriter.Write("POS DESTEK NO: " + ToString(VTerm.GetVTermPrms().AcqInfos[i].PosSupportPhoneNo));
            lineWriter.Write("OTORİZASYON NO: " + ToString(VTerm.GetVTermPrms().AcqInfos[i].PosAuthPhoneNo));
            lineWriter.Write("\n");
        }

        lineWriter.Write_Center("RAPOR SONU");
        lineWriter.Write("\n\n\n\n\n");
        lineWriter.Write("\n\n\n\n\n");

        PrintFlush();

        UI.UiUtil.ShowMessageHide();
    }

    public static void PrintFuncList() throws InterruptedException, Exception {
        Rtc now = Rtc.RtcGet();

        PrintMerchantInfo();
        PrintSerial();
        lineWriter.Write_LeftRight(String.format("TARİH:%02d.%02d.%02d", now.day, now.mon, now.year), String.format("SAAT:%02d:%02d", now.hour, now.min));


        lineWriter.Write("\n");

        lineWriter.Write("YARDIM:                           1 tuşu");
        lineWriter.Write("GÜNSONU:                          2 tuşu");
        lineWriter.Write("ARA RAPOR:                        3 tuşu");
        lineWriter.Write("FİŞ TEKRARI:                      4 tuşu");
        lineWriter.Write("PARAMETRE YÜKLEME:                5 tuşu");
        lineWriter.Write("ŞİFRE DEĞİŞTİRME:                 6 tuşu");
        lineWriter.Write("ŞİFRE SIFIRLAMA:                  7 tuşu");
        lineWriter.Write("BANKA SEÇİMİ:                     8 tuşu");
        lineWriter.Write("DEBUG:                            9 tuşu");

        lineWriter.Write_Center("RAPOR SONU");
        lineWriter.Write("\n\n\n\n\n");
        lineWriter.Write("\n\n\n\n\n");

        PrintFlush();
    }

    static void PrintTemplate(int slipType, int report, byte[] templateBuf, int inOffset, int templateLen) throws InterruptedException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, Exception {
        int i = 0;

        for (i = inOffset; i < inOffset + templateLen; i++) {
            if ((freeFormatLineIdx > 0) && !(templateBuf[i] >= '1' && templateBuf[i] <= '6')) {
                Utility.log(c.ToString(freeFormatLine));
                PrintStr(lineWriter, freeFormatLine);
                memset(freeFormatLine, (byte) 0, sizeof(freeFormatLine));
                freeFormatLineIdx = 0;
            }

            switch (templateBuf[i]) {
                case 'H':
                    PrintHeader(slipType, report);
                    break;
                case 'K':
                    PrintCardInfo(slipType);
                    break;
                case 'B':
                    PrintTranTitle(slipType);
                    break;
                case 'U':
                    PrintWarnings(slipType);
                    break;
                case 'S':
                    PrintOwnerInfo(slipType);
                    break;
                case 'T':
                    PrintAmount(slipType);
                    break;
                case 'O':
                    PrintAuthDetails(slipType);
                    break;
                case 'I':
                    PrintSign(slipType);
                    break;
                case 'E':
                    PrintPinInfo(slipType);
                    break;
                case 'C':
                    PrintBankLogo(slipType);
                    break;
                case 'D':
                    PrintMerchantLogo(slipType);
                    break;
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                    PrintFreeFormat(slipType, templateBuf[i]);
                    break;
            }
        }

        if (freeFormatLineIdx > 0) {
            Utility.log(c.ToString(freeFormatLine));
            PrintStr(lineWriter, freeFormatLine);
            memset(freeFormatLine, (byte) 0, sizeof(freeFormatLine));
            freeFormatLineIdx = 0;
        }

        lineWriter.Write("\n\n\n\n\n");

        //if(!EcrcurrentTran.msgType)
        //lineWriter.Write("\n\n\n\n\n");

        PrintFlush();
    }

    public static void PrintAddress(byte[] address) throws Exception {
        byte[] tmp = new byte[256];
        int i, j;

        j = 0;
        for (i = 0; i < strlen(address); i++) {
            if (address[i] == '\n') {
                lineWriter.Write_Center(tmp);
                memset(tmp, (byte) 0, sizeof(tmp));
                j = 0;
            } else
                tmp[j++] = address[i];

        }
        if (strlen(tmp) > 0)
            lineWriter.Write_Center(tmp);
    }

    public static void PrintCardHolderName(byte[] cardholder) throws Exception {
        int i;
        String[] arr;
        byte[] tmpStr = new byte[256];

        arr = string.Split(c.ToString(cardholder), "/"); // dtmsplit(cardholder, "/", &arr, &count);

        if (arr.length > 1) {
            sprintf(tmpStr, "%s %s", arr[1], arr[0]);
            lineWriter.Write_Center(tmpStr);
        } else if (cardholder[0] == '/') {
            memcpy(tmpStr, 0, cardholder, +1, strlen(cardholder) - 1);
            lineWriter.Write_Center(tmpStr);
        } else
            lineWriter.Write_Center( cardholder);

    }

    @SuppressLint("DefaultLocale")
    static void PrintHeader(int slipType, int report) throws Exception {
        byte[] tranCode = new byte[4];

        memset(tranCode, (byte) 0, sizeof(tranCode));
        switch (currentTran.EntryMode) {
            case EM_NULL:
                tranCode[0] = 'N';
                break;
            case EM_CHIP:
                tranCode[0] = 'C';
                break;
            case EM_CONTACTLESS:
                tranCode[0] = 'L';
                break;
            case EM_FALLBACK:
                tranCode[0] = 'F';
                break;
            case EM_SWIPE:
                tranCode[0] = 'G';
                break;
            case EM_MANUAL:
                tranCode[0] = 'M';
                break;
            case EM_CONTACTLESS_SWIPE:
                tranCode[0] = 'S';
                break;
            case EM_QR:
                tranCode[0] = 'Q';
                break;
        }

        if (currentTran.EntryMode == EM_QR) {
            tranCode[1] = '1';
            tranCode[2] = '1';
        }
        else {
            if (currentTran.Offline != 0)
                tranCode[1] = '2';
            else
                tranCode[1] = '1';
            tranCode[2] = (byte) (currentTran.PinEntered + 0x31);
        }

        PrintMerchantInfo();

        lineWriter.Write_LeftRight(String.format("TARİH:%02d.%02d.%02d", currentTran.DateTime[2], currentTran.DateTime[1], currentTran.DateTime[0]),                String.format("SAAT:%02d:%02d", currentTran.DateTime[3], currentTran.DateTime[4]));
        lineWriter.Write_LeftRight("İŞYERİ:" + ToString(VTerm.GetVTermAcqInfo().MercId), "POS:" +ToString(VTerm.GetVTermAcqInfo().TermId));
        if (VTerm.GetVTermAcqInfo().AmexMemberNo[0] != 0)
            lineWriter.Write("DİĞER ÖDEME SİS. İŞYERİ: " + ToString(VTerm.GetVTermAcqInfo().AmexMemberNo));
        if (report == 0) {
            lineWriter.Write_LeftRight(String.format("BATCH NO:%s-%06d", ToString(VTerm.GetVTermAcqInfo().BkmReceiptCode), params.BatchNo), ToString(tranCode));
            lineWriter.Write_LeftRight(String.format("İŞLEM NO:%06d", currentTran.TranNo), String.format("STAN:%06d", params.Stan));
        } else {
            lineWriter.Write_LeftRight(String.format("BATCH NO:%s-%06d", ToString(VTerm.GetVTermAcqInfo().BkmReceiptCode), currentTran.BatchNo), ToString(tranCode));
            lineWriter.Write_LeftRight(String.format("İŞLEM NO:%06d", currentTran.TranNo), String.format("STAN:%06d", currentTran.Stan));
        }
        //lineWriter.Write("\n");
        PrintSerial();
        lineWriter.Write("\n");
    }

    static void PrintCardInfo(int slipType) throws Exception {
        byte[] tmpBuff = new byte[64];
        VBin.BinInfo binInfo;
        VSpecialBin.SpecialBinInfo specialbinInfo;
        byte[] srvCode;

        if (Tran.currentTranObject.qr != null && Tran.currentTranObject.qr.isFastQr) {
            memset(tmpBuff, (byte) 0, sizeof(tmpBuff));
            strcpy(tmpBuff, Tran.currentTranObject.qr.iban);
            memset(tmpBuff, 4, (byte) '*', Tran.currentTranObject.qr.iban.length() - 8);

            lineWriter.Write_Center(tmpBuff);
        }
        else if (strlen(currentTran.Pan) > 4){
            memset(tmpBuff, (byte) 0, sizeof(tmpBuff));
            sprintf(tmpBuff, "**** **** **** %s", c.ToString(Arrays.copyOfRange(currentTran.Pan, strlen(currentTran.Pan) - 4, strlen(currentTran.Pan))));
            if (slipType != 0)
                memcpy(tmpBuff, currentTran.Pan, 4);

            if (tmpBuff[strlen(tmpBuff) - 1] == 'F')
                tmpBuff[strlen(tmpBuff) - 1] = 0;

            lineWriter.Write_Center(tmpBuff);
        }

        if (strlen(currentTran.CardHolderName) > 0) {
            //lineWriter.Write("\n");
            //lineWriter.Write_Center(currentTran.CardHolderName);
            byte[] cardHolder = new byte[256];
            memcpy(cardHolder, currentTran.CardHolderName, strlen(currentTran.CardHolderName));
            PrintCardHolderName(cardHolder);
        }

        memset(tmpBuff, (byte) 0, sizeof(tmpBuff));

        if (currentTran.CardDescription[0] != 0) {
            switch (currentTran.CardDescription[2]) {
                case 'A':
                    strcat(tmpBuff, "AMEX");
                    break;
                case 'C':
                    strcat(tmpBuff, "CUP");
                    break;
                case 'D':
                    strcat(tmpBuff, "Diners");
                    break;
                case 'J':
                    strcat(tmpBuff, "JCB");
                    break;
                case 'M':
                    strcat(tmpBuff, "Mastercard");
                    break;
                case 'V':
                    strcat(tmpBuff, "VISA");
                    break;
                case 'T':
                    strcat(tmpBuff, "Troy");
                    break;
                default:
                    strcat(tmpBuff, "Diğer");
                    break;
            }

            strcat(tmpBuff, "/");

            switch (currentTran.CardDescription[0]) {
                case 'D':
                    strcat(tmpBuff, "Debit");
                    break;
                case 'C':
                    strcat(tmpBuff, "Kredi");
                    break;
                case 'P':
                    strcat(tmpBuff, "Prepaid");
                    break;
                default:
                    strcat(tmpBuff, "Diğer");
                    break;
            }

            strcat(tmpBuff, "/");

            switch (currentTran.CardDescription[1]) {
                case 'O':
                    strcat(tmpBuff, "OnUs");
                    break;
                case 'D':
                    strcat(tmpBuff, "Yurt İçi");
                    break;
                case 'M':
                    strcat(tmpBuff, "Marka Paylaşım");
                    break;
                case 'F':
                    strcat(tmpBuff, "Yurt Dışı");
                    break;
                default:
                    strcat(tmpBuff, "Diğer");
                    break;
            }
        } else {
            specialbinInfo = VSpecialBin.GetVSpecialBinSpecialBinInfoByBin(currentTran.Pan);
            binInfo = VBin.GetVBinBinInfoByBin(currentTran.Pan);
            if (binInfo != null) {
                switch (binInfo.Brand) {
                    case 'A':
                        strcat(tmpBuff, "AMEX");
                        break;
                    case 'C':
                        strcat(tmpBuff, "CUP");
                        break;
                    case 'D':
                        strcat(tmpBuff, "Diners");
                        break;
                    case 'J':
                        strcat(tmpBuff, "JCB");
                        break;
                    case 'M':
                        strcat(tmpBuff, "Mastercard");
                        break;
                    case 'V':
                        strcat(tmpBuff, "VISA");
                        break;
                    case 'T':
                        strcat(tmpBuff, "Troy");
                        break;
                    case '-':
                        strcat(tmpBuff, "Diğer");
                        break;
                    default:
                        if (currentTran.Pan[0] == '4')
                            strcat(tmpBuff, "VISA");
                        else if (currentTran.Pan[0] == '5' || currentTran.Pan[0] == '6')
                            strcat(tmpBuff, "Mastercard");
                        else if (currentTran.Pan[0] == '3' && (currentTran.Pan[1] == '4' || currentTran.Pan[1] == '7'))
                            strcat(tmpBuff, "AMEX");
                        else
                            strcat(tmpBuff, "Diğer");
                        break;
                }

                strcat(tmpBuff, "/");

                switch (binInfo.CardType) {
                    case 'D':
                        strcat(tmpBuff, "Debit");
                        break;
                    case 'C':
                        strcat(tmpBuff, "Kredi");
                        break;
                    case 'P':
                        strcat(tmpBuff, "Prepaid");
                        break;
                    default:
                        srvCode = Bkm.GetServiceCode(currentTran.Track2);
                        if ((srvCode[2] == '0') || (srvCode[2] == '5') || (srvCode[2] == '6') || (srvCode[2] == '7'))
                            strcat(tmpBuff, "Debit");
                        else
                            strcat(tmpBuff, "Kredi");
                        break;
                }

                if (memcmp(binInfo.IssId, currentTran.AcqId, 2))
                    strcat(tmpBuff, "/YURT İÇİ");
                else
                    strcat(tmpBuff, "/OnUs");
            } else if (specialbinInfo != null) {
                switch (specialbinInfo.Brand) {
                    case 'A':
                        strcat(tmpBuff, "AMEX");
                        break;
                    case 'C':
                        strcat(tmpBuff, "CUP");
                        break;
                    case 'D':
                        strcat(tmpBuff, "Diners");
                        break;
                    case 'J':
                        strcat(tmpBuff, "JCB");
                        break;
                    case 'M':
                        strcat(tmpBuff, "Mastercard");
                        break;
                    case 'V':
                        strcat(tmpBuff, "VISA");
                        break;
                    case 'T':
                        strcat(tmpBuff, "Troy");
                        break;
                    case '-':
                        strcat(tmpBuff, "Diğer");
                        break;
                    default:
                        if (currentTran.Pan[0] == '4')
                            strcat(tmpBuff, "VISA");
                        else if (currentTran.Pan[0] == '5' || currentTran.Pan[0] == '6')
                            strcat(tmpBuff, "Mastercard");
                        else if (currentTran.Pan[0] == '3' && (currentTran.Pan[1] == '4' || currentTran.Pan[1] == '7'))
                            strcat(tmpBuff, "AMEX");
                        else
                            strcat(tmpBuff, "Diğer");
                        break;
                }

                strcat(tmpBuff, "/");

                switch (specialbinInfo.CardType) {
                    case 'D':
                        strcat(tmpBuff, "Debit");
                        break;
                    case 'C':
                        strcat(tmpBuff, "Kredi");
                        break;
                    case 'P':
                        strcat(tmpBuff, "Prepaid");
                        break;
                    default:
                        srvCode = Bkm.GetServiceCode(currentTran.Track2);
                        if ((srvCode[2] == '0') || (srvCode[2] == '5') || (srvCode[2] == '6') || (srvCode[2] == '7'))
                            strcat(tmpBuff, "Debit");
                        else
                            strcat(tmpBuff, "Kredi");
                        break;
                }

                strcat(tmpBuff, "/YURT DIŞI");
            } else {
                if (currentTran.Pan[0] == '4')
                    strcat(tmpBuff, "VISA");
                else if (currentTran.Pan[0] == '5' || currentTran.Pan[0] == '6')
                    strcat(tmpBuff, "Mastercard");
                else if (currentTran.Pan[0] == '3' && (currentTran.Pan[1] == '4' || currentTran.Pan[1] == '7'))
                    strcat(tmpBuff, "AMEX");
                else
                    strcat(tmpBuff, "Diğer");

                strcat(tmpBuff, "/");

                srvCode = Bkm.GetServiceCode(currentTran.Track2);
                if ((srvCode[2] == '0') || (srvCode[2] == '5') || (srvCode[2] == '6') || (srvCode[2] == '7'))
                    strcat(tmpBuff, "Debit");
                else
                    strcat(tmpBuff, "Kredi");

                strcat(tmpBuff, "/YURT DIŞI");
            }
        }

        lineWriter.Write_Center(tmpBuff);
        //lineWriter.Write("\n");
    }

    static void PrintTranTitle(int slipType) throws Exception {
        if (currentTran.InsCount > 1)
            PrintFLineD(1, 0, "TAKSİT SAYISI : %d", currentTran.InsCount);

        PrintFLineD(1, 0, GetTranNameForReceipt(-1));
        //lineWriter.Write("\n");
    }

    static void PrintWarnings(int slipType) throws Exception {
        if (slipType == 0 && currentTran.CardDescription[1] == 'F')
            lineWriter.Write_Center("PLEASE KEEP THIS PAPER");
        else
            lineWriter.Write_Center("BU BELGEYİ SAKLAYINIZ");
    }

    static void PrintOwnerInfo(int slipType) throws Exception {
        if (slipType != 0)
            lineWriter.Write_Center("İŞYERİ NÜSHASI");
        else if (currentTran.CardDescription[1] == 'F')
            lineWriter.Write_Center("CARDHOLDER RECEIPT");
        else
            lineWriter.Write_Center("MÜŞTERİ NÜSHASI");
    }

    static void PrintAmount(int slipType) throws Exception {
        byte[] tmpStr = new byte[32];
        Utility.FormatAmount(tmpStr, currentTran.Amount, "TL".getBytes());
        PrintFLineD(1, 0, "İŞLEM TUTARI");
        PrintFLineD(1, 0, tmpStr);
        //lineWriter.Write("\n");
    }

    static void PrintAuthDetails(int slipType) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, Exception {
        if (currentTran.Offline == 0) {
            lineWriter.Write_LeftRight("ONAY KODU:" + ToString(currentTran.AuthCode),"RRN:" + ToString(currentTran.RRN));
            lineWriter.Write_Center("BANKA REFERANS:" + ToString(currentTran.BankRefNo));
        } else
            lineWriter.Write_Center("ONAY KODU: " + ToString(currentTran.AuthCode));

        if (currentTran.EntryMode == EM_CHIP || currentTran.EntryMode == EM_CONTACTLESS) {
            lineWriter.Write_Center(currentTran.EMVAppPreferredName);
            if (currentTran.Offline != 0)
                lineWriter.Write_LeftRight("AID:"+ToString(currentTran.EMVAID),  "TC:" + ToString(currentTran.EMVAC));
            else
                lineWriter.Write_LeftRight("AID:"  + ToString(currentTran.EMVAID), "REF:" + ToString(currentTran.EMVAC));

            if (currentTran.Offline != 0) {
                lineWriter.Write_Center("OFFLINE REF:");
                lineWriter.Write_Center(techpos.GetOfflineRef());
            }
        }

        if (currentTran.EntryMode == EM_CONTACTLESS) {
            lineWriter.Write_Center("İŞLEM TEMASSIZ YAPILMIŞTIR");
            if (slipType == 0) {
                if (strlen(CtlsTran.clessLimit) > 0)
                    lineWriter.Write_Center("Temassız Bakiye:" + ToString(CtlsTran.clessLimit));
            }
        }
        if (currentTran.EntryMode == EM_QR) {
            lineWriter.Write_Center("QR REFERANS:" + ToString(Tran.currentTranObject.qr.QRReferansData, 0, 12));

            if (Tran.currentTranObject.qr.isFastQr) {
                lineWriter.Write_Center("GÖNDEREN:" + Tran.currentTranObject.qr.GonderenKatilimciKodu + "      "
                        + "ALICI:" + Tran.currentTranObject.qr.AlanKatilimciKodu);
                lineWriter.Write_Center("FAST REF: " + Tran.currentTranObject.qr.FASTReferansKodu);
            }
        }

        if (currentTran.EntryMode != EM_QR || !Tran.currentTranObject.qr.isFastQr)
            lineWriter.Write_Center(String.format("ACQUIRER ID:%02X%02X", currentTran.AcqId[0], currentTran.AcqId[1]));
        //if(currentTran.Offline)
        //	lineWriter.Write_Center("TC:%s", currentTran.EMVTC);

        switch (currentTran.ProcessingCode) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 300001:
                if (slipType == 0 && currentTran.CardDescription[1] == 'F')
                    lineWriter.Write_Center("GOODS OR SERVICES RECEIVED");
                else
                    lineWriter.Write_Center("TUTAR KARŞILIĞI MAL VEYA HİZMET ALDIM");
                break;
            case 300000:
                if (currentTran.CardDescription[1] == 'F')
                    lineWriter.Write_Center("PREAUTHORIZATION RECEIVED");
                else
                    lineWriter.Write_Center("TUTAR KARŞILIĞI ÖN PROVİZYON ALINMIŞTIR");
                break;
            case 20000:
            case 20001:
            case 20002:
            case 20003:
            case 220000:
            case 220001:
            case 320000:
            case 320001:
                if (currentTran.CardDescription[1] == 'F')
                    lineWriter.Write_Center("TRANSACTION CANCELLED");
                else
                    lineWriter.Write_Center("TUTAR KARŞILIĞI İŞLEM İPTAL EDİLMİŞTİR");
                break;
            case 200000:
            case 200001:
                if (currentTran.CardDescription[1] == 'F')
                    lineWriter.Write_Center("TRANSACTION REFUNDED");
                else
                    lineWriter.Write_Center("TUTAR KARŞILIĞI İŞLEM İADE EDİLMİŞTİR");
                break;
        }
        if (currentTran.EntryMode == EM_QR)
            lineWriter.Write_Center("BU İŞLEM TR KAREKOD İLE YAPILMIŞTIR");

        lineWriter.Write("\n");
    }

    static void PrintSign(int slipType) throws Exception {
        if (currentTran.NoPrntSign == 0 && !(currentTran.PinEntered == 1 || currentTran.PinEntered == 2)) {
            lineWriter.Write( "\n");
            PrintFLineD(1, 0, "----------------");
            PrintFLineD(1, 0, "IMZA");
        }
    }

    static void PrintPinInfo(int slipType) throws Exception {
        if (currentTran.PinEntered == 1 || currentTran.PinEntered == 2)
            PrintFLineD(1, 0, "ŞİFRE KULLANILMIŞTIR");
    }

    static void d(byte[] b)
    {
        //Utility.EP_printf("d[" + c.ToString(b).replace(' ', '_').replace('\n', '#').replace('\r', '$') + "]");
    }
    static void PrintFreeFormat(int slipType, byte format) throws Exception {
        int idx = format - 0x30, i = 0, len = 0, mod = 0;
        idx--;

        MainPrinter.LineWriter lw = MainPrinter.lineWriter;

        //Utility.EP_printf(c.ToString(currentTran.FreeFrmtPrntData[idx]));
        d(currentTran.FreeFrmtPrntData[idx]);

        for (i = 0; i < Math.min(160, strlen(currentTran.FreeFrmtPrntData[idx])); ) {
            byte[] b = Arrays.copyOfRange(currentTran.FreeFrmtPrntData[idx], i, currentTran.FreeFrmtPrntData[idx].length);
            if (!memcmp(b, "<S>".getBytes(), 3)) {
                //EP_printf("<S>");
                lw = MainPrinter.lineWriter; // SetPrinterMode(0);
                i += 3;
                mod = 1;
            } else if (!memcmp(b, "<N>".getBytes(), 3)) {
                //EP_printf("<N>");
                lw = MainPrinter.boldWriter; // SetPrinterMode(1);
                i += 3;
                mod = 2;
            } else if (!memcmp(b, "<K>".getBytes(), 3)) {
                //EP_printf("<K>");
                lw = MainPrinter.boldWriter; // SetPrinterMode(1);
                i += 3;
                mod = 3;
            } else if (currentTran.FreeFrmtPrntData[idx][i] == '\r')
                i++;
            else {
                if (!memcmp(b, "</S>".getBytes(), 4) || !memcmp(b, "</N>".getBytes(), 4) || !memcmp(b, "</K>".getBytes(), 4)) {
                    i += 4;
                    d(freeFormatLine);
                    PrintStr(lw, freeFormatLine);
                    memset(freeFormatLine, (byte) 0, sizeof(freeFormatLine));
                    freeFormatLineIdx = 0;
                } else {
                    if (mod > 0) {
                        freeFormatLine[freeFormatLineIdx++] = currentTran.FreeFrmtPrntData[idx][i];
                        if (currentTran.FreeFrmtPrntData[idx][i] == '\n') {
                            //EP_printf(freeFormatLine);
                            d(freeFormatLine);
                            PrintStr(lw, freeFormatLine);
                            memset(freeFormatLine, (byte) 0, sizeof(freeFormatLine));
                            freeFormatLineIdx = 0;
                        } else {
                            if (mod == 1) {
                                if (freeFormatLineIdx >= 40) {

                                    d(freeFormatLine);
                                    PrintStr(lw, freeFormatLine);
                                    memset(freeFormatLine, (byte) 0, sizeof(freeFormatLine));
                                    freeFormatLineIdx = 0;
                                }
                            } else {
                                if (freeFormatLineIdx >= 20) {
                                    //EP_printf(freeFormatLine);
                                    d(freeFormatLine);
                                    PrintStr(lw, freeFormatLine);
                                    memset(freeFormatLine, (byte) 0, sizeof(freeFormatLine));
                                    freeFormatLineIdx = 0;
                                }
                            }
                        }
                    }
                    i++;
                }
            }
        }
    }

    static String MEMCPY(String srcDest, int destOffset, String src, int srcOffset, int len) {
        StringBuilder sb = new StringBuilder(srcDest);
        for (int i = destOffset, j = srcOffset, counter = 0; counter < len; ++i, ++j, ++counter)
            sb.setCharAt(i, src.charAt(j));
        return sb.toString();
    }

    static void PrintSettleSlipMsg(int acqIdx) throws Exception {
        byte[] tmpStr = new byte[320];
        int len = strlen(VTerm.GetVTermPrms().AcqInfos[acqIdx].SettleSlipMsg), j;
        if (len > 0) {
            memcpy(tmpStr, VTerm.GetVTermPrms().AcqInfos[acqIdx].SettleSlipMsg, len);

            for (j = 0; j < (len / 40); j++) {
                memcpy(tmpStr, (j + 1) * 40 + 2 + (j * 2), tmpStr, (j + 1) * 40 + (j * 2), len - ((j + 1) * 40));
                memcpy(tmpStr, (j + 1) * 40 + (j * 2), "  ".getBytes(), 0, 2);
            }

            strcat(tmpStr, "\n");
            if (len % 40 != 0)
                strcat(tmpStr, "\n");

            SetPrinterMode((byte) 0);
            PrintStr(lineWriter, tmpStr);
        }
    }
//
//    static void PrintFLine(int location, byte[] line) throws Exception {
//        //Utility.EP_printf("line : " + c.ToString(line) + " : " + Convert.Buffer2Hex(line));
//        PrintFLine(location, c.ToString(line));
//    }
//
//    static void PrintFLine(int location, String format, Object... args) throws Exception {
//        StringBuilder tmpStr = new StringBuilder(String.format(format, args));
//        Utility.EP_printf(tmpStr.toString());
//        StringBuilder line;
//        int len = 0;
//
//        if (strlen(format.getBytes()) <= 0)
//            return;
//
//        int lineLength = lineWriter.GetLineLen();
//
//        len = tmpStr.length();
//        if (location == 1 && len < lineLength) {
//            String blank = string.PadLeft("", lineLength, ' ');
//            line = new StringBuilder(MEMCPY(blank, (lineLength - len) / 2, tmpStr.toString(), 0, len));
//
//            //line.append("\n");
//            PrintStr(lineWriter, line.toString());
//        } else {
////            if(tmpStr.length() > 1 && tmpStr.charAt(0) != '\n')
////                PrintStr(lineWriter, "\n");
//            PrintStr(lineWriter, tmpStr.toString());
//        }
//    }

    static void PrintFLineD(int location, int dH, byte[] line) throws Exception {
        PrintFLineD(location, dH, c.ToString(line));
    }

    static void PrintFLineD(int location, int dH, String format, Object... args) throws Exception {
        StringBuilder tmpStr = new StringBuilder(String.format(format, args));
        //Utility.EP_printf("PrintFLineD : " + tmpStr.toString());
        StringBuilder line;
        int len = 0;

        if (strlen(format.getBytes()) <= 0)
            return;

        MainPrinter.LineWriter lw = dH != 0 ? captionWriter : boldWriter;

        int lineLength = lw.GetLineLen();
        //IPrinter.doubleHeight(dH != 0, dH != 0);
        String blank = string.PadLeft("", lineLength, ' ');

        len = tmpStr.length();
        if (location == 1 && len < lineLength - 1) {
            line = new StringBuilder(MEMCPY(blank, (lineLength - len) / 2, tmpStr.toString(), 0, len));

            PrintStr(lw, line.toString());
            //PrintStr(lw, "\n");
        } else {
//            line = new StringBuilder(MEMCPY(blank, 1, tmpStr.toString(), 0, tmpStr.length()));
//            if(line.length() > 1 && line.charAt(0) != '\n')
//                PrintStr(lw, "\n");
            line = tmpStr;
            PrintStr(lw, line.toString());
        }
    }

    static void SetPrinterMode(int mode) throws Exception {
//        if(mode == 1)
//            EP_PrntSetDW();
//        else if(mode == 2)
//            EP_PrntSetDH();
//        else
//            EP_PrntSetN();
    }


    static String GetLogoFile(int selectedAcqIdx) {
        String b = null;
        switch (selectedAcqIdx) {
            case 46:
                b = "akbank.bmp";
                break;
            case 208:
                //memcpy(file, &BankAsya, sizeof(BankAsya));
                break;
            case 134:
                //memcpy(file, &DenizBank, sizeof(DenizBank));
                break;
            case 111:
                b = "finansbank.bmp";
                break;
            case 62:
                b = "garanti.bmp";
                break;
            case 99:
                //memcpy(file, &Ing, sizeof(Ing));
                break;
            case 205:
                b = "kuveyt.bmp";
                break;
            case 59:
                //memcpy(file, &Seker, sizeof(Seker));
                break;
            case 32:
                b = "teb.bmp";
                break;
            case 206:
                //memcpy(file, &Turkiyefinans, sizeof(Turkiyefinans));
                break;
            case 64:
                b = "turkiyeis.bmp";
                break;
            case 15:
                b = "vakif.bmp";
                break;
            case 67:
                b = "yapikredi.bmp";
                break;
            case 10:
                b = "ziraat.bmp";
                break;
            case 12:
                b = "HalkBankasi.bmp";
                break;
            case 135:
                b = "Anadolubank.bmp";
                break;
            default:
                //memcpy(file, &Logo, sizeof(Logo));
                break;
        }
        return (b == null) ? null : "BankaLogo/" + b;
    }

    static void PrintBankLogo(int slipType) throws Exception {
        byte[] tmpStr = new byte[5];
        sprintf(tmpStr, "%02X%02X", currentTran.AcqId[0], currentTran.AcqId[1]);
        String logoFile = GetLogoFile(atoi(tmpStr));

        if (logoFile != null) {
           // PrintLogo(Utility.getBitmapFromAsset(UI.UiUtil.getApplicationContext(), logoFile));
        }
    }

    static void PrintSettlementBankLogo(int tranNo) {
//        char tmpStr[5];
//        unsigned char *filename[10240];
//        memset(tmpStr,0,sizeof(tmpStr));
//        memset(filename,0,sizeof(filename));
//        sprintf(tmpStr, "%02X%02X",GetVTermPrms().AcqInfos[tranNo].AcqId[0], GetVTermPrms().AcqInfos[tranNo].AcqId[1]);
//        GetLogoFile(&filename, atoi(tmpStr));
//
//        if(strlen(EcrcurrentTran.prntFile))
//        {
//            EP_PrntWrite2File(EcrcurrentTran.prntFile, EP_PRN_FILE_TYPE_LOGO, sizeof(filename), filename);
//            //EP_PrntWrite2File(EcrcurrentTran.prntFile, EP_PRN_FILE_TYPE_LOGO, sizeof(Logo), Logo);
//        }
//	    else if(!get_msgdata().IsEcrTran)
//        {
//            EP_PrntLgo(filename, 60);
//        }
//	    else
//        {
//            EP_PrntWrite2File(EcrcurrentTran.prntFile, EP_PRN_FILE_TYPE_LOGO, sizeof(filename), filename);
//		/*unsigned char buf[16];
//		memcpy(buf, currentTran.AcqId, 2);
//		buf[2] = 1;
//		EcrPrntLine(F_GRAPHIC, buf, 3);*/
//        }
    }

    static void PrintMerchantLogo(int slipType) {
	/*if(strlen(EcrcurrentTran.prntFile))
	{
		//EP_printf("Logo:%d", sizeof(Logo));
		//EP_PrntWrite2File(EcrcurrentTran.prntFile, EP_PRN_FILE_TYPE_LOGO, sizeof(Logo), Logo);
	}
	else if(!get_msgdata().IsEcrTran)
		EP_PrntLgo(Logo, 60);
	else
	{
		unsigned char buf[16];
		memcpy(buf, currentTran.AcqId, 2);
		buf[2] = 1;
		EcrPrntLine(F_GRAPHIC, buf, 3);
	}*/
    }

    static void PrintStr(MainPrinter.LineWriter lw, final byte[] bStr) {
	/*
	EP_RTrim(str,'\r');
	if(str[strlen(str) - 1] == '\n' && str[strlen(str) - 2] == '\r')
	{
		str[strlen(str) - 1] = '\0';
		str[strlen(str) - 2] = '\n';
	}
	*/
        String s = c.ToString(bStr);
        lw.Write(s);
    }

    static void PrintStr(MainPrinter.LineWriter lw, final String str) {
	/*
	EP_RTrim(str,'\r');
	if(str[strlen(str) - 1] == '\n' && str[strlen(str) - 2] == '\r')
	{
		str[strlen(str) - 1] = '\0';
		str[strlen(str) - 2] = '\n';
	}
	*/

        String s = str;
        if (s.length() > 1 && s.charAt(s.length() - 1) == '\n')
            s = s.substring(0, s.length() - 1);
        lw.Write(s);
    }

    static void PrintStartReceipt(byte type) {

        if (type == 1) {
            UI.ShowMessage(0, "İŞYERİ NÜSHASI\nBASILIYOR");

        } else {
            UI.ShowMessage(0, "MÜŞTERİ NÜSHASI\nBASILIYOR");
        }
    }

    static void PrintEndReceipt(byte type) {
    }
}
