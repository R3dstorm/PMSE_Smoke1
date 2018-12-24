package sensorReadoutModule;

import sensorReadoutModule.SensorReadout;

import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.app.JobIntentService;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class SensorReadoutService extends JobIntentService {

    private static final String TAG = "SensorReadoutService";
    private SensorReadout sensor;   /* Instance for operating the sensors (Acc, Gyr)*/


    enum ServiceMethod {
        NONE,
        START_SENSOR_SERVICE,
        STOP_SENSOR_SERVICE,
        GET_SINGLE_SAMPLE,
        GET_SAMPLE_STORAGE
    }


    @Override
    protected void onHandleWork(Intent workIntent){
        int status = 0;
        float[] sample = new float[10];
        ServiceMethod serviceMethod = ServiceMethod.NONE;
        Intent localIntent;

        // Muss hier entschieden werden, ob Service gestartet wird etc...?
        // -> Init der Sensoren?
        // -> Starten der Messung... etc.
        /* Supported Methods in Service: */
        /* TODO implement CONSTANTS-Class for item names: https://developer.android.com/training/run-background-service/report-status */
        if (workIntent.getBooleanExtra("START_SENSOR_SERVICE",false)){
            serviceMethod = ServiceMethod.START_SENSOR_SERVICE;
        }
        if (workIntent.getBooleanExtra("STOP_SENSOR_SERVICE", false)){
            serviceMethod = ServiceMethod.STOP_SENSOR_SERVICE;
        }
        if (workIntent.getBooleanExtra("GET_SINGLE_SAMPLE", false)){
            serviceMethod = ServiceMethod.GET_SINGLE_SAMPLE;
        }
        if (workIntent.getBooleanExtra("GET_SAMPLE_STORAGE", false)){
            serviceMethod = ServiceMethod.GET_SAMPLE_STORAGE;
        }

        switch (serviceMethod){
            case START_SENSOR_SERVICE:
                sensor = new SensorReadout(this);

                /* TODO if (isStopped()) return;*/
                sensor.initSensors();

                /* TODO Report Sensors started? */
                break;

            case STOP_SENSOR_SERVICE:
                sensor.stopSensors();
                /* TODO Report Sensors stopped? */
                break;

            case GET_SINGLE_SAMPLE:
                if (sensor != null) {
                    sensor.getSample(sample);
                    /* Send to Broadcast containing Data */
                    localIntent = new Intent("BROADCAST_ACTION")
                            .putExtra("SAMPLE", sample);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
                }
                else{
                    Log.e(TAG, "Houston, we have a problem");
                }

                break;

            default:
                break;

        }

    }

    @Override
    public boolean onStopCurrentWork() {
        Log.d(TAG, "onStopCurrentWork");
        return super.onStopCurrentWork();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }
}
