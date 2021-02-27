package com.cfreesespuffs.github.giftswapper.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.analytics.pinpoint.AWSPinpointAnalyticsPlugin;
import com.amplifyframework.api.ApiOperation;
import com.amplifyframework.api.aws.AWSApiPlugin;
import com.amplifyframework.api.aws.GsonVariablesSerializer;
import com.amplifyframework.api.graphql.GraphQLRequest;
import com.amplifyframework.api.graphql.SimpleGraphQLRequest;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.api.graphql.model.ModelSubscription;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.auth.options.AuthSignOutOptions;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.GuestList;
import com.amplifyframework.datastore.generated.model.Party;
import com.amplifyframework.datastore.generated.model.User;
import com.cfreesespuffs.github.giftswapper.Adapters.PartyAdapter;
import com.cfreesespuffs.github.giftswapper.InvitationList;
import com.cfreesespuffs.github.giftswapper.PendingPage;
import com.cfreesespuffs.github.giftswapper.R;
import com.google.android.material.badge.BadgeDrawable;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity implements PartyAdapter.InteractWithPartyListener {
    public ArrayList<Party> parties = new ArrayList<>();
    public ArrayList<Party> pendingParties = new ArrayList<>();
    public HashMap<String, String> pendingPartiesHM = new HashMap<>();
    Handler handleCheckLoggedIn;
    Handler handleParties;
    RecyclerView partyRecyclerView;
    ImageButton loginButton;
    SharedPreferences preferences;
    MenuItem bellItem;
    LayerDrawable localLayerDrawable;
    boolean[] isSignedIn = {false};
    private FirebaseCrashlytics firebaseCrashlytics;
    private FirebaseAnalytics analytics;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onResume() {
        super.onResume();

        if (!preferences.getString("userId", "NA").equals("NA")) { // might be able to remove.
            Amplify.API.query(
                    ModelQuery.get(User.class, preferences.getString("userId", "NA")),
                    response2 -> {
                        pendingParties.clear();
                        response2.getData().getParties().stream().filter(guestList -> guestList.getInviteStatus().equals("Pending"))
                                .forEach(party -> {
                                    pendingParties.add(party.getParty());
                                    pendingPartiesHM.put(party.getId(), party.getInvitedUser());
                                });

                        Message message = new Message();
                        message.arg1 = 10;
                        handleCheckLoggedIn.sendMessage(message);
                    },
                    error -> Log.e("Amplify", "Failed to retrieve store")
            );

            ApiOperation deleteSubscription = Amplify.API.subscribe(
                    ModelSubscription.onDelete(Party.class),
                    subWork -> Log.i("Amp.subOnDelete", "sub is working"),
                    lessParty -> {
                        Amplify.API.query(
                                ModelQuery.get(User.class, preferences.getString("userId", "NA")),
                                partyList -> {
                                    parties.clear();
                                    for (GuestList party : partyList.getData().getParties()) {
                                        if (party.getInviteStatus().equals("Accepted") && !party.getParty().getIsFinished()) {
                                            parties.add(party.getParty());
                                        }
                                    }
                                    Message message = new Message();
                                    message.arg1 = 1;
                                    handleParties.sendMessage(message);
                                },
                                error -> Log.e("mp.subscribe", "failed sub query")
                        );
                    },
                    subError -> Log.e("Amp.subOnDelete", "failed to subscribe on delete"),
                    () -> Log.i("Amp.subOnDelete", "delete sub complete")
            );

            deleteSubscription.start();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        bellItem = menu.findItem(R.id.mainActivityBadge);
        localLayerDrawable = (LayerDrawable) bellItem.getIcon();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        firebaseCrashlytics = FirebaseCrashlytics.getInstance(); // https://github.com/firebase/quickstart-android/blob/master/crash/app/src/main/java/com/google/samples/quickstart/crash/java/MainActivity.java
        firebaseCrashlytics.log("onCreate"); // https://firebase.google.com/docs/crashlytics/test-implementation?authuser=0&platform=android
        analytics = FirebaseAnalytics.getInstance(this); //this needed permission to network state

        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.green)));

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);  // https://www.geeksforgeeks.org/how-to-change-the-color-of-status-bar-in-an-android-app/
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.green));

        configureAws();
        getIsSignedIn();

        Bundle params = new Bundle();
        params.putString("user_name", preferences.getString("username", "NA"));
        params.putString("isSignedIn", isSignedIn.toString());
        analytics.logEvent("share_image", params);

        createSingleIdGuestListSubscription(preferences.getString("username", "NA"));

//===================================== handler check logged
        handleCheckLoggedIn = new Handler(Looper.getMainLooper(), message -> {

            if (message.arg1 == 6) {
                Intent endPartyIntent = new Intent(MainActivity.this, EndedParties.class);
                MainActivity.this.startActivity(endPartyIntent);
            }

            if (message.arg1 == 5) {
                parties.clear();
                partyRecyclerView.setVisibility(View.INVISIBLE); // VERY BLUNT. Effective, but blunt.
                Toast.makeText(this, "You are now signed out", Toast.LENGTH_LONG).show();
                loginButton.setVisibility(View.VISIBLE);
                Button hostButton = findViewById(R.id.host_party_button);
                hostButton.setVisibility(View.INVISIBLE);
            }

            if (message.arg1 == 10) {
                createBellBadge(pendingPartiesHM.size());
            }

            return false;
        });

