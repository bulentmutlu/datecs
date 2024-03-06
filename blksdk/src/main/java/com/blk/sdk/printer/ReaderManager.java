package com.blk.sdk.printer;

import com.blk.sdk.printer.connectivity.AbstractConnector;
import com.datecs.printer.ProtocolAdapter;
import com.datecs.universalreader.UniversalReader;

import java.io.IOException;

public class ReaderManager {

    private static AbstractConnector sConnector;
    private static UniversalReader sReader;
    private static String sInformation;

    public static void init(AbstractConnector connector) throws IOException {
        UniversalReader.setDebug(true);
        sConnector = connector;
        // Check for printer
        ProtocolAdapter adapter = new ProtocolAdapter(sConnector.getInputStream(), sConnector.getOutputStream());
        if (adapter.isProtocolEnabled()) {
            ProtocolAdapter.Channel universalReaderChannel = adapter.getChannel(ProtocolAdapter.CHANNEL_UNIVERSAL_READER);
            sReader = new UniversalReader(universalReaderChannel.getInputStream(), universalReaderChannel.getOutputStream());
        } else {
            sReader = new UniversalReader(adapter.getRawInputStream(), adapter.getRawOutputStream());
        }

        sInformation = sReader.getIdentification();
    }

    public static void release() {
        if (sReader != null) {
            sReader.close();
        }
        if (sConnector != null) {
            try {
                sConnector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static UniversalReader reader() {
        return sReader;
    }

    public static String getDeviceInfo() {
        return sInformation;
    }

}