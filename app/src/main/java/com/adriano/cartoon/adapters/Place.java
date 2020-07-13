package com.adriano.cartoon.adapters;

import com.google.android.gms.maps.model.LatLng;

public class Place {
    public String placeID;
    public String address;
    public String fullAddress;
    public LatLng position;

    public Place(String placeId, String address, String fullAddress, LatLng position) {
        this.placeID = placeId;
        this.address = address;
        this.fullAddress = fullAddress;
        this.position = position;
    }
}
