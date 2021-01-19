package com.cfreesespuffs.github.giftswapper.Activities;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.analytics.pinpoint.AWSPinpointAnalyticsPlugin;
import com.amplifyframework.api.aws.AWSApiPlugin;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.auth.AuthUser;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.auth.options.AuthSignOutOptions;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.GuestList;
import com.amplifyframework.datastore.generated.model.Party;
import com.amplifyframework.datastore.generated.model.User;
import com.cfreesespuffs.github.giftswapper.Adapters.PartyAdapter;
import com.cfreesespuffs.github.giftswapper.EndedPartyCheck;
import com.cfreesespuffs.github.giftswapper.InvitationList;
import com.cfreesespuffs.github.giftswapper.PendingPage;
import com.cfreesespuffs.github.giftswapper.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity implements PartyAdapter.InteractWithPartyListener {
    public ArrayList<Party> parties = new ArrayList<>();
    Handler handleCheckLoggedIn;
    Handler handleParties;
    RecyclerView partyRecyclerView;
    User currentUser;
    ImageButton loginButton;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configureAws();
        getIsSignedIn();

//============================================================================== handler check logged
        handleCheckLoggedIn = new Handler(Looper.getMainLooper(), message -> {
            if (message.arg1 == 1) {
                if (Amplify.Auth.getCurrentUser() != null) {
                    Log.i("Android.VersionTest", "=== 3 ===");
                    Log.i("Amplify.login", Amplify.Auth.getCurrentUser().getUsername());
                }
            }

            if (message.arg1 == 6) {
                System.out.println("You want to go to your ended parties?");
                Intent endPartyIntent = new Intent(MainActivity.this, EndedPartyCheck.class);
                endPartyIntent.putExtra("userId", currentUser.getId());
                MainActivity.this.startActivity(endPartyIntent);
            }

            if (message.arg1 == 5) {
                parties.clear();
                partyRecyclerView.setVisibility(View.INVISIBLE); // VERY BLUNT. Effective, but blunt.
                Toast.makeText(this, "You are now signed out", Toast.LENGTH_LONG).show();
                loginButton.setVisibility(View.VISIBLE);
                Log.i("Auth.arg1-5", "Logged out via Settings");
            }

            return false;
        });

//=====================Populate recyclerView===========================================

        handleParties = new Handler(Looper.getMainLooper(),
                new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message msg) {
                        if (msg.arg1 == 1) {
                            Log.i("Amplify", "Parties are showing");
                        }
                        partyRecyclerView.getAdapter().notifyItemInserted(parties.size());
                        return false;
                    }
                });

        connectRecycler();

        Log.i("Amplify.authUser", "This is the current user, " + Amplify.Auth.getCurrentUser());
        AuthUser authUser = Amplify.Auth.getCurrentUser();
        if (Amplify.Auth.getCurrentUser() != null) {
            Amplify.API.query(
                    ModelQuery.list(User.class), // we should swap this from .list(.class) to .get(userId) to save cycles & time.
                    response -> {
                        Log.i("Amplify.currentUser", "This is the current user, " + authUser);
                        for (User user : response.getData()) {
                            if (user.getUserName().equalsIgnoreCase(authUser.getUsername())) {
                                currentUser = user;
                                Amplify.API.query(
                                        ModelQuery.get(User.class, currentUser.getId()),
                                        response2 -> {
                                            for (GuestList party : response2.getData().getParties()) {
                                                if (party.getInviteStatus().equals("Accepted")) {
                                                    parties.add(party.getParty());
                                                }
                                            }

                                            System.out.println("Presort: " + parties);

                                            Collections.sort(parties, new Comparator<Party>() {
                                                @Override
                                                public int compare(Party party1, Party party2) {
                                                    return party1.getPartyDate().compareTo(party2.getPartyDate());
                                                }
                                            });

                                            System.out.println("Postsort: " + parties);

                                            handleParties.sendEmptyMessage(1);
                                        },
                                        error -> Log.e("Amplify", "Failed to retrieve store")
                                );
                            }
                        }
                    },
                    error -> {
                        Log.e("Amplify.currentUser", "No current user found");
                    }
            );
        }
