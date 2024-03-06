package com.blk.sdk.printer;

import android.util.Log;

import com.blk.sdk.printer.connectivity.AbstractConnector;
import com.datecs.emsr.EMSR;
import com.datecs.printer.Printer;
import com.datecs.printer.ProtocolAdapter;
import com.datecs.rfid.RC663;

import java.io.IOException;

public class PrinterManager {

    private static final String TAG = "PrinterManager";

    private AbstractConnector mConnector;

    private ProtocolAdapter mProtocolAdapter;
    private ProtocolAdapter.Channel mPrinterChannel;
    private Printer mPrinter;
    private EMSR mEMSR;
    private RC663 mRC663;

    public static final PrinterManager instance;

    static  {
        instance = new PrinterManager();
    }

    private PrinterManager() { }

    public void init(AbstractConnector connector) throws IOException {
        Log.d(TAG, "Initialize printer...");

        mConnector = connector;
        mProtocolAdapter = new ProtocolAdapter(mConnector.getInputStream(), mConnector.getOutputStream());
        if (mProtocolAdapter.isProtocolEnabled()) {
            Log.d(TAG, "Protocol mode is enabled");
            mPrinterChannel = mProtocolAdapter.getChannel(ProtocolAdapter.CHANNEL_PRINTER);
            mPrinter = new Printer(mPrinterChannel.getInputStream(), mPrinterChannel.getOutputStream());
            ProtocolAdapter.Channel emsrChannel = mProtocolAdapter.getChannel(ProtocolAdapter.CHANNEL_EMSR);
            try {
                try {
                    emsrChannel.close();
                } catch (IOException ignored) {
                }
                emsrChannel.open();
                mEMSR = new EMSR(emsrChannel.getInputStream(), emsrChannel.getOutputStream());
                EMSR.EMSRKeyInformation keyInfo = mEMSR.getKeyInformation(EMSR.KEY_AES_DATA_ENCRYPTION);
                if (!keyInfo.tampered && keyInfo.version == 0) {
                    Log.d(TAG, "Missing encryption key");
                    byte[] keyData = CryptographyHelper.createKeyExchangeBlock(0xFF,
                            EMSR.KEY_AES_DATA_ENCRYPTION, 1, CryptographyHelper.AES_DATA_KEY_BYTES,
                            null);
                    mEMSR.loadKey(keyData);
                }
                mEMSR.setEncryptionType(EMSR.ENCRYPTION_TYPE_AES256);
                mEMSR.enable();
                Log.d(TAG, "Encrypted magnetic stripe reader is available");
            } catch (IOException e) {
                if (mEMSR != null) {
                    mEMSR.close();
                    mEMSR = null;
                }
            }
            ProtocolAdapter.Channel rfidChannel = mProtocolAdapter.getChannel(ProtocolAdapter.CHANNEL_RFID);
            try {
                try {
                    rfidChannel.close();
                } catch (IOException ignored) {
                }
                rfidChannel.open();
                mRC663 = new RC663(rfidChannel.getInputStream(), rfidChannel.getOutputStream());
                mRC663.enable();
                Log.d(TAG, "RC663 reader is available");
            } catch (IOException e) {
                if (mRC663 != null) {
                    mRC663.close();
                    mRC663 = null;
                }
            }
            ProtocolAdapter.Channel universalReader = mProtocolAdapter.getChannel(ProtocolAdapter.CHANNEL_UNIVERSAL_READER);
            try {
                try {
                    System.out.println("<ProtocolAdapter> close universal reader");
                    universalReader.close();
                } catch (IOException ignored) {
                }
                System.out.println("<ProtocolAdapter> open universal reader");
                universalReader.open();

                Log.d(TAG, "Universal Reader is available");
            } catch (IOException e) {
                System.out.println("<ProtocolAdapter> exception: " + e);
            }
        } else {
            Log.d(TAG, "Protocol mode is disabled");
            mPrinter = new Printer(
                    mProtocolAdapter.getRawInputStream(),
                    mProtocolAdapter.getRawOutputStream());
        }
    }

    public void close() {
        if (mConnector != null) {
            try {
                mConnector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ProtocolAdapter getProtocolAdapter() {
        return mProtocolAdapter;
    }

    public ProtocolAdapter.Channel getPrinterChannel() {
        return mPrinterChannel;
    }

    public Printer getPrinter() {
        return mPrinter;
    }

    public EMSR getEMSR() {
        return mEMSR;
    }

    public RC663 getRC663() {
        return mRC663;
    }

}
