package com.example.ottoplay.Profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ottoplay.ClassDiagrams.Playlist;
import com.example.ottoplay.ClassDiagrams.StaticWaypoint;
import com.example.ottoplay.ClassDiagrams.User;
import com.example.ottoplay.ClassDiagrams.Waypoint;
import com.example.ottoplay.DatabaseConnector;
import com.example.ottoplay.MyApplication;
import com.example.ottoplay.R;
import com.example.ottoplay.WaypointMap.WaypointPlaylistsFromMapActivity;
import com.example.ottoplay.WaypointMap.waypointSettingsActivity;
import com.google.android.gms.maps.model.Marker;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class MyWaypointsListItem extends AppCompatActivity {

    private androidx.appcompat.widget.Toolbar toolbar;
    private ArrayList<ArrayList<String>> queryResults;
    private HashMap<String, Pair<Integer,String>> playlistIds;
    private MyApplication app;
    private HashMap<Waypoint.Genre, ArrayList<Marker>> staticWaypointsByGenre;

    private StaticWaypoint swp;

    @Override
    public void onRestart() {
        super.onRestart();
        finish();
        startActivity(this.getIntent());
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mywaypoints_listitem);

        ImageView iv = (ImageView) findViewById(R.id.ivProfile);
        iv.setRotation(90);

        Intent intent = getIntent();
        int position = intent.getIntExtra("position", 0);
        ArrayList<String> waypoint_name = getIntent().getStringArrayListExtra("waypoint_name");
//        friend_name = getIntent().getStringArrayListExtra("friend_name");

        // Here we turn your string.xml in an array
//        String[] myKeys = getResources().getStringArray(R.array.sections);
//        System.out.println("friendnameposition"+waypoint_name.get(position));
        TextView myTextView = (TextView) findViewById(R.id.waypointname);
        myTextView.setText(waypoint_name.get(position));

        app = (MyApplication) getApplication();
        User user = app.getUser();

        ArrayList<StaticWaypoint> wps = user.getStaticWaypoints();
        StaticWaypoint wpTemp = new StaticWaypoint();
        for (int i = 0; i < wps.size(); i++) {
            System.out.println("NAME = " + wps.get(i).getWaypointName());
            if (wps.get(i).getWaypointName().compareTo(waypoint_name.get(position)) == 0) {
                System.out.println("EQUAL");
                wpTemp = wps.get(i);
            }
        }

        final StaticWaypoint wp = wpTemp;
        swp = wp;
        getGenres(wp);
        app.setWaypoint(wp);

        playlistIds = new HashMap<>();
        ArrayList<String> playlistList = getWaypointPlaylists(wp);
        if (playlistIds == null) playlistIds = new HashMap<>();
        app.setPlaylistIds(playlistIds);
        final ListView playlists = (ListView) findViewById(R.id.playlistsMyWaypoint);

        toolbar = findViewById(R.id.toolBarTop);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Waypoint Details");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, playlistList);
        playlists.setAdapter(arrayAdapter);
        playlists.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        final int currentPosition = -1;
        playlists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //TODO SHOW PLAYLIST CONTENTS
                //playlists.setSelector(R.color.pressed_color);
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),MyWaypoints.class));
            }
        });
    }

    public void settingsButtonClicked(View v) {
        Intent intent = new Intent(MyWaypointsListItem.this, waypointSettingsActivity.class);
        MyWaypointsListItem.this.startActivity(intent);
    }

    public void deleteButtonClicked(View v) {
        app.getUser().removeStaticWaypoint(swp);
        Thread t = new Thread(new RemoveWaypointThread(swp));
        t.start();
        try {
            t.join();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        this.onBackPressed();
    }

    class RemoveWaypointThread implements Runnable {
        Waypoint wp;
        RemoveWaypointThread(Waypoint wp) {
            this.wp = wp;
        }

        @Override
        public void run() {
            DatabaseConnector dbc = new DatabaseConnector(app.getLock());
            queryResults = dbc.requestData("54:" + Integer.toString(wp.getGlobalId()));
        }
    }

    private void getGenres(Waypoint wp) {
        Thread t = new Thread(new GetWaypointGenresFromDBThread(wp));
        t.start();
        try {
            t.join();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < queryResults.size(); i++) {
            wp.addGenre(Waypoint.Genre.valueOf(queryResults.get(i).get(0)));
        }

    }

    class GetWaypointGenresFromDBThread implements Runnable {
        Waypoint wp;
        GetWaypointGenresFromDBThread(Waypoint wp) {
            this.wp = wp;
        }

        @Override
        public void run() {
            DatabaseConnector dbc = new DatabaseConnector(app.getLock());
            queryResults = dbc.requestData("34:" + Integer.toString(wp.getGlobalId()));
        }
    }

    class GetWaypointPlaylistsFromDBThread implements Runnable {
        int wpId;

        GetWaypointPlaylistsFromDBThread(int id) {
            wpId = id;
        }

        @Override
        public void run() {
            DatabaseConnector dbc = new DatabaseConnector(app.getLock());
            queryResults = dbc.requestData("9:" + Integer.toString(wpId));
        }
    }

    public ArrayList<String> getWaypointPlaylists(Waypoint wp) {
        ArrayList<String> playlistNames = new ArrayList<>();
        try {
            Thread t = new Thread(new GetWaypointPlaylistsFromDBThread(wp.getGlobalId()));
            t.start();
            t.join();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        wp.clearPlaylists();
        for (int i = 0; i < queryResults.size(); i++) {
            playlistIds.put(queryResults.get(i).get(1), new Pair<Integer,String>(Integer.parseInt(queryResults.get(i).get(0)), queryResults.get(i).get(2)));
            playlistNames.add(queryResults.get(i).get(1));
            wp.addPlaylist(new Playlist(queryResults.get(i).get(2), Integer.parseInt(queryResults.get(i).get(0)), queryResults.get(i).get(1)));
        }

        queryResults.clear();
        return playlistNames;
    }

}
