package com.example.gtj_sd_dg240.view;

import android.content.Context;
import android.text.TextUtils;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.example.gtj_sd_dg240.R;

import java.util.List;

public class OptionsPickerViewUtils {
    public interface OnSelectListener1 {
        void onSelect(int position);
    }

    public interface OnSelectListener2 {
        void onSelect(int position);

        void onEmpty();
    }

    @SuppressWarnings("unchecked")
    public static void showSingleColumnPickerView(Context context, String title, String label, final List<String> stringList,
                                                  String currentString, final OnSelectListener1 onSelectListener) {
        int index = optionsItem1Index(currentString, stringList);
        OptionsPickerView optionsPickerView = new OptionsPickerBuilder(context, (options1, options2, options3, v) -> onSelectListener.onSelect(options1)).setTitleText(title)
                .setTitleColor(context.getResources().getColor(R.color.color999))
                .setTitleSize(16)
                .setTitleBgColor(context.getResources().getColor(R.color.colorFFF))
                .setSubmitText("确定")
                .setCancelText("取消")
                .setSubmitColor(context.getResources().getColor(R.color.color333))
                .setCancelColor(context.getResources().getColor(R.color.color999))
                .setDividerColor(context.getResources().getColor(R.color.color333))
                .setLineSpacingMultiplier(2)
                .setTextColorCenter(context.getResources().getColor(R.color.color000)) //设置选中项文字颜色
                .setContentTextSize(18)
                .setLabels(label, null, null)
                .isDialog(false)
                .build();
        optionsPickerView.setPicker(stringList);
        optionsPickerView.setSelectOptions(index);
        optionsPickerView.findViewById(com.bigkoo.pickerview.R.id.btnCancel).setOnClickListener(v -> {
            optionsPickerView.dismiss();
            //可以处理清空操作
        });
        optionsPickerView.show();
    }

    public static void showSingleColumnPickerView(Context context, String title, String label, final List<String> stringList,
                                                  String currentString, final OnSelectListener2 onSelectListener) {
        int index = optionsItem1Index(currentString, stringList);
        final OptionsPickerView optionsPickerView = new OptionsPickerBuilder(context, (options1, options2, options3, v) -> onSelectListener.onSelect(options1)).setTitleText(title)
                .setTitleColor(context.getResources().getColor(R.color.color999))
                .setTitleSize(16)
                .setTitleBgColor(context.getResources().getColor(R.color.colorFFF))
                .setSubmitText("确定")
                .setCancelText("清空")
                .setSubmitColor(context.getResources().getColor(R.color.color333))
                .setCancelColor(context.getResources().getColor(R.color.color999))
                .setDividerColor(context.getResources().getColor(R.color.color333))
                .setLineSpacingMultiplier(2)
                .setTextColorCenter(context.getResources().getColor(R.color.color000)) //设置选中项文字颜色
                .setContentTextSize(18)
                .setLabels(label, null, null)
                .isDialog(false)
                .build();
        optionsPickerView.setPicker(stringList);
        optionsPickerView.setSelectOptions(index);
        optionsPickerView.findViewById(com.bigkoo.pickerview.R.id.btnCancel).setOnClickListener(v -> {
            optionsPickerView.dismiss();
            //可以处理清空操作
            onSelectListener.onEmpty();
        });
        optionsPickerView.show();
    }

    private static int optionsItem1Index(String item, List<String> stringList) {
        int index = 0;
        if (!TextUtils.isEmpty(item)) {
            for (int i = 0; i < stringList.size(); i++) {
                if (TextUtils.equals(item, stringList.get(i))) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }
}
