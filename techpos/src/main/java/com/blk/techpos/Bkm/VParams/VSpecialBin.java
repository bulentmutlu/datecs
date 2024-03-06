package com.blk.techpos.Bkm.VParams;

import static com.blk.sdk.c.memcmp;
import static com.blk.sdk.c.memcpy;
import static com.blk.sdk.c.sprintf;

import android.util.Log;

import com.blk.sdk.Convert;
import com.blk.sdk.file;
import com.blk.sdk.olib.olib;

/**
 * Created by id on 9.03.2018.
 */

public class VSpecialBin {
    private static final String TAG = VSpecialBin.class.getSimpleName();

    public static final int MAX_SPECIALBIN_INFO		= 2048;
    public static final int MAX_SPECIALBIN_ACQINFO	= 32;

    public static final String PRMFILE		= "VSpecialBIN";
    public static final String PRMFILENEW	= "VSpecialBINNEW";

    static int SelectedBinIdx = 0;
    static int PrmReadStat = 0;
    static VSpecialBin gPrms = new VSpecialBin();

    public static class SpecialBinInfo
    {
        public byte[]		        Bin = new byte[6];
        public byte				Brand;
        public byte                AcqsLen;
        public byte[][]      		Acqs = new byte[MAX_SPECIALBIN_ACQINFO][2];
        public byte				CardType;
        public byte[]      		Permissions = new byte[2];

//        void Clear()
//        {
//            memset(Bin, (byte) 0, Bin.length);
//            Brand = 0;
//            AcqsLen = 0;
//            for (int i = 0; i < MAX_SPECIALBIN_ACQINFO; ++i)
//                memset(Acqs[i], (byte) 0, Acqs[i].length);
//            CardType = 0;
//            memset(Permissions, (byte) 0, Permissions.length);
//        }
//
//        void WriteToFile(file f) throws IOException {
//            f.Write(Bin);
//            f.Write(Brand);
//            f.Write(AcqsLen);
//            for (int i = 0; i < AcqsLen; ++i)
//                f.Write(Acqs[i]);
//            f.Write(CardType);
//            f.Write(Permissions);
//        }
//        void ReadFromFile(file f) throws IOException {
//            f.Read(Bin);
//            Brand = f.ReadByte();
//            AcqsLen = f.ReadByte();
//            for (int i = 0; i < AcqsLen; ++i)
//                f.Read(Acqs[i]);
//            CardType = f.ReadByte();
//            f.Read(Permissions);
//        }
//
//        void Copy(SpecialBinInfo from)
//        {
//            memcpy(Bin, from.Bin, Bin.length);
//            Brand = from.Brand;
//            AcqsLen = from.AcqsLen;
//            for (int i = 0; i < AcqsLen; ++i)
//                memcpy(Acqs[i], from.Acqs[i], Acqs[i].length);
//            CardType = from.CardType;
//            memcpy(Permissions, from.Permissions, Permissions.length);
//        }
    }

    PrmFileHeader Hdr = new PrmFileHeader();
    int		RecordCount;
    SpecialBinInfo[]		BinInfos = new SpecialBinInfo[MAX_SPECIALBIN_INFO];

    public VSpecialBin()
    {
//        for (int i = 0; i < BinInfos.length; ++i)
//            BinInfos[i] = new SpecialBinInfo();
    }

//    void Clear()
//    {
//        Hdr.Clear();
//        RecordCount = 0;
//        for (int i = 0; i < MAX_SPECIALBIN_ACQINFO; ++i)
//            BinInfos[i].Clear();
//    }

    void WriteToFile(file f) throws Exception {
        olib.WriteFile(Hdr, f); //Hdr.WriteToFile(f);
        f.Write(RecordCount);
        for (int i = 0; i < RecordCount; ++i)
            olib.WriteFile(BinInfos[i], f);
    }
    void ReadFromFile(file f) throws Exception {
        olib.ReadFile(Hdr, f);
        RecordCount = f.ReadInt();
        for (int i = 0; i < RecordCount; ++i) {
            BinInfos[i] = new SpecialBinInfo();
            olib.ReadFile(BinInfos[i], f);
        }
    }

