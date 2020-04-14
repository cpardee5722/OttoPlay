package com.example.ottoplay.ui.login;

import android.app.Activity;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ottoplay.R;
import com.example.ottoplay.ui.login.LoginViewModel;
import com.example.ottoplay.ui.login.LoginViewModelFactory;

import java.io.EOFException;
import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    static SharedPreferences sp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        sp = getSharedPreferences("login",MODE_PRIVATE);
    }

    public void SignUp(View view) {
        startActivity(new Intent(this, SignUp.class));
    }

    public void PasswordReset(View view) {
        startActivity(new Intent(this, PasswordReset.class));
    }

    public void CheckDatabase(View view) {
        System.out.println("Pressed the button!");
        TextView username = (TextView) findViewById(R.id.usernameLogin);
        TextView password = (TextView) findViewById(R.id.password);

        if (username.getText().toString().length() == 0) {
            System.out.println("This field cannot be blank");
            return;
        }
        if (password.getText().toString().length() == 0) {
            System.out.println("This field cannot be blank");
            return;
        }
        new Thread(new DatabaseLogin(username, password)).start();
        System.out.println("HERE");
    }

    public static void createLoginSession(TextView username, TextView password) {
        System.out.println("Made it with this user info:");
        System.out.println("Username: " + username.getText());
        System.out.println("Password: " + password.getText());
        //if (sp.contains(username.getText().toString()) && sp.contains(password.getText().toString())) {
        if (sp.contains("username") && sp.contains("password")) {
            //No need to add to shared preferences, just go to home activity
            //Currently this will remove the user from shared preferences. Use for logout functionality
            System.out.println("User is in the shared preferences. Removing from shared preferences");
            SharedPreferences.Editor e = sp.edit();
            e.clear();
            e.commit();
            System.out.println("Successfully removed user");
        }
        else {
            //Add to shared preferences and go to home activity
            System.out.println("Adding user to shared preferences");
            SharedPreferences.Editor e = sp.edit();
            e.putString("username",username.getText().toString());
            e.putString("password",password.getText().toString());
            e.commit();
            System.out.println("successfully added user");
        }
    }
}

class DatabaseLogin implements Runnable {

    private final TextView username;
    private final TextView password;

    public DatabaseLogin(TextView username, TextView password){
        this.username = username;
        this.password = password;
    }

    @Override
    public void run() {
        DatabaseConnector dbc = new DatabaseConnector();
        ArrayList<ArrayList<String>> checkUser;

        checkUser = dbc.requestData("33:" + username.getText()); //returns [[id, username, password]]
        System.out.println("checkuser : " + checkUser);

        for (int i = 0; i < checkUser.size(); i++) {
            for (int j = 0; j < checkUser.get(i).size(); j ++) {
                if (checkUser.get(i).get(j).length() == 0) {
                    System.out.println("The user does not exist");
                    return;
                }
            }
            System.out.println("User exists");

            if (!password.getText().toString().equals(checkUser.get(i).get(2)))
                System.out.println("Incorrect password");
            else {
                System.out.println("Login Successful");
                LoginActivity.createLoginSession(username, password);
            }
        }
    }
}
