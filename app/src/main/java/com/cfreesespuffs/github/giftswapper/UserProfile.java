package com.cfreesespuffs.github.giftswapper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Gift;
import com.amplifyframework.datastore.generated.model.User;
import com.cfreesespuffs.github.giftswapper.Adapters.GiftAdapter;
import com.cfreesespuffs.github.giftswapper.Adapters.PartyAdapter;

import java.util.ArrayList;

public class UserProfile extends AppCompatActivity implements GiftAdapter.OnCommWithGiftsListener { // Do need to actually implement and create the listener, *on* this page.

    RecyclerView recyclerView;
    ArrayList<Gift> giftsOfUser;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        handler = new Handler(Looper.getMainLooper(),
                new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message msg) {
                        recyclerView.getAdapter().notifyDataSetChanged();
                        return true;
                    }
                });

        recyclerView = findViewById(R.id.allTheGifts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new GiftAdapter(giftsOfUser, this)); // Build the adapter.

        String username = Amplify.Auth.getCurrentUser().getUsername();
        TextView usernameTv = findViewById(R.id.userNameProfileTv);
        usernameTv.setText(String.format("%s's Party Central", username));

        Amplify.API.query(
                ModelQuery.list(Gift.class),
                response -> {
                    for (Gift gift : response.getData().getItems()) {
                        if (gift.getUser().getUserName().contains(username)) {
                            giftsOfUser.add(gift);
                        }
                    }
                    Log.i("Amplify.query", "Attending this many parties: " + giftsOfUser.size());
                },
                error -> Log.e ("Amplify.query", "No tasks could be received.")
        );
    }

    @Override
    public void giftListener(Gift gift) {
        Intent goToGiftPage = new Intent(UserProfile.this, UserProfile.class);
        goToGiftPage.putExtra("giftTitle", gift.getTitle());
        this.startActivity(goToGiftPage);
    }
}