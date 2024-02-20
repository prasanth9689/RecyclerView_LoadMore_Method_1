package com.skyblue.recyclerviewloadmoremethod1.retrofit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface APIInterface {
    @FormUrlEncoded
    @POST("/video_list.php")
    Call<ResponseBody> getCommonPosts(@Field("user_id") String user_id);
}
