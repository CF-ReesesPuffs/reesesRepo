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
import android.view.MenuItem;
import android.widget.TextView;

import com.amplifyframework.datastore.generated.model.GuestList;
import com.amplifyframework.datastore.generated.model.InviteStatus;
import com.cfreesespuffs.github.giftswapper.Activities.MainActivity;

import java.util.ArrayList;

public class PendingPage extends AppCompatActivity {

    RecyclerView recyclerView;
    Handler handler;
    Handler handleSingleItem;
    ArrayList<InviteStatus> inviteStatusList;

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
                        recyclerView.getAdapter().notifyItemInserted(inviteStatusList.size() - 1);
                        return false;
                    }
                });

        Intent intent = getIntent();

        TextView partyName = PendingPage.this.findViewById(R.id.partyName);
        partyName.setText(intent.getExtras().getString("partyName"));

        TextView host = PendingPage.this.findViewById(R.id.hostUser);
        host.setText(intent.getExtras().getString("host"));

        TextView when = PendingPage.this.findViewById(R.id.startDate);
        when.setText(intent.getExtras().getString("when"));

        TextView setTime = PendingPage.this.findViewById(R.id.startTime);
        setTime.setText(intent.getExtras().getString("setTime"));

        TextView budget = PendingPage.this.findViewById(R.id.priceLimit);
        budget.setText(intent.getExtras().getString("budget"));

    }
    private void connectAdapterToRecycler() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ViewAdapter(inviteStatusList, (ViewAdapter.OnInteractWithTaskListener) this));
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(PendingPage.this, MainActivity.class);
        PendingPage.this.startActivity(intent);
        return true;
    }
}