package com.blk.sdk.emulator;

import com.blk.platform.IPlatform;

public class Emulator extends IPlatform {
    private static final String TAG = Emulator.class.getSimpleName();

    public static final String localHostPcIp = "10.0.2.2";

    public Emulator()
    {
        system = new SystemEmulator();
        printer = new PrinterEmulator();
        card = new CardEmulator();
        emv = new EmvEmulator();
        emvcl = new EmvclEmulator();
    }

    @Override
    public String Name() {
        return "Emulator";
    }
}
