package com.adriano.cartoon.pojo.openrouteservice.common;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ORSGeometryPOJO {
    @SerializedName("coordinates")
    @Expose
    private List<Double> coordinates = null;

    public List<Double> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Double> coordinates) {
        this.coordinates = coordinates;
    }
}
