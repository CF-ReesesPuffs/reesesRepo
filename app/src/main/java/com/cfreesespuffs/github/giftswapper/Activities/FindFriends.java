package com.cfreesespuffs.github.giftswapper.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.FriendList;
import com.amplifyframework.datastore.generated.model.User;
import com.cfreesespuffs.github.giftswapper.Adapters.FriendAdapter;
import com.cfreesespuffs.github.giftswapper.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class FindFriends extends AppCompatActivity implements FriendAdapter.FriendListListener {

    SharedPreferences preferences;
    EditText friendSearchField;
    User currentUser;
    Button findFriend;
    HashMap<String, User> uniqueFriendList = new HashMap<>();
    ArrayList<User> friendList = new ArrayList<>();
    Handler handler;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_friends);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.green)));
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.green));

        handler = new Handler(Looper.getMainLooper(), message -> {
            Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged();
            return true;
        });

        Amplify.API.query(
                ModelQuery.get(User.class, preferences.getString("userId", "NA")),
                response -> currentUser = response.getData(),
                error -> Log.e("Amp.user", "error: " + error)
        );

        friendSearchField = findViewById(R.id.friendSearchET);
        friendSearchField.setTextColor(Color.parseColor("#000000"));

        findFriend = findViewById(R.id.findFriend_button);
        findFriend.setBackgroundColor(getResources().getColor(R.color.green));

        findFriend.setOnClickListener((view) -> {
            String friendLc = friendSearchField.getText().toString().toLowerCase();

            Amplify.API.query(
                    ModelQuery.list(User.class, User.SEARCH_NAME.beginsWith(friendLc)),
                    response -> {
                        for (User user : response.getData()) {
                            if (!uniqueFriendList.containsKey(user.getUserName())) {
                                uniqueFriendList.put(user.getUserName(), user);
                                friendList.add(user);
                            }
                            handler.sendEmptyMessage(1);
                        }
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
                    },
                    error -> Log.e("Amplify", "failed to find user")
            );
        });

        recyclerView = findViewById(R.id.friendSearchRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new FriendAdapter(friendList, this));

        Button addFriends = findViewById(R.id.button_friendRequest);
        addFriends.setOnClickListener(view -> {
            Set<User> friendsToRequest = ((FriendAdapter) Objects.requireNonNull(recyclerView.getAdapter())).friendsToAdd;
            friendsToRequest.addAll(friendsToRequest);

            for (User user : friendsToRequest) {
                FriendList friendList = FriendList.builder()
                        .userName(user.getUserName())
                        .accepted(false) // todo: accepted should be isConnected
                        .declined(false) // todo: declined should be isRespondedTo
                        .user(currentUser)
                        .build();

                Amplify.API.mutate(
                        ModelMutation.create(friendList),
                        response -> Log.i("Amp.friendlist", "Friend now available!: " + response.getData()),
                        error -> Log.e("Amp.friendlist", "No friend for you!")
                );

            }
            Intent intent = new Intent(FindFriends.this, MainActivity.class);
            FindFriends.this.startActivity(intent);
        });
    }

    @Override
    public void listener(User user) {

    }
}
