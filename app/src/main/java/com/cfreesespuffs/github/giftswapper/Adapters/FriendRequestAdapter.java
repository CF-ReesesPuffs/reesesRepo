package com.cfreesespuffs.github.giftswapper.Adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amplifyframework.datastore.generated.model.User;
import com.cfreesespuffs.github.giftswapper.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.FriendListViewHolder> {

    public ArrayList<User> friendList;
    public FriendListListener listener;
    public Set<User> friendsToAdd = new HashSet<>();

    public FriendRequestAdapter(ArrayList<User> friendList, FriendListListener listener) {
        this.friendList = friendList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FriendListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_friend_list, parent, false);
        FriendRequestAdapter.FriendListViewHolder friendListViewHolder = new FriendRequestAdapter.FriendListViewHolder(view);

        view.setOnClickListener((newView) -> {
            listener.listener(friendListViewHolder.user);
        });
        return friendListViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FriendListViewHolder holder, int position) {
        holder.user = friendList.get(position);

        TextView friendTv = holder.itemView.findViewById(R.id.friendName); // todo: create it in layout!
        friendTv.setText(holder.user.getUserName());
        friendTv.setTextColor(Color.parseColor("#000000"));

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setSelected(holder.friendView.isSelected());
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    friendTv.setSelected(true);
                    friendsToAdd.add(friendList.get(position));
                } else {
                    friendTv.setSelected(false);
                    friendsToAdd.remove(friendList.get(position));
                }
            }
        });
        holder.checkBox.setChecked(friendTv.isSelected());
    }

    @Override
    public int getItemCount() {
        if (friendList == null) {
            return 0;
        }
        return friendList.size();
    }

    public interface FriendListListener {
        void listener(User user);
    }

    public static class FriendListViewHolder extends RecyclerView.ViewHolder {
        public User user;
        public View friendView;
        public CheckBox checkBox;

        public FriendListViewHolder(@NonNull View friendView) {
            super(friendView);
            this.friendView = friendView;
            this.checkBox = friendView.findViewById(R.id.friendCheckBox);
        }
    }

}
