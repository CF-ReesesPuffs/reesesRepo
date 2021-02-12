package com.cfreesespuffs.github.giftswapper.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.User;
import com.cfreesespuffs.github.giftswapper.R;

public class Login extends AppCompatActivity {

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

//        getActionBar().setDisplayUseLogoEnabled(true);


        handler = new Handler(Looper.getMainLooper(),
                new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message msg) {
                        if (msg.arg1 == 0) {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "Please check your email and username are correct.", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            View toastView = toast.getView();
                            toastView.getBackground().setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_IN);
                            toast.show();
                        }
                        return false;
                    }
                });

        findViewById(R.id.loginButton).setOnClickListener(view -> {
            EditText username = findViewById(R.id.usernameLogin);
            EditText password = findViewById(R.id.passwordLogin);

            Amplify.Auth.signIn(
                    username.getText().toString().toLowerCase(),
                    password.getText().toString(),
                    result -> {
                        Log.i("Amplify.login", "Sign in successful for: " + result);
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                        SharedPreferences.Editor preferenceEditor = preferences.edit();
                        preferenceEditor.putString("username", username.getText().toString());

                        Amplify.API.query(
                                ModelQuery.list(User.class, User.USER_NAME.eq(username.getText().toString())),
                                response -> {
                                    for (User user : response.getData()) {
                                        preferenceEditor.putString("userId", user.getId());
                                        preferenceEditor.apply();
                                        startActivity(new Intent(Login.this, MainActivity.class));
                                    }
                                },
                                error -> Log.e("Amp.listByName", "error: " + error)
                        );
                    },
                    error -> {
                        Log.e("Amplify.login", "Sign in fail: " + error.toString());
                        Message message = new Message();
                        message.arg1 = 0;
                        handler.sendMessage(message);
                    }
            );
        });

        findViewById(R.id.toSignUp).setOnClickListener(view ->
                Login.this.startActivity(new Intent(Login.this, SignUp.class)));
    }
}