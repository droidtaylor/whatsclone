package com.verbosetech.whatsclone.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.verbosetech.whatsclone.R;
import com.verbosetech.whatsclone.adapters.ChatAdapter;
import com.verbosetech.whatsclone.interfaces.HomeIneractor;
import com.verbosetech.whatsclone.models.Chat;
import com.verbosetech.whatsclone.models.User;
import com.verbosetech.whatsclone.utils.Helper;
import com.verbosetech.whatsclone.views.MyRecyclerView;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by a_man on 30-12-2017.
 */

public class MyGroupsFragment extends Fragment {
    private MyRecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private Realm rChatDb;
    private User userMe;

    private RealmResults<Chat> resultList;
    private ArrayList<Chat> chatDataList = new ArrayList<>();

    private RealmChangeListener<RealmResults<Chat>> chatListChangeListener = new RealmChangeListener<RealmResults<Chat>>() {
        @Override
        public void onChange(RealmResults<Chat> element) {
            if (chatAdapter != null && chatDataList != null) {
                chatDataList.clear();
                chatDataList.addAll(rChatDb.copyFromRealm(element));
                chatAdapter.notifyDataSetChanged();
            }
        }
    };

    private HomeIneractor homeInteractor;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            homeInteractor = (HomeIneractor) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement HomeIneractor");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Helper helper = new Helper(getContext());
        userMe = helper.getLoggedInUser();
        Realm.init(getContext());
        rChatDb = Helper.getRealmInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_recycler, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);

        recyclerView.setEmptyView(view.findViewById(R.id.emptyView));
        recyclerView.setEmptyImageView(((ImageView) view.findViewById(R.id.emptyImage)));
        TextView emptyTextView = view.findViewById(R.id.emptyText);
        emptyTextView.setText(getString(R.string.empty_group_chat_list));
        recyclerView.setEmptyTextView(emptyTextView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RealmQuery<Chat> query = rChatDb.where(Chat.class).equalTo("myId", userMe.getId());//Query from chats whose owner is logged in user
        resultList = query.equalTo("group", true).sort("timeUpdated", Sort.DESCENDING).findAll();//ignore forward list of messages and get rest sorted according to time

        chatDataList.clear();
        chatDataList.addAll(rChatDb.copyFromRealm(resultList));

        chatAdapter = new ChatAdapter(getActivity(), chatDataList);
        recyclerView.setAdapter(chatAdapter);

        resultList.addChangeListener(chatListChangeListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        homeInteractor = null;
        if (resultList != null)
            resultList.removeChangeListener(chatListChangeListener);
    }

    public void deleteSelectedChats() {
        rChatDb.beginTransaction();
        for (Chat chat : chatDataList) {
            if (chat.isSelected()) {
                Chat chatToDelete = rChatDb.where(Chat.class).equalTo("myId", userMe.getId()).equalTo("groupId", chat.getChatId()).findFirst();
                if (chatToDelete != null) {
                    RealmObject.deleteFromRealm(chatToDelete);
                }
            }
        }
        rChatDb.commitTransaction();
    }

    public void disableContextualMode() {
        chatAdapter.disableContextualMode();
    }
}
