package com.blk.platform;

import com.blk.platform.Emv.IEmv;

public abstract class IPlatform {

    public ISystem system;
    public IPrinter printer;
    public ICard card;
    public IEmv emv;
    public IEmvcl emvcl;
    public IIcc icc;
    public IMifare mifare;

    static IPlatform platform;

    public abstract String Name();

    public IPlatform()
    {
        assert platform == null;
        platform = this;
    }

    public static IPlatform get()
    {
        assert platform != null;
        return platform;
    }
    public boolean DebugMode(Boolean fOpen)
    {
        return true;
    }
}
