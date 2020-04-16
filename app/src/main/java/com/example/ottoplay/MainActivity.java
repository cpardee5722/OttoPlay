package com.example.ottoplay;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ArrayList<ArrayList<String>> queryResults;
    private String username = "jgperra";
    MyApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            queryResults = new ArrayList<>();
            Thread t = new Thread(new GetUserIdDBThread());
            t.start();
            t.join();


            User user = null;
            if (!queryResults.isEmpty()) {
                user = new User(username, Integer.parseInt(queryResults.get(0).get(0)));
            }

            queryResults.clear();
            t = new Thread(new GetUserDynamicWaypointDBThread(user));
            t.start();

            t.join();


            if (!queryResults.isEmpty()) {
                user.setDynamicWaypoint(Integer.parseInt(queryResults.get(0).get(0)), queryResults.get(0).get(1));
            }

            app = (MyApplication) getApplication();
            app.setUser(user);

        }
        catch (Exception e) {
            e.printStackTrace();
        }




        Intent intent = new Intent(MainActivity.this, WaypointMapActivity.class);
        MainActivity.this.startActivity(intent);
    }

    class GetUserIdDBThread implements Runnable {
        @Override
        public void run() {
            DatabaseConnector dbc = new DatabaseConnector();
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
            DatabaseConnector dbc = new DatabaseConnector();
            queryResults = dbc.requestData("48:" + Integer.toString(user.getUserId()));
        }
    }


}



