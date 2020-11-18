package com.cfreesespuffs.github.giftswapper.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.amplifyframework.datastore.generated.model.InviteStatus;
import com.cfreesespuffs.github.giftswapper.R;

import java.util.ArrayList;

public class ViewAdapter extends RecyclerView.Adapter<ViewAdapter.AdapterViewHolder> {
    public ArrayList<InviteStatus> listOfAttendees;
    public OnInteractWithTaskListener listener;

    public ViewAdapter(ArrayList<InviteStatus> listOfAttendees, OnInteractWithTaskListener listener) {
        this.listOfAttendees = listOfAttendees;
        this.listener = listener;
    }

    // view holder deals with the passing of data from java to the fragment (list item)
    public static class AdapterViewHolder extends RecyclerView.ViewHolder {
        public InviteStatus inviteStatus;
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
                System.out.println(viewHolder.inviteStatus);
                listener.taskListener(viewHolder.inviteStatus);
            }
        });

        return viewHolder;
    }

    public static interface OnInteractWithTaskListener {
        public void taskListener(InviteStatus inviteStatus);
    }


    @Override
    // This gets called when a fragment(list item) has a java class attached to it
    public void onBindViewHolder(@NonNull AdapterViewHolder holder, int position) {                  // position is the position in the array
        holder.inviteStatus = listOfAttendees.get(position);

        TextView userName = holder.itemView.findViewById(R.id.guestName);
        TextView status = holder.itemView.findViewById(R.id.status);

        userName.setText(holder.inviteStatus.getName().getUserName());
        status.setText(holder.inviteStatus.getStatus());
    }

    @Override
    // This gets called so it knows how many fragments (list item) to put on the screen at once
    public int getItemCount() {
        if(listOfAttendees == null){
            return 0;
        }
        return listOfAttendees.size();
    }
}

