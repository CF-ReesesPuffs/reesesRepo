package com.cfreesespuffs.github.giftswapper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cfreesespuffs.github.giftswapper.Activities.MainActivity;

public class PostParty extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_party_navigation);
        Toolbar actionBar = findViewById(R.id.post_part_actionbar);
        setSupportActionBar(actionBar);

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