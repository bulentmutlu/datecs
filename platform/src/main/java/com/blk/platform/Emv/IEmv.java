package com.blk.platform.Emv;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public abstract class IEmv {

    public interface EmvEvent {
        boolean onPINBypass();
    }

    public static class TlvData
    {
        public int Tag;
        public int Len;
        public byte[] Val;
        public TlvData()
        {
            Tag = 0;
            Len = 0;
        }
        public TlvData(int Tag, int Len, byte[] val)
        {
            this.Tag = Tag;
            this.Len = Len;
            this.Val = new byte[Len];
            if (Len > 0) {
                System.arraycopy(val, 0, this.Val, 0, Len);
            }
        }
    }
    public static class ACType {
        public static final int AC_AAC = 0; // Application Authentication Cryptogram (AAC) — Offline decline
        public static final int AC_TC = 1; // Transaction certificate (TC) — Offline approval
        public static final int AC_ARQC = 2; // Authorization Request Cryptogram (ARQC) — Online authorization
        public int type;

        public ACType() {
        }
    }
    public static class TransactionData {
        public String amount, amountCB;
        public Date dt = new Date();
        public byte txnType = 0x00; // refund 0x20
        public boolean forceOnline;
        public EmvEvent  emvEvent;
    }
    public static class OnlineResponse {
        //        public class OnlineResult {
//            public static final int REFER_APPROVE = 1;
//            public static final int REFER_DENIAL = 2;
//            public static final int ONLINE_APPROVE = 0;
//            public static final int ONLINE_FAILED = 1;
//            public static final int ONLINE_REFER = 2;
//            public static final int ONLINE_DENIAL = 3;
//            public static final int ONLINE_ABORT = 4;
//
//            public OnlineResult() {
//            }
//        }
        public byte onlineResult;
        public byte[] authorizationCode = new byte[2];
        public byte[] issuerAuthenticationData;
        public int issuerAuthenticationDataLen = 0;
        public byte[] issuerScript = new byte[512];
        public int issuerScriptLen = 0;

        public OnlineResponse() {
        }
    }
    public static HashMap<String, String> aidNames = new HashMap<>();
    static {
        aidNames.put("A0000000041010", "MasterCard");
        aidNames.put("A0000000043060", "Maestro (Debit)");
        aidNames.put("A0000000031010", "VISA");
        aidNames.put("A0000000032010", "VISA Electron");
        aidNames.put("A0000000038010", "VISA Plus");
        aidNames.put("A000000333", "UnionPay");
        aidNames.put("A000000333010103", "UnionPay Quasi Credit");
        aidNames.put("A000000333010102", "UnionPay Credit");
        aidNames.put("A000000333010101", "UnionPay Debit");
        aidNames.put("A000000333010106",	"UnionPay E-Cash");
        aidNames.put("A00000002501", "American Express");
        aidNames.put("A0000006723010", "TROY Credit");
        aidNames.put("A0000006723020", "TROY Debit");
        aidNames.put("A0000000651010",		"JCB Credit");
        aidNames.put("A000000152",			"Discover");
        aidNames.put("A000000152301091",	"Discover TROY");
        aidNames.put("A000000152301092", "Discover TROY");
        aidNames.put("A0000001523010", "Discover, Pulse D Pas");
        aidNames.put("A0000006582010",		"MIR Debit");
        aidNames.put("A0000006581010",		"MIR Credit");
    }
    public static String GetNameFromAID(String aid)
    {
        if (aidNames.containsKey(aid))
            return aidNames.get(aid);
        return "Unknown";
    }
    public static String GetPreNameFromAID(String aid)
    {
        if (aid.startsWith("A000000003"))
            return "VISA";
        else if(aid.startsWith("A000000004"))
            return "MASTERCARD";
        else if(aid.startsWith("A000000672"))
            return "TROY";
        else if(aid.startsWith("A000000152"))
            return "Discover";
        else if(aid.startsWith("A000000333"))
            return "UnionPay";
        else if(aid.startsWith("A000000065"))
            return "JCB";
        else if(aid.startsWith("A000000658"))
            return "MIR";

        return "UNKNOWN";
    }
    public boolean IsCardPINBlocked()
    {
        //	95		Terminal Verification Results (TVR)		B5			Mandatory									//
        IEmv.TlvData tlvData;
        if ((tlvData = dataGet(0x95)).Len > 0)
            return (tlvData.Val[2] & 0x20) != 0;
        return false;
    }
    public boolean IsPerformOnlinePin()
    {
        //	95		Terminal Verification Results (TVR)		B5			Mandatory									//
        IEmv.TlvData tlvData;
        if ((tlvData = dataGet(0x95)).Len > 0)
            return (tlvData.Val[2] & 0x04) != 0;
        return false;
    }

    public abstract int Init(final HashMap<String, List<CAPublicKey>> keys, final EmvApp[] appList) throws Exception;
    public abstract int AppSelect(int slot);
    public abstract int StartTransaction(TransactionData tranData, ACType acType);
    public abstract int CompleteTransaction(OnlineResponse onlineResponse);
    public abstract TlvData dataGet(int tag);
    public abstract void Test(final HashMap<String, List<CAPublicKey>> keys, final EmvApp[] appList) throws Exception;
}
