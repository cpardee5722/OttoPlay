package com.example.ottoplay.WaypointMap;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ottoplay.ClassDiagrams.DynamicWaypoint;
import com.example.ottoplay.ClassDiagrams.Playlist;
import com.example.ottoplay.ClassDiagrams.User;
import com.example.ottoplay.ClassDiagrams.Waypoint;
import com.example.ottoplay.DatabaseConnector;
import com.example.ottoplay.MyApplication;
import com.example.ottoplay.Profile.PlaylistSongsActivity;
import com.example.ottoplay.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

public class WaypointPlaylistsFromMapActivity extends AppCompatActivity {
    MyApplication app;
    private User currentUser;

    private HashMap<String, Pair<Integer,String>> playlistIds;
    private ArrayList<ArrayList<String>> queryResults;
    private Waypoint wp;

    private ReentrantLock connectorLock;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waypoint_playlists_from_map);

        app = (MyApplication) getApplication();
        currentUser = app.getUser();

        //Object obj = ((BinderObjectWrapper)getIntent().getExtras().getBinder("obj_val")).getData();
        //wp = (StaticWaypoint) obj;
        wp = app.getWaypoint();
        connectorLock = app.getLock();

        queryResults = new ArrayList<>();
        String ownerUsername = getWaypointOwnerUsername(wp);

        playlistIds = new HashMap<>();
        ArrayList<String> playlistList = getWaypointPlaylists(wp);
        if (playlistIds == null) playlistIds = new HashMap<>();
        app.setPlaylistIds(playlistIds);

        TextView title = (TextView) findViewById(R.id.title);
        title.setText(wp.getWaypointName());

        TextView username = (TextView) findViewById(R.id.username);
        username.setText(ownerUsername);

        Button editWaypointButton = (Button) findViewById(R.id.editWaypointButton);
        editWaypointButton.setEnabled(canUserEdit());

        final Button addToPlaylistsButton = (Button) findViewById(R.id.addPlaylistButton);
        addToPlaylistsButton.setEnabled(false);

        final Button viewPlaylistButton = (Button) findViewById(R.id.button3);
        viewPlaylistButton.setEnabled(false);

        final Button addFriendButton = (Button) findViewById(R.id.addFriend);
        if (wp.getOwnerUserId() == currentUser.getUserId()) addFriendButton.setEnabled(false);

        final ListView playlists = (ListView) findViewById(R.id.playlists);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, playlistList);
        playlists.setAdapter(arrayAdapter);
        playlists.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        playlists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO
                //do not enable this button if this waypoint belongs to the current user
                if (wp.getOwnerUserId() != currentUser.getUserId()) {
                    addToPlaylistsButton.setEnabled(true);
                }
                viewPlaylistButton.setEnabled(true);

                String selected = (String) playlists.getItemAtPosition(position);
                addToPlaylistsButton.setTag((Object) selected);
                viewPlaylistButton.setTag((Object) position);
                playlists.setSelector(R.color.pressed_color);
            }
        });

        viewPlaylistButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int position = (int) viewPlaylistButton.getTag();
                Playlist p = wp.getPlaylist(position);
                app.setPlaylist(p);
                Intent intent = new Intent(WaypointPlaylistsFromMapActivity.this, PlaylistSongsActivity.class);
                WaypointPlaylistsFromMapActivity.this.startActivity(intent);
            }
        });

        addToPlaylistsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String selected = (String) addToPlaylistsButton.getTag();
                addToPlaylistsButton.setEnabled(false);
                playlists.setAdapter(arrayAdapter);//unselects item
                addWaypointPlaylistToMyPlaylists(playlistIds.get(selected));
                viewPlaylistButton.setEnabled(false);
            }
        });

        addFriendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addFriend();
            }
        });

        editWaypointButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                WaypointPlaylistsFromMapActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(WaypointPlaylistsFromMapActivity.this, waypointSettingsActivity.class);
                        WaypointPlaylistsFromMapActivity.this.startActivity(intent);
                    }
                });
            }
        });
    }

    private void addFriend() {
        try {
            Thread t = new Thread(new CheckFriendThread());
            t.start();
            t.join();

            for (int i = 0; i < queryResults.size(); i++) {
                if (Integer.parseInt(queryResults.get(i).get(0)) == wp.getOwnerUserId()) {
                    Toast.makeText(WaypointPlaylistsFromMapActivity.this, "Friend already exists.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            queryResults.clear();

            t = new Thread(new AddFriendThread());
            t.start();
            t.join();
            queryResults.clear();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(WaypointPlaylistsFromMapActivity.this, "Friend added.",
                Toast.LENGTH_SHORT).show();
    }

    class CheckFriendThread implements Runnable {
        @Override
        public void run() {
            DatabaseConnector dbc = new DatabaseConnector(connectorLock);
            queryResults = dbc.requestData("30:" + Integer.toString(currentUser.getUserId()));
        }
    }

    class AddFriendThread implements Runnable {
        @Override
        public void run() {
            DatabaseConnector dbc = new DatabaseConnector(connectorLock);
            queryResults = dbc.requestData("31:" + Integer.toString(currentUser.getUserId()) + "," + Integer.toString(wp.getOwnerUserId()));
        }
    }



    private void addWaypointPlaylistToMyPlaylists(Pair<Integer, String> playlistData) {
        try {
            Thread t = new Thread(new CheckPlaylistAlreadyAddedThread());
            t.start();
            t.join();

            for (int i = 0; i < queryResults.size(); i++) {
                if (playlistData.first == Integer.parseInt(queryResults.get(i).get(0))) {
                    Toast.makeText(WaypointPlaylistsFromMapActivity.this, "Playlist already added.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            queryResults.clear();

            t = new Thread(new addWaypointPlaylistToSharedPlaylistsThread(playlistData));
            t.start();
            t.join();
            queryResults.clear();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(WaypointPlaylistsFromMapActivity.this, "Playlist Added.",
                Toast.LENGTH_SHORT).show();
    }

    class CheckPlaylistAlreadyAddedThread implements Runnable {
        @Override
        public void run() {
            DatabaseConnector dbc = new DatabaseConnector(connectorLock);
            queryResults = dbc.requestData("11:" + Integer.toString(currentUser.getUserId()));
        }
    }

    class addWaypointPlaylistToSharedPlaylistsThread implements Runnable {
        Pair<Integer,String> playlistData;

        addWaypointPlaylistToSharedPlaylistsThread(Pair<Integer,String> p) {
            playlistData = p;
        }

        @Override
        public void run() {
            DatabaseConnector dbc = new DatabaseConnector(connectorLock);
            queryResults = dbc.requestData("17:" + Integer.toString(playlistData.first) + "," + wp.getOwnerUserId() + "," + Integer.toString(currentUser.getUserId()));
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
            wp.addPlaylist(new Playlist(queryResults.get(i).get(2), Integer.parseInt(queryResults.get(i).get(0)), queryResults.get(i).get(1)));
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
            DatabaseConnector dbc = new DatabaseConnector(connectorLock);
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
            DatabaseConnector dbc = new DatabaseConnector(connectorLock);
            queryResults = dbc.requestData("3:" + Integer.toString(userId));
        }
    }

    private boolean canUserEdit() {
        if (wp instanceof DynamicWaypoint) return false;
        if (currentUser.getUserId() == wp.getOwnerUserId()) return true;

        Thread t = new Thread(new CanUserEditThread());
        t.start();
        try {
            t.join();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < queryResults.size(); i++) {
            if (currentUser.getUsername().compareTo(queryResults.get(i).get(0)) == 0) {
                return true;
            }
        }

        return false;
    }

    class CanUserEditThread implements Runnable {
        @Override
        public void run() {
            DatabaseConnector dbc = new DatabaseConnector(connectorLock);
            queryResults = dbc.requestData("23:" + Integer.toString(wp.getOwnerUserId()));
        }
    }
}
