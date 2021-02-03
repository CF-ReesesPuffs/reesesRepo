package com.cfreesespuffs.github.giftswapper.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        ((Button) findViewById(R.id.signUpButton)).setOnClickListener(view  -> {
            EditText userEmail = findViewById(R.id.emailEt);
            EditText userName = findViewById(R.id.usernameEt);
            EditText password = findViewById(R.id.passwordOneEt);
            EditText passwordTwo = findViewById((R.id.passwordTwoEt));

            String regex = "^[\\\\w!#$%&'*+/=?`{|}~^-]+(?:\\\\.[\\\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\\\.)+[a-zA-Z]{2,6}$"; // https://howtodoinjava.com/java/regex/java-regex-validate-email-address/
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(userEmail.getText().toString());

            if (matcher.matches()) {
                Log.i("Amp.EmailMatcher", "The Email is Good! Clear!");
            } else {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Please check your email", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                View toastView = toast.getView();
                toastView.getBackground().setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_IN);
                toast.show();
                Log.e("Amp.EmailMatcher", "Bad email. Bad!");
                return;
            }

            if (userName.getText().toString().contains(" ")) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Username cannot contain spaces.", Toast.LENGTH_LONG);
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
                    result -> Log.i("Amplify.signUp", "Result: " + result.toString()),
                    error -> Log.e("Amplify.signUp", "Failed to sign up: " + error.toString())
            );

            Intent intent = new Intent(this, SignupConfirmation.class);
            intent.putExtra("username", userName.getText().toString());
            intent.putExtra("password", password.getText().toString());
            intent.putExtra("email", userEmail.getText().toString());
            this.startActivity(intent);
        });
    }
}