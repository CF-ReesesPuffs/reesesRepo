package com.cfreesespuffs.github.giftswapper.Activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.amplifyframework.api.aws.GsonVariablesSerializer;
import com.amplifyframework.api.graphql.GraphQLRequest;
import com.amplifyframework.api.graphql.SimpleGraphQLRequest;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.GuestList;
import com.amplifyframework.datastore.generated.model.Party;
import com.amplifyframework.datastore.generated.model.User;
import com.cfreesespuffs.github.giftswapper.Adapters.PartyAdapter;
import com.cfreesespuffs.github.giftswapper.PostParty;
import com.cfreesespuffs.github.giftswapper.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

public class EndedParties extends AppCompatActivity implements PartyAdapter.InteractWithPartyListener {

    RecyclerView endedPartiesRv;
    ArrayList<Party> endedParties = new ArrayList<>();
    HashMap<String, Party> endedPartiesHM = new HashMap<>();
    Handler oRendHandler;
    SharedPreferences prefs;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attended_party_navigation);
        Toolbar actionBar = findViewById(R.id.attended_party_actionbar);
        setSupportActionBar(actionBar);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);  // https://www.geeksforgeeks.org/how-to-change-the-color-of-status-bar-in-an-android-app/
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.main_accent));

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        oRendHandler = new Handler(Looper.getMainLooper(), msg -> {
                    if (msg.arg1 == 1) {
                        Objects.requireNonNull(endedPartiesRv.getAdapter()).notifyDataSetChanged();
                    }
                    return false;
                });

        Button homeDetailButton = EndedParties.this.findViewById(R.id.customHomeButton);
        homeDetailButton.setOnClickListener((view) -> {
            Intent goToMain = new Intent(EndedParties.this, MainActivity.class);
            EndedParties.this.startActivity(goToMain);
        });

        endedPartiesRv = findViewById(R.id.giftRecycler);
        endedPartiesRv.setLayoutManager(new LinearLayoutManager(this));
        endedPartiesRv.setAdapter(new PartyAdapter(endedParties, this));

        Amplify.API.query(
                ModelQuery.get(User.class, prefs.getString("userId", "NA")),
                response -> {
                    if (response.getData().getParties().isEmpty()) return;
                    for (GuestList guestList : response.getData().getParties()) {
                        if (guestList.getParty().getIsFinished()) {
                            endedPartiesHM.put(guestList.getId(), guestList.getParty());
                        }
                    }
                    endedParties.addAll(endedPartiesHM.values());
                    Message message = new Message();
                    message.arg1 = 1;
                    oRendHandler.sendMessage(message);
                },
                error -> Log.e("End.Party", "Fail: " + error)
        );

        Amplify.API.subscribe(getNewGuestList(prefs.getString("username", "NA")),
                subCheck -> Log.i("Amp.gLSub", "success: " + subCheck),
                response -> {
                    endedPartiesHM.remove(response.getData().getId());
                    endedParties.clear();
                    endedParties.addAll(endedPartiesHM.values());

                    Message message = new Message();
                    message.arg1 = 1;
                    oRendHandler.sendMessage(message);
                },
                error -> Log.e("Amp.gLSub", "error: " + error),
                () -> Log.e("Amp.gLSub", "sub is closed")
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

    private GraphQLRequest<GuestList> getNewGuestList(String username) {
        String document = "subscription getNewGuestList ($username: String) { "
                + " onDeleteSpecificGuestList(invitedUser: $username) { "
                + "id "
                + "party { "
                + "id "
                + "}"
                + "}"
                + "}";
        return new SimpleGraphQLRequest<>(
                document,
                Collections.singletonMap("username", username),
                GuestList.class,
                new GsonVariablesSerializer());
    }
}