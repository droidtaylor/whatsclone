package com.verbosetech.whatsclone.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;
import com.verbosetech.whatsclone.R;
import com.verbosetech.whatsclone.models.Attachment;
import com.verbosetech.whatsclone.models.AttachmentTypes;
import com.verbosetech.whatsclone.models.User;
import com.verbosetech.whatsclone.services.SinchService;
import com.verbosetech.whatsclone.utils.ConfirmationDialogFragment;
import com.verbosetech.whatsclone.utils.FirebaseUploader;
import com.verbosetech.whatsclone.utils.Helper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by a_man on 01-01-2018.
 */

public class OptionsFragment extends BaseFullDialogFragment {
    private static String CONFIRM_TAG = "confirmtag";
    private static String PRIVACY_TAG = "privacytag";
    private static String PROFILE_EDIT_TAG = "profileedittag";
    private ImageView userImage;
    private TextView userName, userStatus;
    private Helper helper;
    private SinchService.SinchServiceInterface sinchServiceInterface;
    private User userMe;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_options, container);
        userImage = view.findViewById(R.id.userImage);
        userName = view.findViewById(R.id.userName);
        userStatus = view.findViewById(R.id.userStatus);

        setUserDetails();

        view.findViewById(R.id.userDetailContainer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ProfileEditDialogFragment().show(getChildFragmentManager(), PROFILE_EDIT_TAG);
            }
        });
        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.closeKeyboard(getContext(), view);
                dismiss();
            }
        });
        view.findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.openShareIntent(getContext(), null, String.format(getString(R.string.download_message), getString(R.string.app_name)));
            }
        });
        view.findViewById(R.id.rate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.openPlayStore(getContext());
            }
        });
        view.findViewById(R.id.contact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.openSupportMail(getContext());
            }
        });
        view.findViewById(R.id.privacy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new PrivacyPolicyDialogFragment().show(getChildFragmentManager(), PRIVACY_TAG);
            }
        });
        view.findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConfirmationDialogFragment confirmationDialogFragment = ConfirmationDialogFragment.newInstance(getString(R.string.logout_title),
                        getString(R.string.logout_message),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                FirebaseAuth.getInstance().signOut();
                                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(Helper.BROADCAST_LOGOUT));
                                sinchServiceInterface.stopClient();
                                helper.logout();
                                getActivity().finish();
                            }
                        },
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                            }
                        });
                confirmationDialogFragment.show(getChildFragmentManager(), CONFIRM_TAG);
            }
        });
        return view;
    }

    public void setUserDetails() {
        helper = new Helper(getContext());
        userMe = helper.getLoggedInUser();
        userName.setText(userMe.getNameToDisplay());
        userStatus.setText(userMe.getStatus());
        Glide.with(this).load(userMe.getImage()).apply(new RequestOptions().placeholder(R.drawable.whatsclone_placeholder)).into(userImage);
    }

    public static OptionsFragment newInstance(SinchService.SinchServiceInterface sinchServiceInterface) {
        OptionsFragment fragment = new OptionsFragment();
        fragment.sinchServiceInterface = sinchServiceInterface;
        return fragment;
    }
}
