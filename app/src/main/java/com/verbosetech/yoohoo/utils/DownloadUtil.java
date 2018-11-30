package com.verbosetech.whatsclone.utils;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import com.verbosetech.whatsclone.R;
import com.verbosetech.whatsclone.models.AttachmentTypes;
import com.verbosetech.whatsclone.models.DownloadFileEvent;

import java.io.File;

/**
 * Created by mayank on 11/5/17.
 */

public class DownloadUtil {

    public void checkAndLoad(Context context, DownloadFileEvent downloadFileEvent) {

        File file = new File(Environment.getExternalStorageDirectory() + "/"
                +
                context.getString(R.string.app_name) + "/"
                +
                AttachmentTypes.getTypeName(downloadFileEvent.getAttachmentType()), downloadFileEvent.getAttachment().getName());

        if (file.exists()) {
            Intent newIntent = new Intent(Intent.ACTION_VIEW);
            //newIntent.setDataAndType(FileProvider.getUriForFile(context, context.getString(R.string.authority), file), Helper.getMimeType(context, downloadFileEvent.getAttachment().getData()));
            newIntent.setDataAndType(FileProvider.getUriForFile(context, context.getString(R.string.authority), file), Helper.getMimeType(context, Uri.fromFile(file)));
            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            newIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                context.startActivity(newIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, "No handler for this type of file.", Toast.LENGTH_LONG).show();
            }
        } else {
            downloadFile(context, downloadFileEvent.getAttachment().getUrl(), AttachmentTypes.getTypeName(downloadFileEvent.getAttachmentType()), downloadFileEvent.getAttachment().getName());
            Toast.makeText(context, "Downloading attachment", Toast.LENGTH_SHORT).show();
        }
    }

    private void downloadFile(Context context, String url, String type, String fileName) {
        DownloadManager mgr = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(context.getString(R.string.app_name))
                .setDescription("Downloading " + fileName)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(getDirectoryPath(context) + "/" + type, fileName);
        mgr.enqueue(request);
    }

    private String getDirectoryPath(Context context) {
        return "/" + context.getString(R.string.app_name);
    }
}
