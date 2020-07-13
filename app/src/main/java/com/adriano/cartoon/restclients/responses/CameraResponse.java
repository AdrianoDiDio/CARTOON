package com.adriano.cartoon.restclients.responses;

import com.adriano.cartoon.Camera;

import java.util.ArrayList;

public class CameraResponse {
    private CameraResponseStatus cameraResponseStatus;
    private ArrayList<Camera> cameraArrayList;

    public ArrayList<Camera> getCameraArrayList() {
        return cameraArrayList;
    }
    public CameraResponseStatus getCameraResponseStatus() {
        return cameraResponseStatus;
    }
    public void setCameraResponseStatus(CameraResponseStatus cameraResponseStatus) {
        this.cameraResponseStatus = cameraResponseStatus;
    }
    public void addCamera(Camera camera) {
        cameraArrayList.add(camera);
    }
    public CameraResponse() {
        cameraArrayList = new ArrayList<>();
        cameraResponseStatus = CameraResponseStatus.CAMERA_RESPONSE_OK;
    }
}
