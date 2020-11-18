package com.cfreesespuffs.github.giftswapper.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Party;
import com.amplifyframework.datastore.generated.model.User;

import com.cfreesespuffs.github.giftswapper.Adapters.HostPartyAdapter;
import com.cfreesespuffs.github.giftswapper.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HostParty extends AppCompatActivity implements HostPartyAdapter.GuestListListener {

    public ArrayList<User> guestList = new ArrayList<>();
    public HashSet<Integer> invitedGuestList;
    Handler handler;
    RecyclerView recyclerView;
    HashMap<String, User> uniqueGuestList = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_party);

        priceSpinner();

        handler = new Handler(Looper.getMainLooper(),
                new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message message) {
                        recyclerView.getAdapter().notifyDataSetChanged();
                        return true;
                    }
                });

        guestList.add(User.builder().userName("paul").build());
        guestList.add(User.builder().userName("claudio").build());
        guestList.add(User.builder().userName("meghan").build());
        guestList.add(User.builder().userName("1").build());
        guestList.add(User.builder().userName("2").build());
        guestList.add(User.builder().userName("3").build());
        guestList.add(User.builder().userName("4").build());
        guestList.add(User.builder().userName("5").build());
        guestList.add(User.builder().userName("6").build());
        guestList.add(User.builder().userName("7").build());
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
                            if (user.userName.toLowerCase().contains(foundGuestString.toLowerCase())) {
                                //TODO limit to first letters STRETCH
                                // Log.i("Amplify.string", "this is the user we are looking for: " + user);
                                if (!uniqueGuestList.containsKey(user.userName)) {
                                    uniqueGuestList.put(user.userName, user);
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
                Log.i("Android.usersToAdd", ((HostPartyAdapter)recyclerView.getAdapter()).usersToAdd.toString());

                Set guestsToInvite = ((HostPartyAdapter)recyclerView.getAdapter()).usersToAdd;
                List guestsToInviteList = new ArrayList();
                guestsToInviteList.addAll(guestsToInvite);

                String nameOfParty = partyName.getText().toString();
                String dateOfParty = partyDate.getText().toString();
                String timeOfParty = partyTime.getText().toString();
                String priceOfParty = selectedPriceSpinner.getSelectedItem().toString();
                //String guestOfParty = guestName.getText().toString();


                Party party;
                party = Party.builder()
                        .title(nameOfParty)
                        .hostedAt(timeOfParty)
                        .hostedOn(dateOfParty)
                        .price(priceOfParty)
                       // .
                        .build();
                //Amplify.API.mutate()

            }
        });

    }

    public void priceSpinner() {
        String[] pricePoints = {"0-5", "6-15", "16-25", "25-40"};
        Spinner spinner = (Spinner) findViewById(R.id.price_spinner);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, pricePoints);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }


    @Override
    public void listener(User user) {
//        CheckBox selectedUser = findViewById(R.id.rsvpCheckBox);
//
//        if (selectedUser.isChecked()) {
//
//        }
    }
}