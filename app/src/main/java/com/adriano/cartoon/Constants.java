package com.adriano.cartoon;

public class Constants {

    public static final String MAP_BROWSER_API_KEY = "AIzaSyBNBrYcTPGIDjhW-HTVUVqyrxRiu8N2mP0";
    public static final String OPEN_ROUTE_SERVICE_API_KEY = "5b3ce3597851110001cf6248d82f0925941d4135a1ce70d373d52748";
    public static final String LOCAL_SERVER_ADDRESS = "http://192.168.1.137/";
    public static final double DEFAULT_MAPS_TOLERANCE = 10; //Tolerance in Meters
    public static final String DIALOG_EXIT_CODE = "DialogExitCode";
    public static final int DIALOG_REQUEST_CODE = 1;
    public static final int MAX_CAMERA_ROW_NUMBER = 2;
    public static final int MAX_CAMERA_COLUMN_NUMBER = 2;
    public static final int MAX_CAMERA_NUMBER_IN_GRID = MAX_CAMERA_COLUMN_NUMBER * MAX_CAMERA_ROW_NUMBER;
    public static final int NEARBY_DEFAULT_SEARCH_RANGE = 50;
    public static final int COLOR_BLUE = 0x996200EE;
    public static final int NEARBY_DEFAULT_CAMERA_NUMBER = 10;

    /*
    * Prevent instancing it.
    * */
    private Constants() {
    }
}
