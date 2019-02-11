package eu.senseable.cigarettetracker;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
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

import eu.senseable.SQLiteDatabaseModule.SmokingEvent;
import eu.senseable.SQLiteDatabaseModule.SmokingEventRepository;


public class SynchronizeService extends JobIntentService {
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

        List<SmokingEvent> smokingEvents;
        List<SmokingEvent> unsynchronizedEvents;
        List<SmokingEvent> receivedEvents;
        /* TODO memory leakage issue? */
        dBsyncHandler = new Synchronize(getApplicationContext());

        /* Direct access to database/repository (running in own thread without View): */
        smEvRepo = new SmokingEventRepository(getApplication());

//        Log.i("SynchronizeService", "Executing work: " + intent);
//        String label = intent.getStringExtra("label");
//        if (label == null) {
//            label = intent.toString();
//        }
//        toast("Executing: " + label);
        /* Retrieve the received events from intent: */
        byte[] data = intent.getByteArrayExtra("RECEIVED_EVENTS");
        List<SmokingEventDTO> serialEvents = null;

        /* Cache the received events from watch */
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
            receivedEvents = new ArrayList<>(serialEvents.size());
            for (SmokingEventDTO eventDto : serialEvents) {
                SmokingEvent event = new SmokingEvent("0","0","0",
                        "0","0",false,false,
                        false);
                receivedEvents.add(event.setTransferObject(eventDto));
            }
        }

        /* Search data base for the sync label and send all later events back to watch:*/
        /* Find Sync label*/
        smokingEvents = smEvRepo.getLatestSyncLabelIdTest(); /* TODO rename repo-method */
        if (!smokingEvents.isEmpty()) {
            /* SyncLabel available -> only get not synchronized events */
            lastSyncLabelId = smokingEvents.get(0).getId();
            unsynchronizedEvents = smEvRepo.getNewSyncEventsTest(lastSyncLabelId);
        }
        else{
            /* there has been no SyncLabel set yet -> get all events */
            unsynchronizedEvents = smEvRepo.getAllEventsList();
        }
        /* Send data back to watch */
        //dBsyncHandler.sendSyncMessage(unsynchronizedEvents);
        dBsyncHandler.sendSyncMessage(getTestEvents());

        /* TODO wait for message acknowledge from watch  */

        /* Data received -> import new received elements to database */
        /* TODO ... */
        /* set synchronization label */
        //mSEViewModel.setSyncLabel(latestEventId);
        //syncState = State.IDLE;
        //syncDone = true;
        /* TODO reset syncDone? */

        /* TODO state machine nötig?*/


        Log.i("SimpleJobIntentService", "Completed service @ " + SystemClock.elapsedRealtime());
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

    List<SmokingEvent> getTestEvents(){
        List<SmokingEvent> testEvents = new ArrayList<>(10);
        int size = 10;
        while (size >0){
            SmokingEvent event = new SmokingEvent("0",String.valueOf(10 - size),"0",
                    "0","0",false,false,
                    false);
            testEvents.add(event);

            size -= 1;
        }
        return testEvents;
    }
}
