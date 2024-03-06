package com.blk.techpos;

import android.app.Application;
import android.util.Log;

import com.blk.sdk.emulator.Emulator;

public class AppTechpos extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("AppTechpos", "onCreate: AppTechpos");

//@bm 2024
//       if (Build.DEVICE.startsWith("generic"))
            new Emulator();
//       else
//           new PlatfromCtos();
    }
}
