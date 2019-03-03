package de.uni_freiburg.iems.beatit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationResultReceiver extends BroadcastReceiver {

    @Override public void onReceive(Context context, Intent i) {
        final int code = getResultCode();
        if(code != -1) {
            Log.i("ML", "notification result: " + code);
        }
    }
}
