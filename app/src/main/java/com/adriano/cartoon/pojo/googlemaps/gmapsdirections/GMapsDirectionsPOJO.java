package com.adriano.cartoon.pojo.googlemaps.gmapsdirections;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GMapsDirectionsPOJO {

    @SerializedName("routes")
    @Expose
    private List<GMapsDirectionsRoutePOJO> routes = null;
    @SerializedName("status")
    @Expose
    private String status;

    public List<GMapsDirectionsRoutePOJO> getRoutes() {
        return routes;
    }

    public void setRoutes(List<GMapsDirectionsRoutePOJO> routes) {
        this.routes = routes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}