package com.example.ottoplay;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class PlaylistContentsActivity extends AppCompatActivity {

    private MyApplication app;
    private Playlist playlist;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist_contents);
        app = (MyApplication) getApplication();
        playlist = app.getPlaylist();

    }
}
