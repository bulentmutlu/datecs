package com.blk.platform;

public abstract class System {
    public abstract  void beepOk();
    public abstract void beepErr();
    public abstract String Serial() throws Exception;
    public abstract String Model() throws Exception;
    public abstract void setSystemTime(int year, int month, int day, int hour, int minute, int second);
}
