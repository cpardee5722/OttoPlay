package com.example.ottoplay.Profile;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ottoplay.ClassDiagrams.Playlist;
import com.example.ottoplay.ClassDiagrams.User;
import com.example.ottoplay.DatabaseConnector;
import com.example.ottoplay.MyApplication;
import com.example.ottoplay.R;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class MyPlaylists extends AppCompatActivity  {
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

    private ListView lv;
    ArrayList<String> playlist_name = new ArrayList<String>();
    private androidx.appcompat.widget.Toolbar toolbar;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_playlists);
        final TextView textChange = (TextView) findViewById(R.id.playlistName);

        lv = (ListView) findViewById(R.id.myplaylists);

//        Use onItemClickListener on Listview to open playlist page
//        lv.setOnItemClickListener(this);


//Fetching playlists for the userid
        FutureTask a;
        app = (MyApplication)getApplication();

        String id = Integer.toString(app.getUserId());
        a = new FutureTask<>(new DatabaseConnThread("7:" + id));
        Thread t = new Thread(a);
        t.start();

        FutureTask pl;
        pl = new FutureTask<>(new DatabaseConnThread("1"));
        Thread t1 = new Thread(pl);
        t1.start();
        try {
            ArrayList<ArrayList<String>> b = (ArrayList<ArrayList<String>>) a.get();
            System.out.println(b);

//            Storing playlists of the user in an array to retrieve it in Listview
            for (int i=0;i<b.size();i++){
                playlist_name.add(b.get(i).get(1));
            }
            System.out.println("Playlist:"+ playlist_name);
//            textChange.setText(b.get(0).get(1));
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        Setting into listview
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,playlist_name);
        lv.setAdapter(arrayAdapter);

        final User user = app.getUser();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Playlist p = user.getSyncedPlaylists().get(position);
                app.setPlaylist(p);
                startActivity(new Intent(MyPlaylists.this,PlaylistSongsActivity.class));
            }
        });


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
}
