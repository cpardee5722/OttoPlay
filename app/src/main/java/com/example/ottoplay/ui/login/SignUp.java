package com.example.ottoplay.ui.login;

import android.app.Activity;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

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

import java.util.ArrayList;

public class SignUp extends AppCompatActivity {

    private LoginViewModel loginViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

    }

    public void AddUser(View view) {
        //Go to Home Screen of App
        TextView username = (TextView) findViewById(R.id.usernameSignUp);
        TextView password = (TextView) findViewById(R.id.passwordSignUp);
        TextView reEnterPassword = (TextView) findViewById(R.id.reEnterPasswordSignUp);

        if (username.getText().toString().length() == 0) {
            System.out.println("This field cannot be blank");
            return;
        }
        if (password.getText().toString().length() == 0) {
            System.out.println("This field cannot be blank");
            return;
        }
        if (reEnterPassword.getText().toString().length() == 0 || !reEnterPassword.getText().toString().equals(password.getText().toString())) {
            System.out.println("Password does not match");
            return;
        }

        new Thread(new DatabaseSignUp(username,password)).start();
        this.onBackPressed();
    }

    class DatabaseSignUp implements Runnable {

        private final TextView username;
        private final TextView password;

        public DatabaseSignUp(TextView username, TextView password){
            this.username = username;
            this.password = password;
        }


        @Override
        public void run() {
            DatabaseConnector dbc = new DatabaseConnector();
            ArrayList<ArrayList<String>> createUser;
            ArrayList<ArrayList<String>> checkUser;
            ArrayList<ArrayList<String>> createWaypoint;
            ArrayList<ArrayList<String>> getId;
            checkUser = dbc.requestData("33:" + username.getText()); //returns [[id, username, password]]

            for (int i = 0; i < checkUser.size(); i++) {
                for (int j = 0; j < checkUser.get(i).size(); j++) {
                    if (checkUser.get(i).get(j).length() != 0) {
                        System.out.println("User already exists");
                        return;
                    }
                }
            }


            createUser = dbc.requestData("12:" + username.getText() + "," + password.getText());
            getId = dbc.requestData("4:" + username.getText());
            //CREATE DYNAMIC WAYPOINT userid, ("username" + dwp) as the name, set type as DYNAMIC, SOLO editing, PRIVATE visibility, null)
            createWaypoint = dbc.requestData("13:" + getId.get(0).get(0) + ",dwp,DYNAMIC,SOLO,PRIVATE,null");
        }
    }
}
