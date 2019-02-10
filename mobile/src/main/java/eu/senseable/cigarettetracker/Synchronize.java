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

    Synchronize (Context context){

        myContext = context;
        receivedMessageNumber = sentMessageNumber = 0;

        //Register to receive local broadcasts, which we'll be creating in the next step//
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        Receiver messageReceiver = new Receiver();
        LocalBroadcastManager.getInstance(myContext).registerReceiver(messageReceiver, messageFilter);

        /* Trigger Synchronize Service Intent */
//        Intent synchronizeServiceIntent = new Intent(context, SynchronizeService.class);
//        synchronizeServiceIntent.putExtra ("RECEIVED_EVENTS", "blub");
//        SynchronizeService.enqueueWork(context, synchronizeServiceIntent);

    }

    public void messageText(String newinfo) {
        if (newinfo.compareTo("") != 0) {
            //textview.append("\n" + newinfo);
        }
    }

    //Define a nested class that extends BroadcastReceiver//
    public class Receiver extends BroadcastReceiver {
        @Override

        public void onReceive(Context context, Intent intent) {

            /* TODO distinguish between receiving data and acknowledge from watch */
            //Upon receiving each message from the wearable, display the following text//
            byte[] receivedEvents = intent.getByteArrayExtra("message");

            /* Trigger Synchronize Service Intent */
            Intent synchronizeServiceIntent = new Intent(myContext, SynchronizeService.class);
            synchronizeServiceIntent.putExtra ("RECEIVED_EVENTS", receivedEvents);
            SynchronizeService.enqueueWork(myContext, synchronizeServiceIntent);

//            /*Create object from bytes:*/
//            ByteArrayInputStream bis = new ByteArrayInputStream(data);
//            ObjectInput in = null;
//            try {
//                in = new ObjectInputStream(bis);
//                unsynchronizedEvents = (List<SmokingEvent>) in.readObject();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            } finally {
//                try {
//                    if (in != null) {
//                        in.close();
//                    }
//                } catch (IOException ex) {
//                    // ignore close exception
//                }
//            }

            String message = "I just received a message from the wearable " + receivedMessageNumber++;;
            Log.d (TAG_SYNC, message);
            //textview.setText(message);

            /* TODO acknowledge received -> accept data -> synchronize */
            /* Wait for Acknowledge....  */

            /* Accept Data / Synchronize: */


        }
    }

    /* Create a send routine */
    public void buildSendMessage() {
        String message = "I just sent the handheld a message " + sentMessageNumber++;
        Log.d(TAG_SYNC, message);

        //Make sure you’re using the same path value//
        String dataPath = "/smokeSync";
        new SendMessage(dataPath, message.getBytes()).start();
    }

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
