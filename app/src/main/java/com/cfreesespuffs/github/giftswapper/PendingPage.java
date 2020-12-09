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
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
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
    Handler handler;
    Handler handleSingleItem;
    ArrayList<GuestList> guestList = new ArrayList<>();
    MenuItem partyDeleter;

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
        Log.i("Android.menu", "Here is the partyDeleter: " + partyDeleter.getTitle().toString());
        partyDeleter.setTitle("WHAT???");

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
                                }
                            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
//            Toast.makeText(this,"here tis", Toast.LENGTH_LONG).show();
        }
        return true;
    }
}