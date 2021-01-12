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
import android.widget.Toast;

import com.amplifyframework.api.ApiOperation;
import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.api.graphql.model.ModelSubscription;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Gift;
import com.amplifyframework.datastore.generated.model.GuestList;
import com.amplifyframework.datastore.generated.model.Party;
import com.amplifyframework.datastore.generated.model.User;
import com.cfreesespuffs.github.giftswapper.Activities.MainActivity;
import com.cfreesespuffs.github.giftswapper.Adapters.ViewAdapter;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class PendingPage extends AppCompatActivity implements ViewAdapter.OnInteractWithTaskListener, NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;
    RecyclerView recyclerView;
    Handler handler;
    Handler handleSingleItem;
    ApiOperation subscription;
    ArrayList<GuestList> guestList = new ArrayList<>();
    ArrayList<GuestList> attendeesGuestList = new ArrayList<>();
    MenuItem partyDeleter;
    String partyId;
    Party pendingParty;
    int counter = 0;

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

        handleSingleItem = new Handler(Looper.getMainLooper(),
                new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message msg) {
                        System.out.println("This is the msg arg: " + msg.arg1);

                        if (msg.arg1 == 1) {
                            connectAdapterToRecycler();
                            recyclerView.getAdapter().notifyDataSetChanged();
                            Log.i("Amplify", "It worked!");
                        }

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

            counter = 0;
            for (int i = 0; i < guestList.size(); i++) {
                if (guestList.get(i).getInviteStatus().contains("Accepted")) { // && guestList.get(i).getTurnOrder() == 0
                    counter++;
                    //guestList.get(i).turnOrder = counter;
                    attendeesGuestList.add(guestList.get(i));
                }
            }

            if (counter == 1) {
                Toast.makeText(this, "need more guests to accept", Toast.LENGTH_LONG).show();
                return;
            }

            if (counter == 2) {
                AlertDialog.Builder twoPlayerSwapAlert = new AlertDialog.Builder(this);
                twoPlayerSwapAlert.setCancelable(true)
                        .setTitle("Two Party Giftswapping")
                        .setMessage("There are only two participants. Would you like to automatically swap gifts?")
                        .setPositiveButton("Yes",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        for (GuestList guest : attendeesGuestList) {
                                            Amplify.API.mutate( // don't run this code until we are going to the party
                                                    ModelMutation.update(guest),
                                                    response -> Log.i("Amplify.turnOrder", "You have a turn! " + response.getData()),
                                                    error -> Log.e("Amplify.turnOrder", "Error: " + error)
                                            );
                                        }

                                        try { // makes system pause/wait/sleep to allow above for loop to finish executing. https://www.thejavaprogrammer.com/java-delay/
                                            Thread.sleep(1000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        autoSwap(); // also goes to the PostParty activity, and set Party.isFinished() to true.
                                        Log.i("Counter.Two", "bumpBump");
                                    }
                                });
                twoPlayerSwapAlert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        System.out.println("*****************no dialog hit****************");
                        return;
                    }
                });
                AlertDialog dialog = twoPlayerSwapAlert.create();
                dialog.show();
            }

            if (!pendingParty.isReady && counter > 2) {
                for (GuestList guest : attendeesGuestList) {
                    Amplify.API.mutate( // don't run this code until we are going to the party
                            ModelMutation.update(guest),
                            response -> Log.i("Amplify.turnOrder", "You have a turn! " + response.getData()),
                            error -> Log.e("Amplify.turnOrder", "Error: " + error)
                    );
                }

                try { // makes system pause/wait/sleep to allow above for loop to finish executing. https://www.thejavaprogrammer.com/java-delay/
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                pendingParty.isReady = true;
                Amplify.API.mutate(
                        ModelMutation.update(pendingParty),
                        response -> Log.i("Amp.partyReady", "all set to go"),
                        error -> Log.e("Amp.partyReady", "it did not go")
                );

                goToParty();
//                    subscription.cancel();
//
//                    Intent intent2 = new Intent(PendingPage.this, CurrentParty.class);
//                    intent2.putExtra("id", partyId);
//                    intent2.putExtra("thisPartyId", intent.getExtras().getString("title"));
//
//                    PendingPage.this.startActivity(intent2);
            }
        });

        TextView title = PendingPage.this.findViewById(R.id.partyName);
        title.setText(intent.getExtras().

                getString("title"));


        Button homeButton = findViewById(R.id.customHomeButton);
        homeButton.setOnClickListener((view) ->

        {
            subscription.cancel(); // not functioning as expected (aka not working at all).
            Log.i("Activity.homeButton", "It was clicked.");
            Intent intent1 = new Intent(this, MainActivity.class);
            startActivity(intent1);
        });

        TextView date = PendingPage.this.findViewById(R.id.startDate);
        date.setText(intent.getExtras().

                getString("date"));

        TextView time = PendingPage.this.findViewById(R.id.startTime);
        time.setText(intent.getExtras().

                getString("time"));

        // Has to be included, otherwise doesn't show up via layout xml \
        TextView priceDef = PendingPage.this.findViewById(R.id.price);//|
        priceDef.setText("Price");                                    //|
        // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^|

        TextView price = PendingPage.this.findViewById(R.id.priceLimit);
        price.setText(intent.getExtras().

                getString("price"));

        //TODO: Query api to get users who's preference equals "accepted"/"RSVP"?

        Amplify.API.query(
                ModelQuery.get(Party.class, intent.getExtras().

                        getString("id")),
                response ->

                {
                    for (GuestList user : response.getData().getUsers()) {
                        Log.i("Amplify.test", "stuff to test " + user);
                        guestList.add(user);
                    }
                    handleSingleItem.sendEmptyMessage(1);
                },
                error -> Log.e("Amplify", "Failed to retrieve store")
        );

        subscription = Amplify.API.subscribe( // TODO: how to focus/narrow subscription so it doesn't get the firehose of ALL EVERYTHING ALWAYS BEING CHANGED.
                ModelSubscription.onUpdate(GuestList.class),
                onEstablished -> Log.i("Amp.Subscribe", "Subscription to Guestlist: Success"),
                newGuests ->

                {
                    Log.i("Amp.Subscribe.details", "This is the content: " + newGuests.getData());

                    Amplify.API.query(
                            ModelQuery.get(Party.class, intent.getExtras().getString("id")),
                            response -> {

                                guestList.clear();

                                if (response.getData() != null) { // subscriptions can return a completely empty/null response. ???
                                    for (GuestList user : response.getData().getUsers()) {
                                        Log.i("Amplify.test", "Within the Subscription: " + user);
                                        guestList.add(user);
                                    }
                                }

                                Amplify.API.query(
                                        ModelQuery.get(Party.class, partyId),
                                        responseParty -> {
                                            Log.i("Amp.Partyhere", "Party has been getten.");
                                            pendingParty = responseParty.getData();
                                        },
                                        error -> Log.e("Amp.Partyhere", "Error down: " + error)
                                );

                                Message message = new Message();
                                message.arg1 = 1;
                                handleSingleItem.sendMessage(message);
                            },
                            error -> Log.e("Amplify", "Failed to retrieve store")
                    );
                },
                error -> Log.e("Amp.Sub.Fail", "Failure: " + error),
                () -> Log.i("Amp.Subscribe.details", "Subscription Complete.")
        );

        subscription.start();

        Amplify.API.query(
                ModelQuery.get(Party.class, partyId),
                response ->

                {
                    Log.i("Amp.Partyhere", "Party has been getten.");
                    pendingParty = response.getData();
                    Log.i("Amp.Partyhere", "pendingParty's host: " + pendingParty.getTheHost().getUserName());
                    Log.i("Amp.Partyhere", "Auth username: " + Amplify.Auth.getCurrentUser().getUsername());
                    if (pendingParty.getTheHost().getUserName().equalsIgnoreCase(Amplify.Auth.getCurrentUser().getUsername())) {
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
    public void listener(GuestList guestList) {
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START); // Cannot be included in the if statement.
        System.out.println("A menu item has been clicked!");
        if (item.getItemId() == R.id.partyDeleteMenuItem) { // https://stackoverflow.com/questions/36747369/how-to-show-a-pop-up-in-android-studio-to-confirm-an-order
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

    public void deleteParty() {

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

                                    try { // makes system pause/wait/sleep to allow above for loop to finish executing. https://www.thejavaprogrammer.com/java-delay/
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    subscription.cancel();

                                    Intent intent = new Intent(this, MainActivity.class);
                                    startActivity(intent);
                                }
                            },
                            error -> Log.e("Amp.del.user", "Failure: " + error));
                },
                error -> Log.e("Amp.del.party", "FAIL: " + error));
    }

    public void autoSwap() {

        List<Gift> giftList = pendingParty.getGifts();

        User tempGiftUser = giftList.get(0).getUser();

        giftList.get(0).partyGoer = giftList.get(1).getUser().getUserName();
        giftList.get(0).user = giftList.get(1).getUser();
        giftList.get(1).partyGoer = tempGiftUser.getUserName();
        giftList.get(1).user = tempGiftUser;

        for (Gift gift : giftList) {
            Amplify.API.mutate(
                    ModelMutation.update(gift),
                    response -> Log.i("Amp.2GSwapUpdate", "Gift swap Success"),
                    error -> Log.e("Amp.2GSwapUpdate", "Fail here")
            );
        }

        pendingParty.isFinished = true;

        Amplify.API.query(
                ModelMutation.update(pendingParty),
                response2 -> {
                    subscription.cancel();
                    Intent headToPostParty = new Intent(PendingPage.this, PostParty.class);
                    headToPostParty.putExtra("title", pendingParty.getTitle());
                    headToPostParty.putExtra("partyId", pendingParty.getId());
                    startActivity(headToPostParty);
                    Log.i("Mutation.thisParty", "Party: Complete!");
                },
                error -> Log.e("Mutation.thisParty", "Party mutate: FAIL")
        );
    }

    public void goToParty() {

        subscription.cancel();

        Intent intent2 = new Intent(PendingPage.this, CurrentParty.class);
        intent2.putExtra("id", partyId);
        intent2.putExtra("thisPartyId", getIntent().getExtras().getString("title"));

        PendingPage.this.startActivity(intent2);
    }
}
