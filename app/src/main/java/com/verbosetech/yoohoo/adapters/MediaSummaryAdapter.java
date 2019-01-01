package com.verbosetech.whatsclone.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.verbosetech.whatsclone.R;
import com.verbosetech.whatsclone.activities.ImageViewerActivity;
import com.verbosetech.whatsclone.models.AttachmentTypes;
import com.verbosetech.whatsclone.models.Message;
import com.verbosetech.whatsclone.utils.FileUtils;
import com.verbosetech.whatsclone.utils.Helper;
import com.verbosetech.whatsclone.utils.MyFileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by a_man on 6/28/2017.
 */

public class MediaSummaryAdapter extends RecyclerView.Adapter<MediaSummaryAdapter.MyViewHolder> {
    private Context context;
    private ArrayList<Message> dataList;

    private int imageViewDimens;
    private Drawable audioIcon, videoIcon, documentIcon;
    private String myId;

    public MediaSummaryAdapter(Context context, ArrayList<Message> attachments, boolean grid, String myId) {
        this.context = context;
        this.dataList = attachments;
        this.myId = myId;
        imageViewDimens = context.getResources().getDimensionPixelSize(grid ? R.dimen.media_summary_grid : R.dimen.media_summary_strip);
        createDrawables();
    }

    private void createDrawables() {
        audioIcon = ContextCompat.getDrawable(context, R.drawable.ic_audiotrack_24dp);
        audioIcon = DrawableCompat.wrap(audioIcon);
        DrawableCompat.setTint(audioIcon, Color.WHITE);
        DrawableCompat.setTintMode(audioIcon, PorterDuff.Mode.SRC_ATOP);

        videoIcon = ContextCompat.getDrawable(context, R.drawable.ic_video_24dp);
        videoIcon = DrawableCompat.wrap(videoIcon);
        DrawableCompat.setTint(videoIcon, Color.WHITE);
        DrawableCompat.setTintMode(videoIcon, PorterDuff.Mode.SRC_ATOP);

        documentIcon = ContextCompat.getDrawable(context, R.drawable.ic_insert_64dp);
        documentIcon = DrawableCompat.wrap(documentIcon);
        DrawableCompat.setTint(documentIcon, Color.WHITE);
        DrawableCompat.setTintMode(documentIcon, PorterDuff.Mode.SRC_ATOP);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_media_summary, parent, false);
        view.getLayoutParams().height = imageViewDimens;
        view.getLayoutParams().width = imageViewDimens;
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.setData(dataList.get(position));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView textView;

        private File file = null;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.imageView = (ImageView) itemView.findViewById(R.id.mediaImage);
            this.textView = (TextView) itemView.findViewById(R.id.mediaSummary);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (file != null && file.exists()) {
                        if (dataList.get(getAdapterPosition()).getAttachmentType() == AttachmentTypes.IMAGE) {
                            context.startActivity(ImageViewerActivity.newFileInstance(context, Environment.getExternalStorageDirectory() + "/"
                                            +
                                            context.getString(R.string.app_name) + "/" + AttachmentTypes.getTypeName(dataList.get(getAdapterPosition()).getAttachmentType()) + (myId.equals(dataList.get(getAdapterPosition()).getSenderId()) ? "/.sent/" : "")
                                    , dataList.get(getAdapterPosition()).getAttachment().getName()));
                        } else {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            Uri uri = MyFileProvider.getUriForFile(context,
                                    context.getString(R.string.authority),
                                    file);
                            intent.setDataAndType(uri, Helper.getMimeType(context, uri)); //storage path is path of your vcf file and vFile is name of that file.
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            context.startActivity(intent);
                        }
                    } else if (dataList.get(getAdapterPosition()).getAttachmentType() == AttachmentTypes.IMAGE)
                        context.startActivity(ImageViewerActivity.newUrlInstance(context, dataList.get(getAdapterPosition()).getAttachment().getUrl()));
                    else
                        Toast.makeText(context, R.string.file_not_found, Toast.LENGTH_LONG).show();
                }
            });
        }

        public void setData(Message msg) {
            file = new File(Environment.getExternalStorageDirectory() + "/"
                    +
                    context.getString(R.string.app_name) + "/" + AttachmentTypes.getTypeName(msg.getAttachmentType()) + (myId.equals(msg.getSenderId()) ? "/.sent/" : "")
                    , msg.getAttachment().getName());
            switch (msg.getAttachmentType()) {
                case AttachmentTypes.IMAGE:
                    textView.setVisibility(View.GONE);
                    imageView.setBackgroundColor(Color.TRANSPARENT);
                    Glide.with(context).load(msg.getAttachment().getUrl()).apply(new RequestOptions().placeholder(R.drawable.whatsclone_placeholder)).into(imageView);
                    break;
                case AttachmentTypes.AUDIO:
                    imageView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorGreen));
                    imageView.setImageDrawable(audioIcon);
                    if (file.exists()) {
                        Uri uri = Uri.fromFile(file);
                        try {
                            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                            mmr.setDataSource(context, uri);
                            String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                            int millis = Integer.parseInt(durationStr);
                            textView.setVisibility(View.VISIBLE);
                            textView.setText(TimeUnit.MILLISECONDS.toMinutes(millis) + ":" + TimeUnit.MILLISECONDS.toSeconds(millis));
                            mmr.release();
                        } catch (Exception e) {
                        }
                    }
                    //Glide.with(context).load(R.drawable.ic_audiotrack_24dp).into(imageView);
                    break;
                case AttachmentTypes.VIDEO:
                    textView.setVisibility(View.VISIBLE);
                    imageView.setBackgroundColor(Color.TRANSPARENT);
                    textView.setText(msg.getAttachment().getName());
                    Glide.with(context).load(msg.getAttachment().getData()).apply(new RequestOptions().placeholder(videoIcon)).into(imageView);
                    //imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.video_file));
                    //Glide.with(context).load(R.drawable.ic_video_24dp).into(imageView);
                    break;
                case AttachmentTypes.DOCUMENT:
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(msg.getAttachment().getName() + "." + FileUtils.getExtension(msg.getAttachment().getName()));
                    imageView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorGreen));
                    imageView.setImageDrawable(documentIcon);
                    //Glide.with(context).load(R.drawable.ic_insert_64dp).into(imageView);
                    break;
            }
        }
    }
}
