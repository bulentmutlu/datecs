package com.blk.techpos.d9;

public class Request {

    public byte[] GsmNo = new byte[5];

    //    Card Number	20		ascii (The first 6 digits will be clear, the rest will be masked by ‘*’)
    public byte[] CardNumber = new byte[20];
    //    Currency Type	1		2 bcd (look at appendix)
    public byte CurrencyType;
    //    Available Points (CCB)	8		16 bcd
    public byte[] AvailablePoints = new byte[8];
    //    Card Type	1		2 bcd (look at appendix)
    public byte CardType;
    //    Info Flag	1		2 bcd (0x00:magnetic, 0x01:chip, 0x02:contactless)
    public byte InfoFlag;
    //    Available XCB	8		16 bcd
    public byte[] AvailableXCB1 = new byte[8];
    public byte[] AvailableXCB2 = new byte[8];

    //    Trans. Type (0*, 5*)	1		2 bcd (look at appendix)
    public byte TransType;
    //    Card Input Type	1		2 bcd (look at 8)
    public byte CardInputType;
    //    Card Data	40		asc (look at 8)
    public byte[] CardData = new byte[40];
    //    Currency Digits	1		2 bcd (look at appendix)
    public byte CurrencyDigits;
    //    Tran Amount	8		16 bcd (look at 5)
    public byte[] TranAmount = new byte[8];
    //    Spent Bonus	8		16 bcd
    public byte[] SpentBonus = new byte[8];
    //    Gained Bonus	8		16 bcd
    public byte[] GainedBonus = new byte[8];
    //    STAN (for void trans)	3		6 bcd
    public byte[] STAN = new byte[3];
    //    Auth Number (for void trans)	6		asc
    public byte[] AuthNumber = new byte[6];
    //    Installment Count	1		2 bcd (look at 1, 2)
    public byte InstallmentCount;
    //    Instalment Number	1		2 bcd
    public byte InstalmentNumber;
    //    Instalment Amount 8		16 bcd
    public byte[] InstalmentAmount = new byte[8];
    //    Ins. Gained Bonus	8		16 bcd
    public byte[] InsGainedBonus = new byte[8];

    //    Response Code	2		2 ascii “00” means approval.            “99” means timeout.
//    Other codes are the response codes coming from the host.
    public byte[]  ResponseCode= new byte[2];
//    Response Description	20		If successful (İŞLEM ONAYLANDI) else if timeout (HOST TIMEOUT)
//else the error message coming from host else standart error message (İŞLEM ONAYLANMADI) Right padded with spaces. (ascii)
    public byte[]  ResponseDescription= new byte[20];
//    Authorisation number	6		(ascii) Host received auth.number.
    public byte[]  Authorisationnumber= new byte[6];
//    Authorisation amount	8		16 bcd Authorized Amount
    public byte[]  Authorisationamount= new byte[8];
//    Used CCB (Points)	8		16 bcd Used Bonus
    public byte[]  UsedCCB= new byte[8];
//    Installment Count	1		2 bcd
//    Transaction Flag	1		2 bcd (0x01 means debit)
    public byte TransactionFlag;
//    Card Number	20		ascii (The first 6 digits will be clear, the rest will be masked by ‘*’)
    //    Terminal ID	8		Ascii
    public byte[]  TerminalID= new byte[8];
//    Batch Number	3		6 bcd
    public byte[] BatchNumber = new byte[3];
//    ISystem Trace Number	3		6 bcd
    public byte[]  SystemTraceNumber= new byte[3];
//    Used PCB	8		16 bcd Used Bonus
    public byte[]  UsedPCB= new byte[8];
//    Used XCB	8		16 bcd Used Bonus
    public byte[]  UsedXCB= new byte[8];
//    Cashback Amount	8		16 bcd (look at 10)
    public byte[]  CashbackAmount= new byte[8];

}
