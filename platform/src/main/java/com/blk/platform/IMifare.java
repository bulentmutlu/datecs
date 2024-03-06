package com.blk.platform;

import android.os.RemoteException;
import android.util.Log;

public interface IMifare {
    int powerOn();
    int powerOff();
    byte[] getSerial();
}
