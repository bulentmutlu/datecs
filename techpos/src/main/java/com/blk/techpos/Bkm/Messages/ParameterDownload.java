package com.blk.techpos.Bkm.Messages;

import static com.blk.sdk.Convert.SWAP_UINT16;
import static com.blk.sdk.Convert.SWAP_UINT32;
import static com.blk.sdk.c.memcpy;
import static com.blk.sdk.c.memset;
import static com.blk.techpos.Bkm.TranStruct.currentTran;
import static com.blk.techpos.PrmStruct.params;

import android.util.Log;

import com.blk.platform.Emv.CAPublicKey;
import com.blk.platform.Emv.EmvApp;
import com.blk.platform.IPlatform;
import com.blk.sdk.Convert;
import com.blk.sdk.Iso8583;
import com.blk.sdk.UI;
import com.blk.sdk.c;
import com.blk.sdk.file;
import com.blk.sdk.olib.olib;
import com.blk.techpos.Bkm.Batch;
import com.blk.techpos.Bkm.Bkm;
import com.blk.techpos.Bkm.TranStruct;
import com.blk.techpos.Bkm.VParams.PrmFileHeader;
import com.blk.techpos.Bkm.VParams.VBin;
import com.blk.techpos.Bkm.VParams.VEod;
import com.blk.techpos.Bkm.VParams.VSpecialBin;
import com.blk.techpos.Bkm.VParams.VTerm;
import com.blk.techpos.Print;
import com.blk.techpos.PrmStruct;
import com.blk.techpos.techpos;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
/**
 * Created by id on 14.03.2018.
 */

public class ParameterDownload {
    private static final String TAG = ParameterDownload.class.getSimpleName();