    public static void DeleteParamFile()
    {
        file.Remove(PRMFILE);
        file.Remove(PRMFILENEW);
    }

    public static void ReadVSpecialBinPrms(VSpecialBin s) throws Exception {
        int fd = 0, i, j, k, size, idx = 0;
        short count = 0;
        VSpecialBin dst = s;
        SpecialBinInfo tmpInfo = new SpecialBinInfo();
        byte partial = 0, updated = 0;
        byte[] tmp = new byte[32];
        short tmpId = 0;

        Log.i(TAG, "ReadVSpecialBinPrms size : " + file.Size(PRMFILE));


        if(s == null)                   dst = gPrms;
        if (file.Exist(PRMFILE))     dst.ReadFromFile(new file(PRMFILE));
        if(!file.Exist(PRMFILENEW))  return;

        Log.i(TAG, "ReadVSpecialBinPrms NEW size : " + file.Size(PRMFILENEW));

        byte[] pBuff = file.ReadAllBytes(PRMFILENEW);
        Log.i(TAG, "NEWSBIN : " + Convert.Buffer2Hex(pBuff));

        idx = PrmFileHeader.sizeof;
        partial = pBuff[idx++];
        count = Convert.SWAP_UINT16(Convert.ToShort(pBuff, idx)); idx += 2;

        Log.i(TAG, String.format("partial:%d - add count:%d", partial, count));

        if(count > MAX_SPECIALBIN_INFO)
        {
            Log.i(TAG, String.format("New Bin Count greater than %d. setted from %d to %d", MAX_SPECIALBIN_INFO, count, MAX_SPECIALBIN_INFO));
            count = MAX_SPECIALBIN_INFO;
        }

        if(partial == 0) olib.Clear(dst); // dst.Clear();
        olib.ReadFile(dst.Hdr, new file(PRMFILENEW));// dst.Hdr.Copy(pBuff);

        //Add or update Records
        for(i = 0; i < count; i++)
        {
            olib.Clear(tmpInfo); // tmpInfo.Clear();
            memcpy(tmpInfo.Bin, 0, pBuff, idx, tmpInfo.Bin.length); idx += tmpInfo.Bin.length;
            tmpInfo.Brand = pBuff[idx]; ++idx;
            tmpInfo.AcqsLen = pBuff[idx]; ++idx;
            for (int ii = 0; ii < tmpInfo.AcqsLen; ++ii) {
                memcpy(tmpInfo.Acqs[ii], 0, pBuff, idx, tmpInfo.Acqs[ii].length); idx += tmpInfo.Acqs[ii].length;
            }
            tmpInfo.CardType = pBuff[idx]; ++idx;
            memcpy(tmpInfo.Permissions, 0, pBuff, idx, tmpInfo.Permissions.length); idx += tmpInfo.Permissions.length;

            for(k = 0; k < tmpInfo.AcqsLen; k++)
            {
                tmpId = Convert.SWAP_UINT16(Convert.ToShort(tmpInfo.Acqs[k], 0));
                sprintf(tmp, "%04d", tmpId);
                Convert.EP_ascii2bcd(tmpInfo.Acqs[k], 0, tmp, 4);
            }

            updated = 0;
            for(j = 0; j < dst.RecordCount; j++)
            {
                if(!memcmp(dst.BinInfos[j].Bin, tmpInfo.Bin, 6))
                {
                    updated = 1;
                    olib.Copy(dst.BinInfos[j], tmpInfo); //dst.BinInfos[j].Copy(tmpInfo);
                    Log.i(TAG, String.format("SpecialBinInfo Updated:%c%c%c%c%c%c", dst.BinInfos[j].Bin[0], dst.BinInfos[j].Bin[1], dst.BinInfos[j].Bin[2], dst.BinInfos[j].Bin[3], dst.BinInfos[j].Bin[4], dst.BinInfos[j].Bin[5]));
                    break;
                }
            }

            if(updated == 0)
            {
                if (dst.BinInfos[dst.RecordCount] == null) dst.BinInfos[dst.RecordCount] = new SpecialBinInfo();
                olib.Copy(dst.BinInfos[dst.RecordCount], tmpInfo); // dst.BinInfos[dst.RecordCount].Copy(tmpInfo);
                //EP_printf("SpecialBinInfo Added:%c%c%c%c%c%c", dst.BinInfos[j].Bin[0], dst.BinInfos[j].Bin[1], dst.BinInfos[j].Bin[2], dst.BinInfos[j].Bin[3], dst.BinInfos[j].Bin[4], dst.BinInfos[j].Bin[5]);
                dst.RecordCount++;
            }
        }

        count = Convert.SWAP_UINT16(Convert.ToShort(pBuff, idx)); idx += 2;

        Log.i(TAG, String.format("delete count:%d", count));

        if(partial != 0)
        {
            //Del Records

            if(count > MAX_SPECIALBIN_INFO)
            {
                Log.i(TAG, String.format("Del Bin Count greater than %d. setted from %d to %d", MAX_SPECIALBIN_INFO, count, MAX_SPECIALBIN_INFO));
                count = MAX_SPECIALBIN_INFO;
            }

            for(i = 0; i < count; i++)
            {
                memcpy(tmpInfo.Bin, 0, pBuff, idx, 6); idx += 6;

                for(j = 0; j < dst.RecordCount; j++)
                {
                    if(!memcmp(dst.BinInfos[j].Bin, tmpInfo.Bin, 6))
                    {
                        Log.i(TAG, String.format("SpecialBinInfo Delete:%c%c%c%c%c%c", dst.BinInfos[j].Bin[0], dst.BinInfos[j].Bin[1], dst.BinInfos[j].Bin[2], dst.BinInfos[j].Bin[3], dst.BinInfos[j].Bin[4], dst.BinInfos[j].Bin[5]));

                        // memmove(&dst.BinInfos[j], &dst.BinInfos[j + 1], (dst.RecordCount - (j + 1))*sizeof(SpecialBinInfo));
                        for (int z = j; z < dst.RecordCount - 1; ++z)
                        {
                            dst.BinInfos[z] = dst.BinInfos[z + 1];
                             // dst.BinInfos[z].Copy(dst.BinInfos[z + 1]);
                        }

                        dst.RecordCount--;
                        dst.BinInfos[dst.RecordCount] = null; // new ObjectClear().Clear(dst.BinInfos[dst.RecordCount]);  //dst.BinInfos[dst.RecordCount].Clear();
                        break;
                    }
                }
            }
        }

        file.Remove(PRMFILENEW);
        file.Remove(PRMFILE);

        dst.WriteToFile(new file(PRMFILE));

        Log.i(TAG, String.format("SpecialBinInfo RecCount:%d", dst.RecordCount));
    }

