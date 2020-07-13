package com.adriano.cartoon.services;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;

import androidx.annotation.Nullable;

import com.adriano.cartoon.Constants;
import com.adriano.cartoon.restclients.RestCameraClient;
import com.adriano.cartoon.restclients.RestCameraLocalClient;
import com.adriano.cartoon.restclients.responses.CameraResponse;
import com.adriano.cartoon.restclients.responses.CameraResponseStatus;
import com.google.android.gms.maps.model.LatLng;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import timber.log.Timber;

public class NearByCameraService extends IntentService {
    public static final String SEARCH_RADIUS_KEY = "SearchRadius";
    public static final String SEARCH_ORIGIN_LATITUDE_KEY = "SearchOriginLatitude";
    public static final String SEARCH_ORIGIN_LONGITUDE_KEY = "SearchOriginLongitude";
    public static final String RECEIVER_BUNDLE_KEY = "CameraReceiver";
    public static final String NEAR_BY_CAMERA_SERVICE_RESULT_KEY = "NearByCameraServiceResult";
    public static final String NEAR_BY_CAMERA_SERVICE_RESULT_STATUS_KEY = "NearByCameraServiceResultStatus";
    public static final int NEAR_BY_CAMERA_SERVICE_RESULT_CODE = 66;
    private RestCameraClient restCameraClient;

    public NearByCameraService() {
        super("NearByCameraService");
    }

    private String getURL(Intent intent) {
        Uri.Builder builder = new Uri.Builder();

        double latitude = (double) intent.getExtras().get(SEARCH_ORIGIN_LATITUDE_KEY);
        double longitude = (double) intent.getExtras().get(SEARCH_ORIGIN_LONGITUDE_KEY);
        int radius = (int) intent.getExtras().get(SEARCH_RADIUS_KEY);
        builder.scheme("http")
                .encodedAuthority(Constants.LOCAL_SERVER_ADDRESS)
                .appendPath("getnearbycameralist")
                .appendQueryParameter("centerlatitude",String.valueOf(latitude))
                .appendQueryParameter("centerlongitude",String.valueOf(longitude))
                .appendQueryParameter("radius",String.valueOf(radius));
//                .appendPath("MapsApiTest")
//                .appendPath("ServerBackend")
//                .appendPath("CameraUtilsV2.php");
        String result;
        result = builder.build().toString();
        return result;
    }

    private String encodePostParam(String key, String value) throws UnsupportedEncodingException {
        String param;
        param = URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8") + "&";
        return param;
    }

    private byte[] getPostDataFromIntent(Intent intent) {
        StringBuilder postData = new StringBuilder();
        LatLng searchCenter;
        int radius;

        double latitude = (double) intent.getExtras().get(SEARCH_ORIGIN_LATITUDE_KEY);
        double longitude = (double) intent.getExtras().get(SEARCH_ORIGIN_LONGITUDE_KEY);
        searchCenter = new LatLng(latitude, longitude);
        radius = (int) intent.getExtras().get(SEARCH_RADIUS_KEY);
        try {
            postData.append(encodePostParam("GetAllCamerasWithinRadius", "1"));
            postData.append(encodePostParam("CenterLongitude", String.valueOf(searchCenter.longitude)));
            postData.append(encodePostParam("CenterLatitude", String.valueOf(searchCenter.latitude)));
            postData.append(encodePostParam("Radius", String.valueOf(radius)));
            Timber.d("posting as: " + postData);
            return postData.toString().getBytes(StandardCharsets.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        HttpURLConnection urlConnection;
        Bundle outData;
        ResultReceiver resultReceiver;
        resultReceiver = intent.getParcelableExtra(RECEIVER_BUNDLE_KEY);

        LatLng searchCenter;
        int radius;

        double latitude = (double) intent.getExtras().get(SEARCH_ORIGIN_LATITUDE_KEY);
        double longitude = (double) intent.getExtras().get(SEARCH_ORIGIN_LONGITUDE_KEY);
        searchCenter = new LatLng(latitude, longitude);
        radius = (int) intent.getExtras().get(SEARCH_RADIUS_KEY);

        restCameraClient = new RestCameraLocalClient();
        CameraResponse cameraResponse = restCameraClient.getNearbyCameraList(searchCenter,radius);
        outData = new Bundle();
        if( cameraResponse.getCameraResponseStatus() != CameraResponseStatus.CAMERA_RESPONSE_OK ) {
            outData.putInt(NEAR_BY_CAMERA_SERVICE_RESULT_STATUS_KEY, -1);
        } else {
            outData.putInt(NEAR_BY_CAMERA_SERVICE_RESULT_STATUS_KEY, 1);
            outData.putParcelableArrayList(NEAR_BY_CAMERA_SERVICE_RESULT_KEY,cameraResponse.getCameraArrayList());
        }

        resultReceiver.send(NEAR_BY_CAMERA_SERVICE_RESULT_CODE, outData);
    }

}
