package com.blk.sdk;

public class Timer {
    long m_cur;
    public Timer()
    {
        m_cur = DT.getMilliseconds();
    }
    void reset()
    {
        m_cur = DT.getMilliseconds();
    }
    int elapsedMinutes()
    {
        return (int) (((DT.getMilliseconds() - m_cur) / 1000) / 60);
    }
    int elapsedSecond()
    {
        return (int) ((DT.getMilliseconds() - m_cur) / 1000);
    }
    long elapsedMilliSecond()
    {
        return DT.getMilliseconds() - m_cur;
    }
}
