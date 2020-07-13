package com.adriano.cartoon.restclients;

import android.location.Address;


import com.adriano.cartoon.TravelMode;
import com.adriano.cartoon.Utils;
import com.adriano.cartoon.adapters.Place;
import com.adriano.cartoon.pojo.openrouteservice.common.ORSFeaturePOJO;
import com.adriano.cartoon.pojo.openrouteservice.orsautocomplete.ORSAutoCompleteFeaturePOJO;
import com.adriano.cartoon.pojo.openrouteservice.orsautocomplete.ORSAutoCompletePOJO;
import com.adriano.cartoon.pojo.openrouteservice.orsdirections.ORSDirectionsPOJO;
import com.adriano.cartoon.pojo.openrouteservice.orsgeocode.ORSGeocodePOJO;
import com.adriano.cartoon.restclients.responses.AutoCompleteResponseCallback;
import com.adriano.cartoon.restclients.responses.AutoCompleteResponseStatus;
import com.adriano.cartoon.restclients.responses.DirectionResponseCallback;
import com.adriano.cartoon.restclients.responses.DirectionsResponseStatus;
import com.adriano.cartoon.restclients.responses.GeocodeResponseCallback;
import com.adriano.cartoon.restclients.responses.GeocodeResponseStatus;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OpenRouteServiceRestMapClient extends RestMapClient<Integer> {
    private final static String BASE_URL = "https://api.openrouteservice.org/";
    private OpenRouteServiceAPI openRouteServiceAPI;
    private String apiKey;


    @Override
    protected DirectionsResponseStatus getDirectionsStatus(Integer responseCode) {
        switch ( responseCode ) {
            case 400:
                return DirectionsResponseStatus.DIRECTIONS_RESPONSE_INVALID_ARGS;
            case 500:
                return DirectionsResponseStatus.DIRECTIONS_RESPONSE_SERVICE_NOT_AVAILABLE;
            default:
                return DirectionsResponseStatus.DIRECTIONS_RESPONSE_ERROR;

        }
    }

    @Override
    protected GeocodeResponseStatus getGeocodeStatus(Integer responseCode) {
        switch ( responseCode ) {
            case 400:
                return GeocodeResponseStatus.GEOCODER_SERVICE_INVALID_ARGS;
            case 500:
                return GeocodeResponseStatus.GEOCODER_SERVICE_NOT_AVAILABLE;
            default:
                return GeocodeResponseStatus.GEOCODER_SERVICE_ERROR;

        }
    }

    @Override
    protected AutoCompleteResponseStatus getAutoCompleteStatus(Integer responseCode) {
        switch ( responseCode ) {
            case 400:
                return AutoCompleteResponseStatus.AUTOCOMPLETE_SERVICE_INVALID_ARGS;
            case 500:
                return AutoCompleteResponseStatus.AUTOCOMPLETE_SERVICE_NOT_AVAILABLE;
            default:
                return AutoCompleteResponseStatus.AUTOCOMPLETE_SERVICE_ERROR;

        }
    }



    private ArrayList<Place> buildAutoCompleteList(ORSAutoCompletePOJO orsAutoCompletePOJO) {
        ArrayList<Place> result = new ArrayList<>();
        for (ORSAutoCompleteFeaturePOJO featurePOJO : orsAutoCompletePOJO.getFeatures()) {
            LatLng latLng;
            latLng = new LatLng(featurePOJO.getGeometry().getCoordinates().get(1),
                    featurePOJO.getGeometry().getCoordinates().get(0));
            result.add(new Place(
                    featurePOJO.getProperties().getId(),
                    featurePOJO.getProperties().getName(),
                    featurePOJO.getProperties().getLabel(),
                    latLng));
        }
        return result;
    }
    private ArrayList<Address> buildGeocodeAddressList(ORSGeocodePOJO orsGeocodePOJO) {
        ArrayList<Address> result = new ArrayList<>();
        for (ORSFeaturePOJO featurePOJO : orsGeocodePOJO.getFeatures()) {
            Address address;
            address = new Address(Locale.getDefault());
            address.setLatitude(featurePOJO.getGeometry().getCoordinates().get(1));
            address.setLongitude(featurePOJO.getGeometry().getCoordinates().get(0));
            result.add(address);
        }
        return result;
    }

    private ArrayList<LatLng> buildRoute(ORSDirectionsPOJO orsDirectionsPOJO) {
        ArrayList<LatLng> result = new ArrayList<>();
        List<List<Double>> latLngList;
        latLngList = orsDirectionsPOJO.getFeatures().get(0).getGeometry().getCoordinates();
        for (List<Double> latLng : latLngList ) {
            result.add(new LatLng(latLng.get(1),latLng.get(0)));
        }
        return result;
    }

    private String getRequestPathFromTravelMode(TravelMode travelMode) {
        switch ( travelMode ) {
            case WALK:
                return "foot-walking";
            case BICYCLE:
                return "cycling-regular";
            case DRIVE:
            default:
                return "driving-car";
        }
    }


    public void getRoute(TravelMode travelMode, LatLng startPosition, LatLng endPosition,
                         final int requestID, final DirectionResponseCallback directionResponseCallback/*,Callback!*/) {
        Call<ORSDirectionsPOJO> call = openRouteServiceAPI.getDirectionRoute(
                getRequestPathFromTravelMode(travelMode),
                apiKey,
                Utils.getLngLat(startPosition),Utils.getLngLat(endPosition));
        call.enqueue(new Callback<ORSDirectionsPOJO>() {
            @Override
            public void onResponse(Call<ORSDirectionsPOJO> call, Response<ORSDirectionsPOJO> response) {
                if( response.isSuccessful() ) {
                    ArrayList<LatLng> result;
                    result = buildRoute(response.body());
                    directionResponseCallback.onDirectionResponseResult(requestID,result, DirectionsResponseStatus.
                            DIRECTIONS_RESPONSE_OK);
                } else {
                    DirectionsResponseStatus responseStatus;
                    responseStatus = getDirectionsStatus(response.code());
                    directionResponseCallback.onDirectionResponseResult(requestID,null, responseStatus);
                }
            }

            @Override
            public void onFailure(Call<ORSDirectionsPOJO> call, Throwable t) {
                directionResponseCallback.onDirectionResponseResult(requestID,null,
                        DirectionsResponseStatus.DIRECTIONS_RESPONSE_SERVICE_NOT_AVAILABLE);
            }
        });
    }

    public void geocode(String position, final int requestID, final GeocodeResponseCallback geocodeResponseCallback) {
        Call<ORSGeocodePOJO> call = openRouteServiceAPI.geocode(
                apiKey,
                position);
        call.enqueue(new Callback<ORSGeocodePOJO>() {
            @Override
            public void onResponse(Call<ORSGeocodePOJO> call, Response<ORSGeocodePOJO> response) {
                GeocodeResponseStatus geocodeResponseStatus = GeocodeResponseStatus.GEOCODER_SERVICE_OK;
                if( response.isSuccessful() ) {
                    ArrayList<Address> addressArrayList;
                    addressArrayList = buildGeocodeAddressList(response.body());
                    if( addressArrayList.isEmpty() ) {
                        geocodeResponseStatus = GeocodeResponseStatus.GEOCODER_SERVICE_NO_RESULTS;
                    }
                    geocodeResponseCallback.onGeocodeResponseResult(requestID,addressArrayList,geocodeResponseStatus);
                } else {
                    geocodeResponseStatus = getGeocodeStatus(response.code());
                    geocodeResponseCallback.onGeocodeResponseResult(requestID,null,
                            geocodeResponseStatus);
                }
            }

            @Override
            public void onFailure(Call<ORSGeocodePOJO> call, Throwable t) {
                t.printStackTrace();
                geocodeResponseCallback.onGeocodeResponseResult(requestID,null,
                        GeocodeResponseStatus.GEOCODER_SERVICE_NOT_AVAILABLE);
            }
        });
    }

    @Override
    public void autocomplete(String text, int requestID, AutoCompleteResponseCallback autoCompleteResponseCallback) {
        Call<ORSAutoCompletePOJO> call = openRouteServiceAPI.autocomplete(Locale.getDefault().getLanguage().toString(),
                "5",
                apiKey,
                text);
        call.enqueue(new Callback<ORSAutoCompletePOJO>() {
            @Override
            public void onResponse(Call<ORSAutoCompletePOJO> call, Response<ORSAutoCompletePOJO> response) {
                AutoCompleteResponseStatus autoCompleteResponseStatus = AutoCompleteResponseStatus.AUTOCOMPLETE_SERVICE_OK;
                if( response.isSuccessful() ) {
                    ArrayList<Place> result;
                    result = buildAutoCompleteList(response.body());
                    autoCompleteResponseCallback.onAutoCompleteResponseResult(requestID,result,autoCompleteResponseStatus);
                } else {
                    autoCompleteResponseStatus = getAutoCompleteStatus(response.code());
                    autoCompleteResponseCallback.onAutoCompleteResponseResult(requestID,null,
                            autoCompleteResponseStatus);
                }
            }

            @Override
            public void onFailure(Call<ORSAutoCompletePOJO> call, Throwable t) {
                t.printStackTrace();
                autoCompleteResponseCallback.onAutoCompleteResponseResult(requestID,null,
                        AutoCompleteResponseStatus.AUTOCOMPLETE_SERVICE_NOT_AVAILABLE);
            }
        });
    }
    @Override
    public ArrayList<Place> autocomplete(String text) {
        ArrayList<Place> result = null;
        Call<ORSAutoCompletePOJO> call = openRouteServiceAPI.autocomplete(Locale.getDefault().getLanguage().toString(),
                "5",
                apiKey,
                text);
        try {
            Response<ORSAutoCompletePOJO> response = call.execute();
            if( response.isSuccessful() ) {
                ORSAutoCompletePOJO orsAutoCompletePOJO = response.body();
                result = buildAutoCompleteList(orsAutoCompletePOJO);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if( result == null ) {
            result = new ArrayList<>();
        }
        return result;
    }

    public OpenRouteServiceRestMapClient(String apiKey) {
        super(BASE_URL);
        this.apiKey = apiKey;
        openRouteServiceAPI = retrofit.create(OpenRouteServiceAPI.class);
    }
}
