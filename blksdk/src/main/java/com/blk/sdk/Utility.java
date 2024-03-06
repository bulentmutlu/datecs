package com.blk.sdk;

import static com.blk.sdk.c.memcpy;
import static com.blk.sdk.c.memset;
import static com.blk.sdk.c.strlen;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.util.Log;

import com.blk.sdk.printer.PrinterManager;
import com.blk.sdk.printer.connectivity.AbstractConnector;
import com.blk.sdk.printer.connectivity.PrinterConnector;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by id on 9.03.2018.
 */

public class Utility extends ContextWrapper {
    static final String TAG = Utility.class.getSimpleName();
    static final String envTableName = "Environment";
    public static String filesPath;
    public static String cachePath;
    public static String dbPath;
    public static String packagePath;
    public static String sharedPath;


    public static Context appContext;
    private static SharedPreferences sharedPrefs;
    public static String appName;

    public Utility(Context base) {
        super(base);
    }

    public static String LibVersion()
    {
        return BuildConfig.LIB_VERSION;
    }

    static {
        //ISystem.loadLibrary("crypto");
        try {
            System.loadLibrary("sdklib");
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public static void Init(String appName) throws Exception {
        Context context = UI.UiUtil.getApplicationContext();
        Utility.appName = appName;
        filesPath = context.getFilesDir().getPath(); // "/data/data/com.blk.techpos/files"
        cachePath = context.getCacheDir().getPath();
        sharedPath = android.os.Environment.getExternalStorageDirectory().getPath();
        sharedPrefs = context.getSharedPreferences(appName, 0);

        if (GetSharedPrefInt("storage") == 1) {
            filesPath = sharedPath + "/" + appName;;
        }

        dbPath = filesPath + "/" + appName + ".db";
        Log.i(TAG, "dbpath : " + dbPath);

        File theDir = new File(filesPath);
        if (!theDir.exists() && !theDir.mkdirs()){
            throw new Exception("can not create dir : " + filesPath);
        }

        if (!file.Exist(dbPath) || !IsTableExist(dbPath, envTableName)) {
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbPath, null);
            db.execSQL("CREATE TABLE " + envTableName + " (Name TEXT PRIMARY KEY, Value TEXT)");
            db.close();
        }


        DeviceInfo.Init();
        //CardReader.Init(DeviceInfo.idal);
        //MainPrinter.Init();
        AbstractConnector connector = new PrinterConnector(context);
        initPrinter(connector);




        }

private static void initPrinter(AbstractConnector item){
        log("initPrinter");
    final Thread thread = new Thread(() -> {
        try {
            try {
                item.connect();
            } catch (Exception e) {
                return;
            }

            try {
                PrinterManager.instance.init(item);
            } catch (Exception e) {
                try {
                    item.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                return;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    });
    thread.start();
}

    public static void Destroy() {
        MainPrinter.Destroy();
    }

    public static boolean IsTableExist(String dbPath, String table)
    {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
        boolean rv = IsTableExist(db, table);
        db.close();
        return rv;
    }
    public static boolean IsTableExist(SQLiteDatabase db, String table) {
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + table + "';", null);
        String name = "";

        while (cursor.moveToNext()) {
            name = cursor.getString(0);
        }
        return name.length() > 0;
    }

    public static void log(String fmt, Object... args)
    {
        Log.i("", String.format(fmt, args));
    }
    public static void logDump(byte[] b, int len)
    {
        logDump("", b, len);
    }
    public static void logDump(String header, byte[] b, int len)
    {
        Log.i("", header + " : " + Convert.Buffer2Hex(b, 0, len));
    }

    public static String GetEnvironmentVariable(String key)
    {
        String val = "";
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
        Cursor cursor = db.rawQuery("SELECT Value FROM " + envTableName + " WHERE Name = '" + key + "'", null);

        while (cursor.moveToNext()) {
            val = cursor.getString(0);
        }
        db.close();
        Log.i(TAG, "EP_GetEnv " + key + " = " + val);
        return val;
    }
    public static int GetEnvironmentVariableInt(String key)
    {
        try {
            return Integer.parseInt(GetEnvironmentVariable(key));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static void SetEnvironmentVariable(String key, String val)
    {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        db.execSQL("INSERT OR REPLACE INTO " + envTableName + " Values('" + key + "', '" + val + "')");
        db.close();
    }
    public static void SetEnvironmentVariableInt(String key, int val)
    {
        SetEnvironmentVariable(key, Integer.toString(val));
    }
    public static boolean IsNullorEmpty(byte[] buf)
    {
        if (buf == null) return true;

        for (int i = 0; i < buf.length; ++i) {
            if (buf[i] != 0x00)
                return false;
        }
        return true;
    }
    public static boolean IsNullorEmpty(String s)
    {
        return s == null || s.length() == 0;
    }
    public static void sleep(int ms) {
        SystemClock.sleep(ms);
//        try {
//            Thread.sleep(ms);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }
    public static void FormatAmount(byte[] pDest, byte[] pBuf2, byte[] CurDes)
    {
        int cnt;
        int CurDesLen = strlen(CurDes); // 3
        byte[] tmp = new byte[32];
        byte[] pBuf = Arrays.copyOf(pBuf2, pBuf2.length);


        memcpy(pBuf, pBuf2, pBuf.length);
        memset(tmp, (byte) '0', 16);

        if (strlen(pBuf) > 16)
            pBuf[16] = 0;

        if (pBuf[0] == '-')
            memcpy(tmp, 16 - (strlen(pBuf) - 1), pBuf, 1, strlen(pBuf) - 1);
        else
            memcpy(tmp, 16 - strlen(pBuf), pBuf, 0, strlen(pBuf));

        // CurDes " TL" veya "TL " şeklinde gelirse formatı düzelt, tutarı bir basamak sağa kaydır.
        if ((CurDes[0] == 0x20) || (CurDes.length > 2 && CurDes[2] == 0x20)) {
            throw new InvalidParameterException(c.ToString(CurDes));
//            *pDest++ = 0x20;
//            CurDesLen--;
//            if (CurDes[0] == 0x20)
//                CurDes++;
        }

        for (cnt = 0; (((tmp[cnt] == 0x30) || (tmp[cnt] == 0x20)) && (cnt < 16)); cnt++)
            ;    // Accepts zero or space fore-fed strings
        cnt = 16 - cnt;

        memset(pDest, (byte) 0, pDest.length);
        memcpy(pDest, "                0,00 ".getBytes(), 21);
        memcpy(pDest, 21, CurDes, 0, CurDesLen);
        memcpy(pDest, 18, tmp, 14, 2);

        if (cnt > 2) {
            memcpy(pDest, 14, tmp, 11, 3);
            if (cnt < 5)
                pDest[14] = 0x20;
            if (cnt < 4)
                pDest[15] = 0x20;
        }

        if (cnt > 5) {
            pDest[13] = '.';
            memcpy(pDest, 10, tmp, 8, 3);
            if (cnt < 8)
                pDest[10] = 0x20;
            if (cnt < 7)
                pDest[11] = 0x20;
        }

        if (cnt > 8) {
            pDest[9] = '.';
            memcpy(pDest, 6, tmp, 5, 3);
            if (cnt < 11)
                pDest[6] = 0x20;
            if (cnt < 10)
                pDest[7] = 0x20;
        }

        if (cnt > 11) {
            pDest[5] = '.';
            memcpy(pDest, 2, tmp, 2, 3);
            if (cnt < 11)
                pDest[2] = 0x20;
            if (cnt < 10)
                pDest[3] = 0x20;
        }

        if (cnt > 14) {
            pDest[1] = '.';
            pDest[0] = tmp[1];
        }

        string.TrimStart(pDest, (byte) ' ');
        if (cnt > 2) {
            string.TrimStart(pDest, (byte) '0');
        }

        if (pBuf[0] == '-') {
            cnt = strlen(pDest);
            memcpy(pDest, 1, pDest, 0, cnt);
            pDest[0] = '-';
            pDest[cnt + 1] = 0;
        }
    }
    public static Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();

        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(assetManager.open(filePath));
        } catch (IOException e) {
            // handle exception
        }
        return bitmap;
    }
    public static void CloseApp() {
//        UI.activity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                UI.activity.finish();
//            }
//        });
//        ISystem.exit(0);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public static void SaveSharedPref(String key, int value) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt(key, value); //int değer ekleniyor
        editor.commit(); //Kayıt
    }
    public static int GetSharedPrefInt(String key) {
        return sharedPrefs.getInt(key, 0);
    }

    //Lhun algorithm
    public static boolean isCreditCardNumberValid(String value) {

        List<Integer> kartNumarasi = new ArrayList<>();
        List<Integer> ciftKartNumaralari = new ArrayList<Integer>();

        int toplam1 = 0, toplam2 = 0;

        // TextBox’ a girilen string formattaki kart numarasina ait sayı dizisinin her bir elemanı List tipinde int’ değerler tutan generic kartNumarasi isimli koleksiyona aktarılır.
        for (int i = 0; i < value.length(); i++) {
            kartNumarasi.add(Integer.parseInt(String.valueOf(value.charAt(i))));
        }

        // ilk olarak iki katı hesaplaması ve çıkan sayıların toplamı işlemi yapılır.
        for (int i = 0; i < kartNumarasi.size(); i = i + 2) {
            ciftKartNumaralari.add(kartNumarasi.get(i) * 2);
        }

        for (int i = 0; i < ciftKartNumaralari.size(); i++) {
            if (ciftKartNumaralari.get(i) > 9) {
                String var = String.valueOf(ciftKartNumaralari.get(i));
                toplam1 += Integer.parseInt(String.valueOf(var.charAt(0))) + Integer.parseInt(String.valueOf(var.charAt(1)));
            } else {
                toplam1 += ciftKartNumaralari.get(i);
            }
        }

        // iki katı hesabı dışında kalan elemanların toplamı hesaplanır.
        for (int i = 1; i < kartNumarasi.size(); i += 2) {
            toplam2 += kartNumarasi.get(i);
        }

        // Genel toplam alınır ve 10 ile tam bölünüp bölünmediğine bakılır.
        int toplam = toplam1 + toplam2;
        if (toplam % 10 == 0) {
            return true; // hata mesajı döndürülmez. Validation işlemi geçerlidir.
        } else {
            return false;
        }
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

    public static Bitmap QRencodeAsBitmap(String contents, int img_width, int img_height) throws Exception {
        String contentsToEncode = contents;

        Log.i("QR", contents);
        Log.i("QR", Convert.byteArray2HexString(contents.getBytes(StandardCharsets.UTF_8)));
        if (contents == null)
            return null;
//            Map<EncodeHintType, Object> hints = null;
//            String encoding = guessAppropriateEncoding(contents);
//            if (encoding != null) {
//                hints = new EnumMap(EncodeHintType.class);
//                hints.put(EncodeHintType.CHARACTER_SET, encoding);
//            }
//
//            MultiFormatWriter writer = new MultiFormatWriter();
        com.google.zxing.qrcode.QRCodeWriter writer = new QRCodeWriter();
        BitMatrix result;
        try {
            result = writer.encode(contentsToEncode, BarcodeFormat.QR_CODE, img_width, img_height, null);
        } catch (IllegalArgumentException var16) {
            return null;
        }

        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];

        for(int y = 0; y < height; ++y) {
            int offset = y * width;

            for(int x = 0; x < width; ++x) {
                pixels[offset + x] = result.get(x, y) ? -16777216 : -1;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    public static String GetAppVersion(boolean withDot) {
        try {
            Context c = UI.UiUtil.getApplicationContext();
            PackageInfo pInfo = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            return withDot ? pInfo.versionName : ("" + pInfo.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "0000";
    }
}
