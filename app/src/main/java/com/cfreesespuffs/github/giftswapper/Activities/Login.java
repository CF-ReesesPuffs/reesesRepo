package com.cfreesespuffs.github.giftswapper.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.amplifyframework.core.Amplify;
import com.cfreesespuffs.github.giftswapper.R;

public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ((Button) findViewById(R.id.loginButton)).setOnClickListener(view -> {
            EditText username = findViewById(R.id.usernameLogin);
            EditText password = findViewById(R.id.passwordLogin);

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
                    error -> Log.e("Amplify.login", "Sign in fail: " + error.toString())
            );
        });
    }
}