package com.cfreesespuffs.github.giftswapper.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amplifyframework.datastore.generated.model.Party;
import com.amplifyframework.datastore.generated.model.User;
import com.cfreesespuffs.github.giftswapper.R;

import java.util.ArrayList;

public class InvitationAdapter extends RecyclerView.Adapter<InvitationAdapter.AdapterViewHolder> { // Todo: move all commented out code to ViewAdapter.java
    public ArrayList<Party> partyResults;
    public OnInteractWithTaskListener listener;
//    public ArrayList<User> toRemove;

    public InvitationAdapter(ArrayList<Party> partyResults, OnInteractWithTaskListener listener) {
        this.partyResults = partyResults;
        this.listener = listener;
    }

    // view holder deals with the passing of data from java to the fragment (list item)
    public static class AdapterViewHolder extends RecyclerView.ViewHolder {
        public Party party;
//        public View itemView;
//        public CheckBox checkBox;

        public AdapterViewHolder(@NonNull View itemView) {
            super(itemView);
//            this.itemView = itemView;
//            this.checkBox = (CheckBox) itemView.findViewById(R.id.deletePartycB);
        }
    }

    @NonNull
    @Override
    public AdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_invitation, parent, false);

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

        TextView partyGoer = holder.itemView.findViewById(R.id.partyName);
        partyGoer.setText(holder.party.getTitle());

//        holder.checkBox.setOnCheckedChangeListener(null);
//        holder.checkBox.setSelected(holder.itemView.isSelected());

//        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if(isChecked){
//                    partyGoer.setSelected(true);
//                    Log.i("Android.invitedAfter", "Selection" + partyResults.get(position) + "add position " + position);
//                    toRemove.add(partyResults.get(1);
//                }
//            }
//        });


    }

//        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                if(b){
//                    usernameView.setSelected(true);
//                    Log.i("Android.hostAdapter", "This has been selected " + guestList.get(position) + "add position " + position);
//                    usersToAdd.add(guestList.get(position));
//                } else {
//                    usernameView.setSelected(false);
//                    usersToAdd.remove(guestList.get(position));
//                }
//            }
//        });
//        holder.checkBox.setChecked(usernameView.isSelected());
//    }


    public static interface OnInteractWithTaskListener {
        public void taskListener(Party party);
    }


    @Override
    // This gets called so it knows how many fragments (list item) to put on the screen at once
    public int getItemCount() {
        if(partyResults == null){
            return 0;
        }
        return partyResults.size();
    }
}
