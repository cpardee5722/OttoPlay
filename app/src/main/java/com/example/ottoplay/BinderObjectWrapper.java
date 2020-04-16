package com.example.ottoplay;

import android.os.Binder;

//from https://stackoverflow.com/questions/2736389/how-to-pass-an-object-from-one-activity-to-another-on-android
public class BinderObjectWrapper extends Binder {
    private final Object objData;

    public BinderObjectWrapper(Object data) {
        objData = data;
    }

    public Object getData() {
        return objData;
    }
}
