package com.example.ottoplay.WaypointMap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Location;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.ottoplay.ClassDiagrams.StaticWaypoint;
import com.example.ottoplay.ClassDiagrams.User;
import com.example.ottoplay.ClassDiagrams.Waypoint;
import com.example.ottoplay.DatabaseConnector;
import com.example.ottoplay.MyApplication;
import com.example.ottoplay.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;


public class CreateWaypoint extends AppCompatActivity  {

    private MyApplication app;
    private User user;
    private StaticWaypoint waypoint;

    ArrayList<String> playlist_name = new ArrayList<String>();
    ArrayList<String> playlist_id = new ArrayList<String>();
    private androidx.appcompat.widget.Toolbar toolbar;

    DecimalFormat df = new DecimalFormat("#.#############");

    int PERMISSION_ID = 44;
    FusedLocationProviderClient mFusedLocationClient;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_waypoint);

        // ToolBar (Works with navigation drawer)

//        toolbar = findViewById(R.id.toolBarTop);
//
//        setSupportActionBar(toolbar);
//        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                startActivity(new Intent(getApplicationContext(), MainActivity.class));
//            }
//        });

        //Fetching current location of the user
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);;
        getLastLocation();

        //Instantiate waypoint object
        waypoint = new StaticWaypoint();

        app = (MyApplication) getApplication();

        //Database call for fetching user id
        FutureTask cc = new FutureTask<>(new DatabaseConnThread("33:"+app.getLoginUsername()));
        Thread t1 = new Thread(cc);
        t1.start();

        try {
            ArrayList<ArrayList<String>> usid = (ArrayList<ArrayList<String>>) cc.get();
            user = new User(app.getLoginUsername(), Integer.parseInt(usid.get(0).get(0)));
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @SuppressLint("MissingPermission")
    private void getLastLocation(){
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData();
                                } else {
                                    waypoint.setLocation(Double.toString(location.getLatitude()) + " " + Double.toString(location.getLongitude()));
                                    TextView waypoint_location = (TextView)findViewById(R.id.waypoint_location_text);
                                    waypoint_location.setText(waypoint.getLocation());
                                }
                            }
                        }
                );
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            //waypoint.setLocation(df.format(mLastLocation.getLatitude()) + " " + df.format(mLastLocation.getLongitude()));
            waypoint.setLocation(Double.toString(mLastLocation.getLatitude()) + " " + Double.toString(mLastLocation.getLongitude()));
            TextView waypoint_location = findViewById(R.id.waypoint_location_text);
            waypoint_location.setText(waypoint.getLocation());
        }
    };

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID
        );
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }
    }

    public void createWaypoint(View view) {

        //Fetch waypoint name
        EditText waypoint_name = findViewById(R.id.waypoint_name_text);
        waypoint.setWaypointName(waypoint_name.getText().toString());
        System.out.println("WAYPOINT NAME = " + waypoint.getWaypointName());

        //Validate waypoint name edittext
        if (waypoint_name.getText().toString().length() == 0) {
            waypoint_name.setError("This field cannot be blank");
            return;
        }

        //Fetch visibility setting
        RadioGroup vis_group = findViewById(R.id.visibility_radio);
        int selectedId = vis_group.getCheckedRadioButtonId();
        RadioButton vis = findViewById(selectedId);
        System.out.println("VisSetting:"+vis.getText().toString());
        if (vis.getText().toString().toUpperCase().equals("PUBLIC")) {
            waypoint.setVisSetting(Waypoint.VisibilitySetting.PUBLIC);
        }
        else if (vis.getText().toString().toUpperCase().equals("PRIVATE")) {
            waypoint.setVisSetting(Waypoint.VisibilitySetting.PRIVATE);
        }
        else {
            waypoint.setVisSetting(Waypoint.VisibilitySetting.HIDDEN);
        }

        //Fetch editing settings
        RadioGroup edit_group = findViewById(R.id.edit_radio);
        int selectedId1 = edit_group.getCheckedRadioButtonId();
        RadioButton edit = findViewById(selectedId1);
        String editSetting = edit.getText().toString();
        waypoint.setEditSetting(Waypoint.EditingSetting.valueOf(editSetting.toUpperCase()));
        waypoint.setOwnerUserId(user.getUserId());
        waypoint.setLocation(app.getLocation());

        //Generating waypoint creation query
        String waypoint_query = "13:"+ user.getUserId() +"," + waypoint.getWaypointName() + "," + "STATIC," + editSetting.toUpperCase() + "," + waypoint.getVisSetting().toString() +"," + waypoint.getLocation();

        System.out.println(waypoint_query);

        //Database call for waypoint creation
        FutureTask a;
        a = new FutureTask<>(new DatabaseConnThread(waypoint_query));
        Thread t = new Thread(a);
        t.start();

        try {
            ArrayList<ArrayList<String>> b = (ArrayList<ArrayList<String>>) a.get();
            Toast.makeText(getApplicationContext(), "Created Successfully!", Toast.LENGTH_SHORT).show();
//            wpid = b.get(b.size()-1).get(0).toString();
            //Set waypoint id
            waypoint.setGlobalId(Integer.parseInt(b.get(b.size()-1).get(0)));
            app.getUser().addStaticWaypoint(waypoint);

            System.out.println("New Waypoint id:"+waypoint.getGlobalId());
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch(NumberFormatException e) {
            Toast.makeText(getApplicationContext(), "Name already exists", Toast.LENGTH_SHORT).show();
            waypoint_name.setError("Name already exists");
        }

        //Database call to fetch each checked playlist and add them to Waypoint
        LinearLayout ll = (LinearLayout) findViewById(R.id.ll);
        ArrayList<String> checked_pl = new ArrayList<String>();
        int count = ll.getChildCount();
        for (int i = 0; i<count;i++){
            View v = ll.getChildAt(i);
            if (v instanceof CheckBox){
                if (((CheckBox) v).isChecked()){
                    FutureTask rr = new FutureTask<>(new DatabaseConnThread("15:"+playlist_id.get(i)+","+waypoint.getGlobalId()));
                    Thread t4 = new Thread(rr);
                    t4.start();
                    try {
                        ArrayList<ArrayList<String>> bb = (ArrayList<ArrayList<String>>) rr.get();
                        System.out.println(bb);
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        //Uncomment to set intent to go back to previous page once Waypoint is created
//        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }

    public void addPlaylist(View view){

        LinearLayout ll = (LinearLayout) findViewById(R.id.ll);
        app = (MyApplication) getApplication();

        //Fetch all playlists of the user
        FutureTask a = new FutureTask<>(new DatabaseConnThread("7:"+user.getUserId()));
        Thread t2 = new Thread(a);
        t2.start();
        try{
            ArrayList<ArrayList<String>> b = (ArrayList<ArrayList<String>>) a.get();
            for (int i = 0; i<b.size();i++){
                playlist_name.add(b.get(i).get(1));
                playlist_id.add(b.get(i).get(0));
            }
            System.out.println("Playlist name:"+playlist_id+playlist_name);
        }catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Create dynamic checkboxes for each fetched playlist
        for (int i = 0;i<playlist_name.size();i++){
            CheckBox cb = new CheckBox(getApplicationContext());
            cb.setText(playlist_name.get(i));
            ll.addView(cb);
        }
    }

    class DatabaseConnThread implements Callable {
        String q;
        public DatabaseConnThread(String val) {q = val;}
        private String getQuery() {
            return q;
        }
        public Object call() {
            DatabaseConnector dbc = new DatabaseConnector(app.getLock());
            ArrayList<ArrayList<String>> result;

            System.out.println("Value inside thread " + getQuery());
            result = dbc.requestData(getQuery());

            return result;
        }
    }
}







