package com.cfreesespuffs.github.giftswapper;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.auth.AuthUser;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.GuestList;
import com.amplifyframework.datastore.generated.model.Party;
import com.amplifyframework.datastore.generated.model.User;
import com.cfreesespuffs.github.giftswapper.Activities.InvitationDetails;
import com.cfreesespuffs.github.giftswapper.Adapters.PartyAdapter;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class InvitationList extends AppCompatActivity implements PartyAdapter.InteractWithPartyListener {

    RecyclerView recyclerView;
    public ArrayList<Party> parties = new ArrayList<>();
    Handler handleParties;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation_list);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.green)));
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);  // https://www.geeksforgeeks.org/how-to-change-the-color-of-status-bar-in-an-android-app/
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.green));

        handleParties = new Handler(Looper.getMainLooper(), msg -> {
            recyclerView.getAdapter().notifyItemInserted(parties.size());
            return false;
        });
        connectAdapterToRecycler();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        Amplify.API.query(
                ModelQuery.get(User.class, preferences.getString("userId", "NA")),
                response2 -> {
                    Log.e("userID", "here is userID: " + preferences.getString("userId", "NA"));
                    if (response2.getData().getParties().isEmpty()) return;
                    for (GuestList party : response2.getData().getParties()) {
                        if (party.getInviteStatus().equals("Pending") && !party.getParty().getIsReady()) {
                            parties.add(party.getParty());
                        }
                    }
                    handleParties.sendEmptyMessage(1);
                },
                error -> Log.e("Amplify", "Failed to retrieve store")
        );
    }

    private void connectAdapterToRecycler() {
        recyclerView = findViewById(R.id.postPartyRV);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new PartyAdapter(parties, this));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void listener(Party party) {
        Intent intent = new Intent(InvitationList.this, InvitationDetails.class);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("PST"));

        intent.putExtra("partyName", party.getTitle());
        intent.putExtra("when", party.getHostedOn());
        intent.putExtra("setTime", party.getHostedAt());
        intent.putExtra("budget", party.getPrice());
        intent.putExtra("partyId", party.getId());
        this.startActivity(intent);
    }
}