//=====================Populate recyclerView===========================================

        handleParties = new Handler(Looper.getMainLooper(), msg -> {
            partyRecyclerView.getAdapter().notifyDataSetChanged();
            return false;
        });

        connectRecycler();

        if (!preferences.getString("userId", "NA").equals("NA")) {
            Amplify.API.query(
                    ModelQuery.get(User.class, preferences.getString("userId", "NA")),
                    response2 -> {
                        pendingParties.clear();
                        for (GuestList party : response2.getData().getParties()) {
                            if (party.getInviteStatus().equals("Pending")) {
                                pendingPartiesHM.put(party.getId(), party.getInvitedUser());
                                pendingParties.add(party.getParty());
                            }
                            if (party.getInviteStatus().equals("Accepted") && !party.getParty().getIsFinished()) {
                                parties.add(party.getParty());
                            }
                            Collections.sort(parties, (party1, party2) -> party1.getPartyDate().compareTo(party2.getPartyDate()));
                        }
                        handleParties.sendEmptyMessage(1);
                        Message message = new Message();
                        message.arg1 = 10;
                        handleCheckLoggedIn.sendMessage(message);
                    },
                    error -> Log.e("Amplify", "Failed to retrieve store")
            );
        }

        Button hostPartyButton = MainActivity.this.findViewById(R.id.host_party_button);
        hostPartyButton.setBackgroundColor(getResources().getColor(R.color.green));
        hostPartyButton.setOnClickListener((view) -> {
            Intent goToHostPartyIntent = new Intent(MainActivity.this, HostParty.class);
            MainActivity.this.startActivity(goToHostPartyIntent);
        });
    }

//=========== RecyclerView =======================

    private void connectRecycler() {
        partyRecyclerView = findViewById(R.id.party_recyclerview);
        partyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        partyRecyclerView.setAdapter(new PartyAdapter(parties, this));
    }

//========== User Sign-in =======================
    public void getIsSignedIn() {
        Amplify.Auth.fetchAuthSession(
                result -> {
                    if (result.isSignedIn()) {
                        isSignedIn[0] = true;
                    } else {
                        MainActivity.this.startActivity(new Intent(MainActivity.this, Login.class));
                    }
                },
                error -> Log.e("Amplify.login", error.toString())
        );
    }

    //========================================================================== aws
    private void configureAws() {
        try {
            Amplify.addPlugin(new AWSApiPlugin());
            Amplify.addPlugin(new AWSCognitoAuthPlugin());
            Amplify.addPlugin(new AWSPinpointAnalyticsPlugin(getApplication()));
            Amplify.configure(getApplicationContext());
        } catch (AmplifyException error) {
            Log.e("MyAmplifyApp", "Could not initialize Amplify");
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
                        preferences.edit().clear().apply();
                        MainActivity.this.startActivity(new Intent(MainActivity.this, Login.class));
                    },
                    error -> Log.e("Auth.logout", "The error: ", error)
            );
        }

        if (item.getItemId() == R.id.completed_parties) {
            Message goToEndPartyActivity = new Message();
            goToEndPartyActivity.arg1 = 6;
            handleCheckLoggedIn.sendMessage(goToEndPartyActivity);
        }

        if (item.getItemId() == R.id.mainActivityBadge) {
            Intent goToNotificationsIntent = new Intent(MainActivity.this, InvitationList.class);
            MainActivity.this.startActivity(goToNotificationsIntent);
        }
        return true;
    }

    private void createBellBadge(int paramInt) {

        Drawable bellBadgeDrawable = localLayerDrawable.findDrawableByLayerId(R.id.badge);
        com.cfreesespuffs.github.giftswapper.Activities.BadgeDrawable badgeDrawable;

        if (bellBadgeDrawable instanceof BadgeDrawable && paramInt < 10) {
            badgeDrawable = (com.cfreesespuffs.github.giftswapper.Activities.BadgeDrawable) bellBadgeDrawable;
        } else {
            badgeDrawable = new com.cfreesespuffs.github.giftswapper.Activities.BadgeDrawable(this);
        }
        badgeDrawable.setCount(paramInt);
        localLayerDrawable.mutate();
        localLayerDrawable.setDrawableByLayerId(R.id.badge, badgeDrawable);
        bellItem.setIcon(localLayerDrawable);
    }

    private GraphQLRequest<GuestList> getPendingParty(String username) { // https://graphql.org/blog/subscriptions-in-graphql-and-relay/
        String document = "subscription getPendingParty($invitedUser: String) { "
                + "onCreateOfUserId(invitedUser: $invitedUser) { "
                + "inviteStatus "
                + "invitedUser "
                + "id "
                + "}"
                + "}";
        return new SimpleGraphQLRequest<>(
                document,
                Collections.singletonMap("invitedUser", username),
                GuestList.class,
                new GsonVariablesSerializer());
    }

    private void createSingleIdGuestListSubscription(String username) {
        Amplify.API.subscribe(getPendingParty(username),
                subCheck -> Log.d("Sub.SingleGuestList", "Connection established. this is ID: " + username),
                response -> {
                    pendingPartiesHM.put(response.getData().getId(), response.getData().getInvitedUser());
                    pendingParties.add(response.getData().getParty());
                    Message message = new Message();
                    message.arg1 = 10;
                    handleCheckLoggedIn.sendMessage(message);
                },
                failure -> Log.e("Sub.SingleGuestList", "failure: " + failure),
                () -> Log.i("Sub.SingleGuestList", "Sub is closed")
        );
    }
}