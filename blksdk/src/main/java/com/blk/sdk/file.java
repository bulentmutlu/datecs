package com.blk.sdk;

import java.io.*;
/**
 * Created by idris on 21.02.2018.
 */

public class file implements AutoCloseable {
    private static final String TAG = file.class.getSimpleName();

    public enum SeekMode {SEEK_SET, SEEK_CUR, SEEK_END};
    public enum OpenMode {RDONLY, RDWR}

    RandomAccessFile f; // RandomAccessFile is big-endian.

    public file(String fileName) throws FileNotFoundException {
        String fn = fileName;
        if (!fn.startsWith("/")) fn = Utility.filesPath + "/" + fileName;

        f = new RandomAccessFile(fn, "rw");
    }
    public file(String fileName, OpenMode mode) throws FileNotFoundException {
        String m = (mode == OpenMode.RDONLY) ? "r" : "rw";
        String fn = fileName;
        if (!fn.startsWith("/")) fn = Utility.filesPath + "/" + fileName;

        f = new RandomAccessFile(fn, m);
    }

    public int Size() throws IOException {
        return (int) f.length();
    }
    public void Close() {
        try {
            if (f.getFD().valid()) {
                f.getFD().sync();
                f.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void close() throws Exception {
        Close();
    }
    public int Seek(int offset, SeekMode m) throws IOException {
        long origin = 0;
        if (m == SeekMode.SEEK_CUR) origin = f.getFilePointer();
        else if (m == SeekMode.SEEK_END) origin = f.length();

        f.seek(origin + offset);
        return (int) f.getFilePointer();
    }

    public byte ReadByte() throws IOException {
        return f.readByte();
    }
//    public char ReadChar() throws IOException {
//        return (char) f.readUnsignedByte();
//    }
    public int ReadUnsignedByte() throws IOException {
        return f.readUnsignedByte();
    }
    public short ReadShort() throws IOException {
        return Convert.SWAP_UINT16(f.readShort());
    }
    public int ReadInt() throws IOException {
        return Convert.SWAP_UINT32(f.readInt());
    }
    public long ReadLong() throws IOException {
        return Long.reverseBytes(f.readLong());
    }

    public byte[] Read(int nByte) throws IOException {
        byte[] o = new byte[nByte];
        f.read(o, 0, o.length);
        return o;
    }
    public int Read(byte[] output) throws IOException {
        return f.read(output);
    }
    public int Read(byte[] output, int oOffset, int len)throws IOException
    {
        return f.read(output, oOffset, len);
    }

    public void Write(byte b) throws IOException {
        f.writeByte(b);
    }
//    public void Write(char c) throws IOException {
//        f.writeByte((byte) c);
//    }
    public void Write(short s) throws IOException {
        f.writeShort(Convert.SWAP_UINT16(s));
    }
    public void Write(int i) throws IOException {
        f.writeInt(Convert.SWAP_UINT32(i));
    }
    public void Write(long l) throws IOException {
        f.writeLong(Long.reverseBytes(l));
    }

    public void Write(byte[] b, int bOffset, int len) throws IOException {
        f.write(b, bOffset, len);
    }
    public void Write(byte[] b) throws IOException {
        f.write(b);
    }
    public void Write(byte[][] bytes) throws IOException {
        for (int i = 0; i < bytes.length; ++i)
            Write(bytes[i]);
    }
    public void Read(byte[][] bytes) throws IOException {
        for (int i = 0; i < bytes.length; ++i)
            Read(bytes[i]);
    }

    // static functions

    public static byte[] ReadAllBytes(String filename) throws IOException {

        if (!file.Exist(filename)) return new byte[0];

        file f = new file(filename, OpenMode.RDONLY);

        byte[] bytes = new byte[f.Size()];
        f.f.readFully(bytes);
        f.Close();

        return bytes;
    }
    public static String ReadAllText(String filename) throws IOException {
        return new String(ReadAllBytes(filename));
    }
    static String[] ReadAllLines(String filename) throws IOException
    {
        return ReadAllText(filename).split("\n");
    }
    public static void WriteAllBytes(String filename, byte[] bytes) throws IOException {
        file.Remove(filename);

        file f = new file(filename);
        f.f.write(bytes);
        f.Close();
    }
    public static void WriteAllText(String filename, String text) throws IOException
    {
        WriteAllBytes(filename, text.getBytes());
    }
    public static void WriteAllLines(String filename, String[] lines) throws IOException {
        WriteAllText(filename, string.Join(lines, "\n"));
    }
    public static boolean Remove(String filename)
    {
        if (!filename.startsWith("/")) filename = Utility.filesPath + "/" + filename;
        return new File(filename).delete();
    }
    public static boolean Exist(String filename)
    {
        if (!filename.startsWith("/")) filename = Utility.filesPath + "/" + filename;

        java.io.File file = new File (filename);
        return file.exists();
    }
    // returns 0 if not exist
    public static int Size(String filename)
    {
        if (!filename.startsWith("/")) filename = Utility.filesPath + "/" + filename;

        java.io.File file = new File (filename);
        return (int) file.length();
    }
}
