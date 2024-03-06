package com.blk.platform_castle;

import static com.blk.sdk.c.memcpy;

import com.blk.platform.IPlatform;
import com.blk.platform_castle.emvcl.EmvclCtos;
import com.blk.platform.ICard;

import CTOS.CtEMVCL;

class CardCtos implements ICard {

    private static final String TAG = CardCtos.class.getSimpleName();

    Result result;

    boolean fDetect = false;
    int waitEvent;

    @Override
    public Result Detect(int cardTypes) {

        PlatfromCtos._emvMsr.flushTracksBuffer(); // clear buffer

        result = new Result();
        fDetect = true;
        waitEvent = ((cardTypes & ICard.MAGNETIC) != 0) ? CTOS_API.d_EVENT_MSR : 0;
        waitEvent |= ((cardTypes & ICard.ICC) != 0) ? CTOS_API.d_EVENT_SC : 0;

        if ((cardTypes & ICard.ICC) != 0)
            PlatfromCtos._system.sc_led(1);
        if ((cardTypes & ICard.PICC) != 0)
            PlatfromCtos._system.cl_led(1);


        Thread msrSC = new Thread(() -> {
        //    msr.readTracks();

            do {
                if ((cardTypes & ICard.PICC) != 0 && PlatfromCtos._emvcl.detectCard() == CtEMVCL.d_EMVCL_NO_ERROR)
                {
                    result.status = Status.OK;
                    result.cardType = ICard.PICC;

                    EmvclCtos emvclCtos = (EmvclCtos) IPlatform.get().emvcl;
                    emvclCtos.getCardData(result.ctlsPAN);
                    return;
                }
                if ((cardTypes & ICard.ICC) != 0 &&  isSCPresent()) {
                    result.status = Status.OK;
                    result.cardType = ICard.ICC;
                    return;
                }
                if ((cardTypes & ICard.MAGNETIC) != 0 && PlatfromCtos._msr.read(result.track1, result.track2, result.track3) == 0) {
                    result.Tk1Len = PlatfromCtos._msr.getTk1Len();
                    result.Tk2Len = PlatfromCtos._msr.getTk2Len();
                    result.Tk3Len = PlatfromCtos._msr.getTk3Len();

                    result.status = Status.OK;
                    result.cardType = ICard.MAGNETIC;
                    return;
                }
//
//
//                int events = api.CTOS_SystemWait(
//                        waitEvent
//                        , 10);
//                if (events == CTOS_API.d_SYSWAIT_TIMEOUT)
//                    continue;
//                else if (events == 0) {
//                    // error.
//                    return;
//                }
//                else if ((events & CTOS_API.d_EVENT_MSR) != 0) {
//                    result.status = Status.OK;
//                    result.cardType = ICard.MAGNETIC;
//                    api.CTOS_MSRRead();
//                    result.track1 = Arrays.copyOfRange(api.baTk1Buf, 0, api.usTk1Len);
//                    result.track2 = Arrays.copyOfRange(api.baTk2Buf, 0, api.usTk2Len);
//                    result.track3 = Arrays.copyOfRange(api.baTk3Buf, 0, api.usTk3Len);
//                    return;
//                }
//                else if ((events & CTOS_API.d_EVENT_SC) != 0) {
//                    result.status = Status.OK;
//                    result.cardType = ICard.ICC;
//                    return;
//                }
            } while (fDetect);
        });
        msrSC.start();


        try {
            msrSC.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        PlatfromCtos._system.sc_led(0);
        PlatfromCtos._system.cl_led(0);

        return result;
    }

    @Override
    public void Interrupt(InterruptReason reason) {
        if (!fDetect) return;

        if (reason == InterruptReason.CANCEL) result.status = Status.CANCEL;
        if (reason == InterruptReason.MANUEL) result.status = Status.MANUEL;
        if (reason == InterruptReason.QR) result.status = Status.QR;
        fDetect = false;
    }

    @Override
    public boolean isDetecting() {
        return fDetect;
    }

    @Override
    public boolean isSCPresent() {
        PlatfromCtos._sc.status(0);
        return (PlatfromCtos._sc.getStatus() & 0x01) == 0x01;
    }
}
