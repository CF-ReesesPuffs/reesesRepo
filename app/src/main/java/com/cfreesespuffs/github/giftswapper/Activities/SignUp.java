package com.cfreesespuffs.github.giftswapper.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amplifyframework.auth.AuthUserAttributeKey;
import com.amplifyframework.auth.options.AuthSignUpOptions;
import com.amplifyframework.core.Amplify;
import com.cfreesespuffs.github.giftswapper.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUp extends AppCompatActivity {

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        handler = new Handler(Looper.getMainLooper(),
                new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message msg) {

                        if (msg.arg1 == 0) {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "Please choose a different username", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            View toastView = toast.getView();
                            toastView.getBackground().setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_IN);
                            toast.show();
                            Log.e("Amp.EmailMatcher", "Bad email. Bad!");
                        }

                        if (msg.arg1 == 1) {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "A confirmation code has been sent to your email", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            View toastView = toast.getView();
                            toastView.getBackground().setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_IN);
                            toast.show();
                        }

                        return false;
                    }
                });

        ((Button) findViewById(R.id.signUpButton)).setOnClickListener(view -> {
            EditText userEmail = findViewById(R.id.emailEt);
            EditText userName = findViewById(R.id.usernameEt);
            EditText password = findViewById(R.id.passwordOneEt);
            EditText passwordTwo = findViewById((R.id.passwordTwoEt));

            String regex = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"; // https://emailregex.com/
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(userEmail.getText().toString().toLowerCase());

            // TODO: ensure no one else has username (query the user list);
            // TODO: if username already picked, create toast.

            if (matcher.matches()) {
                Log.i("Amp.EmailMatcher", "The Email is Good! Clear!");
            } else {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Enter valid email", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                View toastView = toast.getView();
                toastView.getBackground().setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_IN);
                toast.show();
                Log.e("Amp.EmailMatcher", "Bad email. Bad!");
                return;
            }

            if (userName.getText().toString().contains(" ") || userName.getText().toString().equals("")) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Username must be filled and cannot contain spaces.", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                View toastView = toast.getView();
                toastView.getBackground().setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_IN);
                toast.show();
                return;
            }

            if (password.getText().toString().length() < 8) { // https://stackoverflow.com/questions/2506876/how-to-change-position-of-toast-in-android
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Password must be at least 8 characters and no spaces.", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                View toastView = toast.getView(); // find the whole of the toast
                toastView.getBackground().setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_IN); //  to keep the rounded corners https://stackoverflow.com/questions/31175601/how-can-i-change-default-toast-message-color-and-background-color-in-android
//                toastView.setBackgroundColor(Color.YELLOW); // discards the entire Toast view+background, makes the toast bg square hard corners. https://www.zealtyro.com/2020/05/how-to-use-toast-in-android-studio.html#:~:text=To%20change%20the%20Toast%20background%20color%2C%20first%20of,can%20change%20the%20%27view%27%20with%20your%20desired%20one.
                toast.show();
                return;
            }

            if (!password.getText().toString().equals(passwordTwo.getText().toString())) { // https://www.geeksforgeeks.org/character-equals-method-in-java-with-examples/ because PVO forgot that != doesn't work on strings.
                Toast.makeText(this, "Your password doesn't match.", Toast.LENGTH_SHORT).show();
                return;
            }

            Amplify.Auth.signUp(
                    userName.getText().toString().toLowerCase(),
                    password.getText().toString(),
                    AuthSignUpOptions.builder().userAttribute(AuthUserAttributeKey.email(), userEmail.getText().toString()).build(),
                    result -> {
                        Log.i("Amplify.signUp", "Result: " + result.toString());

                        Message message = new Message();
                        message.arg1 = 1;
                        handler.sendMessage(message);

                        Intent intent = new Intent(this, SignupConfirmation.class);
                        intent.putExtra("username", userName.getText().toString());
                        intent.putExtra("password", password.getText().toString());
                        intent.putExtra("email", userEmail.getText().toString());
                        this.startActivity(intent);
                    },
                    error -> {
                        Log.e("Amplify.signUp", "Failed to sign up: " + error.toString());

                        Message message = new Message();
                        message.arg1 = 0;
                        handler.sendMessage(message);

                        return;
                    }
            );
        });
    }
}