package com.example.gtj_sd_dg240.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.gtj_sd_dg240.R;

import androidx.core.content.ContextCompat;

public class StyleAlertDialog extends Dialog {
    private Context context;
    private CharSequence contentText;
    private String strleft, strright, strok;
    private View.OnClickListener leftListener;
    private View.OnClickListener rightListener;
    private View.OnClickListener okListener;
    private String type;
    public Button btnLeft, btnRight, btnOk;

    public StyleAlertDialog(Context context, String type) {
        super(context, R.style.StyleAlertDialog);
        setCancelable(false);
        this.context = context;
        this.type = type;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_dialog);
        initView();
    }

    private void initView() {
        TextView tvContent = findViewById(R.id.dialog_content);
        btnLeft = findViewById(R.id.btn_dialog_cancel);
        btnRight = findViewById(R.id.btn_dialog_submit);
        btnOk = findViewById(R.id.btn_dialog_ok);
        switch (type) {
            case "blue":
                tvContent.setTextColor(ContextCompat.getColor(context, R.color.colorBlue));
                btnLeft.setBackgroundResource(R.drawable.bg_gradient_blue);
                btnRight.setBackgroundResource(R.drawable.bg_gradient_blue);
                btnOk.setBackgroundResource(R.drawable.bg_gradient_blue);
                break;
            case "yellow":
                tvContent.setTextColor(ContextCompat.getColor(context, R.color.colorFF9600));
                btnLeft.setBackgroundResource(R.drawable.bg_gradient_yellow);
                btnRight.setBackgroundResource(R.drawable.bg_gradient_yellow);
                btnOk.setBackgroundResource(R.drawable.bg_gradient_yellow);
                break;
            case "red":
                tvContent.setTextColor(ContextCompat.getColor(context, R.color.colorFF3B30));
                btnLeft.setBackgroundResource(R.drawable.bg_gradient_red);
                btnRight.setBackgroundResource(R.drawable.bg_gradient_red);
                btnOk.setBackgroundResource(R.drawable.bg_gradient_red);
                break;
        }
        if (!TextUtils.isEmpty(contentText)) {
            tvContent.setText(contentText);
        }
        if (okListener != null) {
            btnOk.setText(strok);
            btnOk.setVisibility(View.VISIBLE);
            btnOk.setOnClickListener(okListener);
            return;
        }
        if (leftListener != null) {
            btnLeft.setText(strleft);
            btnLeft.setVisibility(View.VISIBLE);
            btnLeft.setOnClickListener(leftListener);
        }
        if (rightListener != null) {
            btnRight.setText(strright);
            btnRight.setVisibility(View.VISIBLE);
            btnRight.setOnClickListener(rightListener);
        }
    }

    public StyleAlertDialog setContent(CharSequence content) {
        contentText = content;
        return this;
    }

    public StyleAlertDialog setLeftButton(String strleft, View.OnClickListener listener) {
        this.strleft = strleft;
        leftListener = listener;
        return this;
    }

    public StyleAlertDialog setRightButton(String strright, View.OnClickListener listener) {
        this.strright = strright;
        rightListener = listener;
        return this;
    }

    public StyleAlertDialog setOKButton(String strok, View.OnClickListener listener) {
        this.strok = strok;
        okListener = listener;
        return this;
    }
}
