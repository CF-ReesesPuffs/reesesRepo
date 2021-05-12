package com.cfreesespuffs.github.giftswapper.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.amplifyframework.auth.AuthUserAttributeKey;
import com.amplifyframework.auth.options.AuthSignUpOptions;
import com.amplifyframework.core.Amplify;
import com.cfreesespuffs.github.giftswapper.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUp extends AppCompatActivity {

    Handler handler;
    EditText userEmail;
    EditText userName;
    EditText password;
    EditText passwordTwo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable((getResources().getColor(R.color.main_accent))));
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);  // https://www.geeksforgeeks.org/how-to-change-the-color-of-status-bar-in-an-android-app/
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.main_accent));

        handler = new Handler(Looper.getMainLooper(), msg -> {
            if (msg.arg1 == 0) {
                toastEssential("Please choose a different username");
                Log.e("Amp.EmailMatcher", "Bad email. Bad!");
            }

            if (msg.arg1 == 1) {
                toastEssential("A confirmation code has been sent to your email");
            }

            return false;
        });

        userEmail = findViewById(R.id.emailEt);
        userEmail.setTextColor(getResources().getColor(R.color.black));
        userName = findViewById(R.id.usernameEt);
        userName.setTextColor(getResources().getColor(R.color.black));
        password = findViewById(R.id.passwordOneEt);
        password.setTextColor(getResources().getColor(R.color.black));
        passwordTwo = findViewById((R.id.passwordTwoEt));
        passwordTwo.setTextColor(getResources().getColor(R.color.black));

        findViewById(R.id.signUpButton).setOnClickListener(view -> {

            String regex = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"; // https://emailregex.com/
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(userEmail.getText().toString().toLowerCase());

            if (matcher.matches()) {
                Log.i("Amp.EmailMatcher", "The Email is Good! Clear!");
            } else {
                toastEssential("Enter valid email");
                Log.e("Amp.EmailMatcher", "Bad email. Bad!");
                return;
            }

            if (userName.getText().toString().contains(" ") || userName.getText().toString().equals("")) {
                toastEssential("Username must be filled and cannot contain spaces.");
                return;
            }

            if (password.getText().toString().length() < 8) { // https://stackoverflow.com/questions/2506876/how-to-change-position-of-toast-in-android
                toastEssential("Password must be at least 8 characters and no spaces.");
                return;
            }

            if (!password.getText().toString().equals(passwordTwo.getText().toString())) { // https://www.geeksforgeeks.org/character-equals-method-in-java-with-examples/ because PVO forgot that != doesn't work on strings.
                toastEssential("Your password doesn't match.");
                return;
            }

            String lowerCaseUserName = userName.getText().toString().toLowerCase();
            if (lowerCaseUserName.length() > 20) {
                toastEssential("Create a shorter username");
                return;
            }

            Amplify.Auth.signUp(
                    lowerCaseUserName,
                    password.getText().toString(),
                    AuthSignUpOptions.builder().userAttribute(AuthUserAttributeKey.email(), userEmail.getText().toString()).build(),
                    result -> {
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
                    }
            );
        });
    }

    public void toastEssential(String text) {
        Toast toast = Toast.makeText(getApplicationContext(),
                text, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 0, 190);
        View toastView = toast.getView();
        // https://stackoverflow.com/questions/6687666/android-how-to-set-the-colour-of-a-toasts-text if wanted to change text color.
        toastView.setBackground(getDrawable(R.drawable.toast_bg));
        toast.show();
    }

}