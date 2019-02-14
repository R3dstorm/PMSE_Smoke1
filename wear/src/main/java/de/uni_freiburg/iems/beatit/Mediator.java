package de.uni_freiburg.iems.beatit;

import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;

import MachineLearningModule.SmokeDetector;
import SQLiteDatabaseModule.SmokingEvent;
import SQLiteDatabaseModule.SmokingEventViewModel;
import SensorReadoutModule.SensorReadoutService;
import SensorReadoutModule.SensorReadoutService.SensorReadoutBinder;

public class Mediator {

    final private int SLIDING_STEP_MS = 1000;
    final private int START_DELAY_MS = 100;

    private Intent sensorServiceIntent;
    private SensorReadoutService sensorService;
    private boolean sensorServiceBound = false;
    private boolean sensorServiceRunning = false;
    private SmokeDetector smokeDetector = null;
    private ModelEvaluatedListener modelEvaluatedListener;
    private Context myContext;
    private Handler mainHandler;
    private SmokingEventViewModel mSEViewModel;

    private Synchronize dBsyncHandler;          /* Sync handler for data base */

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

    Mediator(Context context, boolean bindSensorService, ModelEvaluatedListener _modelEvaluatedListener) {
        myContext = context;
        mainHandler = new Handler(myContext.getMainLooper());
        dBsyncHandler = new Synchronize(myContext);

        /* Access Database: Get a new or existing viewModel from viewModelProvider */
        mSEViewModel = ViewModelProviders.of((FragmentActivity) myContext).get(SmokingEventViewModel.class);



        if (bindSensorService) {
            /* Start ModelCalcCycleTimerTask */
            startModelCalcCycleTimerTask(START_DELAY_MS, SLIDING_STEP_MS);
            /* Binding to Service*/
            if (!sensorServiceBound) {
                sensorServiceIntent = new Intent(context, SensorReadoutService.class);
                sensorServiceIntent.putExtra("BIND_SENSOR_SERVICE", true); /* TODO is this necessary? */
                context.bindService(sensorServiceIntent, sensorServiceConnection, Context.BIND_AUTO_CREATE);
            }
            modelEvaluatedListener = _modelEvaluatedListener;
			smokeDetector = new SmokeDetector(context.getAssets());
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            /* Collect Data from Sensor */
            sensorServiceRunning = sensorServiceBound && sensorService.isSensorServiceRunning();

            if (sensorServiceRunning) {
                /* Start continuous measurement */
                sensorService.triggerContinuousMeasurement();

                /* Try to read out and process data*/
                if (sensorService.isContMeasDataAvailable()) {
                    /* Hand continuous data to ML-Module*/
                    smokeDetector.feedSensorData(sensorService.getContinuousMeasurementDataStorage());
                    modelEvaluatedListener.modelEvaluatedCB(smokeDetector.isSmokingDetected());
                }
            }
            mainHandler.postDelayed(runnable, SLIDING_STEP_MS);
        }
    };

    public SmokeDetector getSmokeDetector() {
        return smokeDetector;
    }

    private void startModelCalcCycleTimerTask(int startDelay, int repeatDelay) {
        mainHandler.postDelayed(runnable, repeatDelay);
    }

    public void unbindFromServices (){
        if (sensorServiceBound) {
            myContext.unbindService(sensorServiceConnection);
            sensorServiceBound = false;
        }
    }

    public void storeSmokingEvent (SmokingEvent smokingEvent) {
        mSEViewModel.insert(smokingEvent);
    }

    public boolean synchronizeEventsBackground(){
        Intent synchronizeServiceIntent = new Intent(myContext, SynchronizeService.class);
        synchronizeServiceIntent.putExtra("SEND_NEW_EVENTS", true);
        synchronizeServiceIntent.putExtra("NEW_EVENTS_RECEIVED", false);
        SynchronizeService.enqueueWork(myContext, synchronizeServiceIntent);
        return true;
    }
}
