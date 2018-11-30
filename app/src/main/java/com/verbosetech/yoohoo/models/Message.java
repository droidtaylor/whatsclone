package com.verbosetech.whatsclone.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.annotations.Ignore;
import io.realm.annotations.RealmClass;

/**
 * Created by a_man on 1/10/2017.
 */

@RealmClass
public class Message implements Parcelable, RealmModel {
    private String senderName, senderImage, senderStatus;
    private String recipientName, recipientImage, recipientStatus;
    private String body, id, recipientId, senderId;
    private RealmList<MyString> recipientGroupIds;
    private long date;
    private boolean delivered = false, sent = false;
    private
    @AttachmentTypes.AttachmentType
    int attachmentType;
    private Attachment attachment;

    @Ignore
    private boolean selected;

    public Message() {
    }

    public Message(int attachmentType) {
        this.attachmentType = attachmentType;
        this.senderId = "";
    }

    protected Message(Parcel in) {
        senderName = in.readString();
        senderImage = in.readString();
        senderStatus = in.readString();
        recipientName = in.readString();
        recipientImage = in.readString();
        recipientStatus = in.readString();
        body = in.readString();
        id = in.readString();
        recipientId = in.readString();
        senderId = in.readString();
        date = in.readLong();
        delivered = in.readByte() != 0;
        sent = in.readByte() != 0;
        attachmentType = in.readInt();
        attachment = in.readParcelable(Attachment.class.getClassLoader());
        selected = in.readByte() != 0;
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;

        Message message = (Message) o;

        return getId() != null ? getId().equals(message.getId()) : message.getId() == null;

    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderImage() {
        return senderImage;
    }

    public void setSenderImage(String senderImage) {
        this.senderImage = senderImage;
    }

    public String getSenderStatus() {
        return senderStatus;
    }

    public void setSenderStatus(String senderStatus) {
        this.senderStatus = senderStatus;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientImage() {
        return recipientImage;
    }

    public void setRecipientImage(String recipientImage) {
        this.recipientImage = recipientImage;
    }

    public String getRecipientStatus() {
        return recipientStatus;
    }

    public void setRecipientStatus(String recipientStatus) {
        this.recipientStatus = recipientStatus;
    }

    public RealmList<MyString> getRecipientGroupIds() {
        return recipientGroupIds;
    }

    public void setRecipientGroupIds(ArrayList<MyString> recipientGroupIds) {
        this.recipientGroupIds = new RealmList<>();
        if (recipientGroupIds != null && !recipientGroupIds.isEmpty())
            this.recipientGroupIds.addAll(recipientGroupIds);
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    @AttachmentTypes.AttachmentType
    public int getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(int attachmentType) {
        this.attachmentType = attachmentType;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(senderName);
        dest.writeString(senderImage);
        dest.writeString(senderStatus);
        dest.writeString(recipientName);
        dest.writeString(recipientImage);
        dest.writeString(recipientStatus);
        dest.writeString(body);
        dest.writeString(id);
        dest.writeString(recipientId);
        dest.writeString(senderId);
        dest.writeLong(date);
        dest.writeByte((byte) (delivered ? 1 : 0));
        dest.writeByte((byte) (sent ? 1 : 0));
        dest.writeInt(attachmentType);
        dest.writeParcelable(attachment, flags);
        dest.writeByte((byte) (selected ? 1 : 0));
    }
}