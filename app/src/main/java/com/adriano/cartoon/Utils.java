package com.adriano.cartoon;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.adriano.cartoon.fragments.AndroidThemePreference;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class Utils {

    public static boolean isDarkModeEnabled(Context context) {
        int currentNightMode;
        currentNightMode = context.getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }

    public static void setThemeFromSharedPreferences(SharedPreferences sharedPreferences, Resources resources) {
        String[] darkModeValues = resources.getStringArray(R.array.theme_values);
        String darkModeString;
        String darkModeValue;
        AndroidThemePreference androidThemePreference;
        darkModeString = resources.getString(R.string.key_dark_mode_preference);
        darkModeValue = sharedPreferences.getString(darkModeString,darkModeValues[0]);
        androidThemePreference = AndroidThemePreference.valueOf(darkModeValue);
        switch (androidThemePreference) {
            case MODE_NIGHT_YES:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case MODE_NIGHT_NO:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case MODE_NIGHT_FOLLOW_SYSTEM:
            case MODE_NIGHT_FOLLOW_BATTERY_SAVER:
            default:
                if( Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                }
                break;
        }
    }

    public static String getLatLng(LatLng position) {
        StringBuilder result = new StringBuilder();
        result.append(position.latitude);
        result.append(",");
        result.append(position.longitude);
        return result.toString();
    }

    public static String getLngLat(LatLng position) {
        StringBuilder result = new StringBuilder();
        result.append(position.longitude);
        result.append(",");
        result.append(position.latitude);
        return result.toString();
    }

    public static int roundUp(int num, int divisor) {
        return (num + divisor - 1) / divisor;
    }

    public static boolean hasPermission(Context context,String permission) {
        return ContextCompat.checkSelfPermission(context,permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasGPSPermission(Context context) {
        return hasPermission(context,Manifest.permission.ACCESS_FINE_LOCATION)
                && hasPermission(context,Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    public static void displayWarning(String message, Context context) {
        MaterialAlertDialogBuilder materialAlertDialogBuilder;
        materialAlertDialogBuilder = new MaterialAlertDialogBuilder(context);
        materialAlertDialogBuilder.setTitle(R.string.title_warning_dialog);
        materialAlertDialogBuilder.setMessage(message);
        materialAlertDialogBuilder.setPositiveButton(android.R.string.ok, null);
        materialAlertDialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        materialAlertDialogBuilder.show();
    }
}
