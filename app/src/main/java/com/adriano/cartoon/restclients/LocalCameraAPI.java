package com.adriano.cartoon.restclients;

import com.adriano.cartoon.pojo.camera.CameraLocalListPOJO;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface LocalCameraAPI {
    @GET("getcameralist")
    Call<CameraLocalListPOJO> getCameraList();
    @GET("getnearbycameralist")
    Call<CameraLocalListPOJO> getNearbyCameraList(@Query("centerlatitude") String centerLatitude,
                                                  @Query("centerlongitude") String centerLongitude,
                                                  @Query("radius") String radius);

}
