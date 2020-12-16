package com.cfreesespuffs.github.giftswapper;

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
import com.cfreesespuffs.github.giftswapper.Activities.MainActivity;
import com.cfreesespuffs.github.giftswapper.Adapters.CurrentPartyUserAdapter;
import com.cfreesespuffs.github.giftswapper.Adapters.GiftAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class CurrentParty extends AppCompatActivity implements GiftAdapter.OnCommWithGiftsListener, CurrentPartyUserAdapter.OnInteractWithTaskListener{
    ArrayList<GuestList> guestList = new ArrayList<>();
    HashMap<Integer, GuestList> gLHashMap = new HashMap<>();
    ArrayList<Gift> giftList = new ArrayList<>();
    Handler handler;
    RecyclerView recyclerView;
    RecyclerView recyclerView2;
//    ArrayList<String> attendingGuests = new ArrayList<>();
    User amplifyUser;
    Intent intent;
    String partyId;
    int currentTurn = 100; // this is not smart :P
    ApiOperation subscription;

    // TODO: Once each user has chosen a gift, display post party page

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_party);

        intent = getIntent();
        partyId = intent.getExtras().getString("id");
        Log.e("Amp.partyFromIntent", "Here's that partyID: " + partyId);

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

        Amplify.API.query(
                ModelQuery.get(Party.class, intent.getExtras().getString("id")),
                response -> {
                    for (GuestList user : response.getData().getUsers()) {
                        if(user.getInviteStatus().equals("Accepted")){
//                            attendingGuests.add(user.getInvitedUser());
                            guestList.add(user);
//                            for (int i = 0; i < guestList.size(); i++) {
                            gLHashMap.put(user.getTurnOrder(), user); // Todo: could break here for turn order logic...
                            if (!user.getTakenTurn() && user.getTurnOrder() < currentTurn) currentTurn = user.getTurnOrder(); // Todo: current solution *should* work. but is not elegant.
//                            }
                        }
                        Log.i("Amplify.test", "glHashMap: " + gLHashMap.toString());
                        Log.i("Amplify.test", "Turn Order: " + currentTurn);
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
                        Log.i("Amplify.gifts", "Here is all the gifts from users! " + giftBrought);
                        giftList.add(giftBrought);
                        }
                    handler.sendEmptyMessage(1);
                },
                error -> Log.e("Amplify", "Failed to retrieve store")
        );

        String SUBSCRIBETAG = "Amplify.subscription";

        ApiOperation guestListSub = Amplify.API.subscribe(
                ModelSubscription.onUpdate(GuestList.class),
                onEstablished -> Log.i(SUBSCRIBETAG, "Guestlist Sub established."),
                createdItem -> {
                    Log.i(SUBSCRIBETAG, "Updated guestlist object: " + ((GuestList) createdItem.getData()).getUser().getUserName());
                    GuestList updatedGl = createdItem.getData();
                    gLHashMap.replace(updatedGl.getTurnOrder(), updatedGl);
                    Log.i("Amp.hashmapsize", "Hashmap size is: " + gLHashMap.size());
                    for (int i = 1; i < gLHashMap.size()+1; i++) { // is size zero based, or does it start at 1? // todo: watch for weird turnOrder shenanigans when stealing gifts (guestlist turn order could go funny?)
                        if (!gLHashMap.get(i).getTakenTurn()) {
                            currentTurn = i;
                            Log.i("Amp.NewCurrentTurn", "This is the turn: " + currentTurn);
                            break;
                        }
                    }
                },
                onFailure -> Log.i(SUBSCRIBETAG, onFailure.toString()),
                () -> Log.i(SUBSCRIBETAG, "Subscription completed")
        );

        subscription = Amplify.API.subscribe( // TODO: ensure subscription is subscribing to
                ModelSubscription.onUpdate(Gift.class), // Updates the gift info
                onEstablished -> Log.i(SUBSCRIBETAG, "Subscription established"),
                createdItem -> {
                    Log.i(SUBSCRIBETAG, "Subscription created: " + ((Gift) createdItem.getData()).getTitle());
                    Gift newItem = createdItem.getData();

                    Amplify.API.query(
                            ModelQuery.get(Party.class, partyId),
                            response -> {
                                Party completedParty = response.getData();
                                Log.i("Amp.PartyQuery", "This is completedParty: " + completedParty);
                                giftList.clear();
                                for (Gift giftBrought : response.getData().getGifts()) {
                                    Log.i("Amplify.gifts", "Here is all the gifts from users! " + giftBrought);
                                    giftList.add(giftBrought);
                                }

                                Boolean allTaken = true;

                                for (Gift gift : giftList) {
                                    Log.i("Android.Gift", "Here's the gift being checked: " + gift);
                                    if (gift.getPartyGoer().equals("TBD")) allTaken = false;
                                    System.out.println("Alltaken: " + allTaken);
                                }

                                System.out.println("Alltaken after loop: " + allTaken);

                                if (allTaken) { // Todo: break this out into a function to allow for a check to happen at beginning of activity to go straight to post party page. Or could be a "flag"/field check if "party complete".
                                    Intent headToPostParty = new Intent(CurrentParty.this, PostParty.class);

                                    headToPostParty.putExtra("title", completedParty.getTitle());
                                    headToPostParty.putExtra("partyId", completedParty.getId());
                                    headToPostParty.putExtra("when", String.valueOf(completedParty.HOSTED_ON)); // TODO: check this works. It's... "query-able" via SQL. Is that beneficial here, or just different?
                                    headToPostParty.putExtra("setTime", String.valueOf(completedParty.HOSTED_AT)); // see above.

                                    subscription.cancel(); // KILL THE SUBSCRIPTION. BURN IT DOWN.

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

    public void guestsTakeTurns(){
        for(int i = 0; i < guestList.size(); i ++){
            while(guestList.get(i).getTakenTurn() == false){
//                TextView currentUser = CurrentParty.this.findViewById(R.id.usersTurn);
//                currentUser.setVisibility(View.VISIBLE);
//                currentUser.setText(guestList.get(i).getUser().getUserName());

                }//TODO: how do we add a single gift to a list of gifts, then show that gift?
        }
        Intent intent = new Intent(CurrentParty.this, PostParty.class);

//        intent.putExtra("users", String.valueOf(Party.));
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
        System.out.println(gift.getUser().getUserName());
        System.out.println("This is whose turn it should be: " + gLHashMap.get(currentTurn).getUser().getUserName());
        System.out.println("This is whose phone this is: " + Amplify.Auth.getCurrentUser().getUsername());

        if (!gLHashMap.get(currentTurn).getUser().getUserName().contains(Amplify.Auth.getCurrentUser().getUsername())) { // Todo: could break here for turn order logic...

            Toast.makeText(this, "It is not your turn!", Toast.LENGTH_SHORT).show();

            return;
        }

        AuthUser authUser = Amplify.Auth.getCurrentUser();
                Amplify.API.query(
                        ModelQuery.list(User.class),
                        response ->{
                            for(User user : response.getData()){
                                if(user.getUserName().equals(authUser.getUsername())){
                                    amplifyUser = user;
                                }
                            }

                            Amplify.API.query(
                                    ModelQuery.get(Party.class, intent.getExtras().getString("id")),
                                    response3 -> {
                                        for (GuestList user : response3.getData().getUsers()) {
                                            Log.i("Amplify.guestList", "users turn " + user);
                                            if(user.getInvitedUser().contains(amplifyUser.getUserName())){
                                                user.takenTurn = true;

                                                Amplify.API.query(
                                                        ModelMutation.update(user),
                                                        response4 -> Log.i("Mutation.user", "users turn taken "),
                                                        error -> Log.e("Mutation.user", "fail")
                                                );
                                            }
                                        }
                                    },
                                    error -> Log.e("Amplify", "Failed to retrieve store")
                            );

                            gift.partyGoer = amplifyUser.getUserName(); // changes the "in party" owner
//                            gift.partyGoer = "TBD"; // changes the "in party" owner
//                            gift.user = amplifyUser; // changes the "real" owner

                            Amplify.API.mutate(
                                    ModelMutation.update(gift),
                                    response2 -> Log.i("Mutation", "mutated the gifts user " + gift),
                                    error -> Log.e("Mutation", "Failure, you disgrace family " + error)
                            );

                        },
                        error -> Log.e("amplify.user", String.valueOf(error))
                );


        Toast.makeText(this, "You chose a gift! " + gift.getTitle(), Toast.LENGTH_SHORT).show();

    }

    @Override
    public void taskListener(String party) {

    }
}