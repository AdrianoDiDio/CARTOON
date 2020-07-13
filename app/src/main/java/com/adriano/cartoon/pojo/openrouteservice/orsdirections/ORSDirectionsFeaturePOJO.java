package com.adriano.cartoon.pojo.openrouteservice.orsdirections;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ORSDirectionsFeaturePOJO {

    @SerializedName("geometry")
    @Expose
    private ORSDirectionsGeometryPOJO geometry;

    public ORSDirectionsGeometryPOJO getGeometry() {
        return geometry;
    }

    public void setGeometry(ORSDirectionsGeometryPOJO orsDirectionsGeometryPOJO) {
        this.geometry = orsDirectionsGeometryPOJO;
    }

}