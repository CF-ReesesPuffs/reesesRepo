package com.cfreesespuffs.github.giftswapper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class PostParty extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_party);

        Intent intent = getIntent();

        TextView partyName = PostParty.this.findViewById(R.id.homePartyTitleButton);
        partyName.setText(intent.getExtras().getString("partyName"));
    }
}