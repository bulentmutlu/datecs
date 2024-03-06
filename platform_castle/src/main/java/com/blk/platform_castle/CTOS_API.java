package com.blk.platform_castle;

import java.util.Arrays;

public class CTOS_API {
    static {
        //System.loadLibrary("ctos_api");
    }

    //ISystem Wait Event
public static final int d_EVENT_KBD						= 0x00000001;
public static final int d_EVENT_SC						= 0x00000002;
public static final int d_EVENT_MSR						= 0x00000004;
public static final int d_EVENT_MODEM					= 0x00000008;
public static final int d_EVENT_ETHERNET				= 0x00000010;
public static final int d_EVENT_COM1					= 0x00000020;
public static final int d_EVENT_COM2					= 0x00000040;
public static final int d_EVENT_TOUCH					= 0x00000080;
public static final int d_TIME_INFINITE					= 0xFFFFFFFF;
public static final int d_SYSWAIT_TIMEOUT				= 0x5002;

    public class CTOS_RTC {
        public byte bSecond;
        public byte bMinute;
        public byte bHour;
        public byte bDay;
        public byte bMonth;
        public byte bYear;
        public byte bDoW;
    }

    //sc
    public byte status;

    //msr
    public byte baTk1Buf[] = new byte[128],baTk2Buf[]= new byte[128],baTk3Buf[]= new byte[128],baBuff[]= new byte[1024],pwSrc[]= new byte[1024];
    private byte mBuf[] = new byte[1024],dBuf[]= new byte[1024];
    private int mLen = 0, dLen = 0;
    public byte scTestRtn[]= new byte[1024];
    private byte baTk1Err,baTk2Err,baTk3Err;
    public  int usTk1Len, usTk2Len, usTk3Len;
    private int pulFileSize,pusLen;
    //rtc
    private byte RTC_bSecond, RTC_bMinute, RTC_bHour, RTC_bDay, RTC_bMonth, RTC_bYear, RTC_bDoW;

    public int CTOS_RTCGet(CTOS_RTC pstRTC) {
        int rtn = CTOS_RTCGetTemp(pstRTC.bSecond, pstRTC.bMinute, pstRTC.bHour, pstRTC.bDay, pstRTC.bMonth, pstRTC.bYear, pstRTC.bDoW);
        if (rtn == 0) {
            pstRTC.bSecond = RTC_bSecond;
            pstRTC.bMinute = RTC_bMinute;
            pstRTC.bHour = RTC_bHour;
            pstRTC.bDay = RTC_bDay;
            pstRTC.bMonth = RTC_bMonth;
            pstRTC.bYear = RTC_bYear;
            pstRTC.bDoW = RTC_bDoW;
        }
        return rtn;
    }

    public int CTOS_RTCSet(CTOS_RTC pstRTC) {
        int rtn = CTOS_RTCSetTemp(pstRTC.bSecond, pstRTC.bMinute, pstRTC.bHour, pstRTC.bDay, pstRTC.bMonth, pstRTC.bYear, pstRTC.bDoW);
        return rtn;
    }

    public int CTOS_MSRRead() {
        Arrays.fill(baTk1Buf, (byte)0);
        Arrays.fill(baTk2Buf, (byte)0);
        Arrays.fill(baTk3Buf, (byte)0);
        usTk1Len = usTk2Len = usTk3Len = 0;

        int rtn = CTOS_MSRReadTemp();

//        String sbaTk1Buf = new String(baTk1Buf);
//        String sbaTk2Buf = new String(baTk2Buf);
//        String sbaTk3Buf = new String(baTk3Buf);
//        String temp = "rtn:" + rtn + "\n track1buf:" + sbaTk1Buf + "\n track2buf:" + sbaTk2Buf + "\n track3buf:" + sbaTk3Buf + "\n track1:" + usTk1Len + "\n track2:" + Integer.toString(usTk2Len) + "\n track3:" + Integer.toString(usTk3Len);
        return rtn;
    }