    public static VSpecialBin GetVSpecialBinPrms()
    {
        return gPrms;
    }

    public static SpecialBinInfo GetVSpecialBinSpecialBinInfoByBin(final byte[] bin)
    {
        int i = 0;
        VSpecialBin tmp = GetVSpecialBinPrms();

        for(i = 0; i < tmp.RecordCount; i++)
        {
            //Log.i(TAG, String.format("%c%c%c%c%c%c - %c%c%c%c%c%c", bin[0], bin[1], bin[2], bin[3], bin[4], bin[5], tmp.BinInfos[i].Bin[0], tmp.BinInfos[i].Bin[1], tmp.BinInfos[i].Bin[2], tmp.BinInfos[i].Bin[3], tmp.BinInfos[i].Bin[4], tmp.BinInfos[i].Bin[5]));
            if((tmp.BinInfos[i].Bin[0] == bin[0] || tmp.BinInfos[i].Bin[0] == 'X')
                    && (tmp.BinInfos[i].Bin[1] == bin[1] || tmp.BinInfos[i].Bin[1] == 'X')
                    && (tmp.BinInfos[i].Bin[2] == bin[2] || tmp.BinInfos[i].Bin[2] == 'X')
                    && (tmp.BinInfos[i].Bin[3] == bin[3] || tmp.BinInfos[i].Bin[3] == 'X')
                    && (tmp.BinInfos[i].Bin[4] == bin[4] || tmp.BinInfos[i].Bin[4] == 'X')
                    && (tmp.BinInfos[i].Bin[5] == bin[5] || tmp.BinInfos[i].Bin[5] == 'X')
                    )
                break;
        }

        return tmp.BinInfos[i];
    }

