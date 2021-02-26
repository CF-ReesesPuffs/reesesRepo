package com.cfreesespuffs.github.giftswapper;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
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

import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.auth.AuthUser;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.GuestList;
import com.amplifyframework.datastore.generated.model.Party;
import com.amplifyframework.datastore.generated.model.User;
import com.cfreesespuffs.github.giftswapper.Activities.InvitationDetails;
import com.cfreesespuffs.github.giftswapper.Adapters.PartyAdapter;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class InvitationList extends AppCompatActivity implements PartyAdapter.InteractWithPartyListener {

    RecyclerView recyclerView;
    public ArrayList<Party> parties = new ArrayList<>();
    Handler handleParties;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation_list);

        handleParties = new Handler(Looper.getMainLooper(),
                new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message msg) {
                        if (msg.arg1 == 1) {
                        }
                        recyclerView.getAdapter().notifyItemInserted(parties.size());
                        return false;
                    }
                });
        connectAdapterToRecycler();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        Amplify.API.query(
                ModelQuery.get(User.class, preferences.getString("userId", "NA")),
                response2 -> {
                    for (GuestList party : response2.getData().getParties()) { // todo: turn into lambda/iterated.
                        if (party.getInviteStatus().equals("Pending")) {
                            parties.add(party.getParty());
                        }
                    }
                    handleParties.sendEmptyMessage(1);
                },
                error -> Log.e("Amplify", "Failed to retrieve store")
        );
    }

    private void connectAdapterToRecycler() {
        recyclerView = findViewById(R.id.postPartyRV);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new PartyAdapter(parties, this));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void listener(Party party) {
        Intent intent = new Intent(InvitationList.this, InvitationDetails.class);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("PST"));

        intent.putExtra("partyName", party.getTitle());
        intent.putExtra("when", party.getHostedOn());
        intent.putExtra("setTime", party.getHostedAt());
        intent.putExtra("budget", party.getPrice());
        intent.putExtra("partyId", party.getId());
        this.startActivity(intent);
    }
}