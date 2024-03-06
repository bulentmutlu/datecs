package com.blk.sdk;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Created by id on 28.02.2018.
 */

public class Convert {


    public static short SWAP_UINT16(short x)
    {
        return Short.reverseBytes(x);
        //return (short) (((x) >>> 8) | ((x) << 8));
    }
    public static int SWAP_UINT32(int x)
    {
        return Integer.reverseBytes(x);
        //return (((x) >>> 24) | (((x) & 0x00FF0000) >>> 8) | (((x) & 0x0000FF00) << 8) | ((x) << 24));
    }
    public static long SWAP_UINT64(long x)
    {
        return Long.reverseBytes(x);
        //return (((x) >>> 24) | (((x) & 0x00FF0000) >>> 8) | (((x) & 0x0000FF00) << 8) | ((x) << 24));
    }


    public static byte EP_asciibcd2bin(byte b)
    {
        if (b >= 0x41 && b <= 0x46)
            return (byte) (b-0x37);
        else
            return (byte) (b-0x30);
    }

    public static short EP_ascii2bcd(byte[] dst, int dstOffset, final byte[] src, int srcLen)
    {
        short i;
        byte x;
        byte[] SRC = src;

        int mod = srcLen % 2;
        if (mod != 0) {
            SRC = new byte[srcLen + 1];
            SRC[0] = '0';
            c.memcpy(SRC, 1, src, 0, srcLen);
            srcLen++;
        }

        for (i=0; i < srcLen; i++)
        {
            x = EP_asciibcd2bin(SRC[i]);
            if (x > 0x0f)
                break;
            if ((i&1) != 0)
            {
                dst[dstOffset + i/2] &= 0xf0;
                dst[dstOffset + i/2] |= x;
            }
            else
            {
                dst[dstOffset + i/2] = (byte) ((x<<4)|0x0f);
            }
        }

        return i;
    }
    public static byte[] Ascii2Bcd(final byte[] source)
    {
        return Ascii2Bcd(source, 0, source.length);
    }
    public static byte[] Ascii2Bcd(final byte[] source, int offset, int len)
    {
        byte[] SRC;

        if (len % 2 != 0) {
            SRC = new byte[len + 1];
            SRC[0] = '0';
            c.memcpy(SRC, 1, source, offset, len);
            len++;
        }
        else {
            SRC = Arrays.copyOfRange(source, offset, offset + len);
        }

        byte[] dst = new byte[len / 2];

        for (int i=0; i < len; i++)
        {
            byte x = EP_asciibcd2bin(SRC[i]);
            if (x > 0x0f)
                break;
            if ((i&1) != 0)
            {
                dst[i/2] &= 0xf0;
                dst[i/2] |= x;
            }
            else
            {
                dst[i/2] = (byte) ((x<<4)|0x0f);
            }
        }

        return dst;
    }

    public static byte[] bcd2Str(byte[] bytes) {
        byte[] dest = new byte[bytes.length * 2];
        EP_bcd2str(dest, bytes, bytes.length);
        return dest;
    }
    public static byte[] bcd2Str(byte[] bytes, int len) {
        byte[] dest = new byte[len * 2];
        EP_bcd2str(dest, bytes, len);
        return dest;
    }

    public static byte[] str2Bcd(final byte[] src)
    {
        byte[] dest = new byte[src.length / 2];
        EP_ascii2bcd(dest, 0, src, src.length);
        return dest;
    }

    public static byte[] int2Bcd(int input, int outLength)
    {
        return Convert.str2Bcd(new string("" + input).
                PadLeft(outLength * 2, '0').toString().getBytes());
    }
    public static int bcdToInt(final byte[] bcd, int offset, int length)
    {
        return Integer.parseInt(Convert.bcdToStr(bcd, offset, length));
    }

    public static byte EP_bytebcd2bin(byte x)
    {
        return (byte) ((x / 0x10)* 10 + (x % 0x10));
    }

