package de.uni_freiburg.iems.beatit;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
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

import MachineLearningModule.ModelHandler;
import sensorReadoutModule.SensorReadoutService;
import sensorReadoutModule.SensorReadoutService.SensorReadoutBinder;

public class Mediator {

    final private int SLIDING_STEP_MS = 1000;
    final private int START_DELAY_MS = 100;

    private Intent sensorServiceIntent;
    private SensorReadoutService sensorService;
    private boolean sensorServiceBound = false;
    private boolean sensorServiceRunning = false;
    private ModelHandler m;
    private ModelEvaluatedListener modelEvaluatedListener;
    private Context myContext;
    private Handler mainHandler;
    private SmokingEventViewModel mSEViewModel;

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

    Mediator(Context context, ModelEvaluatedListener _modelEvaluatedListener) {
        myContext = context;
        Activity myActivity = (Activity)context;
        mainHandler = new Handler(myContext.getMainLooper());
        /* Access Database: Get a new or existing viewModel from viewModelProvider */
        mSEViewModel = ViewModelProviders.of((FragmentActivity) myContext).get(SmokingEventViewModel.class); /* TODO geht daS?*/

        // Add an observer on the LiveData returned by getAlphabetizedWords.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.
        /* TODO ....*/
//        mWordViewModel.getAllWords().observe(this, new Observer<List<Word>>() {
//            @Override
//            public void onChanged(@Nullable final List<Word> words) {
//                // Update the cached copy of the words in the adapter.
//                adapter.setWords(words);
//            }
//        });

        /* Start ModelCalcCycleTimerTask */
        startModelCalcCycleTimerTask(START_DELAY_MS, SLIDING_STEP_MS);
        /* Binding to Service*/
        if (!sensorServiceBound) {
            sensorServiceIntent = new Intent(context, SensorReadoutService.class);
            sensorServiceIntent.putExtra("BIND_SENSOR_SERVICE", true); /* TODO is this necessary? */
            context.bindService(sensorServiceIntent, sensorServiceConnection, Context.BIND_AUTO_CREATE);
        }
        modelEvaluatedListener = _modelEvaluatedListener;
        m = new ModelHandler();
        m.loadModel(context.getAssets());

    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            /* Collect Data from Sensor */
            if (sensorServiceBound) {
                sensorServiceRunning = sensorService.isSensorServiceRunning();
            }
            if (sensorServiceRunning) {
                /* Start continuous measurement */
                sensorService.triggerContinuousMeasurement();

                /* Try to read out and process data*/
                if (sensorService.isContMeasDataAvailable()) {
                    /* Hand continuous data to ML-Module*/
                    boolean smokingLabel = m.predict(sensorService.getContinuousMeasurementDataStorage());
                    modelEvaluatedListener.modelEvaluatedCB(smokingLabel);
                }
                /* TODO Connect to GUI?*/
            }
            mainHandler.postDelayed(runnable, SLIDING_STEP_MS);
        }
    };


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
        LiveData<List<SmokingEvent>> blub = mSEViewModel.getAllEvents();
        List dub = blub.getValue();
        if (dub != null) {
            if (dub.size() > 0) {
                String cub = ((SmokingEvent) dub.get(0)).getStartDate();
                /* TODO zugriff nur über Observer möglich?*/
            }
        }
    }

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
