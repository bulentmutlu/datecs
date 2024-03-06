package com.blk.platform;

public interface ICard {
    public static final int MAGNETIC = 1;
    public static final int ICC = 2;
    public static final int PICC = 4;

    public static final int MAG_ICC_PICC = MAGNETIC | ICC | PICC;
    public static final int MAG_ICC = MAGNETIC | ICC;

    public enum Status {
        OK,
        FAIL,
        TIMEOUT,
        CANCEL,
        MANUEL,
        QR,
    }


    public static class Result {
        public Status status;
        public int cardType;

        public byte[] track1 = new byte[128], track2 = new byte[128], track3 = new byte[128];
        public int Tk1Len = 0, Tk2Len = 0, Tk3Len = 0;
        public byte[] ctlsPAN = new byte[20];

        public Result()
        {
            status = Status.TIMEOUT;
            cardType = 0;
        }
        public Result(Status s, int t)
        {
            status = s;
            cardType = t;
        }
    }

    enum InterruptReason {
        CANCEL,
        MANUEL,
        QR,
        EMULATE_MAGNETIC // emulator test
    }

    Result Detect(int cardTypes);
    void Interrupt(InterruptReason reason);
    boolean isDetecting();
    boolean isSCPresent();
}
