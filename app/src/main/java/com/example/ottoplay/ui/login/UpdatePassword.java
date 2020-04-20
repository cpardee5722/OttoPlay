package com.example.ottoplay.ui.login;

import android.app.Activity;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
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

public class UpdatePassword extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    String username;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);
        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        Intent getData = getIntent();
        username = getData.getStringExtra("username");
        getSupportActionBar().setTitle(username);
    }

    public void updatePassword(View view) {
        TextView newPassword = (TextView) findViewById(R.id.newPassword);
        TextView reEnterNewPassword = (TextView) findViewById(R.id.reEnterNewPassword);

        if (newPassword.getText().toString().length() == 0) {
            System.out.println("This field cannot be blank");
            return;
        }
        if (reEnterNewPassword.getText().toString().length() == 0) {
            System.out.println("This field cannot be blank");
            return;
        }
        if (!newPassword.getText().toString().equals(reEnterNewPassword.getText().toString()))
        {
            System.out.println("These fields do not match");
            return;
        }

        new Thread(new DatabasePassword(username,newPassword)).start();
    }

    public void returnToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    class DatabasePassword implements Runnable {

        private final String username;
        private final TextView password;

        public DatabasePassword(String username, TextView password) {
            this.username = username;
            this.password = password;
        }

        @Override
        public void run() {
            DatabaseConnector dbc = new DatabaseConnector();
            ArrayList<ArrayList<String>> passwordUpdate;

            passwordUpdate = dbc.requestData("49:" + username + "," + password.getText());


            System.out.println("Password has been reset");
            returnToLogin();
        }
    }
}