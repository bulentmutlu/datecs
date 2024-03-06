package com.blk.platform.Emv;

import java.util.ArrayList;
import java.util.List;

public class EmvApp {

    public byte[] aid = new byte[17];
    public byte aidLen;
    public byte kernel;

    public List<IEmv.TlvData> tags = new ArrayList<>();
}
