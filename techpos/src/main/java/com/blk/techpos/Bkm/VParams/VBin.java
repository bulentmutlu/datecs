package com.blk.techpos.Bkm.VParams;

import static com.blk.sdk.c.memcmp;
import static com.blk.sdk.c.memcpy;
import static com.blk.sdk.c.memset;
import static com.blk.sdk.c.sprintf;

import android.util.Log;

import com.blk.sdk.Convert;
import com.blk.sdk.file;
import com.blk.sdk.olib.olib;
import com.blk.sdk.c;

import java.io.IOException;

/**
 * Created by id on 9.03.2018.
 */

public class VBin {

    public static final int MAX_BIN_INFO	= 2048;
    public static final String PRMFILE		 = "VBIN";
    static final String PRMFILENEW	= "VBINNEW";
    static int SelectedBinIdx = 0;
    static VBin gPrms = new VBin();
    private static final String TAG = VBin.class.getSimpleName();


    public static class BinInfo
    {
        public byte[]      		Bin = new byte[3];
        public byte				Brand;
        public byte[]      		IssId = new byte[2];
        public byte[]      		AcqId = new byte[2];
        public byte        		ShareBrand;
        public byte				CardType;
        public byte[]      		Permissions = new byte[2];

        public static final int sizeof = 12;

        void Copy(byte[] bytes, int idx)
        {
            memcpy(Bin, 0, bytes, idx, Bin.length); idx += Bin.length;
            Brand = bytes[idx]; ++idx;
            memcpy(IssId, 0, bytes, idx, IssId.length); idx += IssId.length;
            memcpy(AcqId, 0, bytes, idx, AcqId.length); idx += AcqId.length;
            ShareBrand = bytes[idx]; ++idx;
            CardType = bytes[idx]; ++idx;
            memcpy(Permissions, 0, bytes, idx, Permissions.length); idx += Permissions.length;
        }
        void Copy(BinInfo bi)
        {
            memcpy(Bin, bi.Bin, Bin.length);
            Brand = bi.Brand;
            memcpy(IssId, bi.IssId, IssId.length);
            memcpy(AcqId, bi.AcqId, AcqId.length);
            ShareBrand = bi.ShareBrand;
            CardType = bi.CardType;
            memcpy(Permissions, bi.Permissions, Permissions.length);
        }
        void Clear()
        {
            memset(Bin, (byte) 0, Bin.length);
            Brand = 0;
            memset(IssId, (byte) 0, IssId.length);
            memset(AcqId, (byte) 0, AcqId.length);
            ShareBrand = 0;
            CardType = 0;
            memset(Permissions, (byte) 0, Permissions.length);
        }

        public void WriteToFile(file f) throws IOException {
            f.Write(Bin);
            f.Write(Brand);
            f.Write(IssId);
            f.Write(AcqId);
            f.Write(ShareBrand);
            f.Write(CardType);
            f.Write(Permissions);
        }
        public void ReadFromFile(file f) throws IOException {
            f.Read(Bin);
            Brand = f.ReadByte();
            f.Read(IssId);
            f.Read(AcqId);
            ShareBrand = f.ReadByte();
            CardType = f.ReadByte();
            f.Read(Permissions);
        }
    }

    PrmFileHeader       Hdr = new PrmFileHeader();
    int		            RecordCount;
    BinInfo[]			BinInfos = new BinInfo[MAX_BIN_INFO];

    VBin()
    {
        for (int i = 0; i < BinInfos.length; ++i)
            BinInfos[i] = new BinInfo();
    }

    void WriteToFile(file f) throws Exception {
        olib.WriteFile(Hdr, f);
        f.Write(RecordCount);
        for (int i = 0; i < RecordCount; ++i)
        {
            BinInfos[i].WriteToFile(f);
        }
    }
    void ReadFromFile(file f) throws Exception {
        olib.ReadFile(Hdr, f);
        RecordCount = f.ReadInt();
        for (int i = 0; i < RecordCount; ++i)
        {
            BinInfos[i].ReadFromFile(f);
        }
    }

