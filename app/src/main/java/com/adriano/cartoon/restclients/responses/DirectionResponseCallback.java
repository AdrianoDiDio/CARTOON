package com.adriano.cartoon.restclients.responses;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public interface DirectionResponseCallback {
    void onDirectionResponseResult(int requestID, ArrayList<LatLng> route, DirectionsResponseStatus responseStatus);
}
