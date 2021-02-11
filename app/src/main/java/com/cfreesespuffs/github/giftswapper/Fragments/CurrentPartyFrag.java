package com.cfreesespuffs.github.giftswapper.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cfreesespuffs.github.giftswapper.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CurrentPartyFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CurrentPartyFrag extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "user";
    private static final String ARG_PARAM2 = "gift";

    private String mUser;
    private String mGift;

    public CurrentPartyFrag() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CurrentPartyFrag.
     */
    public static CurrentPartyFrag newInstance(String user, String gift) {
        CurrentPartyFrag fragment = new CurrentPartyFrag();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, user);
        args.putString(ARG_PARAM2, gift);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUser = getArguments().getString(ARG_PARAM1);
            mGift = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) { // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_current_party_user, container, false);
    }
}