    public String CTOS_MSRGetLastErr() {
        int rtn = CTOS_MSRGetLastErrTemp();
        String temp = String.format("rtn:%d\ntrack1Err:%02x\ntrack2Err:%02X\ntrack3Err:%02X", rtn, baTk1Err, baTk2Err, baTk3Err);
        //String temp = "rtn:" + rtn + "\n track1Err:" + Integer.toString(baTk1Err&0xff) + "\n track2Err:" + Integer.toString(baTk2Err&0xff) + "\n track3Err:" + Integer.toString(baTk3Err&0xff);
        //String temp = new String(baBuff);
        return temp;
    }

    public String CTOS_PowerSource() {
        int rtn = CTOS_PowerSourceTemp();
        String temp = new String(pwSrc);
        return temp;
    }

    public int CTOS_SystemWait(int events, int timeout) {
        return CTOS_SystemWaitTemp(events, timeout);

        //String temp = new String(baBuff);        return temp;
    }

    public String CTOS_FileGetSize() {
        int rtn = CTOS_FileGetSizeTemp();
        String temp = String.format("CTOS_FileGetSize rtn:%d pulFileSize:%d", rtn, pulFileSize);
        return temp;
    }

    public String CTOS_FileRead() {
        int rtn = CTOS_FileReadTemp();
        String buffer = new String(baBuff);
        String temp = String.format("CTOS_FileGetSize rtn:%d\npulFileSize:%d\nfileString:%s", rtn, pulFileSize, buffer);
        return temp;
    }

    public String CTOS_FileDir() {
        int rtn = CTOS_FileDirTemp();
        String buffer = new String(baBuff);
        String temp = String.format("CTOS_FileDir rtn:%d\npulFileSize:%d\npusLen:%d\nfileString:%s", rtn, pulFileSize, pusLen, buffer);
        return temp;
    }

    public String CTOS_GetSystemInfo() {
        int rtn = CTOS_GetSystemInfoTemp();
        String buffer = new String(baBuff);
        String temp = String.format("CTOS_GetSystemInfo rtn:%d\nrootfs:%s", rtn, buffer);
        return temp;
    }

    public String CTOS_RNG() {
        int rtn = CTOS_RNGTemp();
        String buffer = new String(baBuff);
        String temp = String.format("CTOS_RNG rtn:%d\nrngstr:%s", rtn, buffer);
        return temp;
    }

    public String CTOS_DES() {
        int rtn = CTOS_DESTemp();
        String buffer = new String(baBuff);
        String temp = String.format("CTOS_DES rtn:%d\n  enc str:%s", rtn, buffer);
        return temp;
    }

    public String CTOS_DES_CBC() {
        int rtn = CTOS_DES_CBCTemp();
        String buffer = new String(baBuff);
        String temp = String.format("CTOS_DES_CBC rtn:%d\n enc str:%s", rtn, buffer);
        return temp;
    }

    public String CTOS_AES_ECB() {
        int rtn = CTOS_AES_ECBTemp();
        String buffer = new String(baBuff);
        String temp = String.format("CTOS_AES_ECB rtn:%d\n enc str:%s", rtn, buffer);
        return temp;
    }

    public String CTOS_AES_CBC() {
        int rtn = CTOS_AES_CBCTemp();
        String buffer = new String(baBuff);
        String temp = String.format("CTOS_AES_CBC rtn:%d\n enc str:%s", rtn, buffer);
        return temp;
    }

    public String CTOS_MAC() {
        int rtn = CTOS_MACTemp();
        String buffer = new String(baBuff);
        String temp = String.format("CTOS_MAC rtn:%d\n enc str:%s", rtn, buffer);
        return temp;
    }

    public String CTOS_SHA1Final() {
        CTOS_SHA1FinalTemp();
        String buffer = new String(baBuff);
        String temp = String.format("CTOS_SHA1Final OK enc str:%s", buffer);
        return temp;
    }

    public String CTOS_SHA1() {
        CTOS_SHA1Temp();
        String buffer = new String(baBuff);
        String temp = String.format("CTOS_SHA1 OK enc str:%s", buffer);
        return temp;
    }

    public String CTOS_SHA256Final() {
        CTOS_SHA256FinalTemp();
        String buffer = new String(baBuff);
        String temp = String.format("CTOS_SHA256Final OK enc str:%s", buffer);
        return temp;
    }

