package com.adriano.cartoon.pojo.googlemaps.gmapsautocomplete;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GMapsAutoCompleteStructuredFormattingPOJO {
    @SerializedName("main_text")
    @Expose
    private String mainText;
    @SerializedName("secondary_text")
    @Expose
    private String secondaryText;

    public String getMainText() {
        return mainText;
    }

    public void setMainText(String mainText) {
        this.mainText = mainText;
    }

    public String getSecondaryText() {
        return secondaryText;
    }

    public void setSecondaryText(String secondaryText) {
        this.secondaryText = secondaryText;
    }
}
