package com.adriano.cartoon.pojo.openrouteservice.orsautocomplete;

import com.adriano.cartoon.pojo.openrouteservice.common.ORSGeometryPOJO;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ORSAutoCompleteFeaturePOJO {
    @SerializedName("geometry")
    @Expose
    private ORSGeometryPOJO geometry;
    @SerializedName("properties")
    @Expose
    private ORSAutoCompletePropertiesPOJO properties;

    public ORSGeometryPOJO getGeometry() {
        return geometry;
    }

    public void setGeometry(ORSGeometryPOJO geometry) {
        this.geometry = geometry;
    }

    public ORSAutoCompletePropertiesPOJO getProperties() {
        return properties;
    }

    public void setProperties(ORSAutoCompletePropertiesPOJO properties) {
        this.properties = properties;
    }
}
