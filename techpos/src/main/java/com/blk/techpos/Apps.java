package com.blk.techpos;

import static com.blk.sdk.MainPrinter.PrintTest;
import static com.blk.sdk.c.ToString;
import static com.blk.sdk.c.memcpy;
import static com.blk.sdk.c.memset;
import static com.blk.sdk.c.sizeof;
import static com.blk.techpos.Bkm.TranStruct.currentTran;
import static com.blk.techpos.PrmStruct.params;

import android.database.sqlite.SQLiteDatabase;

import com.blk.platform.IPlatform;
import com.blk.sdk.Emv.Config;
import com.blk.sdk.Rtc;
import com.blk.sdk.UI;
import com.blk.sdk.Utility;
import com.blk.sdk.c;
import com.blk.sdk.file;
import com.blk.techpos.Bkm.Batch;
import com.blk.techpos.Bkm.Messages.EndOfDay;
import com.blk.techpos.Bkm.Messages.ParameterDownload;
import com.blk.techpos.Bkm.Reversal;
import com.blk.techpos.Bkm.VParams.PrmFileHeader;
import com.blk.techpos.Bkm.VParams.VTerm;
import com.blk.techpos.d9.D9;

import java.io.File;
import java.io.IOException;

public class Apps {
    private static final String TAG = Apps.class.getSimpleName();
    private static int[] images = new int[16];

    public static int MerchantPwd(int op) {
        return 0;
    }

    static boolean isManualSend = false;

    public static void MerchantMenu() throws Exception {
        int rv = 0;
        String[] items = new String[16];
        int[] images = new int[16];

        String password;

//        if ((password = UI.GetNumber("ŞİFRE GİRİNİZ", "", 4, 4, true, 30, true)) == null)
//            return;
//
//        if (!password.equals(params.MercPwd)) {
//            UI.ShowMessage(2000, "HATALI ŞİFRE\nTEKRAR DENEYİNİZ");
//            return;
//        }

        while (true) {
            items[0] = "GÜNSONU";
            items[1] = "ARA RAPOR";
            items[2] = "FİŞ TEKRARI";
            items[3] = "PARAMETRE YÜKLEME";
            items[4] ="ŞİFRE DEĞİŞTİRME";
            items[5] = "ŞİFRE SIFIRLAMA";
            items[6] = "BANKA SEÇİMİ";
            items[7] = "YARDIM";

            images[0] = R.drawable.gun_sonu;
            images[1] = R.drawable.ara_rapor;
            images[2] = R.drawable.fis_tekrari;
            images[3] = R.drawable.banka_secimi;
            images[4] = R.drawable.parametre_yukleme;
            images[5] = R.drawable.banka_irtibat;
            images[6] = R.drawable.servis_menu;
            images[7] = R.drawable.debug;
//            images[8] = R.drawable.sifre_degistir;
//            images[9] = R.drawable.sifre_sifirla;
            //   images[10] = R.drawable.kurulum;

//            if (WebServicePrms.GetWSParams().isOdeal.equals("True")) {
//                items[11] = "VERİLERİ GÖNDER";
//                images[11] = R.drawable.verileri_gonder;
//            }

            rv = UI.ShowList("İŞYERİ", items, images);
            if (rv < 0)
                break;

            switch (rv) {
                case 0:
                    Settle();
                    break;
                case 1:
                    if ((VTerm.GetVTermPrms().AcqInfoLen <= 0) || !VTerm.IsVTermExist()) {
                        UI.ShowMessage(3000, "PARAMETRE YÜKLEYİNİZ");
                        return;
                    } else
                        Print.PrintEndOfDay(1, 0, 1);
                    break;
                case 2:
                    RePrintMenu();
                    break;
                case 3:
                    ParameterDownload.Download();
                    break;
                case 4:
                    ChangePassword();
                    break;
                case 5:
                    ResetPassword();
                    break;
                case 6:
                    SetAcquier();
                    break;
                case 7:
                    Print.PrintBankConnInfos();
                    break;
                case 8:
                    ServiceMenu();
                    break;
                case 9:
                    Debug();
                    break;
                case 10:
                    AdminMenu();
                    break;
//                case 11:
//                    Odeal.RetrieveFailedTable();
//                    break;
            }

            if (MyReceiver.fStartTran) {
                return;
            }
        }
    }

