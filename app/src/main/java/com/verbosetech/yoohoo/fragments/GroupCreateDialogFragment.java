package com.verbosetech.whatsclone.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.verbosetech.whatsclone.adapters.GroupNewParticipantsAdapter;
import com.verbosetech.whatsclone.interfaces.ChatItemClickListener;
import com.verbosetech.whatsclone.interfaces.UserGroupSelectionDismissListener;
import com.verbosetech.whatsclone.models.Attachment;
import com.verbosetech.whatsclone.models.AttachmentTypes;
import com.verbosetech.whatsclone.models.Group;
import com.verbosetech.whatsclone.models.MyString;
import com.verbosetech.whatsclone.models.User;
import com.verbosetech.whatsclone.utils.FirebaseUploader;
import com.verbosetech.whatsclone.utils.Helper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by a_man on 31-12-2017.
 */

public class GroupCreateDialogFragment extends BaseFullDialogFragment implements UserGroupSelectionDismissListener, ImagePickerCallback {
    protected String[] permissionsCamera = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private ImagePicker imagePicker;
    private CameraImagePicker cameraPicker;
    private String pickerPath;
    private File mediaFile;

    private ArrayList<User> myUsers, selectedUsers;
    private static final int REQUEST_CODE_PICKER = 4321;
    private static final int REQUEST_CODE_MEDIA_PERMISSION = 999;

    private ImageView groupImage;
    private EditText groupName, groupStatus;
    private TextView participantsCount;
    private ProgressBar groupImageProgress;
    private GroupNewParticipantsAdapter selectedParticipantsAdapter;
    private User userMe;

    private String groupId;
    private ChatItemClickListener chatItemClickListener;
    private View done;
    private Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDetach() {
        this.context = null;
        super.onDetach();
    }