    public static byte EP_bytebin2bcd(byte x)
    {
        return (byte) ((x / 10)*0x10 + (x % 10));
    }

    public static String bcdToStr(byte[] b, int offset, int len) throws IllegalArgumentException {
        if (b == null || offset + len > b.length) {
            throw new IllegalArgumentException("bcdToStr input arg is null");
        }

        char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        StringBuilder sb = new StringBuilder(len * 2);
        for (int i = 0; i < len; i++) {
            sb.append(HEX_DIGITS[(b[offset + i] & 0xf0) >>> 4]);
            sb.append(HEX_DIGITS[b[offset + i] & 0x0f]);
        }

        return sb.toString();
    }
    public static String bcdToStr(byte[] b, int len) throws IllegalArgumentException {
        return bcdToStr(b, 0, len);
    }

    public static void EP_bcd2str(byte[] dest, byte[] source, int sourceLen)
    {
        byte[] b = bcdToStr(source, 0, sourceLen).getBytes();
        c.memcpy(dest, b, b.length);

//        int i,k, sI = 0;
//        k=(sourceLen+1)/2;
//
//        if (sourceLen % 2 != 0) k=1;
//        else k=0;
//
//        for (i=0; i < sourceLen * 2 && sI < sourceLen; i++)
//        {
//            if (k != 0) {
//			    dest[i] = (byte) (unsignedByteToInt((byte) (source[sI++] & 0x0f)) + 0x30);
//                k=0;
//            }
//            else {
//			    dest[i] = (byte) (unsignedByteToInt((byte) (unsignedByteToInt(source[sI]) >>> 4))     + 0x30);
//                k=1;
//            }
//        }
//	    if (dest.length > i) dest[i]=0;
//
//	    if (c.memcmp(bcdToStr(source, sourceLen).getBytes(), dest, sourceLen * 2)) {
//            try {
//                throw new Exception("bcdStr not equeal");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
    }
    public static int unsignedByteToInt(byte b) {
        return (int) b & 0xFF;
    }
    public static int unsignedShortToInt(short s)
    {
        return (int) s & 0xffff;
    }

    public static byte[] ToArray(long l) {

        byte[] to = new byte[8];

        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            to[0] = (byte) ((l >>> 56) & 0xff);
            to[1] = (byte) ((l >>> 48) & 0xff);
            to[2] = (byte) ((l >>> 40) & 0xff);
            to[3] = (byte) ((l >>> 32) & 0xff);
            to[4] = (byte) ((l >>> 24) & 0xff);
            to[5] = (byte) ((l >>> 16) & 0xff);
            to[6] = (byte) ((l >>> 8) & 0xff);
            to[7] = (byte) (l & 0xff);
        } else {
            to[7] = (byte) ((l >>> 56) & 0xff);
            to[6] = (byte) ((l >>> 48) & 0xff);
            to[5] = (byte) ((l >>> 40) & 0xff);
            to[4] = (byte) ((l >>> 32) & 0xff);
            to[3] = (byte) ((l >>> 24) & 0xff);
            to[2] = (byte) ((l >>> 16) & 0xff);
            to[1] = (byte) ((l >>> 8) & 0xff);
            to[0] = (byte) (l & 0xff);
        }

