package com.example.ottoplay;

import java.util.ArrayList;

public class Playlist {
    //songs within playlist
    private ArrayList<String> songList;

    //spotify token corresponding to Spotify API
    private String spotifyToken;

    //ID of owner of playlist.
    private int ownerId;

    Playlist(String token, int id) {
        this.spotifyToken = token;
        this.ownerId = id;
    }

    //Load songs from Spotify API.
    public void loadSongs(String spotifyToken) {
        //TODO
    }

    //Get songs stored in ArrayList.
    public ArrayList<String> getSongs() {
        return songList;
    }

    public String getSpotifyToken() {
        return spotifyToken;
    }

    public void setSpotifyToken(String token) {
        this.spotifyToken = token;
    }

    //Get the id of the owner of the playlist.
    public int getOwnerId() {
        return ownerId;
    }

    //Set the id of the owner of the playlist.
    public void setOwnerId(int id) {
        this.ownerId = id;
    }
}


