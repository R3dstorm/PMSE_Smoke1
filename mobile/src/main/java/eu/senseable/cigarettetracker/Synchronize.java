package eu.senseable.cigarettetracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.commondataobjects.SmokingEventDTO;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import eu.senseable.SQLiteDatabaseModule.SmokingEvent;

public class Synchronize {
    private Handler myHandler;
    private Context myContext;
    private int receivedMessageNumber;
    private int sentMessageNumber;
    private static final String TAG_SYNC = "SYNCHRONIZE";

    private List<SmokingEvent> unsynchronizedEvents;
    Receiver messageReceiver = new Receiver();

    Synchronize (Context context){

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
