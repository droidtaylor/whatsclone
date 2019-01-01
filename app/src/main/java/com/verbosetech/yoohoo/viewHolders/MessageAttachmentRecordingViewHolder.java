package com.verbosetech.whatsclone.viewHolders;

import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.verbosetech.whatsclone.R;
import com.verbosetech.whatsclone.interfaces.OnMessageItemClick;
import com.verbosetech.whatsclone.models.Attachment;
import com.verbosetech.whatsclone.models.AttachmentTypes;
import com.verbosetech.whatsclone.models.Message;
import com.verbosetech.whatsclone.models.User;
import com.verbosetech.whatsclone.utils.FileUtils;
import com.verbosetech.whatsclone.utils.Helper;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by a_man on 02-02-2018.
 */

public class MessageAttachmentRecordingViewHolder extends BaseMessageViewHolder {
    TextView text;
    TextView durationOrSize;
    LinearLayout ll;
    ProgressBar progressBar;
    ImageView playPauseToggle;
    private Message message;
    private File file;

    private RecordingViewInteractor recordingViewInteractor;

    public MessageAttachmentRecordingViewHolder(View itemView, OnMessageItemClick itemClickListener, RecordingViewInteractor recordingViewInteractor) {
        super(itemView, itemClickListener);
        text = itemView.findViewById(R.id.text);
        durationOrSize = itemView.findViewById(R.id.duration);
        ll = itemView.findViewById(R.id.container);
        progressBar = itemView.findViewById(R.id.progressBar);
        playPauseToggle = itemView.findViewById(R.id.playPauseToggle);

        this.recordingViewInteractor = recordingViewInteractor;

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Helper.CHAT_CAB)
                    downloadFile();
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
        Attachment attachment = message.getAttachment();

        boolean loading = message.getAttachment().getUrl().equals("loading");
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        playPauseToggle.setVisibility(loading ? View.GONE : View.VISIBLE);

        file = new File(Environment.getExternalStorageDirectory() + "/"
                +
                context.getString(R.string.app_name) + "/" + AttachmentTypes.getTypeName(message.getAttachmentType()) + (isMine() ? "/.sent/" : "")
                , message.getAttachment().getName());
        if (file.exists()) {
            Uri uri = Uri.fromFile(file);
            try {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(context, uri);
                String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                int millis = Integer.parseInt(durationStr);
                durationOrSize.setText(TimeUnit.MILLISECONDS.toMinutes(millis) + ":" + TimeUnit.MILLISECONDS.toSeconds(millis));
                mmr.release();
            } catch (Exception e) {
            }
        } else
            durationOrSize.setText(FileUtils.getReadableFileSize(attachment.getBytesCount()));

        playPauseToggle.setImageDrawable(ContextCompat.getDrawable(context, file.exists() ? recordingViewInteractor.isRecordingPlaying(message.getAttachment().getName()) ? R.drawable.ic_stop : R.drawable.ic_play_circle_outline : R.drawable.ic_file_download_accent_36dp));
        cardView.setCardBackgroundColor(ContextCompat.getColor(context, message.isSelected() ? R.color.colorPrimary : R.color.colorBgLight));
        ll.setBackgroundColor(message.isSelected() ? ContextCompat.getColor(context, R.color.colorPrimary) : isMine() ? Color.WHITE : ContextCompat.getColor(context, R.color.colorBgLight));
    }

    //@OnClick(R.id.playPauseToggle)
    public void downloadFile() {
        if (file.exists()) {
            recordingViewInteractor.playRecording(file, message.getAttachment().getName(), getAdapterPosition());
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            Uri uri = MyFileProvider.getUriForFile(context,
//                    context.getString(R.string.authority),
//                    file);
//            intent.setDataAndType(uri, Helper.getMimeType(context, uri)); //storage path is path of your vcf file and vFile is name of that file.
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            context.startActivity(intent);
        } else if (!isMine() && !message.getAttachment().getUrl().equals("loading")) {
            broadcastDownloadEvent();
        } else {
            Toast.makeText(context, "File unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    public interface RecordingViewInteractor {
        boolean isRecordingPlaying(String fileName);

        void playRecording(File file, String fileName, int position);
    }

}
