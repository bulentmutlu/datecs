package com.blk.sdk.emulator;

import static com.blk.sdk.c.memcpy;

import com.blk.platform.ICard;

import java.nio.charset.StandardCharsets;

public class CardEmulator implements ICard {

    boolean fInterrupt = true;
    Result result = new Result();
    @Override
    public Result Detect(int cardTypes) {
        fInterrupt = false;

        do {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
        } while (!fInterrupt);
        return result;
    }

    @Override
    public void Interrupt(InterruptReason reason) {
        if (reason == InterruptReason.CANCEL) result.status = Status.CANCEL;
        if (reason == InterruptReason.MANUEL) result.status = Status.MANUEL;
        if (reason == InterruptReason.QR) result.status = Status.QR;
        if (reason == InterruptReason.EMULATE_MAGNETIC) {
            //byte [] track2 = "4355093000658409=24012010000045700000?".getBytes();
            byte[] track2 = ";6765780011031670=23071261123494600000?:".getBytes();
            byte[] track1 = "%B6765780011031670^MUTLU/AHMET               ^2307126112349460000000946000000?O".getBytes();

            result.status = Status.OK;
            result.cardType = MAGNETIC;
            memcpy(result.track2, track2, track2.length);
            result.Tk2Len = track2.length;
            memcpy(result.track1, track1, track1.length);
            result.Tk1Len = track1.length;
        }
        fInterrupt = true;
    }

    @Override
    public boolean isDetecting() {
        return !fInterrupt;
    }

    @Override
    public boolean isSCPresent() {
        return false;
    }
}
