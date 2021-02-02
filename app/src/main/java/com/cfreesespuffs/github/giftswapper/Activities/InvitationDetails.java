package com.cfreesespuffs.github.giftswapper.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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
import com.cfreesespuffs.github.giftswapper.R;

import java.util.List;

public class InvitationDetails extends AppCompatActivity {

    User loggedUser;
    Intent intent;
    Party party;
    GuestList guestList;
    int highestNum = 0;
    EditText giftChosen;
    Button acceptInvite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invited_party_page);

        intent = getIntent();
        String partyId = intent.getExtras().getString("partyId");
        System.out.println("This is the party ID " +partyId);

        Amplify.API.query(
                ModelQuery.get(Party.class, partyId),
                response -> {
                    party = response.getData();
                    TextView hostName = InvitationDetails.this.findViewById(R.id.partyHost);
                    hostName.setText(party.getTheHost().getUserName());
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
                            if (user.getUserName().equalsIgnoreCase(authUser.getUsername())){
                                loggedUser = user;
                                Log.i("Amplify.currentUser", "This is the current user, " + loggedUser);
                            }
                        }
                    },
            error -> Log.e("Amplify.currentUser", "error"));
        }

//=================================================================================================== Invitation details
        Intent intent = getIntent();

        TextView partyName = InvitationDetails.this.findViewById(R.id.partyName);
        partyName.setText(intent.getExtras().getString("partyName"));

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

                List<GuestList> target = party.getUsers();
                for(GuestList thisGuestList : target){
                    if(thisGuestList.getInvitedUser().equalsIgnoreCase(authUser.getUsername())){
                        guestList = thisGuestList;
                    }
                }
                if(guestList == null){
                  Log.i("Amplify.error", "Couldn't find the user");
                }

                guestList.inviteStatus = "Declined";

//                Log.i("Amplify.guestList", "This is guest list " + guestList);
                Amplify.API.mutate(
                        ModelMutation.update(guestList),
                        response -> Log.i("DeclinedInvite", "You declined an invite! " + response.getData().toString()),

                        error -> Log.e("DeclinedInviteFail", error.toString())
                );

                Intent gotoMain = new Intent(InvitationDetails.this, MainActivity.class);
                intent.putExtra("status", guestList.getInviteStatus());
                InvitationDetails.this.startActivity(gotoMain);
            }
        });

        acceptInvite = InvitationDetails.this.findViewById(R.id.acceptInvite);

        giftChosen = InvitationDetails.this.findViewById(R.id.giftUserBrings);
        giftChosen.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) | (actionId == EditorInfo.IME_ACTION_DONE)) {
                    acceptInvite.requestFocus();

                    InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE); // finds the keyboard
                    imm.hideSoftInputFromWindow(v.getWindowToken(),0); // hides the keyboard away in most other cases.
                }
                return false; // puts the keyboard away in most other cases.
            }
        });


//=================================================================================================== Accept invite
        acceptInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String giftName = giftChosen.getText().toString();

                if(giftName.equals("")){
                    handlerCheck(1);
                    return;
                }

                Amplify.API.query(
                        ModelQuery.list(Gift.class),
                        response -> {
                            for (Gift gift : response.getData()) {
                                String theParty = intent.getExtras().getString("partyName");
                                String giftParty = gift.getParty().getTitle();
                                System.out.println("Here is theParty: " + theParty + ". And here is the giftParty: " + giftParty);
                                if (gift.getParty().getTitle().equals(theParty)) {
                                    if (gift.getNumber() > highestNum) highestNum = gift.getNumber();
                                }
                            }

                            System.out.println("Success, Highest Number now: " + highestNum); // count 1

                            Gift gift = Gift.builder()
                                    .title(giftName)
                                    .party(party)
                                    .timesStolen(0)
                                    .user(loggedUser)
                                    .partyGoer("TBD")
                                    .lastPartyGoer(authUser.getUsername())
                                    .number(highestNum + 1) // tried incrementing, was not currently functioning
                                    .build();

                            Amplify.API.mutate(
                                    ModelMutation.create(gift),
                                    response2 -> Log.i("AddGift", "You saved a new gift to bring, " + giftName),
                                    error -> Log.e("AddGiftFail", error.toString())
                            );

                            Log.i("Amplify.endModelQuery", "Success of query.");

                        },
                        error -> Log.e("Amplify.Query", "something went wrong" + error.toString())
                );

                List<GuestList> target = party.getUsers();
                for(GuestList thisGuestList : target){
                    if(thisGuestList.getInvitedUser().equalsIgnoreCase(authUser.getUsername())){
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

                Intent gotoPending = new Intent(InvitationDetails.this, MainActivity.class);
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