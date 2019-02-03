package de.uni_freiburg.iems.beatit;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class Synchronize {

    int receivedMessageNumber = 1;
    int sentMessageNumber = 1;
    Context myContext;
    private static final String TAG_SYNC = "SYNCHRONIZE";

    Synchronize(Context context) {
        myContext = context;

        /* Register the local broadcast receiver */
        IntentFilter newFilter = new IntentFilter(Intent.ACTION_SEND);
        Receiver messageReceiver = new Receiver();
        LocalBroadcastManager.getInstance(myContext).registerReceiver(messageReceiver, newFilter);
    }

    /* Create an send routine */
    public void buildSendMessage() {
        String message = "I just sent the handheld a message " + sentMessageNumber++;
        Log.d(TAG_SYNC, message);

        //Make sure you’re using the same path value//
        String dataPath = "/smokeSync";
        new SendMessage(dataPath, message).start();
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
        String message;

        /* Constructor */
        SendMessage(String p, String m) {
            path = p;
            message = m;
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
                            Wearable.getMessageClient(myContext).sendMessage(node.getId(), path, message.getBytes());
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

    /* TODO create a method that actually performs the synchronisation of the events */
}
