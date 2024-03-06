package com.blk.sdk;

import com.blk.sdk.olib.olib;

public class Security {
    public static class KeyType{
        public static final int EP_SEC_KEY_NULL		= 0;
        public static final int EP_SEC_KEY_DES		= 1;
        public static final int EP_SEC_KEY_AES		= 2;
        public static final int EP_SEC_KEY_ALL		= 99;
    }
    public static class KeyData {
        public int type;
        public int id;
        public int len;
        public byte[] key = new byte[128];
    }
    static String keyFileName = "abc__xyz__";


    static void SecCryptoWrite(file hdl, KeyData keyData) throws Exception {
        int i = 0;
        byte[] magicByte = new byte[] {(byte) 0xEC};

        if(SecGetMagicByte(true, magicByte))
        {
            if(hdl != null && keyData != null && (keyData.len > 0))
            {
                for(i = 0; i < keyData.key.length; i++)
                    keyData.key[i] ^= magicByte[0];

                olib.WriteFile(keyData, hdl);
            }
        }
    }

    public static boolean SecCryptoRead(file hdl, KeyData key) {
        int i = 0;
        byte[] magicByte = new byte[] {(byte) 0xEC};

        if(SecGetMagicByte(false, magicByte))
        {
            if(hdl != null && key != null)
            {
                try {
                    olib.ReadFile(key, hdl);
                } catch (Exception e) {
                    return false;
                }
                for(i = 0; i < key.key.length; i++)
                    key.key[i] ^= magicByte[0];
                return true;
            }
        }
        return false;
    }

    static int SecGetKey(int keyType, int keyId, KeyData key) throws Exception {
        int rv = -1, i = 0, rr = 0;
        KeyData kdata = new KeyData();

        file fd = new file(keyFileName, file.OpenMode.RDWR);
        while(true)
        {
            if (!SecCryptoRead(fd, kdata))
                break;

            if(kdata.type == keyType && kdata.id == keyId)
            {
                if(key != null) olib.Copy(key, kdata);
                rv = i;
                break;
            }
            i++;
        }
        fd.Close();

        return rv;
    }

    static byte magicVal = 0;
    static boolean magicValValid = false;
    static boolean SecGetMagicByte(boolean injectKeys, byte[] val)
    {
        // fixme
        return false;
//        if(magicValValid)
//        {
//            val[0] = magicVal;
//            return true;
//        }
//
//        try {
//            byte[] tmpBuff = null;
//            byte[] mask = new byte[]{0x12, 0x34, 0x56, 0x78, (byte) 0x90, 0x12, 0x34, 0x56};
//            IPed ped = null;//DeviceInfo.idal.getPed(EPedType.INTERNAL);
//
//            try {
//                tmpBuff = ped.calcDes((byte) 2, mask, EPedDesMode.ENCRYPT);
//            } catch (PedDevException e1) {
//
//                if(injectKeys)
//                {
//                    byte[] dstKeyValue = new byte[16];
//                    Utility.OsGetRandom(dstKeyValue,16);
//
//                    try {
//                        //memcpy(tmpBuff, "\x03\x01\x01\x02\x00\x00\x00\x00\x00\x00\x00\x05\x10", 13);
//                        ped.writeKey(EPedKeyType.TLK, (byte) 1, EPedKeyType.TDK, (byte) 2, dstKeyValue, ECheckMode.KCV_NONE, null);
//                        Utility.EP_printf("Des Key Injection OsPedWriteKey");
//                    } catch (PedDevException e2) {
//
//                        //memcpy(tmpBuff, "\x03\x01\x00\x01\x00\x00\x00\x00\x00\x00\x00\x01\x10", 13);
//                        Utility.OsGetRandom(dstKeyValue,16);
//                        ped.writeKey(EPedKeyType.TLK, (byte) 0, EPedKeyType.TLK, (byte) 1, dstKeyValue, ECheckMode.KCV_NONE, null);
//                        Utility.EP_printf("TLK Injection OsPedWriteKey");
//
//                        //memcpy(tmpBuff, "\x03\x01\x01\x02\x00\x00\x00\x00\x00\x00\x00\x05\x10", 13);
//                        Utility.OsGetRandom(dstKeyValue,16);
//                        ped.writeKey(EPedKeyType.TLK, (byte) 1, EPedKeyType.TDK, (byte) 2, dstKeyValue, ECheckMode.KCV_NONE, null);
//                        Utility.EP_printf("Des Key Injection OsPedWriteKey");
//                    }
//
//                    tmpBuff = ped.calcDes((byte) 2, mask, EPedDesMode.ENCRYPT);
//                }
//            }
//
//            if (tmpBuff != null) {
//                Utility.EP_HexDump(tmpBuff, 8);
//                magicVal = 0;
//                for(int i = 0; i < 8; i++)
//                {
//                    magicVal ^= tmpBuff[i];
//                }
//                val[0] = magicVal;
//                magicValValid = true;
//                return true;
//            }
//
//        } catch (PedDevException e) {
//            e.printStackTrace();
//        }
//        return false;
    }

    public static boolean SecEraseKey(int keyType, int keyId) throws Exception {
        int idx = 0;
        KeyData kdata = new KeyData();

        if(keyType == KeyType.EP_SEC_KEY_ALL)
        {
            return file.Remove(keyFileName);
        }
        else
        {
            idx = SecGetKey(keyType, keyId, null);
            if(idx >= 0)
            {
                file fd = new file(keyFileName, file.OpenMode.RDWR);
                fd.Seek(idx* olib.Size(kdata), file.SeekMode.SEEK_SET);
                SecCryptoWrite(fd, kdata);
                fd.Close();
            }
        }

        return true;
    }

    public static int SecStoreKey(int keyType, int keyId, byte[] key, int len) throws Exception {
        file fd;
        int idx = 0;
        KeyData kdata = new KeyData(),kdataTmp = new KeyData();

        if(key == null) return -1;

        kdata.type = keyType;
        kdata.id = keyId;
        kdata.len = len;
        c.memcpy(kdata.key,key,len);

        idx = SecGetKey(keyType, keyId, null);
        if(idx >= 0)
        {
            fd = new file(keyFileName, file.OpenMode.RDWR);
            fd.Seek(idx* olib.Size(kdata), file.SeekMode.SEEK_SET);
            SecCryptoWrite(fd, kdata);
            fd.Close();
            return 0;
        }
        else
        {
            if(!file.Exist(keyFileName))
            {
                fd = new file(keyFileName, file.OpenMode.RDWR);
                fd.Seek(0, file.SeekMode.SEEK_END);
                SecCryptoWrite(fd, kdata);
                fd.Close();

                //chmod(keyFileName, 0666);
                return 0;
            }
            else
            {
                fd = new file(keyFileName, file.OpenMode.RDWR);
                fd.Seek(0, file.SeekMode.SEEK_SET);

                idx = -1;
                while(SecCryptoRead(fd, kdataTmp))
                {
                    if(kdataTmp.type == KeyType.EP_SEC_KEY_NULL)
                    {
                        fd.Seek(0 - olib.Size(kdata), file.SeekMode.SEEK_CUR);
                        SecCryptoWrite(fd, kdata);
                        idx = 0;
                        break;
                    }
                }

                if(idx != 0)
                {
                    SecCryptoWrite(fd, kdata);
                }

                fd.Close();
                return 0;
            }
        }
        //return -2;
    }
}
