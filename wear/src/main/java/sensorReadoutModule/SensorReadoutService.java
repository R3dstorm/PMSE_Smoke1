package sensorReadoutModule;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.widget.Toast;

import java.util.Random;


public class SensorReadoutService extends Service {

    /* Binder to connect application to service */
    public class SensorReadoutBinder extends Binder {
        SensorReadoutService getService() {
            /* Return this instance of local SensorReadoutService so clients can call public methods */
            return SensorReadoutService.this;
        }
    }

    /* Handler that receives messages from the thread */
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {

        }
    }

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private IBinder mServiceBinder;
    private SensorReadout sensor;   /* Instance for operating the sensors (Acc, Gyr) */
    private static final String TAG = "SensorReadoutService";
    private boolean sensorServiceRunning;

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

        sensorServiceRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        /* For each start request, send a message to start a job and deliver the start ID */
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        /* Initialize and start sensors */
        sensor = new SensorReadout(this, mServiceHandler);
        sensorServiceRunning = true;

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
        if (sensorServiceRunning) {
            sensor.stopSensors();
            Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isSensorServiceRunning (){
        return sensorServiceRunning;
    }

    public SensorReadoutStatus getSensorStatus() {
        return sensor.getSensorStatus();
    }

    public void startMeasurement (MeasurementCompleteListener _measurementCompleteListener){
        sensor.triggerMeasurement(_measurementCompleteListener);
    }

    public void stopMeasurement () {
        sensor.stopMeasurement();
    }

    public void setSmokingLabel (boolean isSmoking){
        sensor.setSmokingLabel(isSmoking);
    }

    public void getSingleSample(float[] sample) {
        sensor.getSample(sample);
    }

    public float[][] getFullSampleStorage() {
        return sensor.getDataStorage();
    }

    public int getNumberOfSamples() {
        return sensor.getNumberOfSamples();
    }

    /* Random number generator for testing */
    public int getRandomNumber() {
        final Random mGenerator = new Random();
        return mGenerator.nextInt(100);
    }
}
