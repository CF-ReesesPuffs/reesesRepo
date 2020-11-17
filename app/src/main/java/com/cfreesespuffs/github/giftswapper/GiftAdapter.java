package com.cfreesespuffs.github.giftswapper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amplifyframework.datastore.generated.model.Gift;

import java.util.ArrayList;

public class GiftAdapter extends RecyclerView.Adapter<GiftAdapter.GiftsToViewHolder> {

    public ArrayList<Gift> giftsList;
    public OnCommWithGiftsListener listener;

    public GiftAdapter(ArrayList<Gift> giftsList, OnCommWithGiftsListener listener) {
        this.giftsList = giftsList;
        this.listener = listener;
    }

    public static class GiftsToViewHolder extends RecyclerView.ViewHolder {

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
                .inflate(R.layout.fragment_gifts, parent, false);

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
        giftNameTv.setText(holder.gifts.getTitle());
    }

    @Override
    public int getItemCount() { return giftsList.size(); } // MUST be getItemCount

}
