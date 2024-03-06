package com.blk.techpos.Bkm.VParams;

import static com.blk.sdk.Convert.SWAP_UINT16;
import static com.blk.sdk.Utility.log;
import static com.blk.sdk.c.memcmp;
import static com.blk.sdk.c.memcpy;

import android.util.Log;

import com.blk.sdk.Convert;
import com.blk.sdk.file;
import com.blk.sdk.Utility;
import com.blk.sdk.olib.olib;
import com.blk.sdk.c;
import com.blk.techpos.Bkm.TranStruct;
import com.blk.techpos.techpos;

/**
 * Created by id on 8.03.2018.
 */

public class VTerm {
    public static class AcqAIDList
    {
        public byte AIDCnt;
        public byte[][] AIDList = new byte[24][18];
    }

    public static class AcqCurList
    {
        public byte CurCnt;
        public byte[][] CurList = new byte[16][4];
    }

    public static class AcqInfo
    {
        public byte[]		        AcqId = new byte[2];
        public byte[]				AcqName= new byte[48];
        public byte[]				TermId= new byte[8];
        public byte[]				MercId= new byte[15];
        public byte        		    ShareBrand;
        public byte[]				MenuStr= new byte[24];
        public byte[]				BkmReceiptCode= new byte[2];
        public byte[]				MercSlipName= new byte[48];
        public byte[]				MercSlipAddr= new byte[256];
        public byte[]				MercCity= new byte[24];
        public byte[]				MercPhoneNo= new byte[16];
        public byte[]				MCC= new byte[5];				//Merchant Category Code
        public byte[]				TCC= new byte[2];				//Transaction Category Code
        public byte[]				PosSupportPhoneNo= new byte[16];
        public byte[]				PosAuthPhoneNo= new byte[16];
        public byte[]				TCKN= new byte[16];
        public byte[]				VN= new byte[16];				// Vergi No
        public byte[]				VDN= new byte[24];			// Vergi Dairesi Ismi
        public byte        		    DoctorPos;
        public byte[]		        DoctorPosKDV= new byte[4];
        public byte[]		        DoctorPosStopaj= new byte[4];
        public byte[]		        OfflineSurcharge= new byte[4];
        public byte[]				DefCurCode= new byte[4];
        public byte[]		        Permissions= new byte[10];
        public AcqAIDList			AIDList = new AcqAIDList();
        public AcqAIDList			ClssAIDList = new AcqAIDList();
        public byte[]				SettleSlipMsg= new byte[260];
        public byte[]				AmexMemberNo= new byte[16];
        public AcqCurList			CurList = new AcqCurList();
    }
    public static class QRParams
    {
        public byte IslemIzni;
        public byte KartDatasiSorgulamaBaslangici;
        public byte KartDatasiSorgulamaAraligi;
        public byte KartDatasiSorgulamaSonu;
    }

    public PrmFileHeader		Hdr = new PrmFileHeader();
    public byte[]				RFU= new byte[8];
    public byte[]		FirstBatchNo= new byte[3];
    public byte[]		FirstStan= new byte[3];
    public byte		OffAdvMxSendTryCnt;
    public byte		OffAdvTryPeriod;
    public byte		HandShakeTryPeriod;
    public QRParams qrParams = new QRParams();
    public byte		AcqInfoLen;
    public AcqInfo[]				AcqInfos = new AcqInfo[32];

    static VTerm gPrms = new VTerm();
    static int SelectedAcqIdx = 0;
    public static String PRMFILE = "VTERM";
    private static final String TAG = VTerm.class.getSimpleName();

