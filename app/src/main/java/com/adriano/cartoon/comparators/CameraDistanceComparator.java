package com.adriano.cartoon.comparators;

import com.adriano.cartoon.Camera;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.Comparator;

public class CameraDistanceComparator implements Comparator<Camera> {
    private LatLng point;

    public CameraDistanceComparator(LatLng point) {
        this.point = point;
    }
    @Override
    public int compare(Camera cameraA, Camera cameraB) {
        double aDist = SphericalUtil.computeDistanceBetween(cameraA.position,point);
        double bDist = SphericalUtil.computeDistanceBetween(cameraB.position,point);
        return (int) (aDist - bDist);
    }
}
