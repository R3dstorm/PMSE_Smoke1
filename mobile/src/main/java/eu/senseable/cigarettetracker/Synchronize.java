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

import java.util.List;
import java.util.concurrent.ExecutionException;

public class Synchronize {
    private Handler myHandler;
    private Context myContext;
    private int receivedMessageNumber;
    private int sentMessageNumber;
    private static final String TAG_SYNC = "SYNCHRONIZE";

    Synchronize (Context context){

        myContext = context;
        receivedMessageNumber = sentMessageNumber = 0;

        //Create a message handler//
        myHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Bundle stuff = msg.getData();
                messageText(stuff.getString("messageText"));
                return true;
            }
        });

        //Register to receive local broadcasts, which we'll be creating in the next step//
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        Receiver messageReceiver = new Receiver();
        LocalBroadcastManager.getInstance(myContext).registerReceiver(messageReceiver, messageFilter);

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

            //Upon receiving each message from the wearable, display the following text//
            byte[] data = intent.getByteArrayExtra("message");

            String message = "I just received a message from the wearable " + receivedMessageNumber++;;
            Log.d (TAG_SYNC, message);
            //textview.setText(message);

        }
    }

    private void buildSendMessage() {
        String message = "Sending message.... ";
        Log.d (TAG_SYNC, message);
        //textview.setText(message);

        /* Sending a message can block the main UI thread, so use a new thread */
        new NewThread("/smokeSync", message).start();

    }

    /* Use a Bundle to encapsulate our message */
    public void sendMessage(String messageText) {
        Bundle bundle = new Bundle();
        bundle.putString("messageText", messageText);
        Message msg = myHandler.obtainMessage();
        msg.setData(bundle);
        myHandler.sendMessage(msg);
    }

    class NewThread extends Thread {
        String path;
        String message;

        /* Constructor for sending information to the Data Layer */
        NewThread(String p, String m) {
            path = p;
            message = m;
        }
        public void run() {
            /* Retrieve the connected devices, known as nodes */
            Task<List<Node>> wearableList =
                    Wearable.getNodeClient(myContext).getConnectedNodes();
            try {
                List<Node> nodes = Tasks.await(wearableList);
                for (Node node : nodes) {
                    /* Send the message */
                    Task<Integer> sendMessageTask =
                            Wearable.getMessageClient(myContext).sendMessage(node.getId(), path, message.getBytes());
                    try {
                        /* Block on a task and get the result synchronously */
                        Integer result = Tasks.await(sendMessageTask);
                        sendMessage("I just sent the wearable a message " + sentMessageNumber++);
                        //if the Task fails, then…..//
                    }
                    catch (ExecutionException exception) {
                        //TO DO: Handle the exception//
                    }
                    catch (InterruptedException exception) {
                        //TO DO: Handle the exception//
                    }
                }
            }
            catch (ExecutionException exception) {
                //TO DO: Handle the exception//
            }
            catch (InterruptedException exception) {
                //TO DO: Handle the exception//
            }
        }
    }
}
