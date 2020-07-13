package com.adriano.cartoon.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.adriano.cartoon.Camera;
import com.adriano.cartoon.Constants;
import com.adriano.cartoon.R;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class NearbyMapFragment extends BaseMapFragment implements OnCheckedChangeListener,
        View.OnClickListener, NearByCameraReceiver {

    private static final String GPS_TRACK_BUNDLE_KEY = "GPS_TRACK_STATUS";
    private static final int GPS_ENABLE_LOCATION_REQUEST = 100;

    private ToggleFloatingActionButton trackGPSToggle;
    private FloatingActionButton showNearbyCameraButton;
    private ArrayList<Camera> cameraArrayList = new ArrayList<>();
    private ArrayList<Camera> nearbyCameraArrayList = new ArrayList<>();
    private LatLng currentPosition;
    private NearByCameraResultReceiver nearByCameraResultReceiver;
    private boolean restoreSavedState;
    private LocationCallback locationCallback = new LocationCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            Timber.d("Got locationResult!");
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);
                //move map camera
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                currentPosition = latLng;
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                spawnRadiusCameraSearchService(currentPosition, getSearchRadiusFromPreference(), nearByCameraResultReceiver);
//                if( !map.isMyLocationEnabled() ) {
//                    map.setMyLocationEnabled(true);
//                }
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GPS_ENABLE_LOCATION_REQUEST) {
            //Make sure user doesn't bypass our check.
            if (isLocationEnabled()) {
//                setGPSTrack(true);
                trackGPSToggle.setChecked(true);
            } else {
                showLocationWarning();
            }
        }
    }


    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCode == NearByCameraService.NEAR_BY_CAMERA_SERVICE_RESULT_CODE) {
            ArrayList<Camera> cameraArrayList;
            int status;
            status = resultData.getInt(NearByCameraService.NEAR_BY_CAMERA_SERVICE_RESULT_STATUS_KEY);
            if (status == -1) {
                displayWarning(R.string.camera_request_failed);
                trackGPSToggle.setChecked(false);
//                setGPSTrack(false);
                return;
            }
            map.clear();
            drawCircle(currentPosition, getSearchRadiusFromPreference(), Constants.COLOR_BLUE);
            cameraArrayList = resultData.getParcelableArrayList(NearByCameraService.NEAR_BY_CAMERA_SERVICE_RESULT_KEY);
            nearbyCameraArrayList.clear();
            nearbyCameraArrayList.addAll(cameraArrayList);
            if (nearbyCameraArrayList.size() > 0) {
                showNearbyCameraButton.setEnabled(true);
            }
            for (Camera camera : nearbyCameraArrayList) {
                map.addMarker(getDefaultMakerOptions(camera));
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == showNearbyCameraButton.getId()) {
            Timber.d("Showing " + nearbyCameraArrayList.size() + " cameras.");
            FragmentManager fragmentManager = getParentFragmentManager();
            VideoPlayerHolderDialogFragment.PrepareFragmentManager(fragmentManager);
            VideoPlayerHolderDialogFragment newDialogFragment =
                    VideoPlayerHolderDialogFragment.newInstance(nearbyCameraArrayList);
            newDialogFragment.setTargetFragment(this, Constants.DIALOG_REQUEST_CODE);
            newDialogFragment.show(fragmentManager, "videoDialog");
            return;
        }
    }

//    private void CheckNearbyCamera() {
//        //TODO:Add a timestamp if too much time has elapsed request a new copy from the server!
//        if( cameraArrayList.isEmpty() ) {
//            RequestCameraList();
//            return;
//        }
//        nearbyCameraArrayList.clear();
//        for( Camera camera : cameraArrayList ) {
//            float[] results = new float[1];
//            Location.distanceBetween(currentPosition.latitude,currentPosition.longitude,
//                    camera.position.latitude,camera.position.longitude,results);
//            if( results[0] < GetRadiusFromPreference() ) {
//                map.addMarker(GetDefaultMakerOptions(camera));
//                nearbyCameraArrayList.add(camera);
//            }
//        }
//        if( nearbyCameraArrayList.size() > 0 ) {
//            showNearbyCameraButton.setEnabled(true);
//        }
//    }

    private MarkerOptions getDefaultMakerOptions(Camera camera) {
        MarkerOptions defaultOptions = new MarkerOptions();
        defaultOptions.title(camera.name);
        defaultOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        defaultOptions.position(camera.position);
        return defaultOptions;
    }

//    private boolean hasPermission(String permission) {
//        return ContextCompat.checkSelfPermission(getContext(),permission) == PackageManager.PERMISSION_GRANTED;
//    }
//
//    private void requestPermission(String[] permissions) {
//        requestPermissions(permissions,REQUEST_LOCATION_PERMISSION );
//    }

    //Explain why we need to access position!
//    private void displayPermissionRequestReasonDialog(String[] permissionRequestList) {
//        AlertDialog.Builder dialogBuilder;
//        dialogBuilder = new AlertDialog.Builder(getContext());
//        dialogBuilder.setTitle(R.string.title_warning_dialog);
//        dialogBuilder.setMessage(getString(R.string.permission_request_explanation));
//        dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                requestPermission(permissionRequestList);
//            }
//        });
//        dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
//        dialogBuilder.show();
//    }

    //    private void askPositionAccessPermission() {
