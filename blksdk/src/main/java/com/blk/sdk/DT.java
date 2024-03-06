package com.blk.sdk;

import java.util.Date;

public class DT {
    public static long getSeconds()
    {
        return new Date().getTime() / 1000;
    }
    public static long getMilliseconds()
    {
        return new Date().getTime();
    }
    public static long toSeconds(long milliseconds)
    {
        return milliseconds / 1000;
    }
    public static long toMilliseconds(long seconds)
    {
        return seconds * 1000;
    }
}
