package com.cfreesespuffs.github.giftswapper.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
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

public class HostParty extends AppCompatActivity implements HostPartyAdapter.GuestListListener{

    public ArrayList<User> guestList;
    public HashSet<Integer> invitedGuestList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_party);


        Amplify.API.query(
                ModelQuery.list(User.class),
                response -> {
                    for (User user : response.getData()) {

                    }
                },
                error -> Log.e("Amplify", "failed to find user")
        );

        RecyclerView recyclerView = findViewById(R.id.guestSearchRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new HostPartyAdapter(guestList, this));


        Button addParty = HostParty.this.findViewById(R.id.button_createParty);
        addParty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TextView partyName = findViewById(R.id.textView_partyName);
                TextView partyDate = findViewById(R.id.editTextDate);
                TextView partyTime = findViewById(R.id.editTextTime);
                priceSpinner();
                // TODO add guest list
                //TextView guestName = findViewById(R.id.guestNameTextView);

                Spinner selectedPriceSpinner = findViewById(R.id.price_spinner);

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

    public void priceSpinner(){
        String[] pricePoints = {"0-5", "6-15", "16-25", "25-40"};
        Spinner spinner = (Spinner) findViewById(R.id.price_spinner);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, pricePoints);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }


    @Override
    public void listener(User user) {
        CheckBox selectedUser = findViewById(R.id.rsvpCheckBox);

        if(selectedUser.isChecked()){

        }
    }
}