package com.example.gtj_sd_dg240.net;

import com.example.gtj_sd_dg240.model.EntrustBean;
import com.example.gtj_sd_dg240.model.EntrustWithPageBean;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RetrofitInterfaceServiceUrl {
    //上报数据
    @POST("concreteUtilityPolesDetectionResultReported")
    Call<MessageResult<?>> requestUploadData(@Body RequestBody requestBody);

    //查询委托列表
    @Headers("clientId:1941115683107844096")
    @GET("polesEntrustManage/queryPolesEntrustManage")
    Call<MessageResult<List<EntrustBean>>> requestEntrustList(@Query("status") int status,@Query("sn") String sn);

    //分页查询委托列表
    @Headers("clientId:1941115683107844096")
    @GET("polesEntrustManage/queryPolesEntrustManageWithPage")
    Call<EntrustWithPageBean> requestEntrustListWithPage(@Query("status") int status,@Query("sn") String sn, @Query("pageNum") int pageNum, @Query("pageSize") int pageSize);
}