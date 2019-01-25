package de.senseable.cloudsync;

import android.content.ContentResolver;
import android.net.Uri;

/**
 * Created by phil on 02.07.18.
 */

public class Contract {
    public static final String TIMESTAMP = "timestamp";
    public static final String ID        = "id";
    public static final String AUTHORITY = "de.senseable.manual";
    public static final String EVENTS    = "events";

    public static final Uri EVENTSURI = Uri.parse("content://"+AUTHORITY + "/" + EVENTS);

    public static Uri EVENTURI(Long i) {
        return Uri.withAppendedPath(EVENTSURI, i.toString());
    }
}
