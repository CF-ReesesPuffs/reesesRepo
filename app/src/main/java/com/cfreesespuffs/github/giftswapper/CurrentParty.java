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
import android.view.View;
import android.widget.Button;
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

import org.w3c.dom.Text;

import java.util.ArrayList;

public class CurrentParty extends AppCompatActivity implements GiftAdapter.OnCommWithGiftsListener, CurrentPartyUserAdapter.OnInteractWithTaskListener{
    ArrayList<GuestList> guestList = new ArrayList<>();
    ArrayList<Gift> giftList = new ArrayList<>();
    Handler handler;
    RecyclerView recyclerView;
    RecyclerView recyclerView2;
    ArrayList<String> attendingGuests = new ArrayList<>();
    GuestList loggedUser;
    User amplifyUser;
    Intent intent;

    //TODO: Create user turn functionality
    //TODO: Once each user has chosen a gift, display post party page
    //TODO: Update recycler on click of a new item

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_party);

        intent = getIntent();

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
                        Log.i("Amplify.test", "stuff to test " + user);
                        if(user.getInviteStatus().equals("Accepted")){
                            attendingGuests.add(user.getInvitedUser());
                            guestList.add(user);
                        }
                    }
                    handler.sendEmptyMessage(1);
                },
                error -> Log.e("Amplify", "Failed to retrieve store")
        );

        Amplify.API.query(
                ModelQuery.get(Party.class, intent.getExtras().getString("id")),
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
        ApiOperation subscription = Amplify.API.subscribe(
                ModelSubscription.onUpdate(Gift.class),
                onEstablished -> Log.i(SUBSCRIBETAG, "Subscription established"),
                createdItem -> {
                    Log.i(SUBSCRIBETAG, "Subscription created: " + ((Gift) createdItem.getData()).getTitle());
                    Gift newItem = createdItem.getData();
                    Log.i("Gift chosen", giftList.toString());
                    giftList.clear();

                    Amplify.API.query(
                            ModelQuery.get(Party.class, intent.getExtras().getString("id")),
                            response -> {
                                Party completedParty = response.getData();
                                Log.i("Test party.gift", "========" + response.getData().getGifts());
                                for (Gift giftBrought : response.getData().getGifts()) {
                                    Log.i("Amplify.gifts", "Here is all the gifts from users! " + giftBrought);
                                    giftList.add(giftBrought);
                                }

                                Boolean allTaken = true;

                                for (Gift gift : giftList) {
                                    if (gift.getPartyGoer().contains("TBD")) allTaken = false;
                                    System.out.println("Alltaken: " + allTaken);
                                }

                                System.out.println("Alltaken after loop: " + allTaken);

                                if (allTaken) { // Todo: break this out into a function to allow for a check to happen at beginning of activity to go straight to post party page. Or could be a "flag"/field check if "party complete".
                                    Intent headToPostParty = new Intent(CurrentParty.this, PostParty.class);

                                    headToPostParty.putExtra("title", completedParty.getTitle());
                                    headToPostParty.putExtra("partyId", completedParty.getId());
                                    headToPostParty.putExtra("when", String.valueOf(completedParty.HOSTED_ON)); // TODO: check this works. It's... "query-able" via SQL. Is that beneficial here, or just different?
                                    headToPostParty.putExtra("setTime", String.valueOf(completedParty.HOSTED_AT)); // see above.

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

//        Amplify.API.query(  // TODO: turn off a user's ability to click a gift, or turn off a gift's ability to be clicked.
//                ModelQuery.list(User.class),
//                response -> {
//                    AuthUser authUser = Amplify.Auth.getCurrentUser();
//                    for (User user : response.getData()) {
//                        if (user.getUserName().equals(authUser.getUsername())) {
//                            amplifyUser = user;
//                        }
//
//                        if (guestList.)
//                    }
//                },
//                error -> Log.e("Amplify.userRetrieval", "didn't get'em")
//        );

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
        recyclerView.setAdapter(new CurrentPartyUserAdapter(guestList,  this));
    }

    public void connectAdapterToRecycler2() {
        recyclerView2 = findViewById(R.id.giftRecycler);
        recyclerView2.setLayoutManager(new LinearLayoutManager(this));
        recyclerView2.setAdapter(new GiftAdapter(giftList, amplifyUser, this));
    }

    @Override
    public void giftsToDoListener(Gift gift) {
        System.out.println(gift.getUser().getUserName());

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