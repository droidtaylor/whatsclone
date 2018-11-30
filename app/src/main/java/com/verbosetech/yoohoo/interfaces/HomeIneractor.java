package com.verbosetech.whatsclone.interfaces;

import com.verbosetech.whatsclone.models.Contact;
import com.verbosetech.whatsclone.models.User;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by a_man on 01-01-2018.
 */

public interface HomeIneractor {
    HashMap<String, Contact> getLocalContacts();
}
