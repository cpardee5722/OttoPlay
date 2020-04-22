package com.example.ottoplay.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class SearchActivity extends AppCompatActivity {
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

    Button mButton;
    EditText mEdit;
    private androidx.appcompat.widget.Toolbar toolbar;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);
        EditText et = (EditText) findViewById(R.id.editText);
        String text= et.getEditableText().toString();

        mButton = (Button)findViewById(R.id.button);
        mEdit   = (EditText)findViewById(R.id.editText);

//        To view all users in the table. Just for reference.
        FutureTask abc;
        abc = new FutureTask<>(new DatabaseConnThread("1"));
        Thread t3 = new Thread(abc);
        t3.start();

//        Button to search username in database and add it to the user's friend's list
        mButton.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {
                        Log.v("EditText", mEdit.getText().toString());

                        FutureTask a;
                        a = new FutureTask<>(new DatabaseConnThread("33:"+mEdit.getText().toString()));
                        FutureTask cc;
                        cc = new FutureTask<>(new DatabaseConnThread("4:"+mEdit.getText().toString()));
                        Thread t = new Thread(a);
                        t.start();
                        Thread t1 = new Thread(cc);
                        t1.start();
                        try {
                            ArrayList<ArrayList<String>> b = (ArrayList<ArrayList<String>>) a.get();
                            ArrayList<ArrayList<String>> id = (ArrayList<ArrayList<String>>) cc.get();
//                            Get userid from username
                            FutureTask af;
                            app = (MyApplication)getApplication();
                            String userId = Integer.toString(app.getUserId());
                            af = new FutureTask<>(new DatabaseConnThread("31:"+userId+","+id.get(0).get(0)));
                            Thread t2 = new Thread(af);
                            t2.start();

                            Toast.makeText(getApplicationContext(), "Added Friend!", Toast.LENGTH_SHORT).show();

                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mEdit.getText().clear();
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

