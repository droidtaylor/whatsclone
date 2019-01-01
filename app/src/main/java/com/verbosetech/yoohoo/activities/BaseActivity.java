package com.verbosetech.whatsclone.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.verbosetech.whatsclone.models.Contact;
import com.verbosetech.whatsclone.models.Group;
import com.verbosetech.whatsclone.models.User;
import com.verbosetech.whatsclone.services.FirebaseChatService;
import com.verbosetech.whatsclone.services.SinchService;
import com.verbosetech.whatsclone.utils.Helper;

import java.util.ArrayList;
import java.util.HashMap;

import io.realm.Realm;

/**
 * Created by a_man on 01-01-2018.
 */

public abstract class BaseActivity extends AppCompatActivity implements ServiceConnection {
    protected String[] permissionsRecord = {Manifest.permission.VIBRATE, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    protected String[] permissionsContact = {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    protected String[] permissionsStorage = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    protected String[] permissionsCamera = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    protected String[] permissionsSinch = {Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS, Manifest.permission.READ_PHONE_STATE};

    protected User userMe, user;
    protected Group group;
    protected Helper helper;
    protected Realm rChatDb;

    protected DatabaseReference usersRef, groupRef, chatRef, inboxRef, fcmIdRef;
    private SinchService.SinchServiceInterface mSinchServiceInterface;

    //Group updates receiver(new or updated)
    private BroadcastReceiver groupReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(Helper.BROADCAST_GROUP)) {
                Group group = intent.getParcelableExtra("data");
                String what = intent.getStringExtra("what");
                switch (what) {
                    case "added":
                        groupAdded(group);
                        break;
                    case "changed":
                        groupUpdated(group);
                        break;
                }
            }
        }
    };

    //User updates receiver(new or updated)
    private BroadcastReceiver userReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(Helper.BROADCAST_USER)) {
                User user = intent.getParcelableExtra("data");
                String what = intent.getStringExtra("what");
                switch (what) {
                    case "added":
                        userAdded(user);
                        break;
                    case "changed":
                        userUpdated(user);
                        break;
                }
            }
        }
    };

    private BroadcastReceiver myUsersReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<User> myUsers = intent.getParcelableArrayListExtra("data");
            if (myUsers != null) {
                myUsersResult(myUsers);
            }
        }
    };

    private BroadcastReceiver myContactsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            HashMap<String, Contact> myContacts = (HashMap<String, Contact>) intent.getSerializableExtra("data");
            if (myContacts != null) {
                myContactsResult(myContacts);
            }
        }
    };

    abstract void myUsersResult(ArrayList<User> myUsers);

    abstract void myContactsResult(HashMap<String, Contact> myContacts);

    abstract void userAdded(User valueUser);

    abstract void groupAdded(Group valueGroup);

    abstract void userUpdated(User valueUser);

    abstract void groupUpdated(Group valueGroup);

    abstract void onSinchConnected();

    abstract void onSinchDisconnected();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        helper = new Helper(this);
        userMe = helper.getLoggedInUser();
        Realm.init(this);
        rChatDb = Helper.getRealmInstance();

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();//get firebase instance
        usersRef = firebaseDatabase.getReference(Helper.REF_USERS);//instantiate user's firebase reference
        groupRef = firebaseDatabase.getReference(Helper.REF_GROUP);//instantiate group's firebase reference
        chatRef = firebaseDatabase.getReference(Helper.REF_CHAT);//instantiate chat's firebase reference
        inboxRef = firebaseDatabase.getReference(Helper.REF_INBOX);//instantiate inbox's firebase reference
        fcmIdRef = firebaseDatabase.getReference(Helper.REF_USERS_FCM_IDS);//instantiate fcm id's firebase reference

        startService(new Intent(this, FirebaseChatService.class));
        getApplicationContext().bindService(new Intent(this, SinchService.class), this, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(userReceiver, new IntentFilter(Helper.BROADCAST_USER));
        localBroadcastManager.registerReceiver(groupReceiver, new IntentFilter(Helper.BROADCAST_GROUP));
        localBroadcastManager.registerReceiver(myContactsReceiver, new IntentFilter(Helper.BROADCAST_MY_CONTACTS));
        localBroadcastManager.registerReceiver(myUsersReceiver, new IntentFilter(Helper.BROADCAST_MY_USERS));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.unregisterReceiver(userReceiver);
        localBroadcastManager.unregisterReceiver(groupReceiver);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        if (SinchService.class.getName().equals(componentName.getClassName())) {
            mSinchServiceInterface = (SinchService.SinchServiceInterface) iBinder;
            onSinchConnected();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        if (SinchService.class.getName().equals(componentName.getClassName())) {
            mSinchServiceInterface = null;
            onSinchDisconnected();
        }
    }

    protected SinchService.SinchServiceInterface getSinchServiceInterface() {
        return mSinchServiceInterface;
    }

    protected boolean permissionsAvailable(String[] permissions) {
        boolean granted = true;
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                granted = false;
                break;
            }
        }
        return granted;
    }
}
