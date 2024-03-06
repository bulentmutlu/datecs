package com.blk.sdk.emulator;

import static com.blk.sdk.c.memcpy;

import com.blk.platform.Emv.CAPublicKey;
import com.blk.platform.Emv.EmvApp;
import com.blk.platform.Emv.IEmv;
import com.blk.platform.IEmvcl;

import java.util.HashMap;
import java.util.List;

import com.blk.sdk.Emv.Config;


public class EmvclEmulator extends IEmvcl {

        private static final String TAG = EmvclEmulator.class.getSimpleName();

    @Override
    public int CompleteTransaction(IEmv.OnlineResponse onlineResponse) {
        return 0;
    }

    @Override
    public int CancelTransaction() {
        return 0;
    }

    @Override
    public IEmv.TlvData dataGet(int tag) {
        return null;
    }

    @Override
        public  int Init(final HashMap<String, List<CAPublicKey>> keys, final EmvApp[] appList) throws Exception
        {
            String emvConfigFile = Config.CreateEmvCLConfigFile(keys, appList);

            return 0;
        }

    @Override
    public int StartTransaction(IEmv.TransactionData tranData, Result result) {
        return 0;
    }

    @Override
    public byte[] chipData() {
        return new byte[0];
    }
}
