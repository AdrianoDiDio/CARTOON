package com.adriano.cartoon.restclients;

import com.adriano.cartoon.restclients.responses.CameraResponse;
import com.google.android.gms.maps.model.LatLng;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public abstract class RestCameraClient {
    Retrofit retrofit;

    public abstract CameraResponse getCameraList();
    public abstract CameraResponse getNearbyCameraList(LatLng center, double radius);

    public RestCameraClient(String baseURL) {
        retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(baseURL)
                .build();
    }
}
