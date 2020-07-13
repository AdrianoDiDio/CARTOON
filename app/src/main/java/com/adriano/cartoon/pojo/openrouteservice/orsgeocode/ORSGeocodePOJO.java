package com.adriano.cartoon.pojo.openrouteservice.orsgeocode;

import java.util.List;

import com.adriano.cartoon.pojo.openrouteservice.common.ORSFeaturePOJO;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ORSGeocodePOJO {

    @SerializedName("features")
    @Expose
    private List<ORSFeaturePOJO> features = null;

    public List<ORSFeaturePOJO> getFeatures() {
        return features;
    }

    public void setFeatures(List<ORSFeaturePOJO> features) {
        this.features = features;
    }

}