    static
    {
        try {
            if (file.Exist(PRMFILE)) {
                ReadVTermPrms(gPrms);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public VTerm()
    {
        for (int i = 0; i < AcqInfos.length; ++i)
            AcqInfos[i] = new AcqInfo();
    }
    public static void DeleteParamFile()
    {
        file.Remove(PRMFILE);
    }

    public static void ReadVTermPrms(VTerm s) throws Exception
    {
        int i, j, idx = 0;
        VTerm dst = s;
        int chTmpLen = 0;
        int exDataLen = 0;
        byte[] exData = new byte[2048];
        byte[] tlvData= new byte[1024];
        short tlvDataLen = 0;

        if(s == null)               dst = gPrms;
        if(!file.Exist(PRMFILE)) return;
        Log.i(TAG, "ReadVTermPrms size : " + file.Size(PRMFILE));

        file f = new file(VTerm.PRMFILE, file.OpenMode.RDONLY);
        olib.ReadFile(dst.Hdr, f); // dst.Hdr.ReadFromFile(f);
        f.Read(dst.RFU);
        f.Read(dst.FirstBatchNo);
        f.Read(dst.FirstStan);
        exDataLen = Convert.unsignedShortToInt(SWAP_UINT16(f.ReadShort()));
        f.Read(exData, 0, exDataLen);

        //EP_printf("EXDATA:%d", exDataLen);
        //EP_HexDump(exData, exDataLen);

        if(techpos.GetTLVData(exData, exDataLen, 0x01, tlvData, (short) c.sizeof(tlvData)) > 0)
        {
            dst.OffAdvMxSendTryCnt = tlvData[0];
            dst.OffAdvTryPeriod = tlvData[1];
        }

        if(techpos.GetTLVData(exData, exDataLen, 0x02, tlvData, (short) c.sizeof(tlvData)) > 0)
        {
            dst.HandShakeTryPeriod = tlvData[0];
        }
        if(techpos.GetTLVData(exData, exDataLen, 0x04, tlvData, (short) c.sizeof(tlvData)) > 0)
        {
            dst.qrParams.IslemIzni = tlvData[0];
            dst.qrParams.KartDatasiSorgulamaBaslangici = tlvData[1];
            dst.qrParams.KartDatasiSorgulamaAraligi = tlvData[2];
            dst.qrParams.KartDatasiSorgulamaSonu = tlvData[3];
        }

        dst.AcqInfoLen = f.ReadByte();
        for(i = 0; i < dst.AcqInfoLen; i++)
        {
            f.Read(dst.AcqInfos[i].AcqId);
            chTmpLen = f.ReadUnsignedByte();
            f.Read(dst.AcqInfos[i].AcqName, 0, chTmpLen);
            f.Read(dst.AcqInfos[i].TermId);
            f.Read(dst.AcqInfos[i].MercId);
            dst.AcqInfos[i].ShareBrand = f.ReadByte();
            chTmpLen = f.ReadUnsignedByte();
            f.Read(dst.AcqInfos[i].MenuStr, 0, chTmpLen);
            f.Read(dst.AcqInfos[i].BkmReceiptCode);
            chTmpLen = f.ReadUnsignedByte();
            f.Read(dst.AcqInfos[i].MercSlipName, 0, chTmpLen);
            chTmpLen = f.ReadUnsignedByte();
            f.Read(dst.AcqInfos[i].MercSlipAddr, 0, chTmpLen);
            chTmpLen = f.ReadUnsignedByte();
            f.Read(dst.AcqInfos[i].MercCity, 0, chTmpLen);
            f.Read(dst.AcqInfos[i].MercPhoneNo, 0, 10);
            f.Read(dst.AcqInfos[i].MCC, 0, 4);
            f.Read(dst.AcqInfos[i].TCC, 0, 1);
            f.Read(dst.AcqInfos[i].PosSupportPhoneNo, 0, 10);
            f.Read(dst.AcqInfos[i].PosAuthPhoneNo, 0, 10);
            f.Read(dst.AcqInfos[i].TCKN, 0, 11);
            f.Read(dst.AcqInfos[i].VN, 0, 10);
            chTmpLen = f.ReadUnsignedByte();
            f.Read(dst.AcqInfos[i].VDN, 0, chTmpLen);
            dst.AcqInfos[i].DoctorPos = f.ReadByte();
            f.Read(dst.AcqInfos[i].DoctorPosKDV, 0, 4);
            f.Read(dst.AcqInfos[i].DoctorPosStopaj, 0, 4);
            f.Read(dst.AcqInfos[i].OfflineSurcharge, 0, 4);
            f.Read(dst.AcqInfos[i].DefCurCode, 0, 3);
            f.Read(dst.AcqInfos[i].Permissions, 0, 10);

            exDataLen = Convert.unsignedShortToInt(SWAP_UINT16(f.ReadShort()));
            f.Read(exData, 0, exDataLen);

            //EP_printf("ACQ EXDATA:%d", exDataLen);
            //EP_HexDump(exData, exDataLen);

            idx = 0;
            tlvDataLen = techpos.GetTLVData(exData, exDataLen, 0x01, tlvData, (short) c.sizeof(tlvData));
            if(tlvDataLen > 0)
            {
                dst.AcqInfos[i].AIDList.AIDCnt = tlvData[idx++];
                for(j = 0; j < dst.AcqInfos[i].AIDList.AIDCnt; j++)
                {
                    int tmpLen = Convert.unsignedByteToInt(tlvData[idx++]);
                    Convert.EP_BfAscii(dst.AcqInfos[i].AIDList.AIDList[j], tlvData, idx, tmpLen);
                    //memcpy(dst.AcqInfos[i].AIDList.AIDList[j], &tlvData[idx], tmpLen);
                    idx += tmpLen;
                }
            }

            idx = 0;
            tlvDataLen = techpos.GetTLVData(exData, exDataLen, 0x02, tlvData, (short) c.sizeof(tlvData));
            if(tlvDataLen > 0)
            {
                dst.AcqInfos[i].ClssAIDList.AIDCnt = tlvData[idx++];
                for(j = 0; j < dst.AcqInfos[i].ClssAIDList.AIDCnt; j++)
                {
                    int tmpLen = Convert.unsignedByteToInt(tlvData[idx++]);
                    Convert.EP_BfAscii(dst.AcqInfos[i].ClssAIDList.AIDList[j], tlvData, idx, tmpLen);
                    //memcpy(dst.AcqInfos[i].ClssAIDList.AIDList[j], &tlvData[idx], tmpLen);
                    idx += tmpLen;
                }
            }

            byte[] SettleSlipMsg = new byte[dst.AcqInfos[i].SettleSlipMsg.length];
            byte[] AmexMemberNo = new byte[dst.AcqInfos[i].AmexMemberNo.length];
            techpos.GetTLVData(exData, exDataLen, 0x03, SettleSlipMsg, (short) (c.sizeof(dst.AcqInfos[i].SettleSlipMsg) - 1));
            techpos.GetTLVData(exData, exDataLen, 0x04, AmexMemberNo, (short) (c.sizeof(dst.AcqInfos[i].AmexMemberNo) - 1));
            memcpy(dst.AcqInfos[i].SettleSlipMsg, SettleSlipMsg, SettleSlipMsg.length);
            memcpy(dst.AcqInfos[i].AmexMemberNo, AmexMemberNo, AmexMemberNo.length);

            idx = 0;
            tlvDataLen = techpos.GetTLVData(exData, exDataLen, 0x05, tlvData, (short) c.sizeof(tlvData));
            if(tlvDataLen > 0)
            {
                dst.AcqInfos[i].CurList.CurCnt = tlvData[idx++];
                for(j = 0; j < dst.AcqInfos[i].CurList.CurCnt; j++)
                {
                    memcpy(dst.AcqInfos[i].CurList.CurList[j], 0, tlvData, idx, 3);
                    idx += 3;
                }
            }
        }
        f.Close();
    }

    public static VTerm GetVTermPrms(){
        return gPrms;
    }

    public static void PrntVTermPrms(VTerm s)
    {
        int i = 0, j = 0;
        VTerm  dst = s;

        if(s == null)
            dst = GetVTermPrms();

        Utility.log("********** VTERM **********");
        Utility.log("BATCH NO: %02X%02X%02X", dst.FirstBatchNo[0], dst.FirstBatchNo[1], dst.FirstBatchNo[2]);
        Utility.log("STAN: %02X%02X%02X", dst.FirstStan[0], dst.FirstStan[1], dst.FirstStan[2]);
        Utility.log("OffAdvMxSendTryCnt:%d", dst.OffAdvMxSendTryCnt);
        Utility.log("OffAdvTryPeriod:%d", dst.OffAdvTryPeriod);
        Utility.log("HandShakeTryPeriod:%d", dst.HandShakeTryPeriod);

        for(i = 0; i < dst.AcqInfoLen; i++)
        {
            Utility.log("********** ACQ %d **********", i);
            Utility.log("ID: %02X%02X", dst.AcqInfos[i].AcqId[0], dst.AcqInfos[i].AcqId[1]);
            Utility.log("NAME:%s", c.ToString(dst.AcqInfos[i].AcqName));
            Utility.log("TID:%s", c.ToString(dst.AcqInfos[i].TermId));
            Utility.log("MID:%s", c.ToString(dst.AcqInfos[i].MercId));
            Utility.log("BRAND:%d", dst.AcqInfos[i].ShareBrand);
            Utility.log("DISP:%s", c.ToString(dst.AcqInfos[i].MenuStr));
            Utility.log("BRC:%s", c.ToString(dst.AcqInfos[i].BkmReceiptCode));
            Utility.log("PRNT:%s", c.ToString(dst.AcqInfos[i].MercSlipName));
            Utility.log("ADDR:%s", c.ToString(dst.AcqInfos[i].MercSlipAddr));
            Utility.log("CITY:%s", c.ToString(dst.AcqInfos[i].MercCity));
            Utility.log("PNO:%s", c.ToString(dst.AcqInfos[i].MercPhoneNo));
            Utility.log("MCC:%s", c.ToString(dst.AcqInfos[i].MCC));
            Utility.log("TCC:%s", c.ToString(dst.AcqInfos[i].TCC));
            Utility.log("SPNO:%s", c.ToString(dst.AcqInfos[i].PosSupportPhoneNo));
            Utility.log("APNO:%s", c.ToString(dst.AcqInfos[i].PosAuthPhoneNo));
            Utility.log("TCKN:%s", c.ToString(dst.AcqInfos[i].TCKN));
            Utility.log("VN:%s", c.ToString(dst.AcqInfos[i].VN));
            Utility.log("VDN:%s", c.ToString(dst.AcqInfos[i].VDN));
            Utility.log("DPOS:%d", dst.AcqInfos[i].DoctorPos);
            Utility.log("DPOSKDV:%02X%02X%02X%02X", dst.AcqInfos[i].DoctorPosKDV[0], dst.AcqInfos[i].DoctorPosKDV[1], dst.AcqInfos[i].DoctorPosKDV[2], dst.AcqInfos[i].DoctorPosKDV[3]);
            Utility.log("DPOSSTOPAJ:%02X%02X%02X%02X", dst.AcqInfos[i].DoctorPosStopaj[0], dst.AcqInfos[i].DoctorPosStopaj[1], dst.AcqInfos[i].DoctorPosStopaj[2], dst.AcqInfos[i].DoctorPosStopaj[3]);
            Utility.log("OFFSURCHARGE:%02X%02X%02X%02X", dst.AcqInfos[i].OfflineSurcharge[0], dst.AcqInfos[i].OfflineSurcharge[1], dst.AcqInfos[i].OfflineSurcharge[2], dst.AcqInfos[i].OfflineSurcharge[3]);
            Utility.log("CURCODE:%s", c.ToString(dst.AcqInfos[i].DefCurCode));
            Utility.log("PERMISIONS");
            Utility.logDump(dst.AcqInfos[i].Permissions, c.sizeof(dst.AcqInfos[i].Permissions));

            for(j = 0; j < dst.AcqInfos[i].AIDList.AIDCnt; j++)
            {
                Utility.log("SAID:%s", c.ToString(dst.AcqInfos[i].AIDList.AIDList[j]));
            }

            for(j = 0; j < dst.AcqInfos[i].ClssAIDList.AIDCnt; j++)
            {
                Utility.log("SCAID:%s", c.ToString(dst.AcqInfos[i].ClssAIDList.AIDList[j]));
            }

            Utility.log("SETTLE MSG:%s", c.ToString(dst.AcqInfos[i].SettleSlipMsg));
            Utility.log("AMEX MNO:%s", c.ToString(dst.AcqInfos[i].AmexMemberNo));

            for(j = 0; j < dst.AcqInfos[i].CurList.CurCnt; j++)
            {
                Utility.log("SCUR:%s", c.ToString(dst.AcqInfos[i].CurList.CurList[j]));
            }
        }
    }

    public static AcqInfo GetVTermAcqInfo()
    {
        return GetVTermPrms().AcqInfos[SelectedAcqIdx];
    }

    public static AcqInfo GetVTermAcqInfoByIdx(int idx)
    {
        int i = 0;
        VTerm tmp = GetVTermPrms();

        if(idx < tmp.AcqInfoLen)
            return tmp.AcqInfos[idx];

        return null;
    }

    public static AcqInfo GetVTermAcqInfoByAcqName(byte[] acqName)
    {
        int i = 0;
        VTerm tmp = GetVTermPrms();

        for(i = 0; i < tmp.AcqInfoLen; i++)
        {
            if(!c.strcmp(tmp.AcqInfos[i].AcqName, acqName))
                break;
        }

        return tmp.AcqInfos[i];
    }

    public static AcqInfo GetVTermAcqInfoByAcqId(final byte[] acqId, byte[] termId)
    {
        int i = 0;
        VTerm tmp = GetVTermPrms();

        for(i = 0; i < tmp.AcqInfoLen; i++)
        {
            if(!memcmp(tmp.AcqInfos[i].AcqId, acqId, 2) && !memcmp(tmp.AcqInfos[i].TermId, termId, 8))
                break;
        }

        return tmp.AcqInfos[i];
    }

    public static AcqInfo GetVTermAcqInfoByMenuStr(final byte[] menuStr)
    {
        int i = 0;
        VTerm tmp = GetVTermPrms();

        for(i = 0; i < tmp.AcqInfoLen; i++)
        {
            if(!c.strcmp(tmp.AcqInfos[i].MenuStr, menuStr))
                break;
        }

        return tmp.AcqInfos[i];
    }

    public static int SelVTermAcqInfoByIdx(final int idx)
    {
        int i = 0;
        VTerm tmp = GetVTermPrms();

        if(idx < tmp.AcqInfoLen)
        {
            SelectedAcqIdx = idx;
        }

        return SelectedAcqIdx;
    }

    public static int SelVTermAcqInfoByAcqName(final byte[] acqName)
    {
        int i = 0;
        VTerm tmp = GetVTermPrms();

        for(i = 0; i < tmp.AcqInfoLen; i++)
        {
            if(!c.strcmp(tmp.AcqInfos[i].AcqName, acqName))
                break;
        }

        SelectedAcqIdx = i;
        return SelectedAcqIdx;
    }

    public static int SelVTermAcqInfoByAcqId(final byte[] acqId, byte[] termId)
    {
        int i = 0;
        VTerm tmp = GetVTermPrms();

        for(i = 0; i < tmp.AcqInfoLen; i++)
        {
            if(!memcmp(tmp.AcqInfos[i].AcqId, acqId, 2) && !memcmp(tmp.AcqInfos[i].TermId, termId, 8))
                break;
        }

        SelectedAcqIdx = i;
        return SelectedAcqIdx;
    }

    public static int SelVTermAcqInfoByMenuStr(final byte[] menuStr)
    {
        int i = 0;
        VTerm tmp = GetVTermPrms();

        for(i = 0; i < tmp.AcqInfoLen; i++)
        {
            if(!c.strcmp(tmp.AcqInfos[i].MenuStr, menuStr))
                break;
        }

        SelectedAcqIdx = i;
        return SelectedAcqIdx;
    }

    public static boolean IsVTermExist()
    {
        return file.Exist(VTerm.PRMFILE);
    }

    public static boolean IsPinBypass(AcqInfo acqInfo)
    {
        AcqInfo tmp = acqInfo;
        if(tmp == null)
        {
            tmp = GetVTermPrms().AcqInfos[SelectedAcqIdx];
        }

        return (tmp.Permissions[0] & 0x01) != 0;
    }

    public static boolean IsContactEMVOffline(AcqInfo acqInfo)
    {
        AcqInfo tmp = acqInfo;
        if(tmp == null)
        {
            tmp = GetVTermPrms().AcqInfos[SelectedAcqIdx];
        }

        return (tmp.Permissions[0] & 0x02) != 0;
    }

    public static boolean IsFallbackTransaction(AcqInfo acqInfo)
    {
        AcqInfo tmp = acqInfo;
        //EP_printf("SelectedAcqIdx:%d", SelectedAcqIdx);

        if(tmp == null)
        {
            tmp = GetVTermPrms().AcqInfos[SelectedAcqIdx];
        }
        //EP_printf("SelectedAcq:%02X%02X", tmp.AcqId[0], tmp.AcqId[1]);
        return (tmp.Permissions[0] & 0x04) != 0;
    }

    public static boolean IsManuelKeyEntry(AcqInfo acqInfo)
    {
        AcqInfo tmp = acqInfo;
        if(tmp == null)
        {
            tmp = GetVTermPrms().AcqInfos[SelectedAcqIdx];
        }
        return (tmp.Permissions[0] & 0x08) != 0;
    }

    public static boolean IsEMVPinBlock(AcqInfo acqInfo)
    {
        AcqInfo tmp = acqInfo;
        if(tmp == null)
        {
            tmp = GetVTermPrms().AcqInfos[SelectedAcqIdx];
        }
        return (tmp.Permissions[0] & 0x10) != 0;
    }

    public static boolean IsEmvPuanKullanim(AcqInfo acqInfo)
    {
        AcqInfo tmp = acqInfo;
        if(tmp == null)
        {
            tmp = GetVTermPrms().AcqInfos[SelectedAcqIdx];
        }

        return (tmp.Permissions[0] & 0x20) != 0;
    }

    public static boolean IsPuanSatisTaksit(AcqInfo acqInfo)
    {
        AcqInfo tmp = acqInfo;
        if(tmp == null)
        {
            tmp = GetVTermPrms().AcqInfos[SelectedAcqIdx];
        }

        return (tmp.Permissions[0] & 0x40) != 0;
    }

    // 11.7.3 İzinler 4 (İşlem İzinleri 2)
    public static boolean IsOnlineImprinter(AcqInfo acqInfo)
    {
        AcqInfo tmp = acqInfo;
        if(tmp == null)
        {
            tmp = GetVTermPrms().AcqInfos[SelectedAcqIdx];
        }

        return (tmp.Permissions[3] & 0x01) != 0;
    }
    public static boolean IsVadeFarkliTaksit(AcqInfo acqInfo)
    {
        AcqInfo tmp = acqInfo;
        if(tmp == null)
        {
            tmp = GetVTermPrms().AcqInfos[SelectedAcqIdx];
        }

        return (tmp.Permissions[3] & 0x04) != 0;
    }
    public static boolean IsVadeFarkliTaksitSorgu(AcqInfo acqInfo)
    {
        AcqInfo tmp = acqInfo;
        if(tmp == null)
        {
            tmp = GetVTermPrms().AcqInfos[SelectedAcqIdx];
        }

        return (tmp.Permissions[3] & 0x08) != 0;
    }
    public static boolean IsKareKodSupport(AcqInfo acqInfo)
    {
        AcqInfo tmp = acqInfo;
        if(tmp == null)
        {
            tmp = GetVTermPrms().AcqInfos[SelectedAcqIdx];
        }

        return (tmp.Permissions[3] & 0x10) != 0;
    }
    public static boolean KareKodTipi(AcqInfo acqInfo)
    {
        AcqInfo tmp = acqInfo;
        if(tmp == null)
        {
            tmp = GetVTermPrms().AcqInfos[SelectedAcqIdx];
        }

        return (tmp.Permissions[3] & 0x20) != 0;
    }

    public static boolean _8haneBINdestegi()
    {
        return false;//BIN_BYTE_COUNT >= 4;
    }


    public static boolean IsAnyAcqSupportAnyTran()
    {
        int i = 0;
        boolean rv = false;

        for(i = 0; i < GetVTermPrms().AcqInfoLen; i++)
        {
            rv = IsAcqSupportAnyTran(GetVTermPrms().AcqInfos[i]);
            if(rv) break;
        }
        return rv;
    }

    public static boolean IsAnyAcqSupportTran(int trnCode)
    {
        int i = 0;
        boolean rv = false;

        for(i = 0; i < GetVTermPrms().AcqInfoLen; i++)
        {
            rv = IsAcqSupportTran(trnCode, GetVTermPrms().AcqInfos[i]);
            if(rv)  break;
        }
        return rv;
    }

    public static boolean IsAcqSupportAnyTran(AcqInfo acqInfo)
    {
        AcqInfo tmp = acqInfo;
        if(tmp == null)
        {
            tmp = GetVTermPrms().AcqInfos[SelectedAcqIdx];
        }

        if(tmp.Permissions[2] != 0)
            return true;

        if((tmp.Permissions[3] & 0x01)  != 0)
            return true;
        return false;
    }

    public static boolean IsAcqSupportTran(int trnCode, AcqInfo acqInfo)
    {
        AcqInfo tmp = acqInfo;
        if(tmp == null)
        {
            tmp = GetVTermPrms().AcqInfos[SelectedAcqIdx];
        }

        switch(trnCode)
        {
            case TranStruct.T_SALE:
                return (tmp.Permissions[2] & 0x01) != 0;
            case TranStruct.T_REFUNDCONTROLED:
                return (tmp.Permissions[2] & 0x02) != 0;
            case TranStruct.T_REFUND:
                return (tmp.Permissions[2] & 0x04) != 0;
            case TranStruct.T_PREAUTHOPEN:
                return (tmp.Permissions[2] & 0x08) != 0;
            case TranStruct.T_PREAUTHCLOSE:
                return (tmp.Permissions[2] & 0x10) != 0;
            case TranStruct.T_LOYALTYINSTALLMENT:
                return (tmp.Permissions[2] & 0x20) != 0;
            case TranStruct.T_LOYALTYBONUSINQUIRY:
                return (tmp.Permissions[2] & 0x40) != 0;
            case TranStruct.T_LOYALTYBONUSSPEND:
                return (tmp.Permissions[2] & 0x80) != 0;
            case TranStruct.T_ONLINEIMPRINTER:
                return (tmp.Permissions[3] & 0x01) != 0;
            case TranStruct.T_VOID:
                return (tmp.Permissions[3] & 0x02) != 0;
            case TranStruct.T_VADE_FARKLI_TAKSIT:
                return (tmp.Permissions[3] & 0x04) != 0;
            case TranStruct.T_VADE_FARKLI_TAKSIT_SORGU:
                return (tmp.Permissions[3] & 0x08) != 0;
            case TranStruct.T_KAREKOD:
                return (tmp.Permissions[3] & 0x10) != 0;
        }

        return false;
    }

    public static boolean IsAcqSupportAID(byte[] aid, AcqInfo acqInfo)
    {
        int i = 0;
        AcqInfo tmp = acqInfo;

        if(tmp == null)
        {
            tmp = GetVTermPrms().AcqInfos[SelectedAcqIdx];
        }

        for(i = 0; i < tmp.AIDList.AIDCnt; i++)
        {
            // EP_printf("IsAcqSupportAID:%s - %s", c.ToString(aid), c.ToString(tmp.AIDList.AIDList[i]));
            if(c.strstr(aid, tmp.AIDList.AIDList[i]))
            {
                log("IsAcqSupportAID:%s - %s SUPPORTED", c.ToString(tmp.AcqName), c.ToString(aid));
                return true;
            }
        }

        log("IsAcqSupportAID:%s - %s NOT SUPPORTED", c.ToString(tmp.AcqName), c.ToString(aid));
        return false;
    }

    public static boolean IsAcqSupportAnyAID(AcqInfo acqInfo)
    {
        int i = 0;
        AcqInfo tmp = acqInfo;

        if(tmp == null)
        {
            tmp = GetVTermPrms().AcqInfos[SelectedAcqIdx];
        }

        if(tmp.AIDList.AIDCnt > 0)
        {
            //EP_printf("IsAcqSupportAnyAID:%s SUPPORTED", c.ToString(tmp.AcqName));
            return true;
        }
        //EP_printf("IsAcqSupportAnyAID:%s NOT SUPPORTED", c.ToString(tmp.AcqName));
        return false;
    }

    public static boolean IsAcqSupportClssAID(byte[] aid, AcqInfo acqInfo)
    {
        int i = 0;
        AcqInfo tmp = acqInfo;
        if(tmp == null)
        {
            tmp = GetVTermPrms().AcqInfos[SelectedAcqIdx];
        }

        for(i = 0; i < tmp.ClssAIDList.AIDCnt; i++)
        {
            log("IsAcqSupportClssAID:%s - %s", c.ToString(aid), c.ToString(tmp.AIDList.AIDList[i]));
            if(c.strstr(aid, tmp.ClssAIDList.AIDList[i]))
            {
                //EP_printf("IsAcqSupportClssAID:%s - %s SUPPORTED", c.ToString(tmp.AcqName), c.ToString(aid));
                return true;
            }
        }

        //EP_printf("IsAcqSupportClssAID:%s - %s NOT SUPPORTED", c.ToString(tmp.AcqName), c.ToString(aid));
        return false;
    }

    public static boolean IsAcqSupportClssAnyAID(AcqInfo acqInfo)
    {
        int i = 0;
        AcqInfo tmp = acqInfo;
        if(tmp == null)
        {
            tmp = GetVTermPrms().AcqInfos[SelectedAcqIdx];
        }

        if(tmp.ClssAIDList.AIDCnt > 0)
        {
            //EP_printf("IsAcqSupportClssAnyAID:%s SUPPORTED", c.ToString(tmp.AcqName));
            return true;
        }
        //EP_printf("IsAcqSupportClssAnyAID:%s NOT SUPPORTED", c.ToString(tmp.AcqName));
        return false;
    }

    public static boolean IsAnyAcqSupportClssAnyAID()
    {
        int i = 0;
        AcqInfo tmp;

        for (i = 0; i < GetVTermPrms().AcqInfoLen; i++)
        {
            tmp = GetVTermPrms().AcqInfos[i];
            if(tmp.ClssAIDList.AIDCnt > 0)
            {
                //EP_printf("IsAnyAcqSupportClssAnyAID:%s SUPPORTED", c.ToString(tmp.AcqName));
                return true;
            }
        }

        //EP_printf("IsAnyAcqSupportClssAnyAID:%s NOT SUPPORTED", c.ToString(tmp.AcqName));

        return false;
    }
//
//    void SaveVtermForTsm()
//    {
//        int i = 0;
//        AcqInfo *AcqList[32] = {0};
//        EP_AcqInfo acq[32];
//        unsigned int AcqCnt = 0;
//        memset(acq, 0, sizeof(EP_AcqInfo)*32);
//
//        AcqCnt = GetVTermPrms().AcqInfoLen;
//        for(i = 0; i < AcqCnt; i++)
//        {
//            AcqList[i] = &GetVTermPrms().AcqInfos[i];
//
//            sprintf(acq[i].AcqId, "%02X%02X", AcqList[i].AcqId[0], AcqList[i].AcqId[1]);
//            strcpy(acq[i].AcqName, AcqList[i].AcqName);
//            strcpy(acq[i].TermId, AcqList[i].TermId);
//            strcpy(acq[i].MercId, AcqList[i].MercId);
//
//            EP_OpAcqList(&acq[i], 1);
//        }
//    }
//
//    void DeleteVtermForTsm()
//    {
//        int i = 0;
//        VTerm tmp = GetVTermPrms();
//        EP_AcqInfo acq[32];
//
//        memset(acq, 0, c.sizeof(EP_AcqInfo)*32);
//        for(i = 0; i < tmp.AcqInfoLen; i++)
//        {
//            memcpy(&acq[i].AcqId, tmp.AcqInfos[i].AcqId, 2);
//            EP_OpAcqList(&acq[i], 0);
//        }
//    }
}
