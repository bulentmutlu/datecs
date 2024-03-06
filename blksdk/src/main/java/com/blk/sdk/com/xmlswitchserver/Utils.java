package com.blk.sdk.com.xmlswitchserver;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Hashtable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

class Utils {
    public static String STX = hexToAscii("02");
    public static String ETX = hexToAscii("03");
    public static String EOT = hexToAscii("04");
    public static String ENQ = hexToAscii("05");
    public static String ACK = hexToAscii("06");
    public static String DLE = hexToAscii("10");
    public static String NAK = hexToAscii("15");
    public static String ETB = hexToAscii("17");
    public static String CAN = hexToAscii("18");

    
    public static byte[] headerkey = {
        (byte)0x90, (byte)0x32, (byte)0xA5, (byte)0x83, (byte)0xFC, (byte)0x17, (byte)0x11, (byte)0x30, 
        (byte)0x0D, (byte)0x9B, (byte)0x44, (byte)0x36, (byte)0xC8, (byte)0x65, (byte)0x01, (byte)0x76,
        (byte)0x90, (byte)0x32, (byte)0xA5, (byte)0x83, (byte)0xFC, (byte)0x17, (byte)0x11, (byte)0x30
    };
    
    public static byte[] messagekey = {
        (byte)0x6A, (byte)0x33, (byte)0xDC, (byte)0x5B, (byte)0x2C, (byte)0xDB, (byte)0x56, (byte)0x2C, 
        (byte)0x79, (byte)0xF3, (byte)0xCF, (byte)0xE2, (byte)0x3D, (byte)0x49, (byte)0xDA, (byte)0x39,
        (byte)0x6A, (byte)0x33, (byte)0xDC, (byte)0x5B, (byte)0x2C, (byte)0xDB, (byte)0x56, (byte)0x2C
    };
    
    private static final int[] CRCTable =        {
        0x0000, 0xC0C1, 0xC181, 0x0140, 0xC301, 0x03C0, 0x0280, 0xC241,   
        0xC601, 0x06C0, 0x0780, 0xC741, 0x0500, 0xC5C1, 0xC481, 0x0440,   
        0xCC01, 0x0CC0, 0x0D80, 0xCD41, 0x0F00, 0xCFC1, 0xCE81, 0x0E40,   
        0x0A00, 0xCAC1, 0xCB81, 0x0B40, 0xC901, 0x09C0, 0x0880, 0xC841,   
        0xD801, 0x18C0, 0x1980, 0xD941, 0x1B00, 0xDBC1, 0xDA81, 0x1A40,   
        0x1E00, 0xDEC1, 0xDF81, 0x1F40, 0xDD01, 0x1DC0, 0x1C80, 0xDC41,   
        0x1400, 0xD4C1, 0xD581, 0x1540, 0xD701, 0x17C0, 0x1680, 0xD641,   
        0xD201, 0x12C0, 0x1380, 0xD341, 0x1100, 0xD1C1, 0xD081, 0x1040,   
        0xF001, 0x30C0, 0x3180, 0xF141, 0x3300, 0xF3C1, 0xF281, 0x3240,   
        0x3600, 0xF6C1, 0xF781, 0x3740, 0xF501, 0x35C0, 0x3480, 0xF441,   
        0x3C00, 0xFCC1, 0xFD81, 0x3D40, 0xFF01, 0x3FC0, 0x3E80, 0xFE41,   
        0xFA01, 0x3AC0, 0x3B80, 0xFB41, 0x3900, 0xF9C1, 0xF881, 0x3840,   
        0x2800, 0xE8C1, 0xE981, 0x2940, 0xEB01, 0x2BC0, 0x2A80, 0xEA41,   
        0xEE01, 0x2EC0, 0x2F80, 0xEF41, 0x2D00, 0xEDC1, 0xEC81, 0x2C40,   
        0xE401, 0x24C0, 0x2580, 0xE541, 0x2700, 0xE7C1, 0xE681, 0x2640,   
        0x2200, 0xE2C1, 0xE381, 0x2340, 0xE101, 0x21C0, 0x2080, 0xE041,   
        0xA001, 0x60C0, 0x6180, 0xA141, 0x6300, 0xA3C1, 0xA281, 0x6240,   
        0x6600, 0xA6C1, 0xA781, 0x6740, 0xA501, 0x65C0, 0x6480, 0xA441,   
        0x6C00, 0xACC1, 0xAD81, 0x6D40, 0xAF01, 0x6FC0, 0x6E80, 0xAE41,   
        0xAA01, 0x6AC0, 0x6B80, 0xAB41, 0x6900, 0xA9C1, 0xA881, 0x6840,   
        0x7800, 0xB8C1, 0xB981, 0x7940, 0xBB01, 0x7BC0, 0x7A80, 0xBA41,   
        0xBE01, 0x7EC0, 0x7F80, 0xBF41, 0x7D00, 0xBDC1, 0xBC81, 0x7C40,   
        0xB401, 0x74C0, 0x7580, 0xB541, 0x7700, 0xB7C1, 0xB681, 0x7640,   
        0x7200, 0xB2C1, 0xB381, 0x7340, 0xB101, 0x71C0, 0x7080, 0xB041,   
        0x5000, 0x90C1, 0x9181, 0x5140, 0x9301, 0x53C0, 0x5280, 0x9241,   
        0x9601, 0x56C0, 0x5780, 0x9741, 0x5500, 0x95C1, 0x9481, 0x5440,   
        0x9C01, 0x5CC0, 0x5D80, 0x9D41, 0x5F00, 0x9FC1, 0x9E81, 0x5E40,   
        0x5A00, 0x9AC1, 0x9B81, 0x5B40, 0x9901, 0x59C0, 0x5880, 0x9841,   
        0x8801, 0x48C0, 0x4980, 0x8941, 0x4B00, 0x8BC1, 0x8A81, 0x4A40,   
        0x4E00, 0x8EC1, 0x8F81, 0x4F40, 0x8D01, 0x4DC0, 0x4C80, 0x8C41,   
        0x4400, 0x84C1, 0x8581, 0x4540, 0x8701, 0x47C0, 0x4680, 0x8641,   
        0x8201, 0x42C0, 0x4380, 0x8341, 0x4100, 0x81C1, 0x8081, 0x4040 
    };

