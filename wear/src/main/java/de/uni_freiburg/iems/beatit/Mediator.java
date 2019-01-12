package de.uni_freiburg.iems.beatit;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.Timer;
import java.util.TimerTask;

import sensorReadoutModule.SensorReadoutService;
import sensorReadoutModule.SensorReadoutService.SensorReadoutBinder;

public class Mediator {

    private Timer timer;

    final private int SLIDING_STEP_MS = 1000;
    final private int START_DELAY_MS = 21000;

    private Intent sensorServiceIntent;
    private SensorReadoutService sensorService;
    private boolean sensorServiceBound = false;
    private boolean sensorServiceRunning = false;

    private ServiceConnection sensorServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to SensorReadoutService, cast the IBinder and get SensorReadoutService instance
            SensorReadoutBinder binder = (SensorReadoutBinder) service;
            sensorService = binder.getService();
            sensorServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            sensorServiceBound = false;
            sensorServiceRunning = false;
        }
    };

    Mediator (Context context){
        /* Start ModelCalcCycleTimerTask */
        startModelCalcCycleTimerTask(START_DELAY_MS, SLIDING_STEP_MS);
        /* Binding to Service*/
        if (!sensorServiceBound) {
            sensorServiceIntent = new Intent(context, SensorReadoutService.class);
            sensorServiceIntent.putExtra("BIND_SENSOR_SERVICE", true); /* TODO is this necessary? */
            context.bindService(sensorServiceIntent, sensorServiceConnection, Context.BIND_AUTO_CREATE);
        }

    }

    private void startModelCalcCycleTimerTask(int startDelay, int repeatDelay) {
        timer = new Timer();
        TimerTask modelCalcCycle = new TimerTask() {
            @Override
            public void run() {
                /* Collect Data from Sensor */
                if (sensorServiceBound){
                    sensorServiceRunning = sensorService.isSensorServiceRunning();
                }
                //sensorService.

            }
        };
        timer.scheduleAtFixedRate(modelCalcCycle, startDelay, repeatDelay);

    }
}