    public String CTOS_SHA256() {
        CTOS_SHA256Temp();
        String buffer = new String(baBuff);
        String temp = String.format("CTOS_SHA256 OK enc str:%s", buffer);
        return temp;
    }

    public String CTOS_RSA() {
        int rtn = CTOS_RSATemp();
        String buffer = new String(baBuff);
        String temp = String.format("CTOS_RSA rtn:%d\n enc str:%s", rtn, buffer);
        return temp;
    }

    public String CTOS_RSAKeyGenerate() {
        int rtn = CTOS_RSAKeyGenerateTemp();
        String mbuffer = new String(mBuf);
        String dbuffer = new String(dBuf);
        String temp = String.format("\n\n\nCTOS_RSAKeyGenerate rtn:%d\n m len:%d\n m str:%s\n d len:%d\n d str:%s", rtn, mLen, mbuffer, dLen, dbuffer);
        return temp;
    }

    public String CTOS_LanguageInfo() {
        int rtn = CTOS_LanguageInfoTemp();
        String batemp = new String(baBuff);
        String temp = String.format("CTOS_LanguageInfo rtn:%d\n%s", rtn, batemp);
        return temp;
    }

    public String CTOS_LanguageNum() {
        int rtn = CTOS_LanguageNumTemp();
        String batemp = new String(baBuff);
        String temp = String.format("CTOS_LanguageNum rtn:%d\n%s", rtn, batemp);
        return temp;
    }

    public String CTOS_LanguagePrinterGetFontInfo() {
        int rtn = CTOS_LanguagePrinterGetFontInfoTemp();
        String batemp = new String(baBuff);
        String temp = String.format("CTOS_LanguagePrinterGetFontInfo rtn:%d\n%s", rtn, batemp);
        return temp;
    }

    public String CTOS_LanguageLCDGetFontInfo() {
        int rtn = CTOS_LanguageLCDGetFontInfoTemp();
        String batemp = new String(baBuff);
        String temp = String.format("CTOS_LanguageLCDGetFontInfo rtn:%d\n%s", rtn, batemp);
        return temp;
    }

