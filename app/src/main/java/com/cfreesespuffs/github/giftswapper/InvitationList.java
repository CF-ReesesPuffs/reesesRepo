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
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Party;
import com.cfreesespuffs.github.giftswapper.Adapters.InvitationAdapter;

import java.util.ArrayList;

public class InvitationList extends AppCompatActivity implements InvitationAdapter.OnInteractWithTaskListener{

    RecyclerView recyclerView;
    ArrayList<Party> partyUserIsInvited;
    Handler handler;
    Handler handleSingleItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation_list);

        connectAdapterToRecycler();

        handler = new Handler(Looper.getMainLooper(),
                new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message msg) {
                        connectAdapterToRecycler();
                        recyclerView.getAdapter().notifyDataSetChanged();
                        return false;
                    }
                });

        handleSingleItem = new Handler(Looper.getMainLooper(),
                new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message msg) {
                        recyclerView.getAdapter().notifyItemInserted(partyUserIsInvited.size() - 1);
                        return false;
                    }
                });

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        Amplify.API.query(
                ModelQuery.list(Party.class),
                response -> {
                    for (Party party : response.getData()) { //TODO: find any invitations,
                        if (preferences.contains("RSVP")) {
                            if (party.getUsers().equals(preferences.getString("RSVP", null))) {
                                partyUserIsInvited.add(party);
                            }
                        } else {
                            partyUserIsInvited.add(party);
                        }
                    }
                    handler.sendEmptyMessage(1);
                },
                error -> Log.e("Amplify", "Failed to retrieve store")
        );
    }

    private void connectAdapterToRecycler() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new InvitationAdapter(partyUserIsInvited, this));
    }

    @Override
    public void taskListener(Party party) {
        Intent intent = new Intent(InvitationList.this, InvitationDetails.class);
        intent.putExtra("partyName", Party.class);
        intent.putExtra("when", Party.class);
        intent.putExtra("setTime", Party.class);
        intent.putExtra("budget", Party.class);
    }
}