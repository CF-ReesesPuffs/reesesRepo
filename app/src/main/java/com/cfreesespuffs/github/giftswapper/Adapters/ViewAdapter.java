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

import com.amplifyframework.datastore.generated.model.GuestList;
import com.cfreesespuffs.github.giftswapper.R;

import java.util.ArrayList;
import java.util.List;

public class ViewAdapter extends RecyclerView.Adapter<ViewAdapter.AdapterViewHolder> { // Todo: here for the checkbox/delete partygoer on pending page.
    public List<GuestList> guestList;
    public OnInteractWithTaskListener listener;
    public ArrayList<GuestList> toRemove = new ArrayList<>();

    public ViewAdapter(List<GuestList> guestList, OnInteractWithTaskListener listener) {
        this.guestList = guestList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_pending, parent, false);
        AdapterViewHolder viewHolder = new AdapterViewHolder(view);

        view.setOnClickListener((newView) -> {
            listener.listener(viewHolder.guestList);
        });

//        view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                listener.listener(viewHolder.guestList);
//            }
//        });

        return viewHolder;
    }

    @Override // This gets called when a fragment(list item) has a java class attached to it
    public void onBindViewHolder(@NonNull AdapterViewHolder holder, int position) { // position is the position in the array
        holder.guestList = guestList.get(position);
        System.out.println("this is guestlist: " + guestList);

        TextView userName = holder.itemView.findViewById(R.id.guestName);
        TextView status = holder.itemView.findViewById(R.id.status);

        userName.setText(holder.guestList.getUser().getUserName());
        status.setText(holder.guestList.getInviteStatus());

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setSelected(holder.checkBox.isSelected()); // this line diverges from HostPartyAdapter template
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    userName.setSelected(true);
                    Log.i("Android.viewAdapter", "This has been selected. " + guestList.get(position));
                    toRemove.add(guestList.get(position));
                } else {
                    userName.setSelected(false);
                    Log.i("Android.viewAdapter", "This has been DESELECTED. " + guestList.get(position));
                    toRemove.remove(guestList.get(position));
                }
            }
        });
        holder.checkBox.setChecked(userName.isSelected());
    }

    @Override // This gets called so it knows how many fragments (list item) to put on the screen at once
    public int getItemCount() {
        if(guestList == null){
            return 0;
        }
        return guestList.size();
    }

    public static interface OnInteractWithTaskListener {
        public void listener(GuestList guestList);
    }

    public static class AdapterViewHolder extends RecyclerView.ViewHolder {
        public GuestList guestList;
        public View itemView;
        public CheckBox checkBox;

        public AdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.checkBox = (CheckBox) itemView.findViewById(R.id.deletePartycB);
        }
    }
}
