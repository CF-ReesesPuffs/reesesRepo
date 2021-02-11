package com.cfreesespuffs.github.giftswapper.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cfreesespuffs.github.giftswapper.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link fragment_gifts#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragment_gifts extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "gift";
    private static final String ARG_PARAM2 = "user";

    private String mGiftName;
    private String mUserName;

    public fragment_gifts() {} // Required empty public constructor

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_gifts.
     */
    public static fragment_gifts newInstance(String gift, String user) {
        fragment_gifts fragment = new fragment_gifts();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, gift);
        args.putString(ARG_PARAM2, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mGiftName = getArguments().getString(ARG_PARAM1);
            mUserName = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) { // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_current_party_gifts, container, false);
    }
}