package eu.senseable.cigarettetracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

public class Synchronize {
    private Context myContext;
    private int receivedMessageNumber;
    private int sentMessageNumber;
    private static final String TAG_SYNC = "SYNCHRONIZE";

    Receiver messageReceiver = new Receiver();

    Synchronize (Context context) {

        myContext = context;
        receivedMessageNumber = sentMessageNumber = 0;
        //Register to receive local broadcasts, which we'll be creating in the next step//
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);

        LocalBroadcastManager.getInstance(myContext).registerReceiver(messageReceiver, messageFilter);
    }

    //Define a nested class that extends BroadcastReceiver//
    public class Receiver extends BroadcastReceiver {
        @Override

        public void onReceive(Context context, Intent intent) {

            byte[] receivedEvents = intent.getByteArrayExtra("NEW_EVENTS_DATA");

            /* Trigger Synchronize Service Intent */
            Intent synchronizeServiceIntent = new Intent(myContext, SynchronizeService.class);
            synchronizeServiceIntent.putExtra("NEW_EVENTS_RECEIVED", true);
            synchronizeServiceIntent.putExtra ("RECEIVED_EVENTS", receivedEvents);
            SynchronizeService.enqueueWork(myContext, synchronizeServiceIntent);

        }
    }

    public void unregisterReceivers(){
        LocalBroadcastManager.getInstance(myContext).unregisterReceiver(messageReceiver);
    }
}
