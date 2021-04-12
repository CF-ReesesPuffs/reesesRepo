package com.cfreesespuffs.github.giftswapper.Activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.FriendList;
import com.amplifyframework.datastore.generated.model.User;
import com.cfreesespuffs.github.giftswapper.Adapters.FriendPageAdapter;
import com.cfreesespuffs.github.giftswapper.Adapters.PartyAdapter;
import com.cfreesespuffs.github.giftswapper.R;

import java.util.ArrayList;
import java.util.Objects;

public class FriendsPage extends AppCompatActivity implements FriendPageAdapter.FriendPageListener {

    RecyclerView friendsRv;
    SharedPreferences prefs;
    Handler handler;
    ArrayList<FriendList> friends = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_page_navigation);
        Toolbar actionBar = findViewById(R.id.friend_page_actionBar);
        setSupportActionBar(actionBar);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);  // https://www.geeksforgeeks.org/how-to-change-the-color-of-status-bar-in-an-android-app/
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.green));

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        handler = new Handler(Looper.getMainLooper(), msg -> {
            if (msg.arg1 == 1) {
                friendsRv.getAdapter().notifyDataSetChanged();
            }
            return false;
        });

        Button homeDetailButton = FriendsPage.this.findViewById(R.id.customHomeButton);
        homeDetailButton.setOnClickListener((view) -> {
            Intent goToMain = new Intent(FriendsPage.this, MainActivity.class);
            FriendsPage.this.startActivity(goToMain);
        });

        friendsRv = findViewById(R.id.friendRecycler);
        friendsRv.setLayoutManager(new LinearLayoutManager(this));
        friendsRv.setAdapter(new FriendPageAdapter(friends, this));

        Amplify.API.query(
                ModelQuery.get(User.class, prefs.getString("userId", "NA")),
                response -> {
                    Log.e("Amp.query", "Response: " + response);
                    if (response.getData().getFriends().isEmpty()) return;
                    for (FriendList friend : response.getData().getFriends()) {
                        friends.add(friend);
                    }
                    handler.sendEmptyMessage(1);
                },
                error -> Log.e("Amp.query", "Error: " + error)
        );
    }

    @Override
    public void listener(FriendList friendList) {    }
}