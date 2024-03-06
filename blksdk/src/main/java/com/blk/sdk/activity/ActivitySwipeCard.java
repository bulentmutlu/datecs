package com.blk.sdk.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.blk.platform.ICard;
import com.blk.sdk.Utility;
import com.blk.sdk.R;

import java.text.DecimalFormat;

public class ActivitySwipeCard extends BaseActivity {
    private static final String TAG = ActivitySwipeCard.class.getSimpleName();

    //public int endStatus = 0;
    VideoView vid;
    MediaController m;
    public ImageView led1,led2,led3,led4;
    BroadcastReceiver mReceiver = new ScreenReceiver();
    public TextView tvAmount;
    public static ICard iCard;
    public static boolean allowQR;

    public static class ScreenReceiver extends BroadcastReceiver {

        public static boolean wasScreenOn = true;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                // do whatever you need to do here
                wasScreenOn = false;

            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                // and do whatever you need to do here
                wasScreenOn = true;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG,"ONSTART");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //endStatus = 0;
        //Utility.SaveSharedPref("ActivitySwipeCard.endStatus", endStatus);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe_card);

        Button cancel = findViewById(R.id.button3);
        tvAmount= findViewById(R.id.tvAmount);

        TextView t = findViewById(R.id.textView2);
        String message=getIntent().getStringExtra("message");
        String amount = getIntent().getStringExtra("amount");
        boolean fShowMagneticButton = getIntent().getBooleanExtra("magnetic", false);
        t.setText(message);

        led1 = findViewById(R.id.led1);
        led2 = findViewById(R.id.led2);
        led3 = findViewById(R.id.led3);
        led4 = findViewById(R.id.led4);

        if (!Utility.IsNullorEmpty(amount)) {
            DecimalFormat df=new DecimalFormat("0.00");
            int a= Integer.parseInt(amount);
            double x=(double) a/100;
            amount = df.format(x);
            tvAmount.setVisibility(View.VISIBLE);
            tvAmount.setText("TUTAR: "+amount);
        }
        else{
            tvAmount.setVisibility(View.INVISIBLE);
        }

        ImageView contactlessImage= findViewById(R.id.contactlessImage);
        cancel.setOnClickListener(view -> {
            //endStatus = 1;
            //Utility.SaveSharedPref("ActivitySwipeCard.endStatus", endStatus);
            //DeviceInfo.idal.getCardReaderHelper().stopPolling();
            if (iCard != null) iCard.Interrupt(ICard.InterruptReason.CANCEL);
            finish();
        });

        Button manuel = findViewById(R.id.button4);
        manuel.setOnClickListener(v -> {
           //endStatus = 2;
            //Utility.SaveSharedPref("ActivitySwipeCard.endStatus", endStatus);
            //DeviceInfo.idal.getCardReaderHelper().stopPolling();
            if (iCard != null) iCard.Interrupt(ICard.InterruptReason.MANUEL);
            finish();
        });
        Button qr = findViewById(R.id.button5);
        qr.setVisibility(allowQR ? View.VISIBLE: View.INVISIBLE);
        qr.setOnClickListener(v -> {
            //endStatus = 2;
            //Utility.SaveSharedPref("ActivitySwipeCard.endStatus", endStatus);
            //DeviceInfo.idal.getCardReaderHelper().stopPolling();
            if (iCard != null) iCard.Interrupt(ICard.InterruptReason.QR);
            finish();
        });

        Button magnetic = findViewById(R.id.buttonMAG);
        magnetic.setVisibility(fShowMagneticButton ? View.VISIBLE: View.INVISIBLE);
        magnetic.setOnClickListener(v -> {
            if (iCard != null) iCard.Interrupt(ICard.InterruptReason.EMULATE_MAGNETIC);
            finish();
        });

        if(!t.getText().toString().equals("KART OKUTUNUZ")){
            contactlessImage.setImageResource(R.drawable.card_insert);
            led1.setVisibility(View.GONE);
            led2.setVisibility(View.GONE);
            led3.setVisibility(View.GONE);
            led4.setVisibility(View.GONE);
        }else{
            contactlessImage.setImageResource(R.drawable.contactless2);

        }
    }

    @Override
    public void onBackPressed() {
        //endStatus = 1;
        //Utility.SaveSharedPref("ActivitySwipeCard.endStatus", endStatus);
        if (iCard != null) iCard.Interrupt(ICard.InterruptReason.CANCEL);
        //DeviceInfo.idal.getCardReaderHelper().stopPolling();
        super.onBackPressed();
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();

    }
    public void playVideo(View v) { //Your code here
        m = new MediaController(this);
        vid.setMediaController(m);
    }
}
