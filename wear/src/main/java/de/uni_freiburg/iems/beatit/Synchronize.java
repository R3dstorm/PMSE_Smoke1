package de.uni_freiburg.iems.beatit;


import android.content.Context;
import android.util.Log;

import com.example.commondataobjects.SmokingEventDTO;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import SQLiteDatabaseModule.SmokingEvent;

public class Synchronize {

    private Context myContext;
    private PutDataRequest dataRequest;
    private PutDataMapRequest dataMapRequest;
    private DataClient dataClient;
    private static final String SYNC_KEY = "de.uni_freiburg.iems.beatit";

    private static final String TAG_SYNC = "SYNCHRONIZE";


    Synchronize(Context context) {
        myContext = context;

        /* Handle Data Objects */
        String dataPath = "/watch/newElements";
        dataRequest =  PutDataRequest.create(dataPath);
        dataMapRequest = PutDataMapRequest.create(dataPath);
    }

    public void sendSyncMessage(List<SmokingEvent> unsynchronizedEvents) {
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
        String dataPath = "/watch/newSmokeEvents";
        new SendMessage(dataPath, messageData).start();
    }

    public void requestHashListMessage(){
        String dataPath = "/watch/newSensorData";
        byte[] dummy= {0};
        new SendMessage(dataPath, dummy).start();
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
                        Log.e(TAG_SYNC, "sending message failed; Execution Exception");
                    }
                    catch (InterruptedException exception) {
                        //TO DO//
                        Log.e(TAG_SYNC, "sending message failed; Interrupted Exception");
                        Log.d(TAG_SYNC, "Test error");
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
