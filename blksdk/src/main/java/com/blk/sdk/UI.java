package com.blk.sdk;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;

import androidx.annotation.Nullable;
import android.util.Log;
import android.util.Size;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.blk.platform.ICard;
import com.blk.sdk.activity.ActivityIP;
import com.blk.sdk.activity.ActivityList;
import com.blk.sdk.activity.ActivityPosPed;
import com.blk.sdk.activity.ActivitySwipeCard;
import com.blk.sdk.activity.BaseActivity;
import com.blk.sdk.activity.BaseDialog;
import com.blk.sdk.emulator.CardEmulator;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class UI {

    private static final String TAG = UI.class.getSimpleName();

    public enum ViewType{
        VIEW_GRID,
        VIEW_LIST;
    }

    public static int NONE = 0;
    public static int TIMEOUT = 1;
    public static int CANCEL = 2;
    public static int OK = 3;

    public static boolean networkSet = false, download = false;

    public static void Toast(final String text) {
        BaseActivity ba = BaseActivity.GetTopActivity();
        ba.runOnUiThread(() -> {
            Toast t = Toast.makeText(ba, text, Toast.LENGTH_LONG);
            //thread1.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            t.show();
        });
    }

    //    public static int ShowMessageHide() {
//        activity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                smd.hide();
//            }
//        });
//        return 0;
//    }

    public static void ShowMessage(final String message) {
        ShowMessage(2000, message);
    }
    public static void ShowMessage(final int timeout, final String message) {

        BaseActivity ba = BaseActivity.GetTopActivity();

        Utility.log("SHOWMSG ------" + ba.getClass().getSimpleName() + "-------- " + message);

        BaseDialog dialog = ba.baseDialog;
        AtomicBoolean fWait = new AtomicBoolean(true);

        ba.runOnUiThread(() -> {
            //dialog.hide();
            dialog.reset();
            dialog.setMessage(message);
            dialog.setTimeout(timeout);
            dialog.setTitle("");
            dialog.show();
            fWait.set(false);
        });

        while (!UiUtil.IsMainThread() && fWait.get()) Utility.sleep(10);

        while (timeout > 0 && dialog.isShowing() && !UiUtil.IsMainThread())
            Utility.sleep(10);
    }
    public static void ShowErrorMessage(final String message)
    {
        ShowMessage(5000, message);
    }

    public static int ShowQR2(final String message, final Bitmap qr, final int timeout, DialogInterface.OnDismissListener onDismissListener) {

        Utility.log("ShowQR ------" + BaseActivity.GetTopActivity().getClass().getSimpleName() + "-------- " + message);

        BaseActivity activity = BaseActivity.GetTopActivity();
        BaseDialog baseDialog = BaseActivity.GetTopActivity().baseDialog;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                baseDialog.reset();
                baseDialog.setMessage(message);
                baseDialog.setImage(qr);
                baseDialog.setTimeout(timeout, true, false);
                baseDialog.setButtonText("İPTAL");
                baseDialog.setOnDismissListener(onDismissListener);
                baseDialog.show();
            }
        });
        return 0;
    }

    public interface   IListItemString<T>
    {
        String onItemString(T item);
    }
    public static <T> T ShowList(String title, List<T> items, IListItemString<T> callback)
    {
        List<String> sItems = new ArrayList<>();
        for (T item: items) {
            sItems.add(callback.onItemString(item));
        }
        int selected = UI.ShowList(title, sItems.toArray(new String[0]));
        if (selected < 0) return null;
        return items.get(selected);
    }
    public static int ShowList(final String title, final String[] items) {
        return ShowList(title, items, null, ViewType.VIEW_LIST);
    }
    public static int ShowList(final String title, final String[] items, final int[] images) {
        return ShowList(title, items, images, ViewType.VIEW_LIST);
    }

    public static int ShowList(final String title, final String[] items, final int[] images, final ViewType viewType) {
        Log.i(TAG, "MenuSelect(" + title + ")");

        ActivityList list;
        int timeout = 30;

        //if (title.equals("İŞLEMLER")) timeout = 0;        else timeout = 30;

        // aynı liste oluşturulmak isteniyorsa, varolanı kullansın
        if (true) { //!BaseActivity.GetTopActivity().getTitle().equals(title)) {
            Intent intent = new Intent(BaseActivity.GetTopActivity(), ActivityList.class);
            intent.putExtra("items", items);
            intent.putExtra("images", images);
            intent.putExtra("title", title);
            intent.putExtra("viewType", viewType);
            intent.putExtra("timeout", timeout);

            list = (ActivityList) BaseActivity.StartActivity(title, intent, true);
        } else {
            list = (ActivityList) BaseActivity.GetTopActivity();
            list.endStatus = NONE;
            list.selected = -1;
        }

        while (list.endStatus == NONE && !UiUtil.IsMainThread())
            Utility.sleep(10);
        while (list.activityVisible)
            Utility.sleep(10);

        Log.i(TAG, "List(" + title + ") endStatus(" + list.endStatus + ") activityVisible(" + list.activityVisible + ") selected(" + list.selected + ")");
        BaseActivity.fForward = list.endStatus == OK;

        int rv =list.endStatus == OK ? list.selected : -1;
        //if (rv == -1) list.finish();
        return rv;
    }

    public static void ShowSwipeCard(ICard iCard, String message, @Nullable String amount, boolean allowQR) {

        //if (!BaseActivity.activities.get(BaseActivity.activities.size() - 1).getTitle().equals(title)) {
        ActivitySwipeCard.iCard = iCard;
        ActivitySwipeCard.allowQR = allowQR;
        Intent intent = new Intent(BaseActivity.GetTopActivity(), ActivitySwipeCard.class);
        intent.putExtra("message", message);
        intent.putExtra("amount", amount);
        if (iCard instanceof CardEmulator) {
            intent.putExtra("magnetic", true);
        }
        BaseActivity.StartActivity("ShowSwipeCard", intent);
        //}
    }

    public static String GetAmount(final String header, final int max) {

        Intent intent = new Intent(BaseActivity.GetTopActivity(), ActivityPosPed.class);
        intent.putExtra("header", header);
        intent.putExtra("max", max);
        intent.putExtra("style", ActivityPosPed.INPUT_STYLE.AMOUNT);
        ActivityPosPed posPed = (ActivityPosPed) BaseActivity.StartActivity(header, intent, true);

        while (posPed.endStatus == NONE && !UiUtil.IsMainThread()) Utility.sleep(10);
        BaseActivity.fForward = posPed.endStatus == OK;

        if (posPed.endStatus == OK) {
            String amount = posPed.value;
            Log.i(TAG, "amount : " + amount);

            if (amount.indexOf('.') > 0)
                amount = new StringBuilder(amount).deleteCharAt(amount.indexOf('.')).toString();
            if (c.atoi(amount.getBytes()) == 0) return null;
            return amount;
        }
        return null;
    }
    public static String GetNumber(final String header, final String initial, final int min, final int max, final boolean fAcceptZero, final int timeout, final boolean fMask) {
        return GetNumber(header, initial, min, max, fAcceptZero, timeout, fMask, null, false);
    }
    public static String GetNumber(final String header) {
        return GetNumber(header, "", 1, 30, false, 60000, false, null, false);
    }
    public static String GetNumber(final String header, final String initial, final int min, final int max, final boolean fAcceptZero, final int timeout, final boolean fMask, int[] endStatus, final boolean isBankRef) {

        Intent intent = new Intent(BaseActivity.GetTopActivity(), ActivityPosPed.class);
        intent.putExtra("header", header);
        intent.putExtra("min", min);
        intent.putExtra("max", max);
        intent.putExtra("fAcceptZero", fAcceptZero);
        intent.putExtra("initial", initial);
        intent.putExtra("timeout", timeout);
        intent.putExtra("fMask", fMask);
        intent.putExtra("style", ActivityPosPed.INPUT_STYLE.NUMBER);
        intent.putExtra("isBankRef", isBankRef);
        ActivityPosPed posPed = (ActivityPosPed) BaseActivity.StartActivity(header, intent, true);

        while (posPed.endStatus == NONE && !UiUtil.IsMainThread()) Utility.sleep(10);
        BaseActivity.fForward = posPed.endStatus == OK;

        if (endStatus != null) endStatus[0] = posPed.endStatus;

        if (posPed.endStatus == OK) {
            String number = posPed.value;
            Log.i(TAG, "number : " + number);

            if (number.length() == 0) return null;
            return number;
        }
        return null;
    }
    public static String GetIP(final String header, final String initial) {

        Intent intent = new Intent(BaseActivity.GetTopActivity(), ActivityIP.class);
        intent.putExtra("header", header);
        intent.putExtra("initial", initial);
        ActivityIP IP = (ActivityIP) BaseActivity.StartActivity(header, intent, true);

        while (IP.endStatus == NONE && !UiUtil.IsMainThread()) Utility.sleep(10);
        BaseActivity.fForward = IP.endStatus == OK;

        if (IP.endStatus == OK) {
            Log.i(TAG, "ip : " + IP.value);
            return IP.value;
        }
        return null;
    }
    public static String GetPan(final String header, final int timeout) {

        Intent intent = new Intent(BaseActivity.GetTopActivity(), ActivityPosPed.class);
        intent.putExtra("header", header);
        intent.putExtra("min", 15);
        intent.putExtra("max", 23);
        intent.putExtra("timeout", timeout);
        intent.putExtra("style", ActivityPosPed.INPUT_STYLE.PAN);
        ActivityPosPed posPed = (ActivityPosPed) BaseActivity.StartActivity(header, intent, true);

        while (posPed.endStatus == NONE && !UiUtil.IsMainThread()) Utility.sleep(10);
        BaseActivity.fForward = posPed.endStatus == OK;

        if (posPed.endStatus == OK) {
            Log.i(TAG, "pan : " + posPed.value);

            if (posPed.value.length() == 0) return null;
            return posPed.value;
        }
        return null;
    }
    public static String GetExpDate(final String header, final int timeout) {

        Intent intent = new Intent(BaseActivity.GetTopActivity(), ActivityPosPed.class);
        intent.putExtra("header", header);
        intent.putExtra("min", 4);
        intent.putExtra("max", 6);
        intent.putExtra("timeout", timeout);
        intent.putExtra("style", ActivityPosPed.INPUT_STYLE.EXPDATE);
        ActivityPosPed posPed = (ActivityPosPed) BaseActivity.StartActivity(header, intent, true);

        while (posPed.endStatus == NONE && !UiUtil.IsMainThread()) Utility.sleep(10);
        BaseActivity.fForward = posPed.endStatus == OK;

        if (posPed.endStatus == OK) {
            Log.i(TAG, "Expire Date: " + posPed.value);

            if (posPed.value.length() == 0) return null;
            return posPed.value;
        }
        return null;
    }

    public static void NetworkSettings() {

        networkSet = true;
        Intent intent = new Intent(Settings.ACTION_APN_SETTINGS);
        BaseActivity.GetTopActivity().startActivityForResult(intent, 122);

        while (networkSet)
            Utility.sleep(100);
    }

    public static class UiUtil {

        public static void Keyboard(boolean fShow)
        {
            //For hiding keyboard:
            if (!fShow) {

                InputMethodManager imm = (InputMethodManager) BaseActivity.GetTopActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                //imm.hideSoftInputFromWindow(myEditText.getWindowToken(), 0);
                return;
            }
            //For Showing keyboard:

            InputMethodManager imm = (InputMethodManager) BaseActivity.GetTopActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    //
    //    public static AppCompatActivity getCurrentActivity() {
    //        Class activityThreadClass = null;
    //        try {
    //            activityThreadClass = Class.forName("android.app.ActivityThread");
    //
    //            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
    //            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
    //            activitiesField.setAccessible(true);
    //
    //            Map<Object, Object> activities = (Map<Object, Object>) activitiesField.get(activityThread);
    //            if (activities == null)
    //                return null;
    //
    //            AppCompatActivity activity = null;
    //
    //            for (Object activityRecord : activities.values()) {
    //                Class activityRecordClass = activityRecord.getClass();
    //                Field pausedField = activityRecordClass.getDeclaredField("paused");
    //                pausedField.setAccessible(true);
    //
    //                Field activityField = activityRecordClass.getDeclaredField("activity");
    //                activityField.setAccessible(true);
    //                activity = (AppCompatActivity) activityField.get(activityRecord);
    //
    //                if (pausedField.getBoolean(activityRecord)) {
    //                    continue;
    //                }
    //                return activity;
    //            }
    //            return activity;
    //
    //        } catch (ClassNotFoundException e) {
    //            e.printStackTrace();
    //        } catch (IllegalAccessException e) {
    //            e.printStackTrace();
    //        } catch (InvocationTargetException e) {
    //            e.printStackTrace();
    //        } catch (NoSuchMethodException e) {
    //            e.printStackTrace();
    //        } catch (NoSuchFieldException e) {
    //            e.printStackTrace();
    //        }
    //        return null;
    //    }

        static Application app;
        public static Application getApplication() {
            if (app != null) return app;

            try {
                app = (Application) Class.forName("android.app.ActivityThread")
                        .getMethod("currentApplication").invoke(null, (Object[]) null);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return app;
        }

        public static Context getApplicationContext() {
            return getApplication().getApplicationContext();
        }

        public static void ShowMessageHide() {
            BaseActivity.GetTopActivity().runOnUiThread(() -> BaseActivity.GetTopActivity().baseDialog.hide());
        }

        public static boolean IsMainThread() {
            return Looper.getMainLooper().getThread() == Thread.currentThread();
        }

        public static boolean IsAppInBackground() {
            boolean isInBackground = true;
            Context context = BaseActivity.GetApplicationContext();
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
                List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
                for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                    if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        for (String activeProcess : processInfo.pkgList) {
                            if (activeProcess.equals(context.getPackageName())) {
                                isInBackground = false;
                            }
                        }
                    }
                }
            } else {
                List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
                ComponentName componentInfo = taskInfo.get(0).topActivity;
                if (componentInfo.getPackageName().equals(context.getPackageName())) {
                    isInBackground = false;
                }
            }

            return isInBackground;
        }

        // 720x1184
        public static Size ScreenSize()
        {
            return new Size(Resources.getSystem().getDisplayMetrics().widthPixels, Resources.getSystem().getDisplayMetrics().heightPixels);
        }
    }
}
