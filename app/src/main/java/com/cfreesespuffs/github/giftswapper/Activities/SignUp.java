package com.cfreesespuffs.github.giftswapper.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amplifyframework.auth.AuthUserAttributeKey;
import com.amplifyframework.auth.options.AuthSignUpOptions;
import com.amplifyframework.core.Amplify;
import com.cfreesespuffs.github.giftswapper.R;

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

            //TODO: passwords never match
            if (password.getText().toString() != passwordTwo.getText().toString()) {
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

            this.startActivity(intent);
        });
    }
}