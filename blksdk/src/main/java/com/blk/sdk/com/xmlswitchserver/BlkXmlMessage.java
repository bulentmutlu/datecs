package com.blk.sdk.com.xmlswitchserver;

import com.blk.sdk.olib.olib;

public class BlkXmlMessage {

    public Message message;

    public BlkXmlMessage(String msgID, String MerchantID, String TerminalID) throws Exception {
        message = new Message(msgID);
        message.body.merchantID = MerchantID;
        message.body.terminalID = TerminalID;
    }
    public BlkXmlMessage() {
    }

    public String getMessageID() {
        return message.id;
    }

    public String GetMessage() throws Exception {
        String xmlMessage = null;

        if (xmlMessage == null) {
            xmlMessage = olib.SerializeXml(message);
//            final Format format = new Format(0);
//            Serializer serializer = new Persister(format);
//            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            serializer.write(getMessage(), stream);
//            xmlMessage = stream.toString();
        }
        return xmlMessage;
    }
}