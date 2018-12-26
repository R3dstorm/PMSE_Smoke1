package sensorReadoutModule;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.JobIntentService;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Random;

import sensorReadoutModule.SensorReadout;


public class SensorReadoutService extends Service {

    public class SensorReadoutBinder extends Binder {
        SensorReadoutService getService() {
            /* Return this instance of local SensorReadoutService so clients can call public methods */
            return SensorReadoutService.this;
        }
    }

     /* Random number generator FOR TESTING!"!!!!! */
    public int getRandomNumber() {
        final Random mGenerator = new Random();
        return mGenerator.nextInt(100);
    }
    // TESTING!!!!!!!


    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler; /* TODO deprecated... delte this...*/
    private IBinder mServiceBinder;
    private static final String TAG = "SensorReadoutService";
    private SensorReadout sensor;   /* Instance for operating the sensors (Acc, Gyr) */


    /* Handler that receives messages from the thread */
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            /* Initialize and start sensors */
            sensor = new SensorReadout(SensorReadoutService.this);
            sensor.initSensors();
        }
    }

    @Override
    public void onCreate() {
        /* Start up the thread running the service using background priority. */
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        /* Get the HandlerThread's Looper and use it for our Handler */
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
        mServiceBinder = new SensorReadoutBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        /* For each start request, send a message to start a job and deliver the start ID */
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        /* If we get killed, after returning from here, restart */
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        /* Return our IBinder object */
        return mServiceBinder;
    }

    @Override
    public void onDestroy() {
        /* Send message to user on destroy */
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }
    

//        /* TODO implement CONSTANTS-Class for item names: https://developer.android.com/training/run-background-service/report-status */
//        if (workIntent.getBooleanExtra("START_SENSOR_SERVICE",false)){
//            serviceMethod = ServiceMethod.START_SENSOR_SERVICE;
//        }
//        if (workIntent.getBooleanExtra("STOP_SENSOR_SERVICE", false)){
//            serviceMethod = ServiceMethod.STOP_SENSOR_SERVICE;
//        }
//        if (workIntent.getBooleanExtra("GET_SINGLE_SAMPLE", false)){
//            serviceMethod = ServiceMethod.GET_SINGLE_SAMPLE;
//        }
//        if (workIntent.getBooleanExtra("GET_SAMPLE_STORAGE", false)){
//            serviceMethod = ServiceMethod.GET_SAMPLE_STORAGE;
//        }
//
//        switch (serviceMethod){
//            case START_SENSOR_SERVICE:
//                sensor = new SensorReadout(this);
//
//                /* TODO if (isStopped()) return;*/
//                sensor.initSensors();
//
//                /* TODO Report Sensors started? */
//                break;
//
//            case STOP_SENSOR_SERVICE:
//                sensor.stopSensors();
//                /* TODO Report Sensors stopped? */
//                break;
//
//            case GET_SINGLE_SAMPLE:
//                if (sensor != null) {
//                    sensor.getSample(sample);
//                    /* Send to Broadcast containing Data */
//                    localIntent = new Intent("BROADCAST_ACTION")
//                            .putExtra("SAMPLE", sample);
//                    LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
//                }
//                else{
//                    Log.e(TAG, "Houston, we have a problem");
//                }
//
//                break;
//
//            default:
//                break;
//
//        }
//
//    }

}
