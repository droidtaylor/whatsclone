package com.verbosetech.whatsclone.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.verbosetech.whatsclone.R;
import com.verbosetech.whatsclone.adapters.ContactsAdapter;
import com.verbosetech.whatsclone.models.Attachment;
import com.verbosetech.whatsclone.models.DownloadFileEvent;
import com.verbosetech.whatsclone.utils.Helper;

import java.io.File;

import ezvcard.Ezvcard;
import ezvcard.VCard;

public class ContactViewerActivity extends AppCompatActivity {
    private ImageView contactImage;
    private TextView contactName;
    private RecyclerView contactPhones, contactEmails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_v_card_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Contact");

        contactImage = (ImageView) findViewById(R.id.contactImage);
        contactName = (TextView) findViewById(R.id.contactName);
        contactPhones = (RecyclerView) findViewById(R.id.recyclerPhone);
        contactEmails = (RecyclerView) findViewById(R.id.recyclerEmail);

        contactPhones.setLayoutManager(new LinearLayoutManager(this));
        contactEmails.setLayoutManager(new LinearLayoutManager(this));

        final Attachment attachment = ((Attachment) getIntent().getParcelableExtra("data"));
        final boolean isMine = getIntent().getBooleanExtra("what", false);
        final VCard vcard = Ezvcard.parse(attachment.getData()).first();

        View view = findViewById(R.id.contactAdd);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (attachment != null) {
                    File file = new File(Environment.getExternalStorageDirectory() + "/"
                            +
                            getString(R.string.app_name) + "/" + "Contact" + (isMine ? "/.sent/" : "")
                            , attachment.getName());
                    if (file.exists()) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        Uri uri = FileProvider.getUriForFile(ContactViewerActivity.this,
                                getString(R.string.authority),
                                file);
                        intent.setDataAndType(uri, Helper.getMimeType(ContactViewerActivity.this, uri)); //storage path is path of your vcf file and vFile is name of that file.
                        startActivity(intent);
                    } else if (!isMine) {
                        Intent intent = new Intent(Helper.BROADCAST_DOWNLOAD_EVENT);
                        intent.putExtra("data", new DownloadFileEvent(0, attachment, -1));
                        LocalBroadcastManager.getInstance(ContactViewerActivity.this).sendBroadcast(intent);
                    } else {
                        Toast.makeText(ContactViewerActivity.this, "File unavailable", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        if (vcard.getPhotos().size() > 0)
            Glide.with(this).load(vcard.getPhotos().get(0).getData()).apply(new RequestOptions().dontAnimate()).into(contactImage);

        contactName.setText(vcard.getFormattedName().getValue());

        contactPhones.setAdapter(new ContactsAdapter(this, vcard.getTelephoneNumbers(), null));
        contactEmails.setAdapter(new ContactsAdapter(this, null, vcard.getEmails()));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
