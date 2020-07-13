package com.adriano.cartoon.pojo.googlemaps.gmapsdirections;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GMapsDirectionsRoutePOJO {
    @SerializedName("overview_polyline")
    @Expose
    private GMapsDirectionsOverviewPolylinePOJO overviewPolyline;

    public GMapsDirectionsOverviewPolylinePOJO getOverviewPolyline() {
        return overviewPolyline;
    }

    public void setOverviewPolyline(GMapsDirectionsOverviewPolylinePOJO overviewPolyline) {
        this.overviewPolyline = overviewPolyline;
    }
}
