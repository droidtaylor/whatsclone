package com.verbosetech.whatsclone.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.verbosetech.whatsclone.R;
import com.verbosetech.whatsclone.adapters.MediaSummaryAdapter;
import com.verbosetech.whatsclone.models.Message;
import com.verbosetech.whatsclone.utils.Helper;
import com.verbosetech.whatsclone.views.MyRecyclerView;

import java.util.ArrayList;

/**
 * Created by a_man on 6/30/2017.
 */

public class UserSubMediaFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";

    private int position;
    private ArrayList<Message> messages;

    private MyRecyclerView recyclerView;

    public UserSubMediaFragment() {
        // Required empty public constructor
    }

    public static UserSubMediaFragment newInstance(int position) {
        UserSubMediaFragment fragment = new UserSubMediaFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            position = getArguments().getInt(ARG_PARAM1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_sub_media, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);

        recyclerView.setEmptyView(view.findViewById(R.id.emptyView));
        recyclerView.setEmptyImageView(((ImageView) view.findViewById(R.id.emptyImage)));
        recyclerView.setEmptyTextView(((TextView) view.findViewById(R.id.emptyText)));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setAdapter(new MediaSummaryAdapter(getContext(), messages, true, new Helper(getContext()).getLoggedInUser().getId()));
    }

    public void setAttachment(ArrayList<Message> attachments) {
        this.messages = attachments;
    }
}
