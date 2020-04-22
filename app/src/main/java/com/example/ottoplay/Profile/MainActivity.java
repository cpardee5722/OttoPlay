package com.example.ottoplay.Profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ottoplay.MyApplication;
import com.example.ottoplay.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private MyApplication app;

    //    Fetch from database
//    class DatabaseConnThread implements Callable {
//        String q;
//        public DatabaseConnThread(String val){q = val;}
//        private String getQuery(){return q;}
//        public ArrayList<ArrayList<String>> call() {
//            DatabaseConnector dbc = new DatabaseConnector();
//            ArrayList<ArrayList<String>> result;
//            result = dbc.requestData(getQuery());
//
//            //iterate through 2D array
//            for (int i = 0; i < result.size(); i++) {
//                for (int j = 0; j < result.get(i).size(); j++) {
//                    System.out.println(result.get(i).get(j) + " ");
//                }
//                System.out.println();
//            }
//            return result;
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

//  Fetch textview from header and make it clickable to open profile activity
        View headerview = navigationView.getHeaderView(0);
        TextView profilename = (TextView) headerview.findViewById(R.id.textView);

//        SharedPreferences pref = getSharedPreferences("login",MODE_PRIVATE);
//        SharedPreferences.Editor editor = pref.edit();
//        String username = pref.getString("username",null);
//        System.out.println("usis:"+username);

//        Set username in Nav Drawer header
        app = (MyApplication)getApplication();
        String username = app.getLoginUsername();
        System.out.println("usis:"+username);
        profilename.setText(username);

//        Start profile activity when you click on username in the Nav Drawer
        LinearLayout header = (LinearLayout) headerview.findViewById(R.id.linearView);
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

//        Nav drawer stuff which will probably not be useful when we merge with the actual home screen
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_map)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

//        Display username
//        FutureTask a;
//        a = new FutureTask<>(new DatabaseConnThread());
//        Thread t = new Thread(a);
////        t.start();
//        try {
////            ArrayList<ArrayList<String>> b = (ArrayList<ArrayList<String>>) a.get();
////            profilename.setText(b.get(0).get(0));
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