    public static void DeleteParamFile()
    {
        file.Remove(PRMFILE);
        file.Remove(PRMFILENEW);
    }

    public static void ReadVBinPrms(VBin s) throws Exception
    {
        int fd = 0, i, j, size, idx = 0;
        short count = 0;
        VBin    dst = s;
        BinInfo tmpInfo = new BinInfo();
        byte partial = 0, updated = 0;
        byte[] tmp = new byte[32];
        short tmpId = 0;

        Log.i(TAG, "ReadVBinPrms size : " + file.Size(PRMFILE));


        if(s == null)                   dst = gPrms;
        if(file.Exist(PRMFILE))       dst.ReadFromFile(new file(PRMFILE, file.OpenMode.RDONLY));// dst.ReadFromFile(new file(PRMFILE, file.OpenMode.EP_RDONLY));
        if(!file.Exist(PRMFILENEW))  return;

        Log.i(TAG, "ReadVBinPrms NEW size : " + file.Size(PRMFILENEW));

        byte[] pBuff = file.ReadAllBytes(PRMFILENEW);
        Log.i(TAG, "NEWBIN : " + Convert.Buffer2Hex(pBuff));


        idx = PrmFileHeader.sizeof;
        partial = pBuff[idx++];
        short c = Convert.ToShort(pBuff, idx);
        count = Convert.SWAP_UINT16(Convert.ToShort(pBuff, idx));
        idx += 2;

        Log.i(TAG, "count:" + count);

        if(count > MAX_BIN_INFO)
        {
            Log.i(TAG, String.format("New Bin Count greater than %d. setted from %d to %d", MAX_BIN_INFO, count, MAX_BIN_INFO));
            count = MAX_BIN_INFO;
        }

        Log.i(TAG, "partial:" + partial);
        Log.i(TAG, "BinInfo RecCount:" + dst.RecordCount);

        if(partial == 0) olib.Clear(dst); // dst.Clear();

        Log.i(TAG, "BinInfo RecCount:" + dst.RecordCount);

        olib.ReadFile(dst.Hdr, new file(PRMFILENEW)); // dst.Hdr.Copy(pBuff); //memcpy(&dst.Hdr, pBuff, sizeof(VTerm.PrmFileHeader));

        //Add or update Records
        for(i = 0; i < count; i++)
        {
            tmpInfo.Copy(pBuff, idx); idx += BinInfo.sizeof;

            tmpId = Convert.ToShort(tmpInfo.IssId, 0);
            tmpId = Convert.SWAP_UINT16(tmpId);
            sprintf(tmp, "%04d", tmpId);
            Convert.EP_ascii2bcd(tmpInfo.IssId, 0, tmp, 4);

            tmpId = Convert.ToShort(tmpInfo.AcqId, 0);
            tmpId = Convert.SWAP_UINT16(tmpId);
            sprintf(tmp, "%04d", tmpId);
            Convert.EP_ascii2bcd(tmpInfo.AcqId, 0, tmp, 4);

            //EP_printf("Bin:%02X%02X%02X", tmpInfo.Bin[0], tmpInfo.Bin[1], tmpInfo.Bin[2]);

            updated = 0;
            for(j = 0; j < dst.RecordCount; j++)
            {
                if(!memcmp(dst.BinInfos[j].Bin, tmpInfo.Bin, 3))
                {
                    updated = 1;
                    dst.BinInfos[j].Copy(tmpInfo); // memcpy(&dst.BinInfos[j], &tmpInfo, c.sizeof(BinInfo));

                    Log.i(TAG, String.format("BinInfo Updated:%02X%02X%02X", dst.BinInfos[j].Bin[0], dst.BinInfos[j].Bin[1], dst.BinInfos[j].Bin[2]));
                    break;
                }
            }

            if(updated == 0)
            {
                dst.BinInfos[dst.RecordCount].Copy(tmpInfo); // memcpy(&dst.BinInfos[dst.RecordCount], &tmpInfo, c.sizeof(BinInfo));
                //EP_printf("BinInfo Added:%02X%02X%02X", dst.BinInfos[dst.RecordCount].Bin[0], dst.BinInfos[dst.RecordCount].Bin[1], dst.BinInfos[dst.RecordCount].Bin[2]);
                dst.RecordCount++;
            }
        }

        Log.i(TAG, "BinInfo RecCount:" + dst.RecordCount);

        if(partial != 0)
        {
            //Del Records
            count = Convert. SWAP_UINT16(Convert.ToShort(pBuff, idx)) ; idx += 2;
            Log.i(TAG, "count:" + count);

            if(count > MAX_BIN_INFO)
            {
                Log.i(TAG, String.format("Del Bin Count greater than %d. setted from %d to %d", MAX_BIN_INFO, count, MAX_BIN_INFO));
                count = MAX_BIN_INFO;
            }

            for(i = 0; i < count; i++)
            {
                memcpy(tmpInfo.Bin, 0, pBuff, idx, 3); idx += 3;

                //EP_printf("Bin:%02X%02X%02X", tmpInfo.Bin[0], tmpInfo.Bin[1], tmpInfo.Bin[2]);

                for(j = 0; j < dst.RecordCount; j++)
                {
                    if(!memcmp(dst.BinInfos[j].Bin, tmpInfo.Bin, 3))
                    {
                        Log.i(TAG, String.format("BinInfo Delete:%02X%02X%02X", dst.BinInfos[j].Bin[0], dst.BinInfos[j].Bin[1], dst.BinInfos[j].Bin[2]));

                        //memmove(&dst.BinInfos[j], &dst.BinInfos[j + 1], (dst.RecordCount - (j + 1))*c.sizeof(BinInfo));
                        for (int z = j; z < dst.RecordCount - 1; ++z)
                        {
                            dst.BinInfos[z].Copy(dst.BinInfos[z + 1]);
                        }

                        dst.RecordCount--;
                        dst.BinInfos[dst.RecordCount].Clear();
                        break;
                    }
                }
            }
        }

        file.Remove(PRMFILENEW);
        file.Remove(PRMFILE);

        dst.WriteToFile(new file(PRMFILE));

        Log.i(TAG, "BinInfo RecCount: " + dst.RecordCount);
    }

