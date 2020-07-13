package com.adriano.cartoon.pojo.camera;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CameraLocalPOJO
{
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("IPv4")
    @Expose
    private String iPv4;
    @SerializedName("Port")
    @Expose
    private Integer port;
    @SerializedName("StreamName")
    @Expose
    private String streamName;
    @SerializedName("Longitude")
    @Expose
    private Double longitude;
    @SerializedName("Latitude")
    @Expose
    private Double latitude;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIPv4() {
        return iPv4;
    }

    public void setIPv4(String iPv4) {
        this.iPv4 = iPv4;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
}
