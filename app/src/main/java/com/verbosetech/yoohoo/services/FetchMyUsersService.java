package com.verbosetech.whatsclone.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Patterns;

import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.verbosetech.whatsclone.models.Contact;
import com.verbosetech.whatsclone.models.User;
import com.verbosetech.whatsclone.utils.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

public class FetchMyUsersService extends IntentService {
    private static String EXTRA_PARAM1 = "my_id";
    private static String EXTRA_PARAM2 = "token";
    private HashMap<String, Contact> myContacts;
    private ArrayList<User> myUsers;
    private String myId, idToken;

    public static boolean STARTED = false;

    public FetchMyUsersService() {
        super("FetchMyUsersService");
    }

    public static void startMyUsersService(Context context, String myId, String idToken) {
        Intent intent = new Intent(context, FetchMyUsersService.class);
        intent.putExtra(EXTRA_PARAM1, myId);
        intent.putExtra(EXTRA_PARAM2, idToken);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        STARTED = true;
        myId = intent.getStringExtra(EXTRA_PARAM1);
        idToken = intent.getStringExtra(EXTRA_PARAM2);
        fetchMyContacts();
        broadcastMyContacts();
        fetchMyUsers();
        broadcastMyUsers();
        STARTED = false;
    }

    private void broadcastMyUsers() {
        if (this.myUsers != null) {
            Intent intent = new Intent(Helper.BROADCAST_MY_USERS);
            intent.putParcelableArrayListExtra("data", this.myUsers);
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
            localBroadcastManager.sendBroadcast(intent);
        }
    }

    private void fetchMyUsers() {
        myUsers = new ArrayList<>();
        try {
            StringBuilder response = new StringBuilder();
            URL url = new URL(FirebaseDatabase.getInstance().getReference().toString() + "/" + Helper.REF_USERS + ".json?auth=" + idToken);
            URLConnection conn = url.openConnection();
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line).append(" ");
            }
            rd.close();

            JSONObject responseObject = new JSONObject(response.toString());
            Gson gson = new GsonBuilder().create();
            Iterator<String> keys = responseObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject innerJObject = responseObject.getJSONObject(key);
                User user = gson.fromJson(innerJObject.toString(), User.class);
                if (User.validate(user) && !user.getId().equals(myId)) {
                    String idTrim = Helper.getEndTrim(user.getId());
                    if (myContacts.containsKey(idTrim)) {
                        user.setNameInPhone(myContacts.get(idTrim).getName());
                        myUsers.add(user);
                    }
                }
                if (myUsers.size() == myContacts.size()) {
                    break;
                }
            }

            Collections.sort(myUsers, new Comparator<User>() {
                @Override
                public int compare(User user1, User user2) {
                    return user1.getNameToDisplay().compareToIgnoreCase(user2.getNameToDisplay());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void broadcastMyContacts() {
        if (this.myContacts != null) {
            new Helper(this).setMyUsersNameCache(myContacts);
            Intent intent = new Intent(Helper.BROADCAST_MY_CONTACTS);
            intent.putExtra("data", this.myContacts);
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
            localBroadcastManager.sendBroadcast(intent);
        }
    }

    private void fetchMyContacts() {
        myContacts = new HashMap<>();
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        if (cursor != null && !cursor.isClosed()) {
            cursor.getCount();
            while (cursor.moveToNext()) {
                int hasPhoneNumber = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                if (hasPhoneNumber == 1) {
                    String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replaceAll("\\s+", "");
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
                    if (Patterns.PHONE.matcher(number).matches()) {
                        boolean hasPlus = String.valueOf(number.charAt(0)).equals("+");
                        number = number.replaceAll("[\\D]", "");
                        if (hasPlus) {
                            number = "+" + number;
                        }
                        Contact contact = new Contact(number, name);
                        String endTrim = Helper.getEndTrim(contact.getPhoneNumber());
                        if (!myContacts.containsKey(endTrim))
                            myContacts.put(endTrim, contact);
                    }
                }
            }
            cursor.close();
        }
    }
}
