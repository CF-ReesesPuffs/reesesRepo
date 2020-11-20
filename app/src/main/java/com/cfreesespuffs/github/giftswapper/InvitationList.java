package com.cfreesespuffs.github.giftswapper;

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
import com.amplifyframework.auth.AuthUser;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.GuestList;
import com.amplifyframework.datastore.generated.model.Party;
import com.amplifyframework.datastore.generated.model.User;
import com.cfreesespuffs.github.giftswapper.Activities.MainActivity;
import com.cfreesespuffs.github.giftswapper.Adapters.InvitationAdapter;
import com.cfreesespuffs.github.giftswapper.Adapters.PartyAdapter;

import java.util.ArrayList;

public class InvitationList extends AppCompatActivity implements PartyAdapter.InteractWithPartyListener {

    RecyclerView recyclerView;
    public ArrayList<Party> parties = new ArrayList<>();
    Handler handler;
    Handler handleParties;
    User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation_list);


//        handler = new Handler(Looper.getMainLooper(),
//                new Handler.Callback() {
//                    @Override
//                    public boolean handleMessage(@NonNull Message msg) {
//                        connectAdapterToRecycler();
//                        recyclerView.getAdapter().notifyDataSetChanged();
//                        return false;
//                    }
//                });

        handleParties = new Handler(Looper.getMainLooper(),
                new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message msg) {
                        if (msg.arg1 == 1) {
                            Log.i("Amplify", "Parties are showing");
                        }
                        recyclerView.getAdapter().notifyItemInserted(parties.size());
                        return false;
                    }
                });
        connectAdapterToRecycler();

        AuthUser authUser = Amplify.Auth.getCurrentUser();
        if (Amplify.Auth.getCurrentUser() != null) {
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
                                                if (party.getInviteStatus().equals("Pending")){
                                                    parties.add(party.getParty());
                                                    Log.i("Amplify.currentUser", "This is the number of parties: " + parties.size());
                                                }
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
    }

    private void connectAdapterToRecycler() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new PartyAdapter(parties, this));
    }

    @Override
    public void listener(Party party) {
        Intent intent = new Intent(InvitationList.this, InvitationDetails.class);
//        intent.putExtra("host", party.get);
        intent.putExtra("partyName", party.getTitle());
        intent.putExtra("when", party.getHostedOn());
        intent.putExtra("setTime", party.getHostedAt());
        intent.putExtra("budget", party.getPrice());
        intent.putExtra("partyId", party.getId());
        this.startActivity(intent);
    }
}