package com.adriano.cartoon.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.adriano.cartoon.BuildConfig;
import com.adriano.cartoon.Camera;
import com.adriano.cartoon.Constants;
import com.adriano.cartoon.R;
import com.adriano.cartoon.Utils;
import com.adriano.cartoon.restclients.GoogleMapsRestMapClient;
import com.adriano.cartoon.restclients.OpenRouteServiceRestMapClient;
import com.adriano.cartoon.restclients.RestMapClient;
import com.adriano.cartoon.services.NearByCameraResultReceiver;
import com.adriano.cartoon.services.NearByCameraService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Locale;

import timber.log.Timber;


/**
 * BaseClass that is able to handle a MapView.
 * It handles if necessary a FusedLocationProviderClient.
 * SuppressLint is necessary since it is not detecting it.
 * (Our check is inside utils class).
 */
@SuppressLint("MissingPermission")
public abstract class BaseMapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {
    private static final int SET_LOCATION_UPDATE_PERMISSION_REQUEST = 99;
    //    private static final int REQUEST_LOCATION_PERMISSION = 99;
    private static final int REQUEST_INTERVAL_TIME = 10 * 1000; // 10s
    private static final int REQUEST_FASTEST_INTERVAL_TIME = 5 * 1000; //5s
    protected MapView mapView;
    protected GoogleMap map;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    protected RestMapClient restMapClient;
    protected SharedPreferences sharedPreferences;

//    protected abstract void onLocationResult(LocationResult locationResult);
//
//    private LocationCallback locationCallback = new LocationCallback() {
//        @Override
//        public void onLocationResult(LocationResult locationResult) {
//            super.onLocationResult(locationResult);
//            BaseMapFragment.this.onLocationResult(locationResult);
//
//        }
//    };
//

    private void requestPermission(String[] permissions, int permissionRequestCode) {
        requestPermissions(permissions, permissionRequestCode);
    }

    private void displayPermissionRequestReasonDialog(String[] permissionRequestList, int permissionRequestCode) {
        AlertDialog.Builder dialogBuilder;
        dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle(R.string.title_warning_dialog);
        dialogBuilder.setMessage(getString(R.string.permission_request_explanation));
        dialogBuilder.setPositiveButton(android.R.string.ok, (dialog, which) -> requestPermission(permissionRequestList, permissionRequestCode));
        dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        dialogBuilder.show();
    }

    private boolean permissionGranted(int[] grantResults) {
        if (grantResults.length > 0) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Timber.d("requestPermissionResult: code: " + requestCode);
        if (!permissionGranted(grantResults)) {
            Timber.d("Permission was not granted!");
            getActivity().finish();
            return;
        }
        switch (requestCode) {
            case SET_LOCATION_UPDATE_PERMISSION_REQUEST:
                requestLocationUpdates();
                break;
            default:
                Timber.d("Unknown action to take " + requestCode);
                break;
        }
    }

    private void askPositionAccessPermission(int permissionRequestCode) {
        String[] permissionRequestList = {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };
        if (hasGPSPermission()) {
            Timber.d("User already has permission to access location...");
            return;
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            displayPermissionRequestReasonDialog(permissionRequestList, permissionRequestCode);
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    permissionRequestList, permissionRequestCode);
        }
    }

    public boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean hasGPSPermission() {
        return hasPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                && hasPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
    }

