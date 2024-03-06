package com.blk.platform.Emv;

public class ApplicationConfig {
        public byte[] merchName = new byte[256];
        public byte[] merchCateCode;
        public byte[] merchId;
        public byte[] termId;
        public byte terminalType;
        public byte[] capability;
        public byte[] exCapability;
        public byte transCurrExp;
        public byte referCurrExp;
        public byte[] referCurrCode;
        public byte[] countryCode;
        public byte[] transCurrCode;
        public long referCurrCon;
        public byte transType;
        public byte forceOnline;
        public byte getDataPIN;
        public byte surportPSESel;

        public ApplicationConfig() {
            this.merchCateCode = new byte[2];
            this.merchId = new byte[15];
            this.termId = new byte[8];
            this.capability = new byte[3];
            this.exCapability = new byte[5];
            this.referCurrCode = new byte[2];
            this.countryCode = new byte[2];
            this.transCurrCode = new byte[2];
        }
}
