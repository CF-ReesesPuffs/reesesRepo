package com.cfreesespuffs.github.giftswapper.activities;

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

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.FriendList;
import com.amplifyframework.datastore.generated.model.User;
import com.cfreesespuffs.github.giftswapper.Adapters.FriendRequestAdapter;
import com.cfreesespuffs.github.giftswapper.Adapters.RequestFriendAdapter;
import com.cfreesespuffs.github.giftswapper.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class FindFriends extends AppCompatActivity implements FriendRequestAdapter.FriendListListener, RequestFriendAdapter.RequestFriendListListener {

    SharedPreferences preferences;
    EditText friendSearchField;
    User currentUser;
    Button findFriend;
    HashMap<String, User> uniqueFriendRequestList = new HashMap<>();
    HashMap<String, User> uniqueFriendList = new HashMap<>();
    HashSet<String> userFriendList = new HashSet<>();
    ArrayList<User> friendList = new ArrayList<>();
    ArrayList<FriendList> requestFriendList = new ArrayList<>();
    Handler handler;
    RecyclerView recyclerView, friendRequestRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_friends);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(FindFriends.this, MainActivity.class);
                FindFriends.this.startActivity(intent);
            }
        };

        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.main_accent)));
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.main_accent));

        handler = new Handler(Looper.getMainLooper(), message -> {
            Log.e("amp.rFL", "in arraylist " + requestFriendList.toString());
            Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged();
            Objects.requireNonNull(friendRequestRV.getAdapter()).notifyDataSetChanged(); // todo: might not be efficient?
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
        findFriend.setBackgroundColor(getResources().getColor(R.color.main_accent));

        findFriend.setOnClickListener((view) -> {
            String friendLc = friendSearchField.getText().toString().toLowerCase();

            ArrayList<FriendList> friendAL = (ArrayList) currentUser.getFriends();
            for (FriendList friendList : friendAL) {
                userFriendList.add(friendList.getUserName());
            }

            Amplify.API.query(
                    ModelQuery.list(User.class, User.SEARCH_NAME.beginsWith(friendLc)),
                    response -> {
                        for (User user : response.getData()) {
                            if (!uniqueFriendList.containsKey(user.getUserName())
                                    && !userFriendList.contains(user.getUserName())
                                    && !user.getUserName().equals(preferences.getString("username", "NA"))) {
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
        recyclerView.setAdapter(new FriendRequestAdapter(friendList, this));

        friendRequestRV = findViewById(R.id.friendRequestRv);
        friendRequestRV.setLayoutManager(new LinearLayoutManager(this));
        friendRequestRV.setAdapter(new RequestFriendAdapter(requestFriendList, this));

        Button addFriends = findViewById(R.id.button_friendRequest);
        addFriends.setOnClickListener(view -> {
            Set<User> friendsToRequest = ((FriendRequestAdapter) Objects.requireNonNull(recyclerView.getAdapter())).friendsToAdd;

            for (User user : friendsToRequest) {

                FriendList friendListToDb = FriendList.builder()
                        .userName(user.getUserName())
                        .accepted(false) // todo: accepted should be isConnected
                        .declined(false) // todo: declined should be isRespondedTo
                        .user(currentUser)
                        .build();

                Amplify.API.mutate(
                        ModelMutation.create(friendListToDb),
                        response2 -> Log.i("Amp.friendlist", "Friend now available!: " + response2.getData()),
                        error2 -> Log.e("Amp.friendlist", "No friend for you! " + error2)
                );
            }
            Intent intent = new Intent(FindFriends.this, MainActivity.class);
            FindFriends.this.startActivity(intent);
        });

        Amplify.API.query(
                ModelQuery.list(FriendList.class, FriendList.USER_NAME.eq(preferences.getString("username", "NA"))),
                response -> {
                    for (FriendList friendList : response.getData()) {
                        if (!friendList.getAccepted()
                                && !friendList.getDeclined()
                                && !uniqueFriendRequestList.containsKey(friendList.getId())) {
                            uniqueFriendRequestList.put(friendList.getId(), friendList.getUser());
                            requestFriendList.add(friendList);
                        }
                    }
                    handler.sendEmptyMessage(1);
                },
                error -> Log.e("Amp.friendRequest", "Failed to find: " + error)
        );
    }

    @Override
    public void listener(User user) {
    }

    @Override
    public void rfListener(FriendList friendRequestor) {
        AlertDialog.Builder confirmFriend = new AlertDialog.Builder(this);
        confirmFriend.setCancelable(true)
                .setTitle("Friends List")
                .setMessage("Would you like to accept this request?")
                .setPositiveButton("Accept",
                        (dialog, which) -> {
                            Log.e("listener.Positive", "Yes Pos.");
                            friendRequestor.accepted = true;

                            Amplify.API.mutate(
                                    ModelMutation.update(friendRequestor),
                                    response -> Log.i("Amp.friendRequestor", "Success"),
                                    error -> Log.e("Amp.friendRequestor", "Fail")
                            );

                            Log.e("Friend.two", "rfListener: " + currentUser.getId());

                            FriendList friendList;
                            friendList = FriendList.builder()
                                    .userName(friendRequestor.getUser().getUserName())
                                    .declined(false)
                                    .accepted(true)
                                    .user(currentUser)
                                    .build();

                            Amplify.API.mutate(
                                    ModelMutation.create(friendList),
                                    response2 -> Log.i("Amp.friendBuild", "Success"),
                                    error2 -> Log.e("Amp.friendBuild", "error" + error2)
                            );

                            requestFriendList.remove(friendRequestor);
                            handler.sendEmptyMessage(1);
                        });
        confirmFriend.setNegativeButton("Decline", (dialog, which) -> {

            Amplify.API.mutate(
                    ModelMutation.delete(friendRequestor),
                    response -> Log.i("Requestor", "DELETED"),
                    error -> Log.e("Requestor", "ERROR: " + error)
            );

            Log.e("listener.Negative", "No Neg.");
        });
        AlertDialog dialog = confirmFriend.create();
        dialog.show();
    }
}
