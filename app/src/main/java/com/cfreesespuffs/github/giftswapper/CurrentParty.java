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
import android.widget.ImageButton;
import android.widget.TextView;

import com.amplifyframework.api.graphql.model.ModelQuery;
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

public class CurrentParty extends AppCompatActivity implements GiftAdapter.OnCommWithGiftsListener {
    ArrayList<GuestList> guestList;
    ArrayList<Gift> giftList;
    Handler handler;
    Handler handler2;
    RecyclerView recyclerView;
    RecyclerView recyclerView2;
    ArrayList<String> attendingGuests = new ArrayList<>();
    GuestList loggedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_party);

        Intent intent = getIntent();

        TextView partyName = CurrentParty.this.findViewById(R.id.partyName);
        partyName.setText(intent.getExtras().getString("partyName"));

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

        Amplify.API.query(
                ModelQuery.get(Party.class, intent.getExtras().getString("id")),
                response -> {
                    for (GuestList user : response.getData().getUsers()) {
                        Log.i("Amplify.test", "stuff to test " + user);
                        attendingGuests.add(user.getInvitedUser());
                    }
                    handler.sendEmptyMessage(1);
                },
                error -> Log.e("Amplify", "Failed to retrieve store")
        );

        Amplify.API.query(
                ModelQuery.get(Party.class, intent.getExtras().getString("id")),
                response -> {
                    for (Gift giftBrought : response.getData().getGifts()) {
                        Log.i("Amplify.gifts", "Here is all the gifts from users! ");
                        giftList.add(giftBrought);
                        }
                    handler.sendEmptyMessage(1);
                },
                error -> Log.e("Amplify", "Failed to retrieve store")
        );
        AuthUser authUser = Amplify.Auth.getCurrentUser();
        if(Amplify.Auth.getCurrentUser() != null) {
            Amplify.API.query(
                    ModelQuery.list(GuestList.class),
                    response -> {
                        for (GuestList host : response.getData()) {
                            if (host.getInvitee().contains(authUser.getUsername())) {
                                guestsTakeTurns();
                                loggedUser = host;
                                Log.i("Amplify.currentUser", "This is the current host, he just gave perms to start the game " + loggedUser);
                            }
                        }
                    },
                    error -> Log.e("Amplify.currentUser", "error"));
        }
    ImageButton homeDetailButton = CurrentParty.this.findViewById(R.id.goHome);
    homeDetailButton.setOnClickListener((view)-> {
        Intent goToMainIntent = new Intent(CurrentParty.this, MainActivity.class);
        CurrentParty.this.startActivity(goToMainIntent);
    });
    }


    public void guestsTakeTurns(){
        for(int i = 0; i < guestList.size(); i ++){
            while(guestList.get(i).getUser().getGifts() == null){
                TextView currentUser = CurrentParty.this.findViewById(R.id.usersTurn);
                currentUser.setText(guestList.get(i).getUser().getUserName());
                for(int j = 0; i < giftList.size(); j++){
                    guestList.get(i).getUser().getGifts().add(giftList.get(j));
//                    TextView giftChosen = CurrentParty.this.findViewById(R.id.)
                }//TODO: how do we add a single gift to a list of gifts, then show that gift?
            }
        }
        Intent intent = new Intent(CurrentParty.this, PostParty.class);
        intent.putExtra("partyName", String.valueOf(Party.TITLE));
//        intent.putExtra("host", String.valueOf(Party.));
        intent.putExtra("when", String.valueOf(Party.HOSTED_ON));
        intent.putExtra("setTime", String.valueOf(Party.HOSTED_AT));
//        intent.putExtra("users", String.valueOf(Party.));
        }

    private void connectAdapterToRecycler() {
        recyclerView = findViewById(R.id.usersRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new CurrentPartyUserAdapter(attendingGuests, (CurrentPartyUserAdapter.OnInteractWithTaskListener) this));

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