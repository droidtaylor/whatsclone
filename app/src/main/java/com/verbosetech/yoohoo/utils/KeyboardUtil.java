package com.verbosetech.whatsclone.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by mayank on 8/10/16.
 */

public class KeyboardUtil {

    private static KeyboardUtil keyboardUtil;
    private InputMethodManager inputMethodManager;
    private Activity activity;

    private KeyboardUtil() {
        //no instance
    }

    public static KeyboardUtil getInstance(Activity activity) {
        if (keyboardUtil == null) keyboardUtil = new KeyboardUtil(activity);
        return keyboardUtil;
    }

    private KeyboardUtil(Activity activity) {
        this.activity = activity;
        inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public void closeKeyboard() {
        View view = activity.getCurrentFocus();
        if (view != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void closeKeyboard(View view) {
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void openKeyboard() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                View view = activity.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(view, 0);
                }
            }
        });
    }
}
