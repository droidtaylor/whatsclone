package com.verbosetech.whatsclone.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mayank on 11/5/17.
 */

public class DownloadFileEvent implements Parcelable {
    private Attachment attachment;
    private int position, attachmentType;

    public DownloadFileEvent(int attachmentType, Attachment attachment, int adapterPosition) {
        this.attachment = attachment;
        this.attachmentType = attachmentType;
        this.position = adapterPosition;
    }

    protected DownloadFileEvent(Parcel in) {
        attachment = in.readParcelable(Attachment.class.getClassLoader());
        position = in.readInt();
        attachmentType = in.readInt();
    }

    public static final Creator<DownloadFileEvent> CREATOR = new Creator<DownloadFileEvent>() {
        @Override
        public DownloadFileEvent createFromParcel(Parcel in) {
            return new DownloadFileEvent(in);
        }

        @Override
        public DownloadFileEvent[] newArray(int size) {
            return new DownloadFileEvent[size];
        }
    };

    public int getPosition() {
        return position;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    @AttachmentTypes.AttachmentType
    public int getAttachmentType() {
        return attachmentType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(attachment, flags);
        dest.writeInt(position);
        dest.writeInt(attachmentType);
    }
}

