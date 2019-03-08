package SensorReadoutModule;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import java.util.Random;


public class SensorReadoutService extends Service {

    /* Binder to connect application to service */
    public class SensorReadoutBinder extends Binder {
        public SensorReadoutService getService() {
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
    private HandlerThread handlerThread;

    @Override
    public void onCreate() {
        /* Start up the thread running the service using background priority. */
        handlerThread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();

        /* Get the HandlerThread's Looper and use it for our Handler */
        mServiceLooper = handlerThread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
        mServiceBinder = new SensorReadoutBinder();

        sensorServiceRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Toast.makeText(this, "Detection started", Toast.LENGTH_SHORT).show();

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
        super.onDestroy();
        /* Send message to user on destroy */
        if (sensorServiceRunning) {
            sensorServiceRunning = false;
            stopContinuousMeasurement();
            sensor.stopSensors();
            mServiceHandler = null;
            mServiceBinder = null;
            handlerThread.quit();
            handlerThread = null;
            sensor = null;
            //Toast.makeText(this, "Detection stopped", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isSensorServiceRunning (){
        return sensorServiceRunning;
    }

    public SensorReadoutStatus getSensorStatus() {
        return sensor.getSensorStatus();
    }

    public void triggerSingleMeasurement(MeasurementCompleteListener _measurementCompleteListener){
        sensor.triggerSingleMeasurement(_measurementCompleteListener);
    }

    public void stopSingleMeasurement() {
        sensor.stopSingleMeasurement();
    }

    public void triggerContinuousMeasurement() {
        sensor.triggerContinuousMeasurement();
    }

    public void stopContinuousMeasurement() {
        sensor.stopContinuousMeasurement();
    }

    public void setSmokingLabel (boolean isSmoking){
        sensor.setSmokingLabel(isSmoking);
    }

    public void getSingleSample(float[] sample) {
        sensor.getSample(sample);
    }

    public float[][] getFullSampleStorage() {
        return sensor.getSingleMeasurementDataStorage();
    }

    public double[][] getContinuousMeasurementDataStorage(){
        return sensor.getContinuousMeasurementDataStorage();
    }

    public boolean isContMeasDataAvailable(){
        return sensor.isContMeasDataAvailable();
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
