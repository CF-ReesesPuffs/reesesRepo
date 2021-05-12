package com.cfreesespuffs.github.giftswapper.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.User;
import com.cfreesespuffs.github.giftswapper.R;

public class SignupConfirmation extends AppCompatActivity {

    Handler signUpHandler;
    Message message = new Message();
    EditText usernameConfirm, confirmCode;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_confirmation);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable((getResources().getColor(R.color.main_accent))));
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);  // https://www.geeksforgeeks.org/how-to-change-the-color-of-status-bar-in-an-android-app/
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.main_accent));

        Intent intent = getIntent();
        username = intent.getExtras().getString("username");

//        usernameConfirm = findViewById(R.id.usernameConfirmEt);
//        usernameConfirm.setTextColor(getResources().getColor(R.color.black));
//        usernameConfirm.setText(username);

        TextView usernameTv = findViewById(R.id.codeConfirmTv);
        usernameTv.setText(String.format("Confirmation Code for %s", username));

        confirmCode = findViewById(R.id.codeEt);
        confirmCode.setTextColor(getResources().getColor(R.color.black));

        findViewById(R.id.signUpConfirmButton).setOnClickListener(view -> {
            String password = intent.getExtras().getString("password");
            String email = intent.getExtras().getString("email");
            Amplify.Auth.confirmSignUp(
                    usernameConfirm.getText().toString().toLowerCase(),
                    confirmCode.getText().toString(),
                    result -> {
                        String usernameLowerCase = usernameConfirm.getText().toString().toLowerCase();
                        message.arg1 = 123;
                        signUpHandler.sendEmptyMessage(message.arg1);
                        User newUser = User.builder()
                                .userName(username)
                                .searchName(usernameLowerCase)
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

                                    Amplify.API.query(
                                            ModelQuery.list(User.class, User.SEARCH_NAME.eq(usernameLowerCase)),
                                            response -> {
                                                for (User user : response.getData()) {
                                                    Log.e("mQ", "userId: " + user.getId());
                                                    preferenceEditor.putString("userId", user.getId());
                                                    preferenceEditor.apply();
                                                    this.startActivity(new Intent(SignupConfirmation.this, MainActivity.class));
                                                }
                                            },
                                            error -> Log.e("Amp.userId", "FAIL: " + error)
                                    );

                                },
                                thisError -> Log.e("Auth.Result", "Fail")
                        );
                    },
                    error -> {
                        message.arg1 = 456;
                        signUpHandler.sendMessage(message);
                    }
            );
        });

        signUpHandler = new Handler(Looper.getMainLooper(), message -> {
            if (message.arg1 == 123) {
                toastEssential("User Confirmation Complete!");
            }
            if (message.arg1 == 456) {
                toastEssential("Incorrect Confirmation String. Try Again Please");
            }
            return false;
        });

        Button resendCodeButton = findViewById(R.id.resendConfirmB);
        resendCodeButton.setOnClickListener(view -> {

            if (usernameConfirm.getText().toString().isEmpty()) {
                toastEssential("Input your username below please.");
                return;
            }

            Amplify.Auth.resendSignUpCode(usernameConfirm.getText().toString().toLowerCase(),
                    success -> {
                        toastEssential("New confirmation code has been resent to email.");
                    },
                    error -> Log.e("AuthDemo", "Failed to resend code.", error)
            );
        });
    }

    public void toastEssential(String text) {
        Toast toast = Toast.makeText(getApplicationContext(),
                text, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 0, 190); // todo: make it display at bottom.
        View toastView = toast.getView();
        toastView.setBackground(getDrawable(R.drawable.toast_bg));
        toast.show();
    }

}
