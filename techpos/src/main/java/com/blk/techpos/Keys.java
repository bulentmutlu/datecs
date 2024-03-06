package com.blk.techpos;

public class Keys { }
//    private static IComm uartComm;
//    private static Keys commTester;
//
//    public static void LoadKeys() throws Exception {
//        byte[] rBuff = new byte[64];
//        byte[] recvBuff = new byte[161];
//        int fd = 0;
//
//        UI.ShowMessage("KEY AKTARIM\nLÜTFEN BEKLEYİNİZ", 0);
//
//        IPrinter.lineWriter.EP_PrntWrtStr("LoadKeys");
//
//        UartReceive();
//        if (uartComm == null) {
//            IPrinter.lineWriter.EP_PrntWrtStr("uartComm is null");
//            return;
//        }
//
//        {
//            int idx = 0;
//            int j = 0;
//            int tryCount = 0;
//            while(true)
//            {
//                byte[] buf = recv(160);
//                if (buf == null )
//                {
//                    IPrinter.lineWriter.EP_PrntWrtStr("recv null");
//                    return;
//                }
//                idx = buf.length;
//                j += idx;
//                tryCount++;
//
//                Utility.EP_Wait(10);
//                //EP_HexDump(PAXDUMPMSG, rBuff, j);
//                if(idx>0)
//                    UI.ShowMessage("KEYLER ALINIYOR", 0);
//                if(j>=160 || tryCount>20)
//                    break;
//            }
//        }
//
////        byte[] recvdec16 = new byte[16];
////        byte[] recvdec128 = new byte[128];
////        byte[] keyVal = new byte[] {0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88, (byte) 0x99, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF, 0x00};
////        int i = 0, k = 0, j = 16;
////
////        Utils.OsDES(recvBuff, recvdec16, keyVal, 16, 0);
////        Utils.OsDES(recvBuff, 8, recvdec16, 8, keyVal, 16, 0);
////
////        //EP_printf("decrypted 16-----");
////        //EP_HexDump(recvdec16, sizeof(recvdec16));
////
////
////        UI.ShowMessage(0, "LÜTFEN BEKLEYİNİZ");
////        for(i=0; i<16; i++)
////        {
////            Utils.OsDES(recvBuff, j, recvdec128, k, keyVal, 16, 0);
////            j += 8;
////            k += 8;
////            //EP_printf("recvBuff----- %d", k);
////            //EP_HexDump(recvdec128, k);
////        }
//
//        //int recvLen = testIMMK.length + testHPK.length;
//        //Utility.EP_printf("return ----- %d", recvLen);
//        //EP_HexDump(recvBuff, strlen(recvBuff));
//
////        if(recvLen>=159)
////            UI.ShowMessage(0, "KEYLER YÜKLENDİ\nKAYDEDİLİYOR");
////        else
////        {
////            UI.ShowMessage(2000, "HATA OLUŞTU\nTEKRAR DENEYİNİZ");
////            return;
////        }
//
////
////        UartReceive();
////        connect();
//
////        Write(testIMMK, testHPK);
////
////        Utility.EP_printf("Finished");
////        Utility.EP_SetEnv("KEYFLG", "1");
////        UI.ShowMessage(2000, "KEYLER KAYDEDİLDİ\nUYGULAMAYI YENİDEN BAŞLATINIZ");
////        Utility.CloseApp();
//
//    }
//
//    public static void CheckKeys() {
//        UI.ShowMessage("KEY KONTROL EDİLİYOR\nLÜTFEN BEKLEYİNİZ", 0);
//
//        try {
//            Security.KeyData kdata = new Security.KeyData();
//            SecGetKey(Security.KeyType.EP_SEC_KEY_DES, 998, kdata);
//            if (kdata.key[0] != 0 && kdata.key[15] != 0)
//                UI.ShowMessage("1. KEY YÜKLENMİŞ", 2000);
//            else
//                UI.ShowMessage("1. KEY HATALI", 2000);
//
//            Utility.EP_HexDump("key16  : ", kdata.key, kdata.len);
//
//            Security.KeyData kdata2 = new Security.KeyData();
//            SecGetKey(Security.KeyType.EP_SEC_KEY_DES, 999, kdata2);
//
//            if (kdata2.key[0] != 0 && kdata2.key[123] != 0)
//                UI.ShowMessage("2. KEY YÜKLENMİŞ", 2000);
//            else
//                UI.ShowMessage("2. KEY HATALI", 2000);
//
//            Utility.EP_HexDump("key128 : ", kdata2.key, kdata2.len);
//
//        } catch (Exception e) {
//            UI.ShowMessage("2. KEY HATALI", 2000);
//        }
//    }
//
//    public static void Read() throws Exception {
//        Security.KeyData kdata = new Security.KeyData();
//        if (SecGetKey(Security.KeyType.EP_SEC_KEY_DES, 998, kdata) >= 0) {
//            memcpy(params.IMMK, kdata.key, 16);
//            //EP_printf("key 1 len: %d", strlen(kdata.key));
//            //EP_printf("decrypted 16 read-----");
//            Utility.EP_HexDump("key 1", kdata.key, 16);
//        }
//
//        Security.KeyData kdata2 = new Security.KeyData();
//        if (SecGetKey(Security.KeyType.EP_SEC_KEY_DES, 999, kdata2) >= 0) {
//            memcpy(params.HPK, kdata2.key, 128);
//            //EP_printf("decrypted 128 read-----");
//            Utility.EP_HexDump("key 2", kdata2.key, 128);
//        }
//
//        if (kdata.key[0] != 0 && kdata.key[15] != 0 && kdata2.key[0] != 0 && kdata2.key[123] != 0)
//            Utility.EP_SetEnv("KEYFLG", "1");
//    }
//
//    public static void Write(byte[] recvdec16, byte[] recvdec128) throws Exception {
//        Utility.EP_HexDump("key16  : ", recvdec16, 16);
//        Utility.EP_HexDump("key128 : ", recvdec128, 128);
//
//        Security.EP_SecEraseKey(Security.KeyType.EP_SEC_KEY_DES, 998);
//        Security.EP_SecEraseKey(Security.KeyType.EP_SEC_KEY_DES, 999);
//
//        Security.EP_SecStoreKey(Security.KeyType.EP_SEC_KEY_DES, 998, recvdec16, 16);
//        Security.EP_SecStoreKey(Security.KeyType.EP_SEC_KEY_DES, 999, recvdec128, 128);
//
//        Security.KeyData kdata = new Security.KeyData();
//        SecGetKey(Security.KeyType.EP_SEC_KEY_DES, 998, kdata);
//        //EP_HexDump(kdata.key, 16);
//
//        Security.KeyData kdata2 = new Security.KeyData();
//        SecGetKey(Security.KeyType.EP_SEC_KEY_DES, 999, kdata2);
//        //EP_HexDump(kdata2.key, 128);
//    }
//
//    static int SecGetKey(int keyType, int keyId, Security.KeyData key) throws Exception {
//        int rv = -1, i = 0, rr = 0;
//        String keyFileName = "abc__xyz__";
//        Security.KeyData kdata = new Security.KeyData();
//
//        file fd = new file(keyFileName, file.OpenMode.EP_RDWR);
//        i = 0;
//        while (true) {
//            if (!Security.EP_SecCryptoRead(fd, kdata))
//                break;
//
//            if (kdata.type == keyType && kdata.id == keyId) {
//                if (key != null) olib.Copy(key, kdata);
//                rv = i;
//                break;
//            }
//            i++;
//        }
//        return rv;
//    }
//
//    public static void DeleteKeys() throws Exception {
//        Security.EP_SecEraseKey(Security.KeyType.EP_SEC_KEY_DES, 998);
//        Security.EP_SecEraseKey(Security.KeyType.EP_SEC_KEY_DES, 999);
//        UI.ShowMessage("KEYLER SİLİNDİ", 1000);
//        Utility.EP_printf("Finished");
//        Utility.EP_SetEnv("KEYFLG", "0");
//    }
//
//    ///////////////////////
//
//    public static void UartReceive() {
//
//        String model = Build.MODEL.toString();
//        UartParam uartParam = new UartParam();
//        uartParam.setPort(model.equals("A920") ? (EUartPort.USBDEV) : (EUartPort.COM1));
//
//        uartParam.setAttr("19200,8,n,1");
//        // fixme
//        uartComm = null;//DeviceInfo.idal.getCommManager().getUartComm(uartParam);
//
//        Log.i("getUartComm", uartParam.getPort().toString());
//    }
//
//    public static void connect() {
//        try {
//            if (uartComm.getConnectStatus() == IComm.EConnectStatus.DISCONNECTED) {
//                uartComm.connect();
//                IPrinter.lineWriter.EP_PrntWrtStr("Connected");
//            } else {
//                IPrinter.lineWriter.EP_PrntWrtStr("connect have connected");
//            }
//        } catch (Exception e) {
//            IPrinter.lineWriter.EP_PrntWrtStr("ConnectErr : " + e.getMessage());
//        }
//    }
//
//    public void send(byte[] data) {
//        try {
//            connect();
//            if (uartComm.getConnectStatus() == IComm.EConnectStatus.CONNECTED) {
//                uartComm.send(data);
//                Log.i("Send","send");
//            }
//        }catch (Exception e){
//            Log.i("Send",e.getMessage());
//            e.printStackTrace();
//        }
//    }
//    public static byte[] recv(int len) {
//        try {
//            connect();
//            if (uartComm.getConnectStatus() == IComm.EConnectStatus.CONNECTED) {
//                byte[] result = uartComm.recv(len);
//                IPrinter.lineWriter.EP_PrntWrtStr("recv : " + result.length);
//                return result;
//            } else {
//                IPrinter.lineWriter.EP_PrntWrtStr("please connect first");
//                return null;
//            }
//        } catch (Exception e) {
//            IPrinter.lineWriter.EP_PrntWrtStr("Recv " + e.getMessage());
//            return null;
//        }
//
//    }
//
//    public byte[] recvNonBlocking() {
//        try {
//            connect();
//            if (uartComm.getConnectStatus() == IComm.EConnectStatus.CONNECTED) {
//                byte[] result = uartComm.recvNonBlocking();
//                Log.i("recvNonBlocking","recvNonBlocking");
//                return result;
//            } else {
//                Log.i("recvNonBlocking", "please connect first");
//                return null;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.i("recvNonBlocking", e.getMessage());
//            return null;
//        }
//    }
//
//    public void disConnect() {
//        try {
//            if (uartComm.getConnectStatus() == IComm.EConnectStatus.CONNECTED)
//                uartComm.disconnect();
//            Log.i("DisConnect","DisConnect");
//            commTester = null;
//            uartComm = null;
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.i("DisConnect", e.getMessage());
//        }
//
//    }
//
//}
//
