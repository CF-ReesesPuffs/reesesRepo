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
import java.util.HashSet;

public class HostParty extends AppCompatActivity implements HostPartyAdapter.GuestListListener {

    public ArrayList<User> guestList = new ArrayList<>();
    public HashSet<Integer> invitedGuestList;
    Handler handler;
    RecyclerView recyclerView;

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

        Amplify.API.query(
                ModelQuery.list(User.class),
                response -> {
                    guestList.clear();
                    for (User user : response.getData()) {
                        guestList.add(user);
                    }
                    handler.sendEmptyMessage(1);
                    Log.i("Amplify.queryItems", "this is our users");
                },
                error -> Log.e("Amplify.queryItems", "no users received")
        );


        Button findGuestButton = findViewById(R.id.findGuest_button);
        TextView foundGuest = findViewById(R.id.userFindGuestSearch);
        String foundGuestString = foundGuest.getText().toString();
        findGuestButton.setOnClickListener((view) -> {


            guestList.add(User.builder().userName("paul").build());
            guestList.add(User.builder().userName("claudio").build());
            guestList.add(User.builder().userName("meghan").build());
            handler.sendEmptyMessage(1);


            Amplify.API.query(
                    ModelQuery.list(User.class),
                    response -> {
                        for (User user : response.getData()) {
                            if (user.userName.contains(foundGuestString)) {
                                guestList.add(user);
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

                // TODO add guest list
                //TextView guestName = findViewById(R.id.guestNameTextView);

//                Intent addAttendeeIntent = new Intent(HostParty.this, InvitedPartyPage.class);
//                addAttendeeIntent.putExtra("partyName", party.partyName);

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
                        //TODO guestlist
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