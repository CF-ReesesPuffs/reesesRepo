package com.cfreesespuffs.github.giftswapper.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import com.amplifyframework.datastore.generated.model.Party;
import com.cfreesespuffs.github.giftswapper.InvitedPartyPage;
import com.cfreesespuffs.github.giftswapper.Adapters.PartyAdapter;
import com.cfreesespuffs.github.giftswapper.R;
import com.cfreesespuffs.github.giftswapper.UserProfile;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements PartyAdapter.InteractWithPartyListener{
    public ArrayList<Party> parties;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
//==============================================================================


    }


    @Override
    public void listener(Party party) {
        Intent goToPartyDetailInent = new Intent(MainActivity.this, InvitedPartyPage.class);//we don't have an activity for a single party do we? sent it to invited party for now
        goToPartyDetailInent.putExtra("title",party.getTitle());
        goToPartyDetailInent.putExtra("price",party.getPrice());
        this.startActivity(goToPartyDetailInent);
    }
}