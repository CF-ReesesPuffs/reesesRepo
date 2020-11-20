package com.cfreesespuffs.github.giftswapper.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amplifyframework.datastore.generated.model.GuestList;
import com.cfreesespuffs.github.giftswapper.R;

import java.util.ArrayList;
import java.util.List;

public class ViewAdapter extends RecyclerView.Adapter<ViewAdapter.AdapterViewHolder> {
    public List<GuestList> getTheStatus;
    public OnInteractWithTaskListener listener;

    public ViewAdapter(List<GuestList> getTheStatus, OnInteractWithTaskListener listener) {
        this.getTheStatus = getTheStatus;
        this.listener = listener;
    }

    public static class AdapterViewHolder extends RecyclerView.ViewHolder {
        public GuestList guestList;
        public View itemView;


        public AdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
        }
    }

    @NonNull
    @Override
    public AdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_pending, parent, false);


        final AdapterViewHolder viewHolder = new AdapterViewHolder(view);


//        view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                System.out.println(viewHolder.getTheStatus);
//                listener.taskListener(viewHolder.getTheStatus.getInviteStatus());
//            }
//        });

        return viewHolder;
    }

    public static interface OnInteractWithTaskListener {
        public void listener(GuestList guestList);
    }


    @Override
    // This gets called when a fragment(list item) has a java class attached to it
    public void onBindViewHolder(@NonNull AdapterViewHolder holder, int position) {                  // position is the position in the array
        holder.guestList = getTheStatus.get(position);

        TextView userName = holder.itemView.findViewById(R.id.guestName);
        TextView status = holder.itemView.findViewById(R.id.status);

        userName.setText(holder.guestList.getUser().getUserName());
        status.setText(holder.guestList.getInviteStatus());
    }

    @Override
    // This gets called so it knows how many fragments (list item) to put on the screen at once
    public int getItemCount() {
        if(getTheStatus == null){
            return 0;
        }
        return getTheStatus.size();
    }
}

