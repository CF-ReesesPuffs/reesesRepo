package com.cfreesespuffs.github.giftswapper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.GuestList;
import com.amplifyframework.datastore.generated.model.Party;
import com.cfreesespuffs.github.giftswapper.Activities.MainActivity;
import com.cfreesespuffs.github.giftswapper.Adapters.ViewAdapter;

import java.util.ArrayList;

public class PendingPage extends AppCompatActivity implements ViewAdapter.OnInteractWithTaskListener {

    RecyclerView recyclerView;
    Handler handler;
    Handler handleSingleItem;
    ArrayList<GuestList> guestList;
    ArrayList<String> attendingGuests = new ArrayList<>();
    ArrayList<String> statusGuests;
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
                        if (msg.arg1 == 1) {
                            Log.i("Amplify", "It worked!");
                        }
                        recyclerView.getAdapter().notifyItemInserted(guestList.size());
                        return false;
                    }
                });
        connectAdapterToRecycler();

        ImageButton homeDetailButton = PendingPage.this.findViewById(R.id.homePartyDetailButton);
        homeDetailButton.setOnClickListener((view) -> {
            Intent goToMainIntent = new Intent(PendingPage.this, MainActivity.class);
            PendingPage.this.startActivity(goToMainIntent);
        });


        Intent intent = getIntent();

        TextView partyName = PendingPage.this.findViewById(R.id.homePartyTitleButton);
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

//        Amplify.API.query(
//                ModelQuery.get(Party.class, intent.getExtras().getString("id")),
//                        response -> {
//                    for(GuestList guest : response.getData().getUsers()){
//                        if(party.users.contains(guest)){
//                            guestList.add(guest);
//                        }
//                    }
//                            Log.i("AmplifyTest", "Checking the intent" + intent.getExtras().getString("partyName"));
//                            Log.i("Amplify.Query", "You got a party, lets check that out " + response.getData());
//                            party = response.getData();
//                            handleSingleItem.sendEmptyMessage(1);
//                        },
//                        error -> Log.e("Amplify.Query", "error, you dun goofed")
//        );

        Log.i("Here is our id", intent.getExtras().getString("id"));

        Amplify.API.query(
                ModelQuery.get(Party.class, intent.getExtras().getString("id")),
                response -> {
                    for (GuestList user : response.getData().getUsers()) {
//                        if (guestList.getParty().getId().contains(intent.getExtras().getString("id"))) {
//                            attendingGuests.add(guestList.getUser().getUserName());
                        Log.i("Amplify.test", "stuff to test " + user);
                        attendingGuests.add(user.getInvitedUser());
//                            Log.i("Amplify.test", "Lets look at all of our parties users " + user.);
//                            Log.i("Amplify.test", "================= " + guestList.);
//                            Log.i("Amplify.test", "lets look at all the statuses of our users" + guestList.getUsers());
//                            statusGuests.add(guestList.getUser().getInviteStatus().toString());

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
        recyclerView.setAdapter(new ViewAdapter(attendingGuests, this));
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(PendingPage.this, MainActivity.class);
        PendingPage.this.startActivity(intent);
        return true;
    }

    @Override
    public void taskListener(String inviteStatus) {

    }
}