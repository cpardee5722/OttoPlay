package com.example.ottoplay;

import android.app.Application;
import android.util.Pair;

import com.example.ottoplay.ClassDiagrams.Playlist;
import com.example.ottoplay.ClassDiagrams.User;
import com.example.ottoplay.ClassDiagrams.Waypoint;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

public class MyApplication extends Application {
    private User user;
    private Waypoint waypoint;
    private ReentrantLock lock;
    private Playlist playlist;
    private String loginUsername;
    private String location;
    boolean sharedPlaylist;
    boolean hidePlaylistDelete;

    //bandaid because Jimmy is dumb and forgot to put playlist IDs into Playlist class
    private HashMap<String, Pair<Integer,String>> playlistIds;

    //a user Id of an arbitrary user
    private int userId;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void setSharedPlaylist(boolean b) {
        sharedPlaylist = b;
    }

    public boolean getSharedPlaylist() {
        return sharedPlaylist;
    }

    public boolean getHidePlaylistDelete() {
        return hidePlaylistDelete;
    }

    public void setHidePlaylistDelete(boolean b) {
        hidePlaylistDelete = b;
    }

    public void setLocation(String l) {
        this.location = l;
    }


    public String getLocation() {
        return location;
    }

    public void setUserId(int id) { this.userId = id; }

    public int getUserId() { return userId; }

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

    public void setPlaylistIds(HashMap<String, Pair<Integer,String>> pl) {
        this.playlistIds = pl;
    }

    public HashMap<String, Pair<Integer,String>> getPlaylistIds() {
        return playlistIds;
    }
}
