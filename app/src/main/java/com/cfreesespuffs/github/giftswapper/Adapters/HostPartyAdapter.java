package com.cfreesespuffs.github.giftswapper.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amplifyframework.datastore.generated.model.GuestList;
import com.amplifyframework.datastore.generated.model.User;
import com.cfreesespuffs.github.giftswapper.R;

import java.util.ArrayList;

public class HostPartyAdapter extends RecyclerView.Adapter<HostPartyAdapter.GuestListViewHolder> {
    public ArrayList<User> guestList;
    public GuestListListener listener;

    public HostPartyAdapter(ArrayList<User> guestList, GuestListListener listener){
        this.guestList = guestList;
        this.listener = listener;
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
        holder.user = guestList.get(position);
        TextView usernameView = holder.itemView.findViewById(R.id.usernameFragment);
        usernameView.setText(holder.user.getUserName());
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setSelected(holder.usernameView.isSelected()); //TODO once in the schema this might need updated
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    usernameView.setSelected(true);
                } else {
                    usernameView.setSelected(false);
                }
            }
        });
        holder.checkBox.setChecked(usernameView.isSelected());
    }

    @Override
    public int getItemCount() {
        if(guestList == null){
            return 0;
        }
        return guestList.size();
    }

    public static interface GuestListListener{
        public void listener(User user);
    }

    public static class GuestListViewHolder extends RecyclerView.ViewHolder{
        public User user;
        public View usernameView;
        public CheckBox checkBox;

        public GuestListViewHolder(@NonNull View usernameView){
            super(usernameView);
            this.usernameView = usernameView;
            this.checkBox = (CheckBox) usernameView.findViewById(R.id.rsvpCheckBox);
        }
    }

}
