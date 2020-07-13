package com.adriano.cartoon.restclients;

import com.adriano.cartoon.pojo.googlemaps.gmapsautocomplete.GMapsAutoCompletePOJO;
import com.adriano.cartoon.pojo.googlemaps.gmapsdirections.GMapsDirectionsPOJO;
import com.adriano.cartoon.pojo.googlemaps.gmapsgeocode.GMapsGeocodePOJO;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GoogleMapsAPI {
    @GET("directions/json")
    Call<GMapsDirectionsPOJO> getDirectionsPolyline(@Query("mode") String travelMode,
                                                    @Query("key") String apiKey,
                                                    @Query("origin") String startPosition,
                                                    @Query("destination") String endPosition);
    @GET("geocode/json")
    Call<GMapsGeocodePOJO> geocode(@Query("key") String apiKey,
                                   @Query("address") String address);
    @GET("place/autocomplete/json")
    Call<GMapsAutoCompletePOJO> autocomplete(@Query("language") String language,
                                             @Query("key") String apiKey,
                                             @Query("input") String text);
}
