package com.verbosetech.whatsclone.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.verbosetech.whatsclone.R;
import com.verbosetech.whatsclone.adapters.MenuUsersRecyclerAdapter;
import com.verbosetech.whatsclone.interfaces.UserGroupSelectionDismissListener;
import com.verbosetech.whatsclone.models.User;

import java.util.ArrayList;

/**
 * Created by a_man on 01-12-2017.
 */

public class UserSelectDialogFragment extends BaseFullDialogFragment {
    private TextView heading;
    private EditText query;
    private RecyclerView usersRecycler;
    private ArrayList<User> myUsers;

    public UserSelectDialogFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_select, container);
        heading = view.findViewById(R.id.heading);
        query = view.findViewById(R.id.searchQuery);
        usersRecycler = view.findViewById(R.id.usersRecycler);
        view.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        usersRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        usersRecycler.setAdapter(new MenuUsersRecyclerAdapter(getActivity(), myUsers));
        query.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (usersRecycler.getAdapter() instanceof MenuUsersRecyclerAdapter) {
                    ((MenuUsersRecyclerAdapter) usersRecycler.getAdapter()).getFilter().filter(editable.toString());
                }
            }
        });
    }

    public void refreshUsers(int pos) {
        if (pos == -1) {
            query.setText("");
            usersRecycler.getAdapter().notifyDataSetChanged();
        } else {
            usersRecycler.getAdapter().notifyItemChanged(pos);
        }
    }

    public static UserSelectDialogFragment newInstance(Context context, ArrayList<User> myUsers) {
        UserSelectDialogFragment dialogFragment = new UserSelectDialogFragment();
        dialogFragment.myUsers = myUsers;
        if (context instanceof UserGroupSelectionDismissListener) {
            dialogFragment.dismissListener = (UserGroupSelectionDismissListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement UserGroupSelectionDismissListener");
        }
        return dialogFragment;
    }
}
