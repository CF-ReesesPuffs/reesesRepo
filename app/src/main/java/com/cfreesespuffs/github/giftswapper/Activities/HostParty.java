package com.cfreesespuffs.github.giftswapper.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

//import android.app.AlertDialog;
import android.app.ActionBar;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
//import android.content.DialogInterface;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import android.text.InputType;
import android.preference.PreferenceManager;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.Toolbar;

import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.auth.AuthUser;
import com.amplifyframework.core.Amplify;

import com.amplifyframework.core.model.temporal.Temporal;
import com.amplifyframework.core.reachability.Host;
import com.amplifyframework.datastore.generated.model.GuestList;

import com.amplifyframework.datastore.generated.model.Party;
import com.amplifyframework.datastore.generated.model.User;

import com.cfreesespuffs.github.giftswapper.Adapters.HostPartyAdapter;
import com.cfreesespuffs.github.giftswapper.PendingPage;
import com.cfreesespuffs.github.giftswapper.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;

public class HostParty extends AppCompatActivity implements HostPartyAdapter.GuestListListener, DatePickerDialog.OnDateSetListener {

    ArrayList<User> guestList = new ArrayList<>();
    Handler handler, generalHandler;
    RecyclerView recyclerView;
    HashMap<String, User> uniqueGuestList = new HashMap<>();
    User currentUser;
    Calendar date; // there are 2 potential calendar options
    TextView foundGuest;
    TextView partyDate;
    Spinner selectedPriceSpinner;
    Spinner stealLimitSpinner;
    boolean spinnerFlag = false;
    SharedPreferences preferences;
    private AdView hPAdView;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_party);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.green)));

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);  // https://www.geeksforgeeks.org/how-to-change-the-color-of-status-bar-in-an-android-app/
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.green));

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        MobileAds.initialize(this);
        hPAdView = findViewById(R.id.gsAdview);
        AdRequest adRequest = new AdRequest.Builder().build();
        hPAdView.loadAd(adRequest);

        priceSpinner();
        stealLimitSpinner();
        selectedPriceSpinner = findViewById(R.id.price_spinner);
        stealLimitSpinner = findViewById(R.id.stealLimit_spinner);

        AuthUser authUser = Amplify.Auth.getCurrentUser();

        Amplify.API.query(
                ModelQuery.get(User.class, preferences.getString("userId", "NA")),
                response -> currentUser = response.getData(),
                error -> Log.e("Amplify.user", "error: " + error)
        );

        generalHandler = new Handler(Looper.getMainLooper(), message -> {
            if (message.arg1 == 1) {
                Toast.makeText(this, "Add more party goers!", Toast.LENGTH_LONG).show();
            }
            if (message.arg1 == 2) {         // https://www.javaprogramto.com/2020/08/how-to-convert-calendar-to-localdatetime-in-java-8.html
                Date dateFormat = date.getTime(); // https://www.candidjava.com/tutorial/java-program-to-convert-calendar-to-date-and-date-to-calendar/#:~:text=Calendar%20object%20to%20Date%20object%2C%20Using%20Calendar.getInstance%20%28%29,object%20to%20Calendar%20object%2C%20Date%20d%3Dnew%20Date%20%281515660075000l%29%3B
                SimpleDateFormat format = new SimpleDateFormat("MMMM dd, yyyy  hh:mm a");
                String prettyDate = format.format(dateFormat);
                partyDate.setText(prettyDate);
                selectedPriceSpinner.setFocusableInTouchMode(true);
                selectedPriceSpinner.requestFocus();
            }
            if (message.arg1 == 3) {
                Toast.makeText(this, "You forgot to include a date and time.", Toast.LENGTH_LONG).show();
            }
            if (message.arg1 == 4) {
                Toast.makeText(this, "You need to include a name for the party.", Toast.LENGTH_LONG).show();
            }

            if (message.arg1 == 5) {
                Log.e("Handler.5", "What is flag? " + spinnerFlag);

                if (spinnerFlag) {
                    stealLimitSpinner.setFocusableInTouchMode(true);
                    stealLimitSpinner.requestFocus();
                }
                spinnerFlag = true; // makes the spinner open up correctly but only intermittently.
            }

            if (message.arg1 == 10) {
                Toast.makeText(this, "We're sorry, we only support up to 10 guest right now. :(", Toast.LENGTH_LONG).show();
            }

            return false;
        });

        handler = new Handler(Looper.getMainLooper(), message -> {
            Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged();
            return true;
        });

        partyDate = findViewById(R.id.editTextDate);
        partyDate.setTextColor(Color.parseColor("#000000"));

        foundGuest = findViewById(R.id.userFindGuestSearch);
        foundGuest.setTextColor(Color.parseColor("#000000"));

        Button findGuestButton = findViewById(R.id.findGuest_button);
        findGuestButton.setBackgroundColor(getResources().getColor(R.color.green));

        findGuestButton.setOnClickListener((view) -> { // https://stackoverflow.com/questions/9596010/android-use-done-button-on-keyboard-to-click-button

            String guestLc = foundGuest.getText().toString().toLowerCase();

            Amplify.API.query(
                    ModelQuery.list(User.class, User.SEARCH_NAME.beginsWith(guestLc)),
                    response -> {
                        for (User user : response.getData()) {
                            if (!uniqueGuestList.containsKey(user.getUserName())) {
                                uniqueGuestList.put(user.getUserName(), user);
                                guestList.add(user);
                            }
                            handler.sendEmptyMessage(1);
                        }
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
                    },
                    error -> Log.e("Amplify", "failed to find user")
            );
        });

        selectedPriceSpinner.setOnFocusChangeListener((v, hasFocus) -> { // https://stackoverflow.com/questions/23075561/set-focus-on-spinner-when-selected-in-android
            if (hasFocus) {
                if (selectedPriceSpinner.getWindowToken() != null) {
                    selectedPriceSpinner.performClick();
                }
            }
        });

        selectedPriceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { // https://stackoverflow.com/questions/1337424/android-spinner-get-the-selected-item-change-event
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Message message = new Message();
                message.arg1 = 5;
                generalHandler.sendMessage(message);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        stealLimitSpinner.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (stealLimitSpinner.getWindowToken() != null) {
                    stealLimitSpinner.performClick();
                }
            }
        });

        partyDate.setOnFocusChangeListener((v, hasFocus) -> { // https://stackoverflow.com/questions/4165414/how-to-hide-soft-keyboard-on-android-after-clicking-outside-edittext
            if (hasFocus) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                showDateTimePicker();
            }
        });

        partyDate.setOnClickListener((view) -> {
            if (view != null) { // https://medium.com/cs-random-thoughts-on-tech/android-force-hide-system-keyboard-while-retaining-edittexts-focus-9d3fd8dbed32
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            showDateTimePicker();
        });

        foundGuest.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                Amplify.API.query(
                        ModelQuery.list(User.class, User.USER_NAME.beginsWith(foundGuest.getText().toString())),
                        response -> {
                            for (User user : response.getData()) {
                                if (!uniqueGuestList.containsKey(user.getUserName())) {
                                    uniqueGuestList.put(user.getUserName(), user);
                                    guestList.add(user);
                                }
                                handler.sendEmptyMessage(1);
                            }
                        },
                        error -> Log.e("Amplify", "failed to find user")
                );
            }
            return false; // false hides keyboard. true leaves it up.
        });

        recyclerView = findViewById(R.id.guestSearchRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new HostPartyAdapter(guestList, this));

        Button addParty = HostParty.this.findViewById(R.id.button_createParty);
        addParty.setBackgroundColor(getResources().getColor(R.color.green));

        addParty.setOnClickListener(view -> {

            TextView partyName = findViewById(R.id.textViewPartyName);
            Set guestsToInvite = ((HostPartyAdapter) recyclerView.getAdapter()).usersToAdd;
            List<User> guestsToInviteList = new ArrayList();
            guestsToInviteList.addAll(guestsToInvite);

            boolean flag = false;
            for (User guest : guestsToInviteList) { // todo: convert to hashmap, this issue will be resolved and could be refactored away.
                if (guest.getUserName().equalsIgnoreCase(authUser.getUsername())) flag = true;
            }
            if (!flag) guestsToInviteList.add(currentUser);

            if (guestsToInviteList.size() < 2) { // party can't be created with only one participant (only the host, really)
                Message noGuestsMsg = new Message();
                noGuestsMsg.arg1 = 1;
                generalHandler.sendMessage(noGuestsMsg);
                return;
            }

            if (guestsToInviteList.size() > 10) {
                Message message = new Message();
                message.arg1 = 10;
                generalHandler.sendMessage(message);
                return;
            }

            String nameOfParty = partyName.getText().toString();
            String priceOfParty = selectedPriceSpinner.getSelectedItem().toString();

            if (nameOfParty.equals("")) {
                Message message = new Message();
                message.arg1 = 4;
                generalHandler.sendMessage(message);
                return;
            }

            if (date == null) {
                Message message = new Message();
                message.arg1 = 3;
                generalHandler.sendMessage(message);
                return;
            }

            //David's find https://github.com/aws-amplify/amplify-android/issues/590
            Date dateFormat = date.getTime(); // https://www.candidjava.com/tutorial/java-program-to-convert-calendar-to-date-and-date-to-calendar/#:~:text=Calendar%20object%20to%20Date%20object%2C%20Using%20Calendar.getInstance%20%28%29,object%20to%20Calendar%20object%2C%20Date%20d%3Dnew%20Date%20%281515660075000l%29%3B

            SimpleDateFormat formatTime = new SimpleDateFormat("hh:mm a");
            String prettyTime = formatTime.format(dateFormat);

            SimpleDateFormat formatDate = new SimpleDateFormat("MMMM dd, yyyy");
            String prettyDate = formatDate.format(dateFormat);

            SimpleDateFormat sdf;
            sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            String text = sdf.format(dateFormat);

            int stealLimitNumber = (int) stealLimitSpinner.getSelectedItem();

            Party party;
            party = Party.builder()
                    .title(nameOfParty)
                    .hostedAt(prettyTime)
                    .hostedOn(prettyDate)
                    .partyDate(text)
                    .price(priceOfParty)
                    .theHost(currentUser)
                    .isReady(false)
                    .isFinished(false)
                    .stealLimit(stealLimitNumber)
                    .lastGiftStolen("")
                    .build();

            Amplify.API.mutate(
                    ModelMutation.create(party),
                    response -> {
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
        });
    }

    public void priceSpinner() {
        String[] pricePoints = {"$0 - $10", "$11 - $20", "$21 - $30", "$31 - $40"}; // todo: change this to a "CHOOSE ONE" option as the first option, but then will need to write a validation check so that "CHOOSE ONE" doesn't become a price range option.
        Spinner spinner = findViewById(R.id.price_spinner);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this, R.layout.black_bg_spinner_item, pricePoints);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public void stealLimitSpinner() {
        Integer[] stealLimitOptions = {1, 2, 3, 4, 5, 6};
        Spinner spinner = findViewById(R.id.stealLimit_spinner);
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, R.layout.black_bg_spinner_item, stealLimitOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public void showDateTimePicker() { // https://stackoverflow.com/questions/2055509/how-to-create-a-date-and-time-picker-in-android
        Calendar currentDate = Calendar.getInstance();
        date = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, monthOfYear, dayOfMonth) -> {
            date.set(year, monthOfYear, dayOfMonth);
            new TimePickerDialog(HostParty.this, (view1, hourOfDay, minute) -> {
                date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                date.set(Calendar.MINUTE, minute);
                Message dateMessage = new Message();
                dateMessage.arg1 = 2;
                generalHandler.sendMessage(dateMessage);
            }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false).show();
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
    }

    @Override
    public void listener(User user) {
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
    }
}