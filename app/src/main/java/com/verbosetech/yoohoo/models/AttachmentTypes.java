package com.verbosetech.whatsclone.models;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by mayank on 10/5/17.
 */

public class AttachmentTypes {

    public static final int CONTACT = 0;
    public static final int VIDEO = 1;
    public static final int IMAGE = 2;
    public static final int AUDIO = 3;
    public static final int LOCATION = 4;
    public static final int DOCUMENT = 5;
    public static final int NONE_TEXT = 6;
    public static final int NONE_TYPING = 7;
    public static final int RECORDING = 8;

    @IntDef({CONTACT, VIDEO, IMAGE, AUDIO, LOCATION, DOCUMENT, NONE_TEXT, NONE_TYPING, RECORDING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AttachmentType {
    }

    public static String getTypeName(@AttachmentType int attachmentType) {
        switch (attachmentType) {
            case AUDIO:
                return "Audio";
            case VIDEO:
                return "Video";
            case CONTACT:
                return "Contact";
            case DOCUMENT:
                return "Document";
            case IMAGE:
                return "Image";
            case LOCATION:
                return "Location";
            case NONE_TEXT:
                return "none_text";
            case NONE_TYPING:
                return "none_typing";
            case RECORDING:
                return "Recording";
            default:
                return "none";
        }
    }
}
