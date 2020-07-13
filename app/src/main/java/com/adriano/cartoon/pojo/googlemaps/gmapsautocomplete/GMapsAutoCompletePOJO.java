package com.adriano.cartoon.pojo.googlemaps.gmapsautocomplete;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GMapsAutoCompletePOJO {

    @SerializedName("predictions")
    @Expose
    private List<GMapsAutoCompletePredictionPOJO> predictions = null;
    @SerializedName("status")
    @Expose
    private String status;

    public List<GMapsAutoCompletePredictionPOJO> getPredictions() {
        return predictions;
    }

    public void setPredictions(List<GMapsAutoCompletePredictionPOJO> predictions) {
        this.predictions = predictions;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
