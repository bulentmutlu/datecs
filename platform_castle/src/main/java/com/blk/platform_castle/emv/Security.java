package com.blk.platform_castle.emv;

import static com.blk.sdk.c.memcpy;

import CTOS.CtEMV;
import CTOS.CtKMS2CKBB;
import CTOS.CtKMS2Dukpt;
import CTOS.CtKMS2Exception;
import CTOS.CtKMS2System;
import CTOS.emv.EMVSecureDataInfo;
import CTOS.emvcl.EMVCLSecureDataInfo;

public class Security {
    static {
//        CtKMS2System system = new CtKMS2System();
//        try {
//            system.init();
//            system.setPINScrambling(false);
//            system.setPINPADMoving(false);
//        } catch (CtKMS2Exception e) {
//            e.printStackTrace();
//        }
    }
    public static void writekey()  {
        int KeySet = 0xC602;
        int KeyIndex = 0x0001;
        int KeySetLocation = 0xC602;
        int KeyIndexLocation = 0x0001;
        byte[] baCKBBKeyBlock = {0x30, 0x50, 0x30, 0x54, 0x45, 0x30, 0x31, 0x4E,
                0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30,
                0x44, 0x32, 0x41, 0x45, 0x46, 0x43, 0x44, 0x37,
                0x46, 0x46, 0x39, 0x33, 0x41, 0x41, 0x41, 0x36,
                0x41, 0x35, 0x43, 0x33, 0x38, 0x41, 0x37, 0x43,
                0x46, 0x44, 0x32, 0x38, 0x36, 0x42, 0x35, 0x30,
                0x37, 0x42, 0x46, 0x31, 0x42, 0x46, 0x43, 0x42,
                0x39, 0x37, 0x37, 0x38, 0x37, 0x37, 0x46, 0x38,
                0x33, 0x38, 0x45, 0x44, 0x31, 0x33, 0x46, 0x39,
                0x31, 0x46, 0x43, 0x45, 0x37, 0x37, 0x37, 0x31,
                0x46, 0x37, 0x37, 0x36, 0x37, 0x45, 0x30, 0x38,
                0x44, 0x46, 0x37, 0x41, 0x41, 0x39, 0x46, 0x46,
                0x45, 0x36, 0x37, 0x36, 0x41, 0x30, 0x37, 0x36,
                0x42, 0x32, 0x45, 0x31, 0x35, 0x44, 0x31, 0x33,
                0x44, 0x36, 0x31, 0x30, 0x39, 0x35, 0x36, 0x44,
                0x41, 0x39, 0x44, 0x30, 0x35, 0x31, 0x41, 0x38,
                0x32, 0x30, 0x36, 0x45, 0x38, 0x36, 0x38, 0x34,
                0x34, 0x34, 0x43, 0x34, 0x41, 0x38, 0x42, 0x34};

        CtKMS2CKBB ckbb = new CtKMS2CKBB();
        try {
            ckbb.selectKey(KeySet, KeyIndex);
            ckbb.setKeyLocation(KeySetLocation, KeyIndexLocation);
            ckbb.setCKBBKeyBlock(baCKBBKeyBlock);
            ckbb.writeKey();
        } catch (CtKMS2Exception e) {
            e.printStackTrace();
        }

    }
    public static EMVSecureDataInfo GetEMVSecureDataInfo()
    {
        //Security.writekey();

        EMVSecureDataInfo secureInfo = new EMVSecureDataInfo();
//        secureInfo.version = 2;
//        secureInfo.keyType = (byte)2;		//dukpt key
//        secureInfo.cipherKeySet =  0xC002;
//        secureInfo.cipherKeyIndex = 0x0000;
//        secureInfo.cipherMethod = 0x01; //00:ecb 01:cbc
//        secureInfo.checksumType = 0;
//        secureInfo.ICVLen = 8; //required if cbc
//        secureInfo.ICV = new byte[8];
        secureInfo.version = 2; // When version equal to "2",  isKSNFixed is vaild to change
        secureInfo.keyType = CtEMV.KeyType_3DES_DUKPT;
        secureInfo.cipherKeySet =  0xC002;
        secureInfo.cipherKeyIndex = 0x0000;
        secureInfo.cipherMethod = CtEMV.DATA_ENCRYPT_METHOD_CBC;
        secureInfo.checksumType = CtEMV.ChecksumType_SHA1;
        secureInfo.ICVLen = 8;
        secureInfo.ICV = new byte[8];
        secureInfo.paddingMethod = CtEMV.PaddingMethod_FF;
        secureInfo.LRCIncluded = 1;
        secureInfo.SS_ESIncluded = 1;
        //Below parameter is vaild when version equal to "2"
        //Note: "The first time" using (after inject key), not allow to set to "1"
        secureInfo.isKSNFixed = 0;
        return secureInfo;
    }

