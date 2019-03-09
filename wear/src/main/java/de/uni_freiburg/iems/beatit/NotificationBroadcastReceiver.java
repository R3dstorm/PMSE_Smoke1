package eu.senseable.cigarettetracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.uni_freiburg.iems.beatit.EcologicalMomentaryAssesmentActivity;

/**
 * Created by phil on 29.05.18.
 */

public class NotificationBroadcastReceiver extends BroadcastReceiver {

    public static final String LOGCIG_ACTION = "logcig";

    @Override
    public void onReceive(Context context, Intent intent) {
        intent.setClass(context, EcologicalMomentaryAssesmentActivity.class);
        //intent.setAction(LOGCIG_ACTION);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
