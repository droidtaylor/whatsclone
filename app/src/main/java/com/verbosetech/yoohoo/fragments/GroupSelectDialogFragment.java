package com.verbosetech.whatsclone.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.verbosetech.whatsclone.R;
import com.verbosetech.whatsclone.adapters.GroupsAdapter;
import com.verbosetech.whatsclone.interfaces.UserGroupSelectionDismissListener;
import com.verbosetech.whatsclone.models.Group;
import com.verbosetech.whatsclone.utils.Helper;
import com.verbosetech.whatsclone.utils.SharedPreferenceHelper;
import com.verbosetech.whatsclone.views.MyRecyclerView;

import java.util.ArrayList;

/**
 * Created by a_man on 31-12-2017.
 */

public class GroupSelectDialogFragment extends BaseFullDialogFragment {
    private TextView heading;
    private EditText query;
    private MyRecyclerView recyclerView;
    private ArrayList<Group> myGroups;

    public GroupSelectDialogFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_select, container);
        heading = view.findViewById(R.id.heading);
        heading.setText("Groups");
        query = view.findViewById(R.id.searchQuery);
        query.setHint("Send to:");

        recyclerView = view.findViewById(R.id.recycler_view);

        recyclerView.setEmptyView(view.findViewById(R.id.emptyView));
        recyclerView.setEmptyImageView(((ImageView) view.findViewById(R.id.emptyImage)));
        TextView emptyTextView = view.findViewById(R.id.emptyText);
        emptyTextView.setText(getString(R.string.no_groups));
        recyclerView.setEmptyTextView(emptyTextView);

        view.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        view.findViewById(R.id.createGroup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper(getContext());
                sharedPreferenceHelper.setBooleanPreference(Helper.GROUP_CREATE, true);
                dismiss();
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new GroupsAdapter(getActivity(), myGroups));
    }

    public static GroupSelectDialogFragment newInstance(Context context, ArrayList<Group> myGroups) {
        GroupSelectDialogFragment dialogFragment = new GroupSelectDialogFragment();
        dialogFragment.myGroups = myGroups;
        if (context instanceof UserGroupSelectionDismissListener) {
            dialogFragment.dismissListener = (UserGroupSelectionDismissListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement UserGroupSelectionDismissListener");
        }
        return dialogFragment;
    }

}
