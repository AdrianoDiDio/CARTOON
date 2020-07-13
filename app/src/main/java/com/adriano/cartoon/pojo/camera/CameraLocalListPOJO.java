package com.adriano.cartoon.pojo.camera;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CameraLocalListPOJO {
    @SerializedName("cameralist")
    @Expose
    private List<CameraLocalPOJO> cameraList = null;

    public List<CameraLocalPOJO> getCameralist() {
        return cameraList;
    }

    public void setCameralist(List<CameraLocalPOJO> cameralist) {
        this.cameraList = cameralist;
    }
}

