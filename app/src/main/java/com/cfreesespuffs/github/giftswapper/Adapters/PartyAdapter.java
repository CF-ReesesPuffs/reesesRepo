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

public class PartyAdapter extends RecyclerView.Adapter<PartyAdapter.PartyViewHolder> {
    public ArrayList<Party> parties;
    public InteractWithPartyListener listener;

    public PartyAdapter(ArrayList<Party> parties, InteractWithPartyListener listener) {
        this.parties = parties;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PartyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_party,parent,false);
        PartyViewHolder partyViewHolder = new PartyViewHolder(view);

        view.setOnClickListener((newView)-> {
            listener.listener(partyViewHolder.party);
        });
        return partyViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PartyViewHolder holder, int position) {
        holder.party= parties.get(position);
        TextView titleView = holder.itemView.findViewById(R.id.party_title);
        TextView priceView = holder.itemView.findViewById(R.id.party_price);

        titleView.setText(holder.party.getTitle());
        priceView.setText(holder.party.getPrice());

    }

    @Override
    public int getItemCount() {
        if(parties == null){
            return 0;
        }
        return parties.size();
    }

    public static interface InteractWithPartyListener{
        public void listener(Party party);
    }
    public static class PartyViewHolder extends RecyclerView.ViewHolder{
        public Party party;
        public View itemView;

        public PartyViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
        }
    }
}
