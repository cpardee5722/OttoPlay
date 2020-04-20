package com.example.ottoplay;

import android.app.Application;
import java.util.concurrent.locks.ReentrantLock;

public class MyApplication extends Application {
    private User user;
    private Waypoint waypoint;
    private ReentrantLock lock;
    private Playlist playlist;
    private String loginUsername;

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

    public void setWaypoint(Waypoint waypoint) {
        this.waypoint = waypoint;
    }

    public Waypoint getWaypoint() {
        return waypoint;
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public void setLock(ReentrantLock lock) {
         this.lock = lock;
     }

    public Playlist getPlaylist() {
        return playlist;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    public void setLoginUsername(String name) {
        this.loginUsername = name;
    }

    public String getLoginUsername() {
        return loginUsername;
    }
}
