package com.example.ottoplay.ClassDiagrams;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class Waypoint {
    public enum EditingSetting {
        SOLO,
        SHARED,
        WILD
    }

    public enum VisibilitySetting {
        PUBLIC,
        PRIVATE,
        HIDDEN
    }

    public enum Genre {
        HIPHOP,
        POP,
        JAZZ,
        BLUES,
        COUNTRY,
        ROCK,
        METAL,
        EDM
    }

    protected String waypointName;

    //username of owner of waypoint
    protected String ownerUsername;

    //userId of owner
    protected int ownerUserId;

    //unique ID of waypoint
    protected int globalId;

    //location of waypoint in coordinates
    protected String location;

    //playlists within waypoint
    protected ArrayList<Playlist> playlists;

    protected ArrayList<Genre> genres;
    protected VisibilitySetting visSetting;
    protected EditingSetting editSetting;

    //image of area waypoint is in
    protected Bitmap image;

    public Waypoint(){
        playlists = new ArrayList<>();
        genres = new ArrayList<>();
    };

    public String getWaypointName() {
        return waypointName;
    }

    public void setWaypointName(String wpName) {
        this.waypointName = wpName;
    }

    public int getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(int id) {
        this.ownerUserId = id;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String username) {
        this.ownerUsername = username;
    }

    public int getGlobalId() {
        return globalId;
    }

    public void setGlobalId(int id) {
        this.globalId = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String loc) {
        this.location = loc;
    }

    public void addPlaylist(Playlist playlist) {
        playlists.add(playlist);
    }

    public void removePlaylist(Playlist playlist) {
        playlists.remove(playlist);
    }

    public Playlist getPlaylist(int idx) {
        return playlists.get(idx);
    }

    public ArrayList<Playlist> getAllPlaylists() {
        return playlists;
    }

    public void addGenre(Genre genre) {
        genres.add(genre);
    }

    public void removeGenre(Genre genre) {
        genres.remove(genre);
    }

    public Genre getGenre(int idx) {
        return genres.get(idx);
    }

    public ArrayList<Genre> getAllGenres() {
        return genres;
    }

    public void setVisSetting(VisibilitySetting vs) {
        this.visSetting = vs;
    }

    public VisibilitySetting getVisSetting() {
        return visSetting;
    }

    public void setImage(Bitmap bm) {
        this.image = bm;
    }

    public Bitmap getImage() {
        return image;
    }
}
