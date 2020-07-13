package com.adriano.cartoon.restclients.responses;

import com.adriano.cartoon.adapters.Place;

import java.util.ArrayList;

public interface AutoCompleteResponseCallback {
    //STUB!
    void onAutoCompleteResponseResult(int requestID, ArrayList<Place> placeArrayList, AutoCompleteResponseStatus autoCompleteResponseStatus);
}
