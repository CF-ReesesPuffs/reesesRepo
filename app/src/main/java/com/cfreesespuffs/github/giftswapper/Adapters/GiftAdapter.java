package com.cfreesespuffs.github.giftswapper.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.auth.AuthUser;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Gift;
import com.amplifyframework.datastore.generated.model.GuestList;
import com.amplifyframework.datastore.generated.model.User;
import com.cfreesespuffs.github.giftswapper.R;

import java.util.ArrayList;
import java.util.logging.Handler;

public class GiftAdapter extends RecyclerView.Adapter<GiftAdapter.GiftsToViewHolder> {
    public GuestList user;
    public ArrayList<Gift> giftsList;
    public OnCommWithGiftsListener listener;

    User amplifyUser;
    Gift giftUpdate;

    public GiftAdapter(ArrayList<Gift> giftsList, GuestList user, OnCommWithGiftsListener listener) {
        this.user = user;
        this.giftsList = giftsList;
        this.listener = listener;
    }

    Handler handler;

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
                .inflate(R.layout.fragment_gifts, parent, false);

        GiftsToViewHolder viewHolder = new GiftsToViewHolder(view);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthUser authUser = Amplify.Auth.getCurrentUser();
                Amplify.API.query(
                        ModelQuery.list(User.class),
                        response ->{
                            for(User user : response.getData()){
                                if(user.getUserName().equals(authUser.getUsername())){
                                    amplifyUser = user;
                                }
                            }
                            giftUpdate.user = amplifyUser;


                            Amplify.API.mutate(
                                    ModelMutation.create(giftUpdate),
                                    response2 -> Log.i("Mutation", "mutated the gifts user " + giftUpdate),
                                    error -> Log.e("Mutation", "Failure, you disgrace family " + error)
                            );
                        },
                        error -> Log.e("amplify.user", String.valueOf(error))
                );

                System.out.println(viewHolder.gifts);

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
        TextView userOwner = holder.giftView.findViewById(R.id.userName);
        userOwner.setVisibility(View.VISIBLE);

        giftNameTv.setText(holder.gifts.getTitle());
        giftUpdate = giftsList.get(position);
        userOwner.setText(giftUpdate.getUser().getUserName());

    }


    @Override
    public int getItemCount() {
        if(giftsList == null){
            return 0;
        }
        return giftsList.size();
    } // MUST be getItemCount

}
