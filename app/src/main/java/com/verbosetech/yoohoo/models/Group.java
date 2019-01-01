package com.verbosetech.whatsclone.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;

import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

/**
 * Created by a_man on 30-12-2017.
 */

@RealmClass
public class Group implements Parcelable, RealmModel {
    @PrimaryKey
    private String id;
    private String name, status, image;
    private RealmList<MyString> userIds;

    @Exclude
    @Ignore
    private ArrayList<User> users;

    public Group() {
    }

    protected Group(Parcel in) {
        id = in.readString();
        name = in.readString();
        status = in.readString();
        image = in.readString();
        ArrayList<MyString> userIdsToParse = in.createTypedArrayList(MyString.CREATOR);
        userIds = new RealmList<>();
        userIds.addAll(userIdsToParse);
        users = in.createTypedArrayList(User.CREATOR);
    }

    public static final Creator<Group> CREATOR = new Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Group group = (Group) o;

        return id.equals(group.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public RealmList<MyString> getUserIds() {
//        ArrayList<MyString> toReturn = new ArrayList<>();
//        toReturn.addAll(userIds);
//        return toReturn;
        return userIds;
    }

    public void setUserIds(ArrayList<MyString> userIds) {
        this.userIds = new RealmList<>();
        this.userIds.addAll(userIds);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(status);
        parcel.writeString(image);
        ArrayList<MyString> userIdsToParse = new ArrayList<>();
        userIdsToParse.addAll(this.userIds);
        parcel.writeTypedList(userIdsToParse);
        parcel.writeTypedList(users);
    }

    public static boolean validate(Group group) {
        return group != null && group.getId() != null && group.getName() != null && group.getStatus() != null && group.getUserIds() != null;
    }
}