    public static VBin GetVBinPrms()
    {
        return gPrms;
    }

    public static BinInfo GetVBinBinInfoByBin(final byte[] bin)
    {
        int i = 0;
        VBin tmp = GetVBinPrms();
        byte[] bcdBin = new byte[3];

        Convert.EP_ascii2bcd(bcdBin, 0, bin, 6);

        for(i = 0; i < tmp.RecordCount; i++)
        {
            if(!memcmp(tmp.BinInfos[i].Bin, bcdBin, 3))
                break;
        }

        return tmp.BinInfos[i];
    }

    public static int SelVBinBinInfoByBin(final byte[] bin)
    {
        int i = 0;
        VBin tmp = GetVBinPrms();
        byte[] bcdBin = new byte[3];

        Log.i(TAG, "SelVBinBinInfoByBin : " + c.ToString(bin));

        SelectedBinIdx = -1;

       Convert.EP_ascii2bcd(bcdBin, 0, bin, 6);

        for(i = 0; i < tmp.RecordCount; i++)
        {
            //Log.i(TAG, String.format("%02X%02X%02X - %02X%02X%02X", bcdBin[0], bcdBin[1], bcdBin[2], tmp.BinInfos[i].Bin[0], tmp.BinInfos[i].Bin[1], tmp.BinInfos[i].Bin[2]));
            if(!memcmp(tmp.BinInfos[i].Bin, bcdBin, 3))
                break;
        }

        SelectedBinIdx =  (i >= tmp.RecordCount) ? -1 : i;

        Log.i(TAG, String.format("SelVBinBinInfoByBin:%s - %d = %d", c.ToString(bin), tmp.RecordCount, SelectedBinIdx));

        return SelectedBinIdx;
    }

    public static boolean IsVBinExist()
    {
        return file.Exist(PRMFILE);
    }

