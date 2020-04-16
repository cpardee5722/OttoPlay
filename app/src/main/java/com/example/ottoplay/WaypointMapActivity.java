//Maps Activity Class

package com.example.ottoplay;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class WaypointMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationListener locationListener;

    private Marker userMarker;
    private Circle userCircle;
    private boolean markersInitialized = false;

    private HashMap<Marker, Pair<Circle, Waypoint>> staticWaypoints;
    private HashMap<Waypoint.Genre, ArrayList<Marker>> waypointsByGenre;
    private HashSet<Waypoint.Genre> currentGenreFilters;

    private ArrayList<ArrayList<String>> wpResults;

    //TODO get dwp from current user object
    DynamicWaypoint dwp;

    //TODO implement discovery
    private boolean discoverySetting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waypoint_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.g_map);
        mapFragment.getMapAsync(this);

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMinZoomPreference(10);
        mMap.setMaxZoomPreference(20);

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                setUserLocation(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                try {
                    //if (userMarker == marker) return false;

                    Waypoint wp = staticWaypoints.get(marker).second;
                   // System.out.println(wp.getWaypointName());
                    LatLng l1 = userMarker.getPosition();
                    LatLng l2 = marker.getPosition();
                    float[] res = new float[1];
                    Location.distanceBetween(l1.latitude, l1.longitude, l2.latitude, l2.longitude, res);

                    Bundle bundle = new Bundle();
                    bundle.putBinder("obj_val", new BinderObjectWrapper(wp));
                    Intent intent = new Intent(WaypointMapActivity.this, WaypointPlaylistsFromMapActivity.class);
                    WaypointMapActivity.this.startActivity(intent.putExtras(bundle));
                    //System.out.println(res[0]);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,18));
            setUserLocation(lastKnownLocation);
        }
        else {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }

        dwp = new DynamicWaypoint(19);
        dwp.setOwnerUserId(19); //TODO REMOVE THIS

        staticWaypoints = new HashMap<>();
        //staticWaypoints.put(userMarker, new Pair<Circle, Waypoint>(userCircle,dwp));
        waypointsByGenre = new HashMap<>();
        currentGenreFilters = new HashSet<>();
        loadAndDisplayStaticWaypoints();

        //seattle coordinates
        //LatLng seattle = new LatLng(47.6062095, -122.3320708);
       /* LatLng myLocation = new LatLng(userLat, userLong);
        mMap.addMarker(new MarkerOptions().position(myLocation).title("Seattle"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
        mMap.setMinZoomPreference(16);
        mMap.setMaxZoomPreference(20);

        MarkerOptions marker = new MarkerOptions();

        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(myLocation);
        circleOptions.radius(20);
        circleOptions.strokeColor(Color.RED);
        circleOptions.strokeWidth(4);
        mMap.addCircle(circleOptions);*/
    }

    private void loadAndDisplayStaticWaypoints() {
        try {
            HashMap<Integer, ArrayList<Waypoint.Genre>> waypointGenresTable = new HashMap<>();
            staticWaypoints.clear();
            waypointsByGenre.clear();
            for (Waypoint.Genre g : Waypoint.Genre.values()) {
                waypointsByGenre.put(g, new ArrayList<Marker>());
            }

            wpResults = new ArrayList<ArrayList<String>>();

            Thread dbConn1 = new Thread(new GetWaypointGenresFromDBThread());
            dbConn1.start();
            dbConn1.join();

            for (int i = 0; i < wpResults.size(); i++) {
                if (!waypointGenresTable.containsKey(Integer.parseInt(wpResults.get(i).get(0)))) {
                    waypointGenresTable.put(Integer.parseInt(wpResults.get(i).get(0)), new ArrayList<Waypoint.Genre>());
                }

                waypointGenresTable.get(Integer.parseInt(wpResults.get(i).get(0))).add(Waypoint.Genre.valueOf(wpResults.get(i).get(1)));
            }

            wpResults.clear();

            Thread dbConn2 = new Thread(new GetStaticWaypointsFromDBThread());
            dbConn2.start();
            dbConn2.join();

            //System.out.println(wpResults.size());
            for (int i = 0; i < wpResults.size(); i++) {
                Waypoint wp = new StaticWaypoint(Integer.parseInt(wpResults.get(i).get(0)), Integer.parseInt(wpResults.get(i).get(1)), wpResults.get(i).get(2),
                        Waypoint.EditingSetting.valueOf(wpResults.get(i).get(3)), Waypoint.VisibilitySetting.valueOf(wpResults.get(i).get(4)), wpResults.get(i).get(5));

                System.out.println("loc " + wp.getLocation());

                String[] coords = wp.getLocation().split(" ");
                if (coords.length == 2 && wp.getVisSetting() == Waypoint.VisibilitySetting.PUBLIC) {
                    //System.out.println("id = " + wp.getGlobalId());
                    LatLng wpLoc = new LatLng(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]));
                    Marker m = mMap.addMarker(new MarkerOptions().position(wpLoc));
                    m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    Circle c = mMap.addCircle(new CircleOptions().center(wpLoc));
                    c.setRadius(20);
                    c.setStrokeColor(Color.RED);
                    c.setStrokeWidth(4);

                    staticWaypoints.put(m, Pair.create(c, wp));
                    for (Waypoint.Genre g : waypointGenresTable.get(wp.getGlobalId())) {
                        if (waypointsByGenre.containsKey(g)) {
                            waypointsByGenre.get(g).add(m);
                        }
                    }
                }
            }
            wpResults.clear();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    class GetWaypointGenresFromDBThread implements Runnable {
        @Override
        public void run() {
            DatabaseConnector dbc = new DatabaseConnector();
            wpResults = dbc.requestData("41:");
        }
    }

    class GetStaticWaypointsFromDBThread implements Runnable {
        @Override
        public void run() {
            DatabaseConnector dbc = new DatabaseConnector();
            wpResults = dbc.requestData("46:");
        }
    }

    private void setUserLocation(Location location) {
        LatLng userLocation = new LatLng(location.getLatitude(),location.getLongitude());

        if (!markersInitialized) {
            initializeMarkers(userLocation);
            markersInitialized = true;
        }
        //mMap.clear();
        userMarker.setPosition(userLocation);
        userCircle.setCenter(userLocation);
        //mMap.addMarker(new MarkerOptions().position(userLocation));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
        /*CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(userLocation);
        circleOptions.radius(20);
        circleOptions.strokeColor(Color.RED);
        circleOptions.strokeWidth(4);
        mMap.addCircle(circleOptions);*/
    }

    private void initializeMarkers(LatLng l) {
        userMarker = mMap.addMarker(new MarkerOptions().position(l));
        userMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        //userMarker.setTitle("jgperra");

        userCircle = mMap.addCircle(new CircleOptions().center(l));
        userCircle.setRadius(20);
        userCircle.setStrokeColor(Color.GREEN);
        userCircle.setStrokeWidth(4);
    }

    public void showMenu(View v) {
        final PopupMenu popup = new PopupMenu(WaypointMapActivity.this, v);
        popup.inflate(R.menu.waypoint_settings_menu);

        initializeMenu(popup);

        popup.show();

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (!item.hasSubMenu()) {
                    //check or uncheck box/button
                    item.setChecked(!item.isChecked());

                    String title = item.getTitle().toString();
                    System.out.println(title);
                    if (title.compareTo("PUBLIC") == 0 || title.compareTo("PRIVATE") == 0 || title.compareTo("HIDDEN") == 0) {
                        dwp.setVisSetting(Waypoint.VisibilitySetting.valueOf(title));
                    }
                    else if (title.compareTo("ON") == 0 || title.compareTo("OFF") == 0) {
                        if (title.compareTo("ON") == 0) discoverySetting = true;
                        else discoverySetting = false;
                    }
                    else {
                        if (item.isChecked()) {
                            currentGenreFilters.add(Waypoint.Genre.valueOf(title));
                            filterWaypoints();
                        }
                        else {
                            currentGenreFilters.remove(Waypoint.Genre.valueOf(title));
                            filterWaypoints();
                        }
                    }

                    //keep the menu opened
                    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                    item.setActionView(new View(WaypointMapActivity.this));
                    item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                        @Override
                        public boolean onMenuItemActionExpand(MenuItem item) {
                            return false;
                        }

                        @Override
                        public boolean onMenuItemActionCollapse(MenuItem item) {
                            return false;
                        }
                    });
                }
                return false;
            }
        });
    }

    private void filterWaypoints() {
        for (Waypoint.Genre g : waypointsByGenre.keySet()) {
            if (!currentGenreFilters.contains(g)) {
                for (Marker m : waypointsByGenre.get(g)) {
                    m.setVisible(false);
                    staticWaypoints.get(m).first.setVisible(false);
                }
            }
        }

        for (Waypoint.Genre g : waypointsByGenre.keySet()) {
            if (currentGenreFilters.contains(g) || currentGenreFilters.isEmpty()) {
                for (Marker m : waypointsByGenre.get(g)) {
                    m.setVisible(true);
                    staticWaypoints.get(m).first.setVisible(true);
                }
            }
        }
    }

    private void initializeMenu(PopupMenu popup) {
        //intialize Discover menu
        if (discoverySetting) popup.getMenu().findItem(R.id.discover_options).getSubMenu().findItem(R.id.discover_on).setChecked(true);
        else popup.getMenu().findItem(R.id.discover_options).getSubMenu().findItem(R.id.discover_off).setChecked(true);

        //initialize Filters menu
        if (currentGenreFilters.contains(Waypoint.Genre.ROCK)) {
            popup.getMenu().findItem(R.id.filter_options).getSubMenu().findItem(R.id.filter_rock).setChecked(true);
        }
        if (currentGenreFilters.contains(Waypoint.Genre.METAL)) {
            popup.getMenu().findItem(R.id.filter_options).getSubMenu().findItem(R.id.filter_metal).setChecked(true);
        }
        if (currentGenreFilters.contains(Waypoint.Genre.HIPHOP)) {
            popup.getMenu().findItem(R.id.filter_options).getSubMenu().findItem(R.id.filter_hiphop).setChecked(true);
        }
        if (currentGenreFilters.contains(Waypoint.Genre.POP)) {
            popup.getMenu().findItem(R.id.filter_options).getSubMenu().findItem(R.id.filter_pop).setChecked(true);
        }
        if (currentGenreFilters.contains(Waypoint.Genre.JAZZ)) {
            popup.getMenu().findItem(R.id.filter_options).getSubMenu().findItem(R.id.filter_jazz).setChecked(true);
        }
        if (currentGenreFilters.contains(Waypoint.Genre.BLUES)) {
            popup.getMenu().findItem(R.id.filter_options).getSubMenu().findItem(R.id.filter_blues).setChecked(true);
        }
        if (currentGenreFilters.contains(Waypoint.Genre.COUNTRY)) {
            popup.getMenu().findItem(R.id.filter_options).getSubMenu().findItem(R.id.filter_country).setChecked(true);
        }
        if (currentGenreFilters.contains(Waypoint.Genre.EDM)) {
            popup.getMenu().findItem(R.id.filter_options).getSubMenu().findItem(R.id.filter_edm).setChecked(true);
        }

        //initialize privacy menu
        if (dwp.getVisSetting() == Waypoint.VisibilitySetting.PUBLIC) {
            popup.getMenu().findItem(R.id.privacy_options).getSubMenu().findItem(R.id.PUBLIC).setChecked(true);
        }
        if (dwp.getVisSetting() == Waypoint.VisibilitySetting.PRIVATE) {
            popup.getMenu().findItem(R.id.privacy_options).getSubMenu().findItem(R.id.PRIVATE).setChecked(true);
        }
        if (dwp.getVisSetting() == Waypoint.VisibilitySetting.HIDDEN) {
            popup.getMenu().findItem(R.id.privacy_options).getSubMenu().findItem(R.id.HIDDEN).setChecked(true);
        }
    }
}