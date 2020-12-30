package com.cfreesespuffs.github.giftswapper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.amplifyframework.api.ApiOperation;
import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.api.graphql.model.ModelSubscription;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Gift;
import com.amplifyframework.datastore.generated.model.GuestList;
import com.amplifyframework.datastore.generated.model.Party;
import com.cfreesespuffs.github.giftswapper.Activities.MainActivity;
import com.cfreesespuffs.github.giftswapper.Adapters.ViewAdapter;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class PendingPage extends AppCompatActivity implements ViewAdapter.OnInteractWithTaskListener, NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;
    RecyclerView recyclerView;
    Handler whereHandler;
    Handler handleSingleItem;
    ArrayList<GuestList> guestList = new ArrayList<>();
    MenuItem partyDeleter;
    String partyId;
    Party pendingParty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pending_page_navigation);
        Toolbar toolbar = findViewById(R.id.pending_page_menu_toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.pending_page_drawer);
        navigationView = findViewById(R.id.pending_page_navigation_view);
        navigationView.bringToFront(); // ESSENTIAL. Perhaps because where it is the layout, but this line makes it so that it is not only visible but clickable (why they would make it any other way by default, ga ke itse. https://stackoverflow.com/questions/39424310/onnavigationitemselected-not-working-in-navigationview for the deets
        navigationView.setNavigationItemSelectedListener(this);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close); // https://developer.android.com/reference/androidx/appcompat/app/ActionBarDrawerToggle for dev docs
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();

        actionBarDrawerToggle.getDrawerArrowDrawable().setColor(getColor(R.color.black)); // because setting the drawer arrow drawable in the theme makes it disappear.
        actionBarDrawerToggle.getDrawerArrowDrawable().setTint(getColor(R.color.black)); // and this finally gets rid of the weak gray/lightening tent. Uncertain if attempting to change this in the theme also makes the arrow disappear.

        Menu menu = navigationView.getMenu(); // https://stackoverflow.com/questions/31265530/how-can-i-get-menu-item-in-navigationview because every method of drawing on the screen, means there are that many ways to have to target. I am really interested knowing why targeting the same menu requires at least 3 different methods depending.
        partyDeleter = menu.findItem(R.id.partyDeleteMenuItem);

        whereHandler = new Handler(Looper.getMainLooper(),
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
                        System.out.println("This is the msg arg: " + msg.arg1);

                        if (msg.arg1 == 1) Log.i("Amplify", "It worked!");
                        if (msg.arg1 == 2) partyDeleter.setVisible(true);

                        recyclerView.getAdapter().notifyItemInserted(guestList.size());
                        return false;
                    }
                });

        connectAdapterToRecycler();
        Intent intent = getIntent();
        partyId = intent.getExtras().getString("id");

        System.out.println(intent.getExtras().getString("title"));

        Button startParty = PendingPage.this.findViewById(R.id.start_party);
        startParty.setOnClickListener((view) -> {

            // Todo: check for logic to ensure *only* acceptedInvite guestlist/users get a turn order.

            int counter = 1;

            for (int i = 0; i < guestList.size(); i++) {
                if (guestList.get(i).getInviteStatus().contains("Accepted") && guestList.get(i).turnOrder == 0) {
                    guestList.get(i).turnOrder = counter;
                    counter++;

                    Amplify.API.mutate(
                            ModelMutation.update(guestList.get(i)),
                            response -> Log.i("Amplify.turnOrder", "You have a turn! " + response.getData()),
                            error -> Log.e("Amplify.turnOrder", "Error: " + error)
                    );

                }
            }

            try { // makes system pause/wait/sleep to allow above for loop to finish executing. https://www.thejavaprogrammer.com/java-delay/
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Intent intent2 = new Intent(PendingPage.this, CurrentParty.class);
            intent2.putExtra("id", partyId);
            intent2.putExtra("thisPartyId", intent.getExtras().getString("title"));



            PendingPage.this.startActivity(intent2);
            });

        TextView title = PendingPage.this.findViewById(R.id.partyName);
        title.setText(intent.getExtras().getString("title"));


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

        Amplify.API.query(
                ModelQuery.get(Party.class, intent.getExtras().getString("id")),
                response -> {
                    for (GuestList user : response.getData().getUsers()) {
                        Log.i("Amplify.test", "stuff to test " + user);
                        guestList.add(user);
                    }
                    handleSingleItem.sendEmptyMessage(1);
                },
                error -> Log.e("Amplify", "Failed to retrieve store")
        );

        ApiOperation subscription = Amplify.API.subscribe( // is working. but checking the wrong thing. :\
                ModelSubscription.onUpdate(Party.class), // Todo: should be checking the Guestlist. :P not party.
                onEstablished -> Log.i("Amp.Subscribe", "Subscription to Guestlist: Success"),
                newGuests -> {
                    guestList.clear();
                    Log.i("Amp.Subscribe.details", "This is the content: " + newGuests.getData());

                    for (GuestList user : newGuests.getData().getUsers()) {
                        guestList.add(user);
                        whereHandler.sendEmptyMessage(1); // Todo: Now does this work, being passed to a handler now?
                    }
                },
                error -> Log.e("Amp.Sub.Fail", "Failure: " + error),
                () -> Log.i("Amp.Subscribe.details", "Subscription Complete.")
        );

        Amplify.API.query(
                ModelQuery.get(Party.class, partyId),
                response -> {
                    Log.i("Amp.Partyhere", "Party has been getten.");
                    pendingParty = response.getData();
                    Log.i("Amp.Partyhere", "pendingParty's host: " + pendingParty.getTheHost().getUserName());
                    Log.i("Amp.Partyhere", "Auth username: " + Amplify.Auth.getCurrentUser().getUsername() );
                    if (pendingParty.getTheHost().getUserName().equals(Amplify.Auth.getCurrentUser().getUsername())) {
                        Message message = new Message();
                        message.arg1 = 2;
                        handleSingleItem.sendMessage(message);
                    }
                },
                error -> Log.e("Amp.Partyhere", "Error down: " + error)
        );

    }

    private void connectAdapterToRecycler() {
        recyclerView = findViewById(R.id.postPartyRV);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ViewAdapter(guestList, this));
    }

    @Override
    public void listener(GuestList guestList) { }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START); // Cannot be included in the if statement.
        System.out.println("A menu item has been clicked!");
        if  (item.getItemId() == R.id.partyDeleteMenuItem) { // https://stackoverflow.com/questions/36747369/how-to-show-a-pop-up-in-android-studio-to-confirm-an-order
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true)
                    .setTitle("Party Delete")
                    .setMessage("You looking to delete?")
                    .setPositiveButton("Confirm",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteParty();
                                }
                            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Todo: add analytics here
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        return true;
    }

    public void deleteParty () {

        Amplify.API.query(
                ModelQuery.get(Party.class, partyId),
                partyAllToDelete -> {

                    Amplify.API.query(
                            ModelQuery.list(GuestList.class),
                            thePartyGoers -> {
                                for (GuestList guestList : thePartyGoers.getData()) {
                                    if (guestList.getParty().getId().contains(partyAllToDelete.getData().getId())) {

                                        Amplify.API.mutate(
                                                ModelMutation.delete(guestList),
                                                response4 -> Log.i("Amp.del.user", "You're outta there, " + guestList + "!"),
                                                error -> Log.e("Amp.del.user", "Error: " + error));
                                    }
                                }
                            },
                            error -> Log.e("Amp.del.user", "Failure: " + error));

                    Amplify.API.query(
                            ModelQuery.list(Gift.class),
                            allTheGifts -> {
                                for (Gift gift : allTheGifts.getData()) {
                                    if (gift.getParty().getId().contains(partyAllToDelete.getData().getId())) {

                                        Amplify.API.mutate(
                                                ModelMutation.delete(gift),
                                                response4 -> {

                                                    Amplify.API.mutate(
                                                            ModelMutation.delete(partyAllToDelete.getData()), // as before, it's not enough to have a party, you've got to get it's data too. why?
                                                            theParty -> Log.i("Amplify.delete", "Gone"),
                                                            error2 -> Log.e("Amplify.delete", "Where you at? Error: " + error2)
                                                    );

                                                    Log.i("Amp.del.user", "You're outta there, " + gift + "!");
                                                },
                                                error -> Log.e("Amp.del.user", "Error: " + error));
                                    }
                                    Intent intent = new Intent(this, MainActivity.class);
                                    startActivity(intent);
                                }
                            },
                            error -> Log.e("Amp.del.user", "Failure: " + error));

                },
                error -> Log.e("Amp.del.party", "FAIL: " + error));
        };
    }
