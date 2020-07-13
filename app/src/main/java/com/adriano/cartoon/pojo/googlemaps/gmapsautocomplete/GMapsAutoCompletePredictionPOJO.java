package com.adriano.cartoon.pojo.googlemaps.gmapsautocomplete;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GMapsAutoCompletePredictionPOJO {
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("place_id")
    @Expose
    private String placeId;
    @SerializedName("structured_formatting")
    @Expose
    private GMapsAutoCompleteStructuredFormattingPOJO structuredFormatting;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public GMapsAutoCompleteStructuredFormattingPOJO getStructuredFormatting() {
        return structuredFormatting;
    }

    public void setStructuredFormatting(GMapsAutoCompleteStructuredFormattingPOJO structuredFormatting) {
        this.structuredFormatting = structuredFormatting;
    }

}
