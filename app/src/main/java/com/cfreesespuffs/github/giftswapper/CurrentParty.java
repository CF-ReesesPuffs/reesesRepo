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
    AuthUser authUser;

    // TODO: Once each user has chosen a gift, display post party page

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_party);

        intent = getIntent();
        partyId = intent.getExtras().getString("id");
        Log.e("Amp.partyFromIntent", "Here's that partyID: " + partyId);
        authUser = Amplify.Auth.getCurrentUser(); // TODO: didn't test, might break :shrug:

        Amplify.API.query(
                ModelQuery.list(User.class),
                response ->{
                    for(User user : response.getData()){
                        if(user.getUserName().equals(authUser.getUsername())){
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
//                    Gift newItem = createdItem.getData();

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
        System.out.println("111111111111111111111111111111111111111" + gift.getPartyGoer());
        String previousGiftOwner = gift.getPartyGoer();

        if (!gLHashMap.get(currentTurn).getUser().getUserName().contains(Amplify.Auth.getCurrentUser().getUsername())) { // Todo: could break here for turn order logic...
            Toast.makeText(this, "It is not your turn!", Toast.LENGTH_SHORT).show();
            return;
        }

        Amplify.API.query(
                ModelQuery.get(Party.class, intent.getExtras().getString("id")),
                response3 -> {
                    Log.e("Amp.TBDcheck", "This is gift to steal partygoer? : " + gift.getPartyGoer());
        System.out.println("++++++++++++++++++++++++++++++++++++++++++" + gift.getPartyGoer());
                    for (GuestList user : response3.getData().getUsers()) {
                        Log.i("Amplify.guestList", "users turn " + user);
                        System.out.println("//////////////////////////////////////////" + gift.getPartyGoer());
                        if(user.getInvitedUser().contains(amplifyUser.getUserName())){
                            user.takenTurn = true;
                            System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&" + gift.getPartyGoer());
                            Amplify.API.query(
                                    ModelMutation.update(user),
                                    response4 -> Log.i("Mutation.user", "users turn taken "),
                                    error -> Log.e("Mutation.user", "fail")
                            );
                        }
                        System.out.println("4444444444444444444444444444444444444444444444444" + gift.getPartyGoer());
                    }
//                                try { // makes system pause/wait/sleep to allow above for loop to finish executing. https://www.thejavaprogrammer.com/java-delay/
//                                    Thread.sleep(1500);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }  // Todo: THIS CODE BLOCK BREAKS THE HAPPY PATH + ALL PATHS. (if turned on)

                    System.out.println("Party goer before if " + gift.getPartyGoer());

                    if (!previousGiftOwner.equalsIgnoreCase("TBD")) { // Todo: THIS CODE ALSO BREAKS THE HAPPY PATH + ALL PATHS. (if turned on)

                        for (GuestList previousUser : response3.getData().getUsers()) {
                            Log.i("Amplify.guestList", "users turn " + previousUser);

                            System.out.println("partygoer " + gift.partyGoer + "user " + previousUser.getInvitedUser());

                            if(gift.getPartyGoer().equalsIgnoreCase(previousUser.getInvitedUser())){
                                System.out.println("Entered the if");
                                //  if(previousUser.getInvitedUser().equalsIgnoreCase(gift.getPartyGoer())){

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

        gift.partyGoer = amplifyUser.getUserName(); // changes the "in party" owner

        Amplify.API.mutate(
                ModelMutation.update(gift),
                response2 -> Log.i("Mutation", "mutated the gifts user " + gift),
                error -> Log.e("Mutation", "Failure, you disgrace family " + error)
        );

        Toast.makeText(this, "You chose a gift! " + gift.getTitle(), Toast.LENGTH_SHORT).show();

    }
//                    try { // makes system pause/wait/sleep to allow above for loop to finish executing. https://www.thejavaprogrammer.com/java-delay/
//                        Thread.sleep(5000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }

    @Override
    public void taskListener(String party) { }
}