package com.blk.sdk.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;

import com.blk.sdk.UI;
import com.blk.sdk.TickTimer;
import com.blk.sdk.R;

import java.util.ArrayList;
import java.util.List;

public class ActivityList extends BaseActivity implements TickTimer.TickTimerListener {
    ListView listView;
    GridView gridView;

    public int endStatus = UI.NONE;
    public int selected = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_menu_item_click_event);
        listView = (ListView) findViewById(R.id.list);
        gridView = (GridView) findViewById(R.id.gridList);


        Intent intent = getIntent();
        String[] values = intent.getStringArrayExtra("items");
        int[] images = intent.getIntArrayExtra("images");
        String title = intent.getStringExtra("title");
        UI.ViewType viewType = (UI.ViewType) intent.getSerializableExtra("viewType");
        int timeout = intent.getIntExtra("timeout", 0);

        this.setTitle(title);

        if (timeout > 0) {
            timer = new TickTimer(timeout, 1);
            timer.setTimeCountListener(this);
            timer.start();
        }

        //  TimerReset(true);

        List<String> valueList = new ArrayList<String>();
        List<Integer> imageList = new ArrayList<Integer>();

        for (int i = 0; i < values.length; ++i) {
            if (values[i] == null) break;
            valueList.add(values[i]);
            if (images != null) imageList.add(images[i]);
        }

//        valueList.addAll(Arrays.asList(values));
//        for (int i = 0; i < images.length; i++) {
//            imageList.add(images[i]);
//        }

        CustomAdapter adapter = new CustomAdapter(this, valueList, imageList, title, viewType);
        if (viewType == UI.ViewType.VIEW_GRID) {

            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    endStatus = UI.OK;
                    selected = position;
                    TimerReset(true);
                    finish();
                }
            });

        } else {
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    endStatus = UI.OK;
                    selected = position;
                    TimerReset(true);
                    finish();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        TimerReset(false);
        if (endStatus == UI.NONE) endStatus = UI.CANCEL;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        endStatus = UI.CANCEL;
        super.onBackPressed();
    }


    private TickTimer timer;

    void TimerReset(boolean fRestart) {
        if (timer != null) {
            timer.cancel();
            if (fRestart) timer.start();
        }
    }

    //timer
    @Override
    public void onTickTimerFinish() {
        TimerReset(false);
        endStatus = UI.CANCEL;
        finish();
    }

    @Override
    public void onTick(long leftTime) {

    }
}