    public static boolean IsDebit(BinInfo binInfo)
    {
        BinInfo tmp = binInfo;
        if(tmp == null)
        {
            Log.i(TAG,"SelectedBinIdx:" + SelectedBinIdx);
            if(SelectedBinIdx < 0)
                return false;

            tmp = GetVBinPrms().BinInfos[SelectedBinIdx];
        }

        Log.i(TAG, "tmp.CardType:" + tmp.CardType);

        return tmp.CardType == 'D';
    }

    public static boolean IsCredit(BinInfo binInfo)
    {
        BinInfo tmp = binInfo;
        if(tmp == null)
        {
            Log.i(TAG, "SelectedBinIdx:" + SelectedBinIdx);
            if(SelectedBinIdx < 0)
                return false;

            tmp = GetVBinPrms().BinInfos[SelectedBinIdx];
        }

        return tmp.CardType == 'C';
    }

    public static boolean IsOnlinePin(BinInfo binInfo)
    {
        BinInfo tmp = binInfo;
        if(tmp == null)
        {
            Log.i(TAG, "SelectedBinIdx:%d" + SelectedBinIdx);
            if(SelectedBinIdx < 0)
                return false;

            tmp = GetVBinPrms().BinInfos[SelectedBinIdx];
        }

        return (tmp.Permissions[0] & 0x01) != 0;
    }

    public static boolean IsInstallment(BinInfo binInfo)
    {
        BinInfo tmp = binInfo;
        if(tmp == null)
        {
            Log.i(TAG, "SelectedBinIdx:" + SelectedBinIdx);
            if(SelectedBinIdx < 0)
                return false;

            tmp = GetVBinPrms().BinInfos[SelectedBinIdx];
        }

        return (tmp.Permissions[0] & 0x02) != 0;
    }

    public static boolean IsInquiryAndSpentBonus(BinInfo binInfo)
    {
        BinInfo tmp = binInfo;
        if(tmp == null)
        {
            Log.i(TAG, "SelectedBinIdx:" + SelectedBinIdx);
            if(SelectedBinIdx < 0)
                return false;

            tmp = GetVBinPrms().BinInfos[SelectedBinIdx];
        }

        return (tmp.Permissions[0] & 0x04) != 0;
    }

    public static boolean IsPrepaid(BinInfo binInfo)
    {
        BinInfo tmp = binInfo;
        if(tmp == null)
        {
            Log.i(TAG, "SelectedBinIdx:" + SelectedBinIdx);
            if(SelectedBinIdx < 0)
                return false;

            tmp = GetVBinPrms().BinInfos[SelectedBinIdx];
        }

        return (tmp.Permissions[0] & 0x08) != 0;
    }

    public static void PrintVBin()
    {
        int i = 0;

        Log.i(TAG, "********** VBIN **********");
        for(i = 0; i < GetVBinPrms().RecordCount; i++)
        {
            Log.i(TAG, "----------------------------------------");
            Log.i(TAG, String.format("Bin:%02X%02X%02X", GetVBinPrms().BinInfos[i].Bin[0], GetVBinPrms().BinInfos[i].Bin[1], GetVBinPrms().BinInfos[i].Bin[2]));
            Log.i(TAG, String.format("Brand:%d", GetVBinPrms().BinInfos[i].Brand));
            Log.i(TAG, String.format("IssId:%02X%02X", GetVBinPrms().BinInfos[i].IssId[0], GetVBinPrms().BinInfos[i].IssId[1]));
            Log.i(TAG, String.format("AcqId:%02X%02X", GetVBinPrms().BinInfos[i].AcqId[0], GetVBinPrms().BinInfos[i].AcqId[1]));
            Log.i(TAG, String.format("ShareBrand:%d", GetVBinPrms().BinInfos[i].ShareBrand));
            Log.i(TAG, String.format("CardType:%d", GetVBinPrms().BinInfos[i].CardType));
            Log.i(TAG, String.format("Permissions:%02X%02X", GetVBinPrms().BinInfos[i].Permissions[0], GetVBinPrms().BinInfos[i].Permissions[1]));

//            if(EP_KbGetLast() == EP_KEYCANCEL)
//            {
//                break;
//            }
        }
        //PrintFlush();
    }

}
