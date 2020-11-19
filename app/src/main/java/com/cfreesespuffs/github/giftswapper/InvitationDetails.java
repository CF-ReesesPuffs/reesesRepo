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

import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Gift;
import com.amplifyframework.datastore.generated.model.GuestList;
import com.amplifyframework.datastore.generated.model.Party;
import com.amplifyframework.datastore.generated.model.User;
import com.cfreesespuffs.github.giftswapper.Activities.MainActivity;

public class InvitationDetails extends AppCompatActivity {

    Handler handlecheckLoggedIn;
    User loggedUser;
    Intent intent;
    Party party;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invited_party_page);

        getIsSignedIn();
        intent = getIntent();
        String partyId = intent.getExtras().getString("partyId");

        Amplify.API.query(
                ModelQuery.get(Party.class, partyId),
                response -> {
                    party = response.getData();
                    Log.i("Amplify.query", "We got a party");
                },
                error -> Log.e("Amplify.query", "no party " + error)
        );

        handlecheckLoggedIn = new Handler(Looper.getMainLooper(), message -> {
            if (message.arg1 == 0) {
                Log.i("Amplify.login", "They weren't logged in");
            } else if (message.arg1 == 1) {
                Log.i("Amplify.login", Amplify.Auth.getCurrentUser().getUsername());
              //  TextView loggedUser = InvitationDetails.this.findViewById(R.id.current_user);
              //  loggedUser.setText(Amplify.Auth.getCurrentUser().getUsername());
               // loggedUser.setVisibility(View.VISIBLE);

            } else {
                Log.i("Amplify.login", "Send true or false pls");
            }
            return false;
        });
//=================================================================================================== Invitation details
        Intent intent = getIntent();

        TextView partyName = InvitationDetails.this.findViewById(R.id.homePartyTitleButton);
        partyName.setText(intent.getExtras().getString("title"));

        TextView host = InvitationDetails.this.findViewById(R.id.partyHost);
        host.setText(intent.getExtras().getString("host"));

        TextView when = InvitationDetails.this.findViewById(R.id.dateOfParty);
        when.setText(intent.getExtras().getString("hostedOn"));

        TextView setTime = InvitationDetails.this.findViewById(R.id.timeOfParty);
        setTime.setText(intent.getExtras().getString("hostedAt"));

        TextView budget = InvitationDetails.this.findViewById(R.id.budgetLimit);
        budget.setText(intent.getExtras().getString("price"));

//=================================================================================================== Decline invite
        Button declineInvite = InvitationDetails.this.findViewById(R.id.declineInvite);
        declineInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Set the invite status to false
                GuestList status;
                status = GuestList.builder()
                        .inviteStatus("declined")
                        .invitee("host")
                        .invitedUser(loggedUser.getUserName())
                        .user(loggedUser)
                        .party(party)
                        .build();
                Log.i("Aplify.status", "This is status " + status);
                Amplify.API.mutate(
                        ModelMutation.create(status),
                        response -> Log.i("DeclinedInvite", "You declined an invite!"),
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

                //TODO: Set the invite status to accepted/true
                GuestList status;
                status = GuestList.builder()
                        .inviteStatus("accepted")
                        .user(loggedUser)
                        .build();

                Amplify.API.mutate(
                        ModelMutation.create(status),
                        response -> Log.i("AcceptedInvite", "You declined an invite!"),
                        error -> Log.e("DeclinedInviteFail", error.toString())
                );

                Gift gift;
                gift = Gift.builder()
                        .title(giftName)
                        .user(loggedUser) //---------------------------> Does a user have to own this gift, to bring it to the party?
                        .build();

                Amplify.API.mutate(
                        ModelMutation.create(gift),
                        response -> Log.i("AddGift", "You saved a new gift to bring, " + giftName + "----"),
                        error -> Log.e("AddGiftFail", error.toString())
                );
                Intent gotoPending = new Intent(InvitationDetails.this, PendingPage.class);
                InvitationDetails.this.startActivity(gotoPending);
            }
        });
    }

    public boolean getIsSignedIn() {
        boolean[] isSignedIn = {false};
        Amplify.Auth.fetchAuthSession(
                result -> {
                    Log.i("Amplify.login", result.toString());
                    Message message = new Message();
                    if(result.isSignedIn()) {
                        message.arg1 = 1;
                        handlecheckLoggedIn.sendMessage(message);
                    } else {
                        message.arg1 = 0;
                        handlecheckLoggedIn.sendMessage(message);
                    }
                },
                error -> Log.e("Amplify.login", error.toString())
        );
        return isSignedIn[0];
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(InvitationDetails.this, MainActivity.class);
        InvitationDetails.this.startActivity(intent);
        return true;
    }
}