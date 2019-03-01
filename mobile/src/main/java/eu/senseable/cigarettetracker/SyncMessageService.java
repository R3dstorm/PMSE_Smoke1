package eu.senseable.cigarettetracker;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;


public class SyncMessageService extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        /* Receive incoming data for synchronization from path "/watch/newSmokeEvents" */
        if (messageEvent.getPath().equals("/watch/newSmokeEvents")) {

            /* ...retrieve the message */
            final byte[] receivedEvents = messageEvent.getData();

            Intent messageIntent = new Intent();
            messageIntent.setAction(Intent.ACTION_SEND);
            messageIntent.putExtra("NEW_EVENTS_DATA", receivedEvents);

            /* Broadcast the received Data Layer messages locally -> send to Synchronize */
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
        }
        else if (messageEvent.getPath().equals("/watch/newSensorData")){

            Intent synchronizeServiceIntent = new Intent(getApplicationContext(), SynchronizeService.class);
            synchronizeServiceIntent.putExtra("RECEIVED_SYNC_HASH_LIST_REQUEST", true);
            SynchronizeService.enqueueWork(getApplicationContext(), synchronizeServiceIntent);

        }
        else {
            super.onMessageReceived(messageEvent);
        }

    }
}