    private native int CTOS_RTCGetTemp(byte bSecond, byte bMinute, byte bHour, byte bDay, byte bMonth, byte bYear, byte bDoW);
    private native int CTOS_RTCSetTemp(byte bSecond, byte bMinute, byte bHour, byte bDay, byte bMonth, byte bYear, byte bDoW);
    public native int CTOS_Delay(int ulMSec);
    public native int CTOS_TickGet();
    public native int CTOS_TimeOutSet(byte bTID, int ulMSec);
    public native int CTOS_TimeOutCheck(byte bTID);
    private native int CTOS_MSRReadTemp();
    private native int CTOS_MSRGetLastErrTemp();
    public native int CTOS_SCStatus(int id);
    public native void CTOS_SCTest(int id);
    public native int CTOS_CLInit();
    public native int CTOS_CLPowerOn();
    public native int CTOS_CLPowerOff();
    public native int CTOS_FelicaPolling();
    public native int CTOS_CLTypeAActiveFromIdle();
    public native int TypeA_Cmd();
    public native int CTOS_CLRATS();
    public native int CTOS_REQB();
    public native int CTOS_WUPB();
    public native int CTOS_ATTRIB();
    public native int CTOS_HALTB();
    public native int CTOS_CLTypeBActive();
    public native int CTOS_CLTypeBActiveEx();
    public native int CTOS_MifareLOADKEY();
    public native int CTOS_MifareAUTHEx();
    public native int CTOS_MifareWRITEBLOCK1();
    public native int CTOS_MifareWRITEBLOCK2();
    public native int CTOS_MifareREADBLOCK1();
    public native int CTOS_MifareREADBLOCK2();
    private native int CTOS_PowerSourceTemp();
    public native int CTOS_PowerModeSleep();
    public native int CTOS_PowerModeStandby();
    public native int CTOS_PowerModeReboot();
    public native int CTOS_PowerModePWOff();
    private native int CTOS_SystemWaitTemp(int events, int timeout);
    public native int CTOS_PowerAwakening();
    public native void CTOS_SystemReset();
    public native int CTOS_PrinterLogo();
    public native int CTOS_PrinterPutString();
    public native int CTOS_PrinterFline();
    public native int CTOS_PrinterSetHeatLevel(int level);
    public native int CTOS_PrinterPutStringAligned();
    public native int CTOS_PrinterStatus();
    public native void CTOS_PrinterBufferEnable();
    public native int CTOS_PrinterBufferInit();
    public native int CTOS_PrinterBufferLogo();
    public native int CTOS_PrinterBufferPutString();
    public native int CTOS_PrinterBufferOutput();
    public native int CTOS_PrinterBufferFill();
    public native int CTOS_PrinterBufferHLine();
    public native int CTOS_PrinterBufferVLine();
    public native int CTOS_PrinterBufferPixel();
    public native int CTOS_PrinterBufferPutStringAligned();
    public native int CTOS_PrinterBufferSelectActiveAddress();
    public native int CTOS_PrinterFontSelectMode();
    public native int CTOS_PrinterBufferBMPPic();
    public native int CTOS_PrinterBMPPic();
    public native int CTOS_PrinterTTFSelect();
    public native int CTOS_PrinterTTFSwitchDisplayMode();
    public native int CTOS_PrinterSetDefaultASCIIStyle();
    public native int CTOS_FileOpen();
    public native int CTOS_FileOpenAttrib();
    public native int CTOS_FileClose();
    public native int CTOS_FileDelete();
    private native int CTOS_FileGetSizeTemp();
    public native int CTOS_FileWrite();
    public native int CTOS_FileSeek();
    private native int CTOS_FileReadTemp();
    private native int CTOS_FileDirTemp();
    public native int CTOS_Beep();
    public native int CTOS_Sound(int usFreq, int usDuration);
    private native int CTOS_GetSystemInfoTemp();
    private native int CTOS_RNGTemp();
    private native int CTOS_DESTemp();
    private native int CTOS_DES_CBCTemp();
    private native int CTOS_AES_ECBTemp();
    private native int CTOS_AES_CBCTemp();
    private native int CTOS_MACTemp();
    public native void CTOS_SHA1Init();
    public native void CTOS_SHA1Update();
    private native void CTOS_SHA1FinalTemp();
    private native void CTOS_SHA1Temp();
    public native void CTOS_SHA256Init();
    public native void CTOS_SHA256Update();
    private native void CTOS_SHA256FinalTemp();
    private native void CTOS_SHA256Temp();
    private native int CTOS_RSATemp();
    private native int CTOS_RSAKeyGenerateTemp();
    public native int CTOS_KMS2Init();
    public native int CTOS_KMS2KeyCheck();
    public native int CTOS_KMS2KeyDelete();
    public native int CTOS_KMS2KeyCheckAll();
    public native int CTOS_KMS2KeyDeleteAll();
    public native int CTOS_KMS2KeyWrite();
    public native int CTOS_KMS2KeyWriteByTR31();
    public native int CTOS_KMS2MAC();
    public native int CTOS_KMS2DataEncrypt();
    public native int CTOS_LanguageConfig();
    public native int CTOS_LanguageLCDFontSize();
    public native int CTOS_LanguagePrinterFontSize();
    private native int CTOS_LanguageInfoTemp();
    private native int CTOS_LanguageNumTemp();
    public native int CTOS_LanguagePrinterSelectASCII();
    public native int CTOS_LanguageLCDSelectASCII();
    private native int CTOS_LanguagePrinterGetFontInfoTemp();
    private native int CTOS_LanguageLCDGetFontInfoTemp();
    public native int CTOS_LCDTClearDisplay();
    public native int CTOS_LCDTGotoXY();
    public native int CTOS_LCDTWhereX();
    public native int CTOS_LCDTWhereY();
    public native int CTOS_LCDTPrint();
    public native int CTOS_LCDTPrintXY();
    public native int CTOS_LCDSelectMode();
    public native byte[] CTOS_GETGBBuffer();
}
