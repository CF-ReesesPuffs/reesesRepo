package com.cfreesespuffs.github.giftswapper.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amplifyframework.core.Amplify;
import com.cfreesespuffs.github.giftswapper.R;

public class Login extends AppCompatActivity {

    EditText username;
    EditText password;
    Handler handleMismatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = findViewById(R.id.usernameLogin);
        password = findViewById(R.id.passwordLogin);

        ((Button) findViewById(R.id.loginButton)).setOnClickListener(view -> {

            if (username.getText().toString().equals("") || password.getText().toString().equals("")) {
                Toast.makeText(this, "Fill in both fields please.", Toast.LENGTH_SHORT).show();
                return;
            }

            Amplify.Auth.signIn(
                    username.getText().toString().toLowerCase(),
                    password.getText().toString(),
                    result -> {
                        Log.i("Amplify.login", "Sign in successful for: " + result);
                        startActivity(new Intent(Login.this, MainActivity.class));

                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                        final SharedPreferences.Editor preferenceEditor = preferences.edit();
                        preferenceEditor.putString("username", username.getText().toString());
                        preferenceEditor.apply();

                    },
                    error -> {
                        Log.e("Amplify.login", "Sign in fail: " + error.toString());
                        runOnUiThread(new Runnable() { // https://stackoverflow.com/questions/47536005/cant-toast-on-a-thread-that-has-not-called-looper-prepare
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Please make sure your username and password are correct", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
            );
        });

        findViewById(R.id.toSignUp).setOnClickListener(view -> {
            Login.this.startActivity(new Intent(Login.this, SignUp.class));
        });

    }
}