package com.blk.platform;

import android.graphics.Bitmap;
import android.graphics.Typeface;


public abstract class IPrinter {
    public int fontSize;
    public int textStyle;
    public Typeface typeface;

    public abstract void PrintLine(String text);
    public abstract void InitPage();
    public abstract int Status();
    public abstract void Print();
    public abstract void DrawImage(Bitmap image);
    public abstract boolean OutofPaper();
}
