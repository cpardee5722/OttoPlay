package com.example.ottoplay.ClassDiagrams;

import java.util.ArrayList;

public class Playlist {
    //songs within playlist
    private ArrayList<String> songList;

    private int playlistId;

    //spotify token corresponding to Spotify API
    private String spotifyToken;

    //ID of owner of playlist.
    private int ownerId;

    //Name of playlist
    private String playlistName;

    public Playlist(){};

    /*public Playlist(String token, int id, String name) {
        this.spotifyToken = token;
        this.ownerId = id;
        this.playlistName = name;
    }*/

    public Playlist(String token, int id, String name) {
        this.spotifyToken = token;
        this.playlistId = id;
        this.playlistName = name;
    }

    public void setPlaylistId(int id) {
        this.playlistId = id;
    }

    public int getPlaylistId() {
        return playlistId;
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

    //Get the name of of the playlist.
    public String getPlaylistName() {
        return playlistName;
    }

    //Set the id of the owner of the playlist.
    public void setOwnerId(int id) {
        this.ownerId = id;
    }
}
