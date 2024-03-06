package com.blk.platform;

import com.blk.platform.Emv.CAPublicKey;
import com.blk.platform.Emv.EmvApp;
import com.blk.platform.Emv.IEmv;

import java.util.HashMap;
import java.util.List;

public abstract class IEmvcl {
    public abstract int CompleteTransaction(IEmv.OnlineResponse onlineResponse);

    public abstract int CancelTransaction();

    public abstract IEmv.TlvData dataGet(int tag);

    public static class Result {
//Transction Result : Used for EMVCL_RC_DATA_ANALYZE--> usTransResult
// d_EMVCL_OUTCOME_APPROVAL							0x0002
// d_EMVCL_OUTCOME_DECLINED							0x0003
// d_EMVCL_OUTCOME_ONL								0x0004
        public short transResult;
//Scheme ID : Used for EMVCL_RC_DATA_EX --> bSID
// d_EMVCL_SID_VISA_OLD_US								0x13
// d_EMVCL_SID_VISA_WAVE_2								0x16
// d_EMVCL_SID_VISA_WAVE_QVSDC							0x17
// d_EMVCL_SID_VISA_WAVE_MSD							0x18
// d_EMVCL_SID_PAYPASS_MAG_STRIPE						0x20
// d_EMVCL_SID_PAYPASS_MCHIP							0x21
// d_EMVCL_SID_APPLE_VAS								0x3E
// d_EMVCL_SID_ANDROID_SMART_TAP						0x3F
// d_EMVCL_SID_JCB_WAVE_2								0x61
// d_EMVCL_SID_JCB_WAVE_QVSDC							0x62
// d_EMVCL_SID_JCB_EMV									0x63
// d_EMVCL_SID_JCB_MSD									0x64
// d_EMVCL_SID_JCB_LEGACY								0x65
// d_EMVCL_SID_AE_EMV									0x50
// d_EMVCL_SID_AE_MAG_STRIPE							0x52
// d_EMVCL_SID_DISCOVER								    0x41
// d_EMVCL_SID_DISCOVER_DPAS							0x42
// d_EMVCL_SID_DISCOVER_DPAS_MAG_STRIPE				    0x43
// d_EMVCL_SID_INTERAC_FLASH							0x48
// d_EMVCL_SID_MEPS_MCCS								0x81
// d_EMVCL_SID_CUP_QPBOC								0x91
// d_EMVCL_SID_CB_PAGOBANCOMAT							0x86
// d_EMVCL_SID_MIR										0x87
// d_EMVCL_SID_ECPC_CPACE								0x88
// d_EMVCL_SID_EFTPOS									0x89
// d_EMVCL_SID_SIBS_MB									0x8A
// d_EMVCL_SID_GRIOCARD_MAG_STRIPE						0x8B
// d_EMVCL_SID_GRIOCARD_MCHIP							0x8C
        public byte sid;
        public byte cvmAnalysis;
        public byte[] cvmResults = new byte[3];
        public byte visaAOSAPresent;
        public byte[] visaAOSA = new byte[6];
        public boolean odaFail;
        public boolean completeFunRequired;

        public byte track2Len;
        public byte[] track2Data = new byte[128];

        public byte[] aid = new byte[10];
        public int aidLen = 0;
    }

    public abstract int Init(final HashMap<String, List<CAPublicKey>> keys, final EmvApp[] appList) throws Exception;
    public abstract int StartTransaction(final IEmv.TransactionData tranData, Result result);
    public abstract byte[] chipData();
}
