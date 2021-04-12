package com.cfreesespuffs.github.giftswapper.Adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amplifyframework.datastore.generated.model.FriendList;
import com.amplifyframework.datastore.generated.model.User;
import com.cfreesespuffs.github.giftswapper.R;

import java.util.ArrayList;

public class FriendPageAdapter extends RecyclerView.Adapter<FriendPageAdapter.FriendPageViewHolder> {

    public ArrayList<FriendList> friendArrayList;
    public FriendPageListener listener;

    public FriendPageAdapter(ArrayList<FriendList> friendArrayList, FriendPageListener listener) {
        this.friendArrayList = friendArrayList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FriendPageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_friend_page, parent, false);
        FriendPageAdapter.FriendPageViewHolder friendPageViewHolder = new FriendPageAdapter.FriendPageViewHolder(view);

        return friendPageViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FriendPageViewHolder holder, int position) {
        holder.friendList = friendArrayList.get(position);

        TextView friendPageTv = holder.itemView.findViewById(R.id.friendListName);
        friendPageTv.setText((holder.friendList.getUserName()));
        friendPageTv.setTextColor(Color.parseColor("#000000"));
    }

    @Override
    public int getItemCount() {
        if (friendArrayList == null) return 0;
        return friendArrayList.size();
    }

    public interface FriendPageListener {
        void listener(FriendList friendList);
    }

    public static class FriendPageViewHolder extends RecyclerView.ViewHolder {
        public FriendList friendList;
        public View friendPageView;

        public FriendPageViewHolder(@NonNull View friendPageView) {
            super(friendPageView);
            this.friendPageView = friendPageView;
        }
    }

}
