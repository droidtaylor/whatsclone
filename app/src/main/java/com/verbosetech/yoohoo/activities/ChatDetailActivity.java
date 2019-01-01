package com.verbosetech.whatsclone.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;
import com.verbosetech.whatsclone.R;
import com.verbosetech.whatsclone.fragments.ChatDetailFragment;
import com.verbosetech.whatsclone.fragments.UserMediaFragment;
import com.verbosetech.whatsclone.interfaces.OnUserDetailFragmentInteraction;
import com.verbosetech.whatsclone.models.Attachment;
import com.verbosetech.whatsclone.models.AttachmentTypes;
import com.verbosetech.whatsclone.models.Chat;
import com.verbosetech.whatsclone.models.Contact;
import com.verbosetech.whatsclone.models.Group;
import com.verbosetech.whatsclone.models.Message;
import com.verbosetech.whatsclone.models.MyString;
import com.verbosetech.whatsclone.models.User;
import com.verbosetech.whatsclone.utils.GeneralUtils;
import com.verbosetech.whatsclone.utils.Helper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatDetailActivity extends BaseActivity implements OnUserDetailFragmentInteraction, ImagePickerCallback {
    private Handler handler;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private AppBarLayout appBarLayout;
    private CoordinatorLayout coordinatorLayout;
    private View userDetailsSummaryContainer, pickImage;
    private ImageView userImage;
    private EditText userStatus, userName;
    private ArrayList<Message> mediaMessages;

    private static final String TAG_DETAIL = "TAG_DETAIL", TAG_MEDIA = "TAG_MEDIA";
    private static String EXTRA_DATA_USER = "extradatauser";
    private static String EXTRA_DATA_GROUP = "extradatagroup";
    private static final int REQUEST_CODE_PICKER = 4321;
    private static final int REQUEST_CODE_MEDIA_PERMISSION = 999;
    private ChatDetailFragment fragmentUserDetail;
    private View done;

    private String pickerPath;
    private ImagePicker imagePicker;
    private CameraImagePicker cameraPicker;

    @Override
    void myUsersResult(ArrayList<User> myUsers) {

    }

    @Override
    void myContactsResult(HashMap<String, Contact> myContacts) {

    }

    @Override
    void userAdded(User valueUser) {
        //doNothing
    }

    @Override
    void groupAdded(Group valueGroup) {
        //doNothing
    }

    @Override
    void userUpdated(User valueUser) {
        if (user != null && user.getId().equals(valueUser.getId())) {
            valueUser.setNameInPhone(user.getNameInPhone());
            user = valueUser;
            setUserData();

            Intent resultIntent = new Intent();
            resultIntent.putExtra(EXTRA_DATA_USER, user);
            setResult(Activity.RESULT_OK, resultIntent);
        }
    }

    @Override
    void groupUpdated(Group valueGroup) {
        if (group != null && group.getId().equals(valueGroup.getId())) {
            group = valueGroup;
            if (fragmentUserDetail != null) {
                fragmentUserDetail.notifyGroupUpdated(group);
            }
            if (!group.getUserIds().contains(new MyString(userMe.getId()))) {
                userStatus.setText(getString(R.string.removed_from_group));
                userName.setEnabled(false);
                userStatus.setEnabled(false);
                done.setVisibility(View.GONE);
            } else {
                setUserData();
            }

            Intent resultIntent = new Intent();
            resultIntent.putExtra(EXTRA_DATA_GROUP, group);
            setResult(Activity.RESULT_OK, resultIntent);
        }
    }

    @Override
    void onSinchConnected() {

    }

    @Override
    void onSinchDisconnected() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        collapsingToolbarLayout = findViewById(R.id.collapsingToolbar);
        appBarLayout = findViewById(R.id.appBarLayout);
        userDetailsSummaryContainer = findViewById(R.id.userDetailsSummaryContainer);
        pickImage = findViewById(R.id.pickImage);
        setSupportActionBar(((Toolbar) findViewById(R.id.toolbar)));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_keyboard_arrow_left_white_36dp);
        userImage = findViewById(R.id.expandedImage);
        userName = findViewById(R.id.user_name);
        userStatus = findViewById(R.id.emotion);
        done = findViewById(R.id.done);

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_DATA_USER)) {
            user = intent.getParcelableExtra(EXTRA_DATA_USER);
        } else if (intent.hasExtra(EXTRA_DATA_GROUP)) {
            group = intent.getParcelableExtra(EXTRA_DATA_GROUP);
        } else {
            finish();
        }
        handler = new Handler();

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.closeKeyboard(ChatDetailActivity.this, view);
                updateGroupNameAndStatus(userName.getText().toString().trim(), userStatus.getText().toString().trim());
            }
        });
        pickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ChatDetailActivity.this);
                alertDialog.setMessage(R.string.get_image_title);
                alertDialog.setPositiveButton(R.string.get_image_camera, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        openImageClick();
                    }
                });
                alertDialog.setNegativeButton(R.string.get_image_gallery, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        openImagePick();
                    }
                });
                alertDialog.create().show();
            }
        });

        setupViews();
        getMediaInfo();
        loadFragment(TAG_DETAIL);
    }

    void openImageClick() {
        if (permissionsAvailable(permissionsCamera)) {
            cameraPicker = new CameraImagePicker(this);
            cameraPicker.shouldGenerateMetadata(true);
            cameraPicker.shouldGenerateThumbnails(true);
            cameraPicker.setImagePickerCallback(this);
            pickerPath = cameraPicker.pickImage();
        } else {
            ActivityCompat.requestPermissions(this, permissionsCamera, 47);
        }
    }

    public void openImagePick() {
        if (permissionsAvailable(permissionsStorage)) {
            imagePicker = new ImagePicker(this);
            imagePicker.shouldGenerateMetadata(true);
            imagePicker.shouldGenerateThumbnails(true);
            imagePicker.setImagePickerCallback(this);
            imagePicker.pickImage();
        } else {
            ActivityCompat.requestPermissions(this, permissionsStorage, 36);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 36:
                if (permissionsAvailable(permissions))
                    openImagePick();
                break;
            case 47:
                if (permissionsAvailable(permissions))
                    openImageClick();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == AppCompatActivity.RESULT_OK) {
            switch (requestCode) {
                case Picker.PICK_IMAGE_DEVICE:
                    if (imagePicker == null) {
                        imagePicker = new ImagePicker(this);
                        imagePicker.setImagePickerCallback(this);
                    }
                    imagePicker.submit(data);
                    break;
                case Picker.PICK_IMAGE_CAMERA:
                    if (cameraPicker == null) {
                        cameraPicker = new CameraImagePicker(this);
                        cameraPicker.setImagePickerCallback(this);
                        cameraPicker.reinitialize(pickerPath);
                    }
                    cameraPicker.submit(data);
                    break;
            }
        }
    }

    private void userImageUploadTask(final File fileToUpload, @AttachmentTypes.AttachmentType final int attachmentType, final Attachment attachment) {
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child(getString(R.string.app_name))
                .child("ProfileImage")
                .child(group.getId());
        UploadTask uploadTask = storageReference.putFile(Uri.fromFile(fileToUpload));
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                // Continue with the task to get the download URL
                return storageReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    group.setImage(downloadUri.toString());
                    groupRef.child(group.getId()).setValue(group).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(ChatDetailActivity.this, getString(R.string.updated), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(ChatDetailActivity.this, R.string.err_upload_img, Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChatDetailActivity.this, R.string.err_upload_img, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateGroupNameAndStatus(String updatedName, String updatedStatus) {
        if (TextUtils.isEmpty(updatedName)) {
            Toast.makeText(this, R.string.validation_req_username, Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(updatedStatus)) {
            Toast.makeText(this, R.string.validation_req_status, Toast.LENGTH_SHORT).show();
        } else if (!group.getName().equals(updatedName) || !group.getStatus().equals(updatedStatus)) {
            group.setName(updatedName);
            group.setStatus(updatedStatus);
            groupRef.child(group.getId()).setValue(group).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(ChatDetailActivity.this, R.string.updated, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void getMediaInfo() {
        String myId = userMe.getId();
        Chat query = Helper.getChat(rChatDb, myId, user != null ? user.getId() : group.getId()).notEqualTo("messages.attachmentType", 6).findFirst();

        mediaMessages = new ArrayList<>();
        if (query != null) {
            for (Message m : query.getMessages()) {
                if (m.getAttachmentType() == AttachmentTypes.AUDIO
                        ||
                        m.getAttachmentType() == AttachmentTypes.IMAGE
                        ||
                        m.getAttachmentType() == AttachmentTypes.VIDEO
                        ||
                        m.getAttachmentType() == AttachmentTypes.DOCUMENT) {
                    if (m.getAttachmentType() != AttachmentTypes.IMAGE && !new File(Environment.getExternalStorageDirectory() + "/"
                            +
                            getString(R.string.app_name) + "/" + AttachmentTypes.getTypeName(m.getAttachmentType()) + (myId.equals(m.getSenderId()) ? "/.sent/" : "")
                            , m.getAttachment().getName()).exists()) {
                        continue;
                    }
                    mediaMessages.add(m);
                }
            }
        }
    }

    private void setupViews() {
        appBarLayout.post(new Runnable() {
            @Override
            public void run() {
                setAppBarOffset(GeneralUtils.getDisplayMetrics().widthPixels / 2);
            }
        });

        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        collapsingToolbarLayout.setTitle(" ");
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    userDetailsSummaryContainer.setVisibility(View.INVISIBLE);
                    collapsingToolbarLayout.setTitle(user != null ? user.getNameToDisplay() : group.getName());
                    isShow = true;
                } else if (isShow) {
                    userDetailsSummaryContainer.setVisibility(View.VISIBLE);
                    collapsingToolbarLayout.setTitle(" ");
                    isShow = false;
                }
            }
        });

        userName.setText(user != null ? user.getNameToDisplay() : group.getName());
        setUserData();
    }

    private void setUserData() {
        if (user != null) {
            userName.setCompoundDrawablesWithIntrinsicBounds(user.isOnline() ? R.drawable.ring_green : 0, 0, 0, 0);
        }
        userStatus.setText(user != null ? user.getStatus() : group.getStatus());
        Glide.with(this).load(user != null ? user.getImage() : group.getImage()).apply(new RequestOptions().placeholder(R.drawable.whatsclone_placeholder)).into(userImage);

        if (group != null) {
            userName.setEnabled(true);
            userStatus.setEnabled(true);
            done.setVisibility(View.VISIBLE);
            pickImage.setVisibility(View.VISIBLE);
        } else {
            userName.setEnabled(false);
            userStatus.setEnabled(false);
            done.setVisibility(View.GONE);
            pickImage.setVisibility(View.GONE);
        }
    }

    private void loadFragment(final String fragmentTag) {
        if (getSupportFragmentManager().findFragmentByTag(fragmentTag) != null)
            return;

        handler.post(new Runnable() {
            @Override
            public void run() {
                Fragment fragment = null;
                switch (fragmentTag) {
                    case TAG_DETAIL:
                        if (user != null)
                            fragmentUserDetail = ChatDetailFragment.newInstance(user);
                        else
                            fragmentUserDetail = ChatDetailFragment.newInstance(group);
                        fragment = fragmentUserDetail;
                        break;
                    case TAG_MEDIA:
                        fragment = new UserMediaFragment();
                        break;
                }
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.frameLayout, fragment, fragmentTag);
                if (fragmentTag.equals(TAG_MEDIA)) {
                    fragmentTransaction.addToBackStack(null);
                }
                fragmentTransaction.commit();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

//    @Override
//    public void onBackPressed() {
//        if (getSupportFragmentManager().findFragmentByTag(TAG_DETAIL) == null)
//            loadFragment(TAG_DETAIL);
//        else
//            super.onBackPressed();
//    }

    private void setAppBarOffset(int offsetPx) {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
        behavior.onNestedPreScroll(coordinatorLayout, appBarLayout, null, 0, offsetPx, new int[]{0, 0});
    }

    @Override
    public void getAttachments() {
        ChatDetailFragment fragment = ((ChatDetailFragment) getSupportFragmentManager().findFragmentByTag(TAG_DETAIL));
        if (fragment != null) {
            fragment.setupMediaSummary(mediaMessages);
        }
    }

    @Override
    public ArrayList<Message> getAttachments(int tabPos) {
        if (getSupportFragmentManager().findFragmentByTag(TAG_MEDIA) != null) {
            ArrayList<Message> toReturn = new ArrayList<>();
            switch (tabPos) {
                case 0:
                    for (Message msg : mediaMessages)
                        if (msg.getAttachmentType() == AttachmentTypes.IMAGE || msg.getAttachmentType() == AttachmentTypes.VIDEO)
                            toReturn.add(msg);
                    break;
                case 1:
                    for (Message msg : mediaMessages)
                        if (msg.getAttachmentType() == AttachmentTypes.AUDIO)
                            toReturn.add(msg);
                    break;
                case 2:
                    for (Message msg : mediaMessages)
                        if (msg.getAttachmentType() == AttachmentTypes.DOCUMENT)
                            toReturn.add(msg);
                    break;
            }
            return toReturn;
        } else
            return null;
    }

    @Override
    public void onImagesChosen(List<ChosenImage> list) {
        if (list != null && !list.isEmpty())
            userImageUploadTask(new File(Uri.parse(list.get(0).getOriginalPath()).getPath()), AttachmentTypes.IMAGE, null);
    }

    @Override
    public void onError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // You have to save path in case your activity is killed.
        // In such a scenario, you will need to re-initialize the CameraImagePicker
        outState.putString("picker_path", pickerPath);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // After Activity recreate, you need to re-intialize these
        // two values to be able to re-intialize CameraImagePicker
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("picker_path")) {
                pickerPath = savedInstanceState.getString("picker_path");
            }
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void switchToMediaFragment() {
        appBarLayout.setExpanded(false, true);
        loadFragment(TAG_MEDIA);
    }

    public static Intent newIntent(Context context, User user) {
        Intent intent = new Intent(context, ChatDetailActivity.class);
        intent.putExtra(EXTRA_DATA_USER, user);
        return intent;
    }

    public static Intent newIntent(Context context, Group group) {
        Intent intent = new Intent(context, ChatDetailActivity.class);
        intent.putExtra(EXTRA_DATA_GROUP, group);
        return intent;
    }
}
