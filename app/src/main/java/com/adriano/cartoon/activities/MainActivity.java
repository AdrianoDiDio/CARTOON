package com.adriano.cartoon.activities;


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.adriano.cartoon.BuildConfig;
import com.adriano.cartoon.R;
import com.adriano.cartoon.fragments.NearbyMapFragment;
import com.adriano.cartoon.fragments.RouteMapFragment;
import com.adriano.cartoon.fragments.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import timber.log.Timber;

/*
    2020 Adriano Di Dio
    TODO:Check permission here, not in fragment...take code from NearbyMapFragment!
*/

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private static final String ACTIVE_FRAGMENT_BUNDLE_KEY = "OldActiveFragment";
    private static final int GPS_ENABLE_LOCATION_REQUEST = 100;
    private static final int REQUEST_LOCATION_PERMISSION = 99;
    private BottomNavigationView bottomNavigation;
    private RouteMapFragment routeMapFragment;
    private NearbyMapFragment nearbyMapFragment;
    private SettingsFragment settingsFragment;
    private Fragment activeFragment;
    private AlertDialog alertDialog;
    private Bundle savedInstanceState;

    private BroadcastReceiver gpsSwitchStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Timber.d("Got it");
            if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
                Timber.d("Provider has changed...");
                checkIfLocationIsEnabled();
            }
        }
    };

    /*
     * Utility function to check whether the location services are enabled or not.
     * If they are enabled and we are already showing a dialog it gets cancelled
     * Otherwise it builds a new warning dialog and display it to the user.
     * */
    private void checkIfLocationIsEnabled() {
        if (!isLocationEnabled()) {
            //Make sure we don't overwrite out own dialog...
            if( alertDialog != null && alertDialog.isShowing() ) {
                return;
            }
            alertDialog = createLocationWarningDialog();
            alertDialog.show();
        } else {
            if (alertDialog != null) {
                alertDialog.dismiss();
            }
        }
    }

    private void requestPermission(String[] permissions) {
        requestPermissions(permissions,REQUEST_LOCATION_PERMISSION );
    }

    private void displayPermissionRequestReasonDialog(String[] permissionRequestList) {
        AlertDialog.Builder dialogBuilder;
        dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.title_warning_dialog);
        dialogBuilder.setMessage(getString(R.string.permission_request_explanation));
        dialogBuilder.setPositiveButton(android.R.string.ok, (dialog, which) -> requestPermission(permissionRequestList));
        dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        dialogBuilder.show();
    }

    private AlertDialog createLocationWarningDialog() {
        String Message;
        Message = String.format(getResources().getString(R.string.location_not_enabled));
        AlertDialog.Builder DialogBuilder;
        DialogBuilder = new AlertDialog.Builder(this);
        DialogBuilder.setTitle(R.string.title_warning_dialog);
        DialogBuilder.setMessage(Message);
        DialogBuilder.setPositiveButton(android.R.string.ok, (dialog, which) ->
                startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                        GPS_ENABLE_LOCATION_REQUEST));
        DialogBuilder.setNegativeButton(android.R.string.no, (dialog, which) ->
                finish());
        DialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        return DialogBuilder.create();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GPS_ENABLE_LOCATION_REQUEST) {
            //Make sure user doesn't bypass our check.
            checkIfLocationIsEnabled();
        }
    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        //Avoid changing if it's the same.
        if (bottomNavigation.getSelectedItemId() == menuItem.getItemId()) {
            return false;
        }
        switch (menuItem.getItemId()) {
            case R.id.action_search_by_route:
                switchFragment(routeMapFragment);
                break;
            case R.id.action_search_nearby:
                Timber.d("Search Nearby!");
                switchFragment(nearbyMapFragment);
                break;
            case R.id.action_settings:
                Timber.d("Opening settings!");
                switchFragment(settingsFragment);
                break;
        }
        return true;
    }

    private boolean isLocationEnabled() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private String getFragmentTagFromID(int Id) {
        String Tag;
        Tag = Integer.toString(Id);
        return Tag;
    }

    private void switchFragment(Fragment newFragment) {
        getSupportFragmentManager().beginTransaction().hide(activeFragment).show(newFragment).commit();
        activeFragment = newFragment;
    }

    private void initComponents() {
        bottomNavigation = findViewById(R.id.BottomNavigationBar);
        bottomNavigation.setSelectedItemId(R.id.action_search_by_route);
        bottomNavigation.setOnNavigationItemSelectedListener(this);
    }


    private void initFragmentManager() {

        getSupportFragmentManager().beginTransaction().add(R.id.FragmentContainer, settingsFragment,
                getFragmentTagFromID(R.id.SettingsFragment)).
                hide(settingsFragment).commit();
        getSupportFragmentManager().beginTransaction().
                add(R.id.FragmentContainer, nearbyMapFragment,
                        getFragmentTagFromID(R.id.NearbyFragment)).
                hide(nearbyMapFragment).commit();
        getSupportFragmentManager().beginTransaction().
                add(R.id.FragmentContainer, routeMapFragment,
                        getFragmentTagFromID(R.id.RouteFragment)).commit();
    }

    private void initFragments() {
        routeMapFragment = new RouteMapFragment();
        nearbyMapFragment = new NearbyMapFragment();
        settingsFragment = new SettingsFragment();
        activeFragment = routeMapFragment;
        initFragmentManager();
    }

    private void init() {
        if (savedInstanceState != null) {
            routeMapFragment = (RouteMapFragment) getSupportFragmentManager().findFragmentByTag(getFragmentTagFromID(R.id.RouteFragment));
            nearbyMapFragment = (NearbyMapFragment) getSupportFragmentManager().findFragmentByTag(getFragmentTagFromID(R.id.NearbyFragment));
            settingsFragment = (SettingsFragment) getSupportFragmentManager().findFragmentByTag(getFragmentTagFromID(R.id.SettingsFragment));
            activeFragment = getSupportFragmentManager().getFragment(savedInstanceState, ACTIVE_FRAGMENT_BUNDLE_KEY);
        } else {
            initFragments();
            checkIfLocationIsEnabled();
        }
        initComponents();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, ACTIVE_FRAGMENT_BUNDLE_KEY, activeFragment);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkIfLocationIsEnabled();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(gpsSwitchStateReceiver);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(gpsSwitchStateReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        if( savedInstanceState != null ) {
            this.savedInstanceState = savedInstanceState;
        }

//        if(!Utils.hasGPSPermission(this)) {
//            Timber.d("Need to ask for permission.");
//            askPositionAccessPermission();
//        } else {
            Timber.d("init.");
            init();
//        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder DialogBuilder;
        DialogBuilder = new AlertDialog.Builder(this);
        DialogBuilder.setTitle(R.string.exit_confirm_title);
        DialogBuilder.setMessage(R.string.exit_confirm_message);
        DialogBuilder.setPositiveButton(android.R.string.ok, (dialog, which) -> super.onBackPressed());
        DialogBuilder.setNegativeButton(android.R.string.no, (dialog, which) -> dialog.cancel());
        DialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        DialogBuilder.show();
    }
}
