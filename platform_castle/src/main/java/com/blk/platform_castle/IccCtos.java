package com.blk.platform_castle;

import android.util.Log;

import com.blk.platform.ByteArray;
import com.blk.sdk.Convert;
import com.blk.sdk.c;

import CTOS.CtSC;

class IccCtos implements com.blk.platform.IIcc {

    CtSC sc = PlatfromCtos._sc;
    private static final String TAG = IccCtos.class.getSimpleName();

    @Override
    public int reset() {
        byte baATR[] = new byte[128];
        int ret = sc.resetISO(0, 1, baATR);
        Log.d(TAG, String.format("resetISO return code:0x%02x", ret));

        if (ret == 0) {
            int ATRLen = sc.getATRLen();
            Log.d(TAG, "ATR : " + Convert.byteArray2HexString(baATR, ATRLen));
            Log.d(TAG, String.format("CardType = %d", sc.getCardType()));
        }
        return ret;
    }

    @Override
    public int sendAPDU(byte[] apdu, ByteArray response, ByteArray sw) {
        byte[] baRBuf = new byte[128];
        response.length = 0;
        sw.length = 0;

        int ret = sc.sendAPDU(0, apdu, baRBuf);
        Log.d(TAG, "sendAPDU(" + Convert.byteArray2HexString(apdu, apdu.length) + ") :  rv(" + ret + ")");

        if(ret == 0) {
            int len = sc.getRLen();

            if (sw.buffer == null)
                sw.buffer = new byte[2];
            c.memcpy(sw.buffer, 0, baRBuf, len - 2, sw.length = 2);

            response.length = len - 2;
            if (response.buffer == null)
                response.buffer = new byte[response.length];
            c.memcpy(response.buffer, baRBuf, response.length);
            Log.d(TAG, "SW[" + Convert.byteArray2HexString(sw.buffer, sw.length) + "] RX[" + Convert.byteArray2HexString(response.buffer, response.length) + "]");
        }

        return ret;
    }
}
