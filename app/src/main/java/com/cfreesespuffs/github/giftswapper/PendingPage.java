package com.cfreesespuffs.github.giftswapper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.GuestList;
import com.amplifyframework.datastore.generated.model.Party;
import com.cfreesespuffs.github.giftswapper.Activities.MainActivity;
import com.cfreesespuffs.github.giftswapper.Adapters.ViewAdapter;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class PendingPage extends AppCompatActivity implements ViewAdapter.OnInteractWithTaskListener {

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    NavigationView navigationView;
    RecyclerView recyclerView;
    Handler handler;
    Handler handleSingleItem;
    ArrayList<GuestList> guestList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pending_page_navigation);

        toolbar = findViewById(R.id.pending_page_menu_toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.pending_page_drawer);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close); // https://developer.android.com/reference/androidx/appcompat/app/ActionBarDrawerToggle for dev docs
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.getDrawerArrowDrawable().setColor(getColor(R.color.black)); // because setting the drawer arrow drawable in the theme makes it disappear.
        actionBarDrawerToggle.getDrawerArrowDrawable().setTint(getColor(R.color.black)); // and this finally gets rid of the weak gray/lightening tent. Uncertain if attempting to change this in the theme also makes the arrow disappear.

        navigationView = findViewById(R.id.pending_page_navigation_view);
        actionBarDrawerToggle.syncState();

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

//        ImageButton homeDetailButton = PendingPage.this.findViewById(R.id.homePartyDetailButton);
//        homeDetailButton.setOnClickListener((view)-> {
//            Intent goToMainIntent = new Intent(PendingPage.this, MainActivity.class);
//            PendingPage.this.startActivity(goToMainIntent);
//        });

        Button startParty = PendingPage.this.findViewById(R.id.start_party);
        startParty.setOnClickListener((view) -> {

            Intent intent2 = new Intent(PendingPage.this, CurrentParty.class);
            intent2.putExtra("id", partyId);
            intent2.putExtra("thisPartyId", intent.getExtras().getString("title"));
            PendingPage.this.startActivity(intent2);
            });

        TextView title = PendingPage.this.findViewById(R.id.partyName);
        title.setText(intent.getExtras().getString("title"));

//        TextView host = PendingPage.this.findViewById(R.id.price);
//        host.setText(intent.getExtras().getString("host"));

        Button homeButton = findViewById(R.id.customHomeButton);
        homeButton.setOnClickListener((view) -> {
            Log.i("Activity.homeButton", "It was clicked.");
            Intent intent1 = new Intent(this, MainActivity.class);
            startActivity(intent1);
        });


        TextView date = PendingPage.this.findViewById(R.id.startDate);
        date.setText(intent.getExtras().getString("date"));

        TextView time = PendingPage.this.findViewById(R.id.startTime);
        time.setText(intent.getExtras().getString("time"));

        // Has to be included, otherwise doesn't show up via layout xml \
        TextView priceDef = PendingPage.this.findViewById(R.id.price);//|
        priceDef.setText("Price");                                    //|
        // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^|

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
    public void listener(GuestList guestList) { }
}