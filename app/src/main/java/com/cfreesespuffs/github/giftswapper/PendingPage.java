package com.cfreesespuffs.github.giftswapper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.GuestList;
import com.amplifyframework.datastore.generated.model.InviteStatus;
import com.amplifyframework.datastore.generated.model.Party;
import com.amplifyframework.datastore.generated.model.User;
import com.cfreesespuffs.github.giftswapper.Activities.MainActivity;
import com.cfreesespuffs.github.giftswapper.Adapters.ViewAdapter;

import java.util.ArrayList;

public class PendingPage extends AppCompatActivity implements ViewAdapter.OnInteractWithTaskListener{

    RecyclerView recyclerView;
    Handler handler;
    Handler handleSingleItem;
    ArrayList<InviteStatus> inviteStatusList;
    ArrayList<GuestList> guestList;
    Party party;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_page);


        handler = new Handler(Looper.getMainLooper(),
                new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message msg) {
                        connectAdapterToRecycler();
                        recyclerView.getAdapter().notifyDataSetChanged();
                        return false;
                    }
                });

        handleSingleItem = new Handler(Looper.getMainLooper(),
                new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message msg) {
                        if(msg.arg1 == 1){
                            Log.i("Amplify");
                        }
                        recyclerView.getAdapter().notifyItemInserted(inviteStatusList.size());
                        return false;
                    }
                });
        connectAdapterToRecycler();

        Intent intent = getIntent();

        TextView partyName = PendingPage.this.findViewById(R.id.partyName);
        partyName.setText(intent.getExtras().getString("partyName"));

//        TextView host = PendingPage.this.findViewById(R.id.hostUser);
//        host.setText(intent.getExtras().getString("host"));

        TextView when = PendingPage.this.findViewById(R.id.startDate);
        when.setText(intent.getExtras().getString("when"));

        TextView setTime = PendingPage.this.findViewById(R.id.startTime);
        setTime.setText(intent.getExtras().getString("setTime"));

        TextView budget = PendingPage.this.findViewById(R.id.priceLimit);
        budget.setText(intent.getExtras().getString("budget"));

        //TODO: Query api to get users who's preference equals "accepted"/"RSVP"?

        Amplify.API.query(
                ModelQuery.get(Party.class, intent.getExtras().getString("id")),
                        response -> {
                    for(GuestList guest : response.getData().getUsers()){
                        if(party.users.contains(guest)){
                            guestList.add(guest);
                        }
                    }
                            Log.i("AmplifyTest", "Checking the intent" + intent.getExtras().getString("partyName"));
                            Log.i("Amplify.Query", "You got a party, lets check that out " + response.getData());
                            party = response.getData();
                            handleSingleItem.sendEmptyMessage(1);
                        },
                        error -> Log.e("Amplify.Query", "error, you dun goofed")
        );

        Amplify.API.query(
                ModelQuery.list(InviteStatus.class),
                response -> {
                    for (InviteStatus invite : response.getData()) {
//                        if (preferences.contains("RSVP")) {
//                            if (invite.getStatus().equals(preferences.getString("RSVP", null))) {
//                                inviteStatusList.add(invite);
//                            }
//                        } else {
//                            inviteStatusList.add(invite);
//                        }
                    }
                    handler.sendEmptyMessage(1);
                },
                error -> Log.e("Amplify", "Failed to retrieve store")
        );
        //TODO: How do we keep track of the gifts?

    }
    private void connectAdapterToRecycler() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ViewAdapter(inviteStatusList, this));
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(PendingPage.this, MainActivity.class);
        PendingPage.this.startActivity(intent);
        return true;
    }

    @Override
    public void taskListener(InviteStatus inviteStatus) {

    }
}