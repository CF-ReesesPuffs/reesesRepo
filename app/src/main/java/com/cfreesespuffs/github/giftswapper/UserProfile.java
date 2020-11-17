package com.cfreesespuffs.github.giftswapper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Gift;

import java.util.ArrayList;

public class UserProfile extends AppCompatActivity {

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
//        recyclerView.setAdapter(new ); // Build the adapter.

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
}