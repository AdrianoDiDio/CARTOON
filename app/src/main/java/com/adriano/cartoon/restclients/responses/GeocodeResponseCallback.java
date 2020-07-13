package com.adriano.cartoon.restclients.responses;

import android.location.Address;

import java.util.ArrayList;

public interface GeocodeResponseCallback {
    void onGeocodeResponseResult(int requestID, ArrayList<Address> addressArrayList, GeocodeResponseStatus geoCodeResponseStatus);
}
