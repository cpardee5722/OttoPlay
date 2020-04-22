package com.example.ottoplay.WaypointMap;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ottoplay.ClassDiagrams.Playlist;
import com.example.ottoplay.ClassDiagrams.StaticWaypoint;
import com.example.ottoplay.ClassDiagrams.User;
import com.example.ottoplay.ClassDiagrams.Waypoint;
import com.example.ottoplay.DatabaseConnector;
import com.example.ottoplay.MyApplication;
import com.example.ottoplay.Profile.MainActivity;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class WaypointMapTestingSetupActivity extends AppCompatActivity {
    private ArrayList<ArrayList<String>> queryResults;
    private String username;
    private MyApplication app;

    private ReentrantLock connectorLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            app = (MyApplication) getApplication();
            connectorLock = new ReentrantLock();
            app.setLock(connectorLock);

            username = app.getLoginUsername();

            System.out.println("USER IS " + username);
            //get initial user data
            queryResults = new ArrayList<>();
            Thread t = new Thread(new GetUserIdDBThread());
            t.start();
            t.join();
            User user = null;
            if (!queryResults.isEmpty()) {
                user = new User(username, Integer.parseInt(queryResults.get(0).get(0)));
            }

            //get users dynamic waypoint
            queryResults.clear();
            t = new Thread(new GetUserDynamicWaypointDBThread(user));
            t.start();
            t.join();
            if (!queryResults.isEmpty()) {
                System.out.println("SET DYNAMIC WAYPOINT");
                user.setDynamicWaypoint(Integer.parseInt(queryResults.get(0).get(0)), queryResults.get(0).get(1));
            }

            //get users friend list
            queryResults.clear();
            t = new Thread(new GetUsersFriendsThread(user));
            t.start();
            t.join();
            if (!queryResults.isEmpty()) {
                for (int i = 0; i < queryResults.size(); i++) {
                    user.addToFriendList(Integer.parseInt(queryResults.get(i).get(0)));
                }
            }

            //get user's actual playlists
            queryResults.clear();
            t = new Thread(new GetUserPlaylistsThread(user));
            t.start();
            t.join();
            if (!queryResults.isEmpty()) {
                for (int i = 0; i < queryResults.size(); i++) {
                    user.addToSyncedPlaylist(new Playlist(queryResults.get(i).get(2), Integer.parseInt(queryResults.get(i).get(0)), queryResults.get(i).get(1)));
                }
            }


            //get user's shared playlists
            queryResults.clear();
            t = new Thread(new GetUserSharedPlaylistsThread(user));
            t.start();
            t.join();
            if (!queryResults.isEmpty()) {
                for (int i = 0; i < queryResults.size(); i++) {
                    user.addToSharedPlaylist(new Playlist(queryResults.get(i).get(2), Integer.parseInt(queryResults.get(i).get(0)), queryResults.get(i).get(1)));
                }
            }

            //get user's static waypoints
            queryResults.clear();
            t = new Thread(new GetUserStaticWaypointsThread(user));
            t.start();
            t.join();
            if (!queryResults.isEmpty()) {
                for (int i = 0; i < queryResults.size(); i++) {
                    if (queryResults.get(i).get(2).compareTo("STATIC") == 0) {
                        user.addStaticWaypoint(new StaticWaypoint(Integer.parseInt(queryResults.get(i).get(0)), user.getUserId(), queryResults.get(i).get(1),
                                Waypoint.EditingSetting.valueOf(queryResults.get(i).get(3)), Waypoint.VisibilitySetting.valueOf(queryResults.get(i).get(4)), queryResults.get(i).get(5)));
                    }
                }
            }

            app.setUserId(user.getUserId());

            app.setUser(user);

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        //Intent intent = new Intent(WaypointMapTestingSetupActivity.this, WaypointMapActivity.class);
        Intent intent = new Intent(WaypointMapTestingSetupActivity.this, MainActivity.class);
        WaypointMapTestingSetupActivity.this.startActivity(intent);
    }

    class GetUserStaticWaypointsThread implements Runnable {
        private User user;

        GetUserStaticWaypointsThread(User user) {
            this.user = user;
        }

        @Override
        public void run() {
            DatabaseConnector dbc = new DatabaseConnector(connectorLock);
            queryResults = dbc.requestData("5:" + Integer.toString(user.getUserId()));
        }
    }

    class GetUserPlaylistsThread implements Runnable {
        private User user;

        GetUserPlaylistsThread(User user) {
            this.user = user;
        }

        @Override
        public void run() {
            DatabaseConnector dbc = new DatabaseConnector(connectorLock);
            queryResults = dbc.requestData("7:" + Integer.toString(user.getUserId()));
        }
    }

    class GetUserSharedPlaylistsThread implements Runnable {
        private User user;

        GetUserSharedPlaylistsThread(User user) {
            this.user = user;
        }

        @Override
        public void run() {
            DatabaseConnector dbc = new DatabaseConnector(connectorLock);
            queryResults = dbc.requestData("11:" + Integer.toString(user.getUserId()));
        }
    }

    class GetUsersFriendsThread implements Runnable {
        private User user;

        GetUsersFriendsThread(User user) {
            this.user = user;
        }

        @Override
        public void run() {
            DatabaseConnector dbc = new DatabaseConnector(connectorLock);
            queryResults = dbc.requestData("30:" + Integer.toString(user.getUserId()));
        }
    }

    class GetUserIdDBThread implements Runnable {
        @Override
        public void run() {
            DatabaseConnector dbc = new DatabaseConnector(connectorLock);
            queryResults = dbc.requestData("4:" + username);
        }
    }

    class GetUserDynamicWaypointDBThread implements Runnable {
        private User user;

        GetUserDynamicWaypointDBThread(User user) {
            this.user = user;
        }
        @Override
        public void run() {
            DatabaseConnector dbc = new DatabaseConnector(connectorLock);
            queryResults = dbc.requestData("48:" + Integer.toString(user.getUserId()));
        }
    }
}



