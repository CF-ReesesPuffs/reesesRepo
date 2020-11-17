package com.cfreesespuffs.github.giftswapper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Gift;
import com.amplifyframework.datastore.generated.model.InviteStatus;
import com.amplifyframework.datastore.generated.model.User;
import com.cfreesespuffs.github.giftswapper.Activities.MainActivity;

public class InvitedPartyPage extends AppCompatActivity {

    Handler handlecheckLoggedIn;
    User loggedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invited_party_page);

        handlecheckLoggedIn = new Handler(Looper.getMainLooper(), message -> {
            if (message.arg1 == 0) {
                Log.i("Amplify.login", "They weren't logged in");
            } else if (message.arg1 == 1) {
                Log.i("Amplify.login", Amplify.Auth.getCurrentUser().getUsername());
                TextView loggedUser = InvitedPartyPage.this.findViewById(R.id.current_user);
                loggedUser.setText(Amplify.Auth.getCurrentUser().getUsername());
                loggedUser.setVisibility(View.VISIBLE);

            } else {
                Log.i("Amplify.login", "Send true or false pls");
            }
            return false;
        });
//=================================================================================================== Invitation details
        Intent intent = getIntent();

        TextView partyName = InvitedPartyPage.this.findViewById(R.id.partyName);
        partyName.setText(intent.getExtras().getString("partyName"));

        TextView host = InvitedPartyPage.this.findViewById(R.id.partyHost);
        host.setText(intent.getExtras().getString("host"));

        TextView when = InvitedPartyPage.this.findViewById(R.id.dateOfParty);
        when.setText(intent.getExtras().getString("when"));

        TextView setTime = InvitedPartyPage.this.findViewById(R.id.timeOfParty);
        setTime.setText(intent.getExtras().getString("setTime"));

        TextView budget = InvitedPartyPage.this.findViewById(R.id.budgetLimit);
        budget.setText(intent.getExtras().getString("budget"));

//=================================================================================================== Decline invite
        Button declineInvite = InvitedPartyPage.this.findViewById(R.id.declineInvite);
        declineInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TODO: Set the invite status to false
                InviteStatus status;
                status = InviteStatus.builder()
                        .status("declined")
                        .name(loggedUser)
                        .build();
                Amplify.API.mutate(
                        ModelMutation.create(status),
                        response -> Log.i("DeclinedInvite", "You declined an invite!"),
                        error -> Log.e("DeclinedInviteFail", error.toString())
                );

                Intent gotoMain = new Intent(InvitedPartyPage.this, MainActivity.class);
                InvitedPartyPage.this.startActivity(gotoMain);
            }
        });

//=================================================================================================== Accept invite
        Button acceptInvite = InvitedPartyPage.this.findViewById(R.id.acceptInvite);
        acceptInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText giftChosen = InvitedPartyPage.this.findViewById(R.id.giftUserBrings);
                String giftName = giftChosen.getText().toString();

                //TODO: Set the invite status to accepted/true
                InviteStatus status;
                status = InviteStatus.builder()
                        .status("accepted")
                        .name(loggedUser)
                        .build();
                Amplify.API.mutate(
                        ModelMutation.create(status),
                        response -> Log.i("AcceptedInvite", "You declined an invite!"),
                        error -> Log.e("DeclinedInviteFail", error.toString())
                );

                Gift gift;
                gift = Gift.builder()
                        .title(giftName)
                        .build();

                Amplify.API.mutate(
                        ModelMutation.create(gift),
                        response -> Log.i("AddGift", "You saved a new gift to bring, " + giftName + "----"),
                        error -> Log.e("AddGiftFail", error.toString())
                );
                Intent gotoPending = new Intent(InvitedPartyPage.this, PendingPage.class);
                InvitedPartyPage.this.startActivity(gotoPending);
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(InvitedPartyPage.this, MainActivity.class);
        InvitedPartyPage.this.startActivity(intent);
        return true;
    }
}