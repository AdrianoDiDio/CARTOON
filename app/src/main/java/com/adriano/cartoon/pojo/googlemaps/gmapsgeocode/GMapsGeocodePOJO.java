package com.adriano.cartoon.pojo.googlemaps.gmapsgeocode;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GMapsGeocodePOJO {
    @SerializedName("results")
    @Expose
    private List<GMapsGeocodeResultPOJO> results = null;
    @SerializedName("status")
    @Expose
    private String status;

    public List<GMapsGeocodeResultPOJO> getResults() {
        return results;
    }

    public void setResults(List<GMapsGeocodeResultPOJO> results) {
        this.results = results;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
