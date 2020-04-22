package com.example.ottoplay.Profile;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.ottoplay.R;

public class ButtonDemoActivity extends Activity {


    private Button btnDemo;
    private boolean isPink = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listitem);

        btnDemo = (Button) findViewById(R.id.removebutton);
        btnDemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isPink = !isPink;
                int resId = isPink ? R.drawable.orange_button : R.drawable.grey_button;
                btnDemo.setBackgroundResource(resId);
                btnDemo.setText(isPink ? "Remove Friend" : "Removed");
            }
        });
    }
}