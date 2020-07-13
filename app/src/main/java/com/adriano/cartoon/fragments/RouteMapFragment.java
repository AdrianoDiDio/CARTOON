package com.adriano.cartoon.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

import com.adriano.cartoon.Camera;
import com.adriano.cartoon.Constants;
import com.adriano.cartoon.ProgressDialog;
import com.adriano.cartoon.R;
import com.adriano.cartoon.TravelMode;
import com.adriano.cartoon.adapters.Place;
import com.adriano.cartoon.adapters.PlacesAutoCompleteAdapter;
import com.adriano.cartoon.adapters.PlacesAutoCompleteClickListener;
import com.adriano.cartoon.comparators.CameraDistanceComparator;
import com.adriano.cartoon.restclients.responses.DirectionResponseCallback;
import com.adriano.cartoon.restclients.responses.DirectionsResponseStatus;
import com.adriano.cartoon.restclients.responses.GeocodeResponseCallback;
import com.adriano.cartoon.restclients.responses.GeocodeResponseStatus;
import com.adriano.cartoon.services.NearByCameraReceiver;
import com.adriano.cartoon.services.NearByCameraResultReceiver;
import com.adriano.cartoon.services.NearByCameraService;
import com.adriano.cartoon.widgets.OnCheckedChangeListener;
import com.adriano.cartoon.widgets.ToggleFloatingActionButton;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import timber.log.Timber;

/*
 * TODO:After fetching camera object from server store it locally and save if necessary (It's parcelable)...
 *      When restoring check if restored marker has the same title!
 * TODO:RecyclerView on click outside close.
 *
 * */