    public GroupCreateDialogFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_new, container);
        groupImage = view.findViewById(R.id.groupImage);
        groupName = view.findViewById(R.id.groupName);
        groupStatus = view.findViewById(R.id.groupStatus);
        participantsCount = view.findViewById(R.id.participantsCount);
        groupImageProgress = view.findViewById(R.id.groupImageProgress);
        groupImageProgress.setVisibility(View.GONE);

        RecyclerView participantsRecycler = view.findViewById(R.id.participants);
        participantsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        selectedParticipantsAdapter = new GroupNewParticipantsAdapter(this, selectedUsers, false);
        participantsRecycler.setAdapter(selectedParticipantsAdapter);

        view.findViewById(R.id.groupImageContainer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage();
            }
        });
        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        done = view.findViewById(R.id.done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                done();
            }
        });
        view.findViewById(R.id.participantsAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myUsers.isEmpty())
                    Toast.makeText(getContext(), R.string.empty_contact_list_for_group, Toast.LENGTH_SHORT).show();
                else
                    GroupMembersSelectDialogFragment.newInstance(GroupCreateDialogFragment.this, selectedUsers, myUsers).show(getChildFragmentManager(), "selectgroupmembers");
            }
        });
        setCancelable(false);
        return view;
    }

    private void pickImage() {
        if (mediaPermissions().isEmpty()) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
            alertDialog.setMessage(R.string.get_image_title);
            alertDialog.setPositiveButton(R.string.get_image_camera, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();

                    cameraPicker = new CameraImagePicker(GroupCreateDialogFragment.this);
                    cameraPicker.shouldGenerateMetadata(true);
                    cameraPicker.shouldGenerateThumbnails(true);
                    cameraPicker.setImagePickerCallback(GroupCreateDialogFragment.this);
                    pickerPath = cameraPicker.pickImage();
                }
            });
            alertDialog.setNegativeButton(R.string.get_image_gallery, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();

                    imagePicker = new ImagePicker(GroupCreateDialogFragment.this);
                    imagePicker.shouldGenerateMetadata(true);
                    imagePicker.shouldGenerateThumbnails(true);
                    imagePicker.setImagePickerCallback(GroupCreateDialogFragment.this);
                    imagePicker.pickImage();
                }
            });
            alertDialog.create().show();
        } else {
            requestPermissions(permissionsCamera, REQUEST_CODE_MEDIA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_MEDIA_PERMISSION) {
            if (mediaPermissions().isEmpty()) {
                pickImage();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Picker.PICK_IMAGE_DEVICE:
                    if (imagePicker == null) {
                        imagePicker = new ImagePicker(this);
                    }
                    imagePicker.submit(data);
                    break;
                case Picker.PICK_IMAGE_CAMERA:
                    if (cameraPicker == null) {
                        cameraPicker = new CameraImagePicker(this);
                        cameraPicker.reinitialize(pickerPath);
                    }
                    cameraPicker.submit(data);
                    break;
            }
        }
    }

    @Override
    public void onImagesChosen(List<ChosenImage> images) {
        mediaFile = new File(Uri.parse(images.get(0).getOriginalPath()).getPath());
        Glide.with(this).load(mediaFile).apply(new RequestOptions().placeholder(R.drawable.whatsclone_placeholder)).into(groupImage);
    }

    @Override
    public void onError(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // You have to save path in case your activity is killed.
        // In such a scenario, you will need to re-initialize the CameraImagePicker
        outState.putString("picker_path", pickerPath);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("picker_path")) {
                pickerPath = savedInstanceState.getString("picker_path");
            }
        }
    }

    private List<String> mediaPermissions() {
        List<String> missingPermissions = new ArrayList<>();
        for (String permission : permissionsCamera) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        return missingPermissions;
    }

    private void done() {
        if (selectedParticipantsAdapter.getItemCount() == 0) {
            Toast.makeText(getContext(), R.string.validation_req_participant, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(groupName.getText().toString().trim())) {
            Toast.makeText(getContext(), R.string.validation_req_name, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(groupStatus.getText().toString().trim())) {
            Toast.makeText(getContext(), R.string.validation_req_description, Toast.LENGTH_SHORT).show();
            return;
        }

        done.setClickable(false);
        done.setFocusable(false);

        groupId = Helper.GROUP_PREFIX + "_" + userMe.getId() + "_" + System.currentTimeMillis();

        if (mediaFile == null) {
            createGroup("");
        } else {
            userImageUploadTask(mediaFile, AttachmentTypes.IMAGE, null);
        }

    }

    private void userImageUploadTask(final File fileToUpload, @AttachmentTypes.AttachmentType final int attachmentType, final Attachment attachment) {
        groupImageProgress.setVisibility(View.VISIBLE);

        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child(getString(R.string.app_name))
                .child("ProfileImage")
                .child(groupId);

        FirebaseUploader firebaseUploader = new FirebaseUploader(new FirebaseUploader.UploadListener() {
            @Override
            public void onUploadFail(String message) {
                if (context != null) {
                    groupImageProgress.setVisibility(View.GONE);
                    Toast.makeText(getContext(), R.string.err_upload_image, Toast.LENGTH_SHORT).show();
                    createGroup("");
                }
            }

            @Override
            public void onUploadSuccess(String downloadUrl) {
                if (context != null) {
                    groupImageProgress.setVisibility(View.GONE);
                    createGroup(downloadUrl);
                }
            }

            @Override
            public void onUploadProgress(int progress) {

            }

            @Override
            public void onUploadCancelled() {

            }
        }, storageReference);
        firebaseUploader.setReplace(true);
        firebaseUploader.uploadImage(getContext(), fileToUpload);
    }

    private void createGroup(String groupImageUrl) {
        final Group group = new Group();
        group.setId(groupId);
        group.setName(groupName.getText().toString());
        group.setStatus(groupStatus.getText().toString());
        group.setImage(groupImageUrl);
        ArrayList<MyString> userIds = new ArrayList<>();
        userIds.add(new MyString(userMe.getId()));
        for (User user : selectedUsers) {
            userIds.add(new MyString(user.getId()));
        }
        group.setUserIds(userIds);

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference groupRef = firebaseDatabase.getReference(Helper.REF_GROUP).child(groupId);
        groupRef.setValue(group).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if (context != null) {
                    Toast.makeText(getContext(), R.string.group_created, Toast.LENGTH_SHORT).show();
                    chatItemClickListener.onChatItemClick(group, -1, null);
                    dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (context != null) {
                    Toast.makeText(getContext(), R.string.error_request, Toast.LENGTH_SHORT).show();
                    done.setClickable(true);
                    done.setFocusable(true);
                }
            }
        });
    }

    public static GroupCreateDialogFragment newInstance(ChatItemClickListener chatItemClickListener, User user, ArrayList<User> myUsers) {
        GroupCreateDialogFragment dialogFragment = new GroupCreateDialogFragment();
        dialogFragment.userMe = user;
        dialogFragment.myUsers = myUsers;
        dialogFragment.chatItemClickListener = chatItemClickListener;
        dialogFragment.selectedUsers = new ArrayList<>();
        return dialogFragment;
    }

    @Override
    public void onUserGroupSelectDialogDismiss() {
        //do nothing
    }

    @Override
    public void selectionDismissed() {
        if (selectedParticipantsAdapter != null) {
            selectedParticipantsAdapter.notifyDataSetChanged();
            participantsCount.setText(String.format(getString(R.string.participants_count), selectedParticipantsAdapter.getItemCount()));
        }
    }
}
