package de.uni_freiburg.iems.beatit;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.JobIntentService;
import android.util.Log;
import android.widget.Toast;

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

        List<SmokingEvent> smokingEvents;
        List<SmokingEvent> unsynchronizedEvents;
        dBsyncHandler = new Synchronize(getApplicationContext());

        /* Direct access to database/repository (running in own thread without View): */
        smEvRepo = new SmokingEventRepository(getApplication());

        Log.i("SynchronizeService", "Executing work: " + intent);
        String label = intent.getStringExtra("label");
        if (label == null) {
            label = intent.toString();
        }
        toast("Executing: " + label);

        /* Search data base for the sync label and send all later events to phone:*/
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
        /* Send data to phone */
        dBsyncHandler.sendSyncMessage(unsynchronizedEvents);
        //syncState = Mediator.State.SYNC_SENT; /* TODO brauchen wir das noch?*/

        /* TODO wait for message with return data received from phone  */

        /* Data received -> import new received elements to database */
        /* TODO ... */
        /* set synchronization label */
        //mSEViewModel.setSyncLabel(latestEventId);
        //syncState = State.IDLE;
        //syncDone = true;
        /* TODO reset syncDone? */

        /* TODO state machine nötig?*/



        for (int i = 0; i < 5; i++) {
            Log.i("SimpleJobIntentService", "Running service " + (i + 1)
                    + "/5 @ " + SystemClock.elapsedRealtime());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
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

}
