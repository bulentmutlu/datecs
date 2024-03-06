package com.blk.platform_castle;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.blk.platform.IPrinter;
import com.blk.sdk.Utility;

import CTOS.CtPrint;

class PrinterCtos extends IPrinter {
    CtPrint printer = PlatfromCtos._printer;

    int Currently_high;

    private static final int BLACK = -16777216;


    @Override
    public void PrintLine(String text) {

        if (text.length() > 0 && text.charAt(0) == '\n' ) {
            PrintLine(" ");
            PrintLine(text.substring(1));
            return;
        }

        //if (typeface != null)
        Currently_high += fontSize / 2 + 2;//print_y;

        printer.drawText(0, Currently_high, text, fontSize, 1, BLACK,
                    true, (float) 0, false, false, "monospace");
        //else
        //IPrinter.drawText(0, print_y + Currently_high, text, fontSize);
        Currently_high += fontSize / 2 + 2;//print_y;
    }

    @Override
    public void InitPage()
    {
//            h : painting set height
//            Note :
//            height = (h % 8 != 0 ? (h / 8 + 1) * 8 : h);
//            IPrinter buffer size = ((384 * height) / 8 ) byte
//            please do not over
        printer.initPage(1200);
        Currently_high = 16;
        printer.setHeatLevel(6);
    }

    @Override
    public int Status() {
        return printer.status();
    }

    @Override
    public void Print() {
        String file = Utility.filesPath + "/print.png";
        com.blk.sdk.file.Remove(file);
        printer.savePNG(file);
        Bitmap png = BitmapFactory.decodeFile(file);

        printer.initPage(Currently_high);
        printer.drawImage(png, 0, 0);
        printer.printPage();
    }

    @Override
    public void DrawImage(Bitmap image) {
        printer.drawImage(image, 0, 0);
    }

    @Override
    public boolean OutofPaper() {
        return printer.status() == CtPrint.STATUS_NOPAPPER_ERR;
    }
}
