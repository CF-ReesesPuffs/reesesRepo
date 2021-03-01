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
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.amplifyframework.api.ApiOperation;
import com.amplifyframework.api.aws.GsonVariablesSerializer;
import com.amplifyframework.api.graphql.GraphQLRequest;
import com.amplifyframework.api.graphql.SimpleGraphQLRequest;
import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.api.graphql.model.ModelSubscription;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Gift;
import com.amplifyframework.datastore.generated.model.GuestList;
import com.amplifyframework.datastore.generated.model.Party;
import com.amplifyframework.datastore.generated.model.User;
import com.cfreesespuffs.github.giftswapper.Activities.CurrentParty;
import com.cfreesespuffs.github.giftswapper.Activities.MainActivity;
import com.cfreesespuffs.github.giftswapper.Adapters.ViewAdapter;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class
PendingPage extends AppCompatActivity implements ViewAdapter.OnInteractWithTaskListener, NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;
    RecyclerView recyclerView;
    Handler handleSingleItem;
    ApiOperation subscription;
    ApiOperation deleteSubscription;
    ArrayList<GuestList> guestList = new ArrayList<>();
    ArrayList<GuestList> attendeesGuestList = new ArrayList<>();
    MenuItem partyDeleter;
    MenuItem guestRemover;
    String partyId;
    Party pendingParty;
    int counter = 0;
    Button startParty;
    Intent intent;

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
        guestRemover = menu.findItem(R.id.partyRemoveGoer);

        startParty = PendingPage.this.findViewById(R.id.start_party);
        startParty.setEnabled(false);

        handleSingleItem = new Handler(Looper.getMainLooper(), msg -> {
            if (msg.arg1 == 1) {
                connectAdapterToRecycler();
                recyclerView.getAdapter().notifyDataSetChanged();
            }

            if (msg.arg1 == 2) {
                startParty.setText("Go to party!");
                startParty.setEnabled(true);
                partyDeleter.setVisible(true);
                guestRemover.setVisible(true);
                connectAdapterToRecycler();
            }

            if (msg.arg1 == 3) {
                startParty.setEnabled(true);
                startParty.setText("Go to party!");
            }

            if (msg.arg1 == 4) {

                subscription = Amplify.API.subscribe(
                        getGuestListByHost(pendingParty.getTheHost().getUserName()),
                        onEstablished -> Log.i("Amp.Subscribe", "Subscription to Guestlist: Success"),
                        newGuests -> {
                            Amplify.API.query(
                                    ModelQuery.get(Party.class, intent.getExtras().getString("id")),
                                    response -> {
                                        guestList.clear();
                                        if (response.getData() != null) { // subscriptions can return a completely empty/null response. ???
                                            guestList.addAll(response.getData().getUsers());
                                        }

                                        pendingParty = response.getData();

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

            }

            if (msg.arg1 == 6) {
                startParty.setEnabled(true);
                startParty.setText("Party's over!");
            }

            recyclerView.getAdapter().notifyItemInserted(guestList.size()); // might not be the right place...
            return false;
        });

        connectAdapterToRecycler();
        intent = getIntent();
        partyId = intent.getExtras().getString("id");

        Amplify.API.query(
                ModelQuery.get(Party.class, partyId),
                response -> {
                    pendingParty = response.getData();

                    TextView hostTv = findViewById(R.id.hostTv);
                    hostTv.setText(String.format("Host: %s", pendingParty.getTheHost().getUserName()));

                    if (pendingParty.getTheHost().getUserName().equalsIgnoreCase(Amplify.Auth.getCurrentUser().getUsername())) {
                        Message message = new Message();
                        message.arg1 = 2;
                        handleSingleItem.sendMessage(message);
                    }
                    if (pendingParty.isReady || pendingParty.isFinished) {
                        Message message = new Message();
                        message.arg1 = 3;
                        handleSingleItem.sendMessage(message);
                    }

                    Message message = new Message();
                    message.arg1 = 4;
                    handleSingleItem.sendMessage(message);

                },
                error -> Log.e("Amp.Partyhere", "Error down: " + error)
        );

        startParty.setOnClickListener((view) -> {

            if (pendingParty.isFinished) {
                Intent headToPostParty = new Intent(PendingPage.this, PostParty.class);

                headToPostParty.putExtra("title", pendingParty.getTitle());
                headToPostParty.putExtra("partyId", pendingParty.getId());
                headToPostParty.putExtra("when", String.valueOf(pendingParty.HOSTED_ON));
                headToPostParty.putExtra("setTime", String.valueOf(pendingParty.HOSTED_AT));

                subscription.cancel();

                startActivity(headToPostParty);
            } else if (pendingParty.isReady) {
                goToParty();
            } else {

                counter = 0;
                for (int i = 0; i < guestList.size(); i++) {
                    if (guestList.get(i).getInviteStatus().contains("Accepted")) {
                        counter++;
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
                                    (dialog, which) -> {
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
                                    });
                    twoPlayerSwapAlert.setNegativeButton("No", (dialogInterface, i) -> {
                    });
                    AlertDialog dialog = twoPlayerSwapAlert.create();
                    dialog.show();
                }

                if (!pendingParty.isReady && counter > 2) {
                    counter = 0;
                    for (int i = 0; i < guestList.size(); i++) {
                        if (guestList.get(i).getInviteStatus().contains("Accepted") && guestList.get(i).getTurnOrder() == 0) {
                            counter++;
                            guestList.get(i).turnOrder = counter;

                            Amplify.API.mutate(
                                    ModelMutation.update(guestList.get(i)),
                                    response -> Log.i("Amplify.turnOrder", "You have a turn! " + response.getData()),
                                    error -> Log.e("Amplify.turnOrder", "Error: " + error)
                            );
                        }
                    }
                    AlertDialog.Builder finalPartyAlert = new AlertDialog.Builder(this); // this is to slow down the process and allow guest mutation
                    finalPartyAlert.setCancelable(false)
                            .setTitle("let the party begin!")
                            .setMessage("the confetti is launched")
                            .setPositiveButton("huzzah",
                                    (dialogInterface, i) -> {
                                        pendingParty.isReady = true;
                                        Amplify.API.mutate(
                                                ModelMutation.update(pendingParty),
                                                response -> Log.i("Amp.partyReady", "all set to go"),
                                                error -> Log.e("Amp.partyReady", "it did not go")
                                        );
                                        goToParty();
                                    });
                    AlertDialog dialog = finalPartyAlert.create();
                    dialog.show();
                }
            }
        });

        TextView title = PendingPage.this.findViewById(R.id.partyName);
        title.setText(intent.getExtras().getString("title"));

        Button homeButton = findViewById(R.id.customHomeButton);
        homeButton.setOnClickListener((view) -> {
            subscription.cancel();
            deleteSubscription.cancel();

            Intent intent1 = new Intent(this, MainActivity.class);
            startActivity(intent1);
        });

        TextView date = PendingPage.this.findViewById(R.id.startDate);
        date.setText(intent.getExtras().getString("date"));

        TextView time = PendingPage.this.findViewById(R.id.startTime);
        time.setText(intent.getExtras().getString("time"));

        // Has to be included, otherwise doesn't show up via layout xml \
        TextView priceDef = PendingPage.this.findViewById(R.id.price);//|
        priceDef.setText(String.format("Price: %s", intent.getExtras().getString("price")));
        // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^|

        Amplify.API.query(
                ModelQuery.get(Party.class, intent.getExtras().getString("id")),
                response -> {
                    guestList.addAll(response.getData().getUsers());
                    handleSingleItem.sendEmptyMessage(1);
                },
                error -> Log.e("Amplify", "Failed to retrieve store")
        );

        createSinglePartySubscription(intent.getExtras().getString("id"));

        deleteSubscription = Amplify.API.subscribe(
                ModelSubscription.onDelete(GuestList.class),
                onDeleteFunctioning -> Log.i("Amp.SubOnDelete", "Killer Sub working."),
                lessGuest -> {
                    Amplify.API.query(
                            ModelQuery.get(Party.class, intent.getExtras().getString("id")),
                            delResponse -> {
                                guestList.clear();
                                if (delResponse.getData() != null) {
                                    guestList.addAll(delResponse.getData().getUsers());
                                }
                                Message message = new Message();
                                message.arg1 = 1;
                                handleSingleItem.sendMessage(message);
                            },
                            errorDel -> Log.e("Amp.SubDelete", "No nothing.")
                    );
                },
                subError -> Log.e("Amp.SubOnDelete", "FAIL"),
                () -> Log.i("Amp.SubOnDelete", "Delete Sub Complete")
        );

        deleteSubscription.start();
    }

    private void connectAdapterToRecycler() {
        recyclerView = findViewById(R.id.postPartyRV);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (pendingParty != null) {
            recyclerView.setAdapter(new ViewAdapter(guestList, pendingParty.getTheHost().getUserName(), this));
        } else {
            recyclerView.setAdapter(new ViewAdapter(guestList, "Alan Smithee", this)); // not great code. Could break one user's experience :P
        }
    }

    @Override
    public void listener(GuestList guestList) {
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START); // Cannot be included in the if statement.
        if (item.getItemId() == R.id.partyRemoveGoer) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true)
                    .setTitle("Remove Partygoers")
                    .setMessage("Would you like to remove them?")
                    .setPositiveButton("Yes", (dialog, which) -> removePartyGoers());
            builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                // nothing need be done.
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        if (item.getItemId() == R.id.partyDeleteMenuItem) { // https://stackoverflow.com/questions/36747369/how-to-show-a-pop-up-in-android-studio-to-confirm-an-order
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true)
                    .setTitle("Party Delete")
                    .setMessage("You looking to delete?")
                    .setPositiveButton("Confirm", (dialog, which) -> deleteParty());
            builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                // Todo: add analytics here
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        return true;
    }

    public void removePartyGoers() {

        ArrayList<GuestList> userToDeleteNow = ((ViewAdapter) recyclerView.getAdapter()).toRemove;

        for (GuestList toDelete : userToDeleteNow) {
            Amplify.API.query(
                    ModelQuery.get(User.class, toDelete.getUser().getId()),
                    userToGet -> {
                        User thisUser = userToGet.getData();
                        for (Gift thisPartysGift : thisUser.getGifts()) {
                            if (thisPartysGift.getParty().getId().equalsIgnoreCase(pendingParty.getId())) {
                                Amplify.API.mutate(
                                        ModelMutation.delete(thisPartysGift),
                                        giftToDelete -> Log.i("Amp.removeGift", "Removed gift"),
                                        error -> Log.e("Amp.removeGift", "Gift NOT GONE")
                                );
                            }
                        }
                    },
                    error -> Log.e("Amp.user", "ERROR: " + error)
            );

            Amplify.API.mutate(
                    ModelMutation.delete(toDelete),
                    partyGoerToDelete -> Log.i("Amp.removeGuest", "Remove partygoer."),
                    error -> Log.e("Amp.removeGuest", "Error." + error)
            );
        }
    }

    public void deleteParty() {

        Amplify.API.query(
                ModelQuery.get(Party.class, partyId),
                partyAllToDelete -> {
                    List<GuestList> gLToDelete = partyAllToDelete.getData().getUsers();
                    List<Gift> giftsToDelete = partyAllToDelete.getData().getGifts();

                    for (int i = 0; i < gLToDelete.size(); i++) {
                        Amplify.API.mutate(
                                ModelMutation.delete(gLToDelete.get(i)),
                                response4 -> Log.i("Amp.del.user", "You're outta there, " + guestList + "!"),
                                error -> Log.e("Amp.del.user", "Error: " + error));
                    }

                    for (int i = 0; i < giftsToDelete.size(); i++) {
                        Amplify.API.mutate(
                                ModelMutation.delete(giftsToDelete.get(i)),
                                response4 -> Log.i("Amp.del.user", "You're outta there!"),
                                error -> Log.e("Amp.del.user", "Error: " + error));
                    }

                    Amplify.API.mutate(
                            ModelMutation.delete(partyAllToDelete.getData()), // as before, it's not enough to have a party, you've got to get it's data too. why?
                            theParty -> Log.i("Amplify.delete", "Gone"),
                            error2 -> Log.e("Amplify.delete", "Where you at? Error: " + error2)
                    );

                    subscription.cancel();
                    deleteSubscription.cancel();

                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
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
        pendingParty.isReady = true;

        Amplify.API.query(
                ModelMutation.update(pendingParty),
                response2 -> {
                    subscription.cancel();
                    Intent headToPostParty = new Intent(PendingPage.this, PostParty.class);
                    headToPostParty.putExtra("title", pendingParty.getTitle());
                    headToPostParty.putExtra("partyId", pendingParty.getId());
                    startActivity(headToPostParty);
                },
                error -> Log.e("Mutation.thisParty", "Party mutate: FAIL")
        );
    }

    public void goToParty() {
        subscription.cancel();
        deleteSubscription.cancel();

        Intent intent2 = new Intent(PendingPage.this, CurrentParty.class);
        intent2.putExtra("id", partyId);
        intent2.putExtra("host", pendingParty.getTheHost().getUserName());
        intent2.putExtra("thisPartyId", getIntent().getExtras().getString("title"));
        PendingPage.this.startActivity(intent2);
    }

    private GraphQLRequest<Party> getPartyStatus(String id) {
        String document = "subscription getPartyStatus($id: ID!) { "
                + "onUpdateOfSpecificParty(id: $id) { "
                + "id "
                + "title "
                + "hostedOn "
                + "hostedAt "
                + "partyDateAWS "
                + "partyDate "
                + "price "
                + "isReady "
                + "isFinished "
                + "stealLimit "
                + "}"
                + "}";
        return new SimpleGraphQLRequest<>(
                document,
                Collections.singletonMap("id", id),
                Party.class,
                new GsonVariablesSerializer());
    }

    private void createSinglePartySubscription(String id) {
        Amplify.API.subscribe(getPartyStatus(id),
                subCheck -> Log.d("Sub.SingleParty", "Connection established for: " + subCheck),
                response -> { //TODO: add logic for parties ready
                    if (response.getData().isReady) {
                        pendingParty.isReady = true;
                        Message toParty = new Message();
                        toParty.arg1 = 3;
                        handleSingleItem.sendMessage(toParty);
                    }

                    if (response.getData().isFinished) {
                        pendingParty.isFinished = true; // because the current query we run doesn't replace everything of the pendingparty variable, and we don't need it to.
                        Message toPostParty = new Message();
                        toPostParty.arg1 = 6;
                        handleSingleItem.sendMessage(toPostParty);
                    }
                },
                failure -> Log.e("Sub.SingleParty", "failure: " + failure),
                () -> Log.i("Amp.SingleParty", "sub is closed")
        );
    }

    private GraphQLRequest<GuestList> getGuestListByHost(String host) {
        String document = "subscription hostGuestList ($invitee: String) { "
                + "onUpdateHostGuestList(invitee: $invitee) { "
                + "party { "
                + "id "
                + "}"
                + "}"
                + "}";
        return new SimpleGraphQLRequest<>(
                document,
                Collections.singletonMap("invitee", host),
                GuestList.class,
                new GsonVariablesSerializer());
    }
}
