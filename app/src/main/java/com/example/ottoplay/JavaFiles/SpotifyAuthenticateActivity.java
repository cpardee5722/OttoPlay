package com.example.ottoplay.JavaFiles;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.ottoplay.Connectors.UserService;
import com.example.ottoplay.Models.User;
import com.example.ottoplay.R;
import com.spotify.sdk.android.auth.*;

public class SpotifyAuthenticateActivity extends AppCompatActivity {

    private SharedPreferences.Editor editor;
    private SharedPreferences msharedPreferences;
    private RequestQueue queue;

    //private static final String CLIENT_ID = "4ffa67263c5747e182a197afbad6ecc0";
    private static final String CLIENT_ID = "f78c3ba2f8404764a5a813ca8d050528";
    private static final String REDIRECT_URI = "com.example.ottoplay://callback/";
    private static final int REQUEST_CODE = 1337;
    private static final String SCOPES = "user-read-recently-played," +
            "user-library-modify," +
            "user-read-email," +
            "user-read-private," +
            "playlist-read-private";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plsylist_activity_splash);
        authenticateSpotify();
        msharedPreferences = this.getSharedPreferences("SPOTIFY", 0);
        queue = Volley.newRequestQueue(this);
    }
    private void authenticateSpotify() {
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{SCOPES});
        AuthorizationRequest request = builder.build();
        System.out.println("SPOTIFY REQUEST = " + request);
        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);
            System.out.println("SPOTIFY " + response.getType() + " " + resultCode);
            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    editor = getSharedPreferences("SPOTIFY", 0).edit();
                    editor.putString("token", response.getAccessToken());
                    Log.d("STARTING", "GOT AUTH TOKEN");
                    editor.apply();
                    waitForUserInfo();
                    break;
                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    break;
                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }
        }
    }
    private void waitForUserInfo() {
        UserService userService = new UserService(queue, msharedPreferences);
        userService.get(() -> {
            User user = userService.getUser();
            editor = getSharedPreferences("SPOTIFY", 0).edit();
            editor.putString("userid", user.id);
            Log.d("STARTING", "GOT USER INFORMATION");
            // We use commit instead of apply because we need the information stored immediately
            editor.apply();
            startMainActivity();
        });
    }
    private void startMainActivity() {
        Intent newintent = new Intent(SpotifyAuthenticateActivity.this, PlaylistActivity.class);
        startActivity(newintent);
    }
}