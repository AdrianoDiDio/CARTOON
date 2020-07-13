package com.adriano.cartoon.restclients;

import com.adriano.cartoon.Camera;
import com.adriano.cartoon.Constants;
import com.adriano.cartoon.pojo.camera.CameraLocalListPOJO;
import com.adriano.cartoon.pojo.camera.CameraLocalPOJO;
import com.adriano.cartoon.restclients.responses.CameraResponseStatus;
import com.adriano.cartoon.restclients.responses.CameraResponse;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

public class RestCameraLocalClient extends RestCameraClient {
    private final static String BASE_URL = Constants.LOCAL_SERVER_ADDRESS;
    private LocalCameraAPI localCameraAPI;

    private CameraResponseStatus getCameraResponseStatusFromErrorCode(int errorCode) {
        switch ( errorCode ) {
            case 400:
                return CameraResponseStatus.CAMERA_RESPONSE_OK;
            case 500:
                return CameraResponseStatus.CAMERA_RESPONSE_SERVICE_NOT_AVAILABLE;
            case 404:
            default:
                return CameraResponseStatus.CAMERA_RESPONSE_ERROR;
        }
    }

    @Override
    public CameraResponse getCameraList() {
        CameraResponse cameraResponse = new CameraResponse();
        Call<CameraLocalListPOJO> call = localCameraAPI.getCameraList();
        try {
            Response<CameraLocalListPOJO> response = call.execute();
            if( response.isSuccessful() ) {
                CameraLocalListPOJO cameraLocalListPOJO = response.body();
                for( CameraLocalPOJO cameraLocalPOJO : cameraLocalListPOJO.getCameralist() ) {
                    cameraResponse.addCamera(new Camera(cameraLocalPOJO.getName(),
                            new LatLng(cameraLocalPOJO.getLatitude(),cameraLocalPOJO.getLongitude()),
                            cameraLocalPOJO.getIPv4(),cameraLocalPOJO.getPort(),cameraLocalPOJO.getStreamName()));
                }
                cameraResponse.setCameraResponseStatus(CameraResponseStatus.CAMERA_RESPONSE_OK);
            } else {
                cameraResponse.setCameraResponseStatus(getCameraResponseStatusFromErrorCode(response.code()));
            }
        } catch (IOException e) {
            cameraResponse.setCameraResponseStatus(CameraResponseStatus.CAMERA_RESPONSE_ERROR);
            e.printStackTrace();
        }
        return cameraResponse;
    }

    @Override
    public CameraResponse getNearbyCameraList(LatLng center, double radius) {
        CameraResponse cameraResponse = new CameraResponse();
        Call<CameraLocalListPOJO> call = localCameraAPI.getNearbyCameraList(
                String.valueOf(center.latitude),
                String.valueOf(center.longitude),
                String.valueOf(radius)
        );
        try {
            Response<CameraLocalListPOJO> response = call.execute();
            Timber.d("Camera: " + call.request().url());
            if( response.isSuccessful() ) {
                CameraLocalListPOJO cameraLocalListPOJO = response.body();
                for( CameraLocalPOJO cameraLocalPOJO : cameraLocalListPOJO.getCameralist() ) {
                    cameraResponse.addCamera(new Camera(cameraLocalPOJO.getName(),
                            new LatLng(cameraLocalPOJO.getLatitude(),cameraLocalPOJO.getLongitude()),
                            cameraLocalPOJO.getIPv4(),cameraLocalPOJO.getPort(),cameraLocalPOJO.getStreamName()));
                }
                cameraResponse.setCameraResponseStatus(CameraResponseStatus.CAMERA_RESPONSE_OK);
            } else {
                cameraResponse.setCameraResponseStatus(getCameraResponseStatusFromErrorCode(response.code()));
            }
        } catch (IOException e) {
            cameraResponse.setCameraResponseStatus(CameraResponseStatus.CAMERA_RESPONSE_ERROR);
        }
        return cameraResponse;
    }

    public RestCameraLocalClient() {
        super(BASE_URL);
        localCameraAPI = retrofit.create(LocalCameraAPI.class);
    }
}