    public static EMVCLSecureDataInfo GetEMVCLSecureDataInfo()
    {
        EMVCLSecureDataInfo emvclSecureInfo = new EMVCLSecureDataInfo();
        emvclSecureInfo.version = 1;
        emvclSecureInfo.keyType = (byte)2;		//dukpt key
        emvclSecureInfo.cipherKeySet =  0xC002;
        emvclSecureInfo.cipherKeyIndex = 0x0000;
        emvclSecureInfo.cipherMethod = 0x01; //00:ecb 01:cbc
        emvclSecureInfo.checksumType = 0;
        emvclSecureInfo.ICVLen = 8; //required if cbc
        emvclSecureInfo.ICV = new byte[8];
        return emvclSecureInfo;
    }

    public static byte[] whiteList()
    {
        byte[] listData = new byte[24];

        int listDatLen =  0;
        memcpy(listData, new byte[] {0, 0, 0, 10}, 4); // entry num
        listDatLen += 4;
        memcpy(listData, listDatLen, new byte[]{1, (byte) '0'}, 0, 2); listDatLen += 2;
        memcpy(listData, listDatLen, new byte[]{1, (byte) '1'}, 0, 2); listDatLen += 2;
        memcpy(listData, listDatLen, new byte[]{1, (byte) '2'}, 0, 2); listDatLen += 2;
        memcpy(listData, listDatLen, new byte[]{1, (byte) '3'}, 0, 2); listDatLen += 2;
        memcpy(listData, listDatLen, new byte[]{1, (byte) '4'}, 0, 2); listDatLen += 2;
        memcpy(listData, listDatLen, new byte[]{1, (byte) '5'}, 0, 2); listDatLen += 2;
        memcpy(listData, listDatLen, new byte[]{1, (byte) '6'}, 0, 2); listDatLen += 2;
        memcpy(listData, listDatLen, new byte[]{1, (byte) '7'}, 0, 2); listDatLen += 2;
        memcpy(listData, listDatLen, new byte[]{1, (byte) '8'}, 0, 2); listDatLen += 2;
        memcpy(listData, listDatLen, new byte[]{1, (byte) '9'}, 0, 2); listDatLen += 2;

        return listData;
    }

    public static byte[] Decrypt(byte[] InputData) {
        int KeySet = 0xC002;
        int KeyIndex = 0x0000;
        byte CipherMethod = CtEMV.DATA_ENCRYPT_METHOD_CBC;
        boolean UseCurrentKey = false;
        byte[] ICV = new byte[8];

        CtKMS2Dukpt dukpt = new CtKMS2Dukpt();
        try {
            dukpt.selectKey(KeySet, KeyIndex);

        dukpt.setCipherMethod(CipherMethod);
        dukpt.isUseCurrentKey(UseCurrentKey);
        dukpt.setICV(ICV, 0, ICV.length);
        dukpt.setInputData(InputData, 0, InputData.length);
        dukpt.dataDecrypt();
        byte[] output = dukpt.getOutpuData();
        byte[] ksn = dukpt.getKSN();
        return  output;
        } catch (CtKMS2Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }
}
