package com.example.gtj_sd_dg240.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_sd_dg240.R;
import com.example.gtj_sd_dg240.StaticConstant;
import com.example.gtj_sd_dg240.utils.OtherUtils;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 登录弹出框
 */
@SuppressLint("SetTextI18n")
public class DialogLogin extends Dialog {
    private AppCompatActivity context;
    private TextView tv01;
    private EditText et_password;
    private onDialogListener onDialogListener;

    public interface onDialogListener {
        void onClick(boolean flag, int type);
    }

    public DialogLogin(AppCompatActivity context) {
        super(context, R.style.MyDialog);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_login);
        initView();
        setLable();
    }

    private void setLable() {
        tv01.setText("密码登录");
        et_password.setHint("请输入密码");
    }

    private void initView() {
        tv01 = findViewById(R.id.tv_01);
        et_password = findViewById(R.id.et_password);
        et_password.requestFocus();
        Button btnLeft = findViewById(R.id.btn_dialog_left);
        btnLeft.setOnClickListener(v -> dismiss());
        Button btnRight = findViewById(R.id.btn_dialog_right);
        btnRight.setOnClickListener(view -> {
            String strPassword = et_password.getText().toString().trim();
            et_password.setText("");
            if (TextUtils.equals(StaticConstant.pwd, strPassword)) {
                OtherUtils.hideKeyboard(context);
                dismiss();
                onDialogListener.onClick(true, 1);
            } else if (TextUtils.equals(StaticConstant.pwd1, strPassword)) {
                OtherUtils.hideKeyboard(context);
                dismiss();
                onDialogListener.onClick(true, 2);
            } else {
                ToastUtils.showShort("密码错误");
            }
        });
    }

    public DialogLogin setDialogListener(onDialogListener onDialogListener) {
        this.onDialogListener = onDialogListener;
        return this;
    }
}
