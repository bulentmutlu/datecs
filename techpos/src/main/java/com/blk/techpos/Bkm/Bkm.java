package com.blk.techpos.Bkm;

import static com.blk.sdk.c.atoi;


import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Build;
import android.util.Log;

import com.blk.platform.Emv.CAPublicKey;
import com.blk.platform.Emv.EmvApp;
import com.blk.platform.Emv.IEmv;
import com.blk.sdk.Convert;
import com.blk.sdk.DT;
import com.blk.sdk.DeviceInfo;
import com.blk.sdk.UI;
import com.blk.sdk.file;
import com.blk.sdk.string;
import com.blk.sdk.Utility;
import com.blk.sdk.Tlv;
import com.blk.sdk.c;
import com.blk.techpos.Bkm.Messages.Msgs;
import com.blk.techpos.Bkm.Messages.OfflineAdvice;
import com.blk.techpos.Bkm.Messages.QR;
import com.blk.techpos.Bkm.VParams.PrmFileHeader;
import com.blk.techpos.Bkm.VParams.VTerm;
import com.blk.techpos.Comms;
import com.blk.techpos.Print;
import com.blk.techpos.PrmStruct;
import com.blk.techpos.R;
import com.blk.techpos.tran.Tran;

import static com.blk.sdk.c.memcpy;
import static com.blk.sdk.c.memset;
import static com.blk.sdk.c.strcpy;
import static com.blk.techpos.Bkm.TranStruct.EM_QR;
import static com.blk.techpos.Bkm.TranStruct.currentTran;
import static com.blk.techpos.PrmStruct.params;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Bkm {
    private static final String TAG = Bkm.class.getSimpleName();
    
    public static final String file_VCAPK =                   "VCAK";
    public static final String file_VCOMM                 = "VCOMM";
    public static final String file_VEMVCLSPP2            = "VEMVCLSPP2";
    public static final String file_VEMVCLSPP3            = "VEMVCLSPP3";
    public static final String file_VEMVCLSPW2            = "VEMVCLSPW2";
    public static final String file_VEMVCONFIG               = "VEMVCON";
    public static final String file_VEOD                  = "VEOD";
    public static final String file_VPRMS                 = "VPRMS";

    public static byte VendorID = 0x12;

    public static String BkmDeviceModel()
    {
        return "V10";
    }
    public static String GetSerialStr()
    {
        String serial = DeviceInfo.devInfo.dSerial.substring(5, 15);
        return serial;
    }

    // CA2171700160 / CA21717001600000BLKV10010036470000000000
    static String dvcInfo;
    @SuppressLint("DefaultLocale")
    public static String GetDeviceInfo()
    {
        int osVer = atoi(string.DeleteChar(Build.VERSION.RELEASE, '.').getBytes());
        int appVer = atoi(Utility.GetAppVersion(false).getBytes());

        osVer %= 10000; // strip to 4 character
        appVer%= 10000;

        osVer = 3647;
        appVer = 100;

        dvcInfo = String.format("CA%.10s0000BLK%.03s%04d%04d%02X%02X000000",
                Bkm.GetSerialStr(), BkmDeviceModel(),
                appVer, osVer, params.DefAcq[0], params.DefAcq[1]);
        Log.i(TAG, "DeviceInfo: " + dvcInfo);

        return dvcInfo;
    }

    public static void GenerateOffAuthCode() throws Exception {
        c.sprintf(currentTran.AuthCode, "O%03d%02d", params.BatchNo % 1000, Batch.GetTranCountOfflineApproved() % 100);
    }
    public static byte[] GetServiceCode(byte[] track2) {
        int i = 0;

        for (i = 0; i < c.sizeof(track2); i++) {
            if (track2[i] == '=')
                return Arrays.copyOfRange(track2, i + 5, track2.length);
        }

        return "FFF".getBytes();
    }

    public static EmvApp[] ParseConfig(String file, byte kernel) throws IOException {
        List<EmvApp> apps = new ArrayList<>();

        byte[] pBuff = com.blk.sdk.file.ReadAllBytes(file);
        if (pBuff.length == 0) return new EmvApp[0];

        int idx = PrmFileHeader.sizeof;
        int size = pBuff.length - idx;
        int i = 0;

        while(true)
        {
            Tlv aData = Tlv.GetBerTlvData(pBuff, idx, pBuff.length, 0xBF8B02, i);
            if (aData == null) break;

            Tlv tData = Tlv.GetBerTlvData(aData.val, aData.len, 0x9F06, 0);
            if(tData != null)
            {
                EmvApp emvApp= new EmvApp();
                emvApp.kernel = kernel;

                memcpy(emvApp.aid, tData.val, tData.len);
                emvApp.aidLen = (byte) tData.len;

                //Log.i(TAG, String.format("AIDPrms %s", Convert.Buffer2Hex(emvApp.aid, 0, emvApp.aidLen)));

                if((tData = Tlv.GetBerTlvData(aData.val, aData.len, 0x9F35, 0)) != null)
                {
                    emvApp.tags.add(new IEmv.TlvData(0x9F35, tData.len, tData.val));
                    //Terminal Type - Binary
                }
                if((tData = Tlv.GetBerTlvData(aData.val, aData.len, 0x9F33, 0)) != null)
                {
                    emvApp.tags.add(new IEmv.TlvData(0x9F33, tData.len, tData.val));
                    // Terminal Capabilities - Binary
                }
                if((tData = Tlv.GetBerTlvData(aData.val, aData.len, 0x9F40, 0)) != null)
                {
                    emvApp.tags.add(new IEmv.TlvData(0x9F40, tData.len, tData.val));
                    // Additional Terminal Capabilities - Binary
                }
                if((tData = Tlv.GetBerTlvData(aData.val, aData.len, 0x9F1A, 0)) != null)
                {
                    emvApp.tags.add(new IEmv.TlvData(0x9F1A , tData.len, tData.val));
                    // Terminal Country Code  - BCD
                }
                if((tData = Tlv.GetBerTlvData(aData.val, aData.len, 0x5F2A, 0)) != null)
                {
                    emvApp.tags.add(new IEmv.TlvData(0x5F2A, tData.len, tData.val));
                    // Transaction Para Birimi Kodu - BCD
                }
                if((tData = Tlv.GetBerTlvData(aData.val, aData.len, 0x5F36, 0)) != null)
                {
                    emvApp.tags.add(new IEmv.TlvData(0x5F36, tData.len, tData.val));
                    // Transaction Currency Exponent - Binary
                }

                if((tData = Tlv.GetBerTlvData(aData.val, aData.len, 0xDF8B11, 0)) != null)
                {
                    // Threshold Value for Biased random Selection
                    emvApp.tags.add(new IEmv.TlvData(0xDF8B11, tData.len, tData.val));
                    //emvApp.threshold = Convert.SWAP_UINT32(Convert.ToInt(tData.Val, 0));
                }
                if((tData = Tlv.GetBerTlvData(aData.val, aData.len, 0xDF8B12, 0)) != null)
                {
                    emvApp.tags.add(new IEmv.TlvData(0xDF8B12, tData.len, tData.val));
//                    emvApp.dDOL[0] = (byte) tData.Len;
//                    memcpy(emvApp.dDOL, 1, tData.Val, 0, tData.Len);
                }

                if((tData = Tlv.GetBerTlvData(aData.val, aData.len, 0xDF8B13, 0)) != null)
                {
                    emvApp.tags.add(new IEmv.TlvData(0xDF8B13, tData.len, tData.val));
//                    emvApp.tDOL[0] = (byte) tData.Len;
//                    memcpy(emvApp.tDOL, 1, tData.Val, 0, tData.Len);
                }

                if((tData = Tlv.GetBerTlvData(aData.val, aData.len, 0xDF8B14, 0)) != null)
                {
                    emvApp.tags.add(new IEmv.TlvData(0xDF8B14, tData.len, tData.val));
                    //emvApp.maxTargetPer = tData.Val[0];
                }

                if((tData = Tlv.GetBerTlvData(aData.val, aData.len, 0xDF8B15, 0)) != null)
                {
                    emvApp.tags.add(new IEmv.TlvData(0xDF8B15, tData.len, tData.val));
                    //emvApp.targetPer = tData.Val[0];
                }
                if((tData = Tlv.GetBerTlvData(aData.val, aData.len, 0xDF8B16, 0)) != null)
                {
                    // fixme
                    //emvApp.tags.add(new TlvData(0xDF8B16, tData.len, tData.val));
                    // Application Selection Indicator (1: partial supported)
                }
                if((tData = Tlv.GetBerTlvData(aData.val, aData.len, 0xDF8120, 0)) != null)
                {
                    emvApp.tags.add(new IEmv.TlvData(0xDF8120, tData.len, tData.val));
                    //memcpy(emvApp.tacDefault, tData.Val, tData.Len);
                    //Log.i(TAG, String.format("TACDefault:%02X%02X%02X%02X%02X", emvApp.TACDefault[0], emvApp.TACDefault[1], emvApp.TACDefault[2], emvApp.TACDefault[3], emvApp.TACDefault[4]);
                }

                if((tData = Tlv.GetBerTlvData(aData.val, aData.len, 0xDF8121, 0)) != null)
                {
                    emvApp.tags.add(new IEmv.TlvData(0xDF8121, tData.len, tData.val));
                    //memcpy(emvApp.tacDenial, tData.Val, tData.Len);
                    //Log.i(TAG, String.format("TACDenial:%02X%02X%02X%02X%02X", emvApp.TACDenial[0], emvApp.TACDenial[1], emvApp.TACDenial[2], emvApp.TACDenial[3], emvApp.TACDenial[4]);
                }

                if((tData = Tlv.GetBerTlvData(aData.val, aData.len, 0xDF8122, 0)) != null)
                {
                    emvApp.tags.add(new IEmv.TlvData(0xDF8122, tData.len, tData.val));
                    //memcpy(emvApp.tacOnline, tData.Val, tData.Len);
                    //Log.i(TAG, String.format("TACOnline:%02X%02X%02X%02X%02X", emvApp.TACOnline[0], emvApp.TACOnline[1], emvApp.TACOnline[2], emvApp.TACOnline[3], emvApp.TACOnline[4]);
                }

                if((tData = Tlv.GetBerTlvData(aData.val, aData.len, 0x9F09, 0)) != null)
                {
                    emvApp.tags.add(new IEmv.TlvData(0x9F09, tData.len, tData.val));
                    //memcpy(emvApp.version, tData.Val, tData.Len);
                }
                if((tData = Tlv.GetBerTlvData(aData.val, aData.len, 0x9F1B, 0)) != null)
                {
                    emvApp.tags.add(new IEmv.TlvData(0x9F1B, tData.len, tData.val));
                    //emvApp.floorLimit = Convert.SWAP_UINT32(Convert.ToInt(tData.Val, 0));
                    //Log.i(TAG, String.format("floorlimit:%d", emvApp.FloorLimit);
                }

                // CTLS

                // Entry Point Kernel to Use
                if((tData = Tlv.GetBerTlvData(aData.val, aData.len, 0xDF8B01, 0)) != null)
                    emvApp.tags.add(new IEmv.TlvData(0xDF8B01, tData.len, tData.val));
                // Entry Point AID Options
                if((tData = Tlv.GetBerTlvData(aData.val, aData.len, 0xDF8B02, 0)) != null)
                    emvApp.tags.add(new IEmv.TlvData(0xDF8B02, tData.len, tData.val));
                // Entry Point Contactless Transaction Limit
                if((tData = Tlv.GetBerTlvData(aData.val, aData.len, 0xDF8B03, 0)) != null)
                    emvApp.tags.add(new IEmv.TlvData(0xDF8B03, tData.len, tData.val));
                // Entry Point CVM Required Limit
                if((tData = Tlv.GetBerTlvData(aData.val, aData.len, 0xDF8B04, 0)) != null)
                    emvApp.tags.add(new IEmv.TlvData(0xDF8B04, tData.len, tData.val));
                // Entry Point Reader Contactles Floor Limit
                if((tData = Tlv.GetBerTlvData(aData.val, aData.len, 0xDF8B05, 0)) != null)
                    emvApp.tags.add(new IEmv.TlvData(0xDF8B05, tData.len, tData.val));
                // Default UDOL
                if((tData = Tlv.GetBerTlvData(aData.val, aData.len, 0xDF811A, 0)) != null)
                    emvApp.tags.add(new IEmv.TlvData(0xDF811A, tData.len, tData.val));
                // Mag-stripe Application Version Number (Reader)
                if((tData = Tlv.GetBerTlvData(aData.val, aData.len, 0x9F6D, 0)) != null)
                    emvApp.tags.add(new IEmv.TlvData(0x9F6D, tData.len, tData.val));
                // Terminal Capabilities CVM Required
                if((tData = Tlv.GetBerTlvData(aData.val, aData.len, 0xDF8B21, 0)) != null)
                    emvApp.tags.add(new IEmv.TlvData(0xDF8B21, tData.len, tData.val));
                // Terminal Capabilities No CVM Required
                if((tData = Tlv.GetBerTlvData(aData.val, aData.len, 0xDF8B22, 0)) != null)
                    emvApp.tags.add(new IEmv.TlvData(0xDF8B22, tData.len, tData.val));
                // Entry Point Terminal Transaction Qualifiers
                if((tData = Tlv.GetBerTlvData(aData.val, aData.len, 0x9F66, 0)) != null)
                    emvApp.tags.add(new IEmv.TlvData(0x9F66, tData.len, tData.val));

                apps.add(emvApp);
            }
            i++;
        }

        return apps.toArray(new EmvApp[0]);
    }

    public static HashMap<String, List<CAPublicKey>> ParseKeys() throws IOException {
        HashMap<String, List<CAPublicKey>> keys = new HashMap<>();
        byte[] pBuff = file.ReadAllBytes(file_VCAPK);

        if (pBuff.length == 0) return keys;

        int idx = PrmFileHeader.sizeof;
        int size = pBuff.length - idx;
        int i = 0;

        Tlv aData, tData;

        while(true)
        {
            if((aData = Tlv.GetBerTlvData(pBuff, idx, size, 0xBF8B01, i)) == null)
                break;
            if((tData = Tlv.GetBerTlvData(aData.val, aData.len, 0xDF8B01, 0)) != null)
            {
                CAPublicKey emvCak = new CAPublicKey();

                memcpy(emvCak.rID, tData.val, 5);
                memcpy(emvCak.expDate, new byte[] {0x12,0x12,0x31}, 3);

                if((tData = Tlv.GetBerTlvData(aData.val, aData.len, 0xDF8B02, 0)) != null)
                {
                    emvCak.index = tData.val[0];
                }

                if((tData = Tlv.GetBerTlvData(aData.val, aData.len, 0xDF8B03, 0)) != null)
                {
                    emvCak.hashInd = tData.val[0];
                }

                if((tData = Tlv.GetBerTlvData(aData.val, aData.len, 0xDF8B04, 0)) != null)
                {
                    emvCak.arithInd = tData.val[0];
                }

                if((tData = Tlv.GetBerTlvData(aData.val, aData.len, 0xDF8B05, 0)) != null)
                {
                    if(tData.len > c.sizeof(emvCak.modulus))
                        tData.len = c.sizeof(emvCak.modulus);

                    emvCak.modulusLen = (short) tData.len;
                    memcpy(emvCak.modulus, tData.val, tData.len);
                }

                if((tData = Tlv.GetBerTlvData(aData.val, aData.len, 0xDF8B06, 0)) != null)
                {
                    if(tData.len > c.sizeof(emvCak.exponent))
                        tData.len = c.sizeof(emvCak.exponent);

                    emvCak.exponentLen = (byte) tData.len;
                    memcpy(emvCak.exponent, tData.val, tData.len);
                }

                if((tData = Tlv.GetBerTlvData(aData.val, aData.len, 0xDF8B07, 0)) != null)
                {
                    if(tData.len > c.sizeof(emvCak.hash))
                        tData.len = c.sizeof(emvCak.hash);

                    memcpy(emvCak.hash, tData.val, tData.len);
                }
                String rId = Convert.Buffer2Hex(emvCak.rID);
                if (!keys.containsKey(rId))
                    keys.put(rId, new ArrayList<>());
                keys.get(rId).add(emvCak);

                //Log.i(TAG, String.format("CAPK rID : %s keyID : %02X ", rId, emvCak.index));
            }

            i++;
        }
        return keys;
    }


    public static int returnBmpImage(byte[] acqId) {

        InputStream inputStream = new ByteArrayInputStream(acqId);
        int data = 0;
        try {
            data = inputStream.read();
            data = inputStream.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String hex = Integer.toHexString(data);
        Log.i("BANKA ID=", hex);

        if (hex.equals("1")) {
            return R.drawable.merkez_bankasi;
        } else if (hex.equals("111")) {
            return R.drawable.finansbank;
        } else if (hex.equals("32")) {
            return R.drawable.teb;
        } else if (hex.equals("64")) {
            return R.drawable.turkiyeis;
        } else if (hex.equals("10")) {
            return R.drawable.ziraat;
        } else if (hex.equals("12")) {
            return R.drawable.halkbank;
        } else if (hex.equals("205")) {
            return R.drawable.kuveyt;
        } else if (hex.equals("15")) {
            return R.drawable.vakif;
        } else if (hex.equals("67")) {
            return R.drawable.akbank;
        } else if (hex.equals("46")) {
            return R.drawable.akbank;
        } else if (hex.equals("62")) {
            return R.drawable.garanti;
        } else {
            return 0;
        }
    }

    public static int DoTranOnl() throws Exception {
        int rv = -1;

        OfflineAdvice.ProcessOfflineAdvice(0);

        rv = Msgs.ProcessMsg(Msgs.MessageType.M_AUTHORIZATION);

        PrmStruct.Save();
        if (rv == -999) {
        } else {
            if (!c.strcmp(currentTran.RspCode, "55")) {
                UI.ShowMessage(1000, c.ToString(currentTran.ReplyDescription));
                if (TranUtils.PerformPinEntry(1) != 0)
                    return rv;

                currentTran.MsgTypeId -= 10;
                c.memset(currentTran.ReplyDescription, (byte) 0, c.sizeof(currentTran.ReplyDescription));

                return DoTranOnl();
            }

            if (rv == 0) {
                if (!(currentTran.emvOnlineFlow != 0 && currentTran.unableToGoOnline != 0)) {
                    if (c.strlen(currentTran.ReplyDescription) > 0 && c.memcmp(currentTran.RspCode, "00".getBytes(), 2))
                        UI.ShowMessage(1500, c.ToString(currentTran.ReplyDescription));
                }

                if (currentTran.EntryMode == EM_QR) // && currentTran.f39OK())
                {
                    QR qr = Tran.currentTranObject.qr;

                    if (currentTran.MsgTypeId == 810 && currentTran.ProcessingCode == 400001) {

                        if (qr.QRReferansData == null) {
                            if (currentTran.f39OK()) c.memset(currentTran.RspCode, (byte) 0, currentTran.RspCode.length);
                            Print.PrintRED();
                            return rv;
                        }

                        final boolean[] fDismissed = {false};
                        Log.i("QR", Convert.byteArray2HexString(qr.GetQr()));
                        UI.ShowQR2("QR OKUTUNUZ",
                                Utility.QRencodeAsBitmap(new String(qr.GetQr(), StandardCharsets.UTF_8),
                                        UI.UiUtil.ScreenSize().getWidth(), UI.UiUtil.ScreenSize().getWidth() * 4 / 5),
                                QR.qrShowTimeout, new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialogInterface) {
                                        fDismissed[0] = true;
                                    }
                                });

                    if (true) {
                        int sleep = (int) DT.toMilliseconds(Convert.unsignedByteToInt(VTerm.GetVTermPrms().qrParams.KartDatasiSorgulamaBaslangici));
                        while (sleep > 0 && !fDismissed[0]) {
                            Utility.sleep(1000);
                            sleep -= 1000;
                        }

                        currentTran.ProcessingCode = 400002;
                        long queryEnd = DT.getSeconds() + Convert.unsignedByteToInt(VTerm.GetVTermPrms().qrParams.KartDatasiSorgulamaSonu);
                        //queryEnd -= 200;
                        do {
                            long currentQueryStart = DT.getSeconds();
                            long nextQueryStart = currentQueryStart + Convert.unsignedByteToInt(VTerm.GetVTermPrms().qrParams.KartDatasiSorgulamaAraligi);

                            if (fDismissed[0]) {
                                if (currentTran.f39OK()) c.memset(currentTran.RspCode, (byte) 0, currentTran.RspCode.length);
                                return -1;
                            }

                            currentTran.MsgTypeId = 800;
                            c.memset(currentTran.ReplyDescription, (byte) 0, c.sizeof(currentTran.ReplyDescription));
                            c.memset(currentTran.RspCode, (byte) 0, currentTran.RspCode.length);
                            Comms.fShowUI = false;
                            rv = DoTranOnl();
                            Comms.fShowUI = true;

                            while (qr.QrCardData == null && DT.getSeconds() < nextQueryStart && !fDismissed[0]) {
                                Utility.sleep(1000);
                                Log.i("", "left(" + (queryEnd - DT.getSeconds()) + ") queryEnd(" + queryEnd + ") nextQ(" + nextQueryStart + ") current(" + DT.getSeconds() + ")");
                            }

                        } while (qr.QrCardData == null && DT.getSeconds() < queryEnd && !fDismissed[0]);

                        if (fDismissed[0]) {
                            if (currentTran.f39OK()) c.memset(currentTran.RspCode, (byte) 0, currentTran.RspCode.length);
                            return -1;
                        }

                        if (qr.QrCardData == null) {
                            if (currentTran.f39OK()) c.memset(currentTran.RspCode, (byte) 0, currentTran.RspCode.length);
                            Print.PrintRED();
                            UI.ShowErrorMessage("Zaman Aşımı");
                            return rv;
                        }
                    }
                    else {
                        qr.QrCardData = "4030820129326010;5001;TE**** TE*******;33;0,00;0,00;0;904;109".getBytes(StandardCharsets.UTF_8);
                    }

                        if (!qr.isFastQr) {
                            //                        Kart Numarası	01-32	ASCII	Seçilen kartın PAN bilgisi
//                        Son Kullanma Tarihi	04	ASCII	Seçilen kartın son kullanma tarihi
//                        Masked Card Holder Name	32	ASCII
//                        CVM Tipi	01	ASCII	İşlemde uygulanacak CVM tipi
//                        İşlem Tutarı 	01-13	ASCII	İşlem tutarı
//                        Puan Tutarı 	01-13	ASCII	İşlemde kullanılacak puan karşılığı tutar bilgisi
//                        Taksit Adedi	02	ASCII	Seçilen taksit adedi
//                        ECI 	03	ASCII	Kullanıcının nasıl doğrulandığı bilgisi
//                        Wallet Program Data	03	ASCII	Kartın nerede tutulduğu ile ilgili değer

                            // 4030820129326010;5001;TE**** TE*******;33;0,00;0,00;0;904;109;
                            try {
                                String[] s = new String(qr.QrCardData).split(";");
                                byte[] pan = s[0].getBytes();
                                memcpy(currentTran.Pan, pan, pan.length);
                                memcpy(currentTran.ExpDate, s[1].getBytes(), 4);
                                memcpy(currentTran.CardHolderName, s[2].getBytes(), s[2].length());
                                Tran.currentTranObject.qr.CVM = s[3];

                                String sAmount = string.PadLeft(s[4].replace(",", ""), currentTran.Amount.length, '0');
                                byte[] amount = sAmount.getBytes();
                                memcpy(currentTran.Amount, amount, amount.length);
                                Log.i(TAG, "Amount : " + c.ToString(currentTran.Amount));

                                sAmount = string.PadLeft(s[5].replace(",", ""), currentTran.Amount.length, '0');
                                amount = sAmount.getBytes();
                                memcpy(currentTran.BonusAmount, amount, amount.length);
                                Log.i(TAG, "BonusAmount : " + c.ToString(currentTran.Amount));

                                currentTran.InsCount = (byte) Integer.parseInt(s[6]);
                                qr.ECI = s[7].getBytes();
                                qr.WalletProgramData = s[8].getBytes();
                            } catch (Exception ex) {
                                c.memset(currentTran.RspCode, (byte) 0, currentTran.RspCode.length);
                                UI.ShowErrorMessage("Hatali QR Bilgisi");
                                return rv;
                            }
                        }
                        else {
//                            İşlem Tutarı 	01-13	ASCII	İşlem tutarı
//                            Gönderen Hesap Maskeli IBAN No	26	ASCII
//                            Ad Soyad	32	ASCII	Kart için Maskeli Kart Sahibi Adı
//                            FAST için Gönderen Ad Soyadı
//                            FAST Gönderen Katılımcı Kodu	4	ASCII
//                            FAST Alan Katılımcı Kodu	4	ASCII
//                            FAST Referans Kodu	30	ASCII
                            try {
                                String[] s = new String(qr.QrCardData).split(";");
                                byte[] amount = s[0].getBytes();
                                memcpy(currentTran.Amount, amount, amount.length);
                                qr.iban = s[1];
                                memcpy(currentTran.CardHolderName, s[2].getBytes(), s[2].length());
                                qr.GonderenAdSoyad = s[3];
                                qr.GonderenKatilimciKodu = s[4];
                                qr.AlanKatilimciKodu = s[5];
                                qr.FASTReferansKodu = s[6];
                            } catch (Exception ex) {
                                c.memset(currentTran.RspCode, (byte) 0, currentTran.RspCode.length);
                                UI.ShowErrorMessage("Hatali QR Bilgisi");
                                return rv;
                            }
                        }

                        if (!qr.isTranAllowed())
                        {
                            c.memset(currentTran.RspCode, (byte) 0, currentTran.RspCode.length);
                            Print.PrintRED();
                            return rv;
                        }

                        currentTran.MsgTypeId = 200;
                        currentTran.ProcessingCode = qr.originalProcessingCode;
                        c.memset(currentTran.ReplyDescription, (byte) 0, c.sizeof(currentTran.ReplyDescription));
                        c.memset(currentTran.RspCode, (byte) 0, currentTran.RspCode.length);
                        return DoTranOnl();
                    }
                }

            }
        }

        return rv;
    }
}
