package com.cfreesespuffs.github.giftswapper.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amplifyframework.datastore.generated.model.User;
import com.cfreesespuffs.github.giftswapper.R;

import java.util.ArrayList;

public class RequestFriendAdapter extends RecyclerView.Adapter<RequestFriendAdapter.RequestFriendViewHolder> {

    public ArrayList<User> friendRequestList;
    public RequestFriendListListener rfListener;

    public RequestFriendAdapter(ArrayList<User> friendRequestList, RequestFriendListListener rfListener) {
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
            rfListener.rfListener(requestFriendViewHolder.user);
        });

        return requestFriendViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RequestFriendViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public interface RequestFriendListListener {
        void rfListener(User user);
    }

    public static class RequestFriendViewHolder extends RecyclerView.ViewHolder {
        public User user;
        public View requestFriendView;

        public RequestFriendViewHolder(@NonNull View friendView) {
            super(friendView);
            this.requestFriendView = friendView;
        }
    }
}
