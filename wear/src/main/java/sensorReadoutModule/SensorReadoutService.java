package sensorReadoutModule;

import sensorReadoutModule.SensorReadout;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.app.JobIntentService;
import android.support.v4.content.LocalBroadcastManager;

public class SensorReadoutService extends JobIntentService {

    private SensorReadout sensor;

//    public SensorReadoutService() {
//
//        super("SensorReadoutService");
//    }

   // @Override
//    protected void onHandleIntent(Intent workIntent){
//        // Gets data from the incoming Intent
//        String dataString = workIntent.getDataString();
//
//        // Do work here, based on the contents of dataString
//        int error;
//        sensor = new SensorReadout(this);
//        error = sensor.initSensors();
//    }

    @Override
    protected void onHandleWork(Intent workIntent){
        int status = 0;
        // Muss hier entschieden werden, ob Service gestartet wird etc...?
        // -> Init der Sensoren?
        // -> Starten der Messung... etc.



        // Messung erledigt.. Super gemacht... Ergebnis rauhauen:
        /*
         * Creates a new Intent containing a Uri object
         * BROADCAST_ACTION is a custom Intent action
         */
        int value = workIntent.getIntExtra("triggerMeasurement",2);
        int value2 = workIntent.getIntExtra("dubber", 10);
        Intent localIntent = new Intent("BROADCAST_ACTION")
                        // Puts the status into the Intent
                        .putExtra("habeFertig", status);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        //while(true);

    }


}
