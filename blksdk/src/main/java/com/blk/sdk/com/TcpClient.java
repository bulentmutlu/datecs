package com.blk.sdk.com;

import static com.blk.sdk.c.memcpy;

import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.InvalidParameterException;
import java.util.Arrays;

/**
 * Created by id on 2.03.2018.
 */

public class TcpClient {
    private static final String TAG = TcpClient.class.getSimpleName();

    public static class Config {
        public String ip;
        public int port;
        public int connectionTimeoutSecond = 10;
        public int receiveTimeoutSecond = 30;
        public Config(String ip, int port, int connectionTimeoutSecond, int receiveTimeoutSecond)
        {
            this.ip = ip;
            this.port = port;
            this.connectionTimeoutSecond = connectionTimeoutSecond;
            this.receiveTimeoutSecond = receiveTimeoutSecond;
        }
        public Config(String ip, int port)
        {
            this.ip = ip;
            this.port = port;
        }
    }

    public interface IReceiveHandler {
        boolean onReceive(byte[] output, int length) throws Exception;
    }


    public Config config;
    public Socket socket;
    OutputStream os;
    InputStream is;

    public TcpClient()
    {

    }
    public TcpClient(TcpClient.Config config)
    {
        this.configure(config);
    }

    public void configure(TcpClient.Config config)
    {
        this.config = new Config(config.ip, config.port, config.connectionTimeoutSecond, config.receiveTimeoutSecond);
    }

    public void open(String ip, int port) throws Exception {
        if (this.config == null)
            this.config = new Config(ip, port);
        else {
            this.config.ip = ip;
            this.config.port = port;
        }

        open();
    }

    public void open() throws Exception {

        if (config == null)  {
            throw  new Exception("no config");
        }

        Log.i(TAG, "Connect(" + config.ip + ":" + config.port + ")");

        socket=new Socket();
        socket.connect(new InetSocketAddress(config.ip, config.port), config.connectionTimeoutSecond * 1000);
        socket.setSoTimeout(config.receiveTimeoutSecond * 1000);

        os = socket.getOutputStream();
        is = socket.getInputStream();
    }
    public void close() {
        try {
            if (is != null) is.close();
            if (os != null) os.close();
            if (socket != null) socket.close();
        } catch (Exception e) {
        }
    }
    public void write(byte[] data, int len) throws Exception {
        if (socket == null)
            throw new InvalidParameterException();

        os.write(Arrays.copyOfRange(data, 0, len));
    }
    public byte[] read(int len) throws Exception {
        if (socket == null)
            throw new InvalidParameterException();

        byte [] buf = new byte[len];
        int readed = is.read(buf, 0, len);
        return Arrays.copyOfRange(buf, 0, readed);
    }

    public int read(byte[] output, int offset, int length) throws Exception {
        if (socket == null)
            throw new InvalidParameterException();

        return is.read(output, offset, length);
    }

    public byte[] read(IReceiveHandler iReceiveHandler) throws Exception {
        int chunkLength = 4096;
        byte[] output = new byte[chunkLength];
        int totalLength = 0;

        do {
            byte[] chunk = read(chunkLength);
            if (chunk == null || chunk.length == 0) break;

            while (totalLength + chunk.length > output.length)
                output = Arrays.copyOf(output, output.length + (chunkLength *= 2));

            memcpy(output, totalLength, chunk, 0, chunk.length);
            totalLength += chunk.length;

            if (iReceiveHandler.onReceive(output, totalLength))
                break;

        } while (true);

        return Arrays.copyOf(output, totalLength);
    }

    public OutputStream getOutputStream()
    {
        return os;
    }
    public InputStream getInputStream()
    {
        return is;
    }
//
//
//    public static boolean IsNetworkOnline() {
//        boolean status = false;
//        try {
//            ConnectivityManager cm = (ConnectivityManager) UI.UiUtil.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
//            NetworkInfo netInfo = cm.getNetworkInfo(0);
//            if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
//                status = true;
//            } else {
//                netInfo = cm.getNetworkInfo(1);
//                if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED)
//                    status = true;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//
//        }
//        return status;
//    }

}