//    protected void getLastLocation(OnSuccessListener<Location> locationOnSuccessListener) {
//        if (!hasGPSPermission()) {
//            askPositionAccessPermission();
//            return;
//        }
//        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(locationOnSuccessListener);
//    }

    protected int getSearchRadiusFromPreference() {
//        if( !isAdded() ) {
//            Timber.d("Not yet...try later!");
//            return Constants.NEARBY_DEFAULT_SEARCH_RANGE;
//        }
        return sharedPreferences.getInt(
                getString(R.string.key_nearby_search_radius), Constants.NEARBY_DEFAULT_SEARCH_RANGE);
    }

    protected void setLocationCallback(LocationCallback locationCallback) {
        this.locationCallback = locationCallback;
    }

    protected void stopLocationUpdates() {
        if (locationCallback == null) {
            Timber.d("Failed locationCallback cannot be null");
            return;
        }
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        map.setMyLocationEnabled(false);
    }

    protected void requestLocationUpdates() {
        if (locationCallback == null) {
            Timber.d("Failed locationCallback cannot be null");
            return;
        }
        if (!hasGPSPermission()) {
            askPositionAccessPermission(SET_LOCATION_UPDATE_PERMISSION_REQUEST);
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        map.setMyLocationEnabled(true);
    }

    protected void setLocationRequestInterval(long interval, long fastestInterval) {
        locationRequest.setInterval(interval);
        locationRequest.setFastestInterval(fastestInterval);
    }

    private void initLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        locationRequest = new LocationRequest();
        locationRequest.setInterval(REQUEST_INTERVAL_TIME);
        locationRequest.setFastestInterval(REQUEST_FASTEST_INTERVAL_TIME);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    protected void spawnRadiusCameraSearchService(LatLng position, int radius,
                                                  NearByCameraResultReceiver nearByCameraResultReceiver) {
        Intent intent = new Intent(getContext(), NearByCameraService.class);
        intent.putExtra(NearByCameraService.RECEIVER_BUNDLE_KEY, nearByCameraResultReceiver);
        intent.putExtra(NearByCameraService.SEARCH_ORIGIN_LATITUDE_KEY, position.latitude);
        intent.putExtra(NearByCameraService.SEARCH_ORIGIN_LONGITUDE_KEY, position.longitude);
        intent.putExtra(NearByCameraService.SEARCH_RADIUS_KEY, radius);
        getContext().startService(intent);
    }

    protected void displayWarning(int MessageResourceID, Object... Args) {
        String message;
        message = String.format(getString(MessageResourceID), Args);
        Utils.displayWarning(message, getContext());
    }

    protected void drawCircle(LatLng center, float radius, int color) {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(center);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.BLACK);
        circleOptions.fillColor(color);
        circleOptions.strokeWidth(2);
        map.addCircle(circleOptions);
    }

    protected void drawCircle(CircleOptions circleOptions, int color) {
        if (circleOptions == null) {
            return;
        }
        circleOptions.strokeColor(Color.BLACK);
        circleOptions.fillColor(color);
        circleOptions.strokeWidth(2);
        map.addCircle(circleOptions);
    }

    protected ArrayList<Camera> getCameraList(JSONArray jsonArray) {
        Camera cameraObject;
        ArrayList<Camera> cameraArrayList;

        if (jsonArray == null) {
            return null;
        }
        //TODO:CameraList is implementation-dependant.
        cameraArrayList = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                cameraObject = new Camera();
                cameraObject.fromJSON(jsonArray.getJSONObject(i));
                cameraArrayList.add(cameraObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return cameraArrayList;
    }

    public void clearMap() {
        map.clear();
    }

    /*
     * Set the map view for the fragment and initialize it.
     * It also calls getMapAsync and receive results in on MapReady.
     * */
    protected void initMapView(MapView mapView, Bundle savedInstanceState) {
        this.mapView = mapView;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mapView != null) {
            mapView.onStop();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mapView != null) {
            mapView.onStart();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        if (fusedLocationProviderClient != null && locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Places.isInitialized()) {
            Places.initialize(getContext(), getString(R.string.google_maps_key), Locale.ITALIAN);
        }
        if( BuildConfig.USE_GOOGLE_MAPS_REST_CLIENT ) {
            restMapClient = new GoogleMapsRestMapClient(getString(R.string.google_maps_rest_api_key));
        } else {
            restMapClient = new OpenRouteServiceRestMapClient(getString(R.string.open_route_service_api_key));
        }
        //
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        initLocation();

//        if( savedInstanceState != null ) {
//            Timber.d("onCreate called restoring map state");
//            polylineOptionsArrayList.clear();
//            polylineOptionsArrayList.addAll(savedInstanceState.
//                    <PolylineOptions>getParcelableArrayList(POLYLINE_OPTIONS_BUNDLE_KEY));
//            markerOptionsArrayList.clear();
//            markers.clear();
//            ArrayList<MarkerOptions> markerOptionsParcelableList = savedInstanceState.
//                    <MarkerOptions>getParcelableArrayList(MARKER_OPTIONS_BUNDLE_KEY);
//            for( MarkerOptions markerOptions2 : markerOptionsParcelableList ) {
//                Timber.d("Parcelable contains: " + markerOptions2.getTitle());
//            }
//            markerOptionsArrayList = markerOptionsParcelableList;
//            for( MarkerOptions markerOptions2 : markerOptionsArrayList ) {
//                Timber.d("copied contains: " + markerOptions2.getTitle());
//            }
//            cameraPosition = savedInstanceState.getParcelable(CAMERA_STATE_BUNDLE_KEY);
//            restoreMapState = true;
//            Timber.d("onCreate got " + polylineOptionsArrayList.size() + " polyLines");
//        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /*
     * Called when getMapAsync returns.
     * */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMapLoadedCallback(this);
    }

    @Override
    public void onMapLoaded() {
    }
}
