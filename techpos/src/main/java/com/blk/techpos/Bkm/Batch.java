package com.blk.techpos.Bkm;

import static com.blk.sdk.c.memcmp;
import static com.blk.sdk.c.strcpy;
import static com.blk.techpos.Bkm.TranStruct.currentTran;
import static com.blk.techpos.PrmStruct.params;

import android.util.Log;

import com.blk.sdk.file;
import com.blk.sdk.Utility;
import com.blk.sdk.olib.olib;
import com.blk.sdk.c;
import com.blk.techpos.Bkm.VParams.VEod;
import com.blk.techpos.Bkm.VParams.VTerm;
import com.blk.techpos.PrmStruct;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by id on 23.02.2018.
 */

public class Batch {

    private static final String TAG = Batch.class.getSimpleName();
    static String BATCH_FN	 = Utility.filesPath + "/BATCH";
    static String LASTTRN_FN = Utility.filesPath + "/LSTTRN";
    static String LASTBTCH_FN = Utility.filesPath + "/LSTBTCH";
    static String AUTOBTCH_FN = Utility.filesPath + "/AUTOBTCH";

    static int sizeofBatchRec = 419;
    static BatchTotals batchTotal = new BatchTotals();

    public static class BatchAcqTotals {
        public int trnCnt;
        public long trnTotAmt;
        public short len;
        public int[][] tots = new int[32][2]; //0 procode 1 count
        public long[] totAmts = new long[32];

        public static final int sizeof = 526;
    }

    public static class BatchTotals {
        public int BatchNo;
        public int batchCount;
        public long batchTotAmt;
        public short len;
        public BatchAcqTotals[] acqTots = new BatchAcqTotals[32];

        public static final int sizeof = 16850;

        public BatchTotals() {
            for (int i = 0; i < acqTots.length; ++i)
                acqTots[i] = new BatchAcqTotals();
        }
    }

    public static void DeleteFiles() {
        file.Remove("BATCH");
        file.Remove("LSTTRN");
        file.Remove("LSTBTCH");
        file.Remove("AUTOBTCH");
    }

    public static void GenerateTranNos() throws NoSuchFieldException, IllegalAccessException {
        currentTran.TranNo = ++params.TranNo;
        PrmStruct.Save("TranNo");
        currentTran.TranNoLOnT = params.TranNoLOnT;
        currentTran.BatchNoLOnT = params.BatchNoLOnT;
        currentTran.TranNoLOffT = params.TranNoLOffT;
        currentTran.BatchNoLOffT = params.BatchNoLOffT;
    }

    public static int CloseBatch() {
        Reversal.RemoveReversalTran();
        RemoveBatch();

        params.BatchNo++;
        if (params.BatchNo > 999999) {
            params.BatchNo = 1;
        }
        params.TranNo = 0;

        return params.BatchNo;
    }

    public static boolean IsSettleRequired() throws Exception {
        if ((VEod.GetVEodPrms().MxTrn > 0) && (CalcBatchTotals() >= VEod.GetVEodPrms().MxTrn)) {
            return true;
        }
        return false;
    }

