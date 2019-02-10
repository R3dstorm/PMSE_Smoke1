package eu.senseable.cigarettetracker;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;


public class SyncMessageService extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        /* If the message’s path equals "/smokeSync"...*/
        if (messageEvent.getPath().equals("/watch/newSmokeEvents")) {

            /* ...retrieve the message */
            //final String message = new String(messageEvent.getData());
            final byte[] message = messageEvent.getData();

            Intent messageIntent = new Intent();
            messageIntent.setAction(Intent.ACTION_SEND);
            messageIntent.putExtra("message", message);

            /* Broadcast the received Data Layer messages locally */
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
        }
        else {
            super.onMessageReceived(messageEvent);
        }
    }
}