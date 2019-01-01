package com.verbosetech.whatsclone.models;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmModel;
import io.realm.annotations.RealmClass;

/**
 * Created by a_man on 31-12-2017.
 */

@RealmClass
public class MyString implements Parcelable, RealmModel {
    private String string;

    public MyString(String string) {
        this.string = string;
    }

    public MyString() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MyString myString = (MyString) o;

        return string.equals(myString.string);
    }

    @Override
    public int hashCode() {
        return string.hashCode();
    }

    protected MyString(Parcel in) {
        string = in.readString();
    }

    public static final Creator<MyString> CREATOR = new Creator<MyString>() {
        @Override
        public MyString createFromParcel(Parcel in) {
            return new MyString(in);
        }

        @Override
        public MyString[] newArray(int size) {
            return new MyString[size];
        }
    };

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(string);
    }
}
