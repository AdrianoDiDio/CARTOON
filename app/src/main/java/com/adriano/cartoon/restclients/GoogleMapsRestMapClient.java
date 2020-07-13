package com.adriano.cartoon.restclients;

import android.location.Address;


import com.adriano.cartoon.TravelMode;
import com.adriano.cartoon.Utils;
import com.adriano.cartoon.adapters.Place;
import com.adriano.cartoon.pojo.googlemaps.gmapsautocomplete.GMapsAutoCompletePOJO;
import com.adriano.cartoon.pojo.googlemaps.gmapsautocomplete.GMapsAutoCompletePredictionPOJO;
import com.adriano.cartoon.pojo.googlemaps.gmapsdirections.GMapsDirectionsPOJO;
import com.adriano.cartoon.pojo.googlemaps.gmapsgeocode.GMapsGeocodePOJO;
import com.adriano.cartoon.pojo.googlemaps.gmapsgeocode.GMapsGeocodeResultPOJO;
import com.adriano.cartoon.restclients.responses.AutoCompleteResponseCallback;
import com.adriano.cartoon.restclients.responses.AutoCompleteResponseStatus;
import com.adriano.cartoon.restclients.responses.DirectionResponseCallback;
import com.adriano.cartoon.restclients.responses.DirectionsResponseStatus;
import com.adriano.cartoon.restclients.responses.GeocodeResponseCallback;
import com.adriano.cartoon.restclients.responses.GeocodeResponseStatus;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class GoogleMapsRestMapClient extends RestMapClient<String> {
    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/";
    private GoogleMapsAPI googleMapsAPI;
    private String apiKey;



    protected String getTravelMode(TravelMode travelMode) {
        switch (travelMode) {
            case WALK:
                return "walking";
            case BICYCLE:
                return "bicycling";
            case DRIVE:
            default:
                return "driving";
        }
    }



    private ArrayList<LatLng> buildRoute(GMapsDirectionsPOJO gMapsDirectionsPOJO) {
        ArrayList<LatLng> result =
                new ArrayList<>(PolyUtil.decode(gMapsDirectionsPOJO.getRoutes().get(0).getOverviewPolyline().getPoints()));
        return result;
    }

    private ArrayList<Address> buildGeocodeAddressList(GMapsGeocodePOJO gMapsGeocodePOJO) {
        ArrayList<Address> result = new ArrayList<>();
        for (GMapsGeocodeResultPOJO resultPOJO : gMapsGeocodePOJO.getResults()) {
            Address address;
            address = new Address(Locale.getDefault());
            address.setLatitude(resultPOJO.getGeometry().getLocation().getLat());
            address.setLongitude(resultPOJO.getGeometry().getLocation().getLng());
            result.add(address);
        }
        return result;
    }

    private ArrayList<Place> buildAutoCompleteList(GMapsAutoCompletePOJO gMapsAutoCompletePOJO) {
        ArrayList<Place> result = new ArrayList<>();
        Timber.d("Got: " + gMapsAutoCompletePOJO.getPredictions().size());
        for (GMapsAutoCompletePredictionPOJO gMapsAutoCompletePredictionPOJO : gMapsAutoCompletePOJO.getPredictions()) {
            Timber.d("Got place: " + gMapsAutoCompletePredictionPOJO.getStructuredFormatting().getMainText());
            result.add(
                 new Place(
                    gMapsAutoCompletePredictionPOJO.getPlaceId(),
                    gMapsAutoCompletePredictionPOJO.getStructuredFormatting().getMainText(),
                    gMapsAutoCompletePredictionPOJO.getDescription(),
                    null));
        }
        return result;
    }

    @Override
    protected DirectionsResponseStatus getDirectionsStatus(String responseCode) {
        if( responseCode.equals("OK") ) {
            return DirectionsResponseStatus.DIRECTIONS_RESPONSE_OK;
        }
        if( responseCode.equals("ZERO_RESULT") ) {
            return DirectionsResponseStatus.DIRECTIONS_RESPONSE_ROUTE_NOT_FOUND;
        }
        if( responseCode.equals("NOT_FOUND") ) {
            return DirectionsResponseStatus.DIRECTIONS_RESPONSE_GEOCODE_FAILED;
        }
        if( responseCode.equals("INVALID_REQUEST") ) {
            return DirectionsResponseStatus.DIRECTIONS_RESPONSE_INVALID_ARGS;
        }
        if( responseCode.equals("MAX_ROUTE_LENGTH_EXCEEDED") ) {
            return DirectionsResponseStatus.DIRECTIONS_RESPONSE_ROUTE_TOO_LONG;
        }
        return DirectionsResponseStatus.DIRECTIONS_RESPONSE_ERROR;
    }

    @Override
    protected GeocodeResponseStatus getGeocodeStatus(String responseCode) {
        // If we have no results we can still return an empty list...it's not necessary to be an error.
        if( responseCode.equals("OK") ) {
            return GeocodeResponseStatus.GEOCODER_SERVICE_OK;
        }
        if( responseCode.equals("ZERO_RESULTS") ) {
            return GeocodeResponseStatus.GEOCODER_SERVICE_NO_RESULTS;
        }
        if( responseCode.equals("OVER_DAILY_LIMIT") || responseCode.equals("OVER_QUERY_LIMIT") ) {
            return GeocodeResponseStatus.GEOCODER_SERVICE_QUOTA_REACHED;
        }
        if( responseCode.equals("INVALID_REQUEST") || responseCode.equals("REQUEST_DENIED") ) {
            return GeocodeResponseStatus.GEOCODER_SERVICE_INVALID_ARGS;
        }
        return GeocodeResponseStatus.GEOCODER_SERVICE_ERROR;
    }

    @Override
    protected AutoCompleteResponseStatus getAutoCompleteStatus(String responseCode) {
        if( responseCode.equals("OK") ) {
            return AutoCompleteResponseStatus.AUTOCOMPLETE_SERVICE_OK;
        }
        if( responseCode.equals("ZERO_RESULT") ) {
            return AutoCompleteResponseStatus.AUTOCOMPLETE_SERVICE_NO_RESULTS;
        }
        if( responseCode.equals("OVER_QUERY_LIMIT") ) {
            return AutoCompleteResponseStatus.AUTOCOMPLETE_SERVICE_QUOTA_REACHED;
        }
        if( responseCode.equals("INVALID_REQUEST") || responseCode.equals("REQUEST_DENIED") ) {
            return AutoCompleteResponseStatus.AUTOCOMPLETE_SERVICE_INVALID_ARGS;
        }
        return AutoCompleteResponseStatus.AUTOCOMPLETE_SERVICE_ERROR;
    }

    @Override
    public void getRoute(TravelMode travelMode, LatLng startPosition, LatLng endPosition, int requestID,
                         DirectionResponseCallback directionResponseCallback) {
        Call<GMapsDirectionsPOJO> call = googleMapsAPI.getDirectionsPolyline(getTravelMode(travelMode),
                apiKey,
                Utils.getLatLng(startPosition), Utils.getLatLng(endPosition));
        call.enqueue(new Callback<GMapsDirectionsPOJO>() {
            @Override
            public void onResponse(Call<GMapsDirectionsPOJO> call, Response<GMapsDirectionsPOJO> response) {
                DirectionsResponseStatus directionsResponseStatus;
                if( response.isSuccessful() ) {
                    ArrayList<LatLng> result = null;
                    GMapsDirectionsPOJO gMapsDirectionsPOJO = response.body();
                    directionsResponseStatus = getDirectionsStatus(gMapsDirectionsPOJO.getStatus());
                    if( directionsResponseStatus == DirectionsResponseStatus.DIRECTIONS_RESPONSE_OK ) {
                        result = buildRoute(response.body());
                    }
                    directionResponseCallback.onDirectionResponseResult(requestID,result,
                            directionsResponseStatus);

                } else {
                    directionsResponseStatus = DirectionsResponseStatus.DIRECTIONS_RESPONSE_ERROR;
                    directionResponseCallback.onDirectionResponseResult(requestID,null, directionsResponseStatus);
                }
            }

            @Override
            public void onFailure(Call<GMapsDirectionsPOJO> call, Throwable t) {
                directionResponseCallback.onDirectionResponseResult(requestID,null,
                        DirectionsResponseStatus.DIRECTIONS_RESPONSE_SERVICE_NOT_AVAILABLE);
            }
        });
    }

    @Override
    public void geocode(String position, int requestID, GeocodeResponseCallback geocodeResponseCallback) {
        Call<GMapsGeocodePOJO> call = googleMapsAPI.geocode(
                apiKey,
                position);
        call.enqueue(new Callback<GMapsGeocodePOJO>() {
            @Override
            public void onResponse(Call<GMapsGeocodePOJO> call, Response<GMapsGeocodePOJO> response) {
                GeocodeResponseStatus geocodeResponseStatus = GeocodeResponseStatus.GEOCODER_SERVICE_ERROR;
                if( response.isSuccessful() ) {
                    ArrayList<Address> addressArrayList = null;
                    geocodeResponseStatus = getGeocodeStatus(response.body().getStatus());
                    Timber.d("Got: " + response.body().getStatus());
                    if( geocodeResponseStatus == GeocodeResponseStatus.GEOCODER_SERVICE_OK ) {
                        addressArrayList = buildGeocodeAddressList(response.body());
                    }
                    Timber.d("ResponseStatus: " + geocodeResponseStatus);
                    geocodeResponseCallback.onGeocodeResponseResult(requestID,addressArrayList,
                            geocodeResponseStatus);
                } else {
                    geocodeResponseCallback.onGeocodeResponseResult(requestID,null,
                            geocodeResponseStatus);
                }
            }

            @Override
            public void onFailure(Call<GMapsGeocodePOJO> call, Throwable t) {
                Timber.d("Failed!");
                t.printStackTrace();
                geocodeResponseCallback.onGeocodeResponseResult(requestID,null,
                        GeocodeResponseStatus.GEOCODER_SERVICE_NOT_AVAILABLE);
            }
        });
    }

    @Override
    public void autocomplete(String text, int requestID, AutoCompleteResponseCallback autoCompleteResponseCallback) {
        Call<GMapsAutoCompletePOJO> call = googleMapsAPI.autocomplete(
                Locale.getDefault().getLanguage().toString(),
                apiKey,
                text);
        call.enqueue(new Callback<GMapsAutoCompletePOJO>() {
            @Override
            public void onResponse(Call<GMapsAutoCompletePOJO> call, Response<GMapsAutoCompletePOJO> response) {
                AutoCompleteResponseStatus autoCompleteResponseStatus =
                        AutoCompleteResponseStatus.AUTOCOMPLETE_SERVICE_ERROR;
                if( response.isSuccessful() ) {
                    ArrayList<Place> result = null;
                    autoCompleteResponseStatus = getAutoCompleteStatus(response.body().getStatus());
                    if( autoCompleteResponseStatus == AutoCompleteResponseStatus.AUTOCOMPLETE_SERVICE_OK ) {
                        result = buildAutoCompleteList(response.body());
                    }
                    autoCompleteResponseCallback.onAutoCompleteResponseResult(requestID,result,autoCompleteResponseStatus);
                } else {
                    autoCompleteResponseCallback.onAutoCompleteResponseResult(requestID,null, autoCompleteResponseStatus);
                }
            }

            @Override
            public void onFailure(Call<GMapsAutoCompletePOJO> call, Throwable t) {
                t.printStackTrace();
                autoCompleteResponseCallback.onAutoCompleteResponseResult(requestID,null,
                        AutoCompleteResponseStatus.AUTOCOMPLETE_SERVICE_NOT_AVAILABLE);
            }
        });
    }

    @Override
    public ArrayList<Place> autocomplete(String text) {
        ArrayList<Place> result = null;
        Call<GMapsAutoCompletePOJO> call = googleMapsAPI.autocomplete(Locale.getDefault().getLanguage().toString(),
                apiKey,
                text);
        try {
            Response<GMapsAutoCompletePOJO> response = call.execute();
            if( response.isSuccessful() ) {
                Timber.d("autocomplete with key = " + apiKey);
                GMapsAutoCompletePOJO gMapsAutoCompletePOJO = response.body();
                Timber.d("Status: " + gMapsAutoCompletePOJO.getStatus());
                result = buildAutoCompleteList(gMapsAutoCompletePOJO);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if( result == null ) {
            result = new ArrayList<>();
        }
        return result;
    }

    public GoogleMapsRestMapClient(String apiKey) {
        super(BASE_URL);
        this.apiKey = apiKey;
        googleMapsAPI = retrofit.create(GoogleMapsAPI.class);
    }
}
