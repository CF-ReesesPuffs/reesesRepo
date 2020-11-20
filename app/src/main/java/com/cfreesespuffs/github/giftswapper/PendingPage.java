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
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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
    ArrayList<GuestList> guestList = new ArrayList<>();

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
        Intent intent = getIntent();
        String partyId = intent.getExtras().getString("id");
        String partyTitle = intent.getExtras().getString("partyName");

        System.out.println(intent.getExtras().getString("title"));


        ImageButton homeDetailButton = PendingPage.this.findViewById(R.id.homePartyDetailButton);
        homeDetailButton.setOnClickListener((view)-> {
            Intent goToMainIntent = new Intent(PendingPage.this, MainActivity.class);
            PendingPage.this.startActivity(goToMainIntent);
        });

        Button startParty = PendingPage.this.findViewById(R.id.start_party);
        startParty.setOnClickListener((view) -> {

            Intent intent2 = new Intent(PendingPage.this, CurrentParty.class);
            intent2.putExtra("id", partyId);
            intent2.putExtra("thisPartyId", intent.getExtras().getString("title"));
            PendingPage.this.startActivity(intent2);
            });

        TextView title = PendingPage.this.findViewById(R.id.partyName);
        title.setText(intent.getExtras().getString("title"));

//        TextView host = PendingPage.this.findViewById(R.id.hostUser);
//        host.setText(intent.getExtras().getString("host"));

        TextView date = PendingPage.this.findViewById(R.id.startDate);
        date.setText(intent.getExtras().getString("date"));

        TextView time = PendingPage.this.findViewById(R.id.startTime);
        time.setText(intent.getExtras().getString("time"));

        TextView price = PendingPage.this.findViewById(R.id.priceLimit);
        price.setText(intent.getExtras().getString("price"));

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

//        Log.i("Here is our id", intent.getExtras().getString("id"));

        Amplify.API.query(
                ModelQuery.get(Party.class, intent.getExtras().getString("id")),
                response -> {
//                    party = response.getData();
                    Log.i("Amplify.test", "====" + response);
                    for (GuestList user : response.getData().getUsers()) {
                        Log.i("Amplify.test", "stuff to test " + user);
                        guestList.add(user);
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
        recyclerView.setAdapter(new ViewAdapter(guestList, this));
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(PendingPage.this, MainActivity.class);
        PendingPage.this.startActivity(intent);
        return true;
    }


    @Override
    public void listener(GuestList guestList) {

    }
}