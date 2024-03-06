package com.blk.sdk;

import static com.blk.sdk.c.ToString;
import static com.blk.sdk.c.memcpy;
import static com.blk.sdk.c.memset;
import static com.blk.sdk.c.strcpy;

import android.util.Log;
import com.blk.platform.ICard;
import com.blk.platform.IPlatform;

import java.util.Arrays;

public class CardReader {
    private static final String TAG = CardReader.class.getSimpleName();


    public String cardHolderName;
    public byte[] pan  = new byte[20];
    public byte[] ExpDate = new byte[4];

    public ICard iCard = IPlatform.get().card;
    public ICard.Result result = new ICard.Result();

    public void Read(int cardTypes, String amount, boolean allowQR)
    {
        String msg = "KART OKUTUNUZ";
        if (cardTypes == ICard.MAGNETIC)    msg = "MANYETİK OKUYUCUYU KULLANINIZ";
        if (cardTypes == ICard.MAG_ICC)     msg = "ÇİPLİ VEYA MANYETİK\nKARTINIZI OKUTUNUZ";

        UI.ShowSwipeCard(iCard, msg, amount, allowQR);

        Thread t = new Thread(() -> {
            Log.i(TAG, "iCard.Detect ... " + cardTypes);
            result = iCard.Detect(cardTypes);
        });
        t.start();

        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "Detect Status(" + result.status + ", " + result.cardType + ")");

        if (result.status == ICard.Status.OK && result.cardType == ICard.MAGNETIC) {
            getMagneticData();
        }
        if (result.status == ICard.Status.OK && result.cardType == ICard.PICC) {

        }
    }

    void getMagneticData()
    {
        //        int panStart = StringUtils.indexOfAny(track[i - 1], "0123456789");
//        int panLast = StringUtils.indexOfAnyBut(track[i - 1].substring(panStart), "0123456789");
//        pan = track[i - 1].substring(panStart, panLast + 1);
//        ISystem.out.println("Magnetic Track" + i + " = " + new String(stripDataBytes) + " PAN : " + pan);

        if (result.Tk2Len > 0) //rv == EP_MSR_OK) && (trk2.stat == 1))
        {
            Log.i(TAG, "result.track2 : " + new String(result.track2, 0, result.Tk2Len));

            int i, track2Start = 0, track2End = result.Tk2Len;

            if (result.track2[0] < '0' || result.track2[0]  > '9')
                track2Start = 1;
            if (result.track2[result.Tk2Len - 2] == '?')
                track2End--;
            if (result.track2[result.Tk2Len - 1] != '?')
                track2End++;

            memcpy(result.track2, Arrays.copyOfRange(result.track2, track2Start, track2End), result.Tk2Len = track2End - track2Start);
            Log.i(TAG, "track2 : " + new String(result.track2, 0, result.Tk2Len));

            if (result.track2[result.track2.length - 1] != '?')
                result.track2[result.track2.length - 1] = '?';


            for (i = 0; i < result.track2.length; i++)
                if (result.track2[i] < '0' || result.track2[i]  > '9')
                    break;

            strcpy(pan, new String(result.track2, 0, i));
            Log.i(TAG, "pan : " + ToString(pan));
        }

        // result.track1 : %B6765780011031670^MUTLU/AHMET               ^2307126112349460000000946000000?O
        if (result.Tk1Len > 0)
        {
            Log.i(TAG, "result.track1 : " + new String(result.track1, 0, result.Tk1Len));

            int i, start = -1, end = -1;
            for (i = 0; i < result.track1.length; i++) {
                if (start == -1 && result.track1[i] >= '0' && result.track1[i] <= '9') {
                    start = i;
                }
                if (start != -1 && (result.track1[i] < '0' || result.track1[i] > '9')) {
                    end = i;
                    break;
                }
            }
            if (pan[0] == 0 && start >= 0 && end > start) {
                memcpy(pan, 0, result.track1, start, end - start);
                Log.i(TAG, "pan : " + ToString(pan));
            }

            String st1 = new String(result.track1, 0, result.Tk1Len);
            start = st1.indexOf('^');
            end = st1.indexOf('^', start + 1);
            if (start >= 0 && end > start) {
                byte[] b = string.Trim(Arrays.copyOfRange(result.track1, start + 1, end), (byte) ' ');
                cardHolderName = new String(b, 0, c.strlen(b));
                Log.i(TAG, "cardHolderName : " + cardHolderName);
            }
            if (ExpDate[0] == 0 && end > 0) {
                memcpy(ExpDate, 0, result.track1, end + 1, 4);
                Log.i(TAG, "ExpDate : " + ToString(ExpDate));
            }
        }
    }


}
