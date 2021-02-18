package com.cfreesespuffs.github.giftswapper.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
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
    SharedPreferences preferences;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invited_party_page);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        intent = getIntent();
        String partyId = intent.getExtras().getString("partyId");

        handler = new Handler(Looper.getMainLooper(),
                new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message msg) {

                        if (msg.arg1 == 1) {
                            findViewById(R.id.declineInvite).setVisibility(View.VISIBLE);
                        }
                        return false;
                    }
                });

                Amplify.API.query(
                        ModelQuery.get(Party.class, partyId),
                        response -> {
                            party = response.getData();
                            TextView hostName = InvitationDetails.this.findViewById(R.id.partyHost);
                            hostName.setText(party.getTheHost().getUserName());

                            if (!preferences.getString("username", "NA").equals(party.getTheHost().getUserName())) {
                                Log.e("pref.Username", "we here?");
                                Message message = new Message();
                                message.arg1 = 1;
                                handler.sendMessage(message);
                            }

                        },
                        error -> Log.e("Amplify.query", "no party " + error)
                );

        Amplify.API.query(
                ModelQuery.list(User.class, User.ID.eq(preferences.getString("userId", "NA"))),
                response -> {
                    for (User user : response.getData()) {
                        loggedUser = user;
                    }
                },
                error -> Log.e("Amplify.currentUser", "error")
        );

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
                for (GuestList thisGuestList : target) {
                    if (thisGuestList.getInvitedUser().equalsIgnoreCase(loggedUser.getUserName())) {
                        guestList = thisGuestList;
                    }
                }

                guestList.inviteStatus = "Declined";

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

                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE); // finds the keyboard
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0); // hides the keyboard away in most other cases.
                }
                return false; // puts the keyboard away in most other cases.
            }
        });

//============================================= Accept invite
        acceptInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String giftName = giftChosen.getText().toString();

                if (giftName.equals("")) {
                    handlerCheck(1);
                    return;
                }

                Gift gift = Gift.builder()
                        .title(giftName)
                        .party(party)
                        .timesStolen(0)
                        .user(loggedUser) // who *OWNS* the gift
                        .partyGoer("TBD") // who *holds* the gift
//                        .lastPartyGoer(loggedUser.getUserName())
                        .number(42)
                        .build();

                Amplify.API.mutate(
                        ModelMutation.create(gift),
                        response2 -> Log.i("AddGift", "You saved a new gift to bring, " + giftName),
                        error -> Log.e("AddGiftFail", error.toString())
                );

                List<GuestList> target = party.getUsers();
                for (GuestList thisGuestList : target) {
                    if (thisGuestList.getInvitedUser().equalsIgnoreCase(loggedUser.getUserName())) {
                        guestList = thisGuestList;
                    }
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
        }

        if (messageCode == 3) {
            Toast.makeText(this, "You need to bring a gift", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(InvitationDetails.this, MainActivity.class);
        InvitationDetails.this.startActivity(intent);
        return true;
    }
}