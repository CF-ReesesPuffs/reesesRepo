package com.cfreesespuffs.github.giftswapper.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Party;
import com.amplifyframework.datastore.generated.model.User;

import com.cfreesespuffs.github.giftswapper.InvitedPartyPage;
import com.cfreesespuffs.github.giftswapper.R;

public class HostParty extends AppCompatActivity {

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
        //recyclerView.setAdapter(new HostPartyAdapter(users, this));





        Button addParty = HostParty.this.findViewById(R.id.button_createParty);
        addParty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TextView partyName = findViewById(R.id.textView_partyName);
                TextView partyDate = findViewById(R.id.editTextDate);
                TextView partyTime = findViewById(R.id.editTextTime);
                // TODO Add spinner for price range
                // TODO add guest list
                //TextView guestName = findViewById(R.id.guestNameTextView);

//                Intent addAttendeeIntent = new Intent(HostParty.this, InvitedPartyPage.class);
//                addAttendeeIntent.putExtra("partyName", party.partyName);

                String nameOfParty = partyName.getText().toString();
                String dateOfParty = partyDate.getText().toString();
                String timeOfParty = partyTime.getText().toString();
                //String priceOfParty = priceName.getText().toString();
                //String guestOfParty = guestName.getText().toString();


                Party party;
                party = Party.builder()
                        .title(nameOfParty)
                        .hostedAt(timeOfParty)
                        .hostedOn(dateOfParty)
                      //  .price(priceOfParty)

                        .build();

                //Amplify.API.mutate()


            }
        });



    }





}