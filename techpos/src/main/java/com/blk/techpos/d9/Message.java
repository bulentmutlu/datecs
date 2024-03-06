package com.blk.techpos.d9;

import static com.blk.sdk.c.memcmp;
import static com.blk.sdk.c.memcpy;
import static com.blk.sdk.c.strcpy;
import static com.blk.sdk.c.strncpy;

import com.blk.sdk.Convert;
import com.blk.sdk.string;
import com.blk.techpos.Bkm.TranStruct;
import com.blk.techpos.Bkm.TranUtils;
import com.blk.techpos.Bkm.VParams.VBin;
import com.blk.techpos.tran.Tran;

import java.util.Arrays;

public class Message {
    public static final byte XLS_ID_INFO = 0x60;
    public static final byte AMOUNT_REQUEST = 0x62;
    public static final byte INFO_MESSAGE = (byte) 0x89;
    public static final byte AUTHORIZATION_REQUEST  = (byte) 0x91;


    public static final byte AMOUNT_RESPONSE = 0x61;
    public static final byte AUTHORIZATION_RESPONSE   = (byte) 0x92;

    public Request request = new Request();
    public TranStruct ts;

    public Message(TranStruct ts)
    {
        this.ts = ts;
    }

    public static byte LRC(byte[] bytes, int offset, int length)
    {
        byte LRC = 0;
        for (int i = 0; i < length; i++)
        {
            LRC ^= bytes[offset + i];
        }
        return LRC;
    }

//        XLS ID INFO  (Message ID=0x60)
//
//        NAME	LEN	VALUE	DESCRIPTION
//        STX	1	0x02	Start of Text
//        Message Len	2		4 bcd
//        Message ID	1	0x60	2 bcd
//        Akbank Unique ID	8		ascii
//        ETX	1	0x03	End of Text
//        LRC	1		Longitudinal Redundancy Char
    public static byte[] get(byte [] msg)
    {
        // 6.	Message Len is equal to the length of  the fields between Message ID and ETX (Message ID included, ETX excluded)
        int i = 0;
        byte[] message = new byte[1024];
        message[i++] = 0x02;

        //        Message Len	2		4 bcd
        string sLen = new string("" + msg.length).PadLeft(4, '0');
        byte[] MessageLen = Convert.str2Bcd(sLen.toString().getBytes());
        memcpy(message, i, MessageLen, 0, 2); i += 2;

        //        Message ID	1	0x60	2 bcd
        //        Akbank Unique ID	8		ascii
        memcpy(message, i, msg, 0, msg.length); i += msg.length;

        message[i++] = 0x03;

        byte lrc = LRC(message, 1, i - 1);
        message[i++] = lrc;

        return Arrays.copyOf(message, i);
    }

    //        XLS ID INFO  (Message ID=0x60)
//
//        NAME	LEN	VALUE	DESCRIPTION
//        STX	1	0x02	Start of Text
//        Message Len	2		4 bcd
//        Message ID	1	0x60	2 bcd
//        Akbank Unique ID	8		ascii
//        ETX	1	0x03	End of Text
//        LRC	1		Longitudinal Redundancy Char
    public static byte[] xls_id_info(String UniqueId)
    {
        byte[] msg = new byte[1024];
        int i = 0;
        msg[i++] = XLS_ID_INFO;
        memcpy(msg, i, UniqueId.getBytes(), 0, 8); i+= 8;
        return get(Arrays.copyOf(msg, i));
    }

