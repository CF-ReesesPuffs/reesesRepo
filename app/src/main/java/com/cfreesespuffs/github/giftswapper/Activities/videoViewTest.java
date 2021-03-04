package com.cfreesespuffs.github.giftswapper.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

import com.cfreesespuffs.github.giftswapper.R;

public class videoViewTest extends AppCompatActivity {

    VideoView videoView;
    MediaPlayer vVmP;
    int mCurrentVideoPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view_test);

        MediaController mediaController = new MediaController(this);
        videoView = findViewById(R.id.videoView); // https://www.youtube.com/watch?v=9NrSQfcurUk&t=229s
        mediaController.setAnchorView(videoView);
        Uri uri = Uri.parse("android.resource://" // todo: add file
                + getPackageName()
                + "/"
                + R.raw.mvideo);

        videoView.setVideoURI(uri);
        videoView.setMediaController(mediaController);

        videoView.start();

//        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() { // https://www.youtube.com/watch?v=WLwQ3SJjWfY&t=645s
//            @Override
//            public void onPrepared(MediaPlayer mp) {
//                vVmP = mp;
//                vVmP.setLooping(true);
//                if (mCurrentVideoPos != 0) {
//                    vVmP.seekTo(mCurrentVideoPos);
//                    Log.e("video.play", "Play!");
//                    vVmP.start();
//                }
//            }
//        });

    }
}