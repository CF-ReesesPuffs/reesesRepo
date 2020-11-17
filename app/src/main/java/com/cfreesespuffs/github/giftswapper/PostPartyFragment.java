package com.cfreesespuffs.github.giftswapper;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PostPartyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PostPartyFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "guestName";
    private static final String ARG_PARAM2 = "gift";

    // TODO: Rename and change types of parameters
    private String mGuestName;
    private String mGift;

    public PostPartyFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param guestName Parameter 1.
     * @param gift Parameter 2.
     * @return A new instance of fragment PostPartyFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PostPartyFragment newInstance(String guestName, String gift) {
        PostPartyFragment fragment = new PostPartyFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, guestName);
        args.putString(ARG_PARAM2, gift);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mGuestName = getArguments().getString(ARG_PARAM1);
            mGift = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_post_party, container, false);
    }
}