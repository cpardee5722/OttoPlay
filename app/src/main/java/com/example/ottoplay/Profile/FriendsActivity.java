package com.example.ottoplay.Profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ottoplay.DatabaseConnector;
import com.example.ottoplay.MyApplication;
import com.example.ottoplay.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class FriendsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private MyApplication app;
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


    //    fetch username by id
//    class DatabaseConnThread2 implements Callable {
//        String q;
//        public DatabaseConnThread2(String val){q = val;}
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
    private ListView lv;
    private androidx.appcompat.widget.Toolbar toolbar;
    ArrayList<String> friend_name = new ArrayList<String>();
    ArrayList<String> friend_id = new ArrayList<String>();

    @Override
    public void onRestart() {
        super.onRestart();
        finish();
        startActivity(this.getIntent());
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friends_activity);

//      Fetching userid through MyApplication
        app = (MyApplication)getApplication();
        String id = Integer.toString(app.getUserId());

        lv = (ListView) findViewById(R.id.listViewFriends);
        lv.setOnItemClickListener(this);

        List<String> friend_list = new ArrayList<String>();

        FutureTask a;
        a = new FutureTask<>(new DatabaseConnThread("30:"+id));
        Thread t = new Thread(a);
        t.start();
        FutureTask c;
        try {
            ArrayList<ArrayList<String>> b = (ArrayList<ArrayList<String>>) a.get();
//            System.out.println("This is b:"+b.get(1).get(0));
            for (int i=0;i<b.size();i++){
                friend_id.add(b.get(i).get(0));
                //Looping through user's friend's ids to fetch their usernames in a listview
                c = new FutureTask<>(new DatabaseConnThread("3:"+b.get(i).get(0)));
                Thread t1 = new Thread(c);
                t1.start();
                ArrayList<ArrayList<String>> b1 = (ArrayList<ArrayList<String>>) c.get();
                friend_name.add(b1.get(0).get(0));
            }
            System.out.println("friends:"+friend_name);
            System.out.println("friendids:"+friend_id);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,friend_name);
            lv.setAdapter(arrayAdapter);

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        back button
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
//Clicking on each friend name opens to ListItemDetail.java which will have the friend's info/playlists

            System.out.println(pos);
            System.out.println("friendname:"+friend_name.get(pos));
            Intent intent = new Intent();
            intent.putStringArrayListExtra("friend_name",friend_name);
            intent.putStringArrayListExtra("friend_id",friend_id);
            intent.setClass(this, ListItemDetail.class);
            intent.putExtra("position", pos);
            startActivity(intent);
    }
}
