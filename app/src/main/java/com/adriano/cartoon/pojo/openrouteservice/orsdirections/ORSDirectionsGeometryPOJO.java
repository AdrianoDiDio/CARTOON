package com.adriano.cartoon.pojo.openrouteservice.orsdirections;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ORSDirectionsGeometryPOJO {

    @SerializedName("coordinates")
    @Expose
    private List<List<Double>> coordinates = null;

    public List<List<Double>> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<List<Double>> coordinates) {
        this.coordinates = coordinates;
    }

}