    public static int GetBatchTotalsTranByAcqForPrint(BatchRec rec, byte[] acqId, byte[] termId, int idx) throws Exception {
        int rv = 0, i = 0, j = 0, totalSize;
        file fd;

        try {
            totalSize = file.Size(LASTBTCH_FN);
            fd = new file(LASTBTCH_FN, file.OpenMode.RDONLY);

            for (int currentOffset = fd.Seek(BatchTotals.sizeof, file.SeekMode.SEEK_SET);
                 currentOffset < totalSize;
                 currentOffset = fd.Seek(0, file.SeekMode.SEEK_CUR)) {
                olib.ReadFile(rec, fd); // tmpRec.FromFile(fd, true);

                if (!memcmp(acqId, rec.AcqId, 2) && !memcmp(termId, rec.TermId, 8)) {
                    if (j == idx) {
                        //olib.Copy(rec, tmpRec); // tmpRec.CopyTo(rec);
                        return 0;
                    } else
                        j++;
                }

            }
            ;

            fd.Close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static BatchTotals GetBatchTotalsForPrint() {
        try {
            olib.ReadFile(batchTotal, new file(LASTBTCH_FN, file.OpenMode.RDONLY));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return batchTotal;
    }

    public static BatchTotals GetAutoBatchTotalsForPrint() {
        try {
            olib.ReadFile(batchTotal, new file(AUTOBTCH_FN, file.OpenMode.RDONLY));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return batchTotal;
    }

    public static int CopyFile(String from, String to) {
        try {
            file.WriteAllBytes(to, file.ReadAllBytes(from));
        } catch (Exception e) {
            e.printStackTrace();
            return 2;
        }

        return 1;
    }

    public static int CopyAutoFile() {
        file.Remove(AUTOBTCH_FN);
        return CopyFile(LASTBTCH_FN, AUTOBTCH_FN);
    }

    public static BatchTotals CalcBatchTotalsForPrint() throws Exception {
        int i = 0, idx = 0, rv = 0;
        long tmp, tmp1;
        long[] totAmt = new long[1];

        batchTotal = new BatchTotals();
        batchTotal.BatchNo = params.BatchNo;

        for (i = 0; i < VTerm.GetVTermPrms().AcqInfoLen; i++) {
            idx = 0;

            batchTotal.acqTots[i].tots[idx][0] = 0;
            totAmt[0] = batchTotal.acqTots[i].totAmts[idx];
            batchTotal.acqTots[i].tots[idx][1] = GetTranTotalByAcq(batchTotal.acqTots[i].tots[idx][0], VTerm.GetVTermPrms().AcqInfos[i].AcqId, VTerm.GetVTermPrms().AcqInfos[i].TermId, totAmt);
            batchTotal.acqTots[i].totAmts[idx] = totAmt[0];
            batchTotal.acqTots[i].trnCnt += batchTotal.acqTots[i].tots[idx][1];
            batchTotal.acqTots[i].trnTotAmt += batchTotal.acqTots[i].totAmts[idx];
            idx++;

            batchTotal.acqTots[i].tots[idx][0] = 1;
            totAmt[0] = batchTotal.acqTots[i].totAmts[idx];
            batchTotal.acqTots[i].tots[idx][1] = GetTranTotalByAcq(batchTotal.acqTots[i].tots[idx][0], VTerm.GetVTermPrms().AcqInfos[i].AcqId, VTerm.GetVTermPrms().AcqInfos[i].TermId, totAmt);
            batchTotal.acqTots[i].totAmts[idx] = totAmt[0];
            batchTotal.acqTots[i].trnCnt += batchTotal.acqTots[i].tots[idx][1];
            batchTotal.acqTots[i].trnTotAmt += batchTotal.acqTots[i].totAmts[idx];
            idx++;

            batchTotal.acqTots[i].tots[idx][0] = 2;
            totAmt[0] = batchTotal.acqTots[i].totAmts[idx];
            batchTotal.acqTots[i].tots[idx][1] = GetTranTotalByAcq(batchTotal.acqTots[i].tots[idx][0], VTerm.GetVTermPrms().AcqInfos[i].AcqId, VTerm.GetVTermPrms().AcqInfos[i].TermId, totAmt);
            batchTotal.acqTots[i].totAmts[idx] = totAmt[0];
            batchTotal.acqTots[i].trnCnt += batchTotal.acqTots[i].tots[idx][1];
            batchTotal.acqTots[i].trnTotAmt += batchTotal.acqTots[i].totAmts[idx];
            idx++;

            batchTotal.acqTots[i].tots[idx][0] = 3;
            totAmt[0] = batchTotal.acqTots[i].totAmts[idx];
            batchTotal.acqTots[i].tots[idx][1] = GetTranTotalByAcq(batchTotal.acqTots[i].tots[idx][0], VTerm.GetVTermPrms().AcqInfos[i].AcqId, VTerm.GetVTermPrms().AcqInfos[i].TermId, totAmt);
            batchTotal.acqTots[i].totAmts[idx] = totAmt[0];
            batchTotal.acqTots[i].trnCnt += batchTotal.acqTots[i].tots[idx][1];
            batchTotal.acqTots[i].trnTotAmt += batchTotal.acqTots[i].totAmts[idx];
            idx++;

            batchTotal.acqTots[i].tots[idx][0] = 200000;
            totAmt[0] = batchTotal.acqTots[i].totAmts[idx];
            batchTotal.acqTots[i].tots[idx][1] = GetTranTotalByAcq(batchTotal.acqTots[i].tots[idx][0], VTerm.GetVTermPrms().AcqInfos[i].AcqId, VTerm.GetVTermPrms().AcqInfos[i].TermId, totAmt);
            batchTotal.acqTots[i].totAmts[idx] = totAmt[0];
            batchTotal.acqTots[i].trnCnt += batchTotal.acqTots[i].tots[idx][1];
            batchTotal.acqTots[i].trnTotAmt += batchTotal.acqTots[i].totAmts[idx];
            idx++;

            batchTotal.acqTots[i].tots[idx][0] = 200001;
            totAmt[0] = batchTotal.acqTots[i].totAmts[idx];
            batchTotal.acqTots[i].tots[idx][1] = GetTranTotalByAcq(batchTotal.acqTots[i].tots[idx][0], VTerm.GetVTermPrms().AcqInfos[i].AcqId, VTerm.GetVTermPrms().AcqInfos[i].TermId, totAmt);
            batchTotal.acqTots[i].totAmts[idx] = totAmt[0];
            batchTotal.acqTots[i].trnCnt += batchTotal.acqTots[i].tots[idx][1];
            batchTotal.acqTots[i].trnTotAmt += batchTotal.acqTots[i].totAmts[idx];
            idx++;

            batchTotal.acqTots[i].tots[idx][0] = 300000;
            totAmt[0] = batchTotal.acqTots[i].totAmts[idx];
            batchTotal.acqTots[i].tots[idx][1] = GetTranTotalByAcq(batchTotal.acqTots[i].tots[idx][0], VTerm.GetVTermPrms().AcqInfos[i].AcqId, VTerm.GetVTermPrms().AcqInfos[i].TermId, totAmt);
            batchTotal.acqTots[i].totAmts[idx] = totAmt[0];
            batchTotal.acqTots[i].trnCnt += batchTotal.acqTots[i].tots[idx][1];
            batchTotal.acqTots[i].trnTotAmt += batchTotal.acqTots[i].totAmts[idx];
            idx++;

            batchTotal.acqTots[i].tots[idx][0] = 300001;
            totAmt[0] = batchTotal.acqTots[i].totAmts[idx];
            batchTotal.acqTots[i].tots[idx][1] = GetTranTotalByAcq(batchTotal.acqTots[i].tots[idx][0], VTerm.GetVTermPrms().AcqInfos[i].AcqId, VTerm.GetVTermPrms().AcqInfos[i].TermId, totAmt);
            batchTotal.acqTots[i].totAmts[idx] = totAmt[0];
            batchTotal.acqTots[i].trnCnt += batchTotal.acqTots[i].tots[idx][1];
            batchTotal.acqTots[i].trnTotAmt += batchTotal.acqTots[i].totAmts[idx];
            idx++;

            batchTotal.acqTots[i].tots[idx][0] = 20000;
            totAmt[0] = batchTotal.acqTots[i].totAmts[idx];
            batchTotal.acqTots[i].tots[idx][1] = GetTranTotalByAcq(batchTotal.acqTots[i].tots[idx][0], VTerm.GetVTermPrms().AcqInfos[i].AcqId, VTerm.GetVTermPrms().AcqInfos[i].TermId, totAmt);
            batchTotal.acqTots[i].totAmts[idx] = totAmt[0];
            batchTotal.acqTots[i].trnCnt += batchTotal.acqTots[i].tots[idx][1];
            batchTotal.acqTots[i].trnTotAmt += batchTotal.acqTots[i].totAmts[idx];
            idx++;

            batchTotal.acqTots[i].len = (short) idx;

            batchTotal.len++;
            batchTotal.batchCount += batchTotal.acqTots[i].trnCnt;
            batchTotal.batchTotAmt += batchTotal.acqTots[i].trnTotAmt;
        }

        try {
            file.Remove(LASTBTCH_FN);
            file fd = new file(LASTBTCH_FN, file.OpenMode.RDWR);
            olib.WriteFile(batchTotal, fd);

            idx = 0;
            rv = 0;
            do {
                BatchRec tmpRec = new BatchRec();
                rv = GetTran(tmpRec, idx);
                idx++;
                if (rv > 0) {
                    olib.WriteFile(tmpRec, fd);
                }
            } while (rv > 0);


            fd.Close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return batchTotal;
    }

    public static int GetTranTotalByAcq(int proCode, byte[] acqId, byte[] termId, long[] totAmt) throws Exception {
        int rv = 0, idx = 0;
        int oCount = 0;
        long amt, tmpAmt;

        do {
            BatchRec rec = new BatchRec();
            rv = GetTran(rec, idx);
            idx++;

            if (rv > 0) {
                if ((!memcmp(rec.RspCode, "00".getBytes(), 2) || !memcmp(rec.RspCode, "Y1".getBytes(), 2) || !memcmp(rec.RspCode, "Y3".getBytes(), 2)) && (rec.ProcessingCode != 950000)) {
                    if (!memcmp(acqId, rec.AcqId, 2) && !memcmp(termId, rec.TermId, 8)) {
                        if ((proCode == 20000) && TranStruct.IsReverse(rec.ProcessingCode)) {
                            oCount++;
                            if (rec.ProcessingCode != 320000) {
                                amt = c.atol(rec.Amount);

                                if ((rec.ProcessingCode == 220000) || (rec.ProcessingCode == 220001))
                                    tmpAmt = totAmt[0] + amt;
                                else
                                    tmpAmt = totAmt[0] - amt;
                                totAmt[0] = tmpAmt;
                            }
                        } else if (rec.ProcessingCode == proCode) {
                            oCount++;
                            amt = c.atol(rec.Amount);

                            if ((rec.ProcessingCode == 200000) || (rec.ProcessingCode == 200001))
                                tmpAmt = totAmt[0] - amt;
                            else
                                tmpAmt = totAmt[0] + amt;
                            totAmt[0] = tmpAmt;
                        } else {
                            if (TranStruct.IsReverse(rec.ProcessingCode)) {
                                if (rec.OrgProcessingCode == proCode) {
                                    oCount++;
                                    amt = c.atol(rec.Amount);
                                    if ((rec.OrgProcessingCode == 200000) || (rec.OrgProcessingCode == 200001))
                                        tmpAmt = totAmt[0] - amt;
                                    else
                                        tmpAmt = totAmt[0] + amt;
                                    totAmt[0] = tmpAmt;
                                }
                            }
                        }
                    }
                }
            }
        } while (rv > 0);

        return oCount;
    }

    public static int CalcBatchTotals() throws Exception {
        int rv = 0, idx = 0;
        long amt, tmp;

        currentTran.TotalsLen = 1;
        strcpy(currentTran.Totals[0].CurrencyCode, "949");
        do {
            BatchRec rec = new BatchRec();

            rv = GetTran(rec, idx);
            if (rv > 0) {
                idx++;

                if ((!memcmp(rec.RspCode, "00".getBytes(), 2) || !memcmp(rec.RspCode, "Y1".getBytes(), 2) || !memcmp(rec.RspCode, "Y3".getBytes(), 2)) && (rec.ProcessingCode != 950000)) {
                    switch (rec.ProcessingCode) {
                        case 0:                //Satış
                            //case 20000:			//Satış İptal
                        case 1:                //Online Imprinter
                            //case 20001:			//Online Imprinter İptal
                        case 2:                //Loyalty Taksit
                            //case 20002:			//Loyalty Taksit İptal
                        case 3:                //Loyalty Puan Kullanım
                            //case 20003:			//Loyalty Puan Kullanım İptal
                        case 300001:        //Ön Prov. Kapama
                            //case 320001:		//Ön Prov. Kapama İptal
                            if (rec.MsgTypeId == 210) //Online
                            {
                                currentTran.Totals[0].POnlTCnt++;
                                amt = c.atol(rec.Amount);
                                currentTran.Totals[0].POnlTAmt += amt;
                            } else if (rec.MsgTypeId == 230 || rec.MsgTypeId == 220) //Offline
                            {
                                currentTran.Totals[0].POffTCnt++;
                                amt = c.atol(rec.Amount);
                                currentTran.Totals[0].POffTAmt += amt;
                            }
                            break;
                        case 200000:
                            //case 220000:
                        case 200001:
                            //case 220001:
                            if (rec.MsgTypeId == 210) //Online
                            {
                                currentTran.Totals[0].NOnlTCnt++;
                                amt = c.atol(rec.Amount);
                                currentTran.Totals[0].NOnlTAmt += amt;
                            } else if (rec.MsgTypeId == 230) //Offline
                            {
                                currentTran.Totals[0].NOffTCnt++;
                                amt = c.atol(rec.Amount);
                                currentTran.Totals[0].NOffTAmt += amt;
                            }
                            break;
                    }
                }
            }

        } while (rv > 0);

        return idx;
    }

    public static void SaveTran() throws Exception {
        int rv = -1, idx = 0, found = 0;
        BatchRec rec;

        if (currentTran.Offline == 0)
            Reversal.RemoveReversalTran();

        if (!memcmp(currentTran.RspCode, "00".getBytes(), 2) || !memcmp(currentTran.RspCode, "Y1".getBytes(), 2) || !memcmp(currentTran.RspCode, "Y3".getBytes(), 2)) {
            if (currentTran.MsgTypeId == 220 && currentTran.ProcessingCode == 0) {
                SaveLastTran();
                params.BatchNoLOffT = params.BatchNo;
                params.TranNoLOffT = currentTran.TranNo;
            } else if (currentTran.MsgTypeId == 110 || currentTran.MsgTypeId == 210 || currentTran.MsgTypeId == 810) {
                SaveLastTran();
                params.BatchNoLOnT = params.BatchNo;
                params.TranNoLOnT = currentTran.TranNo;
            }
        }

        if (currentTran.MsgTypeId == 220 || currentTran.MsgTypeId == 120) {
            params.OffAdvTryCount = 0;
        }

        PrmStruct.Save();

        CreateBatch();

        if ((currentTran.MsgTypeId < 200 || currentTran.MsgTypeId > 230) && (currentTran.ProcessingCode != 300000 && currentTran.ProcessingCode != 320000 && currentTran.ProcessingCode != 950000))
            return;

        if (currentTran.TranType == TranStruct.T_VOID || currentTran.MsgTypeId == 230 || currentTran.MsgTypeId == 130) {
            found = 0;
            do {
                rec = new BatchRec();
                rv = GetTran(rec, idx);
                if (rv > 0) {
                    Log.i(TAG, String.format("tran no %d %d %d %d", rec.TranNo, currentTran.OrgTranNo, rec.ProcessingCode, currentTran.ProcessingCode));
                    if (rec.TranNo == currentTran.OrgTranNo) {
                        if (currentTran.ProcessingCode != 950000)
                            found = 1;
                        else if (currentTran.ProcessingCode == rec.ProcessingCode)
                            found = 1;

                        if (found != 0) {
                            file fd = new file(BATCH_FN, file.OpenMode.RDWR);
                            rec = BatchRec.Get(currentTran);
                            fd.Seek(idx * sizeofBatchRec, file.SeekMode.SEEK_SET);
                            olib.WriteFile(rec, fd);
                            fd.Close();
                            rv = 0;
                            break;
                        }
                    }
                }
                idx++;

            } while (rv > 0);
        } else {
            file fd = new file(BATCH_FN, file.OpenMode.RDWR);
            rec = BatchRec.Get(currentTran);
            fd.Seek(0, file.SeekMode.SEEK_END);
            olib.WriteFile(rec, fd);
            fd.Close();
            rv = 0;
        }
    }

    public static int GetTranCount() {
        return file.Size(BATCH_FN) / sizeofBatchRec;
    }

    public static List<BatchRec> GetBatch() {
        List<BatchRec> recs = new ArrayList<>();
        BatchRec rec = new BatchRec();
        file fd = null;

        if (!file.Exist(BATCH_FN)) return recs;

        try {
            fd = new file(BATCH_FN, file.OpenMode.RDONLY);

            int count = GetTranCount();

            for (int i = 0; i < count; ++i) {
                olib.ReadFile(rec, fd);
                recs.add(rec);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fd != null)
                fd.Close();
        }

        return recs;
    }

    public static int GetTranCountOfflineApproved() throws Exception {
        int rv = 0, idx = 0, cnt = 0;


        do {
            BatchRec tmpRec = new BatchRec();
            rv = GetTran(tmpRec, idx);
            idx++;

            if (rv > 0) {
                if (!memcmp(tmpRec.RspCode, "Y1".getBytes(), 2) || !memcmp(tmpRec.RspCode, "Y3".getBytes(), 2)
                        || !memcmp(tmpRec.RspCode, "Z1".getBytes(), 2) || !memcmp(tmpRec.RspCode, "Z3".getBytes(), 2)
                        || (TranStruct.IsReverse(tmpRec.ProcessingCode) && (tmpRec.OrgMsgTypeId == 220))) {
                    cnt++;
                }
            }
        } while (rv > 0);

        return cnt;
    }

    public static int GetTran(BatchRec rec, int idx) throws Exception {
        int rv = 0, count = 0;

        CreateBatch();

        count = GetTranCount();
        if (idx < count) {
            if (!file.Exist(BATCH_FN)) return rv;

            file fd = new file(BATCH_FN, file.OpenMode.RDONLY);
            fd.Seek(idx * sizeofBatchRec, file.SeekMode.SEEK_SET);

            if (rec != null) olib.ReadFile(rec, fd);

            rv = sizeofBatchRec;

            fd.Close();
        }
        return rv;
    }

    public static int GetTranByNo(BatchRec rec, int no) throws Exception {
        int rv = 0, idx = 0;

        do {
            BatchRec tmpRec = new BatchRec();
            rv = GetTran(tmpRec, idx);
            idx++;

            if (rv > 0) {
                if (tmpRec.TranNo == no) {
                    olib.Copy(rec, tmpRec);
                    return 0;
                }
            }
        } while (rv > 0);

        return -1;
    }

    public static int GetTranByAcq(BatchRec rec, byte[] acqId, int idx) throws Exception {
        int rv = 0, i = 0, j = 0;

        do {
            BatchRec tmpRec = new BatchRec();
            rv = GetTran(tmpRec, i);
            i++;

            if (rv > 0) {
                if (!memcmp(tmpRec.RspCode, "00".getBytes(), 2) || !memcmp(tmpRec.RspCode, "Y1".getBytes(), 2) || !memcmp(tmpRec.RspCode, "Y3".getBytes(), 2)) {
                    if (!memcmp(acqId, tmpRec.AcqId, 2)) {
                        if (j == idx) {
                            olib.Copy(rec, tmpRec);
                            return 0;
                        } else
                            j++;
                    }
                }
            }
        } while (rv > 0);

        return -1;
    }

    public static void PrintBatch() throws Exception {
        int rv = 0, idx = 0;

        do {
            BatchRec rec = new BatchRec();
            rv = GetTran(rec, idx);
            idx++;

            if (rv > 0) {
                Utility.log("********************************");
                Utility.log("MsgTypeId:%d", rec.MsgTypeId);
                Utility.log("ProcessingCode:%d", rec.ProcessingCode);
                Utility.log("OrgMsgTypeId:%d", rec.OrgMsgTypeId);
                Utility.log("OrgProcessingCode:%d", rec.OrgProcessingCode);
                Utility.log("DateTime:%02d/%02d%02d %02d:%02d:%02d", rec.DateTime[2], rec.DateTime[1], rec.DateTime[0], rec.DateTime[3], rec.DateTime[4], rec.DateTime[5]);
                Utility.log("OrgDateTime:%02d/%02d%02d %02d:%02d:%02d", rec.OrgDateTime[2], rec.OrgDateTime[1], rec.OrgDateTime[0], rec.OrgDateTime[3], rec.OrgDateTime[4], rec.OrgDateTime[5]);
                Utility.log("Pan:%s", c.ToString(rec.Pan));
                Utility.log("Amount:%s", c.ToString(rec.Amount));
                Utility.log("ExpDate:%s", c.ToString(rec.ExpDate));
                Utility.log("EntryMode:%d", rec.EntryMode);
                Utility.log("ConditionCode:%02X", rec.ConditionCode);
                Utility.log("AcqId:%02X%02X", rec.AcqId[0], rec.AcqId[1]);
                Utility.log("RRN:%s", c.ToString(rec.RRN));
                Utility.log("AuthCode:%s", c.ToString(rec.AuthCode));
                Utility.log("RspCode:%s", c.ToString(rec.RspCode));
                Utility.log("TermId:%s", c.ToString(rec.TermId));
                Utility.log("MercId:%s", c.ToString(rec.MercId));
                Utility.log("CurrencyCode:%02X%02X", rec.CurrencyCode[0], rec.CurrencyCode[1]);
                Utility.log("Stan:%d", rec.Stan);
                Utility.log("TranNo:%d", rec.TranNo);
                Utility.log("OrgTranNo:%d", rec.OrgTranNo);
                Utility.log("TranNoLOnT:%d", rec.TranNoLOnT);
                Utility.log("BatchNoLOnT:%d", rec.BatchNoLOnT);
                Utility.log("TranNoLOffT:%d", rec.TranNoLOffT);
                Utility.log("BatchNoLOffT:%d", rec.BatchNoLOffT);

                Log.i(TAG, String.format("********************************"));
                Log.i(TAG, String.format("MsgTypeId:%d", rec.MsgTypeId));
                Log.i(TAG, String.format("ProcessingCode:%d", rec.ProcessingCode));
                Log.i(TAG, String.format("OrgMsgTypeId:%d", rec.OrgMsgTypeId));
                Log.i(TAG, String.format("OrgProcessingCode:%d", rec.OrgProcessingCode));
                Log.i(TAG, String.format("DateTime:%02d/%02d%02d %02d:%02d:%02d", rec.DateTime[2], rec.DateTime[1], rec.DateTime[0], rec.DateTime[3], rec.DateTime[4], rec.DateTime[5]));
                Log.i(TAG, String.format("OrgDateTime:%02d/%02d%02d %02d:%02d:%02d", rec.OrgDateTime[2], rec.OrgDateTime[1], rec.OrgDateTime[0], rec.OrgDateTime[3], rec.OrgDateTime[4], rec.OrgDateTime[5]));
                Log.i(TAG, String.format("Pan:%s", c.ToString(rec.Pan)));
                Log.i(TAG, String.format("Amount:%s", c.ToString(rec.Amount)));
                Log.i(TAG, String.format("ExpDate:%s", c.ToString(rec.ExpDate)));
                Log.i(TAG, String.format("EntryMode:%d", rec.EntryMode));
                Log.i(TAG, String.format("ConditionCode:%02X", rec.ConditionCode));
                Log.i(TAG, String.format("AcqId:%02X%02X", rec.AcqId[0], rec.AcqId[1]));
                Log.i(TAG, String.format("RRN:%s", c.ToString(rec.RRN)));
                Log.i(TAG, String.format("AuthCode:%s", c.ToString(rec.AuthCode)));
                Log.i(TAG, String.format("RspCode:%s", c.ToString(rec.RspCode)));
                Log.i(TAG, String.format("TermId:%s", c.ToString(rec.TermId)));
                Log.i(TAG, String.format("MercId:%s", c.ToString(rec.MercId)));
                Log.i(TAG, String.format("CurrencyCode:%02X%02X", rec.CurrencyCode[0], rec.CurrencyCode[1]));
                Log.i(TAG, String.format("Stan:%d", rec.Stan));
                Log.i(TAG, String.format("TranNo:%d", rec.TranNo));
                Log.i(TAG, String.format("OrgTranNo:%d", rec.OrgTranNo));
                Log.i(TAG, String.format("TranNoLOnT:%d", rec.TranNoLOnT));
                Log.i(TAG, String.format("BatchNoLOnT:%d", rec.BatchNoLOnT));
                Log.i(TAG, String.format("TranNoLOffT:%d", rec.TranNoLOffT));
                Log.i(TAG, String.format("BatchNoLOffT:%d", rec.BatchNoLOffT));
            }

        } while (rv > 0);
    }

    public static int SaveLastTran() throws Exception {
        file.Remove(LASTTRN_FN);

        currentTran.BatchNo = params.BatchNo;
        currentTran.Stan = params.Stan;

        olib.WriteFile(currentTran, new file(LASTTRN_FN));

        return 0;
    }

    public static int RestoreLastTran(TranStruct trn) {
        if (trn == null || !file.Exist(LASTTRN_FN))
            return -1;

        try {
            olib.ReadFile(trn, new file(LASTTRN_FN));
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

        return 0;
    }

    public static void CreateBatch() throws IOException {
        int fd = -1;

        if(!file.Exist(BATCH_FN))
        {
            new file(BATCH_FN, file.OpenMode.RDWR).Close();
        }
    }

    public static void RemoveBatch() {
        file.Remove(BATCH_FN);
    }


}
