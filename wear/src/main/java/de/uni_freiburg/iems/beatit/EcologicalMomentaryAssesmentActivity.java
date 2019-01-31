package de.uni_freiburg.iems.beatit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.support.v7.app.AppCompatActivity;
import android.support.wear.ambient.AmbientModeSupport;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.ToggleButton;

import MachineLearningModule.SmokeDetector;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import SQLiteDatabaseModule.SmokingEvent;
import SensorReadoutModule.dataAcquisitionActivity;
import SensorReadoutModule.SensorReadoutService;

/* Callback interface for measurementCompleteEvent*/
interface ModelEvaluatedListener {
    void modelEvaluatedCB(boolean smoking);
}

public class EcologicalMomentaryAssesmentActivity extends AppCompatActivity implements View.OnClickListener, ModelEvaluatedListener, AmbientModeSupport.AmbientCallbackProvider {

    private ToggleButton playButton;
    private Intent sensorServiceIntent;
    private Boolean sensorServiceStarted = false;
    private Mediator sensorAiMediator = null;
    private LocalDateTime timeOfEvent;
    private AmbientModeSupport.AmbientController mAmbientController;

    /* TODO remove this as soon smoking notification exists*/
    private CheckBox smokingDetected;
    private TextView detectorText;
    private TextView timingText;
    private TextView stateText;
    private TextView framesText;

    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return new MyAmbientCallback();
    }

    private class MyAmbientCallback extends AmbientModeSupport.AmbientCallback {
        @Override
        public void onEnterAmbient(Bundle ambientDetails) {
            // Handle entering ambient mode
        }

        @Override
        public void onExitAmbient() {
            // Handle exiting ambient mode
        }

        @Override
        public void onUpdateAmbient() {
            // Update the content
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecological_momentary_assesment);

        // Enables Always-on
        mAmbientController = AmbientModeSupport.attach(this);

        Button daqButton = findViewById(R.id.startPageButtonLable);
        playButton = findViewById(R.id.startPageButtonPlay);
        daqButton.setOnClickListener(this);

        /* TODO remove this as soon smoking notification exists*/
        smokingDetected = findViewById(R.id.checkBox);
        detectorText = findViewById(R.id.detectorText);
        timingText = findViewById(R.id.timingText);
        stateText = findViewById(R.id.stateText);
        framesText = findViewById(R.id.framesText);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(sensorServiceStarted)
        {
            stopService(sensorServiceIntent);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

    }


    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, dataAcquisitionActivity.class);
        startActivity(intent);
    }

    /* TODO Put some more useful content here */
    public void onAddEventButtonClick(View v){
        if (sensorAiMediator == null) {
            /* sensorAiMediator not initialized */
            sensorAiMediator = new Mediator(this, false,EcologicalMomentaryAssesmentActivity.this);
        }
        else{
            /* sensorAiMediator initialized*/
        }
        timeOfEvent = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");
        String startDate = timeOfEvent.format(dateFormatter);
        String startTime = timeOfEvent.format(timeFormatter);
        String stopDate = timeOfEvent.format(dateFormatter);
        String stopTime = timeOfEvent.format(timeFormatter);

        SmokingEvent event = new SmokingEvent("manualEvent", startDate,
                startTime, stopDate, stopTime, true);
        sensorAiMediator.storeSmokingEvent(event);
    }

    public void onPlayButtonClick(View v){
        if(playButton.isChecked() == true) {
            sensorServiceIntent = new Intent(EcologicalMomentaryAssesmentActivity.this, SensorReadoutService.class);
            startService(sensorServiceIntent);
            sensorServiceStarted = true;

            if(sensorAiMediator != null)
            {
                sensorAiMediator = null;
                Log.i("ML", "previous sensorAiMediator instance deleted");
            }
            sensorAiMediator = new Mediator(this, true,EcologicalMomentaryAssesmentActivity.this);
        }
        else
        {
            stopService(sensorServiceIntent);
            /* tell connected modules to unbind */
            sensorAiMediator.unbindFromServices();

            sensorServiceStarted = false;
        }
    }

    public void onLogButtonClick(View v){
        Intent intent = new Intent(this, SmokingLogActivity.class);
        startActivity(intent);
    }

    @Override
    public void modelEvaluatedCB(boolean smoking) {
        /* The measurement is completed*/
        SmokeDetector sd = sensorAiMediator.getSmokeDetector();

        // debug stuff --->
        detectorText.setText(sd.getCurrentProbability() + " (" + sd.getCurrentStartStopFrames() + ")");
        timingText.setText(sd.getCurrentTiming() + " ms");
        framesText.setText("" + sd.getCurrentFrame());
        if (sd.isSmokingPhase() && !smokingDetected.isChecked()) {
            smokingDetected.setChecked(true);
        } else if(!sd.isSmokingPhase() && smokingDetected.isChecked()) {
            smokingDetected.setChecked(false);
        }
        String currentState = sd.getCurrentState();
        if(currentState == "Start") {
            currentState += " (" + sd.getGestureCounter() + ")";
        }
        if(stateText.getText() != currentState) {
            stateText.setText(currentState);
        } // <---

        if (smoking) { showSmokingDetectedPopUp(sd.getStartTime(), sd.getStopTime()); }
    }
    private void showSmokingDetectedPopUp(LocalDateTime start, LocalDateTime stop) {
        Log.i("ML", "started: " + start.toString() + "  stopped: " + stop.toString());
        Intent intent = new Intent(this, SmokeDetectedPopUpActivity.class);
        startActivity(intent);
    }
}
