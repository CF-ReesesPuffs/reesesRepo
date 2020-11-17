package com.cfreesespuffs.github.giftswapper.Adapters;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cfreesespuffs.github.giftswapper.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PendingPartyFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PendingPartyFrag extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "guestName";
    private static final String ARG_PARAM2 = "status";

    // TODO: Rename and change types of parameters
    private String mGuestName;
    private String mStatus;

    public PendingPartyFrag() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param guestName Parameter 1.
     * @param status Parameter 2.
     * @return A new instance of fragment PendingPartyFrag.
     */
    // TODO: Rename and change types and number of parameters
    public static PendingPartyFrag newInstance(String guestName, String status) {
        PendingPartyFrag fragment = new PendingPartyFrag();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, guestName);
        args.putString(ARG_PARAM2, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mGuestName = getArguments().getString(ARG_PARAM1);
            mStatus = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pending, container, false);
    }
}