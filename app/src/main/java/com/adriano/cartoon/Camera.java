package com.adriano.cartoon;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

public class Camera implements Parcelable {
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Camera> CREATOR = new Parcelable.Creator<Camera>() {
        @Override
        public Camera createFromParcel(Parcel in) {
            return new Camera(in);
        }

        @Override
        public Camera[] newArray(int size) {
            return new Camera[size];
        }
    };
    public String name;
    public LatLng position;
    public String ip;
    public String streamName;
    public int port;
    public boolean enabled;  // This controls whether the camera object is selectable or not.
    public boolean selected;

    protected Camera(Parcel in) {
        name = in.readString();
        position = (LatLng) in.readValue(LatLng.class.getClassLoader());
        ip = in.readString();
        port = in.readInt();
        streamName = in.readString();
        enabled = in.readByte() != 0x00;
        selected = in.readByte() != 0x00;
    }

    public Camera(String name, LatLng position, String ip, int port, String streamName) {
        this.name = name;
        this.position = position;
        this.ip = ip;
        this.port = port;
        this.streamName = streamName;
        this.enabled = true;
        this.selected = false;
    }

    public Camera() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeValue(position);
        dest.writeString(ip);
        dest.writeInt(port);
        dest.writeString(streamName);
        dest.writeByte((byte) (enabled ? 0x01 : 0x00));
        dest.writeByte((byte) (selected ? 0x01 : 0x00));
    }

    public void fromJSON(JSONObject jsonObject) {
        double latitude;
        double longitude;
        try {
            name = jsonObject.getString("Name");
            latitude = Double.parseDouble(jsonObject.getString("Latitude"));
            longitude = Double.parseDouble(jsonObject.getString("Longitude"));
            ip = jsonObject.getString("IPv4");
            port = jsonObject.getInt("Port");
            streamName = jsonObject.getString("StreamName");
            position = new LatLng(latitude, longitude);
            enabled = true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
