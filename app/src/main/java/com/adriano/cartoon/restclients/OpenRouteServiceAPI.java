package com.adriano.cartoon.restclients;


import com.adriano.cartoon.pojo.openrouteservice.orsautocomplete.ORSAutoCompletePOJO;
import com.adriano.cartoon.pojo.openrouteservice.orsdirections.ORSDirectionsPOJO;
import com.adriano.cartoon.pojo.openrouteservice.orsgeocode.ORSGeocodePOJO;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface OpenRouteServiceAPI {
    @GET("v2/directions/{travel_mode}")
    Call<ORSDirectionsPOJO> getDirectionRoute(@Path("travel_mode") String travelMode,
                                              @Query("api_key") String apiKey,
                                              @Query("start") String startPosition,
                                              @Query("end") String endPosition);
    @GET("geocode/search")
    Call<ORSGeocodePOJO> geocode(@Query("api_key") String apiKey,
                                 @Query("text") String position);
    @GET("geocode/autocomplete")
    Call<ORSAutoCompletePOJO> autocomplete(@Query("lang") String language,
                                           @Query("size") String resultSize,
                                           @Query("api_key") String apiKey,
                                           @Query("text") String text);
}
