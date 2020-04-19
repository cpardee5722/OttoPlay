package com.example.ottoplay;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class WaypointMapTestingSetupActivity extends AppCompatActivity {
    private ArrayList<ArrayList<String>> queryResults;
    private String username = "jgperra";
    private MyApplication app;

    private ReentrantLock connectorLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            app = (MyApplication) getApplication();
            connectorLock = new ReentrantLock();
            app.setLock(connectorLock);

            //get intial user data
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



            app.setUser(user);


        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(WaypointMapTestingSetupActivity.this, WaypointMapActivity.class);
        WaypointMapTestingSetupActivity.this.startActivity(intent);
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



