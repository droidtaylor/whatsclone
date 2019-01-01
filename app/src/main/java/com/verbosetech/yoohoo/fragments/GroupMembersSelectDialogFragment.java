package com.verbosetech.whatsclone.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.verbosetech.whatsclone.R;
import com.verbosetech.whatsclone.adapters.GroupNewParticipantsAdapter;
import com.verbosetech.whatsclone.interfaces.UserGroupSelectionDismissListener;
import com.verbosetech.whatsclone.models.User;

import java.util.ArrayList;

/**
 * Created by a_man on 31-12-2017.
 */

public class GroupMembersSelectDialogFragment extends BaseFullDialogFragment implements GroupNewParticipantsAdapter.ParticipantClickListener{
    private ArrayList<User> selectedUsers, myUsers;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_select_members, container);
        RecyclerView participants = view.findViewById(R.id.participants);
        RecyclerView myUsersRecycler = view.findViewById(R.id.myUsers);

        participants.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        myUsersRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        GroupNewParticipantsAdapter selectedParticipantsAdapter = new GroupNewParticipantsAdapter(this, selectedUsers);
        participants.setAdapter(selectedParticipantsAdapter);
        myUsersRecycler.setAdapter(new GroupNewParticipantsAdapter(this, myUsers, selectedParticipantsAdapter));

        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        view.findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return view;
    }

    public static GroupMembersSelectDialogFragment newInstance(UserGroupSelectionDismissListener dismissListener, ArrayList<User> selectedUsers, ArrayList<User> myUsers) {
        GroupMembersSelectDialogFragment fragment = new GroupMembersSelectDialogFragment();
        fragment.selectedUsers = selectedUsers;
        fragment.myUsers = myUsers;
        fragment.dismissListener = dismissListener;
        return fragment;
    }

    @Override
    public void onParticipantClick(int pos, User participant) {

    }
}
