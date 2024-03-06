package com.blk.sdk;

import static com.blk.sdk.c.memcpy;
import static com.blk.sdk.c.memset;
import static com.blk.sdk.c.strlen;

import androidx.annotation.NonNull;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Created by idris on 21.02.2018.
 */

public class string {
    public static Charset w1254 = Charset.forName("windows-1254");

    StringBuilder sb;
    public string(byte[] bytes)
    {
        sb = new StringBuilder(new String(bytes, 0, strlen(bytes), w1254));
    }
    public string(byte[] bytes, int offset, int len)
    {
        sb = new StringBuilder(new String(bytes, offset, len, w1254));
    }
    public string(String str)
    {
        sb = new StringBuilder(new String(str.getBytes(w1254), w1254));
    }
    @NonNull
    public String toString()
    {
        return sb.toString();
    }

    public boolean StartsWith(String prefix) {
        return sb.toString().startsWith(prefix);
    }
    public boolean EndsWith(String suffix)
    {
        return sb.toString().endsWith(suffix);
    }
    public string PadLeft(int totalWidth, char paddingChar)
    {
        if (totalWidth <= sb.length())	return this;

        char[] pad = new char[totalWidth - sb.length()];
        Arrays.fill(pad, paddingChar);
        sb.insert(0, pad);
        return this;
    }
    public string PadRight(int totalWidth, char paddingChar)
    {
        if (totalWidth <= sb.length())	return this;

        char[] pad = new char[totalWidth - sb.length()];
        Arrays.fill(pad, paddingChar);
        sb.append(pad);
        return this;
    }
    public string Pad(int totalWidth, char paddingChar)
    {
        return PadLeft((totalWidth + sb.length()) / 2, paddingChar).PadRight(totalWidth, paddingChar);
    }
    public string PadCenter(String rightText, int totalWidth, char paddingChar /*= ' '*/ )
    {
        PadRight(totalWidth, paddingChar);
        int st = Math.max(0, totalWidth - rightText.length());
        int len = Math.min(totalWidth, rightText.length());
        sb.replace(st, st + len, rightText);
        return this;
    }
    public string Reverse()
    {
        sb.reverse();
        return this;
    }
    public string TrimStart(char ch)
    {
        int start = 0;
        for (start = 0; start < sb.length(); ++start) {
            if (sb.charAt(start) != ch)
                break;
        }
        sb.delete(0, start);
        return this;
    }

    public static String[] Split(String str, String delim)
    {
        return str.split(delim);
    }
    public static String Join(String[] lines, String delim)
    {
        String s = new String ();

        for (String line: lines) {
            s += line + delim;
        }
        return s;
    }
    public static String DeleteChar(String str, char ch)
    {
        StringBuilder s = new StringBuilder(str);
        String sCH = new String(new char[] {ch});
        for (int i = s.indexOf(sCH); i >= 0; i = s.indexOf(sCH)) {
            s.deleteCharAt(i);
        }
        return s.toString();
    }
    public static boolean IsSpace(String str)
    {
        for (int i = 0; i < str.length(); ++i)
        {
            if (!Character.isWhitespace(str.charAt(i)))
                return false;
        }
        return true;
    }
    public static String PadLeft(String str, int totalWidth, char paddingChar)
    {
        String p = "";
        for (int i = 0; i < totalWidth - str.length(); ++i)
            p += paddingChar;
        return p + str;
    }
    public static byte[] TrimEnd(byte[] in, byte junk) {
        for (int i = c.strlen(in) - 1; i >= 0; --i) {
            if (in[i] != junk)
                break;
            in[i] = 0;
        }
        return in;
    }

    public static byte[] TrimStart(byte[] in, byte junk) {
        int start = 0;
        for (start = 0; start < in.length; ++start) {
            if (in[start] != junk)
                break;
        }
        int newLen = in.length - start;
        memcpy(in, 0, in, start, newLen);
        memset(in, newLen, (byte) 0, in.length - newLen);

        return in;
    }

    public static byte[] TrimStart(byte[] in, int inOffset, byte junk) {
        int start = 0;
        for (start = 0; start < in.length - inOffset; ++start) {
            if (in[inOffset + start] != junk)
                break;
        }
        int newLen = in.length - (inOffset + start);
        memcpy(in, inOffset, in, inOffset + start, newLen);
        memset(in, inOffset + newLen, (byte) 0, start);

        return in;
    }

    public static byte[] Trim(byte[] in, byte junk) {
        return TrimStart(TrimEnd(in, junk), junk);
    }
}
