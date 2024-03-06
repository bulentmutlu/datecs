package com.blk.sdk;

import java.util.Calendar;

/**
 * Created by id on 27.02.2018.
 */

public class Rtc {

    static Calendar calendar= Calendar.getInstance();

    public byte year;		/*!< \brief year (without century) */
    public byte mon;		/*!< \brief month */
    public byte day;		/*!< \brief day */
    public byte hour;		/*!< \brief hour */
    public byte min;		/*!< \brief minute */
    public byte sec;		/*!< \brief second */
    public byte dow;		/*!< \brief day of week */

    public Rtc(){
        calendar = Calendar.getInstance();
    }

    public static int GetTimeSeconds()
    {
        int cal=calendar.get(Calendar.SECOND);
        if(cal==0)cal=1;
        return cal;

    }
    public static byte[] GetDateTime()
    {
        byte[] DateTime = new byte[8];
        Rtc rtc = Rtc.RtcGet();
        DateTime[0] = rtc.year;
        DateTime[1] = rtc.mon;
        DateTime[2] = rtc.day;
        DateTime[3] = rtc.hour;
        DateTime[4] = rtc.min;
        DateTime[5] = rtc.sec;
        DateTime[6] = rtc.dow;
        return DateTime;
    }
    public static Rtc RtcGet()
    {
        Rtc rtc = new Rtc();
        rtc.year = (byte) (calendar.get(Calendar.YEAR) % 100);
        rtc.mon = (byte) (calendar.get(Calendar.MONTH)+1);
        rtc.day = (byte) calendar.get(Calendar.DATE);
        rtc.hour = (byte) calendar.get(Calendar.HOUR_OF_DAY);
        rtc.min = (byte) calendar.get(Calendar.MINUTE);
        rtc.sec = (byte) calendar.get(Calendar.SECOND);
        return rtc;
    }

}
