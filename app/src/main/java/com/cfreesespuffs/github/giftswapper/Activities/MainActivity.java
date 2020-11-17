package com.cfreesespuffs.github.giftswapper.Activities;

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
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.auth.options.AuthSignOutOptions;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Party;
import com.cfreesespuffs.github.giftswapper.InvitedPartyPage;
import com.cfreesespuffs.github.giftswapper.Adapters.PartyAdapter;
import com.cfreesespuffs.github.giftswapper.R;
import com.cfreesespuffs.github.giftswapper.UserProfile;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements PartyAdapter.InteractWithPartyListener{
    public ArrayList<Party> parties;
    Handler handlecheckLoggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configureAws();
        getIsSignedIn();
        //=========== RecyclerView=======================
        RecyclerView partyRecyclerView = findViewById(R.id.party_recyclerview);
        partyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        partyRecyclerView.setAdapter(new PartyAdapter(parties, this)); //will we need to create a PartyAdapter?
// =======================================================================

//===================== Buttons =====================================
        ImageButton profileButton = MainActivity.this.findViewById(R.id.profile_button);
        profileButton.setOnClickListener((view)-> {
            Intent goToProfileIntent = new Intent(MainActivity.this, UserProfile.class);
            MainActivity.this.startActivity(goToProfileIntent);
        });

        ImageButton notificationButton = MainActivity.this.findViewById(R.id.notification_button);
        notificationButton.setOnClickListener((view)-> {
            Intent goToNotificationsIntent = new Intent(MainActivity.this, InvitedPartyPage.class);//Is this were we want to send them?
            MainActivity.this.startActivity(goToNotificationsIntent);
        });

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
//==============================================================================
        handlecheckLoggedIn = new Handler(Looper.getMainLooper(), message -> {
            if (message.arg1 == 0) {
                Log.i("Amplify.login", "They weren't logged in");
                ImageButton profile = MainActivity.this.findViewById(R.id.profile_button);
                profile.setVisibility(View.INVISIBLE);
            } else if (message.arg1 == 1) {
                Log.i("Amplify.login", Amplify.Auth.getCurrentUser().getUsername());
            } else {
                Log.i("Amplify.login", "Send true or false pls");
            }
            return false;
        });

    }

    public boolean getIsSignedIn() {
        boolean[] isSingedIn = {false};
        Amplify.Auth.fetchAuthSession(
                result -> {
                    Log.i("Amplify.login", result.toString());
                    Message message = new Message();
                    if (result.isSignedIn()) {
                        message.arg1 = 1;
                        handlecheckLoggedIn.sendMessage(message);
                    } else {
                        message.arg1 = 0;
                        handlecheckLoggedIn.sendMessage(message);
                    }
                },
                error -> Log.e("Amplify.login", error.toString())
        );
        return isSingedIn[0];
    }
    private void configureAws() {
        try {
            Amplify.addPlugin(new AWSApiPlugin());
            Amplify.addPlugin(new AWSCognitoAuthPlugin());
//            Amplify.addPlugin(new AWSS3StoragePlugin());
            Amplify.addPlugin(new AWSPinpointAnalyticsPlugin(getApplication()));
            Amplify.configure(getApplicationContext());
        } catch (AmplifyException error) {
            Log.e("MyAmplifyApp", "Could not initialize Amplify", error);
        }
    }


    @Override
    public void listener(Party party) {
        Intent goToPartyDetailInent = new Intent(MainActivity.this, InvitedPartyPage.class);//we don't have an activity for a single party do we? sent it to invited party for now
        goToPartyDetailInent.putExtra("title",party.getTitle());
        goToPartyDetailInent.putExtra("price",party.getPrice());
        this.startActivity(goToPartyDetailInent);
    }
}