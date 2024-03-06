package com.blk.sdk;

import java.util.Arrays;

/**
 * Created by id on 1.03.2018.
 */

public class c {

    public static void memcpy(byte[] dest, byte[] src, int len)
    {
        System.arraycopy(src, 0, dest, 0, len);
    }
    public static void memcpy(byte[] dest, int dStartIndex, byte[] src, int sStartIndex, int len)
    {
        System.arraycopy(src, sStartIndex, dest, dStartIndex, len);
    }

    public static void memset(byte[] b, byte val, int len)
    {
       memset(b, 0, val, len);
    }
    public static void memset(byte[] b, int fromIndex, byte val, int len)
    {
        if (len == 0) return;
        Arrays.fill(b, fromIndex, fromIndex + len, val);
    }
    // memcmp returns 0 if equals. we return false if equals
    public static boolean memcmp(byte[] m1, byte[] m2, int len)
    {
        if (m1.length == m2.length && m1.length == len)
            return !Arrays.equals(m1, m2);

        for (int i = 0; i < len; ++i)
        {
            if (m1[i] != m2[i]) return true;
        }
        return false;
    }

    public static void sprintf(byte[] out, String format, Object...args)
    {
        sprintf(out, 0, format, args);
    }
    public static void sprintf(byte[] out, int outIndex, String format, Object...args)
    {
        byte[] r = String.format(format, args).getBytes();
        System.arraycopy(r, 0, out, outIndex, r.length);
        if (out.length > outIndex + r.length)
            out[outIndex + r.length] = 0;
    }
    public static int atoi(byte[] s)
    {
        try {
            return Integer.parseInt(new String(s, 0, strlen(s)));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    public static long atol(byte[] s)
    {
        try {
            return Long.parseLong(new String(s, 0, strlen(s)));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static void strcpy(byte[] dest, String src)
    {
        strcpy(dest, c.ToBytes(src));
    }
    public static void strcpy(byte[] dest, byte[] src)
    {
        int len = strlen(src);
        System.arraycopy(src, 0, dest, 0, len);
        if (dest.length > len)
            dest[len] = 0;
    }
    public static void strncpy(byte[] dest, byte[] src, int n)
    {
        int len = Math.min(strlen(src), n);
        System.arraycopy(src, 0, dest, 0, len);
        if (dest.length > len)
            dest[len] = 0;
    }

    public static void strcat(byte[] dest, byte[] src)
    {
        System.arraycopy(src, 0, dest, strlen(dest), strlen(src));
        if (dest.length > strlen(dest))
            dest[strlen(dest)] = 0;
    }
    public static void strcat(byte[] dest, String src)
    {
        strcat(dest, c.ToBytes(src));
    }
    // strcmp returns 0 if equals. we return false if equals
    public static boolean strncmp(byte[] s1, byte[] s2, int len)
    {
        if ((s1.length < len || s2.length < len) && s1.length != s2.length) return true;

        for (int i = 0; i < len && i < s1.length && i < s2.length; ++i)
        {
            if (s1[i] != s2[i])
                return true;
        }
        return false; // equal
    }
    // strcmp returns 0 if equals. we return false if equals
    public static boolean strcmp(byte[] s1, byte[] s2)
    {
        int l1 = strlen(s1);
        if (l1 != strlen(s2)) return true;

        for (int i = 0; i < l1; ++i)
        {
            if (s1[i] != s2[i])
                return true;
        }
        return false; // equal
    }
    // strcmp returns 0 if equals. we return false if equals
    public static boolean strcmp(byte[] s1, String s2)
    {
        return strcmp(s1, s2.getBytes());
    }
    public static int strlen(byte[] s)
    {
        int destIndex = 0;
        for (byte b : s)
        {
            if (b == 0)
                break;
            ++destIndex;
        }
        return destIndex;
    }
    public static int sizeof (byte[] a)
    {
        return a.length;
    }

    public static boolean strstr(byte[] s1, byte[] s2)
    {
        return new String(s1).contains(new String(s2).subSequence(0, s2.length));
    }

    public static String ToString(byte[] b)
    {
        return new String(b, 0, strlen(b), string.w1254);
    }
    public static String ToString(byte[] b, int offset, int length)
    {
        return new String(b, offset, length, string.w1254);
    }
    public static byte[] ToBytes(String str)
    {
        return str.getBytes(string.w1254);
    }

}
