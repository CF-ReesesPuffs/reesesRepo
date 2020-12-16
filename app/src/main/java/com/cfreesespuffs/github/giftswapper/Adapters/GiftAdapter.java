package com.cfreesespuffs.github.giftswapper.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amplifyframework.auth.AuthUser;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Gift;
import com.amplifyframework.datastore.generated.model.GuestList;
import com.amplifyframework.datastore.generated.model.User;
import com.cfreesespuffs.github.giftswapper.R;

import java.util.ArrayList;
import java.util.logging.Handler;

public class GiftAdapter extends RecyclerView.Adapter<GiftAdapter.GiftsToViewHolder> {
    public ArrayList<Gift> giftsList;
    public OnCommWithGiftsListener listener;
    public User user;
    public AuthUser authUser;

    public Gift giftUpdate;
    public Gift heldGift;
    public TextView userOwner;

    public GiftAdapter(ArrayList<Gift> giftsList, User user, OnCommWithGiftsListener listener) {
        this.giftsList = giftsList;
        this.listener = listener;
        this.user = user;
    }

    public static class GiftsToViewHolder extends RecyclerView.ViewHolder {
        public GuestList user;
        public Gift gifts;
        public View giftView;

        public GiftsToViewHolder(@NonNull View giftView) {
            super(giftView);
            this.giftView = giftView;
        }
    }

    @NonNull
    @Override
    public GiftsToViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_current_party_gifts, parent, false);

        GiftsToViewHolder viewHolder = new GiftsToViewHolder(view);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.giftsToDoListener(viewHolder.gifts);
            }
        });
        return viewHolder;
    }

    public static interface OnCommWithGiftsListener {
        public void giftsToDoListener(Gift gift);
    }

    @Override
    public void onBindViewHolder(@NonNull GiftsToViewHolder holder, int position) {
        holder.gifts = giftsList.get(position);
        TextView giftNameTv = holder.giftView.findViewById(R.id.giftNameFrag);
        TextView userOwner = holder.giftView.findViewById(R.id.giftOwner);
        userOwner.setVisibility(View.VISIBLE);

        heldGift = holder.gifts;
       // giftNameTv.setText(holder.gifts.getTitle());
        giftUpdate = giftsList.get(position);

        authUser = Amplify.Auth.getCurrentUser();

        Log.i("System.viewholder", "The authuser: " + authUser);
        Log.i("System.viewholder", "From the giftUpdate: " + giftUpdate.getUser().getUserName());

        if (giftUpdate.getUser().getUserName().contains(authUser.getUsername()) && giftUpdate.getPartyGoer().contains("TBD")) { // This works! TODO: test with parties after pulling from PR.
            userOwner.setText("you brought this gift!");
        } else {
            userOwner.setText(giftUpdate.getPartyGoer()); // to change name that shows up. b
        }

        if (giftUpdate.getPartyGoer().contains("TBD")) {
            giftNameTv.setText(giftUpdate.getNumber().toString());
        } else {
            giftNameTv.setText(giftUpdate.getTitle());
        }
    }


    @Override
    public int getItemCount() {
        if(giftsList == null){
            return 0;
        }
        return giftsList.size();
    } // MUST be getItemCount

}
