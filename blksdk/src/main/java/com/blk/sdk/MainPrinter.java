package com.blk.sdk;

import android.graphics.Bitmap;

import com.blk.sdk.printer.PrinterManager;
import com.datecs.printer.Printer;

import java.io.IOException;


/**
 * Created by id on 29.03.2018.
 */

public class MainPrinter {


    private interface PrinterRunnable {
        void run(Printer printer) throws IOException;
    }

    public static LineWriter lineWriter = new LineWriter(16, 38);
    public static LineWriter lineWriter42 = new LineWriter(15, 42);
    public static LineWriter boldWriter = new LineWriter(32, 20);
    public static LineWriter captionWriter = new LineWriter(40, 16);

   public static Printer printer;

    private static final int mDefaultFeed = 255;

    public static class LineWriter
    {
        public int fontSize;
        int textStyle;
        int maxChar;

        public LineWriter(int fontSize, int maxChar)
        {
            this.fontSize = fontSize;this.maxChar = maxChar;
        }
        public int SetStyle(int textStyle)
        {
            return this.textStyle = textStyle;
        }
        public int  GetLineLen()
        {
            return maxChar; //return (int) (paperWidth / fontSize);
        }

        public void Write_LeftRight(String left, String right)
        {
                runTask((printer) -> {
                    printer.reset();
                    printer.printText(new string(left).PadRight(maxChar, ' ').toString());
                });
               // printer.printText(new string(left).PadCenter(right, maxChar, ' ').toString());
        }
        public void Write_Center(final String buf)
        {
            runTask((printer) -> {
                printer.reset();
                printer.printTaggedText("{center}"+buf);
            });
        }
        public void Write_Center(final byte[] buf)
        {
            runTask((printer) -> {
                printer.reset();
                printer.setAlign(Printer.ALIGN_CENTER);
                printer.printText(new string(buf).toString());
            });

        }

        public void Write(final String buf)
        {
            if (this != lineWriter && string.IsSpace(buf)) {
                lineWriter.Write(buf);
                return;
            }

            String s = buf;
            if (buf.equals("\n"))
                s = " ";
            else if (buf.endsWith("\n") && !buf.endsWith("\n\n")) // sondaki \n sil
                s = buf.substring(0, buf.length() - 1);
            s = s.replace("\n\n", "\n");

            //            String s = buf.replace(' ', '_');
//            s = s.replace('\n', '#');
            //page.addLine().addUnit(s, fontSize, IPage.EAlign.LEFT, textStyle);

            if (s.length() > maxChar && this == lineWriter)
            {
                lineWriter42.Write(s);
                return;
            }
            final String finalS = s;
            runTask((printer) -> {
                printer.reset();
                printer.printText(finalS);
            });
        }
        public void Write(final byte[] buf)
        {
            Write(new string(buf).toString());
        }
    }

    public static void PrintFlush()
    {
        runTask((printer) -> {
            printer.reset();
            printer.feedPaper(printer.getInformation().getFeedLines());
        });
    }

    public static void LineSpace()
    {
        runTask((printer) -> {
            printer.reset();
            //String textBuffer = "{br}";
            //printer.printTaggedText(textBuffer);
            printer.flush();
        });
    }

    private static void runTask(final PrinterRunnable r) {
        Thread t = new Thread(() -> {
            try {
                Printer printer = PrinterManager.instance.getPrinter();
                r.run( printer);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        t.start();
    }
    public static void PrintTest() {

        runTask((printer) -> {
            printer.reset();
            String textBuffer = "{reset}{center}{w}{h}RECEIPT" +
                    "{br}" +
                    "{br}" +
                    "{reset}1. {b}First item{br}" +
                    "{reset}{right}{h}$0.50 A{br}" +
                    "{reset}2. {u}Second item{br}" +
                    "{reset}{right}{h}$1.00 B{br}" +
                    "{reset}3. {i}Third item{br}" +
                    "{reset}{right}{h}$1.50 C{br}" +
                    "{br}" +
                    "{reset}{right}{w}{h}TOTAL: {/w}$3.00  {br}" +
                    "{br}" +
                    "{reset}{center}{s}Thank You!{br}";
            printer.printTaggedText(textBuffer);
            printer.feedPaper(mDefaultFeed);
            printer.flush();
        });
    }
/*    public static void PrintTest()
    {
        try {
            String buf = "12345678901234567890123456789012345678901234567890";

            //captionWriter.EP_PrntWrtStr("------PrintTest-------");
            lineWriter.Write(buf);
            lineWriter42.Write(buf);
            lineWriter.Write("---------0------1234567890---------0-----6");
            lineWriter.Write_Center("1234567890");
            boldWriter.Write(buf);
            lineWriter.Write("ğüşiöçıĞÜŞİÖÇI");
            captionWriter.Write(buf);
            captionWriter.Write("---- ~PrintTest~ -----");

            printer.printText("\n");

            Utility.log("lineWriter %d", lineWriter.GetLineLen());
            Utility.log("boldWriter %d", boldWriter.GetLineLen());
            Utility.log("captionWriter %d", captionWriter.GetLineLen());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    /*
    public static void Init()  {
        printer = IPlatform.get().printer;
        Reset();
    }

     */
    public static void Destroy()
    {
        printer = null;
    }

    public static void Reset()  {
       // printer.InitPage();
    }

    public static void PrintLogo(Bitmap szLogo, int offset)  {

         //   printer.printImage(szLogo);

    }

}
