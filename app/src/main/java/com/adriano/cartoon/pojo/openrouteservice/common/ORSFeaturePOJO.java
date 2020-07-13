package com.adriano.cartoon.pojo.openrouteservice.common;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ORSFeaturePOJO {

    @SerializedName("geometry")
    @Expose
    private ORSGeometryPOJO geometry;

    public ORSGeometryPOJO getGeometry() {
        return geometry;
    }

    public void setGeometry(ORSGeometryPOJO orsDirectionGeometryPOJO) {
        this.geometry = orsDirectionGeometryPOJO;
    }

}