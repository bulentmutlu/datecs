package com.blk.techpos;

import static com.blk.sdk.c.memcpy;
import static com.blk.sdk.c.memset;
import static com.blk.sdk.c.sprintf;
import static com.blk.sdk.c.strcat;
import static com.blk.techpos.PrmStruct.params;


import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.blk.platform.Emv.CAPublicKey;
import com.blk.platform.Emv.EmvApp;
import com.blk.platform.IPlatform;
import com.blk.sdk.Convert;
import com.blk.sdk.string;
import com.blk.sdk.Utility;
import com.blk.sdk.c;
import com.blk.techpos.Bkm.Bkm;
import com.blk.techpos.Bkm.TranStruct;
import com.blk.techpos.Bkm.VParams.VBin;
import com.blk.techpos.Bkm.VParams.VEod;
import com.blk.techpos.Bkm.VParams.VSpecialBin;
import com.blk.techpos.Bkm.VParams.VTerm;
import com.blk.techpos.d9.D9;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by id on 27.02.2018.
 */

public class techpos {

    private static final String TAG = PrmStruct.class.getSimpleName();
    public static boolean isInited = false;

    public static void Init() throws Exception {

        if (isInited) return;
        isInited = true;
        Utility.Init("techpos");

        PrmStruct.Read();

        if (params.BkmParamStatus != 0)
        {
            VTerm.ReadVTermPrms(null);
            VBin.ReadVBinPrms(null);
            VSpecialBin.ReadVSpecialBinPrms(null);
            VEod.ReadVEodPrms(null);

            //IPlatform.get().emv.Test(Bkm.ParseKeys(), Bkm.ParseConfig(Bkm.file_VEMVCONFIG));

            HashMap<String, List<CAPublicKey>> keys = Bkm.ParseKeys();
            EmvApp[] p1 = Bkm.ParseConfig(Bkm.file_VEMVCLSPP3, (byte) 2);
            EmvApp[] p2 = Bkm.ParseConfig(Bkm.file_VEMVCLSPW2,(byte) 3);
            EmvApp[] ctlsApps = new EmvApp[p1.length + p2.length];
            System.arraycopy(p1, 0, ctlsApps, 0, p1.length);
            System.arraycopy(p2, 0, ctlsApps, p1.length, p2.length);

            IPlatform.get().emv.Init(keys, Bkm.ParseConfig(Bkm.file_VEMVCONFIG, (byte) 0));
            IPlatform.get().emvcl.Init(keys, ctlsApps);
        }
        if (params.D9(null)) {
            D9.Connect();
        }
        //Keys.Read();
    }


    public static void ExecuteQuery(String query) {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(Utility.dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        db.execSQL(query);
        db.close();
    }

    public static void DumpISO(String TAG, String caption, HashMap<String, byte[]> map) {
        Log.i(TAG, "========== " + caption + " ISO =============");
        HashMap<Integer, byte[]> m = new HashMap<>();
        for (String key : map.keySet()) {
            if (key.equals("h"))
                m.put(0, map.get(key));
            else if (key.equals("m"))
                m.put(1, map.get(key));
            else
                m.put(Integer.parseInt(key), map.get(key));
        }

        for (int i = 0; i <= 64; ++i) {
            Integer key = new Integer(i);
            if (m.containsKey(key)) {
                if (i == 63 || i == 62 || i == 55 || i == 48 || i == 52)
                    Log.i(TAG, String.format("%02d : %s", i, Convert.Buffer2Hex(m.get(key))));
                else
                    Log.i(TAG, String.format("%02d : %s", i, new String(m.get(key))));
            }
        }
        Log.i(TAG, "========== END OF ISO =======");
    }

    public static void adjustDESParity(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            int b = bytes[i];
            bytes[i] = (byte) ((b & 0xfe) | ((((b >> 1) ^ (b >> 2) ^ (b >> 3) ^ (b >> 4) ^ (b >> 5) ^ (b >> 6) ^ (b >> 7)) ^ 0x01) & 0x01));
        }
    }

    public static void OsDES(byte[] input, int iStartIndex, byte[] output, int oStartIndex, byte[] DesKey, int keyLen, int mode) throws NoSuchPaddingException, NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException, InvalidKeySpecException, BadPaddingException, IllegalBlockSizeException {
        //memcpy(input, Convert.Hex2Buffer("045D70CD8C39CAC1".getBytes(), 0, 16), 8);

        Cipher c3des = Cipher.getInstance("DESede/ECB/NoPadding");
        SecretKeySpec myKey = new SecretKeySpec(DesKey, "DESede");
//
        c3des.init((mode == 0) ? Cipher.DECRYPT_MODE : Cipher.ENCRYPT_MODE, myKey);
        byte[] cipherText = c3des.doFinal(input, iStartIndex, 8);


        //byte[] cipherText =Openssl.Des(mode != 0, DesKey, input);
        //byte[] cipherText = TDES.crypt((mode != 0), DesKey, input, iStartIndex, 8);
        System.arraycopy(cipherText, 0, output, oStartIndex, cipherText.length);
    }
    public static void OsDES(byte[] input, byte[] output, byte[] DesKey, int keyLen, int mode) throws NoSuchPaddingException, NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException, InvalidKeySpecException, BadPaddingException, IllegalBlockSizeException {
        OsDES(input, 0, output, 0, DesKey, keyLen, mode);
    }

