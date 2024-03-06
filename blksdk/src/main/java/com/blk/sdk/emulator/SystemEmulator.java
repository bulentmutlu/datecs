package com.blk.sdk.emulator;

import com.blk.platform.ISystem;

class SystemEmulator extends ISystem {


    @Override
    public void beepOk() {

    }

    @Override
    public void beepErr() {

    }

    @Override
    public String Serial() throws Exception {
        return "0005021717001600";
    }

    @Override
    public String Model() throws Exception {
        return "V3000";
    }

    @Override
    public void setSystemTime(int year, int month, int day, int hour, int minute, int second) {

    }

}
