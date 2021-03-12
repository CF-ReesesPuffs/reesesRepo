package com.cfreesespuffs.github.giftswapper.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cfreesespuffs.github.giftswapper.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link friendRequestFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class friendRequestFrag extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "friendRequestName";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mFriendRequestName;
    private String mParam2;

    public friendRequestFrag() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param mFriendRequestName Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment friendRequestFrag.
     */
    // TODO: Rename and change types and number of parameters
    public static friendRequestFrag newInstance(String mFriendRequestName, String param2) {
        friendRequestFrag fragment = new friendRequestFrag();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, mFriendRequestName);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFriendRequestName = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friend_request, container, false);
    }
}