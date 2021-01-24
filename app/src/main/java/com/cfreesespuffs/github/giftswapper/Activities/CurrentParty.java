package com.cfreesespuffs.github.giftswapper.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.amplifyframework.api.ApiOperation;
import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.api.graphql.model.ModelSubscription;
import com.amplifyframework.auth.AuthUser;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Gift;
import com.amplifyframework.datastore.generated.model.GuestList;
import com.amplifyframework.datastore.generated.model.Party;
import com.amplifyframework.datastore.generated.model.User;
import com.cfreesespuffs.github.giftswapper.Adapters.CurrentPartyUserAdapter;
import com.cfreesespuffs.github.giftswapper.Adapters.GiftAdapter;
import com.cfreesespuffs.github.giftswapper.PostParty;
import com.cfreesespuffs.github.giftswapper.R;

import java.util.ArrayList;
import java.util.HashMap;

public class CurrentParty extends AppCompatActivity implements GiftAdapter.OnCommWithGiftsListener, CurrentPartyUserAdapter.OnInteractWithTaskListener{
    ArrayList<GuestList> guestList = new ArrayList<>();
    HashMap<Integer, GuestList> gLHashMap = new HashMap<>();
    ArrayList<Gift> giftList = new ArrayList<>();
    Handler handler, handlerGeneral;
    RecyclerView recyclerView;
    RecyclerView recyclerView2;
    User amplifyUser;
    Intent intent;
    String partyId;
    Party party;
    int currentTurn = 100; // this is not smart :P
    ApiOperation subscription;
    AuthUser authUser;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_party);


        intent = getIntent();
        partyId = intent.getExtras().getString("id");
        authUser = Amplify.Auth.getCurrentUser();

        Amplify.API.query(
                ModelQuery.list(User.class),
                response ->{
                    for(User user : response.getData()){
                        if(user.getUserName().equalsIgnoreCase(authUser.getUsername())){
                            amplifyUser = user;
                        }
                    }
                },
                error -> Log.e("amplify.user", String.valueOf(error))
        );

        TextView partyName = CurrentParty.this.findViewById(R.id.partyName);
        partyName.setText(intent.getExtras().getString("thisPartyId"));

        handler = new Handler(Looper.getMainLooper(),
                new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message msg) {
                        connectAdapterToRecycler();
                        connectAdapterToRecycler2();
                        recyclerView.getAdapter().notifyDataSetChanged();
                        return false;
                    }
                });

        handlerGeneral = new Handler(Looper.getMainLooper(), message -> { // Todo: confirm working.
            Log.e ("Amp.TurnNotice", "Hit the turnNotice");
            if (message.arg1 == 1) {
                Toast.makeText(this, "It is your turn, pick a gift!", Toast.LENGTH_LONG).show();
            }
            return false;
        });

        Amplify.API.query(
                ModelQuery.get(Party.class, intent.getExtras().getString("id")),
                response -> {
                    party = response.getData();
                    for (GuestList user : response.getData().getUsers()) {
                        if(user.getInviteStatus().equals("Accepted")){
                            guestList.add(user);
                            gLHashMap.put(user.getTurnOrder(), user);
                            if (!user.getTakenTurn() && user.getTurnOrder() < currentTurn) currentTurn = user.getTurnOrder();
                        }
                    }
                    handler.sendEmptyMessage(1);
                },
                error -> Log.e("Amplify", "Failed to retrieve store")
        );

        Amplify.API.query(
                ModelQuery.get(Party.class, partyId),
                response -> {
                    Log.i("Test party.gift", "===" + response.getData().getGifts());

                    for (Gift giftBrought : response.getData().getGifts()) {
                        giftList.add(giftBrought);
                        }
                    handler.sendEmptyMessage(1);
                },
                error -> Log.e("Amplify", "Failed to retrieve store")
        );

        String SUBSCRIBETAG = "Amplify.subscription";

        ApiOperation guestListSub = Amplify.API.subscribe( // Todo: this might be awful code if more than one party is ongoing.
                ModelSubscription.onUpdate(GuestList.class),
                onEstablished -> Log.i(SUBSCRIBETAG, "Guestlist Sub established."),
                createdItem -> {
                    GuestList updatedGl = createdItem.getData();
                    gLHashMap.replace(updatedGl.getTurnOrder(), updatedGl);
                    for (int i = 1; i < gLHashMap.size()+1; i++) {
                        if (!gLHashMap.get(i).getTakenTurn()) {
                            currentTurn = i;
                            if (gLHashMap.get(i).getUser().getUserName().equalsIgnoreCase(amplifyUser.getUserName())) { // Todo: check that this works.
                                Message turnAlertMsg = new Message();
                                turnAlertMsg.arg1 = 1;
                                handlerGeneral.sendMessage(turnAlertMsg);
                            }
                            break;
                        }
                    }
                    Log.i("Amp.NewCurrentTurn", "This is the turn: " + currentTurn);
                },
                onFailure -> Log.i(SUBSCRIBETAG, onFailure.toString()),
                () -> Log.i(SUBSCRIBETAG, "Subscription completed")
        );

        subscription = Amplify.API.subscribe(
                ModelSubscription.onUpdate(Gift.class),
                onEstablished -> Log.i(SUBSCRIBETAG, "Subscription established"),
                createdItem -> {
                    Log.i(SUBSCRIBETAG, "Subscription created: " + ((Gift) createdItem.getData()).getTitle());

                    Amplify.API.query(
                            ModelQuery.get(Party.class, partyId),
                            response -> {
                                Party completedParty = response.getData();
                                giftList.clear();
                                for (Gift giftBrought : response.getData().getGifts()) {
                                    giftList.add(giftBrought);
                                }

                                Boolean allTaken = true;
                                for (Gift gift : giftList) {
                                    if (gift.getPartyGoer().equals("TBD")) allTaken = false;
                                }

                                if (allTaken) {
                                    Intent headToPostParty = new Intent(CurrentParty.this, PostParty.class);

                                    headToPostParty.putExtra("title", completedParty.getTitle());
                                    headToPostParty.putExtra("partyId", completedParty.getId());
                                    headToPostParty.putExtra("when", String.valueOf(completedParty.HOSTED_ON));
                                    headToPostParty.putExtra("setTime", String.valueOf(completedParty.HOSTED_AT));

                                    subscription.cancel(); // KILL THE SUBSCRIPTION. BURN IT DOWN.

                                    party.isFinished = true;

                                    Amplify.API.query(
                                            ModelMutation.update(party), // TODO: not certain if party will update as global variable. But will find out.
                                            response2 -> Log.i("Mutation.thisParty", "Party: Complete!"),
                                            error -> Log.e("Mutation.thisParty", "Party mutate: FAIL")
                                    );

                                    startActivity(headToPostParty);
                                }
                                handler.sendEmptyMessage(1);
                            },
                            error -> Log.e("Amplify", "Failed to retrieve store")
                    );
                },
                onFailure -> {
                    Log.i(SUBSCRIBETAG, onFailure.toString());
                },
                () -> Log.i(SUBSCRIBETAG, "Subscription completed")
        );

        ImageButton homeDetailButton = CurrentParty.this.findViewById(R.id.goHome);
        homeDetailButton.setOnClickListener((view)-> {
            Intent goToMainIntent = new Intent(CurrentParty.this, MainActivity.class);
            CurrentParty.this.startActivity(goToMainIntent);
        });
    }

    public void connectAdapterToRecycler() {
        recyclerView = findViewById(R.id.usersRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new CurrentPartyUserAdapter(guestList, this));
    }

    public void connectAdapterToRecycler2() {
        recyclerView2 = findViewById(R.id.giftRecycler);
        recyclerView2.setLayoutManager(new LinearLayoutManager(this));
        recyclerView2.setAdapter(new GiftAdapter(giftList, amplifyUser, this));
    }

    @Override
    public void giftsToDoListener(Gift gift) {
        String previousGiftOwner = gift.getPartyGoer();

        if (!gLHashMap.get(currentTurn).getUser().getUserName().equalsIgnoreCase(Amplify.Auth.getCurrentUser().getUsername())) {
            Toast.makeText(this, "It is not your turn!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (gift.getLastPartyGoer().equalsIgnoreCase(amplifyUser.getUserName())) {
            Toast.makeText(this,"You can't steal a gift that was just taken from you.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (gift.getTimesStolen() >= 2) {
            Toast.makeText(this, "This gift can not be stolen anymore!", Toast.LENGTH_SHORT).show();
            return;
        } else {
            gift.timesStolen = gift.getTimesStolen() + 1;

            Amplify.API.query(
                    ModelQuery.get(Party.class, intent.getExtras().getString("id")),
                    response3 -> {
                        for (GuestList user : response3.getData().getUsers()) {
                            if (user.getInvitedUser().equalsIgnoreCase(amplifyUser.getUserName())) {
                                user.takenTurn = true;
                                Amplify.API.query(
                                        ModelMutation.update(user),
                                        response4 -> Log.i("Mutation.user", "users turn taken "),
                                        error -> Log.e("Mutation.user", "fail")
                                );
                            }
                        }

                        if (!previousGiftOwner.equalsIgnoreCase("TBD")) {
                            for (GuestList previousUser : response3.getData().getUsers()) {
                                if (previousGiftOwner.equalsIgnoreCase(previousUser.getInvitedUser())) {
                                    previousUser.takenTurn = false;

                                    Amplify.API.query(
                                            ModelMutation.update(previousUser),
                                            response4 -> Log.i("Mutation.user", "users turn taken "),
                                            error -> Log.e("Mutation.user", "fail")
                                    );
                                }
                            }
                        }
                    },
                    error -> Log.e("Amplify", "Failed to retrieve store")
            );

            gift.lastPartyGoer = previousGiftOwner;
            gift.partyGoer = amplifyUser.getUserName(); // changes the "in party" owner

            Amplify.API.mutate(
                    ModelMutation.update(gift),
                    response2 -> Log.i("Mutation", "mutated the gifts user " + gift),
                    error -> Log.e("Mutation", "Failure, you disgrace family " + error)
            );

            Toast.makeText(this, "You chose a gift! " + gift.getTitle(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void taskListener(String party) { }
}