    private static void ServiceMenu() {
        int rv = 0;
        String[] items = new String[4];
        String pass;

        if (!BuildConfig.DEBUG) {
            if ((pass = UI.GetNumber("ŞİFRE GİRİNİZ", "", 4, 4, false, 30, true)) == null)
                return;

            if (!pass.equals("1234")) {
                UI.ShowMessage(2000, "HATALI ŞİFRE\nTEKRAR DENEYİNİZ");
                return;
            }
        }

        while (true) {
            items[0] = "KEYLERİ AKTAR";
            items[1] = "KEYLERİ SİL";
            items[2] = "KEY KONTROL";
            items[3] = "BAĞLANTI AYARLARI";

            rv = UI.ShowList("SERVİS MENÜSÜ", items, images);
            if (rv < 0)
                break;

            try {
                switch (rv) {
                    case 0:
                        //Keys.LoadKeys();
                        break;
                    case 1:
                        //Keys.DeleteKeys();
                        break;
                    case 2:
                        //Keys.CheckKeys();
                        break;
                    case 3:
                        UI.NetworkSettings();
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static int Debug() throws Exception {

        switch (UI.ShowList("DEBUG", new String[]{
                "Reversal Sil",
                "Batch Sil",
                (IPlatform.get().DebugMode(null) ? "Debug Mode Kapat" : "Debug Mode Aç") ,
                (Utility.GetSharedPrefInt("storage") == 0 ? "SDCard Kullan": "SDCard Kullanma"),
                "Sıfırla"
        }, images)) {
            case 0:
                Reversal.RemoveReversalTran();
                UI.Toast("Reversal silindi.");
                break;
            case 1:
                Batch.DeleteFiles();
                UI.Toast("Batch silindi.");
                break;
            case 2:
                if (IPlatform.get().DebugMode(!IPlatform.get().DebugMode(null))) {
                    UI.ShowMessage("Debug mode on");
                    Utility.CloseApp();
                }
                else {
                    UI.ShowMessage("Debug mode off.");
                    //Utility.CloseApp();
                }
                break;
            case 3:
                //file.Remove(Utility.dbPath);
                SQLiteDatabase.deleteDatabase(new File(Utility.dbPath));
                PrmFileHeader.DeleteVParams();
                Batch.DeleteFiles();
                Reversal.RemoveReversalTran();
                file.Remove(Config.emv_config_file);
                file.Remove(Config.emvcl_config_file);

                Utility.SaveSharedPref("storage", (Utility.GetSharedPrefInt("storage") == 0) ? 1 : 0);

                UI.ShowMessage("Yeniden başlatılıyor.");
                Utility.CloseApp();
                break;
            case 4:
                //file.Remove(Utility.dbPath);
                SQLiteDatabase.deleteDatabase(new File(Utility.dbPath));
                PrmFileHeader.DeleteVParams();
                Batch.DeleteFiles();
                Reversal.RemoveReversalTran();
                file.Remove(Config.emv_config_file);
                file.Remove(Config.emvcl_config_file);

                UI.ShowMessage("Uygulama Sıfırlandı.");
                Utility.CloseApp();
                break;

        }
        return 0;
    }

    private static void ChangePassword() throws InterruptedException, NoSuchFieldException, IllegalAccessException {
        String firstEntry, secondEntry;
        if ((firstEntry = UI.GetNumber("ŞİFRE GİRİNİZ", "", 4, 4, true, 30, true)) == null)
            return;

        if ((secondEntry = UI.GetNumber("ŞİFRE TEKRAR", "", 4, 4, true, 30, true)) == null)
            return;

        if (firstEntry.equals(secondEntry)) {
            UI.ShowMessage("ŞİFRELER EŞLEŞTİ");

            params.MercPwd = firstEntry;

            params.Save("MercPwd");

        } else {
            UI.ShowMessage("ŞİFRELER EŞLEŞMEDİ, TEKRAR DENEYİNİZ");
            ChangePassword();
        }

    }

    private static void ResetPassword() throws NoSuchFieldException, IllegalAccessException {
        params.MercPwd = "0000";
        params.Save("MercPwd");
        UI.ShowMessage("ŞİFRE SIFIRLANDI");
    }

    private static void Settle() throws Exception {

        if ((VTerm.GetVTermPrms().AcqInfoLen <= 0) || !VTerm.IsVTermExist()) {
            if (!VTerm.IsVTermExist())
                Utility.log("IsVTermExist");
            if ((VTerm.GetVTermPrms().AcqInfoLen <= 0))
                Utility.log("AcqInfoLen");

            UI.ShowMessage("PARAMETRE YÜKLEYİNİZ");
            return;
        }

        UI.ShowMessage(0, "LÜTFEN BEKLEYİNİZ\nİŞLEMİNİZ YAPILIYOR");
        if (EndOfDay.ProcessEndOfDay(false) == 0) {
            params.LastManuelEodTime = Rtc.GetTimeSeconds();
            PrmStruct.Save("LastManuelEodTime");
        }


    }

    private static void SetAcquier() throws NoSuchFieldException, IllegalAccessException {
        int rv = 0, i = 0;
        String[] items = new String[32];

        if (params.BkmParamStatus == 0) {
            UI.ShowMessage(2000, "PARAMETRE YÜKLEYİNİZ");
            return;
        }

        if (VTerm.GetVTermPrms().AcqInfoLen <= 1) {
            UI.ShowMessage(2000, "BANKA SAYISI\nYETERLİ DEĞİL");
            return;
        }

        items[0] = "OTOMATİK";
        items[1] = "MANUEL";

        rv = UI.ShowList("BANKA SEÇİMİ", items, images);
        switch (rv) {
            case 0:
                params.AcqSelMode = 0;

                for (i = 0; i < VTerm.GetVTermPrms().AcqInfoLen; i++) {
                    items[i] = ToString(VTerm.GetVTermPrms().AcqInfos[i].MenuStr);
                }
                items[i++] = "HİÇBİRİ";

                rv = UI.ShowList("DEFAULT BANKA", items, images);
                if (rv >= 0) {
                    if (rv == (i - 1)) {
                        memset(params.DefAcq, (byte) 0, sizeof(params.DefAcq));
                        memset(params.DefTermId, (byte) 0, sizeof(params.DefTermId));
                    } else {
                        memcpy(params.DefAcq, VTerm.GetVTermPrms().AcqInfos[rv].AcqId, 2);
                        memcpy(params.DefTermId, VTerm.GetVTermPrms().AcqInfos[rv].TermId, 8);
                    }
                }

                break;
            case 1:
                params.AcqSelMode = 1;
                memset(params.DefAcq, (byte) 0, 2);
                memset(params.DefTermId, (byte) 0, sizeof(params.DefTermId));
                break;
        }

        PrmStruct.Save();
    }

    private static void RePrintMenu() {
        try {
            int rv = 0;
            String[] items = new String[2];

            items[0] = "SON İŞLEM";
            items[1] = "SON GÜNSONU";

            rv = UI.ShowList("FİŞ TEKRARI", items, new int[]{R.drawable.islem_menu, R.drawable.gun_sonu});
            if (rv >= 0) {
                switch (rv) {
                    case 0:
                        if (Batch.RestoreLastTran(currentTran) == 0) {
                            VTerm.SelVTermAcqInfoByAcqId(currentTran.AcqId, currentTran.TermId);
                            Print.PrintTran(1);
                        } else
                            UI.ShowMessage(2000, "İŞLEM YOK");
                        break;
                    case 1:
                        if (Batch.GetBatchTotalsForPrint().len != 0)
                            Print.PrintEndOfDay(0, 1, 0);
                        else
                            UI.ShowMessage(2000, "İŞLEM YOK");
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void ChangeIpPort() throws NoSuchFieldException, IllegalAccessException, IOException {
        if (Batch.GetTranCount() > 0) {
            UI.ShowMessage(2000, "GÜNSONU YAPINIZ");
            return;
        }

        String input;

        if ((input = UI.GetIP("İP GİRİNİZ", params.sTcpip.destIp)) == null)
            return;
        params.sTcpip.destIp = input;
        params.Save("sTcpip.destIp");

        if ((input = UI.GetNumber("PORT GİRİNİZ", "" + params.sTcpip.destPort, 2, 5, false, 30, false)) == null)
            return;
        params.sTcpip.destPort = c.atoi(input.getBytes());
        params.Save("sTcpip.destPort");

        if ((input = UI.GetIP("YEDEK İP GİRİNİZ", params.sBackupTcpip.destIp)) == null)
            return;
        params.sBackupTcpip.destIp = input;
        params.Save("sBackupTcpip.destIp");

        if ((input = UI.GetNumber("YEDEK PORT GİRİNİZ", "" + params.sBackupTcpip.destPort, 2, 5, false, 30, false)) == null)
            return;
        params.sBackupTcpip.destPort = c.atoi(input.getBytes());
        params.Save("sBackupTcpip.destPort");

        UI.ShowMessage(2000, "IP-PORT KAYDEDİLDİ");
    }

    public static void AdminMenu() throws Exception {

//        String passEntry;
//
//        String day = String.valueOf(Rtc.EP_RtcGet().day);
//        String month = String.valueOf(Rtc.EP_RtcGet().mon);
//        String year = String.valueOf(Rtc.EP_RtcGet().year);
//
//        int total = Integer.parseInt(day) + Integer.parseInt(month);
//
//        StringBuilder passBuilder = new StringBuilder();
//        passBuilder.append(year);
//        passBuilder.append(total);
//
//        String password = String.valueOf(passBuilder);
//
//        if ((passEntry = UI.GetNumber("ŞİFRE GİRİNİZ", "", 4, 4, true, 30, true)) == null)
//            return;
//
//        if (!passEntry.equals(password)) {
//            UI.ShowMessage("HATALI ŞİFRE\nTEKRAR DENEYİNİZ", 2000);
//            return;
//        }


        while (true) {

            int rv = 0;
            String[] items = new String[16];
            items[0] = "NETWORK SETUP";
            items[1] = (params.D9(null) ? "D9 KAPAT" : "D9 AÇ");
            items[2] = "UZAKTAN YUKLEME";
            items[3] = "SISTEM UPDATE";
            items[4] = "DEBUG MENU";
            items[5] = "PRINTER TEST";

            rv = UI.ShowList("ADMİN", items, images);

            if (rv < 0)
                break;

            switch (rv) {
                case 0:
                    UI.NetworkSettings();
                    //ChangeIpPort();
                    break;
                case 1:
                    if (!params.D9(null)) {
                        params.D9(true);
                        if (D9.Connect())
                            UI.ShowMessage("D9 AÇILDI");
                    }
                    else {
                        D9.Disconnect();
                        params.D9(false);
                        UI.ShowMessage("D9 KAPATILDI");
                    }
                    break;
                case 2:
                {
                }
                    break;
                case 4:
                    Apps.Debug();
                    break;

                case 5:
                    PrintTest();
                    break;
        }
    }


    }
}
