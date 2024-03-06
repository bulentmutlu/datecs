package com.blk.techpos;

import static com.blk.sdk.c.memcpy;
import static com.blk.sdk.c.strcpy;



import android.util.Log;

import com.blk.sdk.Convert;
import com.blk.sdk.com.TcpClient;
import com.blk.sdk.UI;
import com.blk.sdk.Rtc;
import com.blk.sdk.c;
import com.blk.techpos.Bkm.Messages.Msgs;
import com.blk.techpos.Bkm.Reversal;

import java.util.Arrays;
import static com.blk.techpos.PrmStruct.params;
import static com.blk.techpos.Bkm.TranStruct.currentTran;
/**
 * Created by id on 2.03.2018.
 */

public class Comms {

    private static final String TAG = Comms.class.getSimpleName();


    TcpClient comm = new TcpClient();
    byte[] CommBuff = new byte[4 * 1024];
    byte[] retBuff;
    public static boolean fShowUI = true;

    public byte[] CommsSendRecv(final Msgs.MessageType aMsgType, final byte[] aMsg, final int aLen) throws Exception {

        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    if (aMsgType == Msgs.MessageType.M_PRMDOWNLOAD)
                        UI.ShowMessage(0, "LÜTFEN BEKLEYİNİZ\nPARAMETRE YÜKLENİYOR");
                    else if (aMsgType == Msgs.MessageType.M_BATCHUPLOAD)
                        UI.ShowMessage(0, "LÜTFEN BEKLEYİNİZ\nBATCH UPLOAD\nYAPILIYOR");
                    else if (aMsgType == Msgs.MessageType.M_ENDOFDAY)
                        UI.ShowMessage(0, "LÜTFEN BEKLEYİNİZ\nGÜNSONU YAPILIYOR");
                    else if (fShowUI)
                        UI.ShowMessage(0, "LÜTFEN BEKLEYİNİZ\nİŞLEM YAPILIYOR");

                    try {
                        Connect();
                    } catch (Exception e) {
                        e.printStackTrace();

                        comm.close();

                        strcpy(currentTran.RspCode, "C7");
                        strcpy(currentTran.ReplyDescription, " BAĞLANTI KURULAMADI");
                        c.strcat(currentTran.ReplyDescription, "  TEKRAR  DENEYİNİZ ");
                        return;
                    }

                    if (aMsgType == Msgs.MessageType.M_AUTHORIZATION)
                        Reversal.ReverseTran();
                    currentTran.unableToGoOnline = 0;

                    try {
                        Log.i(TAG, "CommSend(" + aLen + ") : " + Convert.Buffer2Hex(aMsg));
                        comm.write(aMsg, aLen);
                    } catch (Exception e) {
                        e.printStackTrace();

                        comm.close();

                        strcpy(currentTran.RspCode, "C8");
                        strcpy(currentTran.ReplyDescription, "   BAĞLANTI HATASI  ");
                        c.strcat(currentTran.ReplyDescription, "  TEKRAR  DENEYİNİZ ");
                        return;
                    }


                    int srIdx = 0;
                    byte[] recvBuffer = comm.read(2);
                    Log.i(TAG, "CommRecv(" + recvBuffer.length + ") : " + Convert.Buffer2Hex(recvBuffer));

                    if (recvBuffer != null && recvBuffer.length >= 2) {
                        params.LastCommTime = Rtc.GetTimeSeconds();
                        PrmStruct.Save("LastCommTime");

                        memcpy(CommBuff, srIdx, recvBuffer, 0, recvBuffer.length);
                        srIdx += recvBuffer.length;

                        //EP_printf("EP_CommRecv:%d", srIdx);
                        //EP_HexDump(CommBuff, srIdx);

                        int msgLen = GetMsgLenFromHeader(CommBuff);
                        //Log.i(TAG, "msglen : " + msgLen);
                        if (srIdx < (msgLen + 2)) {
                            while (true) {
                                recvBuffer = comm.read(msgLen);
                                //Log.i(TAG,"CommRecv : " + ((recvBuffer == null) ? "null" : recvBuffer.length));
                                if (recvBuffer == null)
                                    break;

                                memcpy(CommBuff, srIdx, recvBuffer, 0, recvBuffer.length);
                                srIdx += recvBuffer.length;
                                if (srIdx >= (msgLen + 2))
                                    break;
                            }
                        }

                        retBuff = Arrays.copyOf(CommBuff, srIdx);
                        Log.i(TAG, "COMRECEIVE(" + retBuff.length + ") : " + Convert.Buffer2Hex(retBuff));

                    } else {
                        strcpy(currentTran.RspCode, "C9");
                        strcpy(currentTran.ReplyDescription, "   CEVAP ALINAMADI  ");
                        c.strcat(currentTran.ReplyDescription, "  TEKRAR  DENEYİNİZ ");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();

        t.join();

        return retBuff;
    }

    public byte[] SendRecvOdeAl(final byte[] aMsg, final int aLen) throws Exception {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

//                try {
//
//                    try {
//                        comm.EP_CommConnect("176.53.48.150", 8180);
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//
//                        comm.EP_CommDisConnect();
//
//                        strcpy(currentTran.RspCode, "C7");
//                        strcpy(currentTran.ReplyDescription, " BAĞLANTI KURULAMADI");
//                        c.strcat(currentTran.ReplyDescription, "  TEKRAR  DENEYİNİZ ");
//                        return;
//                    }
//
//
//                    currentTran.unableToGoOnline = 0;
//
//                    try {
//                        Log.i(TAG, "CommSend(" + aLen + ") : " + Convert.Buffer2Hex(aMsg));
//                        comm.EP_CommSend(aMsg, aLen);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//
//                        comm.EP_CommDisConnect();
//
//                        strcpy(currentTran.RspCode, "C8");
//                        strcpy(currentTran.ReplyDescription, "   BAĞLANTI HATASI  ");
//                        c.strcat(currentTran.ReplyDescription, "  TEKRAR  DENEYİNİZ ");
//                        return;
//                    }
//
//
//                    int srIdx = 0;
//                    byte[] recvBuffer = comm.EP_CommRecv(2);
//                    Log.i(TAG, "CommRecv(" + recvBuffer.length + ") : " + Convert.Buffer2Hex(recvBuffer));
//
//                    if (recvBuffer != null && recvBuffer.length >= 2) {
//                        params.LastCommTime = Rtc.EP_GetTimeSeconds();
//                        PrmStruct.Save("LastCommTime");
//
//                        memcpy(CommBuff, srIdx, recvBuffer, 0, recvBuffer.length);
//                        srIdx += recvBuffer.length;
//
//                        //EP_printf("EP_CommRecv:%d", srIdx);
//                        //EP_HexDump(CommBuff, srIdx);
//
//                        int msgLen = GetMsgLenFromHeader(CommBuff);
//                        //Log.i(TAG, "msglen : " + msgLen);
//                        if (srIdx < (msgLen + 2)) {
//                            while (true) {
//                                recvBuffer = comm.EP_CommRecv(msgLen);
//                                //Log.i(TAG,"CommRecv : " + ((recvBuffer == null) ? "null" : recvBuffer.length));
//                                if (recvBuffer == null)
//                                    break;
//
//                                memcpy(CommBuff, srIdx, recvBuffer, 0, recvBuffer.length);
//                                srIdx += recvBuffer.length;
//                                if (srIdx >= (msgLen + 2))
//                                    break;
//                            }
//                        }
//
//                        retBuff = Arrays.copyOf(CommBuff, srIdx);
//                        Log.i(TAG, "COMRECEIVE(" + retBuff.length + ") : " + Convert.Buffer2Hex(retBuff));
//
//                    } else {
//                        strcpy(currentTran.RspCode, "C9");
//                        strcpy(currentTran.ReplyDescription, "   CEVAP ALINAMADI  ");
//                        c.strcat(currentTran.ReplyDescription, "  TEKRAR  DENEYİNİZ ");
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }


            }
        });
        t.start();

        t.join();

        return retBuff;
    }

    void Connect() throws Exception {
        try {
            comm.open(params.sTcpip.destIp, params.sTcpip.destPort);
        } catch (Exception e) {
            comm.open(params.sBackupTcpip.destIp, params.sBackupTcpip.destPort);
        }
    }

    static int GetMsgLenFromHeader(byte[] buff) {
        return Convert.unsignedByteToInt(buff[0]) * 256 + Convert.unsignedByteToInt(buff[1]);
    }


}
