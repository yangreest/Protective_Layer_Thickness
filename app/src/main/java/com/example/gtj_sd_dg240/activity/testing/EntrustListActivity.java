package com.example.gtj_sd_dg240.activity.testing;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_sd_dg240.R;
import com.example.gtj_sd_dg240.activity.start.BaseActivity;
import com.example.gtj_sd_dg240.adapter.EntrustListAdapter;
import com.example.gtj_sd_dg240.databinding.ActEntrustListBinding;
import com.example.gtj_sd_dg240.model.EntrustBean;
import com.example.gtj_sd_dg240.model.EntrustWithPageBean;
import com.example.gtj_sd_dg240.net.RetrofitPresenter;
import com.example.gtj_sd_dg240.net.RetrofitPresenterResultListener;
import com.example.gtj_sd_dg240.utils.DateUtils;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;

import java.util.List;

/**
 * 委托列表
 */
@SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
public class EntrustListActivity extends BaseActivity<ActEntrustListBinding> {

    private EntrustListAdapter adapter;
    private EntrustBean entrustBean;

    //是否分页加载
    private static final boolean paging = false;
    private int page = 1;
    private int total = 0;

    @Override
    protected ActEntrustListBinding getBinding() {
        return ActEntrustListBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding.navigationBar.setTitleText("委托列表");
        initBatteryReceiver(binding.navigationBar);
        initBluetoothStatus(binding.navigationBar);
        entrustBean = (EntrustBean) getIntent().getSerializableExtra("entrustBean");

        binding.btnOk.setOnClickListener(this::onViewClicked);
        binding.btnReturn.setOnClickListener(this::onViewClicked);

        initEntrustView();
    }

    private void onViewClicked(View view) {
        if (view.getId() == R.id.btn_ok) {
            if (entrustBean == null) {
                ToastUtils.showShort("请选择委托单");
                return;
            }
            setResult(RESULT_OK, new Intent().putExtra("entrustBean", entrustBean));
            finish();
        } else {
            finish();
        }
    }

    private void initEntrustView() {
        binding.rvEntrust.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EntrustListAdapter(this);
        adapter.bindToRecyclerView(binding.rvEntrust);
        adapter.setOnItemChildClickListener((adapter, view, position) -> {
            for (int i = 0; i < adapter.getData().size(); i++) {
                EntrustBean bean = (EntrustBean) adapter.getData().get(i);
                bean.isSelected = false;
            }

            entrustBean = (EntrustBean) adapter.getData().get(position);
            entrustBean.isSelected = true;
            adapter.notifyDataSetChanged();
            //
            refreshTextView();
        });
        if (paging) {
            binding.refreshLayout.setRefreshHeader(new ClassicsHeader(this));
            binding.refreshLayout.setRefreshFooter(new ClassicsFooter(this));
            binding.refreshLayout.setOnRefreshListener(refreshLayout -> {
                page = 1;
                getEntrustListWithPage();
            });
            binding.refreshLayout.setOnLoadMoreListener(refreshLayout -> {
                if (adapter.getItemCount() < total) {
                    ++page;
                    getEntrustListWithPage();
                } else {
                    binding.refreshLayout.finishLoadMore();
                }
            });
            progressiveDialog.show();
            getEntrustListWithPage();
        } else {
            binding.refreshLayout.setEnableRefresh(false);
            binding.refreshLayout.setEnableLoadMore(false);
            progressiveDialog.show();
            getEntrustList();
        }
    }

    private void getEntrustList() {
        RetrofitPresenter.requestEntrustList(new RetrofitPresenterResultListener<>() {
            @Override
            public void onSuccess(List<EntrustBean> result) {
                progressiveDialog.dismiss();
                if (result == null || result.isEmpty()) {
                    ToastUtils.showShort("暂无委托信息");
                    binding.rvEntrust.setVisibility(View.GONE);
                    return;
                }
                binding.rvEntrust.setVisibility(View.VISIBLE);
                if (entrustBean != null) {
                    for (int i = 0; i < result.size(); i++) {
                        if (result.get(i).id.equals(entrustBean.id)) {
                            result.get(i).isSelected = true;
                            entrustBean = result.get(i);
                            refreshTextView();
                        }
                    }
                }
                adapter.setNewData(result);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                if (!isDestroyed() && progressiveDialog.isShowing()) {
                    progressiveDialog.dismiss();
                }
                ToastUtils.showShort(error);
            }
        });
    }

    private void getEntrustListWithPage() {
        RetrofitPresenter.requestEntrustListWithPage(page, 10, new RetrofitPresenterResultListener<>() {
            @Override
            public void onSuccess(EntrustWithPageBean result) {
                if (page == 1) {
                    if (!isDestroyed() && progressiveDialog.isShowing()) {
                        progressiveDialog.dismiss();
                    }
                    binding.refreshLayout.finishRefresh();
                } else {
                    binding.refreshLayout.finishLoadMore();
                }
                total = result.total;
                if (page == 1 && result.rows.isEmpty()) {
                    ToastUtils.showShort("暂无委托信息");
                    binding.rvEntrust.setVisibility(View.GONE);
                    return;
                }
                binding.rvEntrust.setVisibility(View.VISIBLE);

                if (entrustBean != null) {
                    for (int i = 0; i < result.rows.size(); i++) {
                        if (result.rows.get(i).id.equals(entrustBean.id)) {
                            result.rows.get(i).isSelected = true;
                            entrustBean = result.rows.get(i);
                            refreshTextView();
                        }
                    }
                }
                if (page == 1) {
                    adapter.setNewData(result.rows);
                } else {
                    adapter.addData(result.rows);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                binding.refreshLayout.finishRefresh();
                binding.refreshLayout.finishLoadMore();
                if (!isDestroyed() && progressiveDialog.isShowing()) {
                    progressiveDialog.dismiss();
                }
                ToastUtils.showShort(error);
            }
        });
    }

    private void refreshTextView() {
        if (entrustBean == null) {
            binding.tvDetail.setText("");
            return;
        }
        StringBuilder sbd = new StringBuilder();
        sbd.append("委托单号：").append(entrustBean.entrust_code).append("\n委托状态：").append(entrustBean.status == 0 ? "配置中" : entrustBean.status == 1 ? "试验中" : entrustBean.status == 2 ? "已完成" : "已超期").append("\n委托单位：").append(entrustBean.clientUnitName).append("\n项目名称：").append(entrustBean.project_name).append("\n创建时间：").append(entrustBean.create_time).append("\n检测地址：").append(entrustBean.detection_address).append("\n见  证  人：").append(entrustBean.eyewitness).append("\n计划开始时间：").append(DateUtils.isoDateToNormal(entrustBean.plan_start_date)).append("\n计划结束时间：").append(DateUtils.isoDateToNormal(entrustBean.plan_end_date));
        binding.tvDetail.setText(sbd);
    }
}
