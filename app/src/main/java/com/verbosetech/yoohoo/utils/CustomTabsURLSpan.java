package com.verbosetech.whatsclone.utils;

import android.os.Parcel;
import android.text.style.URLSpan;
import android.view.View;

/**
 * Created by a_man on 11-01-2018.
 */

public class CustomTabsURLSpan extends URLSpan {
    public CustomTabsURLSpan(String url) {
        super(url);
    }

    public CustomTabsURLSpan(Parcel src) {
        super(src);
    }

    @Override
    public void onClick(View widget) {
        String url = getURL();
        Helper.loadUrl(widget.getContext(), url);
    }
}
