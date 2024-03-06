package com.blk.techpos.d9;

import static com.blk.sdk.c.memcpy;

import android.util.Log;

import com.blk.platform.IPlatform;
import com.blk.sdk.Convert;
import com.blk.sdk.UI;
import com.blk.sdk.com.TcpClient;
import com.blk.sdk.emulator.Emulator;
import com.blk.techpos.Bkm.Reversal;
import com.blk.techpos.Bkm.TranStruct;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class D9 {
    private static final String TAG = D9.class.getSimpleName();
    public final static int ack_stx_msglen = 4, etx_lrc = 2;
    public final static byte[] EOT = new byte[] {0x04};

    public static D9 d9;
    public Message msg;

    TcpClient tcpClient = new TcpClient();
    InputStream inputStream;
    OutputStream outputStream;



    public static boolean Connect()  {

        if (d9 == null) d9 = new D9();

        return d9.connect();
    }
    boolean connect() {
        tcpClient.close();
        try {
            String ip = "192.168.2.38";
            if (IPlatform.get() instanceof Emulator)
                ip = Emulator.localHostPcIp;

            tcpClient.open(ip, 6666);
            inputStream = tcpClient.getInputStream();
            outputStream = tcpClient.getOutputStream();
        } catch (Exception e) {
            e.printStackTrace();
            UI.ShowMessage("Ecr bağlantı kurulamadı.");
            return false;
        }
        return true;
    }
    public static void Disconnect()
    {
        if (d9 == null) return;
        d9.tcpClient.close();
        d9 = null;
    }
    public static boolean IsConnected() {
        if (d9 == null) return false;
        return d9.tcpClient.socket.isConnected();
    }
    public void doTran(TranStruct ts) throws IOException {

//        Message message = new Message(ts);
//        //byte[] response = send_receive(Message.xls_id_info("12345678"));
//        byte[] response = send_receive(message.amount_request());
//        message.Parse(response);
//
//        response = send_receive(message.info_message());
//        message.Parse(response);
//        response = send_receive(message.authorization_response(false));
    }

    public byte[] send_receive(byte[] request, int waitResponse) throws IOException {
        Log.i(TAG, "request : " + Convert.Buffer2Hex(request));
        outputStream.write(request);

        if (waitResponse == 0) return null;

        byte[] response = new byte[1024];
        int responseLength = 0;
        do {
            byte[] chunk = new byte[1024];
            int len = inputStream.read(chunk);
            memcpy(response, responseLength, chunk, 0, len);
            responseLength += len;

            if (responseLength > 0 && response[0] != 0x06) {
                Log.i(TAG, "response : " + response[0]);
                return null;
            }
            if (waitResponse == 1)
                break;

            if (responseLength >= ack_stx_msglen) {
                byte[] bMsgLen = Arrays.copyOfRange(response, 2, 4);
                int msgLen = Integer.parseInt(Convert.bcdToStr(bMsgLen, 2));
                if (responseLength >= ack_stx_msglen + msgLen + etx_lrc)
                    break;
            }
        } while (true);

        Log.i(TAG, "response : " + Convert.Buffer2Hex(response, 0, responseLength));
        return Arrays.copyOf(response, responseLength);
    }

    public static boolean info(TranStruct ts)
    {
        try {
            UI.ShowMessage(0, "Ecr bekleniyor...");

            d9.msg = new Message(ts);
            byte[] response = d9.send_receive(d9.msg.info_message(), -1);

            if (response == null) {
                UI.ShowMessage("Ecr haberleşme başarısız");
                return false;
            }
            if (!d9.msg.Parse(response)) {
                d9.send_receive(D9.EOT, 0);
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            UI.ShowMessage("Ecr haberleşme başarısız");
            return false;
        }
        return true;
    }
    public static boolean authorization_response(int trnRv)
    {
        try {
            UI.ShowMessage(0, "Ecr bekleniyor...");

            byte[] response = d9.send_receive(d9.msg.authorization_response(), 1);
            d9.msg = null;

            if (trnRv == 0 && response == null){
                Reversal.ReverseTran();
                UI.ShowMessage("Ecr haberleşme başarısız");
                return false;
            }
            d9.send_receive(D9.EOT, 0);

        } catch (Exception e) {
            e.printStackTrace();
            UI.ShowMessage("Ecr haberleşme başarısız");
            return false;
        }
        return true;
    }

}
