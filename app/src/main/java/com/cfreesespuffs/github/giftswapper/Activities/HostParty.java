package com.cfreesespuffs.github.giftswapper.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.auth.AuthUser;
import com.amplifyframework.core.Amplify;

import com.amplifyframework.datastore.generated.model.GuestList;

import com.amplifyframework.datastore.generated.model.Party;
import com.amplifyframework.datastore.generated.model.User;

import com.cfreesespuffs.github.giftswapper.Adapters.HostPartyAdapter;
import com.cfreesespuffs.github.giftswapper.PendingPage;
import com.cfreesespuffs.github.giftswapper.R;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

public class HostParty extends AppCompatActivity implements HostPartyAdapter.GuestListListener, DatePickerDialog.OnDateSetListener {

    public ArrayList<User> guestList = new ArrayList<>();
    public HashSet<Integer> invitedGuestList;
    Handler handler;
    Handler generalHandler;
    RecyclerView recyclerView;
    HashMap<String, User> uniqueGuestList = new HashMap<>();
    User currentUser;
    Calendar date; // there are 2 potential calendar options
    TextView partyDate, partyTime;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_party);

        priceSpinner();
        stealLimitSpinner();

        AuthUser authUser = Amplify.Auth.getCurrentUser();
        Amplify.API.query(
                ModelQuery.list(User.class),
                response -> {
                    for (User user : response.getData()) {
                        if (user.getUserName().equalsIgnoreCase(authUser.getUsername())) {
                            currentUser = user;
                            System.out.println("This is our current user/host" + currentUser);
                        }
                    }
                },
                error -> Log.e("Amplify.user", "error: " + error)
        );

        // https://www.javaprogramto.com/2020/08/how-to-convert-calendar-to-localdatetime-in-java-8.html

        generalHandler = new Handler(Looper.getMainLooper(), message -> {
            if (message.arg1 == 1) {
                Toast.makeText(this, "Add more party goers!", Toast.LENGTH_LONG).show();
            }
            if (message.arg1 == 2) {
//                TimeZone tz = date.getTimeZone();
//                ZoneId zoneId = tz.toZoneId();
//                LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), zoneId);
//                String dateStr = Integer.toString(date.get(Calendar.DATE));
                Date dateFormat = date.getTime(); // https://www.candidjava.com/tutorial/java-program-to-convert-calendar-to-date-and-date-to-calendar/#:~:text=Calendar%20object%20to%20Date%20object%2C%20Using%20Calendar.getInstance%20%28%29,object%20to%20Calendar%20object%2C%20Date%20d%3Dnew%20Date%20%281515660075000l%29%3B
                SimpleDateFormat format = new SimpleDateFormat("MMMM dd, yyyy  hh:mm a");
                String prettyDate = format.format(dateFormat);
                partyDate.setText(prettyDate);
                //partyDate.setText(date.get(Calendar.MONTH) + "/" + dateStr + "/" + date.get(Calendar.YEAR));
              //  partyTime.setText(date.get(Calendar.HOUR) + ":" + date.get(Calendar.MINUTE));
            }
            return false;
        });

        handler = new Handler(Looper.getMainLooper(),
                new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message message) {
                        recyclerView.getAdapter().notifyDataSetChanged();
                        return true;
                    }
                });

        handler.sendEmptyMessage(1);

        partyDate = findViewById(R.id.editTextDate);
       // partyTime = findViewById(R.id.editTextTime);

        Button dateSelector = findViewById(R.id.dateSelect);
        dateSelector.setOnClickListener((view) -> {
            showDateTimePicker();
        });

        Button findGuestButton = findViewById(R.id.findGuest_button);
        findGuestButton.setOnClickListener((view) -> {

            //        https://stackoverflow.com/questions/9596010/android-use-done-button-on-keyboard-to-click-button

            Amplify.API.query(
                    ModelQuery.list(User.class),
                    response -> {
                        for (User user : response.getData()) {
                            TextView foundGuest = findViewById(R.id.userFindGuestSearch);
                            String foundGuestString = foundGuest.getText().toString();
                            if (user.getUserName().toLowerCase().contains(foundGuestString.toLowerCase())) {
                                if (!uniqueGuestList.containsKey(user.getUserName())) {
                                    uniqueGuestList.put(user.getUserName(), user);
                                    guestList.add(user);
                                }
                                System.out.println("guestList Update from button");
                                handler.sendEmptyMessage(1);
                            }
                        }

                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);

                    },
                    error -> Log.e("Amplify", "failed to find user")
            );

        });

        TextView foundGuest = findViewById(R.id.userFindGuestSearch);
        foundGuest.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    Log.e("Android.KeyPress", "ENTER/DONE been HIT");

                    Amplify.API.query(
                            ModelQuery.list(User.class),
                            response -> {
                                for (User user : response.getData()) {
                                    TextView foundGuest = findViewById(R.id.userFindGuestSearch);
                                    String foundGuestString = foundGuest.getText().toString();
                                    if (user.getUserName().toLowerCase().contains(foundGuestString.toLowerCase())) {
                                        if (!uniqueGuestList.containsKey(user.getUserName())) {
                                            uniqueGuestList.put(user.getUserName(), user);
                                            guestList.add(user);
                                        }
                                    }
                                    System.out.println("guestList Update from keyboard");
                                    handler.sendEmptyMessage(1);
                                }
                            },
                            error -> Log.e("Amplify", "failed to find user")
                    );
                }
                return false; // false hides keyboard. true leaves it up.
            }
        });

        recyclerView = findViewById(R.id.guestSearchRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new HostPartyAdapter(guestList, this));

        Button addParty = HostParty.this.findViewById(R.id.button_createParty);
        addParty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TextView partyName = findViewById(R.id.textViewPartyName);
                // partyDate = findViewById(R.id.editTextDate);
                // partyTime = findViewById(R.id.editTextTime);
                Spinner selectedPriceSpinner = findViewById(R.id.price_spinner);
                Log.i("Android.usersToAdd", ((HostPartyAdapter) recyclerView.getAdapter()).usersToAdd.toString());

                Set guestsToInvite = ((HostPartyAdapter) recyclerView.getAdapter()).usersToAdd;

                List<User> guestsToInviteList = new ArrayList();

                guestsToInviteList.addAll(guestsToInvite);

                System.out.println(guestsToInviteList.toString());

                boolean flag = false;
                for (User guest : guestsToInviteList) {
                    if (guest.getUserName().equalsIgnoreCase(authUser.getUsername())) flag = true;
                }
                if (!flag) guestsToInviteList.add(currentUser);

                if (guestsToInviteList.size() < 2) { // party can't be created with only one participant (only the host, really)
                    Message noGuestsMsg = new Message();
                    noGuestsMsg.arg1 = 1;
                    generalHandler.sendMessage(noGuestsMsg);
                    Log.e("guests.list", "this was hit");
                    return;
                }

                String nameOfParty = partyName.getText().toString();
               // String dateOfParty = partyDate.getText().toString();
              //  String timeOfParty = partyTime.getText().toString();
                String priceOfParty = selectedPriceSpinner.getSelectedItem().toString();

                Date dateFormat = date.getTime(); // https://www.candidjava.com/tutorial/java-program-to-convert-calendar-to-date-and-date-to-calendar/#:~:text=Calendar%20object%20to%20Date%20object%2C%20Using%20Calendar.getInstance%20%28%29,object%20to%20Calendar%20object%2C%20Date%20d%3Dnew%20Date%20%281515660075000l%29%3B

                SimpleDateFormat formatTime = new SimpleDateFormat("hh:mm a");
                String prettyTime = formatTime.format(dateFormat);

                SimpleDateFormat formatDate = new SimpleDateFormat("MMMM dd, yyyy");
                String prettyDate = formatDate.format(dateFormat);

                Party party;
                party = Party.builder()
                        .title(nameOfParty)
                        .hostedAt(prettyTime)
                        .hostedOn(prettyDate)
                        .price(priceOfParty)
                        .theHost(currentUser)
                        .isReady(false)
                        .isFinished(false)
                        .build();

                Amplify.API.mutate(
                        ModelMutation.create(party),
                        response -> {
                            Log.i("Amplify.API", "success party started");
                            Party party2 = response.getData();

                            for (User guest : guestsToInviteList) {
                                GuestList inviteStatus = GuestList.builder()
                                        .inviteStatus("Pending")
                                        .user(guest)
                                        .invitee(currentUser.getUserName())
                                        .invitedUser(guest.getUserName())
                                        .takenTurn(false)
                                        .party(party2)
                                        .turnOrder(0)
                                        .build();

                                Amplify.API.mutate(
                                        ModelMutation.create(inviteStatus),
                                        response2 -> Log.i("Amplify.API", "Users are now pending!!!"),
                                        error -> Log.e("Amplify/API", "Message failed " + error)
                                );
                            }

                            Intent intent = new Intent(HostParty.this, MainActivity.class);
                            intent.putExtra("title", party2.getTitle());
                            intent.putExtra("date", party2.getHostedOn());
                            intent.putExtra("time", party2.getHostedAt());
                            intent.putExtra("price", party2.getPrice());

                            intent.putExtra("id", party2.getId());
                            HostParty.this.startActivity(intent);
                        },
                        error -> Log.e("Amplify/API", "Message failed " + error)
                );

            }
        });
    }

    public void priceSpinner() {
        String[] pricePoints = {"$0- $10", "$11- $20", "$21- $30", "$31- $40"};
        Spinner spinner = (Spinner) findViewById(R.id.price_spinner);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, pricePoints);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public void stealLimitSpinner() {
        Integer[] stealLimitOptions = {1, 2, 3, 4, 5, 6};
        Spinner spinner = (Spinner) findViewById(R.id.stealLimit_spinner);
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stealLimitOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    // https://stackoverflow.com/questions/2055509/how-to-create-a-date-and-time-picker-in-android
    public void showDateTimePicker() {
        Calendar currentDate = Calendar.getInstance();
        date = Calendar.getInstance();
        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                date.set(year, monthOfYear, dayOfMonth);
                new TimePickerDialog(HostParty.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        date.set(Calendar.MINUTE, minute);
                        Log.v("Android.Date", "this is the date " + date.getTime());
                        Message dateMessage = new Message();
                        dateMessage.arg1 = 2;
                        generalHandler.sendMessage(dateMessage);
                    }
                }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false).show();
            }
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
    }

    @Override
    public void listener(User user) {

    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {

    }
}