package de.uni_freiburg.iems.beatit;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import eu.senseable.cigarettetracker.NotificationBroadcastReceiver;


public class MediatorForegroundService extends Service {

    private int ONGOING_NOTIFICATION_ID = 1;
    private String CHANNEL_ID = "BeatNotification";
    private String CHANNEL_DEFAULT_IMPORTANCE = "IMPORTANCE_HIGH";

    @Override
    public void onCreate() {
        /* TODO finish new intent for starting mediator service */
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(this, NotificationBroadcastReceiver.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "abc", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder notification =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.beatsmoking)
                        .setContentTitle(getText(R.string.notification_title))
                        .setContentText(getText(R.string.notification_message))
                        .setContentIntent(pendingIntent);
        /* TODO call service */
        startForeground(ONGOING_NOTIFICATION_ID, notification.build());

// Issue the notification with notification manager.
        //notificationManager.notify(ONGOING_NOTIFICATION_ID,notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Toast.makeText(this, "Detection started", Toast.LENGTH_SHORT).show();


        /* If we get killed, after returning from here, restart */
        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
