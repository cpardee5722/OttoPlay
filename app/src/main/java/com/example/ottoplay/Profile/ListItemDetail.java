package com.example.ottoplay.Profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ottoplay.DatabaseConnector;
import com.example.ottoplay.MyApplication;
import com.example.ottoplay.R;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class ListItemDetail extends AppCompatActivity {
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
    private Button btnDemo;
    private androidx.appcompat.widget.Toolbar toolbar;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listitem);

        Intent intent = getIntent();
        final int position = intent.getIntExtra("position", 0);

//        Fetching friend name and id from FriendsActivity.java
        ArrayList<String> friend_name = getIntent().getStringArrayListExtra("friend_name");
        final ArrayList<String> friend_id = getIntent().getStringArrayListExtra("friend_id");
////        friend_name = getIntent().getStringArrayListExtra("friend_name");
//
//        // Here we turn your string.xml in an array
//        String[] myKeys = getResources().getStringArray(R.array.sections);
        System.out.println("friendnameposition"+friend_name.get(position));
        System.out.println("friendnameid:" + friend_id.get(position));
//        System.out.println("friendID:"+position);

        TextView myTextView = (TextView) findViewById(R.id.my_textview);
        myTextView.setText(friend_name.get(position));

        toolbar = findViewById(R.id.toolBarTop);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),FriendsActivity.class));
            }
        });

//        To remove/delete friend
        btnDemo = (Button) findViewById(R.id.removebutton);
//        setSupportActionBar(btnDemo);
        btnDemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FutureTask a;
//                Fetching userid
                app = (MyApplication)getApplication();
                String id = Integer.toString(app.getUserId());
                a = new FutureTask<>(new DatabaseConnThread("32:"+id+","+friend_id.get(position)));
                Thread t = new Thread(a);
                t.start();
                try {
                    ArrayList<ArrayList<String>> b = (ArrayList<ArrayList<String>>) a.get();
                    Toast.makeText(getApplicationContext(), "Removed Friend!", Toast.LENGTH_SHORT).show();
//                    textViewToChange.setText(b.get(0).get(0));
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                startActivity(new Intent(getApplicationContext(),FriendsActivity.class));
            }
        });
    }
}
