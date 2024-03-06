package com.blk.sdk.com.xmlswitchserver;

import android.util.Log;

import com.blk.sdk.string;
import com.blk.sdk.com.TcpClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SwitchClient
{
    interface ISwitchClient {
        void onWrite();
    }
    TcpClient tcpClient;
    ISwitchClient iSwitchClient;

    int messageLen;
    int headerVersion = 6;
    final int headerLength = 15; // stx + header_key + header(12) + message_key
    static SwitchClient switchClient;
    private static final String TAG = SwitchClient.class.getSimpleName();


    public static void Configure(TcpClient.Config config)
    {
        if (switchClient == null) switchClient = new SwitchClient();
        switchClient.tcpClient = new TcpClient(config);
    }
    public static void SetHeaderVersion(byte v)
    {
        if (switchClient == null) switchClient = new SwitchClient();
        switchClient.headerVersion = v;
    }
    public static String SendReceive(String request) throws Exception
    {
        return SendReceive(request, null);
    }
    public static String SendReceive(String request, ISwitchClient iSwitchClient) throws Exception
    {
        assert switchClient != null;
        switchClient.iSwitchClient = iSwitchClient;
        String response =  switchClient.sendReceive(request);
        switchClient.iSwitchClient = null;
        return response;
    }

    String prepareHeader() {
        String headerVersion = string.PadLeft("" + switchClient.headerVersion, 2, '0').substring(0, 2);
        String connType = "4";
        String termId = "008888888888";
        String appId = "0098";
        String subAppId = "0001";
        String msgId = "00";
        String requestID = string.PadLeft(msgId, 2, '0').substring(0, 2);
        //ISystem.out.println("Header: " + headerVersion + connType + termId + appId + subAppId + requestID);
        return headerVersion + connType + termId + appId + subAppId + requestID;
    }
    String decodeHeader(String HeaderResponse) throws Exception {
        byte[] b64Header = Base64.decode(HeaderResponse);
        if (b64Header.length < 1 || ((b64Header.length % 8) != 0))
            throw new Exception("Error on base64 unpack");
        int iLen = b64Header.length;
        if (iLen % 8 != 0)
        {
            iLen = iLen + (8 - (iLen % 8));
        }
        byte[] strDecryptedHeader = Utils.Decrypt(Utils.headerkey, b64Header, 0, iLen);
        //byte[] strUnpackedHeader = new byte[12];
        //Utils.bcd_asc(strUnpackedHeader, strDecryptedHeader, 12);
        assert strDecryptedHeader != null;
        byte[] strUnpackedHeader = Utils.bcdToString(strDecryptedHeader).getBytes();
        return new String(strUnpackedHeader);
    }
    String decodeMessage(String MessageResponse) throws Exception {
        byte[] message = Base64.decode(MessageResponse);
        int iLength = message.length;
        if (iLength % 8 != 0)
        {
            iLength = iLength + (8 - (iLength % 8));
        }
        message = Utils.Decrypt(Utils.messagekey, message, 0, iLength);
        if (headerVersion < 7) {
            message = Utils.CompressDecompress(message, false);
        }
        if (message == null)
            throw new Exception("invalid message");
        return new String(message, "Windows-1254");
    }
    byte[] receive() throws Exception {
        messageLen = 0;
        return tcpClient.read((output, length) -> {
            // need header
            if (messageLen == 0 && length < headerLength)
                return false;
            // parse header
            if (messageLen == 0) {
                if (output[0] != 2 && output[1] != '=')
                    throw  new Exception("Invalid switch response stx key");

                String headerPlainText = decodeHeader(new String(output, 2, 12));
                String messageID = headerPlainText.substring(0, 2);
                String responseID = headerPlainText.substring(2, 4);
                String responseCode = headerPlainText.substring(4, 6);
                String responseLength = headerPlainText.substring(6, 11);
                //ISystem.out.println("RESPM: " + iLen + "  " + headerPlainText + "  " + messageID + " " + responseID + " " + responseCode + " " + responseLength);
                messageLen = Integer.parseInt(responseLength);

                if (messageLen <= 0)
                    throw new Exception("Invalid switch response messageLen");
            }

            // get all data
            if (length < headerLength + messageLen + 5) return false; // 5 : 2 crc 3 \n

            // check end byte for correctness
            if (output[headerLength + messageLen - 1] != 3)
                throw new Exception("Invalid switch response etx");
            // seems like everything will be fine
            return true;
        });
    }
    String sendReceive(String request) throws Exception {
        String MessageResponse = null;
        try {

            tcpClient.open();
            Log.i(TAG, "Connect(" + tcpClient.config.ip + ":" + tcpClient.config.port + ") BlkXmlMessage : " + request);

            String header = prepareHeader();

            byte[] compresedMessage = request.getBytes();
            if (headerVersion < 7) {
                compresedMessage = Utils.CompressDecompress(compresedMessage, true);
            }
            compresedMessage = Utils.Encrypt(Utils.messagekey, compresedMessage, 0, compresedMessage.length);
            compresedMessage = Utils.Normalize(compresedMessage);
            String message = Base64.encode(compresedMessage);
            message = "!" + message;

            String ServerRequest = Utils.STX + "=" + Base64.encode(Utils.Encrypt(Utils.headerkey, Utils.asc_bcd((header + String.format("%05d", message.length()) + "\0\0").getBytes(),32), 0, 15)) + message + Utils.ETX;

            int crc = Utils.Calculate(ServerRequest.getBytes(), ServerRequest.getBytes().length);
            byte[] CRC = new byte[] {(byte)(crc / 256), (byte)(crc % 256)};
            ServerRequest = ServerRequest + new String(CRC);

            //Log.i(TAG, "Send : " + ServerRequest);
            tcpClient.write(ServerRequest.getBytes(StandardCharsets.US_ASCII), ServerRequest.length());
            if (iSwitchClient != null) iSwitchClient.onWrite();

            byte[] response =receive();

            MessageResponse = new String(response, headerLength - 1, messageLen);
            //ISystem.out.println("BLA: " + MessageResponse);
            MessageResponse = decodeMessage(MessageResponse);
            Log.i(TAG, "Receive : " + MessageResponse);
        }
        finally{
            tcpClient.close();
        }
        return MessageResponse;
    }

    public static void DebugResponse(String ServerResponse)
    {
        try {
        if(ServerResponse.substring(0, 1).equalsIgnoreCase(Utils.STX)) {
            ServerResponse.substring(1, 2); //KEYID
            String HeaderResponse = ServerResponse.substring(2, 14); //HEADER
            byte[] b64Header = Base64.decode(HeaderResponse);
            if (b64Header.length < 1 || ((b64Header.length % 8) != 0)) {
                throw new IOException("Error on base64 unpack");
            } else {
                int iLen = b64Header.length;
                if (iLen % 8 != 0) {
                    iLen = iLen + (8 - (iLen % 8));
                }
                byte[] strDecryptedHeader = Utils.Decrypt(Utils.headerkey, b64Header, 0, iLen);
                assert strDecryptedHeader != null;
                byte[] strUnpackedHeader = Utils.bcdToString(strDecryptedHeader).getBytes();
                String headerPlainText = new String(strUnpackedHeader);
                String messageID = headerPlainText.substring(0, 2);
                String responseID = headerPlainText.substring(2, 4);
                String responseCode = headerPlainText.substring(4, 6);
                String responseLength = headerPlainText.substring(6, 11);
                //ISystem.out.println("RESPM: " + iLen + "  " + headerPlainText + "  " + messageID + " " + responseID + " " + responseCode + " " + responseLength);
                int rspnsLength = Integer.parseInt(responseLength);
                if (messageID.equalsIgnoreCase("03") && responseID.equalsIgnoreCase("00") && responseCode.equalsIgnoreCase("00")) {
                    String MessageResponse = ServerResponse.substring(14, ServerResponse.lastIndexOf(Utils.ETX));// .length()-1); // @idris, etxden fazlası geliyor.
                    byte[] b64Message = Base64.decode(MessageResponse);
                    int iLength = b64Message.length;
                    if (iLength % 8 != 0) {
                        iLength = iLength + (8 - (iLength % 8));
                    }
                    byte[] strDecryptedMessage = Utils.Decrypt(Utils.messagekey, b64Message, 0, iLength);
                    byte[] strDecompresedMessage = Utils.CompressDecompress(strDecryptedMessage, false);
                    if (strDecompresedMessage != null) {
                        String response = new String(strDecompresedMessage);
                        System.out.println("Response from Switch: " + response);
                    }
                } else {
                    throw new IOException("Invalid Response Message");
                }
            }
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
