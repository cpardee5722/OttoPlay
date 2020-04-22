package com.example.ottoplay.ClassDiagrams;

import java.util.ArrayList;
import java.util.HashSet;

public class User {
    private String username;
    private int userId;
    private ArrayList<Integer> friendList;
    private HashSet<Integer> friendSet;

    //These are my playlists that coome from spotify (Playlists Table in DB)
    private ArrayList<Playlist> syncedPlaylists;

    //these are playlists I got from other people's waypoints (Shared Playlists Table in DB)
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

        friendSet = new HashSet<>();
        friendList = new ArrayList<>();
        syncedPlaylists = new ArrayList<>();
        sharedPlaylist = new ArrayList<>();
        staticWpList = new ArrayList<>();
    }

    public DynamicWaypoint getDynamicWaypoint() {
        return dynamicWp;
    }

    public void setDynamicWaypoint(int wpId, String wpName) {
        dynamicWp = new DynamicWaypoint(wpId, userId, wpName);
        /*friendSet = new HashSet<>();
        friendList = new ArrayList<>();
        syncedPlaylists = new ArrayList<>();
        sharedPlaylist = new ArrayList<>();
        staticWpList = new ArrayList<>();*/
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
        friendSet.add(friendId);
        friendList.add(friendId);
    }

    public void removeFriend(int friendId) {
        friendSet.remove(friendId);
        friendList.remove(friendId);
    }

    public boolean isFriend(int userId) {
        return friendSet.contains(userId);
    }

    public void addToSharedPlaylist(Playlist playlist) {
        sharedPlaylist.add(playlist);
    }

    public ArrayList<Playlist> getSharedPlaylists() { return sharedPlaylist; }

    public void addToSyncedPlaylist(Playlist playlist) {
        syncedPlaylists.add(playlist);
    }

    public ArrayList<Playlist> getSyncedPlaylists() { return syncedPlaylists; }

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

    public ArrayList<StaticWaypoint> getStaticWaypoints() { return staticWpList; }

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

