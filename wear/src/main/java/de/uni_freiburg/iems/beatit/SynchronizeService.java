package de.uni_freiburg.iems.beatit;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.JobIntentService;
import android.util.Log;
import android.widget.Toast;

import com.example.commondataobjects.SmokingEventDTO;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.opencsv.CSVWriter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

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

    boolean nodeIsNearby = false;
    private static final String CAPABILITY_NAME = "beat_smoking";
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

            /* find a connected device: */
            nodeIsNearby = nodeNearby();

            if (nodeIsNearby) {
                toast("Sync Starting");

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
            else{
                toast("No device to sync available ");
            }
        }
        else if (intent.getBooleanExtra("NEW_EVENTS_RECEIVED",false) == true){

            /* Data received -> import new received elements to database: */
            /* Get the received events from phone: */
            byte[] data = intent.getByteArrayExtra("NEW_EVENTS_DATA");
            receivedSmokingEvents = deserializeEvents(data);

            /* import the events into the data base: */
            for (SmokingEvent smokingEvent : receivedSmokingEvents) {
                smEvRepo.insertBlocking(smokingEvent);
            }

            /* set synchronization label */
            List<SmokingEvent> latestEvent = smEvRepo.getLatestEventIdTest();
            if (!latestEvent.isEmpty()) {
                latestEventId = latestEvent.get(0).getId();
                smEvRepo.setSyncLabel(latestEventId);
            }
        }

        else if (intent.getBooleanExtra("REQUEST_SYNC_HASH_LIST",false) == true){
            /* send request to receive a hashlist for already synced data files */
            dBsyncHandler.requestHashListMessage();
        }

        else if (intent.getBooleanExtra("SYNC_HASH_LIST_RECEIVED",false) == true){
            /* compare hashes of available data with hash list to get unsynchronized data */
            /* Get the received hash list from phone: */
            List<byte[]> receivedHashList = null;
            List<byte[]> internalHashList = new ArrayList<byte[]>();
            File fileList[] = null;
            ExternalStorageController storageController = new ExternalStorageController();

            byte[] data = intent.getByteArrayExtra("RECEIVED_HASH_LIST");
            receivedHashList = deserializeHashList(data);

            /* Get locally stored data and compare hash values */
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                File dir = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
                fileList = dir.listFiles();
                if (fileList != null) {
                    int numFiles = fileList.length;
                    for (int i=0; i<numFiles; i++){
                        internalHashList.add(storageController.calcHash(fileList[i]));
                    }
                }
                else{
                    /* no files available -> nothing to synchronize */
                }
                /* Compare Hash lists - Find internal hash list elements that are
                 * not part of the received hash list:*/
                int numInternFiles          = internalHashList.size();
                int[] syncindex             = new int[numInternFiles];
                int numberOfElementsToSync  = 0;

                for (int i=0; i<numInternFiles; i++){
                    if (receivedHashList.contains(internalHashList.get(i))){
                        /* TODO directly store not synced elements to a send-list? */
                        syncindex[numberOfElementsToSync] = i;
                        numberOfElementsToSync++;
                    }
                }

                /* TODO send all not synchronized files to phone */
            }
            else{
                Log.e("SynchronizeService", "Storage not available");
            }

        }

        Log.i("SimpleJobIntentService", "Completed service @ " + SystemClock.elapsedRealtime());
        dBsyncHandler = null;
        smEvRepo = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(nodeIsNearby) {
            toast("Sync Finished");
        }
    }

    // Helper for showing tests
    void toast(final CharSequence text) {
        mHandler.post(new Runnable() {
            @Override public void run() {
                Toast.makeText(SynchronizeService.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean nodeNearby() {
        boolean result = false;
        CapabilityInfo capabilityInfo = null;

        try {
            capabilityInfo = Tasks.await(
                    Wearable.getCapabilityClient(getApplicationContext()).getCapability(
                            CAPABILITY_NAME, CapabilityClient.FILTER_REACHABLE));
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Set<Node> connectedNodes = capabilityInfo.getNodes();
        for (Node node : connectedNodes) {
            if (node.isNearby()) {
                result = true;
            }
        }
        return result;
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

    List<byte[]> deserializeHashList(byte[] data){
        List<byte[]> deserializedHashList = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            deserializedHashList = (List<byte[]>)in.readObject();
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
        return deserializedHashList;
    }
}
