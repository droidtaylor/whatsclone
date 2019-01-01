package com.verbosetech.whatsclone.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.verbosetech.whatsclone.R;
import com.verbosetech.whatsclone.activities.ChatActivity;
import com.verbosetech.whatsclone.models.Attachment;
import com.verbosetech.whatsclone.models.AttachmentTypes;
import com.verbosetech.whatsclone.models.Chat;
import com.verbosetech.whatsclone.models.Contact;
import com.verbosetech.whatsclone.models.Group;
import com.verbosetech.whatsclone.models.Message;
import com.verbosetech.whatsclone.models.MyString;
import com.verbosetech.whatsclone.models.User;
import com.verbosetech.whatsclone.utils.FirebaseUploader;
import com.verbosetech.whatsclone.utils.Helper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class FirebaseChatService extends Service {
    private static final String CHANNEL_ID_MAIN = "my_channel_01";
    private static final String CHANNEL_ID_GROUP = "my_channel_02";
    private static final String CHANNEL_ID_USER = "my_channel_03";

    private DatabaseReference usersRef, chatRef, groupsRef, inboxRef, newUserRef;
    private Helper helper;
    private String myId;
    private Realm rChatDb;
    private User userMe;
    private HashMap<String, Contact> myContacts;
    private HashSet<String> registeredChatUpdatedUserIds;

    public FirebaseChatService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("OyApp", "onCreate");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_MAIN, "whatsclone chat service", NotificationManager.IMPORTANCE_LOW);
            channel.setSound(null, null);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
        }
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_MAIN)
                .setSmallIcon(R.drawable.noti_icon)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Chat service running")
                .setSound(null)
                .build();
        startForeground(1, notification);

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(uploadAndSendReceiver, new IntentFilter(Helper.UPLOAD_AND_SEND));
        localBroadcastManager.registerReceiver(logoutReceiver, new IntentFilter(Helper.BROADCAST_LOGOUT));
        localBroadcastManager.registerReceiver(myUsersReceiver, new IntentFilter(Helper.BROADCAST_MY_USERS));
        localBroadcastManager.registerReceiver(myContactsReceiver, new IntentFilter(Helper.BROADCAST_MY_CONTACTS));
    }

    private BroadcastReceiver logoutReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopForeground(true);
            stopSelf();
        }
    };

    private BroadcastReceiver uploadAndSendReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(Helper.UPLOAD_AND_SEND)) {
                Attachment attachment = intent.getParcelableExtra("attachment");
                int type = intent.getIntExtra("attachment_type", -1);
                String attachmentFilePath = intent.getStringExtra("attachment_file_path");
                String attachmentChatChild = intent.getStringExtra("attachment_chat_child");
                String attachmentRecipientId = intent.getStringExtra("attachment_recipient_id");
                User attachmentRecipientUser = intent.getParcelableExtra("attachment_recipient_user");
                Group attachmentRecipientGroup = intent.getParcelableExtra("attachment_recipient_group");
                uploadAndSend(new File(attachmentFilePath), attachment, type, attachmentChatChild, attachmentRecipientId, attachmentRecipientUser, attachmentRecipientGroup);
            }
        }
    };

    private BroadcastReceiver myUsersReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<User> myUsers = intent.getParcelableArrayListExtra("data");
            if (myUsers != null) {
                for (User user : myUsers) {
                    if (!registeredChatUpdatedUserIds.contains(user.getId())) {
                        User userInDb = rChatDb.where(User.class).equalTo("id", user.getId()).findFirst();
                        if (userInDb == null) {
                            registerChatUpdates(true, user.getId());
                            registerUserUpdates(user.getId());
                            broadcastUser("added", user);
                        }
                        rChatDb.beginTransaction();
                        rChatDb.copyToRealmOrUpdate(user);
                        rChatDb.commitTransaction();
                    }
                }
            }
        }
    };

    private BroadcastReceiver myContactsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            HashMap<String, Contact> contactHashMap = (HashMap<String, Contact>) intent.getSerializableExtra("data");
            if (myContacts != null) {
                myContacts.clear();
                myContacts.putAll(contactHashMap);
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("OyApp", "onStartCommand");
        if (!User.validate(userMe)) {
            initVars();
            if (User.validate(userMe)) {
                myId = userMe.getId();
                rChatDb = Helper.getRealmInstance();

                RealmResults<User> myUsers = rChatDb.where(User.class).notEqualTo("id", userMe.getId()).findAll();
                if (myUsers != null) {
                    for (User user : myUsers) {
                        registerChatUpdates(true, user.getId());
                        registerUserUpdates(user.getId());
                    }
                }

                registerGroupUpdates();
                registerInboxUpdates();
                registerNewUserUpdates();
                registerMyUpdates();
            } else {
                stopForeground(true);
                stopSelf();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void registerMyUpdates() {
        if (!TextUtils.isEmpty(myId)) {
            usersRef.child(myId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        User user = dataSnapshot.getValue(User.class);
                        if (User.validate(user)) {
                            rChatDb.beginTransaction();
                            rChatDb.copyToRealmOrUpdate(user);
                            rChatDb.commitTransaction();

                            helper.setLoggedInUser(user);
                            broadcastUser("changed", user);
                        }
                    } catch (Exception ex) {
                        Log.e("USER", "invalid user");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void registerNewUserUpdates() {
        newUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    User user = dataSnapshot.getValue(User.class);
                    if (User.validate(user)) {
                        String endTrim = Helper.getEndTrim(user.getId());
                        if (myContacts.containsKey(endTrim) && rChatDb.where(User.class).equalTo("id", user.getId()).findFirst() == null) {
                            user.setNameInPhone(myContacts.get(endTrim).getName());
                            rChatDb.beginTransaction();
                            rChatDb.copyToRealmOrUpdate(user);
                            rChatDb.commitTransaction();
                            registerChatUpdates(true, user.getId());
                            broadcastUser("added", user);
                            registerUserUpdates(user.getId());
                        }
                    }
                } catch (Exception ex) {
                    Log.e("USER", "invalid user");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void registerInboxUpdates() {
        if (!TextUtils.isEmpty(myId)) {
            inboxRef.child(myId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        Message message = dataSnapshot.getValue(Message.class);
                        if (message != null && !message.getSenderId().equals(message.getRecipientId()) && !registeredChatUpdatedUserIds.contains(message.getSenderId())) {
                            User userInDb = rChatDb.where(User.class).equalTo("id", message.getSenderId()).findFirst();
                            if (userInDb == null) {
                                User user = new User(message.getSenderId(), message.getSenderName(), message.getSenderStatus(), message.getSenderImage());
                                String idTrim = Helper.getEndTrim(message.getSenderId());
                                if (myContacts.containsKey(idTrim)) {
                                    user.setNameInPhone(myContacts.get(idTrim).getName());
                                }
                                rChatDb.beginTransaction();
                                rChatDb.copyToRealmOrUpdate(user);
                                rChatDb.commitTransaction();
                                registerChatUpdates(true, message.getSenderId());
                                broadcastUser("added", user);
                                registerUserUpdates(user.getId());
                            }
                        }
                    } catch (Exception ex) {
                        Log.e("Message", "invalid message");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void initVars() {
        Log.e("OyApp", "initVars");
        helper = new Helper(this);
        myContacts = helper.getMyUsersNameCache();
        registeredChatUpdatedUserIds = new HashSet<>();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        usersRef = firebaseDatabase.getReference(Helper.REF_USERS);
        chatRef = firebaseDatabase.getReference(Helper.REF_CHAT);
        groupsRef = firebaseDatabase.getReference(Helper.REF_GROUP);
        inboxRef = firebaseDatabase.getReference(Helper.REF_INBOX);
        newUserRef = firebaseDatabase.getReference(Helper.REF_NEW_USER);
        Realm.init(this);

        userMe = helper.getLoggedInUser();
    }

    private void restartService() {
        Log.e("OyApp", "Restart");
        if (new Helper(this).isLoggedIn()) {
            Intent intent = new Intent(this, FirebaseChatService.class);
            PendingIntent pendingIntent = PendingIntent.getService(this, 99, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 500, pendingIntent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("OyApp", "onDestroy");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(uploadAndSendReceiver);
        restartService();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        restartService();
        super.onTaskRemoved(rootIntent);
        Log.e("OyApp", "onTaskRemoved");
    }

    private void uploadAndSend(final File fileToUpload,
                               final Attachment attachment, final int attachmentType,
                               final String chatChild, final String recipientId, final User recipientUser, final Group recipientGroup) {
        if (!fileToUpload.exists())
            return;
        final String fileName = Uri.fromFile(fileToUpload).getLastPathSegment();
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child(getString(R.string.app_name)).child(AttachmentTypes.getTypeName(attachmentType)).child(fileName);
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //If file is already uploaded
                Attachment attachment1 = attachment;
                if (attachment1 == null) attachment1 = new Attachment();
                attachment1.setName(fileName);
                attachment1.setUrl(uri.toString());
                attachment1.setBytesCount(fileToUpload.length());
                sendMessage(null, attachmentType, attachment1, chatChild, recipientId, recipientUser, recipientGroup);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //Elase upload and then send message
                FirebaseUploader firebaseUploader = new FirebaseUploader(new FirebaseUploader.UploadListener() {
                    @Override
                    public void onUploadFail(String message) {
                        Log.e("DatabaseException", message);
                    }

                    @Override
                    public void onUploadSuccess(String downloadUrl) {
                        Attachment attachment1 = attachment;
                        if (attachment1 == null) attachment1 = new Attachment();
                        attachment1.setName(fileToUpload.getName());
                        attachment1.setUrl(downloadUrl);
                        attachment1.setBytesCount(fileToUpload.length());
                        sendMessage(null, attachmentType, attachment1, chatChild, recipientId, recipientUser, recipientGroup);
                    }

                    @Override
                    public void onUploadProgress(int progress) {

                    }

                    @Override
                    public void onUploadCancelled() {

                    }
                }, storageReference);
                firebaseUploader.uploadOthers(getApplicationContext(), fileToUpload);
            }
        });
    }

    private void sendMessage(String messageBody,
                             @AttachmentTypes.AttachmentType int attachmentType, Attachment attachment,
                             String chatChild, String userOrGroupId, User recipientUser, Group recipientGroup) {
        //Create message object
        Message message = new Message();
        message.setAttachmentType(attachmentType);
        if (attachmentType != AttachmentTypes.NONE_TEXT)
            message.setAttachment(attachment);
        message.setBody(messageBody);
        message.setDate(System.currentTimeMillis());
        message.setSenderId(userMe.getId());
        message.setSenderImage(userMe.getImage());
        message.setSenderName(userMe.getName());
        message.setSenderStatus(userMe.getStatus());
        message.setSent(true);
        message.setDelivered(false);
        message.setRecipientId(userOrGroupId);
        message.setRecipientGroupIds(recipientGroup != null ? new ArrayList<MyString>(recipientGroup.getUserIds()) : null);
        message.setRecipientName(recipientUser != null ? recipientUser.getName() : recipientGroup.getName());
        message.setRecipientImage(recipientUser != null ? recipientUser.getImage() : recipientGroup.getImage());
        message.setRecipientStatus(recipientUser != null ? recipientUser.getStatus() : recipientGroup.getStatus());
        message.setId(chatRef.child(chatChild).push().getKey());

        //Add message in chat child
        chatRef.child(chatChild).child(message.getId()).setValue(message);
        //Add message in recipient's inbox
        inboxRef.child(userOrGroupId).setValue(message);
    }

    private void registerGroupUpdates() {
        groupsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                try {
                    Group group = dataSnapshot.getValue(Group.class);
                    if (Group.validate(group)) {
                        boolean iAmMember = group.getUserIds().contains(new MyString(myId));
                        Group groupInDb = rChatDb.where(Group.class).equalTo("id", group.getId()).findFirst();
                        if (iAmMember && groupInDb == null) {
                            rChatDb.beginTransaction();
                            rChatDb.copyToRealmOrUpdate(group);
                            rChatDb.commitTransaction();
                            checkAndNotify(group);
                        }
                        if (iAmMember) {
                            registerChatUpdates(true, group.getId());
                            broadcastGroup("added", group);
                        }
                    }
                } catch (Exception ex) {
                    Log.e("GROUP", "invalid group");
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                try {
                    Group group = dataSnapshot.getValue(Group.class);
                    if (Group.validate(group)) {
                        if (group.getUserIds().contains(new MyString(myId))) {
                            broadcastGroup("changed", group);
                            updateGroupInDb(group);
                        } else {
                            Chat chat = rChatDb.where(Chat.class).equalTo("myId", myId).equalTo("chatId", group.getId()).findFirst();
                            if (chat != null) {
                                registerChatUpdates(false, group.getId());
                                broadcastGroup("changed", group);
                                updateGroupInDb(group);
                            }
                        }
                    }
                } catch (Exception ex) {
                    Log.e("GROUP", "invalid group");
                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void checkAndNotify(Group group) {
        Chat thisGroupChat = rChatDb.where(Chat.class).equalTo("myId", myId).equalTo("chatId", group.getId()).findFirst();
        if (thisGroupChat == null) {
            if (!group.getUserIds().get(0).equals(new MyString(myId))) {
                notifyNewGroup(group);
            }
            rChatDb.beginTransaction();
            thisGroupChat = rChatDb.createObject(Chat.class);
            thisGroupChat.setChatName(group.getName());
            thisGroupChat.setChatImage(group.getImage());
            thisGroupChat.setChatStatus(group.getStatus());
            thisGroupChat.setChatId(group.getId());
            thisGroupChat.setGroup(true);
            thisGroupChat.setMessages(new RealmList<Message>());
            thisGroupChat.setMyId(myId);
            thisGroupChat.setRead(false);
            long millis = System.currentTimeMillis();
            thisGroupChat.setLastMessage("Created on " + Helper.getDateTime(millis));
            thisGroupChat.setTimeUpdated(millis);
            rChatDb.commitTransaction();
        }
    }

    private void notifyNewGroup(Group group) {
        // Construct the Intent you want to end up at
        Intent chatActivity = ChatActivity.newIntent(this, null, group.getId(), group.getName());
        // Construct the PendingIntent for your Notification
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // This uses android:parentActivityName and
        // android.support.PARENT_ACTIVITY meta-data by default
        stackBuilder.addNextIntentWithParentStack(chatActivity);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(99, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notificationBuilder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_GROUP, "whatsclone new group notification", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
            notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID_GROUP);
        } else {
            notificationBuilder = new NotificationCompat.Builder(this);
        }

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notificationBuilder.setSmallIcon(R.drawable.noti_icon)
                .setContentTitle("Group: " + group.getName())
                .setContentText("You have been added to new group called " + group.getName())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        int msgId = Integer.parseInt(group.getId().substring(group.getId().length() - 4, group.getId().length() - 1));
        notificationManager.notify(msgId, notificationBuilder.build());
    }

    private void registerUserUpdates(String userId) {
        usersRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    User user = dataSnapshot.getValue(User.class);
                    if (User.validate(user)) {
                        updateUserInDb(user);
                        broadcastUser("changed", user);
                    }
                } catch (Exception ex) {
                    Log.e("USER", "invalid user");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateUserInDb(User user) {
        if (!TextUtils.isEmpty(myId)) {
            Chat chat = rChatDb.where(Chat.class).equalTo("myId", myId).equalTo("chatId", user.getId()).findFirst();
            if (chat != null && (!chat.getChatImage().equals(user.getImage()) || !chat.getChatStatus().equals(user.getStatus()))) {
                rChatDb.beginTransaction();
                chat.setChatImage(user.getImage());
                chat.setChatStatus(user.getStatus());
                String idTrim = Helper.getEndTrim(user.getId());
                chat.setChatName(myContacts.containsKey(idTrim) ? myContacts.get(idTrim).getName() : chat.getChatName());
                user.setNameInPhone(chat.getChatName());
                rChatDb.copyToRealmOrUpdate(user);
                rChatDb.commitTransaction();
            }
        }
    }

    private void updateGroupInDb(Group group) {
        if (!TextUtils.isEmpty(myId)) {
            Chat chat = rChatDb.where(Chat.class).equalTo("myId", myId).equalTo("chatId", group.getId()).findFirst();
            if (chat != null) {
                rChatDb.beginTransaction();
                chat.setChatName(group.getName());
                chat.setChatImage(group.getImage());
                chat.setChatStatus(group.getStatus());
                rChatDb.commitTransaction();
            }

            rChatDb.beginTransaction();
            rChatDb.copyToRealmOrUpdate(group);
            rChatDb.commitTransaction();
        }
    }

    private void registerChatUpdates(boolean register, String id) {
        if (register) {
            registeredChatUpdatedUserIds.add(id);
        } else {
            registeredChatUpdatedUserIds.remove(id);
        }
        if (!TextUtils.isEmpty(myId) && !TextUtils.isEmpty(id)) {
            DatabaseReference idChatRef = chatRef.child(id.startsWith(Helper.GROUP_PREFIX) ? id : Helper.getChatChild(myId, id));
            if (register) {
                idChatRef.addChildEventListener(chatUpdateListener);
            } else {
                idChatRef.removeEventListener(chatUpdateListener);
            }
        }
    }

    private ChildEventListener chatUpdateListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Message message = dataSnapshot.getValue(Message.class);

            //temporary fix for unable to remove chatupdate listener
            if (message.getRecipientId().startsWith(Helper.GROUP_PREFIX) && message.getRecipientGroupIds() != null && !message.getRecipientGroupIds().contains(new MyString(myId)))
                return;

            if (!message.isDelivered()) {
                Message result = rChatDb.where(Message.class).equalTo("id", message.getId()).findFirst();
                if (result == null && !TextUtils.isEmpty(myId) && helper.isLoggedIn()) {
                    saveMessage(message);
                    if (!message.getRecipientId().startsWith(Helper.GROUP_PREFIX) && !message.getSenderId().equals(myId))
                        chatRef.child(dataSnapshot.getRef().getParent().getKey()).child(message.getId()).child("delivered").setValue(true);
                }
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Message message = dataSnapshot.getValue(Message.class);
            Message result = rChatDb.where(Message.class).equalTo("id", message.getId()).findFirst();
            if (result != null) {
                rChatDb.beginTransaction();
                result.setDelivered(message.isDelivered());
                rChatDb.commitTransaction();
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Message message = dataSnapshot.getValue(Message.class);

            Helper.deleteMessageFromRealm(rChatDb, message.getId());

            String userOrGroupId = myId.equals(message.getSenderId()) ? message.getRecipientId() : message.getSenderId();
            Chat chat = Helper.getChat(rChatDb, myId, userOrGroupId).findFirst();
            if (chat != null) {
                rChatDb.beginTransaction();
                RealmList<Message> realmList = chat.getMessages();
                if (realmList.size() == 0)
                    RealmObject.deleteFromRealm(chat);
                else {
                    chat.setLastMessage(realmList.get(realmList.size() - 1).getBody());
                    chat.setTimeUpdated(realmList.get(realmList.size() - 1).getDate());
                }
                rChatDb.commitTransaction();
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void saveMessage(Message message) {
        if (message.getAttachment() != null && !TextUtils.isEmpty(message.getAttachment().getUrl()) && !TextUtils.isEmpty(message.getAttachment().getName())) {
            String idToCompare = "loading" + message.getAttachment().getBytesCount() + message.getAttachment().getName();
            Helper.deleteMessageFromRealm(rChatDb, idToCompare);
        }

        boolean isGroupMessage = message.getRecipientId().startsWith(Helper.GROUP_PREFIX);
        boolean isMeSender = myId.equals(message.getSenderId());

        String userOrGroupId = isGroupMessage ? message.getRecipientId() : isMeSender ? message.getRecipientId() : message.getSenderId();
        Chat chat = Helper.getChat(rChatDb, myId, userOrGroupId).findFirst();
        rChatDb.beginTransaction();
        if (chat == null) {
            chat = rChatDb.createObject(Chat.class);
            chat.setChatId(userOrGroupId);
            chat.setGroup(isGroupMessage);
            chat.setMyId(myId);
            chat.setMessages(new RealmList<Message>());
        }

        String senderIdTrim = Helper.getEndTrim(message.getSenderId());
        message.setSenderName(myContacts.containsKey(senderIdTrim) ? myContacts.get(senderIdTrim).getName() : message.getSenderName());
        if (!isGroupMessage) {
            String recipientIdTrim = Helper.getEndTrim(message.getRecipientId());
            message.setRecipientName(myContacts.containsKey(recipientIdTrim) ? myContacts.get(recipientIdTrim).getName() : message.getRecipientName());
        }

        chat.setChatName(isGroupMessage ? message.getRecipientName() : isMeSender ? message.getRecipientName() : message.getSenderName());
        chat.setChatStatus(isGroupMessage ? message.getRecipientStatus() : isMeSender ? message.getRecipientStatus() : message.getSenderStatus());
        chat.setChatImage(isGroupMessage ? message.getRecipientImage() : isMeSender ? message.getRecipientImage() : message.getSenderImage());

        if (!isMeSender)
            chat.setRead(false);
        chat.setTimeUpdated(message.getDate());
        chat.getMessages().add(message);
        chat.setLastMessage(message.getBody());
        rChatDb.commitTransaction();


        if (!message.isDelivered() && !message.getSenderId().equals(myId) && !helper.isUserMute(message.getSenderId()) && (Helper.CURRENT_CHAT_ID == null || !Helper.CURRENT_CHAT_ID.equals(userOrGroupId))) {
            // Construct the Intent you want to end up at
            Intent chatActivity = ChatActivity.newIntent(this, null, userOrGroupId, chat.getChatName());
            // Construct the PendingIntent for your Notification
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            // This uses android:parentActivityName and
            // android.support.PARENT_ACTIVITY meta-data by default
            stackBuilder.addNextIntentWithParentStack(chatActivity);
            PendingIntent pendingIntent = stackBuilder.getPendingIntent(99, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder notificationBuilder = null;
            String channelId = isGroupMessage ? CHANNEL_ID_GROUP : CHANNEL_ID_USER;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId, "whatsclone new message notification", NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
                notificationBuilder = new NotificationCompat.Builder(this, channelId);
            } else {
                notificationBuilder = new NotificationCompat.Builder(this);
            }

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            notificationBuilder.setSmallIcon(R.drawable.noti_icon)
                    .setContentTitle(chat.getChatName())
                    .setContentText(message.getBody())
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);
            int msgId = 0;
            try {
                msgId = Integer.parseInt(message.getSenderId());
            } catch (NumberFormatException ex) {
                msgId = Integer.parseInt(message.getSenderId().substring(message.getSenderId().length() / 2));
            }
            notificationManager.notify(msgId, notificationBuilder.build());
        }
    }

    private void broadcastUser(String what, User value) {
        Intent intent = new Intent(Helper.BROADCAST_USER);
        intent.putExtra("data", value);
        intent.putExtra("what", what);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.sendBroadcast(intent);
    }

    private void broadcastGroup(String what, Group value) {
        Intent intent = new Intent(Helper.BROADCAST_GROUP);
        intent.putExtra("data", value);
        intent.putExtra("what", what);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.sendBroadcast(intent);
    }
}
