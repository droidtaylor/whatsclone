package com.verbosetech.whatsclone.viewHolders;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.verbosetech.whatsclone.R;
import com.verbosetech.whatsclone.interfaces.OnMessageItemClick;
import com.verbosetech.whatsclone.models.AttachmentTypes;
import com.verbosetech.whatsclone.models.Message;
import com.verbosetech.whatsclone.models.User;
import com.verbosetech.whatsclone.utils.FileUtils;
import com.verbosetech.whatsclone.utils.Helper;
import com.verbosetech.whatsclone.utils.MyFileProvider;

import java.io.File;
import java.util.HashMap;

/**
 * Created by mayank on 11/5/17.
 */

public class MessageAttachmentVideoViewHolder extends BaseMessageViewHolder {
    TextView text;
    TextView durationOrSize;
    ImageView videoThumbnail;
    ImageView videoPlay;
    LinearLayout ll;
    ProgressBar progressBar;

    private Message message;
    private File file;

    public MessageAttachmentVideoViewHolder(View itemView, OnMessageItemClick itemClickListener) {
        super(itemView, itemClickListener);
        text = itemView.findViewById(R.id.text);
        durationOrSize = itemView.findViewById(R.id.videoSize);
        videoThumbnail = itemView.findViewById(R.id.videoThumbnail);
        videoPlay = itemView.findViewById(R.id.videoPlay);
        ll = itemView.findViewById(R.id.container);
        progressBar = itemView.findViewById(R.id.progressBar);
        itemView.findViewById(R.id.videoPlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadFile();
            }
        });
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick(true);
            }
        });

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onItemClick(false);
                return true;
            }
        });
    }

    @Override
    public void setData(Message message, int position) {
        super.setData(message, position);
        this.message = message;
        cardView.setCardBackgroundColor(ContextCompat.getColor(context, message.isSelected() ? R.color.colorPrimary : R.color.colorBgLight));
        ll.setBackgroundColor(message.isSelected() ? ContextCompat.getColor(context, R.color.colorPrimary) : isMine() ? Color.WHITE : ContextCompat.getColor(context, R.color.colorBgLight));

        boolean loading = message.getAttachment().getUrl().equals("loading");
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        videoPlay.setVisibility(loading ? View.GONE : View.VISIBLE);

        file = new File(Environment.getExternalStorageDirectory() + "/"
                +
                context.getString(R.string.app_name) + "/" + AttachmentTypes.getTypeName(message.getAttachmentType()) + (isMine() ? "/.sent/" : "")
                , message.getAttachment().getName());

        if (file.exists()) {
//            Uri uri = Uri.fromFile(file);
//            try {
//                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
//                mmr.setDataSource(context, uri);
//                String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
//                long millis = Long.parseLong(durationStr);
//                durationOrSize.setText(TimeUnit.MILLISECONDS.toMinutes(millis) + ":" + TimeUnit.MILLISECONDS.toSeconds(millis));
//                Log.e("CHECK", String.valueOf(millis));
//                mmr.release();
//            } catch (RuntimeException e) {
//                Cursor cursor = MediaStore.Video.query(context.getContentResolver(), uri, new
//                        String[]{MediaStore.Video.VideoColumns.DURATION});
//                long duration = 0;
//                if (cursor != null && cursor.moveToFirst()) {
//                    duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DURATION));
//                    Log.e("CHECK", String.valueOf(duration));
//                    durationOrSize.setText(TimeUnit.MILLISECONDS.toMinutes(duration) + ":" + TimeUnit.MILLISECONDS.toSeconds(duration));
//                }
//                if (cursor != null && !cursor.isClosed())
//                    cursor.close();
//            }
        } else
            durationOrSize.setText(FileUtils.getReadableFileSize(message.getAttachment().getBytesCount()));
        text.setText(message.getAttachment().getName());
        Glide.with(context).load(message.getAttachment().getData()).apply(new RequestOptions().placeholder(R.drawable.ic_video_24dp).centerCrop()).into(videoThumbnail);
        videoPlay.setImageDrawable(ContextCompat.getDrawable(context, file.exists() ? R.drawable.ic_play_circle_outline : R.drawable.ic_file_download_40dp));
    }

    public void downloadFile() {
        if (!Helper.CHAT_CAB)
            if (file.exists()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = MyFileProvider.getUriForFile(context,
                        context.getString(R.string.authority),
                        file);
                intent.setDataAndType(uri, Helper.getMimeType(context, uri)); //storage path is path of your vcf file and vFile is name of that file.
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intent);
            } else if (!isMine())
                broadcastDownloadEvent();
            else
                Toast.makeText(context, "File unavailable", Toast.LENGTH_SHORT).show();
    }
}
