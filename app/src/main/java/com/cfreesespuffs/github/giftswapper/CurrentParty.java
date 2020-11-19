
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
import com.amplifyframework.datastore.generated.model.Gift;
import com.amplifyframework.datastore.generated.model.Party;
import com.amplifyframework.datastore.generated.model.User;
import com.cfreesespuffs.github.giftswapper.Adapters.CurrentPartyUserAdapter;
import com.cfreesespuffs.github.giftswapper.Adapters.GiftAdapter;

import java.util.ArrayList;

public class CurrentParty extends AppCompatActivity implements GiftAdapter.OnCommWithGiftsListener {
    ArrayList<User> guestList;
    ArrayList<Gift> giftList;
    Handler handler;
    Handler handler2;
    RecyclerView recyclerView;
    RecyclerView recyclerView2;

    //TODO: Take all users from pending-party and display in recycler vertically
    //TODO: Take all gifts from pending-party and display in recycler horizontally
    //TODO: Encrypt the gift name while it is in the giftRecycler

    //TODO: Randomize the order of guests
    //TODO: Give the users the ability to take a SINGLE item

    //TODO: After one users picks a gift, the next user is able to select
    //TODO: The userRecycler updates and shows which gift the user chose

    //TODO: Once each user has taken a gift, end the game

    //TODO: Once the game ends, the post-party-results is shown


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_party);

        connectAdapterToRecycler();
        connectAdapterToRecycler2();

        handler = new Handler(Looper.getMainLooper(),
                new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message msg) {
                        connectAdapterToRecycler();
                        recyclerView.getAdapter().notifyDataSetChanged();
                        return false;
                    }
                });

        handler2 = new Handler(Looper.getMainLooper(),
                new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message msg) {
                        connectAdapterToRecycler();
                        recyclerView2.getAdapter().notifyDataSetChanged();
                        return false;
                    }
                });

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        Amplify.API.query(
                ModelQuery.list(User.class),
                response -> {
                    for (User guest : response.getData()) {
//                        if (preferences.contains("RSVP")) {
//                            if (guest.get().equals(preferences.getString("RSVP", null))) {
//                                guestList.add(guest);
//                            }
//                        } else {
//                            guestList.add(guest);
//                        }
                    }
                    handler.sendEmptyMessage(1);
                },
                error -> Log.e("Amplify", "Failed to retrieve store")
        );

        Amplify.API.query(
                ModelQuery.list(Gift.class),
                response -> {
                    for (Gift giftBrought : response.getData()) {
                        if (preferences.contains("GiftGroup")) {
                            if (giftBrought.getUser().getGifts().equals(preferences.getString("GiftGroup", null))) {
                                giftList.add(giftBrought);
                            }
                        } else {
                            giftList.add(giftBrought); //TODO: when we add the gift, can we mutate its name?
                        }
                    }
                    handler.sendEmptyMessage(1);
                },
                error -> Log.e("Amplify", "Failed to retrieve store")
        );

        guestsTakeTurns();

    }
        public void guestsTakeTurns(){
        for(int i = 0; i < guestList.size(); i ++){
//            while(guestList.get(i).gifts == null){
                //the user can choose a gift

//            }
                }
                Intent intent = new Intent(CurrentParty.this, PostParty.class);
                intent.putExtra("partyName", String.valueOf(Party.TITLE));
//        intent.putExtra("host", String.valueOf(Party.));
                intent.putExtra("when", String.valueOf(Party.HOSTED_ON));
                intent.putExtra("setTime", String.valueOf(Party.HOSTED_AT));
//        intent.putExtra("users", String.valueOf(Party.));
            }
        }
    private void connectAdapterToRecycler() {
        recyclerView = findViewById(R.id.usersRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new CurrentPartyUserAdapter(guestList, (CurrentPartyUserAdapter.OnInteractWithTaskListener) this));
    }

    private void connectAdapterToRecycler2() {
        recyclerView2 = findViewById(R.id.giftRecycler);
        recyclerView2.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView2.setAdapter(new GiftAdapter(giftList, this));
    }

    @Override
    public void giftsToDoListener(Gift gift) {
        //user clicks on a gift
        //gift now belongs to that user
    }

}