public class RouteMapFragment extends BaseMapFragment implements View.OnClickListener,
        DirectionResponseCallback, GeocodeResponseCallback,
        PlacesAutoCompleteClickListener, TextWatcher, NearByCameraReceiver,
        GoogleMap.OnMarkerClickListener, TextView.OnEditorActionListener, OnCheckedChangeListener {
    private static final String CAMERA_LIST_BUNDLE_KEY = "CameraList";
    private static final String CAMERA_SELECTED_BUNDLE_KEY = "SelectedCamera";
    private static final String ROUTE_POLYLINE_OPTIONS = "RoutePolylineOptions";
    private static final String ROUTE_CIRCLE_OPTIONS = "RouteCircleOptions";
    private static final String SEARCH_LAYOUT_VISIBLE_BUNDLE_KEY = "SearchLayoutVisible";
    private static final String START_LOCATION_BUNDLE_KEY = "StartLocation";
    private static final String DEST_LOCATION_BUNDLE_KEY = "DestLocation";
    private static final String CURRENT_LOCATION_BUNDLE_KEY = "CurrentLocation";

    private ViewGroup searchRouteLayoutParent;
    private LinearLayout searchRouteLayout;
    private TransitionSet transitionSet;
    private Transition slideTransitionTop;
    private Transition slideTransitionRight;
    private LinearLayout mapToolbarLayout;
    private EditText routeStartAddressEditText;
    private EditText routeEndAddressEditText;
    private RadioGroup routeSearchModeGroup;
    private ImageButton routePerformSearchButton;
    private FloatingActionButton showRouteSearchButton;
    private FloatingActionButton showVideoCameraButton;
    private ToggleFloatingActionButton dynamicModeToggle;
    private String startAddress;
    private String destAddress;
    private LatLng startPosition;
    private LatLng destPosition;
    private PlacesAutoCompleteAdapter autoCompleteAdapter;
    private RecyclerView recyclerView;
    private ProgressDialog progressDialog;
    private ArrayList<Camera> cameraArrayList;
    //Used in dynamic mode contains a sorted by distance from user subset of the current camera list
    //used to narrow down the visibile number of cameras on screen!
    private ArrayList<Camera> filteredCameraArrayList;
    private Marker prevMarker;
    private Camera selectedCameraObject;
    // Needed if we want to restore state.
    private PolylineOptions routePolylineOptions;
    private CircleOptions routeCircleOptions;
    private boolean restoreMap;
    private LatLng currentPosition;
    private NearByCameraResultReceiver nearByCameraResultReceiver;

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);
                currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
                dynamicModeOnPositionChange(currentPosition);
            }

        }
    };

    public RouteMapFragment() {
        cameraArrayList = new ArrayList<>();
    }

    private void dynamicModeOnPositionChange(LatLng position) {
        if (!PolyUtil.isLocationOnPath(position, routePolylineOptions.getPoints(),
                true, Constants.DEFAULT_MAPS_TOLERANCE)) {
            Timber.d("Not on path recalculating.");
//            if (googleMapsRouteDirectionsAsyncTask != null &&
//                    googleMapsRouteDirectionsAsyncTask.getAsyncStatus() == AsyncTask.Status.RUNNING) {
//                //Let it end before looking for alternative routes...
//                Timber.d("Asynctask is still running...");
//                return;
//            }
            // We have moved away from our path request a new route.
            startPosition = position;
            showVideoCameraButton.setEnabled(false);
            map.clear();
            requestRoute();
        } else {
            Timber.d("In path nearby search now!");
//            filteredCameraArrayList = cameraArrayList.sort();
            //User is on the new track start looking for near-by cameras.
            //In dynamic mode we cannot select any camera!
            //We could narrow down the camera needed just by modifying code.
            //but in this way we are able to recover if user wants to go back to normal mode.

            routeCircleOptions = getRouteCircleOptions();
            spawnRadiusCameraSearchService(routeCircleOptions.getCenter(),
                    (int) routeCircleOptions.getRadius(),
                    nearByCameraResultReceiver);

//            if( cameraArrayList.isEmpty() ) {
//                return;
//            }
//            SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(getContext());
//            int numNearbyCameras = sharedPreferences.getInt(
//                    getString(R.string.key_nearby_camera_number), Constants.NEARBY_DEFAULT_CAMERA_NUMBER);
//            if( numNearbyCameras >= cameraArrayList.size() ) {
//                numNearbyCameras = cameraArrayList.size();
//            }
//            filteredCameraArrayList = new ArrayList<>(cameraArrayList.subList(0,numNearbyCameras));
//            Timber.d("Slicing " + numNearbyCameras + " returned " + filteredCameraArrayList.size());
//            if( !filteredCameraArrayList.isEmpty() ) {
//                showVideoCameraButton.setEnabled(true);
//            }
//            map.clear();
//            drawCircle(routeCircleOptions, Constants.COLOR_BLUE);
//
//            for( Camera camera : filteredCameraArrayList ) {
//                map.addMarker(getDefaultMakerOptions(camera));
//            }
        }
    }

    private boolean isDynamicMode() {
        return dynamicModeToggle.isChecked();
    }

    //TODO:Again check for permissions.
    @SuppressLint("MissingPermission")
    private void setDynamicMode(boolean enabled) {
        if (enabled) {
            Timber.d("GPS dynamic mode on!");
            requestLocationUpdates();
        } else {
            Timber.d("GPS dynamic mode off!");
            stopLocationUpdates();
            showVideoCameraButton.setEnabled(false);
        }
    }

    @Override
    public void onCheckedChange(ToggleFloatingActionButton toggleFloatingActionButton, boolean isChecked) {
        // NOTE(Adriano): We need to make sure that map is loaded to enable/disable dynamic mode
        // otherwise we would get a crash.
        // Since android calls onCheckedChange when restoring from a configuration change
        // we need to handle it in MapLoaded not here...
        if (toggleFloatingActionButton.getId() == dynamicModeToggle.getId() && !restoreMap) {
            setDynamicMode(isChecked);
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_GO) {
            if (v.getId() == routeStartAddressEditText.getId()) {
                routeEndAddressEditText.requestFocus();
            }
            hideRecyclerView();
            return true;
        }
        return false;
    }

    private TravelMode getTravelModeByRadioButtonID(int radioButtonID) {
        if (radioButtonID == R.id.RadioModeWalk) {
            return TravelMode.WALK;
        }
        return TravelMode.DRIVE;
    }

    private TravelMode getCurrentTravelMode() {
        return getTravelModeByRadioButtonID(routeSearchModeGroup.getCheckedRadioButtonId());
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager)
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(routeEndAddressEditText.getWindowToken(), 0);
    }

    private BitmapDescriptor getMarkerIcon(MarkerIconType iconType) {
        switch (iconType) {
            case ICON_TYPE_ON_ROUTE:
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
            case ICON_TYPE_SELECTED:
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
            case ICON_TYPE_START_ADDRESS:
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
            case ICON_TYPE_END_ADDRESS:
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA);
            case ICON_TYPE_OUT_ROUTE:
            case ICON_TYPE_DEFAULT:
            default:
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
        }
    }

    private MarkerOptions getDefaultMakerOptions(String title, LatLng position, MarkerIconType iconType) {
        MarkerOptions defaultOptions = new MarkerOptions();
        defaultOptions.title(title);
        defaultOptions.icon(getMarkerIcon(iconType));
        defaultOptions.position(position);
        return defaultOptions;
    }

    private MarkerOptions getDefaultMakerOptions(Camera camera) {
        MarkerOptions defaultOptions = new MarkerOptions();
        defaultOptions.title(camera.name);
        if (camera.selected) {
            defaultOptions.icon(getMarkerIcon(MarkerIconType.ICON_TYPE_SELECTED));
            selectedCameraObject = camera;
        } else {
            if (camera.enabled) {
                defaultOptions.icon(getMarkerIcon(MarkerIconType.ICON_TYPE_ON_ROUTE));
            } else {
                defaultOptions.icon(getMarkerIcon(MarkerIconType.ICON_TYPE_OUT_ROUTE));
            }
//            if (camera.onRoute) {
//            } else {
//                defaultOptions.icon(GetMarkerIcon(MarkerIconType.ICON_TYPE_OUT_ROUTE));
//            }
        }
        defaultOptions.position(camera.position);
        return defaultOptions;
    }

    private PolylineOptions getDefaultPolylineOptions() {
        PolylineOptions defaultOptions = new PolylineOptions();
        defaultOptions.width(20);
        defaultOptions.color(Color.BLUE);
        return defaultOptions;
    }

    private CircleOptions getRouteCircleOptions() {
        CircleOptions circleOptions;
        LatLng center;
        int radius;
        circleOptions = new CircleOptions();
        if (isDynamicMode()) {
            center = currentPosition;
            radius = getSearchRadiusFromPreference();
        } else {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (int i = 0; i < routePolylineOptions.getPoints().size(); i++) {
                builder.include(routePolylineOptions.getPoints().get(i));
            }
            LatLngBounds latLngBounds = builder.build();
            center = latLngBounds.getCenter();
            float[] r1 = new float[1];
            float[] r2 = new float[1];
            Location.distanceBetween(startPosition.latitude, startPosition.longitude,
                    center.latitude, center.longitude, r1);
            Location.distanceBetween(destPosition.latitude, destPosition.longitude,
                    center.latitude, center.longitude, r2);
            radius = (int) Math.max(r1[0], r2[0]);
        }
        circleOptions.center(center);
        circleOptions.radius(radius);
        return circleOptions;
    }

    private void fitMapToPolyline() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLngBounds bounds;
        if (routePolylineOptions == null) {
            return;
        }
        for (int i = 0; i < routePolylineOptions.getPoints().size(); i++) {
            builder.include(routePolylineOptions.getPoints().get(i));
        }
        bounds = builder.build();
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
    }

    public void showRecyclerView() {
        if (recyclerView.getVisibility() == View.GONE) {
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    public void hideRecyclerView() {
        if (recyclerView.getVisibility() == View.VISIBLE) {
            autoCompleteAdapter.clear();
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void validateUserInput(final String StartAddress, String EndAddress) {
        if (TextUtils.isEmpty(StartAddress)) {
            Timber.d("StartAddress empty.");
            displayWarning(R.string.empty_start_address_warning);
            return;
        }
        if (TextUtils.isEmpty(EndAddress)) {
            Timber.d("EndAddress empty.");
            displayWarning(R.string.empty_end_address_warning);
            return;
        }
        //GEOCODE!
        toggleSearchRouteLayout();
        this.startAddress = StartAddress;
        this.destAddress = EndAddress;
        Timber.d("Geocoding " + StartAddress + "-->" + EndAddress);
        clearMap();
        restMapClient.geocode(startAddress,R.id.geocode_start_address_request_id,this);
//        ORSGeocoder orsGeocoder = new ORSGeocoder(R.id.geocode_start_address_request_id,startAddress,
//                this);
//        googleMapsGeoCoderAsyncTask.execute(StartAddress);
        progressDialog.show();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Camera cameraObject;

        cameraObject = (Camera) marker.getTag();

        if( cameraObject == null ) {
            marker.showInfoWindow();
            return false;
        }

        if (!cameraObject.enabled) {
            return false;
        }

        //Selecting a marker is disable in this mode!
        if (isDynamicMode()) {
            marker.showInfoWindow();
            return false;
        }
        cameraObject.selected = !cameraObject.selected;
        if (cameraObject.selected) {
            showVideoCameraButton.setEnabled(true);
            selectedCameraObject = cameraObject;
            marker.setIcon(getMarkerIcon(MarkerIconType.ICON_TYPE_SELECTED));
            Timber.d("Set icon....");
            marker.showInfoWindow();
        } else {
            showVideoCameraButton.setEnabled(false);
            selectedCameraObject = null;
            marker.setIcon(getMarkerIcon(MarkerIconType.ICON_TYPE_ON_ROUTE));
        }
        if (prevMarker != null) {
            Camera prevCameraObject = (Camera) prevMarker.getTag();
            if (prevCameraObject != null) {
                prevMarker.setIcon(getMarkerIcon(MarkerIconType.ICON_TYPE_ON_ROUTE));
                prevCameraObject.selected = false;
            }
        }
        prevMarker = marker;
        return true;
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCode == NearByCameraService.NEAR_BY_CAMERA_SERVICE_RESULT_CODE) {
            int Status;
            Status = resultData.getInt(NearByCameraService.NEAR_BY_CAMERA_SERVICE_RESULT_STATUS_KEY);
            if (Status == -1) {
                displayWarning(R.string.camera_request_failed);
                progressDialog.dismiss();
                map.clear();
//                showHideSearchLayout(true);
                return;
            }
            map.clear();
            cameraArrayList = resultData.getParcelableArrayList(NearByCameraService.NEAR_BY_CAMERA_SERVICE_RESULT_KEY);
            putStartEndMarkersOnMap();
            putRoutePolylineOnMap();
            parseLocalCameraList();
            progressDialog.dismiss();
            fitMapToPolyline();
            Collections.sort(cameraArrayList,new CameraDistanceComparator(routeCircleOptions.getCenter()));
            putCameraMarkers();
            if (routeCircleOptions != null && isDynamicMode() ) {
                drawCircle(routeCircleOptions, Constants.COLOR_BLUE);
            }
        }
    }

    private void showHideSearchLayout(boolean show) {
        if (show) {
            searchRouteLayout.setVisibility(View.VISIBLE);
            mapToolbarLayout.setVisibility(View.GONE);
        } else {
            searchRouteLayout.setVisibility(View.GONE);
            mapToolbarLayout.setVisibility(View.VISIBLE);
        }
    }

    private void toggleSearchRouteLayout() {
        TransitionManager.beginDelayedTransition(searchRouteLayoutParent, transitionSet);
        if (searchRouteLayout.getVisibility() == View.VISIBLE) {
            showHideSearchLayout(false);
        } else {
            showHideSearchLayout(true);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == showRouteSearchButton.getId()) {
            toggleSearchRouteLayout();
            searchRouteLayout.setVisibility(View.VISIBLE);
        }
        if (v.getId() == routePerformSearchButton.getId()) {
            routeEndAddressEditText.clearFocus();
            hideKeyboard();
            hideRecyclerView();
            map.clear();
            if( isDynamicMode() ) {
                dynamicModeToggle.setChecked(false);
            }
            showVideoCameraButton.setEnabled(false);
            validateUserInput(routeStartAddressEditText.getText().toString(),
                    routeEndAddressEditText.getText().toString());
        }
        if (v.getId() == showVideoCameraButton.getId()) {
            ArrayList<Camera> selectedCameraArrayList = new ArrayList<>();
            if (isDynamicMode()) {
                selectedCameraArrayList.addAll(filteredCameraArrayList);
            } else {
                if (selectedCameraObject == null) {
                    Timber.d("This shouldn't have happened.");
                    return;
                }
                Timber.d("Showing video: " + selectedCameraObject.ip + ":" + selectedCameraObject.port);
                selectedCameraArrayList.add(selectedCameraObject);
            }
            FragmentManager fragmentManager = getParentFragmentManager();
            VideoPlayerHolderDialogFragment.PrepareFragmentManager(fragmentManager);
            VideoPlayerHolderDialogFragment newDialogFragment =
                    VideoPlayerHolderDialogFragment.newInstance(selectedCameraArrayList);
            newDialogFragment.setTargetFragment(this, Constants.DIALOG_REQUEST_CODE);
            newDialogFragment.show(fragmentManager, "videoDialog");
        }
        return;
    }

    @Override
    public void onDirectionResponseResult(int requestID, ArrayList<LatLng> route,
                                          DirectionsResponseStatus responseStatus) {
        if (requestID == R.id.get_route_request_id) {
            if (route == null) {
                displayWarning(R.string.route_search_request_failed);
                progressDialog.dismiss();
                return;
            }
            routePolylineOptions = getOverviewPolylineOptions(route);
            if (routePolylineOptions == null) {
                //TODO:This should not happens since we already checked
                //     that passed information is valid but still add a warning.
                Timber.d("polyLine returned null!");
                return;
            }


            routeCircleOptions = getRouteCircleOptions();

            //TODO:Nudge radius value to have some tolerance...
//            if (isDynamicMode()) {
//                spawnRadiusCameraSearchService(currentPosition, getSearchRadiusFromPreference(),
//                        nearByCameraResultReceiver);
//            } else {
            Timber.d("Spawning it with: " + routeCircleOptions.getCenter().toString());
            spawnRadiusCameraSearchService(routeCircleOptions.getCenter(),
                    (int) routeCircleOptions.getRadius(),
                    nearByCameraResultReceiver);
//            }
        }
    }


    private void requestRoute() {
//        GoogleMapsRouteDirectionsAsyncTask googleMapsRouteDirectionsAsyncTask;
//        googleMapsRouteDirectionsAsyncTask =
//                new GoogleMapsRouteDirectionsAsyncTask(R.id.get_route_request_id, startPosition, destPosition,
//                        getCurrentTravelMode(), this);
//        return googleMapsRouteDirectionsAsyncTask;
        restMapClient.getRoute(getCurrentTravelMode(),startPosition,destPosition,R.id.get_route_request_id,
                this);
        return;
//        OpenRouteServiceRouteDirectionsAsyncTask openRouteServiceRouteDirectionsAsyncTask;
//        openRouteServiceRouteDirectionsAsyncTask =
//                new OpenRouteServiceRouteDirectionsAsyncTask(R.id.get_route_request_id, startPosition, destPosition,
//                        getCurrentTravelMode(), this);
//        return openRouteServiceRouteDirectionsAsyncTask;
    }

    @Override
    public void onGeocodeResponseResult(int requestID, ArrayList<Address> addressArrayList, GeocodeResponseStatus geoCodeResponseStatus) {
        if (geoCodeResponseStatus != GeocodeResponseStatus.GEOCODER_SERVICE_OK) {
            Timber.d("Geocode response: " + geoCodeResponseStatus);
            if (geoCodeResponseStatus == GeocodeResponseStatus.GEOCODER_SERVICE_INVALID_ARGS ||
                    geoCodeResponseStatus == GeocodeResponseStatus.GEOCODER_SERVICE_NO_RESULTS ) {
                displayWarning(R.string.geocoder_invalid_args, startAddress);
            } else if (geoCodeResponseStatus == GeocodeResponseStatus.GEOCODER_SERVICE_NOT_AVAILABLE ||
                    geoCodeResponseStatus == GeocodeResponseStatus.GEOCODER_SERVICE_ERROR ) {
                displayWarning(R.string.geocoder_not_available, startAddress);
            }
            toggleSearchRouteLayout();
            progressDialog.dismiss();
            return;
        }
        if (requestID == R.id.geocode_start_address_request_id) {
            if (addressArrayList == null || addressArrayList.isEmpty()) {
                Timber.d("GeoCoder has not found start location matching address: " + startAddress);
                displayWarning(R.string.search_start_location_not_found, startAddress);
                progressDialog.dismiss();
                return;
            }
            startPosition = new LatLng(addressArrayList.get(0).getLatitude(),
                    addressArrayList.get(0).getLongitude());
            restMapClient.geocode(destAddress,R.id.geocode_end_address_request_id,this);
//            ORSGeocoder orsGeocoder = new ORSGeocoder(
//                    R.id.geocode_end_address_request_id,destAddress,this);
//            orsGeocoderAsyncTask.execute();
            progressDialog.show();
        }
        if (requestID == R.id.geocode_end_address_request_id) {
            if (addressArrayList == null || addressArrayList.isEmpty()) {
                Timber.d("GeoCoder has not found a location matching address: " + destAddress);
                displayWarning(R.string.search_destination_location_not_found, destAddress);
                progressDialog.dismiss();
                return;
            }
            destPosition = new LatLng(addressArrayList.get(0).getLatitude(),
                    addressArrayList.get(0).getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(startPosition, 10));
            requestRoute();
            progressDialog.show();
        }
    }

    @Override
    public void onAutoCompleteResultClick(Place selectedPlace) {
        if (routeStartAddressEditText.hasFocus()) {
            routeStartAddressEditText.removeTextChangedListener(this);
            routeStartAddressEditText.setText(selectedPlace.fullAddress);
            routeStartAddressEditText.addTextChangedListener(this);
            routeEndAddressEditText.requestFocus();
        } else if (routeEndAddressEditText.hasFocus()) {
            routeEndAddressEditText.setText(selectedPlace.fullAddress);
            hideKeyboard();
        }
        hideRecyclerView();
    }

    @Override
    public void afterTextChanged(Editable s) {
        // This gets called on rotation change...
        String text;
        if (restoreMap) {
            return;
        }
        text = s.toString().trim();
        if (!text.equals("")) {
            autoCompleteAdapter.getFilter().filter(text);
            showRecyclerView();
        } else {
            hideRecyclerView();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        return;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        return;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
        map.getUiSettings().setMapToolbarEnabled(false);
        map.setOnMarkerClickListener(this);

        if (restoreMap) {
            map.clear();
            putCameraMarkers();
            putRoutePolylineOnMap();
            putStartEndMarkersOnMap();
            if (routeCircleOptions != null && isDynamicMode() ) {
                drawCircle(routeCircleOptions, Constants.COLOR_BLUE);
            }
            if (selectedCameraObject != null) {
                showVideoCameraButton.setEnabled(true);
            }
            setDynamicMode(dynamicModeToggle.isChecked());
            restoreMap = false;
        }
        //map.clear
        //iterate over markers and reattach tag that we have serialized before
        //should be ok then!
    }

    @Override
    public void onMapLoaded() {
        super.onMapLoaded();
        if (restoreMap) {
            //This requires map to be fully loaded!
            fitMapToPolyline();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Timber.d("Asked to save...");
        if (map != null) {
            //TODO:Save search visibility...
            outState.putParcelableArrayList(CAMERA_LIST_BUNDLE_KEY, cameraArrayList);
            outState.putParcelable(CAMERA_SELECTED_BUNDLE_KEY, selectedCameraObject);
            outState.putParcelable(ROUTE_POLYLINE_OPTIONS, routePolylineOptions);
            outState.putParcelable(ROUTE_CIRCLE_OPTIONS, routeCircleOptions);
            outState.putBoolean(SEARCH_LAYOUT_VISIBLE_BUNDLE_KEY,
                    searchRouteLayout.getVisibility() == View.VISIBLE);
            outState.putParcelable(CURRENT_LOCATION_BUNDLE_KEY, currentPosition);
            outState.putParcelable(START_LOCATION_BUNDLE_KEY, startPosition);
            outState.putParcelable(DEST_LOCATION_BUNDLE_KEY, destPosition);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            Timber.d("Asked to restore...");
            cameraArrayList.clear();
            cameraArrayList.addAll(savedInstanceState.getParcelableArrayList(CAMERA_LIST_BUNDLE_KEY));
            selectedCameraObject = (Camera) savedInstanceState.get(CAMERA_SELECTED_BUNDLE_KEY);
            routePolylineOptions = (PolylineOptions) savedInstanceState.get(ROUTE_POLYLINE_OPTIONS);
            routeCircleOptions = (CircleOptions) savedInstanceState.get(ROUTE_CIRCLE_OPTIONS);
            currentPosition = savedInstanceState.getParcelable(CURRENT_LOCATION_BUNDLE_KEY);
            startPosition = savedInstanceState.getParcelable(START_LOCATION_BUNDLE_KEY);
            destPosition = savedInstanceState.getParcelable(DEST_LOCATION_BUNDLE_KEY);

//            RouteStartAddress.clearFocus();
//            RouteEndAddress.clearFocus();
//            HideRecyclerView();
            restoreMap = true;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.DIALOG_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data.getExtras().containsKey(Constants.DIALOG_EXIT_CODE)) {
                    VideoPlayerExitCode exitCode = (VideoPlayerExitCode) data.getExtras().getSerializable(Constants.DIALOG_EXIT_CODE);
                    switch (exitCode) {
                        case EXIT_SOURCE_ERROR:
                            displayWarning(R.string.camera_connection_failed, selectedCameraObject.name);
                            return;
                        case EXIT_RENDER_ERROR:
                            displayWarning(R.string.camera_render_failed, selectedCameraObject.name);
                            return;
                        case EXIT_UNEXPECTED_ERROR:
                            displayWarning(R.string.camera_unexpected_error, selectedCameraObject.name);
                            return;
                        case EXIT_OK:
                        default:
                            Timber.d("All ok!");
                            return;

                    }
                }
            }
        }
    }

    private void putCameraMarkers() {
        if (cameraArrayList.isEmpty()) {
            Timber.d("cameraArrayList is empty...");
            return;
        }
        if (map == null) {
            Timber.d("Google map is not ready...");
            return;
        }
        if( isDynamicMode() ) {
            int numNearbyCameras = sharedPreferences.getInt(
                    getString(R.string.key_nearby_camera_number), Constants.NEARBY_DEFAULT_CAMERA_NUMBER);
            Timber.d("Got " + numNearbyCameras + " from preference");
            Timber.d("Got " + cameraArrayList.size() + " ordered cameras.");
            if( numNearbyCameras >= cameraArrayList.size() ) {
                numNearbyCameras = cameraArrayList.size();
            }
            filteredCameraArrayList = new ArrayList<>(cameraArrayList.subList(0,numNearbyCameras));
            Timber.d("Slicing " + numNearbyCameras + " returned " + filteredCameraArrayList.size());
            if( !filteredCameraArrayList.isEmpty() ) {
                showVideoCameraButton.setEnabled(true);
            }
            for( Camera camera : filteredCameraArrayList ) {
                map.addMarker(getDefaultMakerOptions(camera));
            }
        } else {
            for (Camera camera : cameraArrayList) {
                Marker marker;
                marker = map.addMarker(getDefaultMakerOptions(camera));
                marker.setTag(camera);
                if (camera.selected) {
                    if (prevMarker == null) {
                        prevMarker = marker;
                    }
                    marker.showInfoWindow();
                }
            }
        }
    }

    private void putRoutePolylineOnMap() {
        if (!sharedPreferences.getBoolean(getString(R.string.key_draw_overview_polyline), true)) {
            return;
        }
        if (routePolylineOptions == null) {
            return;
        }
        map.addPolyline(routePolylineOptions);
    }

    private void putStartEndMarkersOnMap() {
        if (startPosition == null || destPosition == null) {
            return;
        }

        map.addMarker(getDefaultMakerOptions(getString(R.string.start_address),
                startPosition,
                MarkerIconType.ICON_TYPE_START_ADDRESS));
        map.addMarker(getDefaultMakerOptions(getString(R.string.dest_address),destPosition,
                MarkerIconType.ICON_TYPE_END_ADDRESS));
    }

    /*
     * Draw a new Polyline from a JSONObject.
     * Returns a reference to the polyline that was put on the map.
     * TODO:Try not to get the overview but rather add all the points.
     * */
    private PolylineOptions getOverviewPolylineOptions(List<LatLng> route) {
        PolylineOptions polylineOptions;

        if( route == null ) {
            return null;
        }
        polylineOptions = getDefaultPolylineOptions().addAll(route);
        routePolylineOptions = polylineOptions;
        return polylineOptions;
    }

    private void parseLocalCameraList() {
        Iterator<Camera> cameraIterator;
        Timber.d("Parsing camera list!");
        cameraIterator = cameraArrayList.iterator();
        while (cameraIterator.hasNext()) {
            Camera camera = cameraIterator.next();
            if (!PolyUtil.isLocationOnPath(camera.position, routePolylineOptions.getPoints(),
                    true, Constants.DEFAULT_MAPS_TOLERANCE)) {
                cameraIterator.remove();
            } else {
                //At least one camera is on path
                if (isDynamicMode()) {
                    showVideoCameraButton.setEnabled(true);
                }
            }
        }
//        for( Camera camera : cameraArrayList ) {
//            if( !PolyUtil.isLocationOnPath(camera.position,routePolylineOptions.getPoints(),
//                    true,Constants.DEFAULT_MAPS_TOLERANCE) ) {
//                camera.onRoute = true;
//            }
//        }
    }

    private void initComponents(View view) {
        searchRouteLayoutParent = view.findViewById(R.id.FragmentRouteMapParent);
        searchRouteLayout = view.findViewById(R.id.RouteSearchLayout);

        slideTransitionTop = new Slide(Gravity.TOP);
        slideTransitionTop.setDuration(600);
        slideTransitionTop.addTarget(R.id.RouteSearchLayout);

        slideTransitionRight = new Slide(Gravity.RIGHT);
        slideTransitionRight.setDuration(600);
        slideTransitionRight.addTarget(R.id.MapToolbarLayout);

        transitionSet = new TransitionSet();
        transitionSet.addTransition(slideTransitionTop);
        transitionSet.addTransition(slideTransitionRight);

        mapToolbarLayout = view.findViewById(R.id.MapToolbarLayout);
        mapToolbarLayout.setVisibility(View.GONE);
        routeStartAddressEditText = view.findViewById(R.id.RouteStartAddress);
        routeStartAddressEditText.setImeOptions(EditorInfo.IME_ACTION_GO);
        routeStartAddressEditText.setOnEditorActionListener(this);
        routeEndAddressEditText = view.findViewById(R.id.RouteEndAddress);
        routeEndAddressEditText.setImeOptions(EditorInfo.IME_ACTION_GO);
        routeEndAddressEditText.setOnEditorActionListener(this);
        showRouteSearchButton = view.findViewById(R.id.ShowRouteSearchButton);
        showRouteSearchButton.setOnClickListener(this);
//        RouteEndAddress.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
//        RouteEndAddress.setOnEditorActionListener(this);
        routeSearchModeGroup = view.findViewById(R.id.RouteSearchMode);

        routePerformSearchButton = view.findViewById(R.id.RoutePerformSearch);
        routePerformSearchButton.setOnClickListener(this);

        showVideoCameraButton = view.findViewById(R.id.ShowVideoCameraButton);
        showVideoCameraButton.setEnabled(false);
        showVideoCameraButton.setOnClickListener(this);

        dynamicModeToggle = view.findViewById(R.id.RouteDynamicMode);
        dynamicModeToggle.setOnCheckedChangeListener(this);

        progressDialog = new ProgressDialog(getActivity());

        recyclerView = view.findViewById(R.id.PlacesSuggestionBox);

        routeStartAddressEditText.addTextChangedListener(this);
        routeEndAddressEditText.addTextChangedListener(this);

        autoCompleteAdapter = new PlacesAutoCompleteAdapter(restMapClient,getContext());

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setNestedScrollingEnabled(false);

        autoCompleteAdapter.setClickListener(this);

        recyclerView.setAdapter(autoCompleteAdapter);

        autoCompleteAdapter.notifyDataSetChanged();
        hideRecyclerView();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view;
        MapView mapView;
        view = inflater.inflate(R.layout.fragment_route_map, container, false);
        mapView = view.findViewById(R.id.RouteMapView);
        initMapView(mapView, savedInstanceState);
        initComponents(view);
        if (savedInstanceState != null) {
            boolean isSearchLayoutVisible;
            isSearchLayoutVisible = savedInstanceState.getBoolean(SEARCH_LAYOUT_VISIBLE_BUNDLE_KEY);
            showHideSearchLayout(isSearchLayoutVisible);
        }
//        } else {
//            if (currentPosition == null) {
//                getLastLocation(location -> {
//                    if (location != null) {
//                        Timber.d("Got current position");
//                        currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
//                    }
//                });
//
//            }
//        }
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nearByCameraResultReceiver = new NearByCameraResultReceiver(new Handler());
        nearByCameraResultReceiver.setNearByCameraReceiver(this);
        setLocationCallback(locationCallback);
    }

    public enum MarkerIconType {
        ICON_TYPE_ON_ROUTE,
        ICON_TYPE_START_ADDRESS,
        ICON_TYPE_END_ADDRESS,
        ICON_TYPE_OUT_ROUTE,
        ICON_TYPE_SELECTED,
        ICON_TYPE_DEFAULT
    }
}
