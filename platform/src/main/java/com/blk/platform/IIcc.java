package com.blk.platform;

public interface IIcc {
    int reset();
    int sendAPDU(byte[] apdu, ByteArray response, ByteArray sw);
}
