package com.blk.techpos.Bkm.VParams;

import android.util.Log;

import com.blk.sdk.file;
import com.blk.sdk.c;
import com.blk.techpos.Bkm.Bkm;

/**
 * Created by id on 12.03.2018.
 */

public class PrmFileHeader
{
    private static final String TAG = PrmFileHeader.class.getSimpleName();

    public short		PrmType;
    public byte[]		PrmName = new byte[32];
    public byte[]		PrmVer= new byte[4];
    public int		    PrmLen;

    public static final int sizeof = 42;

//    public void Clear()
//    {
//        PrmType = 0;
//        memset(PrmName, (byte) 0, PrmName.length);
//        memset(PrmVer, (byte) 0, PrmVer.length);
//        PrmLen = 0;
//    }
//
//    public void Copy(byte[] hdrBytes)
//    {
//        int idx = 0;
//        PrmType = Convert.ToShort(hdrBytes, idx);                                         idx += 2;
//        memcpy(PrmName, 0, hdrBytes, idx, PrmName.length); idx += PrmName.length;
//        memcpy(PrmVer, 0, hdrBytes, idx, PrmVer.length);    idx += PrmVer.length;
//        PrmLen = Convert.ToInt(hdrBytes, idx);
//    }
//
//    public void WriteToFile(file f) throws IOException {
//        f.Write(PrmType);
//        f.Write(PrmName);
//        f.Write(PrmVer);
//        f.Write(PrmLen);
//    }
//
//    public void ReadFromFile(file f) throws IOException {
//        PrmType = f.ReadShort();
//        f.Read(PrmName);
//        f.Read(PrmVer);
//        PrmLen = f.ReadInt();
//    }
    public void Log()
    {
        String TAG = "PrmFileHeader";
        Log.i(TAG, "hdr.PrmType:" + PrmType);
        Log.i(TAG, "hdr.PrmName:" + new String(PrmName, 0, c.strlen(PrmName)));
        Log.i(TAG, "hdr.PrmVer:" + String.format("%02X%02X%02X%02X", PrmVer[0], PrmVer[1], PrmVer[2], PrmVer[3]));
        Log.i(TAG, "hdr.PrmLen:"+PrmLen);
    }

    public static void DeleteVParams()
    {
        VTerm.DeleteParamFile();
        VBin.DeleteParamFile();
        VSpecialBin.DeleteParamFile();
        VEod.DeleteParamFile();
        file.Remove(Bkm.file_VCAPK);
        file.Remove("VCOMM");
        file.Remove("VEMVCLSPP3");
        file.Remove("VEMVCLSPW2");
        file.Remove("VEMVCON");
    }

    public static void DumpFileSizes()
    {
        Log.i(TAG, "VTerm : " + file.Size(VTerm.PRMFILE));
        Log.i(TAG, "VBin : " + file.Size(VBin.PRMFILE));
        Log.i(TAG, "VSpecialBin : " + file.Size(VSpecialBin.PRMFILE));
        Log.i(TAG, "VEod : " + file.Size(VEod.PRMFILE));
        Log.i(TAG, "VCAK : " + file.Size(Bkm.file_VCAPK));
        Log.i(TAG, "VCOMM : " + file.Size("VCOMM"));
        Log.i(TAG, "VEMVCLSPP3 : " + file.Size("VEMVCLSPP3"));
        Log.i(TAG, "VEMVCLSPW2 : " + file.Size("VEMVCLSPW2"));
        Log.i(TAG, "VEMVCON : " + file.Size("VEMVCON"));
    }
}
