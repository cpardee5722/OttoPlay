package com.example.ottoplay;

import android.app.Application;

public class MyApplication extends Application {
    private User user;

    @Override
    public void onCreate() {

        super.onCreate();
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
