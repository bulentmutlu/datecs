package com.blk.sdk.printer.connectivity;
import android.content.Context;

import com.android.printer.PrinterManager;
import com.android.printer.PrinterSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PrinterConnector extends AbstractConnector {

    private final com.android.printer.PrinterManager mPrinterManager;

    private PrinterSocket mPrinterSocket;

    public PrinterConnector(Context context) {
        super(context);
        mPrinterManager = PrinterManager.getInstance(context);
    }

    @Override
    public void connect() throws IOException {
        mPrinterSocket = mPrinterManager.openPrinter();
    }

    @Override
    public void close() {
        if (mPrinterSocket == null) {
            return;
        }

        mPrinterSocket.close();
    }

    @Override
    public InputStream getInputStream() {
        if (mPrinterSocket == null) {
            return null;
        }

        return mPrinterSocket.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() {
        if (mPrinterSocket == null) {
            return null;
        }

        return mPrinterSocket.getOutputStream();
    }
}
