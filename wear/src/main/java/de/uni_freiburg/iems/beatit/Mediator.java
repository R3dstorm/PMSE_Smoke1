package de.uni_freiburg.iems.beatit;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import java.util.List;

import MachineLearningModule.SmokeDetector;
import SQLiteDatabaseModule.SmokingEvent;
import SQLiteDatabaseModule.SmokingEventListAdapter;
import SQLiteDatabaseModule.SmokingEventViewModel;
import SensorReadoutModule.SensorReadoutService;
import SensorReadoutModule.SensorReadoutService.SensorReadoutBinder;

public class Mediator {

    final private int SLIDING_STEP_MS = 1000;
    final private int START_DELAY_MS = 100;

    enum State { IDLE, SYNC_SENT, SYNC_RECEIVED }

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
    private State syncState;
    int eventSyncLabelId = 0;                   /* Holds the id of the latest sync label*/
    boolean eventSyncLabelAvailable = false;    /* information on lastSyncLabelId being available */
    int latestEventId = 0;
    List<SmokingEvent> newSyncEvents = null;
    List<SmokingEvent> allEvents = null;

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
        syncState = State.IDLE;

        /* Access Database: Get a new or existing viewModel from viewModelProvider */
        final SmokingEventListAdapter adapter = new SmokingEventListAdapter(myContext);
        mSEViewModel = ViewModelProviders.of((FragmentActivity) myContext).get(SmokingEventViewModel.class);

        // Add an observer on the LiveData returned by getAlphabetizedWords.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.
        mSEViewModel.getAllEvents().observe((LifecycleOwner) myContext, new Observer<List<SmokingEvent>>() {
            @Override
            public void onChanged(@Nullable final List<SmokingEvent> events) {
                // Update the cached copy of the words in the adapter.
                adapter.setEvents(events);
                allEvents = events;
            }
        });

        mSEViewModel.getLatestSyncLabelId().observe((LifecycleOwner) myContext, new Observer<List<SmokingEvent>>() {
            @Override
            public void onChanged(@Nullable List<SmokingEvent> smokingEvents) {
                /* update */
                if (!smokingEvents.isEmpty()) {
                    eventSyncLabelId = smokingEvents.get(0).getId();
                    eventSyncLabelAvailable = true;
                }
                else{
                    /* there has been no SyncLabel set yet */
                    eventSyncLabelAvailable = false;
                }

                /* TODO call callback to get events and send to phone?*/
            }
        });

        mSEViewModel.getLatestEventId().observe((LifecycleOwner) myContext, new Observer<List<SmokingEvent>>() {
            @Override
            public void onChanged(@Nullable List<SmokingEvent> smokingEvents) {
                if (!smokingEvents.isEmpty()) {
                    latestEventId = smokingEvents.get(0).getId();
                }
            }
        });

        /* TODO syncLabel ID is fixed to 1 !!! needs to be set depending on Last Sync Label ID */
        /* TODO Solution: Move this to a function where the observer is constructed every time the function is called? */
        /* TODO is lastSyncLabel necessary? -> implement Query to get newSyncEvents without id*/
        mSEViewModel.getNewSyncEvents(1).observe((LifecycleOwner) myContext, new Observer<List<SmokingEvent>>() {
            @Override
            public void onChanged(@Nullable List<SmokingEvent> smokingEvents) {
                newSyncEvents = smokingEvents;
            }
        });

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
                /* TODO Connect to GUI?*/
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

    /* Synchronize object is created at every start of mediator;
        but only active within this function*/
    public boolean synchronizeEvents (){

        boolean syncDone = false;
        List<SmokingEvent> unsynchronizedEvents;
        /* TODO put content here... */
        /* Search data base for the sync label and send all later events to phone*/

        if (syncState.equals(State.IDLE)) {
            /* Find Sync label:*/
            if (eventSyncLabelAvailable) {
                /* only get not synchronized events */
                unsynchronizedEvents = newSyncEvents;
            } else {
                /* No Sync label has been set yet -> need to synchronize all elements */
                unsynchronizedEvents = allEvents;
            }

            /* Send data to phone */
            dBsyncHandler.sendSyncMessage(unsynchronizedEvents);
            syncState = State.SYNC_SENT;
        }
        else if (syncState.equals(State.SYNC_SENT)){

            /* TODO wait for message with return data received from phone  */
            /* ... set next state */

        }
        else if (syncState.equals(State.SYNC_RECEIVED)){
            /* Data received -> import new received elements to database */
            /* TODO ... */
            /* set synchronization label */
            mSEViewModel.setSyncLabel(latestEventId);
            syncState = State.IDLE;
            syncDone = true;
            /* TODO reset syncDone? */
        }



        return syncDone;
    }

    public boolean synchronizeEventsBackground(){
        Intent synchronizeServiceIntent = new Intent(myContext, SynchronizeService.class);
        synchronizeServiceIntent.putExtra("SYNCHRONIZE_DATA", true); /* TODO is this necessary? */
        SynchronizeService.enqueueWork(myContext, synchronizeServiceIntent);
        return true;
    }

    /* Old Stuff: */
    //dBsyncHandler.buildSendMessage();

//        byte[] blub = {1,2,3};
//        dBsyncHandler.sendDataToPhone(blub);

//    private void fetchData() {
//        StringBuilder sb = new StringBuilder();
//        List<User> youngUsers = mDb.userModel().loadAllUsers();
//        for (User youngUser : youngUsers) {
//            sb.append(String.format("%s, %s (%d)\n",
//                    youngUser.lastName, youngUser.name, youngUser.age));
//        }
//        mYoungUsersTextView.setText(sb);
//    }
}
