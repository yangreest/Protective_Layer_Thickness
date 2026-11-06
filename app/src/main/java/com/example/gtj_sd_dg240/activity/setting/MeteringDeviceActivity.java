package com.example.gtj_sd_dg240.activity.setting;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import com.blankj.utilcode.util.SPUtils;
import com.example.gtj_sd_dg240.R;
import com.example.gtj_sd_dg240.StaticConstant;
import com.example.gtj_sd_dg240.activity.start.BaseActivity;
import com.example.gtj_sd_dg240.adapter.MeteringDeviceAdapter;
import com.example.gtj_sd_dg240.databinding.ActMeteringDeviceBinding;
import com.example.gtj_sd_dg240.model.MeteringBean;
import com.example.gtj_sd_dg240.stacon.StaCon_10_4;
import com.example.gtj_sd_dg240.stacon.StaCon_14_4;
import com.example.gtj_sd_dg240.utils.BytesConversion;
import com.example.gtj_sd_dg240.utils.OtherUtils;
import com.example.gtj_sd_dg240.utils.SpinnerUtils;
import com.example.gtj_sd_dg240.utils.StringUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

@SuppressLint("SetTextI18n")
public class MeteringDeviceActivity extends BaseActivity<ActMeteringDeviceBinding> {
    private List<Integer> standardArray;
    private String strDiameter;
    private MeteringDeviceAdapter listAdapter;

    @Override
    protected com.example.gtj_sd_dg240.databinding.ActMeteringDeviceBinding getBinding() {
        return ActMeteringDeviceBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        binding.navigationBar.setTitleText("计量");
        initBatteryReceiver(binding.navigationBar);
        initBluetoothStatus(binding.navigationBar);
        registerBluetoothDataCallback();
        //
        SpinnerUtils.attachDataSource(binding.spDiameter, StaticConstant.rebarDiameterList1, (spinner, text, position) -> {
            SPUtils.getInstance().put("spDiameter", position);
            setStandardList();
        });
        binding.spDiameter.setSelection(SPUtils.getInstance().getInt("spDiameter", 0));  //纵筋直径
        binding.btnDelete.setOnClickListener(this::onViewClicked);
        binding.btnCancel.setOnClickListener(this::onViewClicked);
        //
        initRecyclerView();
        setStandardList();
    }

    private void setStandardList() {
        strDiameter = StaticConstant.rebarDiameterList1.get(SPUtils.getInstance().getInt("spDiameter", 0));
        standardArray = StaCon_10_4.meteringList_10;
        if (TextUtils.equals("14", strDiameter)) {
            standardArray = StaCon_14_4.meteringList_14;

        }
    }

    public void onViewClicked(View view) {
        OtherUtils.hideKeyboard(this);
        if (view.getId() == R.id.btn_delete) {
            listAdapter.getData().clear();
            listAdapter.notifyDataSetChanged();
        } else if (view.getId() == R.id.btn_cancel) {
            finish();
        }
    }

    private void initRecyclerView() {
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 5));
        listAdapter = new MeteringDeviceAdapter();
        listAdapter.bindToRecyclerView(binding.recyclerView);
        //
        String strMeteringDeviceList = SPUtils.getInstance().getString("strMeteringDeviceList");
        if (!TextUtils.isEmpty(strMeteringDeviceList)) {
            List<MeteringBean> list = new Gson().fromJson(strMeteringDeviceList, new TypeToken<List<MeteringBean>>() {
            }.getType());
            listAdapter.setNewData(list);
        }
    }

    @Override
    protected void onBluetoothDataReceived(byte[] data) {
        if (isShow) {
            String string = StringUtils.ByteArraytoHex(data).trim();
            String[] array = string.split(" ");
            if ("F1".equals(array[0]) && array.length == 8) {
                notifyDetectionData(array);
            }
        }
    }

    /**
     * 单点检测
     */
    private void notifyDetectionData(String[] array) {
        //F1	位置高	位置低	左信号高	左信号低	右信号高	右信号低	FF
        int signalLeft = BytesConversion.hex2decimal(array[3] + array[4]);  //左信号
        int signalRight = BytesConversion.hex2decimal(array[5] + array[6]);  //右信号
        int signalValue = (signalLeft + signalRight) / 2;
        treatSignalValue(signalValue);
    }

    private void treatSignalValue(int signalValue) {
        double thickness = 0;
        for (int i = 0; i < standardArray.size(); i++) {
            if (signalValue >= standardArray.get(i)) {
                int max = standardArray.get(i - 1);
                int min = standardArray.get(i);
                thickness = StringUtils.getRounding(StringUtils.getInterpolationValue(max, i, min, i + 1, signalValue), 1);
                break;
            }
        }
        //
        binding.tvValue.setText("钢筋保护层厚度：" + thickness + "mm，" + signalValue);
        //
        listAdapter.getData().add(new MeteringBean(strDiameter, signalValue, thickness));
        listAdapter.notifyItemInserted(listAdapter.getData().size() - 1);
        binding.recyclerView.scrollToPosition(listAdapter.getData().size() - 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 保存数据
        if (listAdapter != null && !listAdapter.getData().isEmpty()) {
            SPUtils.getInstance().put("strMeteringDeviceList", new Gson().toJson(listAdapter.getData()));
        } else {
            SPUtils.getInstance().put("strMeteringDeviceList", "");
        }
    }
}
