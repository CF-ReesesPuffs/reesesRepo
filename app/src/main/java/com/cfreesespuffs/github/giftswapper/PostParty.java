package com.cfreesespuffs.github.giftswapper;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Gift;
import com.cfreesespuffs.github.giftswapper.Activities.MainActivity;
import com.cfreesespuffs.github.giftswapper.Adapters.GiftAdapter;

import java.util.ArrayList;

public class PostParty extends AppCompatActivity implements GiftAdapter.OnCommWithGiftsListener{

    RecyclerView recyclerView;
    ArrayList<Gift> endGifts = new ArrayList<>();
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_party_navigation);
        Toolbar actionBar = findViewById(R.id.post_part_actionbar);
        setSupportActionBar(actionBar);


        OnBackPressedCallback callback = new OnBackPressedCallback(true){
            @Override
            public void handleOnBackPressed() {
                PostParty.this.startActivity(new Intent(PostParty.this, MainActivity.class)); // https://stackoverflow.com/questions/55074497/how-to-add-onbackpressedcallback-to-fragment
            }
        };

        Intent intent = getIntent();

        if(!intent.getExtras().getString("from", "NA").equals("endedList")) {
            getOnBackPressedDispatcher().addCallback(this, callback);
        }

        Button homeDetailButton = PostParty.this.findViewById(R.id.customHomeButton);
        homeDetailButton.setOnClickListener((view)-> {
            Intent goToMainIntent = new Intent(PostParty.this, MainActivity.class);
            PostParty.this.startActivity(goToMainIntent);
        });

        TextView partyName = PostParty.this.findViewById(R.id.partyName);
        partyName.setText(intent.getExtras().getString("title"));

        handler = new Handler(Looper.getMainLooper(),
                new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message message) {
                        connectRecycler();
                        recyclerView.getAdapter().notifyDataSetChanged();
                        return false;
                    }
                }
        );

        Amplify.API.query(
                ModelQuery.list(Gift.class),
                response -> {
                    for(Gift gift : response.getData()){
                        if(gift.getParty().getId().equals(intent.getExtras().getString("partyId"))){
                            endGifts.add(gift);
                        }
                    }
                    handler.sendEmptyMessage(1);
                },
                error -> Log.e("Amplify.gift", "error: " + error)
        );
    }

    public void connectRecycler() {
        recyclerView = findViewById(R.id.postPartyRV);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new GiftAdapter(endGifts, null,this));
    }

    @Override
    public void giftsToDoListener(Gift gift) {}
}