package com.verbosetech.whatsclone.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.verbosetech.whatsclone.R;
import com.verbosetech.whatsclone.models.AttachmentTypes;
import com.verbosetech.whatsclone.models.Message;

import java.io.File;

/**
 * Created by mayank on 10/5/17.
 */

public class ImageViewerActivity extends AppCompatActivity {
    private static final String IMAGE_URL = ImageViewerActivity.class.getPackage().getName() + ".image_url";
    private static final String IMAGE_FILE_PATH = ImageViewerActivity.class.getPackage().getName() + ".image_file_path";
    private static final String IMAGE_FILE_NAME = ImageViewerActivity.class.getPackage().getName() + ".image_file_name";


    public static Intent newUrlInstance(Context context, String url) {
        Intent intent = new Intent(context, ImageViewerActivity.class);
        intent.putExtra(IMAGE_URL, url);
        return intent;
    }

    public static Intent newFileInstance(Context context, String fileParent, String fileName) {
        Intent intent = new Intent(context, ImageViewerActivity.class);
        intent.putExtra(IMAGE_FILE_PATH, fileParent);
        intent.putExtra(IMAGE_FILE_NAME, fileName);
        return intent;
    }

    PhotoView photoView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        photoView = findViewById(R.id.photo_view);

        String imageUrl = getIntent().getStringExtra(IMAGE_URL);
        String imageFileParent = getIntent().getStringExtra(IMAGE_FILE_PATH);
        String imageFileName = getIntent().getStringExtra(IMAGE_FILE_NAME);
        if (!TextUtils.isEmpty(imageFileParent) && !TextUtils.isEmpty(imageFileName)) {
            File file = new File(imageFileParent, imageFileName);
            if (file.exists()) {
                Glide.with(this).load(file).into(photoView);
            }
        } else if (!TextUtils.isEmpty(imageUrl)) {
            Glide.with(this).load(imageUrl).into(photoView);
        }
    }
}
