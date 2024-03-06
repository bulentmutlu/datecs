package com.blk.sdk;

import static com.blk.sdk.c.memcpy;

public class Tlv {
    public int  tag;
    public int len;
    public byte[] val;


    public static Tlv GetBerTlvData(final byte[] inData, int inDataMaxLen, int inTag, int index)
    {
        return GetBerTlvData(inData, 0, inDataMaxLen, inTag, index);
    }

    public static Tlv GetBerTlvData(final byte[] inData, int inOffset, int inDataMaxLen, int inTag, int index)
    {
        int idx = inOffset;
        int tmpTag = 0;
        int tmpLen = 0;
        int tmpIndex = 0;

        try {

            while(idx < inDataMaxLen)
            {
                //Tag
                tmpTag = 0;
                tmpTag |= Convert.unsignedByteToInt(inData[idx++]);
                if((tmpTag & 0x1F) == 0x1F)
                {
                    tmpTag = tmpTag << 8;
                    tmpTag |= Convert.unsignedByteToInt(inData[idx++]);
                    if((tmpTag & 0x80) != 0)
                    {
                        tmpTag = tmpTag << 8;
                        tmpTag |= Convert.unsignedByteToInt(inData[idx++]);
                    }
                }

                if((Convert.unsignedByteToInt(inData[idx]) & 0x81) == 0x81)
                {
                    idx++;
                    tmpLen = Convert.unsignedByteToInt(inData[idx++]);
                }
                else if((Convert.unsignedByteToInt(inData[idx]) & 0x82) == 0x82)
                {
                    idx++;
                    tmpLen = 0;
                    //memcpy(((char *)&tmpLen) + 2, &inData[idx], 2);
                    // tmpLen = Convert.SWAP_UINT32(tmpLen);
                    byte[] tmpLenArray = new byte[4];
                    memcpy(tmpLenArray, 2, inData, idx, 2);
                    tmpLen = Convert.SWAP_UINT32(Convert.ToInt(tmpLenArray, 0));


                    idx += 2;
                }
                else if((Convert.unsignedByteToInt(inData[idx]) & 0x83) == 0x83)
                {
                    idx++;
                    tmpLen = 0;
                    //memcpy(((char *)&tmpLen) + 1, &inData[idx], 3);
                    //tmpLen = Convert.SWAP_UINT32(tmpLen);
                    byte[] tmpLenArray = new byte[4];
                    memcpy(tmpLenArray, 1, inData, idx, 3);
                    tmpLen = Convert.SWAP_UINT32(Convert.ToInt(tmpLenArray, 0));

                    idx += 3;
                }
                else
                {
                    tmpLen = (int)(Convert.unsignedByteToInt(inData[idx++]) & 0x7F);
                }

                if((tmpTag == inTag) || (inTag == 0))
                {
                    if(index == tmpIndex)
                    {
                        Tlv tlv = new Tlv();
                        tlv.tag = tmpTag;
                        tlv.len = tmpLen;
                        tlv.val = new byte[tlv.len];
                        memcpy(tlv.val, 0, inData, idx, tlv.len);
                        return tlv;
                    }
                    else
                        tmpIndex++;
                }

                idx += tmpLen;
            }

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return null;
    }
}
