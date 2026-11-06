package com.example.gtj_sd_dg240.model;

import java.io.Serializable;

public class EntrustBean implements Serializable {
    public String id;//委托单id
    public String entrust_code;//委托单号
    public String project_name;//项目名称
    public String custom_id;//委托单位id
    public String development_unit_id;//建设单位id
    public String design_unit_id; //设计单位id
    public String construction_unit_id;//施工单位id
    public String supervisory_unit_id;//监理单位id
    public String eyewitness;//见证人
    public String temperature;//温度
    public String humidity;//湿度
    public String plan_start_date;//计划开始时间
    public String plan_end_date;//计划结束时间
    public String detection_address;//检测地址
    public int status;//委托状态（0：配置中 1：试验中 2：已完成 3：已超期 默认0）
    public int sorted;
    public int deleted;
    public String create_time;//创建时间
    public String create_by;
    public String last_update_time;
    public String last_update_by;
    public String tenant_id;
    public String clientUnitName;//委托单位名称

    public boolean isSelected;
}
