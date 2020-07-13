package com.adriano.cartoon.pojo.openrouteservice.orsdirections;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


public class ORSDirectionsPOJO {

    @SerializedName("features")
    @Expose
    private List<ORSDirectionsFeaturePOJO> features = null;

    public List<ORSDirectionsFeaturePOJO> getFeatures() {
        return features;
    }

    public void setFeatures(List<ORSDirectionsFeaturePOJO> features) {
        this.features = features;
    }

}