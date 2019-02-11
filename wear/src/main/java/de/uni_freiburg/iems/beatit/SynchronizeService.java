package de.uni_freiburg.iems.beatit;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.JobIntentService;
import android.util.Log;
import android.widget.Toast;

import com.example.commondataobjects.SmokingEventDTO;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import SQLiteDatabaseModule.SmokingEvent;
import SQLiteDatabaseModule.SmokingEventRepository;

public class SynchronizeService extends JobIntentService{
    /**
     * Unique job ID for this service.
     */
    static final int JOB_ID = 1000;
    final Handler mHandler = new Handler();
    private SmokingEventRepository smEvRepo;

    int lastSyncLabelId = 0;                /* Holds the id of the latest sync label*/
    int latestEventId = 0;
    private Synchronize dBsyncHandler;      /* Sync handler for data base */
    /**
     * Convenience method for enqueuing work in to this service.
     */
    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, SynchronizeService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(Intent intent) {

        List<SmokingEvent> newSmokingEvents = null;
        List<SmokingEvent> receivedSmokingEvents = null;
        dBsyncHandler = new Synchronize(getApplicationContext());

        /* Direct access to database/repository (running in own thread without View): */
        smEvRepo = new SmokingEventRepository(getApplication());

        if (intent.getBooleanExtra("SEND_NEW_EVENTS",false) == true) {
            Log.i("SynchronizeService", "Executing work: " + intent);
            String label = intent.getStringExtra("label");
            if (label == null) {
                label = intent.toString();
            }
            toast("Executing: " + label);

            /* Search data base for the sync label and send all later events to phone:*/
            /* Find Sync label*/
            newSmokingEvents = smEvRepo.getLatestSyncLabelIdTest(); /* TODO rename repo-method */
            if (!newSmokingEvents.isEmpty()) {
                /* SyncLabel available -> only get not synchronized events */
                lastSyncLabelId = newSmokingEvents.get(0).getId();
                newSmokingEvents = smEvRepo.getNewSyncEventsTest(lastSyncLabelId);
            } else {
                /* there has been no SyncLabel set yet -> get all events */
                newSmokingEvents = smEvRepo.getAllEventsList();
            }
            /* Send data to phone */
            dBsyncHandler.sendSyncMessage(newSmokingEvents);
        }
        else if (intent.getBooleanExtra("NEW_EVENTS_RECEIVED",false) == true){

            /* Data received -> import new received elements to database: */
            /* Get the received events from phone: */
            byte[] data = intent.getByteArrayExtra("NEW_EVENTS_DATA");
            receivedSmokingEvents = deserializeEvents(data);

            /* import the events into the data base: */
            for (SmokingEvent smokingEvent : receivedSmokingEvents) {
                smEvRepo.insert(smokingEvent);
            }

            /* set synchronization label */
            List<SmokingEvent> latestEvent = smEvRepo.getLatestSyncLabelIdTest();
            latestEventId = latestEvent.get(0).getId();
            smEvRepo.setSyncLabel(latestEventId);
        }
        Log.i("SimpleJobIntentService", "Completed service @ " + SystemClock.elapsedRealtime());
        dBsyncHandler = null;
        smEvRepo = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        toast("All work complete");
    }

    // Helper for showing tests
    void toast(final CharSequence text) {
        mHandler.post(new Runnable() {
            @Override public void run() {
                Toast.makeText(SynchronizeService.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    List<SmokingEvent> deserializeEvents(byte[] data){
        List<SmokingEventDTO> serialEvents = null;
        List<SmokingEvent> deserializedEvents = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            serialEvents = (List<SmokingEventDTO>)in.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        /* Convert de/serializable objects to SmokingEvent objects */
        if (serialEvents != null) {
            deserializedEvents = new ArrayList<>(serialEvents.size());
            for (SmokingEventDTO eventDto : serialEvents) {
                SmokingEvent event = new SmokingEvent("0","0","0",
                        "0","0",false,false,
                        false);
                deserializedEvents.add(event.setTransferObject(eventDto));
            }
        }
        return deserializedEvents;
    }
}
