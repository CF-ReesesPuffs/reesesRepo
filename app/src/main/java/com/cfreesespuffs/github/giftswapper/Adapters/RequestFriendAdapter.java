package com.cfreesespuffs.github.giftswapper.Adapters;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amplifyframework.datastore.generated.model.FriendList;
import com.cfreesespuffs.github.giftswapper.R;

import java.util.ArrayList;

public class RequestFriendAdapter extends RecyclerView.Adapter<RequestFriendAdapter.RequestFriendViewHolder> {

    public ArrayList<FriendList> friendRequestList;
    public RequestFriendListListener rfListener;

    public RequestFriendAdapter(ArrayList<FriendList> friendRequestList, RequestFriendListListener rfListener) {
        this.friendRequestList = friendRequestList;
        this.rfListener = rfListener;
    }

    @NonNull
    @Override
    public RequestFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_friend_request, parent, false);
        RequestFriendAdapter.RequestFriendViewHolder requestFriendViewHolder = new RequestFriendAdapter.RequestFriendViewHolder(view);

        view.setOnClickListener((newView) -> {
            rfListener.rfListener(requestFriendViewHolder.friendList);
        });

        return requestFriendViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RequestFriendViewHolder holder, int position) {
        holder.friendList = friendRequestList.get(position);
        Log.e("ViewH", "FriendList: " + friendRequestList.toString());

        TextView requestFriend = holder.itemView.findViewById(R.id.requestName);
        requestFriend.setText(holder.friendList.getUserName());
        requestFriend.setTextColor(Color.parseColor("#000000"));

    }

    @Override
    public int getItemCount() {
        if (friendRequestList == null) {
            return 0;
        }
        return friendRequestList.size();
    }

    public interface RequestFriendListListener {
        void rfListener(FriendList friendList);
    }

    public static class RequestFriendViewHolder extends RecyclerView.ViewHolder {
        public FriendList friendList;
        public View requestFriendView;

        public RequestFriendViewHolder(@NonNull View friendView) {
            super(friendView);
            this.requestFriendView = friendView;
        }
    }
}
