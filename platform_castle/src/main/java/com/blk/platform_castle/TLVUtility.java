package com.blk.platform_castle;

import android.util.Log;

import com.blk.sdk.Convert;
import com.blk.sdk.string;

import java.util.Arrays;

import CTOS.emv.TlvData;

public class TLVUtility
{
    static class tagInfo {
        int tagLen = 0;
        int lengthLen = 0;
    }

    int intTLVDataBaseLen;
    byte TLVDataBase[] = new byte[1024];


//    class TlvData {
//        public int version;
//        public int tag;
//        public int len;
//        public byte[] value;
//    }


    public boolean TLVDataGet(TlvData tlv)
    {
        int i = 0;
        TlvData tempData = new TlvData();
        tempData.value = new byte[256];

        while(i < intTLVDataBaseLen)
        {
            tagInfo ti = new tagInfo();

            getTLV(TLVDataBase, i, tempData, ti);
            if(tlv.tag == tempData.tag)
            {
                System.arraycopy(tempData.value, 0, tlv.value,0, tempData.len);
                tlv.len = tempData.len;
                return true;
            }
            i += tempData.len + ti.tagLen + ti.lengthLen;
        }

        return false;
    }

    public void TLVDataParse(byte[] buffer, int len)
    {
        int i = 0;

        TlvData tempTlvData = new TlvData();
        tempTlvData.value = new byte[256];

        while(i < len)
        {
            tempTlvData.tag = 0;
            tempTlvData.len = 0;
            Arrays.fill(tempTlvData.value, (byte)0);

            tagInfo ti = new tagInfo();
            getTLV(buffer, i, tempTlvData, ti);
//            Log.i("TLVUtility", "tag(" + new string(String.format("%06X", tempTlvData.tag)).TrimStart('0')
//                    +") len(" + tempTlvData.len + ") val("
//                    + Convert.Buffer2Hex(tempTlvData.value, tempTlvData.len) + ")");
            TLVDataAdd(tempTlvData);
            i += (tempTlvData.len + ti.tagLen + ti.lengthLen);
            //Log.i("", "i(" + i + ") " + tempTlvData.len + " " + ti.tagLen + " " + ti.lengthLen);
        }
    }
    public static void Dump(byte[] buffer, int len)
    {
        TLVUtility tlvUtility = new TLVUtility();
        int i = 0;

        while(i < len)
        {
            tagInfo ti = new TLVUtility.tagInfo();
            TlvData tempTlvData = new TlvData();
            tempTlvData.value = new byte[256];

            if(i > 0)
            {
                Arrays.fill(tempTlvData.value, (byte)0);
            }

            tlvUtility.getTLV(buffer, i, tempTlvData, ti);
            Log.i("TLVUtility", "tag(" + new string(String.format("%06X", tempTlvData.tag)).TrimStart('0')
                    +") len(" + tempTlvData.len + ") val("
                    + Convert.Buffer2Hex(tempTlvData.value, 0, tempTlvData.len) + ")");
            tlvUtility.TLVDataAdd(tempTlvData);
            i += (tempTlvData.len + ti.tagLen + ti.lengthLen);
            //Log.i("", "i(" + i + ") " + tempTlvData.len + " " + ti.tagLen + " " + ti.lengthLen);
        }
    }

    public int getTLV(byte[] buffer, int index, TlvData tlv, tagInfo ti)
    {
        int length_Len = 0;

        if((Convert.unsignedByteToInt(buffer[index]) & 0x1F) == 0X1F)
        {
            if((buffer[index + 1] & 0x80) == 0x80)
            {
                //3 bytes tag
                tlv.tag = ((buffer[index + 2] & 0xFF)) | ((buffer[index + 1] & 0xFF)<< 8) | ((buffer[index] & 0xFF)<< 16);
                index += 3;
                ti.tagLen = 3;
                //Log.d(TAG, "tag" + String.format("0x%06X", tlv.tag));
            }
            else
            {
                //2 bytes tag
                tlv.tag = ((buffer[index + 1] & 0xFF)) | ((buffer[index] & 0xFF)<< 8); //buffer[i]*256 + buffer[i + 1];
                index += 2;
                ti.tagLen = 2;
                //Log.d(TAG, "tag" + String.format("0x%04X", tlv.tag));
            }

        }
        else
        {
            //1 byte tag
            tlv.tag = (buffer[index] & 0xFF);
            index += 1;
            ti.tagLen = 1;
            //Log.d(TAG, "tag" + String.format("0x%02X", tlv.tag));
        }

        if ((buffer[index] & (byte)0x80) == (byte)0x80)
        {
            length_Len = 1 + (buffer[index] & 0x7F);
            if (length_Len == 2)
            {
                tlv.len = (int)(buffer[index + 1]);
            }
            else if (length_Len == 3)
            {
                tlv.len = ((buffer[index + 1] & 0xFF)) | ((buffer[index + 2] & 0xFF)<< 8);
            }
            else
            {
                return 1;
            }
        }
        else
        {
            length_Len = 1;
            tlv.len = (buffer[index] & 0xFF);
            index += 1;
        }

        System.arraycopy(buffer, index, tlv.value, 0, tlv.len);
        ti.lengthLen = length_Len;

        return 0;
    }

    public int TLVDataAdd(TlvData tlv)
    {
        byte temp[] = new byte[4];
        int j = 0;
        int i = 0;
        TlvData tempData = new TlvData();
        tempData.value = new byte[256];


        while(i < intTLVDataBaseLen)
        {
            tagInfo ti = new tagInfo();

            getTLV(TLVDataBase, i, tempData, ti);
            if(tlv.tag == tempData.tag) //tag in this moment is already modified by TLVGetValue(TLVData, i), use it to compare with temptag which has old tag value;
            {
                TLVDataRemove(tlv.tag);
                break;
            }

            i += tempData.len + ti.tagLen + ti.lengthLen;
        }

        if(((tlv.tag >> 16) & 0xFF) > 0)
        {
            temp[j++] = (byte)((tlv.tag >> 16) & 0xFF);
        }

        if(((tlv.tag >> 8) & 0xFF) > 0)
        {
            temp[j++] = (byte)((tlv.tag >> 8) & 0xFF);
        }

        temp[j++] = (byte)(tlv.tag & 0xFF);

        temp[j++] = (byte)tlv.len;

        System.arraycopy(temp, 0, TLVDataBase, intTLVDataBaseLen, j);
        intTLVDataBaseLen += j;

        System.arraycopy(tlv.value, 0, TLVDataBase, intTLVDataBaseLen, tlv.len);
        intTLVDataBaseLen += tlv.len;

        return 0;
    }

    public void TLVDataRemove(int tag)
    {
        byte atag[] = new byte[2];
        int i = 0, j = 0;
        int len;
        int temptag = tag;
        TlvData temptlvData = new TlvData();
        temptlvData.value = new byte[256];

        while(i < intTLVDataBaseLen)
        {
            tagInfo ti = new tagInfo();

            getTLV(TLVDataBase, i, temptlvData, ti);
            len = ti.tagLen + temptlvData.len;
            if(temptag == temptlvData.tag)
            {
                i += len;
                continue;
            }
            else
            {
                System.arraycopy(TLVDataBase, i, TLVDataBase, j, len);
                i += len;
                j += len;
            }
        }

        intTLVDataBaseLen = j;
    }

    public void TLVDataClear()
    {
        intTLVDataBaseLen = 0;
        Arrays.fill(TLVDataBase,(byte)0x00);
    }
}
