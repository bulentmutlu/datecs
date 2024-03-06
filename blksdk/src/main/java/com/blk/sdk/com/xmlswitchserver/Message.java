package com.blk.sdk.com.xmlswitchserver;

/*
<message id="43" type ="1" processStatus ="1" ResponseCode ="0000" >
    <body
    prmVer="1" merchantID="8888888888" terminalID="2222222222" currentDate="161013"
    currentTime="094857" specialMessage="Alisveris basariyla tamamlanmistir."  >
    <BaMobile
        HataKodu="0000" Mesaj="Alisveris basariyla tamamlanmistir."
        Bakiye="1,25" KartBakiye="470,25" IslemNo="90" UyeIdNo ="1"
        KartSahibi="UMUT1 TEST1" TranAmount="1,23" />
    </body>
</message>
 */

public class Message {
    public String id;
    public String type;
    public String processStatus;
    public String ResponseCode;
    public Body body;

    public Message() throws Exception {
        super();
        this.body = new Body();
    }
    public Message(String msgID) throws Exception {
        super();
        this.id = msgID;
        this.body = new Body();
    }
}