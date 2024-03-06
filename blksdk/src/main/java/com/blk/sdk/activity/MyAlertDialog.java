package com.blk.sdk.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.blk.sdk.TickTimer;

public class MyAlertDialog extends AlertDialog implements TickTimer.TickTimerListener {
    private TickTimer tickTimer;
    int timeout;

    public MyAlertDialog(Context context) {
        super(context);

        setButton(Dialog.BUTTON_POSITIVE,"OK", (dialog, which) -> {
            tickTimer.cancel();
            dialog.dismiss();
        });

    }

    public void SetTimeout(int timeout)
    {
        if (tickTimer != null) {
            tickTimer.cancel();
            tickTimer = null;
        }

        this.timeout = timeout;
        setCancelable(timeout != 0);
        if (timeout <= 0) return;

        tickTimer = new TickTimer(timeout, 1);
        tickTimer.setTimeCountListener(this);
        tickTimer.start();
    }

    @Override
    public void onTickTimerFinish() {
        tickTimer.cancel();
        if (isShowing()) dismiss();
    }
    @Override
    public void onTick(long leftTime) {
    }
    @Override
    public void show() {

        try {
            super.show();

            Button b = getButton(Dialog.BUTTON_POSITIVE);
            LinearLayout.LayoutParams positiveButtonLL = (LinearLayout.LayoutParams) b.getLayoutParams();
            positiveButtonLL.gravity = Gravity.CENTER;
            b.setLayoutParams(positiveButtonLL);
            b.setVisibility(timeout <= 0 ? View.GONE: View.VISIBLE);
        } catch (Exception e) {
        }
    }
}
