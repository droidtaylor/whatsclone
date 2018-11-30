package com.verbosetech.whatsclone.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.SiliCompressor;
import com.kbeanie.multipicker.api.AudioPicker;
import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.FilePicker;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.VideoPicker;
import com.kbeanie.multipicker.api.callbacks.AudioPickerCallback;
import com.kbeanie.multipicker.api.callbacks.FilePickerCallback;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.callbacks.VideoPickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenAudio;
import com.kbeanie.multipicker.api.entity.ChosenFile;
import com.kbeanie.multipicker.api.entity.ChosenImage;
import com.kbeanie.multipicker.api.entity.ChosenVideo;
import com.sinch.android.rtc.calling.Call;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.verbosetech.whatsclone.R;
import com.verbosetech.whatsclone.adapters.MessageAdapter;
import com.verbosetech.whatsclone.interfaces.OnMessageItemClick;
import com.verbosetech.whatsclone.models.Attachment;
import com.verbosetech.whatsclone.models.AttachmentTypes;
import com.verbosetech.whatsclone.models.Chat;
import com.verbosetech.whatsclone.models.Contact;
import com.verbosetech.whatsclone.models.DownloadFileEvent;
import com.verbosetech.whatsclone.models.Group;
import com.verbosetech.whatsclone.models.Message;
import com.verbosetech.whatsclone.models.MyString;
import com.verbosetech.whatsclone.models.User;
import com.verbosetech.whatsclone.utils.ConfirmationDialogFragment;
import com.verbosetech.whatsclone.utils.Helper;
import com.verbosetech.whatsclone.utils.DownloadUtil;
import com.verbosetech.whatsclone.utils.FileUtils;
import com.verbosetech.whatsclone.utils.KeyboardUtil;
import com.verbosetech.whatsclone.viewHolders.BaseMessageViewHolder;
import com.verbosetech.whatsclone.viewHolders.MessageAttachmentRecordingViewHolder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class ChatActivity extends BaseActivity implements OnMessageItemClick, MessageAttachmentRecordingViewHolder.RecordingViewInteractor, View.OnClickListener, ImagePickerCallback, FilePickerCallback, AudioPickerCallback, VideoPickerCallback {
    private static final int REQUEST_CODE_CONTACT = 1;
    private static final int REQUEST_PLACE_PICKER = 2;
    private static final int REQUEST_CODE_PLAY_SERVICES = 3;
    private static final int REQUEST_CODE_UPDATE_USER = 753;
    private static final int REQUEST_CODE_UPDATE_GROUP = 357;
    private static final int REQUEST_PERMISSION_RECORD = 159;
    private static final int REQUEST_PERMISSION_CALL = 951;
    private static String EXTRA_DATA_GROUP = "extradatagroup";
    private static String EXTRA_DATA_USER = "extradatauser";
    private static String EXTRA_DATA_CHAT_GROUP = "extradatagroupchat";
    private static String EXTRA_DATA_CHAT_ID = "extradatachatid";
    private static String EXTRA_DATA_CHAT_NAME = "extradatachatname";
    private static String EXTRA_DATA_LIST = "extradatalist";
    private static String DELETE_TAG = "deletetag";
    private MessageAdapter messageAdapter;
    private ArrayList<Message> dataList = new ArrayList<>();
    private RealmResults<Chat> queryResult;
    private String chatChild, userOrGroupId;
    private int countSelected = 0;

    private Handler recordWaitHandler, recordTimerHandler;
    private Runnable recordRunnable, recordTimerRunnable;
    private MediaRecorder mRecorder = null;
    private String recordFilePath;
    private float displayWidth;
    private boolean callIsVideo;

    private ArrayList<Integer> adapterPositions = new ArrayList<>();

    private MediaPlayer mediaPlayer = new MediaPlayer();
    private String currentlyPlaying = "";

    private Toolbar toolbar;
    private RelativeLayout toolbarContent;
    private TextView selectedCount, status, userName;
    private TableLayout addAttachmentLayout;
    private RecyclerView recyclerView;
    private EmojiEditText newMessage;
    private ImageView usersImage, addAttachment, sendMessage, attachment_emoji;
    private LinearLayout rootView, sendContainer;
    private ImageView callAudio, callVideo;

    private String cameraPhotoPath;
    private EmojiPopup emojIcon;

    private String pickerPath;
    private ImagePicker imagePicker;
    private CameraImagePicker cameraPicker;
    private FilePicker filePicker;
    private AudioPicker audioPicker;
    private VideoPicker videoPicker;

    //Download complete listener
    private BroadcastReceiver downloadCompleteReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null)
                switch (intent.getAction()) {
                    case DownloadManager.ACTION_DOWNLOAD_COMPLETE:
                        if (adapterPositions.size() > 0 && messageAdapter != null)
                            for (int pos : adapterPositions)
                                if (pos != -1)
                                    messageAdapter.notifyItemChanged(pos);
                        adapterPositions.clear();
                        break;
                }
        }
    };

    //Download event listener
    private BroadcastReceiver downloadEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadFileEvent downloadFileEvent = intent.getParcelableExtra("data");
            if (downloadFileEvent != null) {
                downloadFile(downloadFileEvent);
            }
        }
    };

    @Override
    void myUsersResult(ArrayList<User> myUsers) {

    }

    @Override
    void myContactsResult(HashMap<String, Contact> myContacts) {

    }

    @Override
    void userAdded(User valueUser) {
        //do nothing
    }

    @Override
    void groupAdded(Group valueGroup) {
        //do nothing
    }

    @Override
    void userUpdated(User valueUser) {
        if (user != null && user.getId().equals(valueUser.getId())) {
            valueUser.setNameInPhone(user.getNameInPhone());
            user = valueUser;
            Glide.with(this).load(user.getImage()).apply(new RequestOptions().placeholder(R.drawable.whatsclone_placeholder)).into(usersImage);
            //userName.setCompoundDrawablesWithIntrinsicBounds(user.isOnline() ? R.drawable.ring_green : 0, 0, 0, 0);
            status.setText(user.getStatus());
            status.setSelected(true);
            showTyping(user.isTyping());//Show typing
        }
    }

    @Override
    void groupUpdated(Group valueGroup) {
        if (group != null && group.getId().equals(valueGroup.getId())) {
            group = valueGroup;
            checkIfChatAllowed();
        }
    }

    @Override
    void onSinchConnected() {
        callAudio.setClickable(true);
        callVideo.setClickable(true);
    }

    @Override
    void onSinchDisconnected() {
        callAudio.setClickable(false);
        callVideo.setClickable(false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_DATA_CHAT_ID)) {
            String chatId = intent.getStringExtra(EXTRA_DATA_CHAT_ID);
            if (chatId.startsWith(Helper.GROUP_PREFIX)) {
                group = intent.getParcelableExtra(EXTRA_DATA_CHAT_GROUP);
                if (group == null) {
                    group = rChatDb.copyFromRealm(rChatDb.where(Group.class).equalTo("id", chatId).findFirst());
                    Helper.CURRENT_CHAT_ID = group.getId();
                }
            } else {
                user = rChatDb.copyFromRealm(rChatDb.where(User.class).equalTo("id", chatId).findFirst());
                String chatName = intent.getStringExtra(EXTRA_DATA_CHAT_NAME);
                if (!TextUtils.isEmpty(chatName) && TextUtils.isEmpty(user.getNameInPhone())) {
                    user.setNameInPhone(chatName);
                }
                Helper.CURRENT_CHAT_ID = user.getId();
            }
        } else {
            finish();//temporary fix
        }

        initUi();

        //set basic user info
        String nameText = null, statusText = null, imageUrl = null;
        if (user != null) {
            nameText = user.getNameToDisplay();
            statusText = user.getStatus();
            imageUrl = user.getImage();
        } else if (group != null) {
            nameText = group.getName();
            statusText = group.getStatus();
            imageUrl = group.getImage();
        }
        userName.setText(nameText);
        status.setText(statusText);
        userName.setSelected(true);
        status.setSelected(true);
        Glide.with(this).load(imageUrl).apply(new RequestOptions().placeholder(R.drawable.whatsclone_placeholder)).into(usersImage);

        callAudio.setClickable(false);
        callVideo.setClickable(false);

        animateToolbarViews();

        //setup chat child reference of logged in user and selected user
        chatChild = user != null ? Helper.getChatChild(user.getId(), userMe.getId()) : group.getId();
        userOrGroupId = user != null ? user.getId() : group.getId();

        //setup recycler view
        messageAdapter = new MessageAdapter(this, dataList, userMe.getId(), newMessage);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);
        recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    recyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);
                        }
                    }, 100);
                }
            }
        });

        emojIcon = EmojiPopup.Builder.fromRootView(rootView).setOnEmojiPopupShownListener(new OnEmojiPopupShownListener() {
            @Override
            public void onEmojiPopupShown() {
                if (addAttachmentLayout.getVisibility() == View.VISIBLE) {
                    addAttachmentLayout.setVisibility(View.GONE);
                    addAttachment.animate().setDuration(400).rotationBy(-45).start();
                }
            }
        }).build(newMessage);

        displayWidth = Helper.getDisplayWidth(this);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                notifyRecordingPlaybackCompletion();
            }
        });

        //Query out chat from existing chats whose owner is logged in user and the user is selected user
        RealmQuery<Chat> query = Helper.getChat(rChatDb, userMe.getId(), userOrGroupId);//rChatDb.where(Chat.class).equalTo("myId", userMe.getId()).equalTo("userId", user.getId());
        queryResult = query.findAll();
        queryResult.addChangeListener(realmChangeListener);//register change listener
        Chat prevChat = query.findFirst();
        //Add all messages from queried chat into recycler view
        if (prevChat != null) {
            dataList.addAll(prevChat.getMessages());
            messageAdapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);
        }
        registerUserUpdates();
        checkAndForward();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initUi() {
        toolbar = findViewById(R.id.chatToolbar);
        toolbarContent = findViewById(R.id.chatToolbarContent);
        selectedCount = findViewById(R.id.selectedCount);
        addAttachmentLayout = findViewById(R.id.add_attachment_layout);
        usersImage = findViewById(R.id.users_image);
        status = findViewById(R.id.emotion);
        userName = findViewById(R.id.user_name);
        recyclerView = findViewById(R.id.recycler_view);
        newMessage = findViewById(R.id.new_message);
        addAttachment = findViewById(R.id.add_attachment);
        sendMessage = findViewById(R.id.send);
        sendContainer = findViewById(R.id.sendContainer);
        rootView = findViewById(R.id.rootView);
        attachment_emoji = findViewById(R.id.attachment_emoji);
        callAudio = findViewById(R.id.callAudio);
        callVideo = findViewById(R.id.callVideo);

        callAudio.setVisibility(user != null && group == null ? View.VISIBLE : View.GONE);
        callVideo.setVisibility(user != null && group == null ? View.VISIBLE : View.GONE);

        setSupportActionBar(toolbar);
        addAttachment.setOnClickListener(this);
        toolbarContent.setOnClickListener(this);
        attachment_emoji.setOnClickListener(this);
        sendMessage.setOnClickListener(this);
        callAudio.setOnClickListener(this);
        callVideo.setOnClickListener(this);
        findViewById(R.id.back_button).setOnClickListener(this);
        findViewById(R.id.attachment_video).setOnClickListener(this);
        findViewById(R.id.attachment_contact).setOnClickListener(this);
        findViewById(R.id.attachment_gallery).setOnClickListener(this);
        findViewById(R.id.attachment_audio).setOnClickListener(this);
        findViewById(R.id.attachment_location).setOnClickListener(this);
        findViewById(R.id.attachment_document).setOnClickListener(this);
        newMessage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (addAttachmentLayout.getVisibility() == View.VISIBLE) {
                    addAttachmentLayout.setVisibility(View.GONE);
                    addAttachment.animate().setDuration(400).rotationBy(-45).start();
                }
                return false;
            }
        });
        sendMessage.setOnTouchListener(voiceMessageListener);
    }

    private View.OnTouchListener voiceMessageListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.i("TAG", "touched down");
                    if (newMessage.getText().toString().trim().isEmpty()) {
                        if (recordWaitHandler == null)
                            recordWaitHandler = new Handler();
                        recordRunnable = new Runnable() {
                            @Override
                            public void run() {
                                recordingStart();
                            }
                        };
                        recordWaitHandler.postDelayed(recordRunnable, 600);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.i("TAG", "moving: (" + displayWidth + ", " + x + ")");
                    if (mRecorder != null && newMessage.getText().toString().trim().isEmpty()) {
                        if (Math.abs(event.getX()) / displayWidth > 0.35f) {
                            recordingStop(false);
                            Toast.makeText(ChatActivity.this, R.string.recording_cancelled, Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    Log.i("TAG", "touched up");
                    if (recordWaitHandler != null && newMessage.getText().toString().trim().isEmpty())
                        recordWaitHandler.removeCallbacks(recordRunnable);
                    if (mRecorder != null && newMessage.getText().toString().trim().isEmpty()) {
                        recordingStop(true);
                    }
                    break;
            }
            return false;
        }
    };

    private void recordingStop(boolean send) {
        try {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        } catch (IllegalStateException ex) {
            mRecorder = null;
        } catch (RuntimeException ex) {
            mRecorder = null;
        }
        recordTimerStop();
        if (send) {
            newFileUploadTask(recordFilePath, AttachmentTypes.RECORDING, null);
        } else {
            new File(recordFilePath).delete();
        }
    }

    private void recordingStart() {
        if (recordPermissionsAvailable()) {
            File recordFile = new File(Environment.getExternalStorageDirectory(), "/" + getString(R.string.app_name) + "/" + AttachmentTypes.getTypeName(AttachmentTypes.RECORDING) + "/.sent/");
            boolean dirExists = recordFile.exists();
            if (!dirExists)
                dirExists = recordFile.mkdirs();
            if (dirExists) {
                try {
                    recordFile = new File(Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name) + "/" + AttachmentTypes.getTypeName(AttachmentTypes.RECORDING) + "/.sent/", System.currentTimeMillis() + ".mp3");
                    if (!recordFile.exists())
                        recordFile.createNewFile();
                    recordFilePath = recordFile.getAbsolutePath();
                    mRecorder = new MediaRecorder();
                    mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                    mRecorder.setOutputFile(recordFilePath);
                    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                    mRecorder.prepare();
                    mRecorder.start();
                    recordTimerStart(System.currentTimeMillis());
                } catch (IOException e) {
                    e.printStackTrace();
                    mRecorder = null;
                } catch (IllegalStateException ex) {
                    ex.printStackTrace();
                    mRecorder = null;
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, permissionsRecord, REQUEST_PERMISSION_RECORD);
        }
    }

    private void recordTimerStart(final long currentTimeMillis) {
        Toast.makeText(this, R.string.recodring, Toast.LENGTH_SHORT).show();
        recordTimerRunnable = new Runnable() {
            public void run() {
                Long elapsedTime = System.currentTimeMillis() - currentTimeMillis;
                newMessage.setHint(String.format(getString(R.string.slide_cancel), Helper.timeFormater(elapsedTime)));
                recordTimerHandler.postDelayed(this, 1000);
            }
        };
        if (recordTimerHandler == null)
            recordTimerHandler = new Handler();
        recordTimerHandler.post(recordTimerRunnable);
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) v.vibrate(100);
    }

    private void recordTimerStop() {
        recordTimerHandler.removeCallbacks(recordTimerRunnable);
        newMessage.setHint(getString(R.string.type_your_message));
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) v.vibrate(100);
    }

    private boolean recordPermissionsAvailable() {
        boolean available = true;
        for (String permission : permissionsRecord) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                available = false;
                break;
            }
        }
        return available;
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(downloadCompleteReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        LocalBroadcastManager.getInstance(this).registerReceiver(downloadEventReceiver, new IntentFilter(Helper.BROADCAST_DOWNLOAD_EVENT));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(downloadCompleteReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(downloadEventReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mediaPlayer.release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Helper.CHAT_CAB)
            undoSelectionPrepared();
        queryResult.removeChangeListener(realmChangeListener);
        markAllReadForThisUser();
        Helper.CURRENT_CHAT_ID = null;
    }

    @Override
    public void onBackPressed() {
        if (Helper.CHAT_CAB)
            undoSelectionPrepared();
        else {
            KeyboardUtil.getInstance(this).closeKeyboard();
            if (Build.VERSION.SDK_INT > 21) {
                finishAfterTransition();
            } else {
                finish();
            }
        }
    }

    private void markAllReadForThisUser() {
        Chat thisChat = Helper.getChat(rChatDb, userMe.getId(), userOrGroupId).findFirst();
        if (thisChat != null) {
            rChatDb.beginTransaction();
            thisChat.setRead(true);
            rChatDb.commitTransaction();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.action_copy:
                StringBuilder stringBuilder = new StringBuilder("");
                for (Message message : dataList) {//Get all selected messages in a String
                    if (message.isSelected() && !TextUtils.isEmpty(message.getBody())) {
                        stringBuilder.append(Helper.getTime(message.getDate()));
                        stringBuilder.append(" ");
                        if (message.getSenderId() != null && user != null && userMe != null && user.getId() != null && user.getName() != null && userMe.getName() != null)
                            stringBuilder.append(message.getSenderId().equals(user.getId()) ? user.getName() : userMe.getName());
                        stringBuilder.append(" : ");
                        stringBuilder.append(message.getBody());
                        stringBuilder.append("\n");
                    }
                }
                //Add String in clipboard
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("simple text", stringBuilder.toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, R.string.message_copied, Toast.LENGTH_SHORT).show();
                undoSelectionPrepared();
                break;
            case R.id.action_delete:
                FragmentManager manager = getSupportFragmentManager();
                Fragment frag = manager.findFragmentByTag(DELETE_TAG);
                if (frag != null) {
                    manager.beginTransaction().remove(frag).commit();
                }

                ConfirmationDialogFragment confirmationDialogFragment = ConfirmationDialogFragment.newInstance(getString(R.string.delete_msg_title),
                        getString(R.string.delete_msg_message),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ArrayList<String> idsToDelete = new ArrayList<>();
                                for (Message msg : dataList) {//Delete all selected messages
                                    if (msg.isSelected()) {
                                        try {
                                            chatRef.child(chatChild).child(msg.getId()).removeValue();
                                        } catch (DatabaseException de) {
                                            Log.e("DatabaseException", de.getMessage());
                                            if (msg.getAttachment() != null) {
                                                String idToCompare = "loading" + msg.getAttachment().getBytesCount() + msg.getAttachment().getName();
                                                idsToDelete.add(idToCompare);
                                            }
                                        }
                                    }
                                }
                                for (String idToCompare : idsToDelete) {
                                    Helper.deleteMessageFromRealm(rChatDb, idToCompare);
                                }
                                toolbar.getMenu().clear();
                                selectedCount.setVisibility(View.GONE);
                                toolbarContent.setVisibility(View.VISIBLE);
                                Helper.CHAT_CAB = false;
                            }
                        },
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                undoSelectionPrepared();
                            }
                        });
                confirmationDialogFragment.show(manager, DELETE_TAG);
                break;
            case R.id.action_forward:
                ArrayList<Message> forwardList = new ArrayList<>();
                for (Message msg : dataList)
                    if (msg.isSelected())
                        forwardList.add(rChatDb.copyFromRealm(msg));
                Intent resultIntent = new Intent();
                resultIntent.putParcelableArrayListExtra("FORWARD_LIST", forwardList);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
                //undoSelectionPrepared();
                break;
        }
        return true;
    }

    private void registerUserUpdates() {
        //Publish logged in user's typing status
        newMessage.addTextChangedListener(new TextWatcher() {
            CountDownTimer timer = null;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sendMessage.setImageDrawable(ContextCompat.getDrawable(ChatActivity.this, s.length() == 0 ? R.drawable.ic_keyboard_voice_24dp : R.drawable.ic_send));
                if (user != null) {
                    if (timer != null) {
                        timer.cancel();
                        usersRef.child(userMe.getId()).child("typing").setValue(true);
                    }
                    timer = new CountDownTimer(1500, 1000) {
                        public void onTick(long millisUntilFinished) {
                        }

                        public void onFinish() {
                            usersRef.child(userMe.getId()).child("typing").setValue(false);
                        }
                    }.start();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private boolean checkIfChatAllowed() {
        if (group == null)
            return true;
        boolean allowed = group.getUserIds().contains(new MyString(userMe.getId()));
        if (!allowed) {
            sendContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.bg_gray));
            newMessage.setText("");
            newMessage.setHint(R.string.removed_from_group);
            newMessage.setEnabled(false);
            addAttachment.setClickable(false);
            sendMessage.setClickable(false);
        }
        return allowed;
    }

    private void checkAndForward() {
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_DATA_LIST) && checkIfChatAllowed()) {
            ArrayList<Message> toForward = intent.getParcelableArrayListExtra(EXTRA_DATA_LIST);
            if (!toForward.isEmpty()) {
                for (Message msg : toForward)
                    sendMessage(msg.getBody(), msg.getAttachmentType(), msg.getAttachment());
            }
        }
    }

    private void showTyping(boolean typing) {
        if (dataList != null && dataList.size() > 0 && RealmObject.isValid(dataList.get(dataList.size() - 1))) {
            boolean lastIsTyping = dataList.get(dataList.size() - 1).getAttachmentType() == AttachmentTypes.NONE_TYPING;
            if (typing && !lastIsTyping) {//if last message is not Typing
                dataList.add(new Message(AttachmentTypes.NONE_TYPING));
                messageAdapter.notifyItemInserted(dataList.size() - 1);
                recyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);
            } else if (lastIsTyping && dataList.size() > 0) {//If last is typing and there is a message in list
                dataList.remove(dataList.size() - 1);
                messageAdapter.notifyItemRemoved(dataList.size());
            }
        }
    }

    private void animateToolbarViews() {
        Animation emotionAnimation = AnimationUtils.makeInChildBottomAnimation(this);
        emotionAnimation.setDuration(400);
        status.startAnimation(emotionAnimation);
        Animation nameAnimation = AnimationUtils.makeInChildBottomAnimation(this);
        nameAnimation.setDuration(420);
        userName.startAnimation(nameAnimation);
    }

    private RealmChangeListener<RealmResults<Chat>> realmChangeListener = new RealmChangeListener<RealmResults<Chat>>() {
        @Override
        public void onChange(RealmResults<Chat> element) {
            if (element != null && element.isValid() && element.size() > 0) {
                RealmList<Message> updatedList = element.get(0).getMessages();//updated list of messages
                if (updatedList.size() < dataList.size()) {//if updated items after deletion
                    dataList.clear();
                    dataList.addAll(element.get(0).getMessages());
                    messageAdapter.notifyDataSetChanged();
                } else {// either new or updated message items
                    showTyping(false);//hide typing indicator
                    int lastPos = dataList.size() - 1;
                    Message newMessage = updatedList.get(updatedList.size() - 1);
                    if (lastPos >= 0 && dataList.get(lastPos).getId().equals(newMessage.getId())) {//Updated message
                        dataList.set(lastPos, newMessage);
                        messageAdapter.notifyItemChanged(lastPos);
                    } else {//new message
                        dataList.add(newMessage);
                        messageAdapter.notifyItemInserted(lastPos + 1);
                    }
                    recyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);//scroll to latest message
                }
            }
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_button:
                Helper.closeKeyboard(this, view);
                onBackPressed();
                break;
            case R.id.add_attachment:
                Helper.closeKeyboard(this, view);
                if (addAttachmentLayout.getVisibility() == View.VISIBLE) {
                    addAttachmentLayout.setVisibility(View.GONE);
                    addAttachment.animate().setDuration(400).rotationBy(-45).start();
                } else {
                    addAttachmentLayout.setVisibility(View.VISIBLE);
                    addAttachment.animate().setDuration(400).rotationBy(45).start();
                    emojIcon.dismiss();
                }
                break;
            case R.id.send:
                if (!TextUtils.isEmpty(newMessage.getText().toString().trim())) {
                    sendMessage(newMessage.getText().toString(), AttachmentTypes.NONE_TEXT, null);
                    newMessage.setText("");
                }
                break;
            case R.id.chatToolbarContent:
                if (toolbarContent.getVisibility() == View.VISIBLE) {
                    if (user != null)
                        startActivityForResult(ChatDetailActivity.newIntent(this, user), REQUEST_CODE_UPDATE_USER);
                    else if (group != null)
                        startActivityForResult(ChatDetailActivity.newIntent(this, group), REQUEST_CODE_UPDATE_GROUP);
                }
                break;
            case R.id.attachment_contact:
                openContactPicker();
                break;
//            case R.id.attachment_camera:
//                openImageClick();
//                break;
            case R.id.attachment_emoji:
                emojIcon.toggle();
                break;
            case R.id.attachment_gallery:
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
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
                break;
            case R.id.attachment_audio:
                openAudioPicker();
                break;
            case R.id.attachment_location:
                openPlacePicker();
                break;
            case R.id.attachment_video:
                openVideoPicker();
                break;
            case R.id.attachment_document:
                openDocumentPicker();
                break;
            case R.id.callVideo:
                callIsVideo = true;
                placeCall();
                break;
            case R.id.callAudio:
                callIsVideo = false;
                placeCall();
                break;
        }
    }

    private void placeCall() {
        if (permissionsAvailable(permissionsSinch)) {
            try {
                Call call = callIsVideo ? getSinchServiceInterface().callUserVideo(user.getId()) : getSinchServiceInterface().callUser(user.getId());
                if (call == null) {
                    // Service failed for some reason, show a Toast and abort
                    Toast.makeText(this, "Service is not started. Try stopping the service and starting it again before placing a call.", Toast.LENGTH_LONG).show();
                    return;
                }
                String callId = call.getCallId();
                startActivity(CallScreenActivity.newIntent(this, user, callId, "OUT"));
            } catch (Exception e) {
                Log.e("CHECK", e.getMessage());
                //ActivityCompat.requestPermissions(this, new String[]{e.getRequiredPermission()}, 0);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissionsSinch, 69);
        }
    }

    private void openDetails() {
        if (toolbarContent.getVisibility() == View.VISIBLE) {
            if (user != null)
                startActivityForResult(ChatDetailActivity.newIntent(this, user), REQUEST_CODE_UPDATE_USER);
            else if (group != null)
                startActivityForResult(ChatDetailActivity.newIntent(this, group), REQUEST_CODE_UPDATE_GROUP);
        }
    }

    private void prepareMessage(String body, int attachmentType, Attachment attachment) {
        Message message = new Message();
        message.setAttachmentType(attachmentType);
        message.setAttachment(attachment);
        message.setBody(body);
        message.setDate(System.currentTimeMillis());
        message.setSenderId(userMe.getId());
        message.setSenderName(userMe.getName());
        message.setSenderStatus(userMe.getStatus());
        message.setSenderImage(userMe.getImage());
        message.setSent(false);
        message.setDelivered(false);
        message.setRecipientId(userOrGroupId);
        message.setRecipientGroupIds(group != null ? new ArrayList<MyString>(group.getUserIds()) : null);
        message.setRecipientName(user != null ? user.getName() : group.getName());
        message.setRecipientImage(user != null ? user.getImage() : group.getImage());
        message.setRecipientStatus(user != null ? user.getStatus() : group.getStatus());
        message.setId(attachment.getUrl() + attachment.getBytesCount() + attachment.getName());

        Helper.deleteMessageFromRealm(rChatDb, message.getId());

        //Loading attachment message

        String userId = message.getRecipientId();
        String myId = message.getSenderId();
        Chat chat = Helper.getChat(rChatDb, myId, userId).findFirst();//rChatDb.where(Chat.class).equalTo("myId", myId).equalTo("userId", userId).findFirst();
        rChatDb.beginTransaction();
        if (chat == null) {
            chat = rChatDb.createObject(Chat.class);
            chat.setMessages(new RealmList<Message>());
            chat.setLastMessage(message.getBody());
            chat.setMyId(myId);
            chat.setTimeUpdated(message.getDate());
            chat.setChatName(user != null ? user.getNameToDisplay() : group.getName());
            chat.setChatStatus(user != null ? user.getStatus() : group.getStatus());
            chat.setChatImage(user != null ? user.getImage() : group.getImage());
            chat.setChatId(user != null ? userId : group.getId());
            chat.setGroup(group != null);
        }
        chat.setTimeUpdated(message.getDate());
        chat.getMessages().add(message);
        chat.setLastMessage(message.getBody());
        rChatDb.commitTransaction();
    }

    private void sendMessage(String messageBody, @AttachmentTypes.AttachmentType int attachmentType, Attachment attachment) {
        //Create message object
        Message message = new Message();
        message.setAttachmentType(attachmentType);
        if (attachmentType != AttachmentTypes.NONE_TEXT)
            message.setAttachment(attachment);
        else
            BaseMessageViewHolder.animate = true;
        message.setBody(messageBody);
        message.setDate(System.currentTimeMillis());
        message.setSenderId(userMe.getId());
        message.setSenderName(userMe.getName());
        message.setSenderStatus(userMe.getStatus());
        message.setSenderImage(userMe.getImage());
        message.setSent(true);
        message.setDelivered(false);
        message.setRecipientId(userOrGroupId);
        message.setRecipientGroupIds(group != null ? new ArrayList<MyString>(group.getUserIds()) : null);
        message.setRecipientName(user != null ? user.getName() : group.getName());
        message.setRecipientImage(user != null ? user.getImage() : group.getImage());
        message.setRecipientStatus(user != null ? user.getStatus() : group.getStatus());
        message.setId(chatRef.child(chatChild).push().getKey());

        //Add message in chat child
        chatRef.child(chatChild).child(message.getId()).setValue(message);
        //Add message in recipient's inbox
        inboxRef.child(userOrGroupId).setValue(message);
    }

    private void checkAndCopy(String directory, File source) {
        //Create and copy file content
        File file = new File(Environment.getExternalStorageDirectory(), directory);
        boolean dirExists = file.exists();
        if (!dirExists)
            dirExists = file.mkdirs();
        if (dirExists) {
            try {
                file = new File(Environment.getExternalStorageDirectory() + directory, Uri.fromFile(source).getLastPathSegment());
                boolean fileExists = file.exists();
                if (!fileExists)
                    fileExists = file.createNewFile();
                if (fileExists && file.length() == 0) {
                    FileUtils.copyFile(source, file);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void openPlacePicker() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), REQUEST_PLACE_PICKER);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
            googleApiAvailability.showErrorDialogFragment(this, googleApiAvailability.isGooglePlayServicesAvailable(this), REQUEST_CODE_PLAY_SERVICES);
            e.printStackTrace();
        }
    }

    void openContactPicker() {
        if (permissionsAvailable(permissionsContact)) {
            Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(contactPickerIntent, REQUEST_CODE_CONTACT);
        } else {
            ActivityCompat.requestPermissions(this, permissionsContact, 14);
        }
    }

    void openAudioPicker() {
        if (permissionsAvailable(permissionsStorage)) {
            audioPicker = new AudioPicker(this);
            audioPicker.setAudioPickerCallback(this);
            audioPicker.pickAudio();
        } else {
            ActivityCompat.requestPermissions(this, permissionsStorage, 25);
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

    public void openDocumentPicker() {
        if (permissionsAvailable(permissionsStorage)) {
            filePicker = new FilePicker(this);
            filePicker.setFilePickerCallback(this);
            filePicker.setMimeType("application/pdf");
            filePicker.pickFile();
        } else {
            ActivityCompat.requestPermissions(this, permissionsStorage, 58);
        }
    }

    private void openVideoPicker() {
        if (permissionsAvailable(permissionsStorage)) {
            videoPicker = new VideoPicker(this);
            videoPicker.shouldGenerateMetadata(true);
            videoPicker.shouldGeneratePreviewImages(true);
            videoPicker.setVideoPickerCallback(this);
            videoPicker.pickVideo();
        } else {
            ActivityCompat.requestPermissions(this, permissionsStorage, 41);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 14:
                if (permissionsAvailable(permissions))
                    openContactPicker();
                break;
            case 25:
                if (permissionsAvailable(permissions))
                    openAudioPicker();
                break;
            case 36:
                if (permissionsAvailable(permissions))
                    openImagePick();
                break;
            case 47:
                if (permissionsAvailable(permissions))
                    openImageClick();
                break;
            case 58:
                if (permissionsAvailable(permissions))
                    openDocumentPicker();
                break;
            case 69:
                if (permissionsAvailable(permissions))
                    placeCall();
                break;
            case 41:
                if (permissionsAvailable(permissions))
                    openVideoPicker();
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
                case Picker.PICK_VIDEO_DEVICE:
                    if (videoPicker == null) {
                        videoPicker = new VideoPicker(this);
                        videoPicker.setVideoPickerCallback(this);
                    }
                    videoPicker.submit(data);
                    break;
                case Picker.PICK_FILE:
                    filePicker.submit(data);
                    break;
                case Picker.PICK_AUDIO:
                    audioPicker.submit(data);
                    break;
            }
        }
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_UPDATE_USER:
                    user = data.getParcelableExtra(EXTRA_DATA_USER);
                    userUpdated(user);
                    break;
                case REQUEST_CODE_UPDATE_GROUP:
                    group = data.getParcelableExtra(EXTRA_DATA_GROUP);
                    groupUpdated(group);
                    break;
                case REQUEST_CODE_CONTACT:
                    getSendVCard(data.getData());
                    break;
                case REQUEST_PLACE_PICKER:
                    Place place = PlacePicker.getPlace(this, data);
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("address", place.getAddress().toString());
                        jsonObject.put("latitude", place.getLatLng().latitude);
                        jsonObject.put("longitude", place.getLatLng().longitude);
                        Attachment attachment = new Attachment();
                        attachment.setData(jsonObject.toString());
                        sendMessage(null, AttachmentTypes.LOCATION, attachment);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case REQUEST_CODE_PLAY_SERVICES:
                    openPlacePicker();
                    break;
            }
        }
    }

    private void getSendVCard(Uri contactsData) {
        @SuppressLint("StaticFieldLeak") AsyncTask<Cursor, Void, File> task = new AsyncTask<Cursor, Void, File>() {
            String vCardData;

            @Override
            protected File doInBackground(Cursor... params) {
                Cursor cursor = params[0];
                File toSend = new File(Environment.getExternalStorageDirectory(), "/" + getString(R.string.app_name) + "/Contact/.sent/");
                if (cursor != null && !cursor.isClosed()) {
                    cursor.getCount();
                    if (cursor.moveToFirst()) {
                        String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                        String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey);
                        try {
                            AssetFileDescriptor assetFileDescriptor = getContentResolver().openAssetFileDescriptor(uri, "r");
                            if (assetFileDescriptor != null) {
                                FileInputStream inputStream = assetFileDescriptor.createInputStream();
                                boolean dirExists = toSend.exists();
                                if (!dirExists)
                                    dirExists = toSend.mkdirs();
                                if (dirExists) {
                                    try {
                                        toSend = new File(Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name) + "/Contact/.sent/", name + ".vcf");
                                        boolean fileExists = toSend.exists();
                                        if (!fileExists)
                                            fileExists = toSend.createNewFile();
                                        if (fileExists) {
                                            OutputStream stream = new BufferedOutputStream(new FileOutputStream(toSend, false));
                                            byte[] buffer = readAsByteArray(inputStream);
                                            vCardData = new String(buffer);
                                            stream.write(buffer);
                                            stream.close();
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } catch (FileNotFoundException e) {
                            Log.e(ChatActivity.class.getSimpleName(), "Vcard for the contact " + lookupKey + " not found", e);
                        } catch (IOException e) {
                            Log.e(ChatActivity.class.getSimpleName(), "Problem creating stream from the assetFileDescriptor.", e);
                        } finally {
                            cursor.close();
                        }
                    }
                }
                return toSend;
            }

            @Override
            protected void onPostExecute(File f) {
                super.onPostExecute(f);
                if (f != null && !TextUtils.isEmpty(vCardData)) {
                    Attachment attachment = new Attachment();
                    attachment.setData(vCardData);
                    newFileUploadTask(f.getAbsolutePath(), AttachmentTypes.CONTACT, attachment);
                }
            }
        };
        task.execute(getContentResolver().query(contactsData, null, null, null, null));
    }

    public byte[] readAsByteArray(InputStream ios) throws IOException {
        ByteArrayOutputStream ous = null;
        try {
            byte[] buffer = new byte[4096];
            ous = new ByteArrayOutputStream();
            int read = 0;
            while ((read = ios.read(buffer)) != -1) {
                ous.write(buffer, 0, read);
            }
        } finally {
            try {
                if (ous != null)
                    ous.close();
            } catch (IOException e) {
            }

            try {
                if (ios != null)
                    ios.close();
            } catch (IOException e) {
            }
        }
        return ous.toByteArray();
    }

    private void uploadImage(String filePath) {
        newFileUploadTask(filePath, AttachmentTypes.IMAGE, null);
    }

    private void uploadThumbnail(final String filePath) {
        Toast.makeText(this, R.string.just_moment, Toast.LENGTH_LONG).show();
        File file = new File(filePath);
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child(getString(R.string.app_name)).child("video").child("thumbnail").child(file.getName() + ".jpg");
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //If thumbnail exists
                Attachment attachment = new Attachment();
                attachment.setData(uri.toString());
                newFileUploadTask(filePath, AttachmentTypes.VIDEO, attachment);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                @SuppressLint("StaticFieldLeak") AsyncTask<String, Void, Bitmap> thumbnailTask = new AsyncTask<String, Void, Bitmap>() {
                    @Override
                    protected Bitmap doInBackground(String... params) {
                        //Create thumbnail
                        return ThumbnailUtils.createVideoThumbnail(params[0], MediaStore.Video.Thumbnails.MINI_KIND);
                    }

                    @Override
                    protected void onPostExecute(Bitmap bitmap) {
                        super.onPostExecute(bitmap);
                        if (bitmap != null) {
                            //Upload thumbnail and then upload video
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] data = baos.toByteArray();
                            UploadTask uploadTask = storageReference.putBytes(data);
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
                                        Attachment attachment = new Attachment();
                                        attachment.setData(downloadUri.toString());
                                        newFileUploadTask(filePath, AttachmentTypes.VIDEO, attachment);
                                    } else {
                                        newFileUploadTask(filePath, AttachmentTypes.VIDEO, null);
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    newFileUploadTask(filePath, AttachmentTypes.VIDEO, null);
                                }
                            });
                        } else
                            newFileUploadTask(filePath, AttachmentTypes.VIDEO, null);
                    }
                };
                thumbnailTask.execute(filePath);
            }
        });
    }

    private void newFileUploadTask(String filePath,
                                   @AttachmentTypes.AttachmentType final int attachmentType, final Attachment attachment) {
        if (addAttachmentLayout.getVisibility() == View.VISIBLE) {
            addAttachmentLayout.setVisibility(View.GONE);
            addAttachment.animate().setDuration(400).rotationBy(-45).start();
        }

        final File fileToUpload = new File(filePath);
        final String fileName = Uri.fromFile(fileToUpload).getLastPathSegment();

        Attachment preSendAttachment = attachment;//Create/Update attachment
        if (preSendAttachment == null) preSendAttachment = new Attachment();
        preSendAttachment.setName(fileName);
        preSendAttachment.setBytesCount(fileToUpload.length());
        preSendAttachment.setUrl("loading");
        prepareMessage(null, attachmentType, preSendAttachment);

        checkAndCopy("/" + getString(R.string.app_name) + "/" + AttachmentTypes.getTypeName(attachmentType) + "/.sent/", fileToUpload);//Make a copy

        Intent intent = new Intent(Helper.UPLOAD_AND_SEND);
        intent.putExtra("attachment", attachment);
        intent.putExtra("attachment_type", attachmentType);
        intent.putExtra("attachment_file_path", filePath);
        intent.putExtra("attachment_chat_child", chatChild);
        intent.putExtra("attachment_recipient_id", userOrGroupId);
        intent.putExtra("attachment_recipient_user", user);
        intent.putExtra("attachment_recipient_group", group);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void downloadFile(DownloadFileEvent downloadFileEvent) {
        if (permissionsAvailable(permissionsStorage)) {
            new DownloadUtil().checkAndLoad(this, downloadFileEvent);
            adapterPositions.add(downloadFileEvent.getPosition());
        } else {
            ActivityCompat.requestPermissions(this, permissionsStorage, 47);
        }
    }

    @Override
    public void OnMessageClick(Message message, int position) {
        if (Helper.CHAT_CAB && RealmObject.isValid(message)) {
            message.setSelected(!message.isSelected());//Toggle message selection
            messageAdapter.notifyItemChanged(position);//Notify changes

            if (message.isSelected())
                countSelected++;
            else
                countSelected--;

            selectedCount.setText(String.valueOf(countSelected));//Update count
            if (countSelected == 0)
                undoSelectionPrepared();//If count is zero then reset selection
        }
    }

    @Override
    public void OnMessageLongClick(Message message, int position) {
        if (!Helper.CHAT_CAB && RealmObject.isValid(message)) {//Prepare selection if not in selection mode
            prepareToSelect();
            message.setSelected(true);
            messageAdapter.notifyItemChanged(position);
            countSelected++;
            selectedCount.setText(String.valueOf(countSelected));
        }
    }

    private void prepareToSelect() {
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.menu_chat_cab);
        getSupportActionBar().setTitle("");
        selectedCount.setText("1");
        selectedCount.setVisibility(View.VISIBLE);
        toolbarContent.setVisibility(View.GONE);
        Helper.CHAT_CAB = true;
    }

    private void undoSelectionPrepared() {
        for (Message msg : dataList) {
            msg.setSelected(false);
        }
        countSelected = 0;
        messageAdapter.notifyDataSetChanged();
        toolbar.getMenu().clear();
        selectedCount.setVisibility(View.GONE);
        toolbarContent.setVisibility(View.VISIBLE);
        Helper.CHAT_CAB = false;
    }

    public static Intent newIntent(Context context, ArrayList<Message> forwardMessages, String chatId, String chatName) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(EXTRA_DATA_CHAT_ID, chatId);
        intent.putExtra(EXTRA_DATA_CHAT_NAME, chatName);
        if (forwardMessages == null)
            forwardMessages = new ArrayList<>();
        intent.putParcelableArrayListExtra(EXTRA_DATA_LIST, forwardMessages);
        return intent;
    }

    public static Intent newIntent(Context context, ArrayList<Message> forwardMessages, Group group) {
        //intent contains user to chat with and message forward list if any.
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(EXTRA_DATA_CHAT_GROUP, group);
        intent.putExtra(EXTRA_DATA_CHAT_ID, "group-1");
        if (forwardMessages == null)
            forwardMessages = new ArrayList<>();
        intent.putParcelableArrayListExtra(EXTRA_DATA_LIST, forwardMessages);
        return intent;
    }

    @Override
    public boolean isRecordingPlaying(String fileName) {
        return isMediaPlayerPlaying() && currentlyPlaying.equals(fileName);
    }

    private boolean isMediaPlayerPlaying() {
        try {
            return mediaPlayer.isPlaying();
        } catch (IllegalStateException ex) {
            return false;
        }
    }

    @Override
    public void playRecording(File file, String fileName, int position) {
        if (recordPermissionsAvailable()) {
            if (isMediaPlayerPlaying()) {
                mediaPlayer.stop();
                notifyRecordingPlaybackCompletion();
                if (!fileName.equals(currentlyPlaying)) {
                    if (startPlayback(file)) {
                        currentlyPlaying = fileName;
                        messageAdapter.notifyItemChanged(position);
                    }
                }
            } else {
                if (startPlayback(file)) {
                    currentlyPlaying = fileName;
                    messageAdapter.notifyItemChanged(position);
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, permissionsRecord, REQUEST_PERMISSION_RECORD);
        }
    }

    private boolean startPlayback(File file) {
        boolean started = true;
        resetMediaPlayer();
        try {
            FileInputStream is = new FileInputStream(file);
            FileDescriptor fd = is.getFD();
            mediaPlayer.setDataSource(fd);
            is.close();
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
            started = false;
        }
        return started;
    }

    private void resetMediaPlayer() {
        try {
            mediaPlayer.reset();
        } catch (IllegalStateException ex) {
            mediaPlayer = new MediaPlayer();
        }
    }

    private void notifyRecordingPlaybackCompletion() {
        if (recyclerView != null && messageAdapter != null) {
            int total = dataList.size();
            for (int i = total - 1; i >= 0; i--) {
                if (dataList.get(i).getAttachment() != null
                        &&
                        dataList.get(i).getAttachment().getName().equals(currentlyPlaying)) {
                    messageAdapter.notifyItemChanged(i);
                    break;
                }
            }
        }
    }

    @Override
    public void onVideosChosen(List<ChosenVideo> list) {
        if (list != null && !list.isEmpty())
            uploadThumbnail(Uri.parse(list.get(0).getOriginalPath()).getPath());
    }

    @Override
    public void onAudiosChosen(List<ChosenAudio> list) {
        if (list != null && !list.isEmpty())
            newFileUploadTask(Uri.parse(list.get(0).getOriginalPath()).getPath(), AttachmentTypes.AUDIO, null);
    }

    @Override
    public void onFilesChosen(List<ChosenFile> list) {
        if (list != null && !list.isEmpty())
            newFileUploadTask(Uri.parse(list.get(0).getOriginalPath()).getPath(), AttachmentTypes.DOCUMENT, null);
    }

    @Override
    public void onImagesChosen(List<ChosenImage> list) {
        if (list != null && !list.isEmpty()) {
            Uri originalFileUri = Uri.parse(list.get(0).getOriginalPath());
            File tempFile = new File(getCacheDir(), originalFileUri.getLastPathSegment());
            try {
                uploadImage(SiliCompressor.with(this).compress(originalFileUri.toString(), tempFile));
            } catch (Exception ex) {
                uploadImage(originalFileUri.getPath());
            }
        }
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
}
