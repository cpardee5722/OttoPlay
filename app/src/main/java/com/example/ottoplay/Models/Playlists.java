package com.example.ottoplay.Models;

public class Playlists {
    private String id;
    private String name;
    private int total;

    public Playlists(String id, String name, int total) {
        this.name = name;
        this.id = id;
        this.total = total;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTotalPlaylist(){
        return total;
    }

    public void setTotalPlaylist(){
        this.total = total;
    }
}
