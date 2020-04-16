package com.example.ottoplay;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("here1");
        //setContentView(R.layout.activity_main);

        Intent intent = new Intent(MainActivity.this, WaypointMapActivity.class);
        MainActivity.this.startActivity(intent);
    }
}



