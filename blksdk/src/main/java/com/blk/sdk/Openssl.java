package com.blk.sdk;

import java.util.Arrays;

public class Openssl {
// https://teskalabs.com/blog/openssl-binary-distribution-for-developers-static-library
    public static native byte[] EncryptWithHPK(byte[] key, byte[] in, int len);
    public static native byte[] DecryptMsg(byte[] key, byte[] in, int len);
    public static native byte[] EncryptMsg(byte[] key, byte[] in, int len);
    public static native byte[] Des(boolean fEncrypt, byte[] key, byte[] in);

    static byte[] PKCS5_Pad(byte[] b)
    {
        int padLen = 8 - (b.length % 8);
        byte[] r = Arrays.copyOf(b, b.length + padLen);

        for (int i = b.length; i < r.length; ++i) {
            r[i] = (byte) padLen;
        }
        return r;
    }
}
