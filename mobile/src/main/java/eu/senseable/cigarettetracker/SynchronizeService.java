package eu.senseable.cigarettetracker;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.JobIntentService;
import android.util.Log;
import android.widget.Toast;

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
import eu.senseable.SQLiteDatabaseModule.SmokingEventRepository;


public class SynchronizeService extends JobIntentService {
    /**
     * Unique job ID for this service.
     */
    static final int JOB_ID = 1000;
    final Handler mHandler = new Handler();
    private SmokingEventRepository smEvRepo;
    private Context myContext;

    int lastSyncLabelId = 0;                /* Holds the id of the latest sync label*/
    int latestEventId = 0;
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
        myContext = getApplicationContext();

        /* Direct access to database/repository (running in own thread without View): */
        smEvRepo = new SmokingEventRepository(getApplication());

//        Log.i("SynchronizeService", "Executing work: " + intent);
//        String label = intent.getStringExtra("label");
//        if (label == null) {
//            label = intent.toString();
//        }
        toast("Sync Starting");

        /* Data received -> retrieve the new events: */
        /* Get the received events from phone: */
        byte[] data = intent.getByteArrayExtra("RECEIVED_EVENTS");
        receivedSmokingEvents = deserializeEvents(data);

        /* Search data base for the sync label and send all later events back to watch:*/
        /* Find Sync label*/
        newSmokingEvents = smEvRepo.getLatestSyncLabelIdTest(); /* TODO rename repo-method */
        if (!newSmokingEvents.isEmpty()) {
            /* SyncLabel available -> only get not synchronized events */
            lastSyncLabelId = newSmokingEvents.get(0).getId();
            newSmokingEvents = smEvRepo.getNewSyncEventsTest(lastSyncLabelId);
        }
        else{
            /* there has been no SyncLabel set yet -> get all events */
            newSmokingEvents = smEvRepo.getAllEventsList();
        }
        /* Send data back to watch */
        sendSyncMessage(newSmokingEvents);
        //sendSyncMessage(getTestEvents());

        /* import retrieved elements to database: */
        for (SmokingEvent smokingEvent : receivedSmokingEvents) {
            smEvRepo.insertBlocking(smokingEvent);
        }

        /* set synchronization label */
        List<SmokingEvent> latestEvent = smEvRepo.getLatestEventIdTest();
        if (!latestEvent.isEmpty()) {
            latestEventId = latestEvent.get(0).getId();
            smEvRepo.setSyncLabel(latestEventId);
        }
        Log.i("SimpleJobIntentService", "Completed service @ " + SystemClock.elapsedRealtime());
        smEvRepo = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        toast("Sync Finished");
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

    private void sendSyncMessage(List<SmokingEvent> unsynchronizedEvents) {
        byte[] messageData = null;

        /* Convert SmokingEvents to de/serializable objects */
        List<SmokingEventDTO> serialEvents = new ArrayList<>(unsynchronizedEvents.size());
        for (SmokingEvent event : unsynchronizedEvents) {
            serialEvents.add(event.getTransferObject());
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(serialEvents);
            out.flush();
            messageData = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        String dataPath = "/phone/newSmokeEvents";
        new SendMessage(dataPath, messageData).start();
    }

    class SendMessage extends Thread {
        String path;
        byte[] messageData;

        /* Constructor */
        SendMessage(String p, byte[] m) {
            path = p;
            messageData = m;
        }

        /* Send the message via the thread. This will send the message to all the currently-connected devices */
        public void run() {

            /* Get all the nodes */
            Task<List<Node>> nodeListTask =
                    Wearable.getNodeClient(myContext).getConnectedNodes();
            try {
                /* Block on a task and get the result synchronously */
                List<Node> nodes = Tasks.await(nodeListTask);

                /* Send the message to each device */
                for (Node node : nodes) {
                    Task<Integer> sendMessageTask =
                            Wearable.getMessageClient(myContext).sendMessage(node.getId(), path, messageData);
                    try {
                        Integer result = Tasks.await(sendMessageTask);
                    }
                    catch (ExecutionException exception) {
                        //TO DO//
                    }
                    catch (InterruptedException exception) {
                        //TO DO//
                    }
                }
            }
            catch (ExecutionException exception) {
                //TO DO//
            }
            catch (InterruptedException exception) {
                //TO DO//
            }
        }
    }
}
