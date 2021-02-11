package com.cfreesespuffs.github.giftswapper;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Gift;
import com.amplifyframework.datastore.generated.model.GuestList;
import com.amplifyframework.datastore.generated.model.Party;
import com.cfreesespuffs.github.giftswapper.Activities.EndedParties;
import com.cfreesespuffs.github.giftswapper.Activities.MainActivity;
import com.cfreesespuffs.github.giftswapper.Adapters.GiftAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;

public class PostParty extends AppCompatActivity implements GiftAdapter.OnCommWithGiftsListener{

    RecyclerView recyclerView;
    ArrayList<Gift> endGifts = new ArrayList<>();
    Handler handler;
    String partyHost;
    Intent intent;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_party_navigation);
        Toolbar actionBar = findViewById(R.id.post_part_actionbar);
        setSupportActionBar(actionBar);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Handler handler;

        OnBackPressedCallback callback = new OnBackPressedCallback(true){
            @Override
            public void handleOnBackPressed() {
                PostParty.this.startActivity(new Intent(PostParty.this, MainActivity.class)); // https://stackoverflow.com/questions/55074497/how-to-add-onbackpressedcallback-to-fragment
            }
        };

        intent = getIntent();
        Button deleteButton = findViewById(R.id.deleteParty);
//
        if(!intent.getExtras().getString("from", "NA").equals("endedList")) {
            getOnBackPressedDispatcher().addCallback(this, callback);
            deleteButton.setVisibility(View.INVISIBLE);
        }


        Amplify.API.query(
                ModelQuery.get(Party.class, intent.getExtras().getString("partyId", "NA")),
                response -> {
                    partyHost = response.getData().getTheHost().getUserName();
                    preferences = PreferenceManager.getDefaultSharedPreferences(this);
                    if (!partyHost.equals(preferences.getString("username", "NA"))) {
                        deleteButton.setVisibility(View.VISIBLE);
                    }
                },
                error -> Log.e("Query.host", "Error.")
        );

        deleteButton.setOnClickListener((view) -> {
            deleteParty();
        });

        Button homeDetailButton = PostParty.this.findViewById(R.id.customHomeButton);
        homeDetailButton.setOnClickListener((view)-> {
            Intent goToMainIntent = new Intent(PostParty.this, MainActivity.class);
            PostParty.this.startActivity(goToMainIntent);
        });

        TextView partyName = PostParty.this.findViewById(R.id.partyName);
        partyName.setText(intent.getExtras().getString("title"));

        handler = new Handler(Looper.getMainLooper(),
                new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message message) {
                        connectRecycler();
                        recyclerView.getAdapter().notifyDataSetChanged();
                        return false;
                    }
                }
        );

        Amplify.API.query(
                ModelQuery.list(Gift.class, Gift.PARTY_GOER.eq(preferences.getString("username", "NA"))),
                response -> {
                    Log.e("Amp.giftqL", "the intent: " + intent.getExtras().getString("partyId"));
                    Log.e("Amp.giftqL", "the response: " + response);
                    for(Gift gift : response.getData()){
                            endGifts.add(gift);
                    }
                    handler.sendEmptyMessage(1);
                },
                error -> Log.e("Amplify.gift", "error: " + error)
        );
    }

    public void connectRecycler() {
        recyclerView = findViewById(R.id.postPartyRV);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new GiftAdapter(endGifts, null,this));
    }

    @Override
    public void giftsToDoListener(Gift gift) {}

    public void deleteParty() {

        Amplify.API.query(
                ModelQuery.get(Party.class, intent.getExtras().getString("partyId")),
                partyAllToDelete -> {
                    List<GuestList> gLToDelete = partyAllToDelete.getData().getUsers();
                    List<Gift> giftsToDelete = partyAllToDelete.getData().getGifts();

                    for (int i = 0; i < gLToDelete.size(); i++) {
                        Amplify.API.mutate(
                                ModelMutation.delete(gLToDelete.get(i)),
                                response4 -> Log.i("Amp.del.user", "You're outta there !"),
                                error -> Log.e("Amp.del.user", "Error: " + error));
                    }

                    for (int i = 0; i < giftsToDelete.size(); i++) {
                        Amplify.API.mutate(
                                ModelMutation.delete(giftsToDelete.get(i)),
                                response4 -> Log.i("Amp.del.user", "You're outta there!"),
                                error -> Log.e("Amp.del.user", "Error: " + error));
                    }

                    Amplify.API.mutate(
                            ModelMutation.delete(partyAllToDelete.getData()), // as before, it's not enough to have a party, you've got to get it's data too. why?
                            theParty -> Log.i("Amplify.delete", "Gone"),
                            error2 -> Log.e("Amplify.delete", "Where you at? Error: " + error2)
                    );

//                    subscription.cancel();

                    Intent intent = new Intent(this, EndedParties.class);
                    startActivity(intent);

                },
                error -> Log.e("Amp.del.party", "FAIL: " + error));
    }

}