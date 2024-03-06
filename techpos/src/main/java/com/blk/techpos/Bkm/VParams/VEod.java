package com.blk.techpos.Bkm.VParams;

import android.util.Log;

import com.blk.sdk.Convert;
import com.blk.sdk.file;
import com.blk.sdk.olib.olib;

/**
 * Created by id on 9.03.2018.
 */

public class VEod {

    private static final String TAG = VEod.class.getSimpleName();
    public static final String PRMFILE		= "VEOD";

    static VEod gPrms = new VEod();


    PrmFileHeader Hdr = new PrmFileHeader();
    public short		MxTrn;
    public byte      	AutoSettle;
    public byte[]		AutoSettleTime = new byte[2];
    public byte		MxTryCnt;
    public byte		PrTry;
    public byte		TmAfterManuel;

    public static void DeleteParamFile()
    {
        file.Remove(PRMFILE);
    }

    public static void ReadVEodPrms(VEod s) throws Exception {
        int fd = 0;
        VEod dst = s;

        if(s == null) dst = gPrms;

        Log.i(TAG, "ReadVEodPrms size : " + file.Size(PRMFILE));


        if (!file.Exist(PRMFILE)) return;

        file f = new file(PRMFILE);
        olib.ReadFile(dst.Hdr, f);//dst.Hdr.ReadFromFile(f);
        dst.MxTrn = Convert.SWAP_UINT16(f.ReadShort());
        dst.AutoSettle = f.ReadByte();
        f.Read(dst.AutoSettleTime);
        dst.MxTryCnt = f.ReadByte();
        dst.PrTry = f.ReadByte();
        dst.TmAfterManuel = f.ReadByte();
    }

    public static VEod GetVEodPrms()
    {
        return gPrms;
    }

    public static void PrntVEodPrms(VEod p)
    {
        int i = 0, j = 0;
        VEod dst = p;

        if(p == null)
            dst = GetVEodPrms();

        Log.i(TAG, String.format("********** VEOD **********"));
        Log.i(TAG, String.format("MxTrn:%d", dst.MxTrn));
        Log.i(TAG, String.format("AutoSettle:%d", dst.AutoSettle));
        Log.i(TAG, String.format("AutoSettleTime:%02X%02X", dst.AutoSettleTime[0], dst.AutoSettleTime[1]));
        Log.i(TAG, String.format("MxTryCnt:%d", dst.MxTryCnt));
        Log.i(TAG, String.format("PrTry:%d", dst.PrTry));
        Log.i(TAG, String.format("TmAfterManuel:%d", dst.TmAfterManuel));
    }

}
