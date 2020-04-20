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

import static androidx.core.content.ContextCompat.startActivity;

public class PasswordReset extends AppCompatActivity {

    private LoginViewModel loginViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

    }

    public void UpdateText(View view) {
        //Check if user exists, if so, go to UpdatePassword
        System.out.println("Button Pressed");
        TextView username = (TextView) findViewById(R.id.usernameReset);

        if (username.getText().toString().length() == 0) {
            System.out.println("This field cannot be blank");
            return;
        }

        new Thread(new DatabaseReset(username)).start();

    }

    public void Update(TextView username) {
        String user = username.getText().toString();
        Intent update = new Intent(this,UpdatePassword.class);
        update.putExtra("username", user);

        startActivity(update);
    }

    class DatabaseReset implements Runnable {

        private final TextView username;

        public DatabaseReset(TextView username) {
            this.username = username;
        }

        @Override
        public void run() {
            DatabaseConnector dbc = new DatabaseConnector();
            ArrayList<ArrayList<String>> checkUser;
            checkUser = dbc.requestData("33:" + username.getText());
            System.out.println("HERE");
            for (int i = 0; i < checkUser.size(); i++) {
                for (int j = 0; j < checkUser.get(i).size(); j++) {
                    if (checkUser.get(i).get(j).length() == 0) {
                        System.out.println("User does not exists");
                        return;
                    }
                }
            }
            Update(username);
        }
    }
}
