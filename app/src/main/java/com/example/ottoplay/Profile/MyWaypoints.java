package com.example.ottoplay.Profile;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ottoplay.DatabaseConnector;
import com.example.ottoplay.MyApplication;
import com.example.ottoplay.R;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class MyWaypoints extends AppCompatActivity implements AdapterView.OnItemClickListener {
    MyApplication app;
    class DatabaseConnThread implements Callable {
        String q;
        public DatabaseConnThread(String val){q = val;}
        private String getQuery(){return q;}
        public ArrayList<ArrayList<String>> call() {
            DatabaseConnector dbc = new DatabaseConnector(app.getLock());
            ArrayList<ArrayList<String>> result;
            result = dbc.requestData(getQuery());

            //iterate through 2D array
            for (int i = 0; i < result.size(); i++) {
                for (int j = 0; j < result.get(i).size(); j++) {
                    System.out.println(result.get(i).get(j) + " ");
                }
                System.out.println();
            }
            return result;
        }
    }

    private ListView lv;
    ArrayList<String> waypoint_name = new ArrayList<String>();
    private androidx.appcompat.widget.Toolbar toolbar;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_waypoints);
        final TextView textChange = (TextView) findViewById(R.id.waypointIds);

        lv = (ListView) findViewById(R.id.mywaypoints);
        lv.setOnItemClickListener(this);

//        Get My waypoints from userid
        app = (MyApplication)getApplication();
        String id = Integer.toString(app.getUserId());

        FutureTask a;
        a = new FutureTask<>(new DatabaseConnThread("5:" + id));
        Thread t = new Thread(a);
        t.start();
        try {
            ArrayList<ArrayList<String>> b = (ArrayList<ArrayList<String>>) a.get();
            System.out.println(b);
            //System.out.println(b.get(0).get(1));
//            Storing waypoint names to display in listview
            for (int i=0;i<b.size();i++){
                if (b.get(i).get(2).compareTo("STATIC") == 0) waypoint_name.add(b.get(i).get(1));
            }
//            textChange.setText("Waypoint id: " + b.get(0).get(0));
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        Display my waypoints in ListView
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,waypoint_name);
        lv.setAdapter(arrayAdapter);

        toolbar = findViewById(R.id.toolBarTop);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
            }
        });
    }
    public void onItemClick(AdapterView<?> l, View v, int pos, long id) {
//        Clicking on each waypoint would lead to MyWaypointListItem.java (need to connect it to Waypoint settings)
//        Need to add object to get waypoint id if WaypointSettings needs to use it

        System.out.println(pos);
        System.out.println(waypoint_name.get(pos));
        Intent intent = new Intent();
        intent.putStringArrayListExtra("waypoint_name",waypoint_name);
        intent.setClass(this, MyWaypointsListItem.class);
        intent.putExtra("position", pos);
        startActivity(intent);
    }
}
