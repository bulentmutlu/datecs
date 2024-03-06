package com.blk.sdk.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.blk.sdk.UI;
import com.blk.sdk.R;

import java.util.List;

public class CustomAdapter extends BaseAdapter {

    private Context mContext;

    private LayoutInflater mInflater;
    private List<String> menuItems;
    private List<Integer> imageId;
    String title;
    UI.ViewType viewType;

    public CustomAdapter(Context context, List<String> menuItems, List<Integer> imageId, String title, UI.ViewType viewType) {
        mContext = context;
        this.imageId = imageId;
        this.menuItems = menuItems;
        this.title = title;
        this.viewType = viewType;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return menuItems.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @SuppressLint("ResourceType")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        TextView textView;
        ImageView imageView = null;

        if (viewType == UI.ViewType.VIEW_LIST)
            view = mInflater.inflate(R.layout.list_single, null);
        else {
            view = mInflater.inflate(R.layout.grid_single, null);
        }
        textView = (TextView) view.findViewById(R.id.grid_text);
        imageView = (ImageView) view.findViewById(R.id.grid_image);

        if (viewType == UI.ViewType.VIEW_GRID) {
            imageView.setBackgroundResource(R.layout.grid_items_border);
        }


        textView.setText(menuItems.get(i));
        if (imageId.size() > i &&  imageId.get(i) != 0) {
            imageView.setImageResource(imageId.get(i));

        } else {
            imageView.setVisibility(View.GONE);
        }

        return view;
    }
}
