package com.adriano.cartoon.pojo.googlemaps.gmapsgeocode;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GMapsGeocodeGeometryPOJO {

    @SerializedName("location")
    @Expose
    private GMapsGeocodeLocationPOJO location;

    public GMapsGeocodeLocationPOJO getLocation() {
        return location;
    }

    public void setLocation(GMapsGeocodeLocationPOJO location) {
        this.location = location;
    }
}
