package com.adriano.cartoon;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;

public class Utils {

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
        AlertDialog.Builder DialogBuilder;
        DialogBuilder = new AlertDialog.Builder(context);
        DialogBuilder.setTitle(R.string.title_warning_dialog);
        DialogBuilder.setMessage(message);
        DialogBuilder.setPositiveButton(android.R.string.ok, null);
        DialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        DialogBuilder.show();
    }
}
