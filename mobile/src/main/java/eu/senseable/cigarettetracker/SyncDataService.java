package eu.senseable.cigarettetracker;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;


public class SyncDataService extends WearableListenerService {

    private static final String TAG = "SyncDataService";
    private static final String dataReceivePath = "/watch/newElements";
    private static final String DATA_ITEM_RECEIVED_PATH = "/phone/data-item-received";

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onDataChanged: " + dataEvents);
        }

        // Loop through the events and send a message
        // to the node that created the data item.
        for (DataEvent event : dataEvents) {
            Uri uri = event.getDataItem().getUri();
            if (uri.getPath().equals(dataReceivePath)) {
                // Get the node id from the host value of the URI
                String nodeId = uri.getHost();
                // Set the data of the message to be the bytes of the URI
                byte[] payload = uri.toString().getBytes();

                byte[] receivedPayload = event.getDataItem().getData();

                // Send the RPC
                Wearable.getMessageClient(this).sendMessage(
                        nodeId, DATA_ITEM_RECEIVED_PATH, payload);

                //Broadcast the received data layer data//
                Intent messageIntent = new Intent();
                messageIntent.setAction(Intent.ACTION_SEND); /* TODO change Action type? */
                messageIntent.putExtra("newElements", receivedPayload);
                LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
            }
            else {
                super.onDataChanged(dataEvents);
            }
        }
    }
}