//        String[] permissionRequestList = {
//                Manifest.permission.ACCESS_COARSE_LOCATION,
//                Manifest.permission.ACCESS_FINE_LOCATION
//        };
//        if( hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
//                && hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ) {
//            Timber.d("User already has permission to access location...");
//            return;
//        }
//        if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
//                Manifest.permission.ACCESS_FINE_LOCATION)) {
//            displayPermissionRequestReasonDialog(permissionRequestList);
//        } else {
//            ActivityCompat.requestPermissions(getActivity(),
//                    permissionRequestList,
//                    REQUEST_LOCATION_PERMISSION );
//        }
//    }
    @Override
    public void onPause() {
        super.onPause();
        Timber.d("onPause");
    }

    @Override
    public void onCheckedChange(ToggleFloatingActionButton toggleFloatingActionButton, boolean isChecked) {
        //TODO:Since our problem is just during orientation changes just check if we
        //     are in restoreState mode.
        /*
        * toggleFloatingActionButton.findViewById(trackGPSToggle.getId()).isPressed() &&
                toggleFloatingActionButton.getId() == trackGPSToggle.getId()
        * */
        //We have to keep this flag here otherwise it could lead to crashes since map
        //on configuration changes could not be ready...
        if (toggleFloatingActionButton.getId() == trackGPSToggle.getId() &&
                !restoreSavedState) {
            setGPSTrack(isChecked);
        }
    }

    private void showLocationWarning() {
//        String message;
//        message = String.format(getResources().getString(R.string.location_not_enabled));
//        AlertDialog.Builder dialogBuilder;
//        dialogBuilder = new AlertDialog.Builder(getContext());
//        dialogBuilder.setTitle(R.string.title_warning_dialog);
//        dialogBuilder.setMessage(message);
//        dialogBuilder.setPositiveButton(android.R.string.ok, (dialog, which) ->
//                startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS),
//                        GPS_ENABLE_LOCATION_REQUEST));
//        dialogBuilder.setNegativeButton(android.R.string.no, (dialog, which) -> {
//            //We need to do this because we check if toggle has been pressed by user or not...
//            //and by calling setchecked manually the listener won't be triggered.
//            setGPSTrack(false);
//        });
//        dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
//        dialogBuilder.show();
    }

//    private void requestLocationUpdates() {
//        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
//        map.setMyLocationEnabled(true);
//    }

    private boolean isLocationEnabled() {
        final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void setGPSTrack(boolean enabled) {
        if (enabled) {
//            if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
//                    != PackageManager.PERMISSION_GRANTED) {
//                //User hasn't given permission...
//                trackGPSToggle.setChecked(false);
//                return;
//            }
//                Timber.d("Need to ask for permission.");
//                askPositionAccessPermission();
//            } else {
            requestLocationUpdates();
//            }
            Timber.d("GPS track on!");
        } else {
            Timber.d("Disabled it");
            stopLocationUpdates();
            nearbyCameraArrayList.clear();
            showNearbyCameraButton.setEnabled(false);
            map.clear();
        }
    }

//    private void RequestCameraList() {
//        CameraListLocalRequestAsyncTask cameraListLocalRequestAsyncTask =
//                new CameraListLocalRequestAsyncTask(R.id.get_camera_from_server, this);
////        if( currentPosition != null ) {
////            cameraListLocalRequestAsyncTask.EnableRadiusSearch(currentPosition,GetRadiusFromPreference());
////        }
//        cameraListLocalRequestAsyncTask.execute();
//    }

    private void initComponents(View view) {
        trackGPSToggle = view.findViewById(R.id.GPSTrackToggleButton);
        trackGPSToggle.setOnCheckedChangeListener(this);

        showNearbyCameraButton = view.findViewById(R.id.ShowNearbyVideoCameraButton);
        showNearbyCameraButton.setEnabled(false);
        showNearbyCameraButton.setOnClickListener(this);
    }


//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        boolean permissionGranted = true;
//        Timber.d("requestPermissionResult: code: " + requestCode);
//        if( requestCode == REQUEST_LOCATION_PERMISSION ) {
//            if( grantResults.length > 0 ) {
//                for( int i = 0; i < grantResults.length; i++ ) {
//                    if( grantResults[i] != PackageManager.PERMISSION_GRANTED ) {
//                        permissionGranted = false;
//                        break;
//                    }
//                }
//                if( !permissionGranted ) {
//                    displayWarning(R.string.permission_not_granted);
////                    setGPSTrack(false);
//                    trackGPSToggle.setChecked(false);
//                } else {
//                    Timber.d("Permission was granted!");
//                    requestLocationUpdates();
//                }
//            }
//        }
//    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
        if (restoreSavedState) {
            setGPSTrack(trackGPSToggle.isChecked());
            restoreSavedState = false;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
//        outState.putBoolean(GPS_TRACK_BUNDLE_KEY,trackGPSToggle.isChecked());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nearByCameraResultReceiver = new NearByCameraResultReceiver(new Handler());
        nearByCameraResultReceiver.setNearByCameraReceiver(this);
        if (savedInstanceState != null) {
            restoreSavedState = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        MapView mapView;
        view = inflater.inflate(R.layout.fragment_nearby_map, container, false);
        mapView = view.findViewById(R.id.NearbyMapView);
        initMapView(mapView, savedInstanceState);
        initComponents(view);
        setLocationCallback(locationCallback);
//        initLocation();
        //TODO:Check if GPS position is enabled otherwise ask user to enable it by spawning
        //an intent to bring him at the right settings screen!
        return view;
    }
}
