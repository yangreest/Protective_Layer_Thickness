package com.example.gtj_sd_dg240.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.gtj_sd_dg240.R;

import androidx.recyclerview.widget.RecyclerView;

public class EmptyDataViewUtils {
    public static View getEmptyView(boolean isCenter, RecyclerView recyclerView) {
        View view = ((Activity) recyclerView.getContext()).getLayoutInflater().inflate(R.layout.view_data_empty, (ViewGroup) recyclerView.getParent(), false);
        View vCenter = view.findViewById(R.id.v_center);
        vCenter.setVisibility(isCenter ? View.GONE : View.VISIBLE);
        TextView textView = view.findViewById(R.id.textView);
        textView.setText("暂无内容");
        return view;
    }

    public static View getEmptyView(Activity context, boolean isCenter) {
        @SuppressLint("InflateParams")
        View view = context.getLayoutInflater().inflate(R.layout.view_data_empty, null);
        View vCenter = view.findViewById(R.id.v_center);
        vCenter.setVisibility(isCenter ? View.GONE : View.VISIBLE);
        return view;
    }
}
