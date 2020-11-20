package com.cfreesespuffs.github.giftswapper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.auth.AuthUser;

import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Gift;
import com.amplifyframework.datastore.generated.model.GuestList;
import com.amplifyframework.datastore.generated.model.Party;
import com.amplifyframework.datastore.generated.model.User;
import com.cfreesespuffs.github.giftswapper.Activities.MainActivity;

import java.util.List;

public class InvitationDetails extends AppCompatActivity {

    Handler handlecheckLoggedIn;
    User loggedUser;
    Intent intent;
    Party party;
    GuestList guestList;
   // Message message = new Message();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invited_party_page);


        intent = getIntent();
        String partyId = intent.getExtras().getString("partyId");

        Amplify.API.query(
                ModelQuery.get(Party.class, partyId),
                response -> {
                    party = response.getData();
                    Log.i("Amplify.query", "We got a party " + partyId);
                },
                error -> Log.e("Amplify.query", "no party " + error)
        );

        AuthUser authUser = Amplify.Auth.getCurrentUser();
        if(Amplify.Auth.getCurrentUser() != null) {
            Amplify.API.query(
                    ModelQuery.list(User.class),
                    response -> {
                        for (User user : response.getData()) {
                            if (user.getUserName().contains(authUser.getUsername())) {
                                loggedUser = user;
                                Log.i("Amplify.currentUser", "This is the current user, " + loggedUser);
                            }
                        }
                    },
            error -> Log.e("Amplify.currentUser", "error"));
        }




//=================================================================================================== Invitation details
        Intent intent = getIntent();

        TextView partyName = InvitationDetails.this.findViewById(R.id.homePartyTitleButton);
        partyName.setText(intent.getExtras().getString("partyName"));

       // TextView host = InvitationDetails.this.findViewById(R.id.partyHost);
       // host.setText(intent.getExtras().getString("host"));

        TextView when = InvitationDetails.this.findViewById(R.id.dateOfParty);
        when.setText(intent.getExtras().getString("when"));

        TextView setTime = InvitationDetails.this.findViewById(R.id.timeOfParty);
        setTime.setText(intent.getExtras().getString("setTime"));

        TextView budget = InvitationDetails.this.findViewById(R.id.budgetLimit);
        budget.setText(intent.getExtras().getString("budget"));

//=================================================================================================== Decline invite
        Button declineInvite = InvitationDetails.this.findViewById(R.id.declineInvite);
        declineInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TODO: Set the invite status to false
                List<GuestList> target = party.getUsers();
                for(GuestList thisGuestList : target){
                    if(thisGuestList.getInvitedUser().contains(loggedUser.getUserName())){
                        guestList = thisGuestList;
                    }
                }
                if(guestList == null){
                  Log.i("Amplify.error", "Couldn't find the user");
                }

                guestList.inviteStatus = "Declined";

                Log.i("Amplify.guestList", "This is guest list " + guestList);
                Amplify.API.mutate(
                        ModelMutation.update(guestList),
                        response -> Log.i("DeclinedInvite", "You declined an invite! " + response.getData().toString()),
                        error -> Log.e("DeclinedInviteFail", error.toString())
                );

                Intent gotoMain = new Intent(InvitationDetails.this, MainActivity.class);
                InvitationDetails.this.startActivity(gotoMain);
            }
        });

//=================================================================================================== Accept invite
        Button acceptInvite = InvitationDetails.this.findViewById(R.id.acceptInvite);
        acceptInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText giftChosen = InvitationDetails.this.findViewById(R.id.giftUserBrings);
                String giftName = giftChosen.getText().toString();

                Log.i("Android.gift", "this is the gift " + giftName);

                if(giftName.equals("")){
                    handlerCheck(1);
                    return;
                }

                Gift gift = Gift.builder()
                        .title(giftName)
                        .party(party)
                        .user(loggedUser)
                        .build();

                Amplify.API.mutate(
                        ModelMutation.create(gift),
                        response -> Log.i("AddGift", "You saved a new gift to bring, " + giftName),
                        error -> Log.e("AddGiftFail", error.toString())
                );

                List<GuestList> target = party.getUsers();
                for(GuestList thisGuestList : target){
                    if(thisGuestList.getInvitedUser().contains(loggedUser.getUserName())){
                        guestList = thisGuestList;
                    }
                }
                if(guestList == null){
                    Log.i("Amplify.error", "Couldn't find the user");
                }

                guestList.inviteStatus = "Accepted";

                Amplify.API.mutate(
                        ModelMutation.update(guestList),
                        response -> Log.i("AcceptedInvite", "You accepted an invite!"),
                        error -> Log.e("AcceptedInviteFail", error.toString())
                );

                Intent gotoPending = new Intent(InvitationDetails.this, PendingPage.class);
                gotoPending.putExtra("partyName", party.getTitle());
                InvitationDetails.this.startActivity(gotoPending);
            }
        });
    }

    public void handlerCheck(int messageCode) {

        if (messageCode == 1) {
            Toast.makeText(this, "You need to bring a gift", Toast.LENGTH_LONG).show();

            Log.i("Amplify.login", "They weren't logged in");
        } else if (messageCode == 2) {
//            Log.i("Amplify.login", Amplify.Auth.getCurrentUser().getUsername());

        } else if (messageCode == 3) {
            Toast.makeText(this, "You need to bring a gift", Toast.LENGTH_LONG).show();
        } else {
            Log.i("Amplify.login", "Send true or false pls");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(InvitationDetails.this, MainActivity.class);
        InvitationDetails.this.startActivity(intent);
        return true;
    }
}