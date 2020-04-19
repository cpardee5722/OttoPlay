//Maps Activity Class

package com.example.ottoplay;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class WaypointMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private MyApplication app;
    private User currentUser;

    private LocationListener locationListener;

    private Marker userMarker;
    private Circle userCircle;
    private boolean markersInitialized = false;

    BitmapDescriptor otherUsersMarkerIcon;

    private HashMap<Marker, Pair<Circle, Waypoint>> waypointMarkers;
    private HashMap<Waypoint.Genre, ArrayList<Marker>> staticWaypointsByGenre;
    private HashSet<Waypoint.Genre> currentGenreFilters;

    private ArrayList<ArrayList<String>> wpResults;

    //TODO get dwp from current user object
    DynamicWaypoint dwp;
    private Location dwpLocation;
    ReentrantLock lock;
    ReentrantLock connectorLock;

    //TODO implement discovery
    private boolean discoverySetting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waypoint_map);


        app = (MyApplication) getApplication();
        currentUser = app.getUser();
        dwp = currentUser.getDynamicWaypoint();

        lock = new ReentrantLock();
        connectorLock = app.getLock();

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
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public boolean onMarkerClick(Marker marker) {
                try {
                    lock.lock();
                    Waypoint wp = waypointMarkers.get(marker).second;
                    lock.unlock();
                    LatLng l1 = userMarker.getPosition();
                    LatLng l2 = marker.getPosition();
                    float[] res = new float[1];
                    Location.distanceBetween(l1.latitude, l1.longitude, l2.latitude, l2.longitude, res);
                    if ((wp instanceof StaticWaypoint && res[0] <= 20.0) || (wp instanceof DynamicWaypoint && res[0] <= 10.0) || wp.getOwnerUserId() == currentUser.getUserId()) {
                       app.setWaypoint(wp);
                       Intent intent = new Intent(WaypointMapActivity.this, WaypointPlaylistsFromMapActivity.class);
                       WaypointMapActivity.this.startActivity(intent);
                    }
                    else {
                        Toast.makeText(WaypointMapActivity.this, "Waypoint not in range.",
                                Toast.LENGTH_SHORT).show();
                    }
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

        //dwp = new DynamicWaypoint(19);
        //dwp.setOwnerUserId(19); //TODO REMOVE THIS

        waypointMarkers = new HashMap<>();
        staticWaypointsByGenre = new HashMap<>();
        currentGenreFilters = new HashSet<>();
        loadAndDisplayStaticWaypoints();
        //locationUpdate();

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

    void locationUpdate() {
        Thread t = new Thread(new LocationUpdateThread());
        t.start();
    }

    class LocationUpdateThread implements Runnable {
        @Override
        public void run() {
            String location;
            final HashMap<Integer, Marker> dwpMarkers = new HashMap<>();
            String dwpData = Integer.toString(dwp.getGlobalId()) + "," + Integer.toString(currentUser.getUserId()) + "," + dwp.getWaypointName();
            while (true) {
                DatabaseConnector dbc = new DatabaseConnector(connectorLock);
                location = Double.toString(dwpLocation.getLatitude()) + " " + Double.toString(dwpLocation.getLongitude());

                final ArrayList<ArrayList<String>> userLocations = dbc.requestData("53:" + dwpData + "," + dwp.getVisSetting().toString() + "," + location);
                final Set<Integer> updatedDwps = new HashSet<>();
                for (int i : dwpMarkers.keySet()) {
                    updatedDwps.add(i);
                }

                WaypointMapActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < userLocations.size(); i++) {

                            final Waypoint wp = new DynamicWaypoint(Integer.parseInt(userLocations.get(i).get(0)),
                                    Integer.parseInt(userLocations.get(i).get(1)), userLocations.get(i).get(2));
                            wp.setVisSetting(Waypoint.VisibilitySetting.valueOf(userLocations.get(i).get(3)));

                            String[] coords = userLocations.get(i).get(4).split(" ");
                            final LatLng wpLoc = new LatLng(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]));

                            System.out.println(wp.getOwnerUserId() + " " + wp.getGlobalId() + " " + wp.getWaypointName() + " " + wp.getVisSetting().toString());
                            if (!dwpMarkers.containsKey(wp.getOwnerUserId())) {
                                System.out.println("CREATING1");
                                Marker m = mMap.addMarker(new MarkerOptions().position(wpLoc));
                                m.setIcon(otherUsersMarkerIcon);
                                Circle c = mMap.addCircle(new CircleOptions().center(wpLoc));
                                c.setStrokeColor(Color.RED);
                                c.setRadius(10);
                                c.setStrokeWidth(4);
                                dwpMarkers.put(wp.getOwnerUserId(), m);
                                lock.lock();
                                System.out.println("CREATING2");
                                waypointMarkers.put(m, new Pair<>(c, wp));
                                lock.unlock();
                            }

                            else {
                                System.out.println("UPDATING");
                                updatedDwps.remove(wp.getOwnerUserId());
                                waypointMarkers.get(dwpMarkers.get(wp.getOwnerUserId())).first.setCenter(wpLoc);
                                dwpMarkers.get(wp.getOwnerUserId()).setPosition(wpLoc);
                            }

                            if (wp.getVisSetting() == Waypoint.VisibilitySetting.PUBLIC || (wp.getVisSetting() == Waypoint.VisibilitySetting.PRIVATE
                                    && currentUser.isFriend(wp.getOwnerUserId()))) {
                                dwpMarkers.get(wp.getOwnerUserId()).setVisible(true);
                                waypointMarkers.get(dwpMarkers.get(wp.getOwnerUserId())).first.setVisible(true);
                            }
                            else {
                                dwpMarkers.get(wp.getOwnerUserId()).setVisible(false);
                                waypointMarkers.get(dwpMarkers.get(wp.getOwnerUserId())).first.setVisible(false);
                            }
                        }

                        lock.lock();
                        for (int i : updatedDwps) {
                            System.out.println("REMOVING" + i);
                            //dwpMarkers.get(i).setVisible(false);
                            dwpMarkers.get(i).remove();
                            waypointMarkers.get(dwpMarkers.get(i)).first.remove();
                            waypointMarkers.remove(dwpMarkers.get(i));
                            dwpMarkers.remove(i);
                        }
                        lock.unlock();
                    }
                });

                try {
                    Thread.sleep(500);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadAndDisplayStaticWaypoints() {
        try {
            HashMap<Integer, ArrayList<Waypoint.Genre>> waypointGenresTable = new HashMap<>();
            waypointMarkers.clear();
            waypointMarkers.put(userMarker, new Pair<Circle, Waypoint>(userCircle,dwp));
            staticWaypointsByGenre.clear();
            for (Waypoint.Genre g : Waypoint.Genre.values()) {
                staticWaypointsByGenre.put(g, new ArrayList<Marker>());
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

                String[] coords = wp.getLocation().split(" ");
                if (coords.length == 2 && (wp.getVisSetting() == Waypoint.VisibilitySetting.PUBLIC || wp.getOwnerUserId() == currentUser.getUserId()
                    || (wp.getVisSetting() == Waypoint.VisibilitySetting.PRIVATE && currentUser.isFriend(wp.getOwnerUserId())))) {
                    //System.out.println("id = " + wp.getGlobalId());
                    LatLng wpLoc = new LatLng(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]));
                    Marker m = mMap.addMarker(new MarkerOptions().position(wpLoc));


                    Circle c = mMap.addCircle(new CircleOptions().center(wpLoc));
                    c.setRadius(20);
                    c.setStrokeWidth(4);

                    if (wp.getOwnerUserId() == currentUser.getUserId()){
                        c.setStrokeColor(Color.GREEN);
                        m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    }
                    else {
                        c.setStrokeColor(Color.RED);
                        m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    }

                    waypointMarkers.put(m, Pair.create(c, wp));
                    for (Waypoint.Genre g : waypointGenresTable.get(wp.getGlobalId())) {
                        if (staticWaypointsByGenre.containsKey(g)) {
                            staticWaypointsByGenre.get(g).add(m);
                            wp.addGenre(g);
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
            DatabaseConnector dbc = new DatabaseConnector(connectorLock);
            wpResults = dbc.requestData("41:");
        }
    }

    class GetStaticWaypointsFromDBThread implements Runnable {
        @Override
        public void run() {
            DatabaseConnector dbc = new DatabaseConnector(connectorLock);
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
        dwpLocation = location;
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
        userMarker.setIcon(bitmapDescriptorFromVector(this, R.drawable.ic_accessibility_new_24px));
        userCircle = mMap.addCircle(new CircleOptions().center(l));
        userCircle.setRadius(10);
        userCircle.setStrokeColor(Color.GREEN);
        userCircle.setStrokeWidth(4);

        otherUsersMarkerIcon = bitmapDescriptorFromVector(this, R.drawable.ic_accessibility_new_other_24px);
    }

    //Based on https://stackoverflow.com/questions/42365658/custom-marker-in-google-maps-in-android-with-vector-asset-icon
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable background= ContextCompat.getDrawable(context, vectorDrawableResourceId);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
       // Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        //vectorDrawable.setBounds(24, 36, vectorDrawable.getIntrinsicWidth() + 24, vectorDrawable.getIntrinsicHeight() + 36);
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        //vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
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
                    else if (title.compareTo("NEW WAYPOINT") == 0) {
                        //TODO
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
        for (Waypoint.Genre g : staticWaypointsByGenre.keySet()) {
            if (!currentGenreFilters.contains(g)) {
                for (Marker m : staticWaypointsByGenre.get(g)) {
                    m.setVisible(false);
                    waypointMarkers.get(m).first.setVisible(false);
                }
            }
        }

        for (Waypoint.Genre g : staticWaypointsByGenre.keySet()) {
            if (currentGenreFilters.contains(g) || currentGenreFilters.isEmpty()) {
                for (Marker m : staticWaypointsByGenre.get(g)) {
                    m.setVisible(true);
                    waypointMarkers.get(m).first.setVisible(true);
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