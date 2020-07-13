package com.adriano.cartoon.services;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class NearByCameraResultReceiver extends ResultReceiver {

    private NearByCameraReceiver nearByCameraReceiver;
    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if( nearByCameraReceiver != null ) {
            nearByCameraReceiver.onReceiveResult(resultCode,resultData);
        }
    }

    public void setNearByCameraReceiver(NearByCameraReceiver nearByCameraReceiver) {
        this.nearByCameraReceiver = nearByCameraReceiver;
    }

    public NearByCameraResultReceiver(Handler handler) {
        super(handler);
    }
}
