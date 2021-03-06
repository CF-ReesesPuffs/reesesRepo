package com.cfreesespuffs.github.giftswapper.Activities;

import androidx.annotation.NonNull;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amplifyframework.api.ApiOperation;
import com.amplifyframework.api.aws.GsonVariablesSerializer;
import com.amplifyframework.api.graphql.GraphQLRequest;
import com.amplifyframework.api.graphql.SimpleGraphQLRequest;
import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
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
import java.util.Collections;
import java.util.HashMap;

public class CurrentParty extends AppCompatActivity implements GiftAdapter.OnCommWithGiftsListener, CurrentPartyUserAdapter.OnInteractWithTaskListener {
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
    Toolbar toolbar;
    SharedPreferences preferences;
    String giftStolenToCheck;

    // Todo: if we want to use Android Jetpack Compose, must follow this: https://blog.jetbrains.com/kotlin/2021/02/the-jvm-backend-is-in-beta-let-s-make-it-stable-together/

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_party);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        toolbar = findViewById(R.id.tb_Current_Party);
        setSupportActionBar(toolbar);

        intent = getIntent();
        partyId = intent.getExtras().getString("id");
        authUser = Amplify.Auth.getCurrentUser();

        Amplify.API.query(
                ModelQuery.get(User.class, preferences.getString("userId", "NA")),
                response -> amplifyUser = response.getData(),
                error -> Log.e("amplify.user", String.valueOf(error))
        );

        TextView partyTv = findViewById(R.id.currentPartyTitleTb);
        partyTv.setText(intent.getExtras().getString("thisPartyId"));

        Button homeButton = findViewById(R.id.thisHomeButton);
        homeButton.setOnClickListener(view -> {
            Intent intent = new Intent(CurrentParty.this, MainActivity.class);
            CurrentParty.this.startActivity(intent);
        });

        handler = new Handler(Looper.getMainLooper(), msg -> {
            connectAdapterToRecycler();
            connectAdapterToRecycler2();
            recyclerView.getAdapter().notifyDataSetChanged();
            return false;
        });

        handlerGeneral = new Handler(Looper.getMainLooper(), message -> {
            if (message.arg1 == 1) {
                Toast.makeText(this, "It is your turn, pick a gift!", Toast.LENGTH_LONG).show();
            }
            return false;
        });

        Amplify.API.query(
                ModelQuery.get(Party.class, partyId),
                response -> {
                    party = response.getData();
                    giftStolenToCheck = party.lastGiftStolen;
                    for (GuestList user : response.getData().getUsers()) {
                        if (user.getInviteStatus().equals("Accepted")) {
                            guestList.add(user);
                            gLHashMap.put(user.getTurnOrder(), user);
                            if (!user.getTakenTurn() && user.getTurnOrder() < currentTurn)
                                currentTurn = user.getTurnOrder();
                        }
                    }
                    giftList.addAll(party.getGifts()); // todo: confirm is working
                    handler.sendEmptyMessage(1);
                },
                error -> Log.e("Amplify", "Failed to retrieve store")
        );

        String SUBSCRIBETAG = "Amplify.subscription";

        ApiOperation guestListByHost = Amplify.API.subscribe(getGuestListByHost(intent.getExtras().getString("host")),
                subCheck -> Log.i("Sub.HostGuestList", "success"),
                response -> {
                    Log.e("Sub.subGuestList", "response: " + response);
                    Amplify.API.query(
                            ModelQuery.get(Party.class, response.getData().getParty().getId()),
                            response2 -> {
                                Log.i("Sub.sub", "response: " + response2);
                                giftStolenToCheck = response2.getData().lastGiftStolen;
                                for (GuestList guestList : response2.getData().getUsers())
                                    gLHashMap.replace(guestList.getTurnOrder(), guestList);
                                for (int i = 1; i < gLHashMap.size() + 1; i++) {
                                    if (!gLHashMap.get(i).getTakenTurn()) {
                                        currentTurn = i;
                                        if (gLHashMap.get(i).getUser().getUserName().equalsIgnoreCase(amplifyUser.getUserName())) {
                                            Message turnAlertMsg = new Message();
                                            turnAlertMsg.arg1 = 1;
                                            handlerGeneral.sendMessage(turnAlertMsg);
                                        }
                                        break;
                                    }
                                }
                                Log.i("Amp.newSub", "This is the turn: " + currentTurn);
                            },
                            error -> Log.e("Sub.sub", "error: " + error)
                    );
                },
                failure -> Log.e("Sub.subGuestList", "failure: " + failure),
                () -> Log.i("Sub.subGuestList", "Sub is closed"));

        subscription = Amplify.API.subscribe(
                getNumberUpdate(partyId),
                onEstablished -> Log.i(SUBSCRIBETAG, "Subscription established"),
                createdItem -> {
                    Log.i(SUBSCRIBETAG, "Subscription created: " + createdItem.getData().getTitle());

                    Amplify.API.query(
                            ModelQuery.get(Party.class, partyId),
                            response -> {
                                Party completedParty = response.getData();
                                giftList.clear();
                                for (Gift giftBrought : completedParty.getGifts()) {
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

                                    subscription.cancel();
                                    guestListByHost.cancel();
                                    party.isFinished = true;

                                    Amplify.API.query(
                                            ModelMutation.update(party),
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

        if (gift.getTitle().equalsIgnoreCase(giftStolenToCheck)) {
            Toast.makeText(this, "You can't steal a gift that was just taken from you.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (gift.getTimesStolen().equals(party.getStealLimit())) {
            Toast.makeText(this, "This gift can not be stolen anymore!", Toast.LENGTH_SHORT).show();
        } else {

            if (!gift.getPartyGoer().equals("TBD")) {
                gift.timesStolen = gift.getTimesStolen() + 1;
                party.lastGiftStolen = gift.getTitle();
            }

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

            gift.partyGoer = amplifyUser.getUserName();

            Amplify.API.mutate(
                    ModelMutation.update(gift),
                    response2 -> Log.i("Mutation", "mutated the gifts user " + gift),
                    error -> Log.e("Mutation", "Failure, you disgrace family " + error)
            );

            Amplify.API.mutate(
                    ModelMutation.update(party),
                    response4 -> Log.i("Mutation", "mutated the party" + party),
                    error -> Log.e("Mutation", "Error: " + error)
            );

            Toast.makeText(this, "You chose a gift! " + gift.getTitle(), Toast.LENGTH_SHORT).show();
        }
    }

    private GraphQLRequest<GuestList> getGuestListByHost(String host) {
        String document = "subscription hostGuestList ($invitee: String) { "
                + "onUpdateHostGuestList(invitee: $invitee) { "
                + "party { "
                + "id "
                + "}"
                + "}"
                + "}";
        return new SimpleGraphQLRequest<>(
                document,
                Collections.singletonMap("invitee", host),
                GuestList.class,
                new GsonVariablesSerializer());
    }

    private GraphQLRequest<Gift> getNumberUpdate(String number) {
        String document = "subscription giftNumber ($number: String) { "
                + "onUpdateGiftOfSpecificParty(number: $number) { "
                + "party { "
                + "id "
                + "}"
                + "}"
                + "}";
        return new SimpleGraphQLRequest<>(
                document,
                Collections.singletonMap("number", number),
                Gift.class,
                new GsonVariablesSerializer());
    }

    @Override
    public void taskListener(String party) {
    }
}