//================= invites
        ImageButton notificationButton = MainActivity.this.findViewById(R.id.notification_button);
        notificationButton.setOnClickListener((view) -> {
            Intent goToNotificationsIntent = new Intent(MainActivity.this, InvitationList.class);//Is this were we want to send them?
            MainActivity.this.startActivity(goToNotificationsIntent);
        });

//================= sign up
        ImageButton navButton = MainActivity.this.findViewById(R.id.nav_button);
        navButton.setOnClickListener((view) -> {
            Intent goToNavIntent = new Intent(MainActivity.this, SignUp.class);//Maybe this shouldn't be a button, possibly a spinner?
            MainActivity.this.startActivity(goToNavIntent);
        });

        Button hostPartyButton = MainActivity.this.findViewById(R.id.host_party_button);
        hostPartyButton.setOnClickListener((view) -> {
            Intent goToHostPartyIntent = new Intent(MainActivity.this, HostParty.class);
            MainActivity.this.startActivity(goToHostPartyIntent);
        });

        loginButton = MainActivity.this.findViewById(R.id.login_button);
        if (Amplify.Auth.getCurrentUser() != null) loginButton.setVisibility(View.INVISIBLE);
        loginButton.setOnClickListener((view) -> {
            Intent goToLoginIntent = new Intent(MainActivity.this, Login.class);
            MainActivity.this.startActivity(goToLoginIntent);
        });

    }

//=========== RecyclerView=======================

    private void connectRecycler() {
        partyRecyclerView = findViewById(R.id.party_recyclerview);
        partyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        partyRecyclerView.setAdapter(new PartyAdapter(parties, this));
    }

    // =======================================================================
//========================================================= user -sign-in
    public boolean getIsSignedIn() {
        boolean[] isSignedIn = {false};
        Amplify.Auth.fetchAuthSession(
                result -> {
                    Message message = new Message();
                    if (result.isSignedIn()) {
                        message.arg1 = 1;
                        handleCheckLoggedIn.sendMessage(message);
                    } else {
                        message.arg1 = 0;
                        handleCheckLoggedIn.sendMessage(message);
                    }
                },
                error -> Log.e("Amplify.login", error.toString())
        );
        return isSignedIn[0];
    }

    //========================================================================== aws
    private void configureAws() {
        try {
            Amplify.addPlugin(new AWSApiPlugin());
            Amplify.addPlugin(new AWSCognitoAuthPlugin());
            Amplify.addPlugin(new AWSPinpointAnalyticsPlugin(getApplication()));
            Amplify.configure(getApplicationContext());
        } catch (AmplifyException error) {
            Log.e("MyAmplifyApp", "Could not initialize Amplify", error);
        }
    }

    @Override
    public void listener(Party party) {
        Intent goToPartyDetailIntent = new Intent(MainActivity.this, PendingPage.class);//we don't have an activity for a single party do we? sent it to invited party for now
        goToPartyDetailIntent.putExtra("title", party.getTitle());
        goToPartyDetailIntent.putExtra("price", party.getPrice());
        goToPartyDetailIntent.putExtra("id", party.getId());
        goToPartyDetailIntent.putExtra("date", party.getHostedOn());
        goToPartyDetailIntent.putExtra("time", party.getHostedAt());
        this.startActivity(goToPartyDetailIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.setting_logout) {
            Amplify.Auth.signOut(
                    AuthSignOutOptions.builder().globalSignOut(true).build(),
                    () -> {
                        Log.i("Auth.logout", "Signed out via Settings menu");
                        Message optionMessage = new Message();
                        optionMessage.arg1 = 5;
                        handleCheckLoggedIn.sendMessage(optionMessage); // setting up a message, I was running into issues. sendEmptyMessage worked like a charm.
                    },
                    error -> Log.e("Auth.logout", "The error: ", error)
            );
        }

        if (item.getItemId() == R.id.completed_parties) {
            Message goToEndPartyActivity = new Message();
            goToEndPartyActivity.arg1 = 6;
            handleCheckLoggedIn.sendMessage(goToEndPartyActivity);
        }

        return true;
    }
}

//    Intent goToNavIntent = new Intent(MainActivity.this, SignUp.class);//Maybe this shouldn't be a button, possibly a spinner?
