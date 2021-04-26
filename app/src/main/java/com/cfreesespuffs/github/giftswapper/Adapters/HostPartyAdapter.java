package com.cfreesespuffs.github.giftswapper.Adapters;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cfreesespuffs.github.giftswapper.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HostPartyAdapter extends RecyclerView.Adapter<HostPartyAdapter.GuestListViewHolder> {
    public ArrayList<String> guestList;
    public GuestListListener listener;
    public Set<String> usersToAdd = new HashSet<>();

    public HostPartyAdapter(ArrayList<String> guestList, GuestListListener listener){
        this.guestList = guestList;
        this.listener = listener;
        Log.e("HPA.GuestList", "messageGL: " + guestList);
    }

    @NonNull
    @Override
    public HostPartyAdapter.GuestListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_guest_list, parent, false);
        HostPartyAdapter.GuestListViewHolder guestListViewHolder = new HostPartyAdapter.GuestListViewHolder(view);

        view.setOnClickListener((newView) -> {
            listener.listener(guestListViewHolder.user);
        });
        return guestListViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull HostPartyAdapter.GuestListViewHolder holder, int position) {
        Log.e("HPA.bind", "messageGL: " + guestList);
        holder.user = guestList.get(position);
        TextView usernameView = holder.itemView.findViewById(R.id.usernameFragment);
        usernameView.setText(holder.user);
        usernameView.setTextColor(Color.parseColor("#000000"));

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setSelected(holder.usernameView.isSelected());
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    usernameView.setSelected(true);
                    usersToAdd.add(guestList.get(position));
                } else {
                    usernameView.setSelected(false);
                    usersToAdd.remove(guestList.get(position));
                }
            }
        });
        holder.checkBox.setChecked(usernameView.isSelected());
    }

    @Override
    public int getItemCount() {
        Log.e("HPA.itemCount", "messageGL: " + guestList);
        if(guestList == null){
            return 0;
        }
        return guestList.size();
    }

    public interface GuestListListener{
        void listener(String user);
    }

    public static class GuestListViewHolder extends RecyclerView.ViewHolder{
        public String user;
        public View usernameView;
        public CheckBox checkBox;
        public GuestListViewHolder(@NonNull View usernameView){
            super(usernameView);
        Log.e("HPA.viewHolder", "where am i");
            this.usernameView = usernameView;
            this.checkBox = usernameView.findViewById(R.id.rsvpCheckBox);
        }
    }

}
