package com.blk.sdk.emulator;

import android.graphics.Bitmap;
import android.util.Log;

import com.blk.platform.IPrinter;

class PrinterEmulator extends IPrinter {


    @Override
    public void PrintLine(String text) {
        Log.i("Emulator.IPrinter", text);
    }

    @Override
    public void InitPage() {
      //  MainPrinter.init(A);
    }

    @Override
    public int Status() {
        return 0;
    }

    @Override
    public void Print() {
    }

    @Override
    public void DrawImage(Bitmap image) {
    }

    @Override
    public boolean OutofPaper() {
        return false;
    }
}
