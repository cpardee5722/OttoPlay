package com.example.ottoplay;

import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class WaypointPlaylistsFromMapActivity extends AppCompatActivity {

    private HashMap<String, Pair<Integer,String>> playlistIds;
    private ArrayList<ArrayList<String>> queryResults;
    private Waypoint wp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waypoint_playlists_from_map);

        Object obj = ((BinderObjectWrapper)getIntent().getExtras().getBinder("obj_val")).getData();
        wp = (Waypoint) obj;

        queryResults = new ArrayList<>();
        String ownerUsername = getWaypointOwnerUsername(wp);

        playlistIds = new HashMap<>();
        ArrayList<String> playlistList = getWaypointPlaylists(wp);


        TextView title = (TextView) findViewById(R.id.title);
        title.setText(wp.getWaypointName());

        TextView username = (TextView) findViewById(R.id.username);
        username.setText(ownerUsername);

        final Button addToPlaylistsButton = (Button) findViewById(R.id.addPlaylistButton);
        addToPlaylistsButton.setEnabled(false);

        final ListView playlists = (ListView) findViewById(R.id.playlists);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, playlistList);
        playlists.setAdapter(arrayAdapter);
        playlists.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        final int currentPosition = -1;
        playlists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO
                //do not enable this button if this waypoint belongs to the current user
                addToPlaylistsButton.setEnabled(true);
                String selected = (String) playlists.getItemAtPosition(position);
                addToPlaylistsButton.setTag((Object) selected);
                playlists.setSelector(R.color.pressed_color);
            }
        });

        addToPlaylistsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String selected = (String) addToPlaylistsButton.getTag();
                addToPlaylistsButton.setEnabled(false);
                playlists.setAdapter(arrayAdapter);//unselects item
                addWaypointPlaylistToMyPlaylists(playlistIds.get(selected));
            }
        });
    }

    private void addWaypointPlaylistToMyPlaylists(Pair<Integer, String> playlistData) {
        try {
            Thread t = new Thread(new addWaypointPlaylistToSharedPlaylistsThread(playlistData));
            t.start();
            t.join();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    class addWaypointPlaylistToSharedPlaylistsThread implements Runnable {
        Pair<Integer,String> playlistData;

        addWaypointPlaylistToSharedPlaylistsThread(Pair<Integer,String> p) {
            playlistData = p;
        }

        @Override
        public void run() {
            DatabaseConnector dbc = new DatabaseConnector();
            //TODO
            //user userId from current user rather than hardcoded value
            queryResults = dbc.requestData("17:" + Integer.toString(playlistData.first) + "," + wp.getOwnerUserId() + "," + "19");
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

        for (int i = 0; i < queryResults.size(); i++) {
            playlistIds.put(queryResults.get(i).get(1), new Pair<Integer,String>(Integer.parseInt(queryResults.get(i).get(0)), queryResults.get(i).get(2)));
            playlistNames.add(queryResults.get(i).get(1));
        }

        queryResults.clear();
        return playlistNames;
    }

    class GetWaypointPlaylistsFromDBThread implements Runnable {
        int wpId;

        GetWaypointPlaylistsFromDBThread(int id) {
            wpId = id;
        }

        @Override
        public void run() {
            DatabaseConnector dbc = new DatabaseConnector();
            queryResults = dbc.requestData("9:" + Integer.toString(wpId));
        }
    }

    private String getWaypointOwnerUsername(Waypoint wp) {
        try {
            Thread t = new Thread(new GetWaypointOwnerNameFromDBThread(wp.getOwnerUserId()));
            t.start();
            t.join();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        String res = queryResults.get(0).get(0);
        queryResults.clear();
        return res;
    }

    class GetWaypointOwnerNameFromDBThread implements Runnable {
        int userId;

        GetWaypointOwnerNameFromDBThread(int id) {
            userId = id;
        }

        @Override
        public void run() {
            DatabaseConnector dbc = new DatabaseConnector();
            queryResults = dbc.requestData("3:" + Integer.toString(userId));
        }
    }
}
