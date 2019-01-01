package com.verbosetech.whatsclone.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.verbosetech.whatsclone.utils.Helper;

import io.realm.RealmModel;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

/**
 * Created by a_man on 5/4/2017.
 */

@RealmClass
public class User implements Parcelable, RealmModel {
    @Ignore
    private boolean online;
    @Exclude
    private String nameInPhone;
    @Ignore
    private boolean typing;

    @Ignore
    @Exclude
    private boolean selected;
    @Ignore
    @Exclude
    private boolean inviteAble;
    @PrimaryKey
    private String id;
    private String name, status, image;

    public User() {
    }

    protected User(Parcel in) {
        online = in.readByte() != 0;
        nameInPhone = in.readString();
        typing = in.readByte() != 0;
        selected = in.readByte() != 0;
        inviteAble = in.readByte() != 0;
        id = in.readString();
        name = in.readString();
        status = in.readString();
        image = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public User(String id, String name, String status, String image) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.online = false;
        this.image = image;
        this.typing = false;
        this.inviteAble = false;
    }

    public User(String id, String name) {
        this.id = id;
        this.name = name;
        this.status = "";
        this.online = false;
        this.image = "";
        this.typing = false;
        this.inviteAble = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return id.equals(user.id);
    }

    public String getNameInPhone() {
        return nameInPhone;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public boolean isTyping() {
        return typing;
    }

    public void setTyping(boolean typing) {
        this.typing = typing;
    }

    public void setNameInPhone(String nameInPhone) {
        this.nameInPhone = nameInPhone;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isOnline() {
        return online;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public String getNameToDisplay() {
        return (this.nameInPhone != null) ? this.nameInPhone : this.name;
    }

    public boolean isInviteAble() {
        return inviteAble;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByte((byte) (online ? 1 : 0));
        parcel.writeString(nameInPhone);
        parcel.writeByte((byte) (typing ? 1 : 0));
        parcel.writeByte((byte) (selected ? 1 : 0));
        parcel.writeByte((byte) (inviteAble ? 1 : 0));
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(status);
        parcel.writeString(image);
    }

    public static boolean validate(User user) {
        return user != null && user.getId() != null && user.getName() != null && user.getStatus() != null;
    }
}