        return to;
    }
    public static byte[] ToArray(int i)  {

        byte[] to = new byte[4];

        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            to[0] = (byte) ((i >>> 24) & 0xff);
            to[1] = (byte) ((i >>> 16) & 0xff);
            to[2] = (byte) ((i >>> 8) & 0xff);
            to[3] = (byte) (i & 0xff);
        } else {
            to[0] = (byte) (i & 0xff);
            to[1] = (byte) ((i >>> 8) & 0xff);
            to[2] = (byte) ((i >>> 16) & 0xff);
            to[3] = (byte) ((i >>> 24) & 0xff);
        }

        return to;
    }
    public static byte[] ToArray(short s) {

        byte[] to = new byte[2];

        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            to[0] = (byte) ((s >>> 8) & 0xff);
            to[1] = (byte) (s & 0xff);
        } else {
            to[0] = (byte) (s & 0xff);
            to[1] = (byte) ((s >>> 8) & 0xff);
        }

        return to;
    }

    public static long ToLong(byte[] from, int offset) {

        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            return ((from[offset] << 56) & 0xff00000000000000L) | ((from[offset + 1] << 48) & 0xff000000000000L)
                    | ((from[offset + 2] << 40) & 0xff0000000000L) | ((from[offset + 3] << 32) & 0xff00000000L)
                    | ((from[offset + 4] << 24) & 0xff000000) | ((from[offset + 5] << 16) & 0xff0000)
                    | ((from[offset + 6] << 8) & 0xff00) | (from[offset + 7] & 0xff);
        } else {
            return ((from[offset + 7] << 56) & 0xff00000000000000L) | ((from[offset + 6] << 48) & 0xff000000000000L)
                    | ((from[offset + 5] << 40) & 0xff0000000000L) | ((from[offset + 4] << 32) & 0xff00000000L)
                    | ((from[offset + 3] << 24) & 0xff000000) | ((from[offset + 2] << 16) & 0xff0000)
                    | ((from[offset + 1] << 8) & 0xff00) | (from[offset] & 0xff);
        }
    }
    public static int ToInt(byte[] from, int offset) throws IllegalArgumentException {

        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            return ((from[offset] << 24) & 0xff000000) | ((from[offset + 1] << 16) & 0xff0000)
                    | ((from[offset + 2] << 8) & 0xff00) | (from[offset + 3] & 0xff);
        } else {
            return ((from[offset + 3] << 24) & 0xff000000) | ((from[offset + 2] << 16) & 0xff0000)
                    | ((from[offset + 1] << 8) & 0xff00) | (from[offset] & 0xff);
        }
    }
    public static short ToShort(byte[] from, int offset) throws IllegalArgumentException {

        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            return (short) (((from[offset] << 8) & 0xff00) | (from[offset + 1] & 0xff));
        } else {
            return (short) (((from[offset + 1] << 8) & 0xff00) | (from[offset] & 0xff));
        }
    }


    // len = Out.len = In.len / 2
    // "00010A0F10" --> byte[] {0x00, 0x01, 0x0A, 0x0F, 0x10}
    public static void EP_AscHex(byte[] Out, byte[] In, int len)
    {
        int i, outIndex = 0;
        byte TempChar;

        for (i = 0; i < (len * 2) && i < In.length; i++) {
            TempChar = In[i];
            if ((TempChar >= 0x61) && (TempChar <= 0x66))					// If small letters     a,b,c,d,e,f
                TempChar -= 0x20;											// Convert to capitals  A,B,C,D,E,F
            if (!(((TempChar >= 0x41) && (TempChar <= 0x46)) || ((TempChar >= 0x30) && (TempChar <= 0x39))))
                TempChar = 0x46;											// If not in A-F or 0-9 interval, convert to 'F'.

            if (i % 2 != 0) {
                if ((TempChar >= 0x41) && (TempChar <= 0x46)) {
                    Out[outIndex] |= ((TempChar - 0x37));
                }
                else {
                    Out[outIndex] |= ((TempChar - 0x30) & 0x0F);
                }
                outIndex++;
            }
            else {
                if ((TempChar >= 0x41) && (TempChar <= 0x46)) {
                    Out[outIndex] = (byte) ((TempChar - 0x37) << 4);
                }
                else {
                    Out[outIndex] = (byte) (TempChar << 4);
                }
            }
        }
        return;
    }
    // byte[] {0x00, 0x01, 0x0A, 0x0F, 0x10} --> byte[] {48, 48, 48, 49, 48, 65, 48, 70, 49, 48} "00010A0F10"
    public static void EP_BfAscii(byte[] Out, byte[] In, int inOffset, int len)
    {
        int i, outIndex = 0;
        int TempByte, TempVal;

        for (i = 0; i < len; i++) {
            TempByte = ((In[i + inOffset] & 0xF0) >>> 4);
            TempVal = (0x30 + TempByte);
            if ((TempByte >= 0x0A) && (TempByte <= 0x0F))
                TempVal += 0x07;
            Out[outIndex] = (byte) TempVal;
            outIndex++;
            TempByte = (In[i + inOffset] & 0x0F);
            TempVal = (0x30 + TempByte);
            if ((TempByte >= 0x0A) && (TempByte <= 0x0F))
                TempVal += 0x07;
            Out[outIndex] = (byte) TempVal;
            outIndex++;
        }
        return;
    }

    // byte[] {0x00, 0x01, 0x0A, 0x0F, 0x10} --> byte[] {48, 48, 48, 49, 48, 65, 48, 70, 49, 48} "00010A0F10"
    public static String Buffer2Hex(byte[] buf, int offset, int len)
    {
        byte[] out = new byte[len * 2];
        EP_BfAscii(out, buf, offset, len);
        return new String(out);

//        String s = "";
//        for (int i = offset; i < offset + len && i < buf.length; ++i)
//        {
//            byte b = buf[i];
//            s += String.format("%02X", b);
//        }
//        return s;
    }
    // byte[] {0x00, 0x01, 0x0A, 0x0F, 0x10} --> byte[] {48, 48, 48, 49, 48, 65, 48, 70, 49, 48} "00010A0F10"
    public static String Buffer2Hex(byte[] buf)
    {
        return Buffer2Hex(buf, 0, buf.length);
    }

    // len should be even.
    // "00010A0F10" --> byte[] {0x00, 0x01, 0x0A, 0x0F, 0x10}
    public static byte[] Hex2Buffer(byte[] ascii, int offset, int len)
    {
        byte[] out = new byte[len / 2];
        EP_AscHex(out, ascii, len / 2);
        return out;
    }
    public static byte[] Hex2Buffer(byte[] ascii)
    {
        int len = ascii.length;
        byte[] out = new byte[len / 2];
        EP_AscHex(out, ascii, len / 2);
        return out;
    }
    public static byte[] hexString2ByteArray(String hexString)
    {
        byte[] byteArray = new byte[hexString.length() / 2];
        for (int i = 0; i < byteArray.length; i++)
        {
            byteArray[i] = (byte) Integer.parseInt(hexString.substring(2 * i, 2 * i + 2), 16);
        }

        return byteArray;
    }

    public static String byteArray2HexString(byte[] array, int len)
    {
        StringBuffer hexString = new StringBuffer();

        for (int i = 0 ; i < len ; i++)
        {
            int intVal = array[i] & 0xFF;

            if (intVal < 0x10)
            {
                hexString.append("0");
            }

            hexString.append(Integer.toHexString(intVal).toUpperCase());
        }
        return hexString.toString();
    }
    public static String byteArray2HexString(byte[] array)
    {
        return byteArray2HexString(array, array.length);
    }

    public static void Test()
    {
        byte[] out = new byte[32];
        byte[] out2 = new byte[32];
        byte[] in = new byte[] {0x00, 0x01, 0x09, 0x43, (byte) 0xAB};
        Convert.EP_bcd2str(out, in, in.length);

        Convert.EP_BfAscii(out2, in, 0, in.length);
        Convert.EP_AscHex(out2, "00010A0F10".getBytes(), 5);


        for (int i = 0; i < 10; ++i)
        {
            byte[] buf = new byte[256];
            Utility.OsGetRandom(buf, buf.length);

            String s1 = Convert.Buffer2Hex(buf, 0, buf.length);
            String s2 = Convert.Buffer2Hex(buf);

            Utility.log("a");
            Utility.log(s1);
            Utility.log(s2);

            //Assert.assertTrue(s1.equals(s2));
        }
    }
}
