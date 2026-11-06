package com.example.gtj_sd_dg240.model;

import android.text.TextUtils;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.io.Serializable;

import androidx.annotation.NonNull;

@Entity
public class ProjectBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id(autoincrement = true)
    public Long id;  //1（序号id）
    public String name;  //工程名称
    public String remark;  //备注
    public String address;  //工程地址
    @Transient
    public boolean isSelected_open;
    @Transient
    public boolean isSelected_treat;

    public ProjectBean(String name) {
        this.name = name;
    }

    @Generated(hash = 762399958)
    public ProjectBean(Long id, String name, String remark, String address) {
        this.id = id;
        this.name = name;
        this.remark = remark;
        this.address = address;
    }

    @Generated(hash = 882656566)
    public ProjectBean() {
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProjectBean) {
            ProjectBean p = (ProjectBean) obj;
            return TextUtils.equals(name, p.name);
        }
        return false;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemark() {
        return this.remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
