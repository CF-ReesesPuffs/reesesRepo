package com.cfreesespuffs.github.giftswapper.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amplifyframework.datastore.generated.model.Party;
import com.cfreesespuffs.github.giftswapper.R;

import java.util.ArrayList;

public class PostPartyViewAdapter extends RecyclerView.Adapter<PostPartyViewAdapter.AdapterViewHolder> {
    public ArrayList<Party> partyResults;
    public OnInteractWithTaskListener listener;

    public PostPartyViewAdapter(ArrayList<Party> partyResults, OnInteractWithTaskListener listener) {
        this.partyResults = partyResults;
        this.listener = listener;
    }

    // view holder deals with the passing of data from java to the fragment (list item)
    public static class AdapterViewHolder extends RecyclerView.ViewHolder {
        public Party party;
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
                .inflate(R.layout.fragment_party, parent, false);


        final AdapterViewHolder viewHolder = new AdapterViewHolder(view);


        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(viewHolder.party);
                listener.taskListener(viewHolder.party);
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterViewHolder holder, int position) {
        holder.party = partyResults.get(position);
        TextView userName = holder.itemView.findViewById(R.id.guestName);
        TextView gift = holder.itemView.findViewById(R.id.giftRecieved);

        userName.setText((CharSequence) holder.party.users);
        gift.setText((CharSequence) holder.party.gifts);
    }


    public static interface OnInteractWithTaskListener {
        public void taskListener(Party party);
    }


    @Override
    // This gets called so it knows how many fragments (list item) to put on the screen at once
    public int getItemCount() {
        return partyResults.size();
    }
}
