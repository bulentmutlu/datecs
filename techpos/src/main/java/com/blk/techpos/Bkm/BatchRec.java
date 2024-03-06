package com.blk.techpos.Bkm;

import static com.blk.sdk.c.memcpy;

public class BatchRec {
    public int					MsgTypeId;
    public int					OrgMsgTypeId;
    public int					ProcessingCode;
    public int					OrgProcessingCode;
    public byte[]				DateTime = new byte[8];
    public byte[]				OrgDateTime = new byte[8];
    public byte[]				Pan = new byte[20];
    public byte[]				Amount = new byte[12];
    public byte[]		        AcqId = new byte[2];
    public int		            TranNo;
    public int		            OrgTranNo;
    public byte[]				ExpDate = new byte[4];
    public byte		            EntryMode;
    public byte                ConditionCode;
    public byte[]				RRN = new byte[12];
    public byte[]				AuthCode = new byte[6];
    public byte[]				RspCode = new byte[2];
    public byte[]				TermId = new byte[8];
    public byte[]				MercId = new byte[15];
    public byte[]               CurrencyCode = new byte[2];
    public short		        DE55Len;
    public byte[]               DE55 = new byte[256];
    public byte[]				OrgBankRefNo = new byte[16];
    public int		Stan;
    public int		TranNoLOnT;
    public int		BatchNoLOnT;
    public int		TranNoLOffT;
    public int		BatchNoLOffT;

    public static BatchRec Get(TranStruct ts)
    {
        BatchRec rec  = new BatchRec();
        rec.MsgTypeId = ts.MsgTypeId;
        rec.ProcessingCode = ts.ProcessingCode;
        rec.OrgMsgTypeId = ts.OrgMsgTypeId;
        rec.OrgProcessingCode = ts.OrgProcessingCode;
        memcpy(rec.OrgDateTime, ts.OrgDateTime, rec.OrgDateTime.length);
        memcpy(rec.DateTime, ts.DateTime, rec.DateTime.length);
        memcpy(rec.Pan, ts.Pan, rec.Pan.length);
        memcpy(rec.Amount, ts.Amount, rec.Amount.length);
        memcpy(rec.ExpDate, ts.ExpDate, rec.ExpDate.length);
        rec.EntryMode = ts.EntryMode;
        rec.ConditionCode = ts.ConditionCode;
        memcpy(rec.AcqId, ts.AcqId, rec.AcqId.length);
        memcpy(rec.RRN, ts.RRN, rec.RRN.length);
        memcpy(rec.AuthCode, ts.AuthCode, rec.AuthCode.length);
        memcpy(rec.RspCode, ts.RspCode, rec.RspCode.length);
        memcpy(rec.TermId, ts.TermId, rec.TermId.length);
        memcpy(rec.MercId, ts.MercId, rec.MercId.length);
        memcpy(rec.CurrencyCode, ts.CurrencyCode, rec.CurrencyCode.length);
        rec.DE55Len = ts.DE55Len;
        memcpy(rec.DE55, ts.DE55, rec.DE55.length);
        memcpy(rec.OrgBankRefNo, ts.OrgBankRefNo, rec.OrgBankRefNo.length);
        rec.Stan = ts.Stan;
        rec.TranNo = ts.TranNo;
        rec.OrgTranNo = ts.OrgTranNo;
        rec.TranNoLOnT = ts.TranNoLOnT;
        rec.BatchNoLOnT = ts.BatchNoLOnT;
        rec.TranNoLOffT = ts.TranNoLOffT;
        rec.BatchNoLOffT = ts.BatchNoLOffT;

        return rec;
    }
}
