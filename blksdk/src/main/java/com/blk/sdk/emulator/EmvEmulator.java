package com.blk.sdk.emulator;

import com.blk.platform.Emv.CAPublicKey;
import com.blk.platform.Emv.EmvApp;
import com.blk.platform.Emv.IEmv;
import com.blk.sdk.Emv.Config;

import java.util.HashMap;
import java.util.List;

public class EmvEmulator extends IEmv {


    @Override
    public int Init(HashMap<String, List<CAPublicKey>> keys, EmvApp[] appList) throws Exception {
        String emvConfigFile = Config.CreateEmvConfigFile(keys, appList);
        return 0;
    }

    @Override
    public int AppSelect(int slot) {
        return 0;
    }

    @Override
    public int StartTransaction(TransactionData tranData, ACType acType) {
        return 0;
    }

    @Override
    public int CompleteTransaction(OnlineResponse onlineResponse) {
        return 0;
    }

    @Override
    public TlvData dataGet(int tag) {
        return new TlvData();
    }

    @Override
    public void Test(HashMap<String, List<CAPublicKey>> keys, EmvApp[] appList) {

    }
}