    public static int SelVSpecialBinSpecialBinInfoByBin(final byte[] bin)
    {
        int i = 0;
        VSpecialBin tmp = GetVSpecialBinPrms();

        for(i = 0; i < tmp.RecordCount; i++)
        {
            //Log.i(TAG, String.format("%c%c%c%c%c%c - %c%c%c%c%c%c", bin[0], bin[1], bin[2], bin[3], bin[4], bin[5], tmp.BinInfos[i].Bin[0], tmp.BinInfos[i].Bin[1], tmp.BinInfos[i].Bin[2], tmp.BinInfos[i].Bin[3], tmp.BinInfos[i].Bin[4], tmp.BinInfos[i].Bin[5]));
            if((tmp.BinInfos[i].Bin[0] == bin[0] || tmp.BinInfos[i].Bin[0] == 'X')
                    && (tmp.BinInfos[i].Bin[1] == bin[1] || tmp.BinInfos[i].Bin[1] == 'X')
                    && (tmp.BinInfos[i].Bin[2] == bin[2] || tmp.BinInfos[i].Bin[2] == 'X')
                    && (tmp.BinInfos[i].Bin[3] == bin[3] || tmp.BinInfos[i].Bin[3] == 'X')
                    && (tmp.BinInfos[i].Bin[4] == bin[4] || tmp.BinInfos[i].Bin[4] == 'X')
                    && (tmp.BinInfos[i].Bin[5] == bin[5] || tmp.BinInfos[i].Bin[5] == 'X')
                    )
                break;
        }

        if(i >= tmp.RecordCount)
            return -1;

        SelectedBinIdx = i;
        return SelectedBinIdx;
    }

    public static int PrintVSpecialBin()
    {
        int i = 0, j = 0;
        VSpecialBin tmp = GetVSpecialBinPrms();

        Log.i(TAG, String.format("********** VSpecialBIN **********"));

        for(i = 0; i < tmp.RecordCount; i++)
        {
            Log.i(TAG, String.format("----------------------------------------"));
            Log.i(TAG, String.format("Bin:%s", new String(tmp.BinInfos[i].Bin)));
            Log.i(TAG, String.format("Brand:%c", tmp.BinInfos[i].Brand));

            for(j = 0; j < tmp.BinInfos[i].AcqsLen; j++)
                Log.i(TAG, String.format("%d.AcqId:%02X%02X", j, tmp.BinInfos[i].Acqs[j][0], tmp.BinInfos[i].Acqs[j][1]));

            Log.i(TAG, String.format("CardType:%c", tmp.BinInfos[i].CardType));
            Log.i(TAG, String.format("Permissions:%02X%02X", tmp.BinInfos[i].Permissions[0], tmp.BinInfos[i].Permissions[1]));

//            if(EP_KbGetLast() == EP_KEYCANCEL)
//            {
//                break;
//            }
        }
        return 0;
    }

}
