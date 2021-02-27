package com.cfreesespuffs.github.giftswapper.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amplifyframework.datastore.generated.model.Gift;
import com.amplifyframework.datastore.generated.model.GuestList;
import com.amplifyframework.datastore.generated.model.User;
import com.cfreesespuffs.github.giftswapper.R;

import java.util.ArrayList;

public class CurrentPartyUserAdapter extends RecyclerView.Adapter<CurrentPartyUserAdapter.AdapterViewHolder> {
    public ArrayList<GuestList> userActiveParty;
//    public ArrayList<Gift> giftsBrought;
    public OnInteractWithTaskListener listener;

//    User amplifyUser;
//    Gift giftUpdate;

    public CurrentPartyUserAdapter(ArrayList<GuestList> userActiveParty, OnInteractWithTaskListener listener) {
        this.userActiveParty = userActiveParty;
//        this.giftsBrought = giftsBrought;
        this.listener = listener;
    }

    // view holder deals with the passing of data from java to the fragment (list item)
    public static class AdapterViewHolder extends RecyclerView.ViewHolder {
        public GuestList user;
        public Gift gift;
        public View itemView;


        public AdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
        }
    }

    @NonNull
    @Override
    // This gets called when a fragment (list item) pops into existence
    public AdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //choose which fragment (list item) to build
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_current_party_user, parent, false);

        final AdapterViewHolder viewHolder = new AdapterViewHolder(view);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.taskListener(viewHolder.user.getUser().getUserName());
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterViewHolder holder, int position) {
        holder.user = userActiveParty.get(position);
        TextView userName = holder.itemView.findViewById(R.id.userNameOnly);
        TextView turnOrderTv = holder.itemView.findViewById(R.id.turnOrder);

        turnOrderTv.setText(holder.user.getTurnOrder().toString()); // might also be able to parseInt as string?
        userName.setText(holder.user.getUser().getUserName());
    }


    public static interface OnInteractWithTaskListener {
        public void taskListener(String party);
    }


    @Override
    // This gets called so it knows how many fragments (list item) to put on the screen at once
    public int getItemCount() {
        return userActiveParty.size();
    }
}
