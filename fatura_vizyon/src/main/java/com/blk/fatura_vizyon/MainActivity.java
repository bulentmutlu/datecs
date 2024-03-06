package com.blk.fatura_vizyon;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import android.util.Log;
import android.view.View;

import com.blk.fatura_vizyon.ui.main.SectionsPagerAdapter;
import com.blk.fatura_vizyon.databinding.ActivityMainBinding;
import com.blk.sdk.UI;
import com.blk.sdk.Utility;
import com.blk.sdk.activity.BaseActivity;

public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;
    public Thread myThread;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = binding.viewPager;
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = binding.tabs;
        tabs.setupWithViewPager(viewPager);
        FloatingActionButton fab = binding.fab;

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        myThread = new Thread(() -> myTreadFunc(this));
        myThread.start();
    }

    void myTreadFunc(BaseActivity activity)
    {
        Log.i(TAG, "Worker Thread Start : " + Thread.currentThread().getId());

        try {
            FaturaVizyon.Init();
        } catch (Exception e) {
            e.printStackTrace();
            UI.ShowMessage(150000, e.getMessage());
            return;
        }

        while (!Thread.currentThread().isInterrupted()) {
            //activity.runOnUiThread(() -> activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON));

            if (!activity.activityVisible) {
                Utility.sleep(100);
                continue;
            }

            try {

                Menu.MainMenu(activity);

            } catch (Exception e) {
                e.printStackTrace();
                UI.ShowMessage(15000, e.getMessage());
            }

        }
    }
}