package com.cfreesespuffs.github.giftswapper.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.auth.AuthUser;
import com.amplifyframework.core.Amplify;

import com.amplifyframework.datastore.generated.model.GuestList;

import com.amplifyframework.datastore.generated.model.Party;
import com.amplifyframework.datastore.generated.model.User;

import com.cfreesespuffs.github.giftswapper.Adapters.HostPartyAdapter;
import com.cfreesespuffs.github.giftswapper.PendingPage;
import com.cfreesespuffs.github.giftswapper.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HostParty extends AppCompatActivity implements HostPartyAdapter.GuestListListener {

    public ArrayList<User> guestList = new ArrayList<>();
//    public ArrayList<String> guestListUserName = new ArrayList<>();
//    public HashSet<Integer> invitedGuestList;
    Handler handler;
    RecyclerView recyclerView;
    HashMap<String, User> uniqueGuestList = new HashMap<>();
    User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_party);

        priceSpinner();

        AuthUser authUser = Amplify.Auth.getCurrentUser();
        Amplify.API.query(
                ModelQuery.list(User.class),
                response -> {
                    for(User user : response.getData()) {
                        if(user.getUserName().equalsIgnoreCase(authUser.getUsername())){
                            currentUser = user;
                            System.out.println("This is our current user/host" + currentUser);
                        }
                    }
                },
                error -> Log.e("Amplify.user", "error: " + error)
        );

        handler = new Handler(Looper.getMainLooper(),
                new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message message) {
                        recyclerView.getAdapter().notifyDataSetChanged();
                        return true;
                    }
                });

        handler.sendEmptyMessage(1);

        Button findGuestButton = findViewById(R.id.findGuest_button);
        findGuestButton.setOnClickListener((view) -> {

            Amplify.API.query(
                    ModelQuery.list(User.class),
                    response -> {
                        for (User user : response.getData()) {
                            TextView foundGuest = findViewById(R.id.userFindGuestSearch);
                            String foundGuestString = foundGuest.getText().toString();
                            //  Log.i("Amplify.string", "this is what we are looking for: " + foundGuestString);
                            if (user.getUserName().toLowerCase().contains(foundGuestString.toLowerCase())) {
                                //TODO limit to first letters STRETCH
                                // Log.i("Amplify.string", "this is the user we are looking for: " + user);
                                if (!uniqueGuestList.containsKey(user.getUserName())) {
                                    uniqueGuestList.put(user.getUserName(), user);
                                    guestList.add(user);
                                }
                            }
                        }
                    },
                    error -> Log.e("Amplify", "failed to find user")
            );

        });

        recyclerView = findViewById(R.id.guestSearchRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new HostPartyAdapter(guestList, this));

        Button addParty = HostParty.this.findViewById(R.id.button_createParty);
        addParty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TextView partyName = findViewById(R.id.textViewPartyName);
                TextView partyDate = findViewById(R.id.editTextDate);
                TextView partyTime = findViewById(R.id.editTextTime);
                Spinner selectedPriceSpinner = findViewById(R.id.price_spinner);
                Log.i("Android.usersToAdd", ((HostPartyAdapter) recyclerView.getAdapter()).usersToAdd.toString());

                Set guestsToInvite = ((HostPartyAdapter) recyclerView.getAdapter()).usersToAdd;
                List<User> guestsToInviteList = new ArrayList();

                guestsToInviteList.addAll(guestsToInvite);

                String nameOfParty = partyName.getText().toString();
                String dateOfParty = partyDate.getText().toString();
                String timeOfParty = partyTime.getText().toString();
                String priceOfParty = selectedPriceSpinner.getSelectedItem().toString();

                Party party;
                party = Party.builder()
                        .title(nameOfParty)
                        .hostedAt(timeOfParty)
                        .hostedOn(dateOfParty)
                        .price(priceOfParty)
                        .theHost(currentUser)
                        .isReady(false)
                        .isFinished(false)
                        .build();

                Amplify.API.mutate(
                        ModelMutation.create(party),
                        response -> {
                            Log.i("Amplify.API", "success party started");
                            Party party2 = response.getData();
//                for (User thisGuest : guestsToInviteList) {
//                    GuestList guestListFinal = GuestList.builder()
//                            .party(party)
//                            .user(thisGuest)
//                            .build();
//
//                    Amplify.API.mutate(
//                            ModelMutation.create(guestListFinal),
//                            response -> Log.i("Amplify.API", "success users added"),
//                            error -> Log.e("Amplify/API", "Message failed " + error)
//                    );
//                }

                boolean flag = false;
                for(User guest : guestsToInviteList){
                    if(guest.getUserName().equalsIgnoreCase(authUser.getUsername())) flag = true;
                }
                if(!flag) guestsToInviteList.add(currentUser);

                for(User guest : guestsToInviteList){
                    GuestList inviteStatus = GuestList.builder()
                            .inviteStatus("Pending")
                            .user(guest)
                            .invitee(currentUser.getUserName())
                            .invitedUser(guest.getUserName())
                            .takenTurn(false)
                            .party(party2)
                            .turnOrder(0)
                            .build();

                    Amplify.API.mutate(
                            ModelMutation.create(inviteStatus),
                            response2 -> Log.i("Amplify.API", "Users are now pending!!!"),
                            error -> Log.e("Amplify/API", "Message failed " + error)
                    );


                }

                Intent intent = new Intent(HostParty.this, MainActivity.class);
                intent.putExtra("title", party2.getTitle());
                intent.putExtra("date", party2.getHostedOn());
                intent.putExtra("time", party2.getHostedAt());
                intent.putExtra("price", party2.getPrice());

                intent.putExtra("id", party2.getId());
                HostParty.this.startActivity(intent);
                        },
                        error -> Log.e("Amplify/API", "Message failed " + error)
                );

            }
        });
    }

    public void priceSpinner() {
        String[] pricePoints = {"$0- $10", "$11- $20", "$21- $30", "$31- $40"};
        Spinner spinner = (Spinner) findViewById(R.id.price_spinner);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, pricePoints);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }


    @Override
    public void listener(User user) {

    }
}