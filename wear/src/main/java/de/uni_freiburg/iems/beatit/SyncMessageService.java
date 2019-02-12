package de.uni_freiburg.iems.beatit;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;


public class SyncMessageService extends WearableListenerService{

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        if (messageEvent.getPath().equals("/phone/newSmokeEvents")) {
            final byte[] receivedEvents = messageEvent.getData();

            /* Start Intent job to put received data to database */
            Intent synchronizeServiceIntent = new Intent(getApplicationContext(), SynchronizeService.class);
            synchronizeServiceIntent.putExtra("SEND_NEW_EVENTS", false);
            synchronizeServiceIntent.putExtra("NEW_EVENTS_RECEIVED", true);
            synchronizeServiceIntent.putExtra("NEW_EVENTS_DATA",receivedEvents);
            SynchronizeService.enqueueWork(getApplicationContext(), synchronizeServiceIntent);
        }
        else {
            super.onMessageReceived(messageEvent);
        }
    }
}