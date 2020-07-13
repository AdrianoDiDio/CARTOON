package com.adriano.cartoon.pojo.googlemaps.gmapsgeocode;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GMapsGeocodeResultPOJO {
    @SerializedName("geometry")
    @Expose
    private GMapsGeocodeGeometryPOJO geometry;

    public GMapsGeocodeGeometryPOJO getGeometry() {
        return geometry;
    }

    public void setGeometry(GMapsGeocodeGeometryPOJO geometry) {
        this.geometry = geometry;
    }
}
