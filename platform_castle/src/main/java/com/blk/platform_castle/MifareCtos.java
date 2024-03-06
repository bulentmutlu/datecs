package com.blk.platform_castle;

import android.os.SystemClock;

import com.blk.platform.IMifare;

import java.util.Arrays;

import CTOS.CtCL;

public class MifareCtos implements IMifare {

    CtCL cl = PlatfromCtos._mifare;

    @Override
    public int powerOn() {
        return cl.powerOn();
    }

    @Override
    public int powerOff() {
        return cl.powerOff();
    }

    @Override
    public byte[] getSerial() {
        byte[] baCSN = new byte[10];
        int TimeOut = 1000;
        long mCreationTime;
        int ret = 0;
        mCreationTime = SystemClock.elapsedRealtime();
        while(SystemClock.elapsedRealtime()<(mCreationTime+TimeOut)){
            ret = cl.ppPolling(baCSN, 0);
            if(ret == 0){
                break;
            }
        }
        if(ret != 0){
            return null;
        }
        return Arrays.copyOf(baCSN, cl.getPPPollingCSNLen());
    }
}
