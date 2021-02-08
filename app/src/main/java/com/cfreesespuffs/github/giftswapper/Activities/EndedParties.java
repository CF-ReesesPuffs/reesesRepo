package com.cfreesespuffs.github.giftswapper.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.GuestList;
import com.amplifyframework.datastore.generated.model.Party;
import com.amplifyframework.datastore.generated.model.User;
import com.cfreesespuffs.github.giftswapper.Adapters.PartyAdapter;
import com.cfreesespuffs.github.giftswapper.PostParty;
import com.cfreesespuffs.github.giftswapper.R;

import java.util.ArrayList;
import java.util.Objects;

public class EndedParties extends AppCompatActivity implements PartyAdapter.InteractWithPartyListener {

    RecyclerView endedPartiesRv;
    ArrayList<Party> endedParties = new ArrayList<>();
    Handler endPartyHandler;
    Handler oRendHandler;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ended_party);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        endPartyHandler = new Handler(Looper.getMainLooper(), message -> {
            if (message.arg1 == 1) {
                Objects.requireNonNull(endedPartiesRv.getAdapter()).notifyDataSetChanged();
            }
            return false;
        });

        oRendHandler = new Handler(Looper.getMainLooper(),
                new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message msg) {
                        if (msg.arg1 == 1) {
                            Objects.requireNonNull(endedPartiesRv.getAdapter()).notifyDataSetChanged();
                        }
                        return false;
                    }
                });

        endedPartiesRv = findViewById(R.id.giftRecycler);
        endedPartiesRv.setLayoutManager(new LinearLayoutManager(this));
        endedPartiesRv.setAdapter(new PartyAdapter(endedParties, this));

        Amplify.API.query(
                ModelQuery.get(User.class, prefs.getString("userId", "NA")),
                response -> {
                    for (GuestList guestList : response.getData().getParties()) {
                        if (guestList.getParty().getIsFinished()) {
                            endedParties.add(guestList.getParty());
                        }
                    }
                    Message message = new Message();
                    message.arg1 = 1;
                    oRendHandler.sendMessage(message);
                },
                error -> Log.e("End.Party", "Fail: " + error)
        );
    }

    @Override
    public void listener(Party party) {
        Intent goToPartyDetailIntent = new Intent(EndedParties.this, PostParty.class);//we don't have an activity for a single party do we? sent it to invited party for now
        goToPartyDetailIntent.putExtra("title", party.getTitle());
        goToPartyDetailIntent.putExtra("price", party.getPrice());
        goToPartyDetailIntent.putExtra("partyId", party.getId());
        goToPartyDetailIntent.putExtra("when", String.valueOf(party.HOSTED_ON));
        goToPartyDetailIntent.putExtra("setTime", String.valueOf(party.HOSTED_AT));
        goToPartyDetailIntent.putExtra("from", "endedList");
        this.startActivity(goToPartyDetailIntent);
    }
}