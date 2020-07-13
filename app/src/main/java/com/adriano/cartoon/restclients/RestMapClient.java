package com.adriano.cartoon.restclients;


import com.adriano.cartoon.TravelMode;
import com.adriano.cartoon.adapters.Place;
import com.adriano.cartoon.restclients.responses.AutoCompleteResponseCallback;
import com.adriano.cartoon.restclients.responses.AutoCompleteResponseStatus;
import com.adriano.cartoon.restclients.responses.DirectionResponseCallback;
import com.adriano.cartoon.restclients.responses.DirectionsResponseStatus;
import com.adriano.cartoon.restclients.responses.GeocodeResponseCallback;
import com.adriano.cartoon.restclients.responses.GeocodeResponseStatus;
import com.google.android.gms.maps.model.LatLng;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.ArrayList;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public abstract class RestMapClient<T> {
    protected Retrofit retrofit;

    protected abstract DirectionsResponseStatus getDirectionsStatus(T responseCode);
    protected abstract GeocodeResponseStatus getGeocodeStatus(T responseCode);
    protected abstract AutoCompleteResponseStatus getAutoCompleteStatus(T responseCode);

    public abstract void getRoute(TravelMode travelMode, LatLng startPosition, LatLng endPosition,
                                  final int requestID, final DirectionResponseCallback directionResponseCallback);

    public abstract void geocode(String position, final int requestID, final GeocodeResponseCallback geocodeResponseCallback);
    public abstract void autocomplete(String text,final int requestID, final AutoCompleteResponseCallback autoCompleteResponseCallback);
    /*
    *
    * Synchronous version of autocomplete that runs on the current thread and returns the results.
    *
    * */
    public abstract ArrayList<Place> autocomplete(String text);

    public RestMapClient(String baseURL) {
        retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(baseURL)
                .build();
    }
}
