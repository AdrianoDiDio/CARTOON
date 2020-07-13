package com.adriano.cartoon.pojo.openrouteservice.orsautocomplete;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ORSAutoCompletePOJO {

    @SerializedName("features")
    @Expose
    private List<ORSAutoCompleteFeaturePOJO> features = null;

    public List<ORSAutoCompleteFeaturePOJO> getFeatures() {
        return features;
    }

    public void setFeatures(List<ORSAutoCompleteFeaturePOJO> features) {
        this.features = features;
    }
}
