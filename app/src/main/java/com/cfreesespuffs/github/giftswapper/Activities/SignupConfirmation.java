package com.cfreesespuffs.github.giftswapper.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
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

import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.auth.AuthUserAttributeKey;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.User;
import com.cfreesespuffs.github.giftswapper.R;
import com.google.android.gms.auth.api.Auth;

public class SignupConfirmation extends AppCompatActivity {

    Handler signUpHandler;
    Message message = new Message();
    EditText usernameConfirm;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_confirmation);

        Intent intent = getIntent();
        username = intent.getExtras().getString("username");
        usernameConfirm = findViewById(R.id.usernameConfirmEt);

        findViewById(R.id.signUpConfirmButton).setOnClickListener(view -> {
            EditText confirmCode = findViewById(R.id.codeEt);
            String password = intent.getExtras().getString("password");
            String email = intent.getExtras().getString("email");
            Amplify.Auth.confirmSignUp(
                    usernameConfirm.getText().toString().toLowerCase(),
                    confirmCode.getText().toString(),
                    result -> {
                        message.arg1 = 123;
                        signUpHandler.sendEmptyMessage(message.arg1);
                        User newUser = User.builder()
                                .userName(username)
                                .email(email)
                                .build();
                        Amplify.API.mutate(
                                ModelMutation.create(newUser),
                                response -> Log.i("Amplify.API", "success"),
                                error -> Log.e("Amplify.API", "newUser not created: " + error)
                        );
                        Amplify.Auth.signIn(
                                username.toLowerCase(),
                                password,
                                loginResult -> {

                                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                                    final SharedPreferences.Editor preferenceEditor = preferences.edit();
                                    preferenceEditor.putString("username", username);
                                    preferenceEditor.apply();

                                    this.startActivity(new Intent(SignupConfirmation.this, MainActivity.class));
                                },
                                thisError -> Log.e("Auth.Result", "Fail")
                        );
                    },
                    error -> {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Incorrect Confirmation String. Try Again Please", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        View toastView = toast.getView();
                        toastView.getBackground().setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_IN);
                        toast.show();
                    }
            );
        });

        signUpHandler = new Handler(Looper.getMainLooper(), message -> {
            if (message.arg1 == 123) {
                Context context = getApplicationContext();
                CharSequence text = "User Confirmation Complete!";
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
            return false;
        });

        Button resendCodeButton = findViewById(R.id.resendConfirmB);
        resendCodeButton.setOnClickListener(view -> {

            if (usernameConfirm.getText().toString().isEmpty()) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Input your username below please.", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                View toastView = toast.getView();
                toastView.getBackground().setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_IN);
                toast.show();
                return;
            }

            Amplify.Auth.resendSignUpCode(usernameConfirm.getText().toString().toLowerCase(),
                    success -> {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "New confirmation code has been resent to email.", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        View toastView = toast.getView();
                        toastView.getBackground().setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_IN);
                        toast.show();
                    },
                    error -> Log.e("AuthDemo", "Failed to resend code.", error)
            );
        });
    }
}
