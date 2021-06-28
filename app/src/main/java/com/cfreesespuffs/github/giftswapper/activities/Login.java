package com.cfreesespuffs.github.giftswapper.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
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
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.User;
import com.cfreesespuffs.github.giftswapper.R;

public class Login extends AppCompatActivity {

    Handler handler;
    VideoView videoView;
    MediaPlayer vVmP;
    int mCurrentVideoPos;
    EditText username;
    EditText password;

    @SuppressLint("UseCompatLoadingForDrawables") // Might fix drawable issue?
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        videoView = findViewById(R.id.videoViewLogin); // https://www.youtube.com/watch?v=9NrSQfcurUk&t=229s or https://www.youtube.com/watch?v=WLwQ3SJjWfY&t=648s
        Uri uri2 = Uri.parse("android.resource://"
                + getPackageName()
                + "/"
                + R.raw.welcomefade);

        videoView.setVideoURI(uri2);
        videoView.start();

//        MediaController mediaController = new MediaController(this); // only necessary if you want media controls. https://www.codeitbro.com/android-videoview-example-project/#:~:text=Android%20VideoView%20Tutorial%20with%20Example%20Project%20in%20Android,video%20through%20the%20Internet.%20...%20More%20items...
//        mediaController.setAnchorView(videoView);
//        videoView.setMediaController(mediaController);

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                vVmP = mp;
                vVmP.setLooping(true);
                if (mCurrentVideoPos != 0) {
                    vVmP.seekTo(mCurrentVideoPos);
                    vVmP.start();
                }
            }
        });

        handler = new Handler(Looper.getMainLooper(), msg -> {
            if (msg.arg1 == 0) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Please check your email and username are correct.", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 0, 190);
                View toastView = toast.getView();
                toastView.setBackground(getDrawable(R.drawable.toast_bg));
                toast.show();
            }
            return false;
        });

        username = findViewById(R.id.usernameLogin);
        username.setTextColor(getResources().getColor(R.color.black));
        password = findViewById(R.id.passwordLogin);
        password.setTextColor(getResources().getColor(R.color.black));

        findViewById(R.id.loginButton).setOnClickListener(view -> {

            String userNameLc = username.getText().toString().toLowerCase();

            Amplify.Auth.signIn(
                    userNameLc,
                    password.getText().toString(),
                    result -> {
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                        SharedPreferences.Editor preferenceEditor = preferences.edit();
                        preferenceEditor.putString("username", username.getText().toString());

                        Amplify.API.query(
                                ModelQuery.list(User.class, User.SEARCH_NAME.eq(userNameLc)),
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

    @Override // best practice for autoplaying bg video
    protected void onPause() {
        super.onPause();
        mCurrentVideoPos = vVmP.getCurrentPosition();
        videoView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoView.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        vVmP.release();
        vVmP = null;
    }

}