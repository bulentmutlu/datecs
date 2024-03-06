package com.blk.sdk.activity;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.blk.sdk.R;
import com.blk.sdk.TickTimer;

import java.util.Timer;
import java.util.TimerTask;

public class BaseDialog extends Dialog implements TickTimer.TickTimerListener
{
    TextView textCounter;
    TextView topText;
    ImageView image;
    ProgressBar progressBar;
    Button okButton;
    ViewGroup layout;

    TickTimer timer;
    boolean fCancelOnTouch = false;
    boolean ffShowTimer = false;

    OnDismissListener onDismissListener;
    public BaseDialog(Activity activity, boolean unused)
    {
        super(activity);
        setContentView(R.layout.fragment_page_pinpad_ex);
    }

    public BaseDialog(Activity activity) {
        super(activity);

        setContentView(R.layout.fragment_lbasedialog);


        layout = findViewById(R.id.frameLayout);;
        textCounter  =  findViewById(R.id.textViewCounter);
        topText =  findViewById(R.id.textViewTop);
        image = findViewById(R.id.imageView);
        progressBar = findViewById(R.id.loadingPanel);
        okButton = findViewById(R.id.buttonOK);

        super.setOnDismissListener(dialogInterface -> {
            if (timer != null) timer.cancel();
            if (onDismissListener != null)
                onDismissListener.onDismiss(dialogInterface);
        });
// hide all views
//        for (int i = 0; i < layout.getChildCount(); i++){
//            View child = layout.getChildAt(i);
//            child.setVisibility(View.GONE);
////                if (child instanceof ViewGroup){
////                    disableEnableControls(enable, (ViewGroup)child);
////                }
//        }
    }
    public void setOnDismissListener(OnDismissListener onDismissListener)
    {
        this.onDismissListener = onDismissListener;
    }
    public void reset()
    {
// hide all views
        for (int i = 0; i < layout.getChildCount(); i++){
            View child = layout.getChildAt(i);
            child.setVisibility(View.GONE);
//                if (child instanceof ViewGroup){
//                    disableEnableControls(enable, (ViewGroup)child);
//                }
        }
        setCancelable(false);
        fCancelOnTouch = false;
        if (timer != null) timer.cancel();
    }
    public void setMessage(CharSequence message)  {
        topText.setText(message);
        topText.setVisibility(View.VISIBLE);
    }
    public void setImage(Bitmap bitmap)  {
        image.setVisibility(View.VISIBLE);
        image.setImageBitmap(bitmap);
//        int horizontalPadding = layout.getPaddingLeft();
//        layout.setPadding(0, layout.getPaddingTop(), 0, layout.getPaddingBottom());
//        layout.setPadding(horizontalPadding, layout.getPaddingTop(), horizontalPadding, layout.getPaddingBottom());
    }

    public void setProgressBar()
    {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void setTimeout(int timeout)
    {
        setTimeout(timeout, false, timeout > 0);
    }
    public void setTimeout(int timeout, boolean fShowTimer, boolean fDismissOnTouch)
    {
        fCancelOnTouch = false;
        this.ffShowTimer = fShowTimer;
        if (timer != null) timer.cancel();

        //setCanceledOnTouchOutside(false);
        if (timeout <= 0) return;

        setCancelable(true);
        setCanceledOnTouchOutside(true);
        fCancelOnTouch = fDismissOnTouch;
        okButton.setVisibility(View.VISIBLE);
        okButton.setOnClickListener(
                view -> dismiss()
        );

        timer = new TickTimer(timeout, 1);
        timer.setTimeCountListener(this);
        timer.start();
        if (fShowTimer) {
            textCounter.setText("" + timeout);
            textCounter.setVisibility(View.VISIBLE);
        }
    }
    public void setButtonText(String text)
    {
        okButton.setText(text);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Tap anywhere to close dialog.
        if (fCancelOnTouch) this.dismiss();
        return true;
    }

    @Override
    public void onTickTimerFinish() {
        if (BaseDialog.super.isShowing())
            dismiss();
    }

    @Override
    public void onTick(long leftTime) {
        try {
            if (ffShowTimer)
                textCounter.setText("" + (Integer.parseInt(String.valueOf(textCounter.getText())) - 1));
        } catch (Exception ignored)
        {

        }
    }
}
