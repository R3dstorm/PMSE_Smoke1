package de.uni_freiburg.iems.beatit;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;

import SQLiteDatabaseModule.SmokingEvent;

public class Synchronize {

    int receivedMessageNumber = 1;
    int sentMessageNumber = 1;
    private Context myContext;
    private PutDataRequest dataRequest;
    private PutDataMapRequest dataMapRequest;
    private DataClient dataClient;
    private static final String SYNC_KEY = "de.uni_freiburg.iems.beatit";

    private static final String TAG_SYNC = "SYNCHRONIZE";


    Synchronize(Context context) {
        myContext = context;

        /* Register the local broadcast receiver */
        IntentFilter newFilter = new IntentFilter(Intent.ACTION_SEND);
        Receiver messageReceiver = new Receiver();
        LocalBroadcastManager.getInstance(myContext).registerReceiver(messageReceiver, newFilter);

        /* Handle Data Objects */
        String dataPath = "/watch/newElements";
        dataRequest =  PutDataRequest.create(dataPath);
        dataMapRequest = PutDataMapRequest.create(dataPath);


    }

    public void sendDataToPhone(byte[] data) {
        dataRequest.setData(data);
        DataMap dataMap = dataMapRequest.getDataMap();
        /* TODO DRY! -> use SYNC_KEY instead of path */
        /* TODO Need for "setUrgent()"? */
        String dataPath = "/watch/newElements";
        dataMap.putByteArray(SYNC_KEY, data);
        dataMapRequest.setUrgent();
        PutDataRequest putDataReq = dataMapRequest.asPutDataRequest();
        Task<DataItem> putDataTask = Wearable.getDataClient(myContext).putDataItem(putDataReq);//dataClient.putDataItem(putDataReq);

        /* TODO wait for Task objecT?*/
//        try {
//            DataItem item = Tasks.await(dataItemTask);
//            Log.d(TAG, "Data item set: " + item.getUri());
//        } catch (ExecutionException | InterruptedException e) {
//  ...
//        }


    }

    /* Create a send routine */
    public void buildSendMessage() {
        String message = "I just sent the handheld a message " + sentMessageNumber++;
        Log.d(TAG_SYNC, message);

        //Make sure you’re using the same path value//
        String dataPath = "/smokeSync";
        new SendMessage(dataPath, message.getBytes()).start();
    }

    /**/
    public void sendSyncMessage(List<SmokingEvent> unsynchronizedEvents) {
        byte[] messageData = null;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(unsynchronizedEvents);
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

        /*Create object from bytes:*/
//        ByteArrayInputStream bis = new ByteArrayInputStream(yourBytes);
//        ObjectInput in = null;
//        try {
//            in = new ObjectInputStream(bis);
//            Object o = in.readObject();
//        } finally {
//            try {
//                if (in != null) {
//                    in.close();
//                }
//            } catch (IOException ex) {
//                // ignore close exception
//            }
//        }

    }

    public class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String onMessageReceived = "I just received a  message from the handheld " + receivedMessageNumber++;
            //textView.setText(onMessageReceived);
            Log.d(TAG_SYNC, onMessageReceived);
        }
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