    public static void OsGetRandom(byte[] output, int len) {
        SecureRandom sr = new SecureRandom();
        if (output.length == len) {
            sr.nextBytes(output);
            return;
        }
        byte[] o = new byte[len];
        sr.nextBytes(o);
        System.arraycopy(o, 0, output, 0, len);
    }


    public static byte[] RSA_public_encrypt(PublicKey key, byte[] in) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchProviderException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(in);
    }

    public static void CRC32(byte[] in, int inOffset, int Len, byte[] out, int outOffset) {
        int crc;
        int bit, _byte, carry;
        crc = 0xFFFFFFFF; /* initialization */

        for (_byte = 0; _byte < Len; _byte++) {
            for (bit = 0; bit < 8; bit++) {
                carry = crc & 1;
                crc >>>= 1;
                if ((carry ^ ((in[_byte + inOffset] >> bit) & 1)) != 0)
                    crc ^= 0xedb88320; /* polynomial, bit X^32 is handled by carry */
            }
        }

        crc = ~crc; /* invert CRC */
        out[0 + outOffset] = (byte) (crc >>> 24);
        out[1 + outOffset] = (byte) (crc >>> 16);
        out[2 + outOffset] = (byte) (crc >>> 8);
        out[3 + outOffset] = (byte) crc;

        Log.i("CRC32(", Convert.Buffer2Hex(in, inOffset, Len) + ") (" + Convert.Buffer2Hex(out, outOffset, 4) + ")");
    }

    public static byte[] GetOfflineRef() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        byte[] tmpStr = new byte[64 + 1];
        byte[] tmpBuff = new byte[16];
        byte[] tmpBuffEnc = new byte[16];
        byte[] out = new byte[32 + 1];

        strcat(tmpStr, TranStruct.currentTran.Pan);
        strcat(tmpStr, TranStruct.currentTran.ExpDate);
        strcat(tmpStr, "FFFFFFFFFFFFFFFF");

        Convert.EP_AscHex(tmpBuff, tmpStr, 16);

        Cipher c3des = Cipher.getInstance("DESede/CBC/NoPadding");
        SecretKeySpec myKey = new SecretKeySpec(params.OKK, "DESede");
        IvParameterSpec ivspec = new IvParameterSpec(new byte[]{0, 0, 0, 0, 0, 0, 0, 0});

        c3des.init(Cipher.ENCRYPT_MODE, myKey, ivspec);
        tmpBuffEnc = c3des.doFinal(tmpBuff);

        for (int i = 0; i < 16; i++)
            sprintf(out, i * 2, "%02X", tmpBuffEnc[i]);

        return out;
    }

    public static String GetSerialStr() {

        // TODO:
        //String serial = "BLK" + DeviceInfo.GetDevInfo().dSerial.substring(1, 10);

        //SERIALSTR
        String serial= params.serialStr;

        return serial;
    }

    public static short GetTLVData(byte[] in, int len, int tag, byte[] out, short maxLen) {
        int i = 0, utLen;
        short tLen = 0;

        //Log.i(TAG, String.format("GetTLVData(%d, %d, %d, %d)", in.length, len, tag, maxLen));

        while (true) {
            //Log.i(TAG, Convert.Buffer2Hex(Arrays.copyOfRange(in, i, i + 10)));

            if (Convert.unsignedByteToInt(in[i++]) == tag) {
                tLen = Convert.ToShort(in, i);
                tLen = Convert.SWAP_UINT16(tLen);
                utLen = Convert.unsignedShortToInt(tLen);
                //Log.i(TAG, "i(" + i + ") utLen(" + utLen + ")");

                i += 2;

                if (utLen > maxLen)
                    utLen = maxLen;

                if (out != null)
                    memcpy(out, 0, in, i, utLen);

                break;
            } else {
                tLen = Convert.ToShort(in, i);
                tLen = Convert.SWAP_UINT16(tLen);
                utLen = Convert.unsignedShortToInt(tLen);

                //Log.i(TAG, "i(" + i + ") utLen(" + utLen + ")");

                i += utLen + 2;
                tLen = 0;
            }

            if (i >= len)
                break;
        }

        if (tLen > 0)
            Log.i(TAG, "GetTLVData T(" + tag + ") L(" + tLen + ") V (" + Convert.Buffer2Hex(out, 0, tLen) + ")");
        //EP_printf("GetTLVData(%02X)-%d", tag, tLen);
        //EP_HexDump(out, tLen);

        return tLen;
    }

    public static void FormatAmount(byte[] pDest, byte[] pBuf, byte[] CurDes) {
        byte cnt;
        byte CurDesLen = 3;
        byte[] tmp = new byte[32];
        int pDestIndex = 0, CurDesIndex = 0;

        memset(tmp, (byte) '0', 16);

        if (c.strlen(pBuf) > 16)
            pBuf[16] = 0;

        if (pBuf[0] == '-')
            memcpy(tmp, 16 - (c.strlen(pBuf) - 1), pBuf, 1, c.strlen(pBuf) - 1);
        else
            memcpy(tmp, 16 - c.strlen(pBuf), pBuf, 0, c.strlen(pBuf));

        // CurDes " TL" veya "TL " şeklinde gelirse formatı düzelt, tutarı bir basamak sağa kaydır.
        if ((CurDes[CurDesIndex + 0] == 0x20) || (CurDes[CurDesIndex + 2] == 0x20)) {
            pDest[pDestIndex++] = 0x20;
            CurDesLen--;
            if (CurDes[CurDesIndex + 0] == 0x20)
                CurDesIndex++;
        }

        for (cnt = 0; (((tmp[cnt] == 0x30) || (tmp[cnt] == 0x20)) && (cnt < 16)); cnt++)
            ;    // Accepts zero or space fore-fed strings
        cnt = (byte) (16 - cnt);

        memcpy(pDest, pDestIndex, "                0,00 ".getBytes(), 0, 21);
        memcpy(pDest, pDestIndex + 21, CurDes, CurDesIndex, CurDesLen);
        memcpy(pDest, pDestIndex + 18, tmp, 14, 2);

        if (cnt > 2) {
            memcpy(pDest, pDestIndex + 14, tmp, 11, 3);
            if (cnt < 5)
                pDest[pDestIndex + 14] = 0x20;
            if (cnt < 4)
                pDest[pDestIndex + 15] = 0x20;
        }

        if (cnt > 5) {
            pDest[pDestIndex + 13] = '.';
            memcpy(pDest, pDestIndex + 10, tmp, 8, 3);
            if (cnt < 8)
                pDest[pDestIndex + 10] = 0x20;
            if (cnt < 7)
                pDest[pDestIndex + 11] = 0x20;
        }

        if (cnt > 8) {
            pDest[pDestIndex + 9] = '.';
            memcpy(pDest, pDestIndex + 6, tmp, 5, 3);
            if (cnt < 11)
                pDest[pDestIndex + 6] = 0x20;
            if (cnt < 10)
                pDest[pDestIndex + 7] = 0x20;
        }

        if (cnt > 11) {
            pDest[pDestIndex + 5] = '.';
            memcpy(pDest, pDestIndex + 2, tmp, 2, 3);
            if (cnt < 11)
                pDest[pDestIndex + 2] = 0x20;
            if (cnt < 10)
                pDest[pDestIndex + 3] = 0x20;
        }

        if (cnt > 14) {
            pDest[pDestIndex + 1] = (byte) '.';
            pDest[pDestIndex + 0] = tmp[1];
        }

        string.TrimStart(pDest, pDestIndex, (byte) ' ');
        if (cnt > 2) {
            string.TrimStart(pDest, pDestIndex, (byte) '0');
        }

        if (pBuf[0] == (byte) '-') {
            cnt = (byte) (c.strlen(pDest) - pDestIndex);
            memcpy(pDest, pDestIndex + 1, pDest, pDestIndex + 0, cnt);
            pDest[pDestIndex + 0] = '-';
            pDest[pDestIndex + cnt + 1] = 0;
        }
    }

    public static int returnImage(byte[] acqId) {

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

        switch (hex) {
            case "1":
                return R.drawable.slider_merkez;
            case "111":
                return R.drawable.finansbank;
            case "32":
                return R.drawable.teb;
            case "64":
                return R.drawable.turkiyeis;
            case "10":
                return R.drawable.ziraat;
            case "12":
                return R.drawable.slider_halkbank;
            case "205":
                return R.drawable.kuveyt;
            case "15":
                return R.drawable.vakif;
            case "67":
                return R.drawable.akbank;
            case "46":
                return R.drawable.akbank;
            case "62":
                return R.drawable.slider_garanti;
            default:
                return 0;
        }
    }
}