    public static int Download() throws NoSuchFieldException, IllegalAccessException, IOException, Exception, InterruptedException {

//        String input;
//
//        if ((input = UI.GetIP("İP GİRİNİZ", params.sTcpip.destIp)) == null)
//            return -1;
//        params.sTcpip.destIp = input;
//        params.Save("sTcpip.destIp");
//
//        if ((input = UI.GetNumber("PORT GİRİNİZ", "" + params.sTcpip.destPort, 2, 5, false, 30, false)) == null)
//            return -1;
//        params.sTcpip.destPort = c.atoi(input.getBytes());
//        params.Save("sTcpip.destPort");
//
//        if ((input = UI.GetIP("YEDEK İP GİRİNİZ", params.sBackupTcpip.destIp)) == null)
//            return -1;
//        params.sBackupTcpip.destIp = input;
//        params.Save("sBackupTcpip.destIp");
//
//        if ((input = UI.GetNumber("YEDEK PORT GİRİNİZ", "" + params.sBackupTcpip.destPort, 2, 5, false, 30, false)) == null)
//            return -1;
//        params.sBackupTcpip.destPort = c.atoi(input.getBytes());
//        params.Save("sBackupTcpip.destPort");


        if(Batch.GetTranCount() > 0)
        {
            UI.ShowMessage(2000, "GÜNSONU YAPINIZ");
            return -1;
        }

        params.BkmParamStatus = 0;
        params.Save("BkmParamStatus");


        try {
            if(KeyExchange.ProcessKeyExchange() == 0)
            {
                if(ParameterDownload.ProcessDownloadPrms() == 0)
                {
                    params.BkmParamStatus = 1;
                    params.Save("BkmParamStatus");
                    UI.ShowMessage(1000, "PARAMETRE YÜKLEME\nBAŞARILI");
                }
            }
            params.Save();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Print.PrintAppPrms(params.BkmParamStatus);

        UI.UiUtil.ShowMessageHide();

        return 0;
    }

    static byte[] GetEmvKernelListData()
    {
        byte[] emvKernelList = new byte[128];
        int idx = 1;
        byte emvKernelCnt = 3;


        emvKernelList[idx++] = 0x00;
        emvKernelList[idx++] = 0x00;
        memcpy(emvKernelList, idx, new byte[] {0x00, 0x06, 0x00, 0x02}, 0, 4);
        idx += 4;

        emvKernelList[idx++] = 0x01;
        emvKernelList[idx++] = 0x01;
        memcpy(emvKernelList, idx, new byte[] {0x00, 0x03, 0x00, 0x01}, 0, 4);
        //memcpy(&emvKernelList[idx], "\x00\x04\x05\x00", 4);
        idx += 4;

        emvKernelList[idx++] = 0x01;
        emvKernelList[idx++] = 0x02;
        memcpy(emvKernelList, idx, new byte[] {0x00, 0x02, 0x00, 0x00}, 0, 4);
        //memcpy(&emvKernelList[idx], "\x00\x03\x00\x05", 4);
        idx += 4;

        emvKernelList[0] = emvKernelCnt;
        return Arrays.copyOf(emvKernelList, idx);
    }

    public static int ProcessDownloadPrms() throws Exception {
        int rv = -1, fd = -1, retVal = -1, tmpFd = -1, vtermUpdated = 0;
        short prmFileNameLen = 0;
        PrmFileHeader hdr;
        int newStan, newBatchNo;
        String tempParamFile = "VPRMS";

        Log.i(TAG,"ProcessDownloadPrms Started");

        TranStruct.ClearTranData();

        file.Remove(tempParamFile);
        file vprmsFile = new file(tempParamFile);

        do
        {
            currentTran.MsgTypeId = 800;
            if(currentTran.ProcessingCode <= 0)
                currentTran.ProcessingCode = 900000;

            currentTran.PrmPackLen = 0;

            rv = Msgs.ProcessMsg(Msgs.MessageType.M_PRMDOWNLOAD);
            if (rv == 0 && !currentTran.f39OK()) {
                rv = -1;
            }
            if(rv == 0)
            {
                vprmsFile.Write(currentTran.PrmPack, 0, currentTran.PrmPackLen); // EP_FileWrite(fd, currentTran.PrmPack, currentTran.PrmPackLen);

                if(currentTran.PackSize <= (currentTran.Offset + currentTran.PrmPackLen))
                {
                    retVal = 0;
                    break;
                }
            }
            else
            {
                UI.ShowMessage(2000, "\nCEVAP ALINAMADI\nTEKRAR DENEYİNİZ");
                break;
            }
        }while(currentTran.ProcessingCode == 900001);
        vprmsFile.Close();

        vprmsFile = new file(tempParamFile, file.OpenMode.RDONLY);

        if(retVal == 0)
        {
            if(vprmsFile.Size() >= 14)
            {
                vprmsFile.Seek(0, file.SeekMode.SEEK_SET);
                do
                {
                    hdr = new PrmFileHeader();

                    hdr.PrmType = SWAP_UINT16(vprmsFile.ReadShort());
                    if(hdr.PrmType == 1) //VTERM UPDATED
                        vtermUpdated = 1;

                    vprmsFile.Seek(1, file.SeekMode.SEEK_CUR);

                    prmFileNameLen = SWAP_UINT16(vprmsFile.ReadShort());
                    vprmsFile.Read(hdr.PrmName, 0, prmFileNameLen);
                    vprmsFile.Read(hdr.PrmVer, 0, 4);
                    hdr.PrmLen = SWAP_UINT32(vprmsFile.ReadInt());

                    hdr.Log();

                    String tmpFN = new String(hdr.PrmName, 0, c.strlen(hdr.PrmName));
                    if(hdr.PrmType == 2 || hdr.PrmType == 11) // 2 VBIN, 11 VSpecialBIN
                        tmpFN += "NEW";

                    file.Remove(tmpFN);
                    if(hdr.PrmLen > 0)
                    {
                        Log.i(TAG, "PARAMFILE : " + tmpFN + " PrmLen : " + hdr.PrmLen);
                        file tempFile = new file(tmpFN);
                        //hdr.WriteToFile(tempFile);
                        olib.WriteFile(hdr, tempFile);

                        byte[] params = vprmsFile.Read(hdr.PrmLen);
                        //Log.i(TAG, "PARAMS : " + Convert.Buffer2Hex(params));

                        tempFile.Write(params);
                        //tempFile.Write(vprmsFile.Read(hdr.PrmLen));
                        tempFile.Close();
                    }

                    rv = vprmsFile.Seek(0, file.SeekMode.SEEK_CUR);
                }while(rv < currentTran.PackSize);
            }
            file.Remove(tempParamFile);

            VTerm.ReadVTermPrms(null);
            VBin.ReadVBinPrms(null);
            VSpecialBin.ReadVSpecialBinPrms(null);
            VEod.ReadVEodPrms(null);

            HashMap<String, List<CAPublicKey>> keys = Bkm.ParseKeys();
            EmvApp[] p1 = Bkm.ParseConfig(Bkm.file_VEMVCLSPP3, (byte) 2);
            EmvApp[] p2 = Bkm.ParseConfig(Bkm.file_VEMVCLSPW2,(byte) 3);
            EmvApp[] ctlsApps = new EmvApp[p1.length + p2.length];
            System.arraycopy(p1, 0, ctlsApps, 0, p1.length);
            System.arraycopy(p2, 0, ctlsApps, p1.length, p2.length);

            IPlatform.get().emv.Init(keys, Bkm.ParseConfig(Bkm.file_VEMVCONFIG, (byte) 0));
            IPlatform.get().emvcl.Init(keys, ctlsApps);

            if(vtermUpdated != 0)
            {
                byte[] tmpStr= new byte[32];
                memset(tmpStr, (byte) 0, c.sizeof(tmpStr));
                Convert.EP_bcd2str(tmpStr, VTerm.GetVTermPrms().FirstStan,3);
                newStan = c.atoi(tmpStr);

                memset(tmpStr, (byte) 0, c.sizeof(tmpStr));
                Convert.EP_bcd2str(tmpStr, VTerm.GetVTermPrms().FirstBatchNo,3);
                newBatchNo = c.atoi(tmpStr);

                Log.i(TAG, String.format("Cur Stan:%d", params.Stan)); 
                Log.i(TAG, String.format("Cur Batch:%d",params.BatchNo));

                Log.i(TAG, String.format("New Stan:%d",newStan));
                Log.i(TAG, String.format("New Batch:%d",newBatchNo));

                if(newStan > 0)
                    newStan--;

                params.Stan = newStan;
                if(params.BatchNo < newBatchNo)
                {
                    Log.i(TAG, String.format("params.BatchNo:%d newBatchNo:%d",params.BatchNo, newBatchNo));

                    if(params.BatchNo > 0)
                    {
                        EndOfDay.ProcessEndOfDay(true);
                    }

                    params.BatchNo = newBatchNo;
                    params.TranNo = 0;

                    Msgs.ProcessSignals(1);
                }

            }
            params.BkmParamStatus = 1;
            HandShake.ProcessHandShake(1);
        }

        PrmStruct.Save();
        Log.i(TAG, String.format("ProcessDownloadPrms Ended:%d", retVal));
        return retVal;
    }

    public static int PreparePrmDownloadMsg(Iso8583 entity) throws Exception {
        int rv = 0;
        short tmpLen = 0;
        byte[] buff = new byte[1024];
        int buffIdx = 0, tmpInt = 0;

        if(currentTran.ProcessingCode == 900000)
        {
            buffIdx = 0;
            byte[] prmListData = Msgs.GetPrmListData();
            short len = (short) prmListData.length;
            buff[0] = 0x07;
            tmpLen = Convert.SWAP_UINT16(len);
            memcpy(buff, 1, Convert.ToArray(tmpLen), 0, 2);
            memcpy(buff, 3, prmListData, 0, len);
            buffIdx += len + 3;

            memcpy(buff, buffIdx, new byte[] {0x0D, 0x00, 0x15}, 0, 3);           buffIdx += 3;
            memcpy(buff, buffIdx, params.TCKN.getBytes(), 0, 11);  buffIdx += 11;
            memcpy(buff, buffIdx, params.VN.getBytes(), 0, 10);    buffIdx += 10;
            memcpy(buff, buffIdx, new byte[] {0x0F, 0x00, 0x03}, 0, 3);           buffIdx += 3;
            memcpy(buff, buffIdx, params.CommType, 0, 3);          buffIdx += 3;

            prmListData = GetEmvKernelListData();
            len = (short) prmListData.length;
            buff[buffIdx++] = 0x13;
            tmpLen = Convert.SWAP_UINT16(len);
            memcpy(buff, buffIdx, Convert.ToArray(tmpLen), 0, 2);         buffIdx += 2;
            memcpy(buff, buffIdx, prmListData, 0, len);                                  buffIdx += len;
            memcpy(buff, buffIdx, new byte[] {0x15, 0x00, 0x01}, 0, 3);           buffIdx += 3;
            buff[buffIdx++] = (byte) params.GprsNetType;
            memcpy(buff, buffIdx, new byte[] {0x18, 0x00, 0x13, 0x00, 0x00, 0x00, 0x20, 0x20, 0x20, 0x20,
                    0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20}, 0, 22); buffIdx += 22;

            // Tag 0x23: Terminal Yetenekleri (Terminalin Faz 2 Kapsamında Desteklediği Özellikler) Uzunluk (Len): 5 byte
            buff[buffIdx++] = 0x23;
            buff[buffIdx++] = 0x00;
            buff[buffIdx++] = 0x05;
            memcpy(buff, buffIdx, QR.Field23(), 0, 5);            buffIdx += 5;

            entity.setFieldBin("63", Arrays.copyOf(buff, buffIdx));
        }
        else if(currentTran.ProcessingCode == 900001)
        {
            entity.setFieldValue("37", currentTran.RRN);

            buffIdx = 0;
            memcpy(buff, buffIdx, new byte[] {0x09, 0x00, 0x04}, 0, 3);       buffIdx += 3;
            tmpInt = Convert.SWAP_UINT32(currentTran.Offset);
            memcpy(buff, buffIdx, Convert.ToArray(tmpInt), 0, 4);       buffIdx += 4;

            entity.setFieldBin("63", Arrays.copyOf(buff, buffIdx));
        }

        return rv;
    }

    public static int ParsePrmDownloadMsg(HashMap<String, byte[]> isoMsg)
    {
        int rv = 0;
        byte[] tlvData = new byte[1024];

        if(isoMsg.containsKey("62")) {
            int len = isoMsg.get("62").length;
            byte[] tmpBuff = isoMsg.get("62");

            memcpy(currentTran.PrmPack, tmpBuff, len);
            currentTran.PrmPackLen = len;
        }

        if(isoMsg.containsKey("63")) {
            int len = isoMsg.get("63").length;
            byte[] tmpBuff = isoMsg.get("63");

            if(techpos.GetTLVData(tmpBuff, len, 0x08, tlvData, (short) c.sizeof(tlvData)) > 0)
            {
                currentTran.CompressionType = tlvData[0];
                currentTran.Offset = Convert.ToInt(tlvData, 1);
                currentTran.PackSize= Convert.ToInt(tlvData, 5);
                memcpy(currentTran.PackCrc, 0, tlvData, 9, 4);

                currentTran.Offset = Convert.SWAP_UINT32(currentTran.Offset);
                currentTran.PackSize = Convert.SWAP_UINT32(currentTran.PackSize);
            }

            if(techpos.GetTLVData(tmpBuff, len, 0x12, tlvData, (short) c.sizeof(tlvData)) > 0)
            {
                params.KeyExchangeFlag = (tlvData[0] + 1) % 4;
            }
        }

	/*
	memcpy(currentTran.PrmPack, "\x00\x01\x00\x00\x05\x56\x54\x45\x52\x4D\x00\x00\x00\x0C\x00\x00\x03\x1C\x30\x30\x30\x30\x30\x30\x30\x30\x00\x00\x17\x00\x00\x52\x00\x09\x01\x00\x02\x03\x01\x02\x00\x01\xFF\x02\x01\x11\x0A\x46\x69\x6E\x61\x6E\x73\x62\x61\x6E\x6B\x50\x53\x30\x30\x33\x38\x38\x38\x30\x39\x30\x32\x30\x30\x30\x30\x30\x30\x30\x32\x35\x35\x37\x00\x0A\x46\xDD\x4E\x41\x4E\x53\x42\x41\x4E\x4B\x46\x4E\x0A\x6B\x69\x72\x61\x20\x6F\x64\x65\x6D\x65\x3A\x67\x67\x67\x67\x67\x67\x67\x67\x67\x67\x67\x67\x67\x67\x67\x20\x61\x61\x61\x61\x61\x61\x20\x64\x0A\x64\x64\x64\x64\x64\x64\x64\x64\x64\x64\x0A\x0A\x41\x4E\x54\x41\x4C\x59\x41\x2D\x28\x32\x34\x32\x29\x20\x34\x34\x34\x34\x34\x34\x34\x07\x41\x4E\x54\x41\x4C\x59\x41\x32\x34\x32\x34\x34\x34\x34\x34\x34\x34\x35\x30\x37\x35\x52\x32\x34\x32\x2D\x34\x34\x34\x34\x34\x34\x38\x35\x30\x32\x32\x32\x31\x39\x30\x30\x33\x38\x31\x37\x33\x36\x33\x38\x38\x37\x34\x31\x32\x33\x31\x32\x33\x31\x32\x33\x31\x13\x54\x55\x5A\x4C\x41\x20\x56\x45\x52\x47\xDD\x20\x44\x41\xDD\x52\x45\x53\xDD\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x39\x34\x39\x0F\x00\xEF\x03\x00\x00\x00\x00\x00\x00\x00\x66\x01\x00\x29\x05\x07\xA0\x00\x00\x00\x03\x10\x10\x07\xA0\x00\x00\x00\x03\x20\x10\x07\xA0\x00\x00\x00\x03\x20\x20\x07\xA0\x00\x00\x00\x04\x10\x10\x07\xA0\x00\x00\x00\x04\x30\x60\x02\x00\x21\x04\x07\xA0\x00\x00\x00\x03\x10\x10\x07\xA0\x00\x00\x00\x03\x20\x10\x07\xA0\x00\x00\x00\x04\x10\x10\x07\xA0\x00\x00\x00\x04\x30\x60\x03\x00\x0C\x42\x4B\x4D\x20\x44\x45\x4E\x45\x4D\x45\x20\x35\x05\x00\x04\x01\x39\x34\x39\x00\x01\x11\x54\x43\x20\x4D\x65\x72\x6B\x65\x7A\x20\x42\x61\x6E\x6B\x61\x73\xFD\x30\x30\x30\x30\x31\x31\x32\x39\x30\x30\x30\x30\x30\x30\x30\x30\x30\x30\x30\x30\x30\x30\x32\x00\x0F\x47\x4F\x4B\x54\x55\x52\x4B\x20\x20\x20\x20\x42\x41\x4E\x4B\x41\x42\x09\x54\x65\x73\x74\x20\x54\x65\x73\x74\x4B\x49\x73\x79\x65\x72\x69\x41\x64\x72\x65\x73\x53\x61\x74\x69\x72\x69\x31\x0A\x49\x73\x79\x65\x72\x69\x41\x64\x72\x65\x73\x53\x61\x74\x69\x72\x69\x32\x0A\x49\x73\x79\x65\x72\x69\x41\x64\x72\x65\x73\x53\x61\x74\x69\x72\x69\x33\x0A\x49\x73\x79\x65\x72\x69\x41\x64\x72\x65\x73\x53\x61\x74\x69\x72\x69\x34\x08\xDD\x73\x74\x61\x6E\x62\x75\x6C\x32\x31\x32\x35\x35\x35\x36\x36\x38\x38\x30\x37\x34\x32\x52\x32\x31\x32\x35\x35\x35\x36\x36\x38\x38\x32\x31\x32\x35\x35\x35\x36\x36\x38\x38\x20\x20\x20\x20\x20\x20\x20\x20\x20\x20\x20\x31\x32\x33\x34\x35\x36\x37\x38\x39\x30\x09\x54\x45\x53\x54\x20\x54\x45\x53\x54\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x39\x34\x39\x11\x00\x3F\x03\x00\x00\x00\x00\x00\x00\x00\xB9\x01\x00\x62\x0C\x07\xA0\x00\x00\x00\x03\x10\x10\x07\xA0\x00\x00\x00\x03\x20\x10\x07\xA0\x00\x00\x00\x03\x20\x20\x07\xA0\x00\x00\x00\x03\x80\x10\x07\xA0\x00\x00\x00\x04\x10\x10\x07\xA0\x00\x00\x00\x04\x30\x60\x05\xA0\x00\x00\x00\x25\x07\xA0\x00\x00\x00\x65\x10\x10\x07\xA0\x00\x00\x01\x52\x30\x10\x08\xA0\x00\x00\x03\x33\x01\x01\x01\x08\xA0\x00\x00\x03\x33\x01\x01\x02\x08\xA0\x00\x00\x03\x33\x01\x01\x03\x02\x00\x31\x06\x07\xA0\x00\x00\x00\x03\x10\x10\x07\xA0\x00\x00\x00\x03\x20\x10\x07\xA0\x00\x00\x00\x03\x20\x20\x07\xA0\x00\x00\x00\x03\x80\x10\x07\xA0\x00\x00\x00\x04\x10\x10\x07\xA0\x00\x00\x00\x04\x30\x60\x03\x00\x16\x40\x40\x40\x20\x47\x55\x4E\x53\x4F\x4E\x55\x20\x4D\x45\x53\x41\x4A\x49\x20\x40\x40\x40\x05\x00\x04\x01\x39\x34\x39", 814);
	currentTran.PrmPackLen = 814;

	currentTran.CompressionType = 0;
	currentTran.Offset = 0;
	currentTran.PackSize = 814;
	*/
        return rv;
    }

}
