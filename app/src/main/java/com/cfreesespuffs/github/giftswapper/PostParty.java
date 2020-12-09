package com.cfreesespuffs.github.giftswapper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cfreesespuffs.github.giftswapper.Activities.MainActivity;

public class PostParty extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_party);

//        ImageButton homeDetailButton = PostParty.this.findViewById(R.id.homePartyDetailButton);
//        homeDetailButton.setOnClickListener((view)-> {
//            Intent goToMainIntent = new Intent(PostParty.this, MainActivity.class);
//            PostParty.this.startActivity(goToMainIntent);
//        });

        Intent intent = getIntent();
        TextView partyName = PostParty.this.findViewById(R.id.partyName);
        partyName.setText(intent.getExtras().getString("title"));
    }
}