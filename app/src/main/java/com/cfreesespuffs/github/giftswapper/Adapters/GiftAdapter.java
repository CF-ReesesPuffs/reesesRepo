package com.cfreesespuffs.github.giftswapper.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amplifyframework.datastore.generated.model.Gift;
import com.cfreesespuffs.github.giftswapper.R;

import java.util.ArrayList;

public class GiftAdapter extends RecyclerView.Adapter<GiftAdapter.GiftsToViewHolder> {
    public ArrayList<Gift> giftsList;
    public OnCommWithGiftsListener listener;

    public GiftAdapter(ArrayList<Gift> giftsList, OnCommWithGiftsListener listener) {
        this.giftsList = giftsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GiftsToViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_gifts, parent, false);
        GiftsToViewHolder giftViewHolder = new GiftsToViewHolder(view);

        view.setOnClickListener((newView) -> {
                listener.giftListener(giftViewHolder.gifts);
        });
        return giftViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull GiftsToViewHolder holder, int position) {
        holder.gifts = giftsList.get(position);

        TextView giftNameTv = holder.giftView.findViewById(R.id.giftNameFrag);
        giftNameTv.setText(holder.gifts.getTitle());
    }

    @Override
    public int getItemCount() {
       if (giftsList == null) {
           return 0;
       } else {
           return giftsList.size();
       }
    }

    public static interface OnCommWithGiftsListener {
        public void giftListener(Gift gift);
    }

    public static class GiftsToViewHolder extends RecyclerView.ViewHolder {
        public Gift gifts;
        public View giftView;

        public GiftsToViewHolder(@NonNull View giftView) {
            super(giftView);
            this.giftView = giftView;
        }
    }
}
