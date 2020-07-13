package com.adriano.cartoon.pojo.googlemaps.gmapsdirections;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GMapsDirectionsOverviewPolylinePOJO {
    @SerializedName("points")
    @Expose
    private String points;

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }
}
