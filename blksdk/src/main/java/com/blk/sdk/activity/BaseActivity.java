package com.blk.sdk.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import com.blk.sdk.UI;
import com.blk.sdk.Utility;

import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();

    public static List<BaseActivity> activities = new ArrayList<>();
    public static  BaseActivity lastActivity;

    public static boolean fForward = true;
    //public MyAlertDialog dialog;
    // Note that leaving the activity when a dialog not dismissed causes the memory leak.
    public BaseDialog baseDialog;
    public boolean activityVisible;
    String _title;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        _title = getIntent().getStringExtra("BaseActivity.title");

        Log.i(TAG, "onCreate(" + _title + ") " + getClass().getSimpleName() + " (" + Thread.currentThread().getId() + ")");

        activities.add(this);
        lastActivity = this;
        //dialog = new MyAlertDialog(this);
        baseDialog = new BaseDialog(this);
    }
    @Override
    protected void onStart() {
        Log.i(TAG,"onstart(" + _title + ") " + getClass().getSimpleName());
        super.onStart();

//        if (fForward) overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
//        else {
//            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
//            fForward = true;
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityVisible = true;
        Log.i(TAG,"onResume(" + _title + ") " + getClass().getSimpleName());
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG,"onPause(" + _title + ", " + isFinishing() + ") " + getClass().getSimpleName());

        if (isFinishing ()) {
            //baseDialog.hide();
            if (baseDialog != null) baseDialog.dismiss();
            activities.remove(this);
        }
        activityVisible = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG,"onstop(" + _title + ", " + isFinishing() + ") "  + getClass().getSimpleName());

        if (isFinishing ()) {
            if (baseDialog != null) baseDialog.dismiss();
        }


        //eğer uygulama backgrounda düştüyse bir şey yapma
        // if(IsAppInBackground()) return;

        // screen saver çıktığında activity destoy olmasın
        //if (activities.get(activities.size() - 1).getClass().equals(ScreenSaver.class))            return;

        // main activityi finish yapma
//        if (this.equals(activities.get(0)))
//            return;

        //dialog.hide();
        //if (baseDialog.isShowing()) baseDialog.hide();
        //finish();
    }

    @Override
    protected void onDestroy() {
        if (baseDialog != null) baseDialog.dismiss();

        super.onDestroy();
        Log.i(TAG,"ondestroy(" + _title + ") "  + getClass().getSimpleName());
    }

    public static BaseActivity GetMainActivity() {
        return activities.get(0);
    }

    public static BaseActivity GetTopActivity() {
        if (activities.size() == 0) return null;

        BaseActivity activity = activities.get(activities.size() - 1);
        Log.i(TAG, "GetTopActivity(" + activity._title + ") " + activity.getClass().getSimpleName());
        return activity;
    }
    public static Context GetApplicationContext() {
        return activities.get(0).getApplicationContext();
    }

    public static BaseActivity StartActivity(String title, Intent intent) {
        return StartActivity(title, intent, false);
    }

    public static BaseActivity StartActivity(String title, Intent intent, boolean fWaitForCreate ) {
        BaseActivity topActivity =  activities.get(activities.size() - 1);

        Utility.log("StartActivity(" + title + ") " + topActivity.getLocalClassName() + " : " + intent.getComponent().getClassName());
        intent.putExtra("BaseActivity.title", title);

        if (fWaitForCreate) lastActivity = null;

        topActivity.startActivity(intent);

        while (fWaitForCreate && BaseActivity.lastActivity == null) Utility.sleep(10);

        return BaseActivity.lastActivity;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult: " + requestCode + " " + resultCode);
        if (requestCode == 122) {

            UI.networkSet = false;

            if (resultCode == RESULT_OK) {
            }
        }
        if (requestCode == 2204) {

            UI.download = false;

            if (resultCode == RESULT_OK) {
            }
        }
    }


}