    public static int Calculate(byte[] Value, int DataLen)
    {
        int CRCVal = 0x0000;
        //*lt3Tpj5w/qk=ad2KUJYCD/A8P+xQ+3DidQoUpil4/aIU3IaXdU/Z6sGs8ExgLHOkCPCTrl97aAewQ715Zp+wtXM=
        for (int i = 0; i < DataLen; i++)
        {
            CRCVal = (CRCVal >> 8) ^ CRCTable[(CRCVal & 0xff) ^ Value[i]];
        }
        return CRCVal;
    }
    
    public static String toHex(String arg) {
        return String.format("%040x", new BigInteger(arg.getBytes()));
    }
    
    public static String hexToAscii(String s) {
        int n = s.length();
        StringBuilder sb = new StringBuilder(n / 2);
        for (int i = 0; i < n; i += 2) {
            char a = s.charAt(i);
            char b = s.charAt(i + 1);
            sb.append((char) ((hexToInt(a) << 4) | hexToInt(b)));
        }
        return sb.toString();
    }
    
    public static String ToHexString(byte[] bytes){
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < bytes.length; i++)
            hexString.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        return hexString.toString();
    }
    
    private static int hexToInt(char ch) {
        if ('a' <= ch && ch <= 'f') { return ch - 'a' + 10; }
        if ('A' <= ch && ch <= 'F') { return ch - 'A' + 10; }
        if ('0' <= ch && ch <= '9') { return ch - '0'; }
        throw new IllegalArgumentException(String.valueOf(ch));
    }

    public static byte[] CompressDecompress(byte[] source, boolean isCompress)
    {
        try{
            if (isCompress)
            {
                return fromByteToGByte(source);
                /*ByteArrayOutputStream compressedStream = new ByteArrayOutputStream();

                DeflaterOutputStream inflater = new DeflaterOutputStream(compressedStream);
                inflater.write(source, 0, source.length);
                inflater.close();
                
                return compressedStream.toByteArray();*/
            }else{
                return fromGByteToByte(source);
                /*ByteArrayOutputStream uncompressedStream = new ByteArrayOutputStream();
                ByteArrayInputStream compressedStream = new ByteArrayInputStream(source);
                InflaterInputStream inflater = new InflaterInputStream(compressedStream);

                int c;
                while ((c = inflater.read()) != -1)
                {
                    uncompressedStream.write(c);
                }
                return uncompressedStream.toByteArray();*/
            }
        }catch(Exception ex){
            ex.toString();
            return null;
        }
    }

    public static byte[] Encrypt(byte[] key, byte[] plaintext, int inputOffset, int inputCount)
    {
        try{ 
            Cipher c = Cipher.getInstance("DESede/ECB/PKCS5Padding");
            c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "DESede"));
            byte[] encrypted = c.doFinal(plaintext, inputOffset, inputCount);
            return encrypted;
        }catch(Exception ex){
            ex.toString();
            return null;
        }
    }

    public static byte[] Decrypt(byte[] key, byte[] ciphertext, int inputOffset, int inputCount)
    {
        try{
            Cipher c = Cipher.getInstance("DESede/ECB/NoPadding");
            c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "DESede"));
            byte[] decrypted = c.doFinal(ciphertext, inputOffset, inputCount);
            return decrypted;
        }catch(Exception ex){
            System.out.println("ssss " + ex.toString());
            return null;
        }
    }
    
    public static String bcdToString(byte[] bcd){
  StringBuilder s = new StringBuilder(bcd.length*2);
  char c = ' ';
  for(byte b : bcd){
    switch((b>>4)&0xF){
      case 0: c = '0';break;
      case 1: c = '1';break;
      case 2: c = '2';break;
      case 3: c = '3';break;
      case 4: c = '4';break;
      case 5: c = '5';break;
      case 6: c = '6';break;
      case 7: c = '7';break;
      case 8: c = '8';break;
      case 9: c = '9';break;
      default: throw new IllegalArgumentException("Bad Decimal: "+((b>>4)&0xF));
    }
    s.append(c);
    switch(b&0xF){
      case 0: c = '0';break;
      case 1: c = '1';break;
      case 2: c = '2';break;
      case 3: c = '3';break;
      case 4: c = '4';break;
      case 5: c = '5';break;
      case 6: c = '6';break;
      case 7: c = '7';break;
      case 8: c = '8';break;
      case 9: c = '9';break;
      default: throw new IllegalArgumentException("Bad Decimal: "+(b&0xF));
    }
    s.append(c);
  }
  /* If you want to remove a leading zero:
     if(s.charAt(0) == '0') return s.substring(1);
  */
  return s.toString();
}
    
    private static Object resizeArray (Object oldArray, int newSize) {
        int oldSize = java.lang.reflect.Array.getLength(oldArray);
        Class elementType = oldArray.getClass().getComponentType();
        Object newArray = java.lang.reflect.Array.newInstance(elementType,newSize);
        int preserveLength = Math.min(oldSize,newSize);
        if (preserveLength > 0)
        System.arraycopy(oldArray,0,newArray,0,preserveLength);
        return newArray;
    }
    
    public static byte[] Normalize(byte[] pr){
        int iLen = pr.length;
        if (iLen % 8 != 0)
        {
            int l1 = iLen;
            byte pc = (byte)(8 - (iLen % 8));
            iLen = iLen + (8 - (iLen % 8));
            pr = (byte[]) resizeArray(pr, iLen);
            for (int i = l1; i < iLen; ++i) pr[i] = pc;
        }
        return pr;
    }
            
    
    public static byte[] asc_bcd(byte[] src, int l_src)
    {
        int i, j = 0, k = 0;
        byte[] Ptd = new byte[l_src + 1];

        for (i = 0; i < ((l_src + 1) / 2); i++)
        {
            if ((l_src % 2) == 0 || i != 0)
            {
                Ptd[k] = (byte)((src[j++] << 4) & 0xF0);
            }
            if (j >= src.length)
            {
                return Ptd;
            }
            Ptd[k] = (byte)(Ptd[k] + (((src[j++])) & 0x0F));
            k++;
        }
        return Ptd;
    }

    public static byte[] bcd_asc(byte[] dst, byte[] src, int l_src)
    {
        int i, j = 0;

        for (i = 0; i < (l_src / 2) + (l_src % 2); i++)
        {
            if (i == 0 && (l_src % 2 != 0)) //Skip first half byte if length is odd
            {
                dst[j++] = (byte)((src[i] & 0x0F) + 0x30);
            }
            else
            {
                dst[j++] = (byte)((src[i] >> 4) + 0x30);
                dst[j++] = (byte)((src[i] & 0x0F) + 0x30);
            }
        }

        return dst;
    }
    
    public static byte[] fromByteToGByte(byte[] bytes) {
        ByteArrayOutputStream baos = null;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            baos = new ByteArrayOutputStream();
            GZIPOutputStream gzos = new GZIPOutputStream(baos);
            byte[] buffer = new byte[1024];
            int len;
            while((len = bais.read(buffer)) >= 0) {
                gzos.write(buffer, 0, len);
            }
            gzos.close();
            baos.close();
        }catch (IOException e) {
            e.toString();
        }
        return(baos.toByteArray());
    }
    
    public static byte[] fromGByteToByte(byte[] gbytes) {
        ByteArrayOutputStream baos = null;
        ByteArrayInputStream bais = new ByteArrayInputStream(gbytes);
        try {
            baos = new ByteArrayOutputStream();
            GZIPInputStream gzis = new GZIPInputStream(bais);
            byte[] bytes = new byte[1024];
            int len;
            while((len = gzis.read(bytes)) > 0) {
                baos.write(bytes, 0, len);
            }
        } catch (IOException e) {
            System.out.println(e.toString());
        }
        return(baos.toByteArray());
    }
    
    public static int b64encode(byte[] from, byte[] to, int length, int quads)
    {
        byte c, d;
        int toIdx = 0;

        int i = 0;

        int qc = 0;
        while (i < length)
        {
            c = from[i];
            to[toIdx++] = ntc((byte) (c / 4));
            c = (byte)(c * 64);

            i++;
            if (i >= length)
            {
                to[toIdx++] = ntc((byte) (c / 4));
                to[toIdx++] = '=';
                to[toIdx++] = '=';
                break;
            }

            d = from[i];
            to[toIdx++] = ntc((byte) (c / 4 + d / 16));
            d = (byte)(d * 16);

            i++;

            if (i >= length)
            {
                to[toIdx++] = ntc((byte) (d / 4));
                to[toIdx++] = '=';
                break;
            }

            c = from[i];
            to[toIdx++] = ntc((byte) (d / 4 + c / 64));
            c = (byte)(c * 4);

            i++;

            to[toIdx++] = ntc((byte) (c / 4));


            qc++;
            if (qc == quads)
            {
                to[toIdx++] = '\n';
                qc = 0;
            }

        }


        return toIdx;

    }



    public static int b64decode(byte[] from, int fromIdx, byte[] to, int length)
    {
        int c, d, e, f;
        int A, B, C;
        int add;
        int toIdx = 0;

        for (int i = 0; i + 3 < length; )
        {
            add = 0;
            A = B = C = 0;
            c = d = e = f = 100;

            while ((c == 100) && (i < length)) c = ctn(from[fromIdx + i++]);
            while ((d == 100) && (i < length)) d = ctn(from[fromIdx + i++]);
            while ((e == 100) && (i < length)) e = ctn(from[fromIdx + i++]);
            while ((f == 100) && (i < length)) f = ctn(from[fromIdx + i++]);

            if (f == 100)
                return -1; /* Not valid end */

            if (c < 64)
            {
                A += c * 4;
                if (d < 64)
                {
                    A += d / 16;
                    B += d * 16;
                    if (e < 64)
                    {
                        B += e / 4;
                        C += e * 64;

                        if (f < 64)
                        {
                            C += f;
                            to[toIdx + 2] = (byte)C;
                            add += 1;
                        }
                        to[toIdx + 1] = (byte)B;
                        add += 1;
                    }
                    to[toIdx] = (byte)A;
                    add += 1;
                }
            }
            toIdx += add;

            if (f == 80) return toIdx; /* end because '=' encountered */
        }
        return toIdx;
    }
    
    public static byte ntc(byte n)
    {
        int t = n;
        if (t < 26) return (byte)('A' + t);
        if (t < 52) return (byte)('a' - 26 + t);
        if (t < 62) return (byte)('0' - 52 + t);
        if (t == 62) return '+';
        return '/';
    }

    public static byte ctn(byte c)
    {
        int t = c;

        if (t == '/') return 63;
        if (t == '+') return 62;
        if ((t >= 'A') && (t <= 'Z')) return (byte)(t - 'A');
        if ((t >= 'a') && (t <= 'z')) return (byte)(t - 'a' + 26);
        if ((t >= '0') && (t <= '9')) return (byte)(t - '0' + 52);
        if (t == '=') return 80;
        return 100;
    }


    private static Bitmap toBufferedImageBMP(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();

        if (width % 8 != 0) {
            return null;
        } else {
            int[] pixels = new int[width * height];

            for(int y = 0; y < height; ++y) {
                for(int x = 0; x < width; ++x) {
                    if (matrix.get(x, y)) {
                        pixels[y * width + x] = -16777216;
                    } else {
                        pixels[y * width + x] = -1;
                    }
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        }
    }
    public static Bitmap getRQBMP(String str, Integer height) {
        if (height == null) {
            height = 240;
        }

        if (height > 400) {
            height = 400;
        }

        height = height - height % 8;

        try {
            Hashtable<EncodeHintType, Object> hints = new Hashtable();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            hints.put(EncodeHintType.MARGIN, 0);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            //LogUtil.si(QRUtil.class, "Generate qr code height = " + height);
            BitMatrix bitMatrix = (new MultiFormatWriter()).encode(str, BarcodeFormat.QR_CODE, height, height, hints);
            return toBufferedImageBMP(bitMatrix);
        } catch (Exception var4) {
            var4.printStackTrace();
            return null;
        }
    }

    public static Bitmap getBarcodeBMP(String str, Integer width, Integer height) {
        if (width == null) {
            width = 240;
        }

        if (width > 400) {
            width = 400;
        }

        width = width - width % 8;

        try {
            Hashtable<EncodeHintType, Object> hints = new Hashtable();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            hints.put(EncodeHintType.MARGIN, 0);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            //LogUtil.si(QRUtil.class, "Generate a one-dimensional code width = " + width + " height = " + height);
            BitMatrix bitMatrix = (new MultiFormatWriter()).encode(str, BarcodeFormat.CODE_128, width, height, hints);
            return toBufferedImageBMP(bitMatrix);
        } catch (Exception var5) {
            var5.printStackTrace();
            return null;
        }
    }
}
