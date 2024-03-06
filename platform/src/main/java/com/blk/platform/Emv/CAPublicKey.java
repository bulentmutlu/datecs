package com.blk.platform.Emv;

public class CAPublicKey {
    public byte[] rID = new byte[5];
    public byte index;
    public int modulusLen;
    public byte[] modulus = new byte[248];
    public int exponentLen;
    public byte[] exponent = new byte[3];
    public byte[] hash = new byte[20];

    public byte hashInd;
    public byte arithInd;
    public byte[] expDate = new byte[3];
    //public byte[] checkSum = new byte[20];

    public CAPublicKey() {
    }
}
