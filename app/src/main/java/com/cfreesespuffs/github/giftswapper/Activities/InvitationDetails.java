package com.cfreesespuffs.github.giftswapper.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
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

// todo: import confetti?

public class InvitationDetails extends AppCompatActivity {

    User loggedUser;
    Intent intent;
    Party party;
    GuestList guestList;
    EditText giftChosen;
    Button acceptInvite;
    SharedPreferences preferences;
    Handler handler;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invited_party_page);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        intent = getIntent();
        String partyId = intent.getExtras().getString("partyId");

        handler = new Handler(Looper.getMainLooper(), msg -> {
            if (msg.arg1 == 1) {
                findViewById(R.id.declineInvite).setVisibility(View.VISIBLE);
            }

            if (msg.arg1 ==2) {
                TextView hostName = InvitationDetails.this.findViewById(R.id.partyHost);
                hostName.setText(party.getTheHost().getUserName());
            }
            return false;
        });

        Amplify.API.query(
                ModelQuery.get(Party.class, partyId),
                response -> {
                    party = response.getData();

                    if (!preferences.getString("username", "NA").equals(party.getTheHost().getUserName())) {
                        Message message = new Message();
                        message.arg1 = 1;
                        handler.sendMessage(message);
                    }

                    Message message = new Message();
                    message.arg1 = 2;
                    handler.sendMessage(message);
                },
                error -> Log.e("Amplify.query", "no party " + error)
        );

        Amplify.API.query(
                ModelQuery.list(User.class, User.ID.eq(preferences.getString("userId", "NA"))),
                response -> {
                    for (User user : response.getData()) loggedUser = user;
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
        declineInvite.setOnClickListener(v -> {
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
        });

        acceptInvite = InvitationDetails.this.findViewById(R.id.acceptInvite);
        giftChosen = InvitationDetails.this.findViewById(R.id.giftUserBrings);

        giftChosen.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) | (actionId == EditorInfo.IME_ACTION_DONE)) {
                acceptInvite.requestFocus();

                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE); // finds the keyboard
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0); // hides the keyboard away in most other cases.
            }
            return false; // puts the keyboard away in most other cases.
        });

//============================================= Accept invite
        acceptInvite.setOnClickListener(v -> {

            long startTime = System.currentTimeMillis();

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
                    .number(intent.getExtras().getString("partyId", "NA"))
                    .build();

            GuestList guestList = party.getUsers().stream()
                    .filter(guest -> guest.getInvitedUser().equalsIgnoreCase(loggedUser.getUserName()))
                    .findFirst().orElse(null); // https://stackoverflow.com/questions/53719097/retrieve-single-object-from-list-using-java8-stream-api

            guestList.inviteStatus = "Accepted";

            Amplify.API.mutate(
                    ModelMutation.create(gift),
                    response2 -> {
                        Log.i("AddGift", "You saved a new gift to bring, " + giftName);

                        Amplify.API.mutate(
                                ModelMutation.update(guestList),
                                response -> {
                                    Log.i("AcceptedInvite", "You accepted an invite!");
                                    Log.e("system.startTIme", Long.toString(startTime));
                                    Intent gotoPending = new Intent(InvitationDetails.this, MainActivity.class);
                                    gotoPending.putExtra("partyName", party.getTitle());
                                    gotoPending.putExtra("startTime", Long.toString(startTime));
                                    InvitationDetails.this.startActivity(gotoPending);
                                },
                                error -> Log.e("AcceptedInviteFail", error.toString())
                        );

                    },
                    error -> Log.e("AddGiftFail", error.toString())
            );
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