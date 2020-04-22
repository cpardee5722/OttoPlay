package com.example.ottoplay.Profile;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ottoplay.DatabaseConnector;
import com.example.ottoplay.MyApplication;
import com.example.ottoplay.R;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ProfileActivity extends AppCompatActivity implements android.widget.AdapterView.OnItemClickListener {
    private Object AdapterView;
    private Object View;
    static SharedPreferences spid;
    private MyApplication app;

// Fetching from database
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

    private androidx.appcompat.widget.Toolbar toolbar;
    String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);

        app = (MyApplication)getApplication();
        String username = app.getLoginUsername();
//        Display username in Profile
        final TextView textViewToChange = (TextView) findViewById(R.id.tvName);
        textViewToChange.setText(username);
//        System.out.println("Usernameeeeee" + username);
        FutureTask a;
        a = new FutureTask<>(new DatabaseConnThread("33:"+username.toString()));
        Thread t = new Thread(a);
        t.start();
        try {
            ArrayList<ArrayList<String>> b = (ArrayList<ArrayList<String>>) a.get();
//            System.out.println("b is:" + b);

//            Setting userid from username
            userId = b.get(0).get(0);
            System.out.println(userId);
            app.setUserId(Integer.parseInt(userId));

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        Making Listview clickable
        ListView listview = (ListView) findViewById(R.id.listView1);
        listview.setOnItemClickListener(this);

//        Back button at toolbarTop
        toolbar = findViewById(R.id.toolBarTop);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }
        });
    }

//    Making Listview clickable

    public void onItemClick(AdapterView<?> l, View v, int position, long id) {
        Log.i("HelloListView", "You clicked Item: " + id + " at position:" + position);
        // Then you start a new Activity via Intent
        System.out.println(position);

        Intent intent = new Intent();
        if (position == 0){
            intent.setClass(this, FriendsActivity.class);
        }
        else if(position == 1){
            intent.setClass(this, MyWaypoints.class);
        }
        else if(position == 2){
            intent.setClass(this, MyPlaylists.class);
        }
        else if (position == 3) {
            intent.setClass(this, SharedPlaylists.class);
        }
        else if(position == 4){
            intent.setClass(this,SearchActivity.class);
        }

        intent.putExtra("position", position);
        // Or / And
        System.out.println("It works!!");
        intent.putExtra("id", id);
        startActivity(intent);
    }
}