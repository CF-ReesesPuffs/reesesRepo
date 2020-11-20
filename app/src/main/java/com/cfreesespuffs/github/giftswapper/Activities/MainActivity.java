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
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

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
import com.cfreesespuffs.github.giftswapper.InvitationDetails;
import com.cfreesespuffs.github.giftswapper.Adapters.PartyAdapter;
import com.cfreesespuffs.github.giftswapper.InvitationList;
import com.cfreesespuffs.github.giftswapper.PendingPage;
import com.cfreesespuffs.github.giftswapper.R;
import com.cfreesespuffs.github.giftswapper.UserProfile;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements PartyAdapter.InteractWithPartyListener{
    public ArrayList<Party> parties= new ArrayList<>();
    Handler handlecheckLoggedIn;
    Handler handleParties;
    RecyclerView partyRecyclerView;
    User currentUser;
    AWSCognitoAuthPlugin auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configureAws();
        getIsSignedIn();

//        Log.i("Auth.detail", "Auth: " + auth.getCurrentUser());
//============================================================================== handler check logged
        handlecheckLoggedIn = new Handler(Looper.getMainLooper(), message -> {
            if (message.arg1 == 0) {
                Log.i("Amplify.login", "They weren't logged in");
                ImageButton profile = MainActivity.this.findViewById(R.id.profile_button);
                profile.setVisibility(View.INVISIBLE);
            } else if (message.arg1 == 1) {
                if (Amplify.Auth.getCurrentUser() != null) {
                    Log.i("Amplify.login", Amplify.Auth.getCurrentUser().getUsername());
                }
            } else {
                Log.i("Amplify.login", "Send true or false pls");
            }
            return false;
        });

//=====================Popluate recyclerView===========================================

        handleParties = new Handler(Looper.getMainLooper(),
                new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message msg) {
                        if(msg.arg1 == 1){
                            Log.i("Amplify", "Parties are showing");
                        }
                        partyRecyclerView.getAdapter().notifyItemInserted(parties.size());
                        return false;
                    }
                });

        connectRecycler();

        Log.i("Amplify.authUser", "This is the current user, " + Amplify.Auth.getCurrentUser());
        AuthUser authUser = Amplify.Auth.getCurrentUser();
        if(Amplify.Auth.getCurrentUser() != null) {
            Amplify.API.query(
                    ModelQuery.list(User.class),
                    response -> {
                        for (User user : response.getData()) {
                            if (user.getUserName().contains(authUser.getUsername())) {
                                currentUser = user;
                                Log.i("Amplify.currentUser", "This is the current user, " + currentUser);
                                Amplify.API.query(
                                        ModelQuery.get(User.class, currentUser.getId()),
                                        response2 -> {
                                            for (GuestList party : response2.getData().getParties()) {
                                                if(party.getInviteStatus().equals("Accepted")){
                                                    parties.add(party.getParty());
                                                }
                                                Log.i("Amplify.currentUser", "This is the number of parties: " + parties.size());

                                            }
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
//===================== Buttons =====================================
        ImageButton profileButton = MainActivity.this.findViewById(R.id.profile_button);
        profileButton.setOnClickListener((view)-> {
            Intent goToProfileIntent = new Intent(MainActivity.this, UserProfile.class);
            MainActivity.this.startActivity(goToProfileIntent);
        });
//================= invites
        ImageButton notificationButton = MainActivity.this.findViewById(R.id.notification_button);
        notificationButton.setOnClickListener((view)-> {
            Intent goToNotificationsIntent = new Intent(MainActivity.this, InvitationList.class);//Is this were we want to send them?
            MainActivity.this.startActivity(goToNotificationsIntent);
        });

//================= sign up
        ImageButton navButton= MainActivity.this.findViewById(R.id.nav_button);
        navButton.setOnClickListener((view)-> {
            Intent goToNavIntent = new Intent(MainActivity.this, SignUp.class);//Maybe this shouldn't be a button, possibly a spinner?
            MainActivity.this.startActivity(goToNavIntent);
        });

        Button hostPartyButton= MainActivity.this.findViewById(R.id.host_party_button);
        hostPartyButton.setOnClickListener((view)-> {
            Intent goToHostPartyIntent = new Intent(MainActivity.this, HostParty.class);
            MainActivity.this.startActivity(goToHostPartyIntent);
        });

        ImageButton loginButton = MainActivity.this.findViewById(R.id.login_button);
        loginButton.setOnClickListener((view)-> {//maybe make this button invisible when clicked
            Intent goToLoginIntent = new Intent(MainActivity.this, Login.class);
            MainActivity.this.startActivity(goToLoginIntent);
        });

        ImageButton logoutButton= MainActivity.this.findViewById(R.id.logout_button);
        logoutButton.setOnClickListener((view)-> {
            logoutButton.setVisibility(View.INVISIBLE);

            Amplify.Auth.signOut(
                    AuthSignOutOptions.builder().globalSignOut(true).build(),
                    () -> Log.i("AuthQuickstart", "Signed out globally"),
                    error -> Log.e("AuthQuickstart", error.toString())

            );
        });


    }
    //=========== RecyclerView=======================
    private void connectRecycler(){
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
                    Log.i("Amplify.login", result.toString());
                    Message message = new Message();
                    if(result.isSignedIn()) {
                        message.arg1 = 1;
                        handlecheckLoggedIn.sendMessage(message);
                    } else {
                        message.arg1 = 0;
                        handlecheckLoggedIn.sendMessage(message);
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
        goToPartyDetailIntent.putExtra("title",party.getTitle());
        goToPartyDetailIntent.putExtra("price",party.getPrice());
        goToPartyDetailIntent.putExtra("id",party.getId());
        goToPartyDetailIntent.putExtra("date",party.getHostedOn());
        goToPartyDetailIntent.putExtra("time",party.getHostedAt());
        this.startActivity(goToPartyDetailIntent);
    }
}