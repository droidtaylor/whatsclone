package com.verbosetech.whatsclone.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.verbosetech.whatsclone.R;
import com.verbosetech.whatsclone.models.User;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by a_man on 31-12-2017.
 */

public class GroupNewParticipantsAdapter extends RecyclerView.Adapter<GroupNewParticipantsAdapter.MyViewHolder> {
    private User userMe;
    private GroupNewParticipantsAdapter groupNewParticipantsAdapter;
    private Context context;
    private ArrayList<User> dataList;
    private boolean staggered;
    private ParticipantClickListener participantClickListener;

    public GroupNewParticipantsAdapter(Fragment fragment, ArrayList<User> selectedUsers) {
        this.context = fragment.getActivity();
        this.dataList = selectedUsers;
        this.staggered = true;
    }

    public GroupNewParticipantsAdapter(Fragment fragment, ArrayList<User> selectedUsers, GroupNewParticipantsAdapter groupNewParticipantsAdapter) {
        if (fragment instanceof ParticipantClickListener) {
            this.participantClickListener = (ParticipantClickListener) fragment;
        } else {
            throw new RuntimeException(fragment.toString() + " must implement ParticipantClickListener");
        }

        this.context = fragment.getActivity();
        this.dataList = selectedUsers;
        this.staggered = false;
        this.groupNewParticipantsAdapter = groupNewParticipantsAdapter;
    }

    public GroupNewParticipantsAdapter(Fragment fragment, ArrayList<User> selectedUsers, boolean staggered) {
        this.context = fragment.getActivity();
        this.dataList = selectedUsers;
        this.staggered = staggered;
    }

    public GroupNewParticipantsAdapter(Fragment fragment, ArrayList<User> selectedUsers, User userMe) {
        if (fragment instanceof ParticipantClickListener) {
            this.participantClickListener = (ParticipantClickListener) fragment;
        } else {
            throw new RuntimeException(fragment.toString() + " must implement ParticipantClickListener");
        }

        this.context = fragment.getActivity();
        this.dataList = selectedUsers;
        this.staggered = false;
        this.userMe = userMe;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(staggered ? R.layout.item_selected_user : R.layout.item_menu_user, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.setData(dataList.get(position));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView userName;

        private AppCompatRadioButton userSelected;
        private ImageView userImage, removeUser;

        public MyViewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            if (!staggered) {
                userImage = itemView.findViewById(R.id.user_image);
                userSelected = itemView.findViewById(R.id.userSelected);
                if (userMe != null) {
                    removeUser = itemView.findViewById(R.id.removeUser);
                    removeUser.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            int pos = getAdapterPosition();
                            participantClickListener.onParticipantClick(pos, dataList.get(pos));
                        }
                    });
                }
                if (groupNewParticipantsAdapter != null) {
                    itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            int pos = getAdapterPosition();
                            User user = dataList.get(pos);
                            user.setSelected(!user.isSelected());
                            notifyItemChanged(pos);

                            int index = groupNewParticipantsAdapter.getDataList().indexOf(user);
                            if (index == -1) {
                                groupNewParticipantsAdapter.getDataList().add(user);
                                groupNewParticipantsAdapter.notifyItemInserted(groupNewParticipantsAdapter.getDataList().size() - 1);
                            } else {
                                groupNewParticipantsAdapter.getDataList().remove(index);
                                groupNewParticipantsAdapter.notifyItemRemoved(index);
                            }
                        }
                    });
                }
            }
        }

        public void setData(User user) {
            userName.setText(user.getNameToDisplay());
            if (!staggered) {
                if (groupNewParticipantsAdapter != null) {
                    userSelected.setVisibility(View.VISIBLE);
                    userSelected.setChecked(user.isSelected());
                }
                Glide.with(context).load(user.getImage()).apply(new RequestOptions().placeholder(R.drawable.whatsclone_placeholder)).into(userImage);
            }
            if (removeUser != null)
                removeUser.setVisibility((userMe.getId().equals(user.getId()) || userMe.getId().equals(dataList.get(0).getId())) ? View.VISIBLE : View.GONE);
        }
    }

    private ArrayList<User> getDataList() {
        return dataList;
    }

    public interface ParticipantClickListener {
        void onParticipantClick(int pos, User participant);
    }

}
