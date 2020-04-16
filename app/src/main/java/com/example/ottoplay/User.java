package com.example.ottoplay;

import java.util.ArrayList;

public class User {
    private String username;
    private int userId;
    private ArrayList<Integer> friendList;
    private ArrayList<Playlist> syncedPlaylists;
    private ArrayList<Playlist> sharedPlaylist;
    private DynamicWaypoint dynamicWp;
    private ArrayList<StaticWaypoint> staticWpList;
    //TODO
    //private WaypointMap wpMap;

    private boolean discoverEnabled;
    private boolean locationEnabled;
    private boolean admin;

    public User(String username, int userId) {
        this.username = username;
        this.userId = userId;
    }

    public DynamicWaypoint getDynamicWaypoint() {
        return dynamicWp;
    }

    public void setDynamicWaypoint(int wpId, String wpName) {
        dynamicWp = new DynamicWaypoint(wpId, userId, wpName);
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUserId(int id) {
        this.userId = id;
    }

    public int getUserId() {
        return userId;
    }

    public void addToFriendList(int friendId) {
        friendList.add(friendId);
    }

    public void removeFriend(int friendId) {
        friendList.remove(friendId);
    }

    public boolean isFriend(int userId) {
        return friendList.contains(userId);
    }

    public void addToSharedPlaylist(Playlist playlist) {
        sharedPlaylist.add(playlist);
    }

    public void addToSyncedPlaylist(Playlist playlist) {
        syncedPlaylists.add(playlist);
    }

    public void removeFromSharedPlaylist(Playlist playlist) {
        syncedPlaylists.remove(playlist);
    }

    public boolean getDiscoverVal() {
        return discoverEnabled;
    }

    public void addStaticWaypoint(StaticWaypoint sw) {
        staticWpList.add(sw);
    }

    public void removeStaticWaypoint(StaticWaypoint sw) {
        staticWpList.remove(sw);
    }

    public void enableDiscover() {
        discoverEnabled = true;
    }

    public void disableDiscover() {
        discoverEnabled = false;
    }

    public boolean getLocationVal() {
        return locationEnabled;
    }

    public void enableLocation() {
        locationEnabled = true;
    }

    public void disableLocation() {
        locationEnabled = false;
    }
}