    public byte[] amount_request()
    {
        byte[] msg = new byte[1024];
        int i = 0;
        msg[i++] = AMOUNT_REQUEST;
//        Function Type
//        0x01	: Banking Txn
//        0x02	: Turkcell Kontör
        msg[i++] = 0x01;
        return get(Arrays.copyOf(msg, i));
    }
    public boolean Parse(byte[] response)
    {
        int i = D9.ack_stx_msglen;
        byte msgId = response[i++];

        if (msgId == AMOUNT_RESPONSE) {
//            Tran Amount 	8		16 bcd (look at 9)
//            GSM No	5		10 bcd
           memcpy(request.TranAmount, 0, response, i, request.TranAmount.length);
           i+= request.TranAmount.length;
           memcpy(request.GsmNo, 0, response, i, request.GsmNo.length);
           i+= request.GsmNo.length;
        }
        if (msgId == AUTHORIZATION_REQUEST) {
            //    Trans. Type (0*, 5*)	1		2 bcd (look at appendix)
//    Card Input Type	1		2 bcd (look at 8)
//    Card Data	40		asc (look at 8)
//    Currency Type	1		2 bcd (look at appendix)
//    Currency Digits	1		2 bcd (look at appendix)
//    Tran Amount	8		16 bcd (look at 5)
//    Spent Bonus	8		16 bcd
//    Gained Bonus	8		16 bcd
//    STAN (for void trans)	3		6 bcd
//    Auth Number (for void trans)	6		asc
//
//    Installment Count	1		2 bcd (look at 1, 2)
//    Instalment Number	1		2 bcd
//    Instalment Amount 8		16 bcd
//    Ins. Gained Bonus	8		16 bcd
            request.TransType = response[i++];
            request.CardInputType = response[i++];

//            8.	Card Input Type :
//            Input from keypad	:  1
//            Input from card reader:  2
//            Input from POS	:  3
//
//            If the Card data is entered manually: (Input Type = 1)
//            The format of the Card will be as:
//            Card_Data
//            {
//                char Card_No[19];
//                char Card_Exp_Date[4];
//                char Card_CVV2[3];
//                char Card_Others[14];
//            };
//
//            If the Card is swiped on ECR: (Input Type = 2)
//            The format of the Card_Data will be exactly the  same as of Track2 (except first digit which is start centinel and the last digit which is end centinel)
//
//            (Input Type = 3)
//            This field will not occur in the message.
            if (request.CardInputType != 3) {
                memcpy(request.CardData, 0, response, i, request.CardData.length);
                i += request.CardData.length;
            }
            request.CurrencyType = response[i++];
            request.CurrencyDigits = response[i++];
            memcpy(request.TranAmount, 0, response, i, request.TranAmount.length); i += request.TranAmount.length;

             String sAmount = new string(Convert.bcd2Str(request.TranAmount)).TrimStart('0').PadLeft(ts.Amount.length, '0').toString();
            memcpy(ts.Amount, sAmount.getBytes(), ts.Amount.length);

            memcpy(request.SpentBonus, 0, response, i,8); i += 8;
            memcpy(request.GainedBonus, 0, response, i,8); i += 8;
            memcpy(request.STAN, 0, response, i,3); i += 3;
            memcpy(request.AuthNumber, 0, response, i,6); i += 6;
            if (ts.TranType == TranStruct.T_VOID) {
                ts.TranNo = Convert.bcdToInt(request.STAN, 0, request.STAN.length);
                try {
                    if (TranUtils.PerformTranNoEntry() != 0)
                        return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
            request.InstallmentCount = response[i++];
//            for (int j = 0; j < request.InstallmentCount; ++j) {
//                request.InstalmentNumber = response[i++];
//                memcpy(request.InstalmentAmount, 0, response, i, 8);
//                i += 8;
//                memcpy(request.InsGainedBonus, 0, response, i, 8);
//                i += 8;
//            }
        }
        return true;
    }

//    Card Number	20		ascii (The first 6 digits will be clear, the rest will be masked by ‘*’)
//    Currency Type	1		2 bcd (look at appendix)
//    Available Points (CCB)	8		16 bcd
//    Card Type	1		2 bcd (look at appendix)
//    Info Flag	1		2 bcd (0x00:magnetic, 0x01:chip, 0x02:contactless)
//    Available XCB	8		16 bcd
//    Available XCB	8		16 bcd
    public byte[] info_message()
    {
        memcpy(request.CardNumber, ts.Pan, 20);
//        Currency Types:
//        Default 0x00 = TL
//        0x01 = USD
//        0x02 = EUR
//        0x03 = STERLING
//        0x04 = YTL
        request.CurrencyType = 0x04;
        request.CardType = 1;
        if (ts.EntryMode == TranStruct.EM_CHIP)
            request.InfoFlag = 1;
        else if (ts.EntryMode == TranStruct.EM_CONTACTLESS || ts.EntryMode == TranStruct.EM_CONTACTLESS_SWIPE)
            request.InfoFlag = 2;


        byte[] msg = new byte[1024];
        int i = 0;
        msg[i++] = INFO_MESSAGE;
        memcpy(msg, i, request.CardNumber, 0, 20); i+= 20;
        msg[i++] = request.CurrencyType;
        memcpy(msg, i, request.AvailablePoints, 0, 8); i += 8;
        msg[i++] = request.CardType;
        msg[i++] = request.InfoFlag;
        memcpy(msg, i, request.AvailableXCB1, 0, 8); i += 8;
        memcpy(msg, i, request.AvailableXCB2, 0, 8); i += 8;

        return get(Arrays.copyOf(msg, i));
    }

    byte tranType() {
        if (ts.EntryMode == TranStruct.EM_CHIP) {
            if (ts.TranType == TranStruct.T_SALE)   return 1;
            if (ts.TranType == TranStruct.T_VOID)   return 2;
            if (ts.TranType == TranStruct.T_REFUND) return 3;
        }
        else if (ts.EntryMode == TranStruct.EM_SWIPE) ;
        return 1;
    }

//    Trans. Type (0* , 5*)	1		2 bcd
//    Response Code	2		2 ascii “00” means approval.            “99” means timeout.
//    Other codes are the response codes coming from the host.
//    Response Description	20		If successful (İŞLEM ONAYLANDI) else if timeout (HOST TIMEOUT)
//else the error message coming from host else standart error message (İŞLEM ONAYLANMADI) Right padded with spaces. (ascii)
//    Authorisation number	6		(ascii) Host received auth.number.
//    Authorisation amount	8		16 bcd Authorized Amount
//    Used CCB (Points)	8		16 bcd Used Bonus
//    Installment Count	1		2 bcd
//    Transaction Flag	1		2 bcd (0x01 means debit)
//    Card Number	20		ascii (The first 6 digits will be clear, the rest will be masked by ‘*’)
//    Terminal ID	8		Ascii
//    Batch Number	3		6 bcd
//    ISystem Trace Number	3		6 bcd
//    Used PCB	8		16 bcd Used Bonus
//    Used XCB	8		16 bcd Used Bonus
//    Cashback Amount	8		16 bcd (look at 10)
    public byte[] authorization_response()
    {
        boolean fTimeout = ts.onlineResult == Tran.OnlineResult.UNABLE.getValue();

        if (fTimeout) {
            memcpy(request.ResponseCode, "99".getBytes(), 2);
            strcpy(request.ResponseDescription, "HOST TIMEOUT");
        }
        else {
            memcpy(request.ResponseCode, ts.RspCode, 2);
            if (!memcmp(request.ResponseCode, "00".getBytes(), 2))
                strcpy(request.ResponseDescription, "ISLEM ONAYLANDI");
            else
                strncpy(request.ResponseDescription, ts.ReplyDescription, 20);
            memcpy(request.Authorisationnumber, ts.AuthCode, 6);
            memcpy(request.Authorisationamount, ts.Amount, 8);
            memcpy(request.UsedCCB, ts.BonusAmount, 8);
            request.InstallmentCount = ts.InsCount;

            if (VBin.IsDebit(null))
                request.TransactionFlag = 1;
            memcpy(request.TerminalID, ts.TermId, 8);
            memcpy(request.BatchNumber, Convert.int2Bcd(ts.BatchNo, 3), 3);
            memcpy(request.SystemTraceNumber, Convert.int2Bcd(ts.Stan, 3), 3);
        }

        byte[] msg = new byte[1024];
        int i = 0;
        msg[i++] = AUTHORIZATION_RESPONSE;
        msg[i++] = request.TransType;
        memcpy(msg, i, request.ResponseCode, 0, 2); i += 2;
        memcpy(msg, i, request.ResponseDescription, 0, 20); i += 20;
        memcpy(msg, i, request.Authorisationnumber, 0, 6); i += 6;
        memcpy(msg, i, request.Authorisationamount, 0, 8); i += 8;
        memcpy(msg, i, request.UsedCCB, 0, 8); i += 8;
        msg[i++] = request.InstallmentCount;
        msg[i++] = request.TransactionFlag;
        memcpy(msg, i, request.CardNumber, 0, 20); i+= 20;
        memcpy(msg, i, request.TerminalID, 0, 8); i += 8;
        memcpy(msg, i, request.BatchNumber, 0, 3); i += 3;
        memcpy(msg, i, request.SystemTraceNumber, 0, 3); i += 3;
        memcpy(msg, i, request.UsedPCB, 0, 8); i += 8;
        memcpy(msg, i, request.UsedXCB, 0, 8); i += 8;
        memcpy(msg, i, request.CashbackAmount, 0, 8); i += 8;

        return get(Arrays.copyOf(msg, i));
    }
}
