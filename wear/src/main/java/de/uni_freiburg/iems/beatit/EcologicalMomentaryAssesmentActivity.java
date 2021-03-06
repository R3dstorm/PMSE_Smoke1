package de.uni_freiburg.iems.beatit;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.support.wear.ambient.AmbientModeSupport;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.UUID;

import MachineLearningModule.SmokeDetector;
import SQLiteDatabaseModule.SmokingEvent;
import SensorReadoutModule.SensorReadoutService;
import SensorReadoutModule.dataAcquisitionActivity;

import static android.content.Intent.ACTION_DREAMING_STARTED;
import static android.content.Intent.ACTION_DREAMING_STOPPED;

/* Callback interface for measurementCompleteEvent*/
interface ModelEvaluatedListener {
    void modelEvaluatedCB(boolean smoking);
}

public class EcologicalMomentaryAssesmentActivity extends AppCompatActivity implements View.OnClickListener, ModelEvaluatedListener, AmbientModeSupport.AmbientCallbackProvider {

    private ToggleButton playButton;
    private Intent sensorServiceIntent;
    private Boolean sensorServiceStarted = false;
    private Mediator sensorAiMediator = null;
    private AmbientModeSupport.AmbientController mAmbientController;
    private LocalDateTime smokingStartTime;
    private LocalDateTime smokingEndTime;
    private boolean isDetectionStarted = false;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock = null;
    private BroadcastReceiver powerSaveReceiver = null;
    private IntentFilter actionFilter = null;
    private Boolean bNoDetection = false;
    private boolean isPopupMode = false;

    /* TODO remove this as soon smoking notification exists*/
    private CheckBox smokingDetected;
    private TextView detectorText;
    private TextView timingText;
    private TextView stateText;
    private TextView framesText;

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyMMdd");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");

    private int requestCodePopUp = 11;
    private int requestCodeDAQ = 12;
    private int requestCodeLog = 13;
    private int requestCodeManual = 15;
    private int requestCodePause = 16;

    private CountDownTimer PauseTime = null;

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

        detectorText = findViewById(R.id.detectorText);
        timingText = findViewById(R.id.timingText);
        stateText = findViewById(R.id.stateText);
        framesText = findViewById(R.id.framesText);
        if(Globals.getInstance().isDebugMode()) {
            detectorText.setVisibility(View.VISIBLE);
            timingText.setVisibility(View.VISIBLE);
            stateText.setVisibility(View.VISIBLE);
            framesText.setVisibility(View.VISIBLE);
        }

        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MyApp::MyWakelockTag");
        wakeLock.acquire();

        powerSaveReceiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(action == ACTION_DREAMING_STARTED) {
                    WindowManager.LayoutParams params = getWindow().getAttributes();
                    params.screenBrightness = 0.01f;
                    getWindow().setAttributes(params);
                } else if(action == ACTION_DREAMING_STOPPED) {
                    WindowManager.LayoutParams params = getWindow().getAttributes();
                    params.screenBrightness = -1;
                    getWindow().setAttributes(params);
                }
            }
        };

        IntentFilter actionFilter = new IntentFilter();
        actionFilter.addAction(ACTION_DREAMING_STARTED);
        actionFilter.addAction(ACTION_DREAMING_STOPPED);
        registerReceiver(powerSaveReceiver, actionFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isDetectionStarted == false && PauseTime == null) {
            isDetectionStarted = true;
            playButton.setChecked(true);
            toggleDetection();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!isPopupMode && isDetectionStarted) {
            isDetectionStarted = false;
            toggleDetection();
        }
        if(!isPopupMode) {
            if (wakeLock != null) {
                if (wakeLock.isHeld()) {
                    wakeLock.release();
                }
                wakeLock = null;
            }
            if (actionFilter != null) {
                actionFilter = null;
            }
            if (powerSaveReceiver != null) {
                unregisterReceiver(powerSaveReceiver);
                powerSaveReceiver = null;
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, dataAcquisitionActivity.class);
        bNoDetection = true;
        isPopupMode = true;
        startActivityForResult(intent, requestCodeDAQ);
    }

    public void onAddEventButtonClick(View v){
        Intent intent = new Intent(this, ManualSmokeEvent.class);
        Bundle b = new Bundle();
        b.putString("Task", "Manual");
        intent.putExtras(b);
        isPopupMode = true;
        startActivityForResult(intent, requestCodeManual);
    }

    public void onSyncButtonClick(View v){
        /* Stop detection */
        if (isDetectionStarted) {
            isDetectionStarted = !isDetectionStarted;
            playButton.setChecked(false);
            toggleDetection();
        }

        if (sensorAiMediator == null) {
            /* sensorAiMediator not initialized */
            sensorAiMediator = new Mediator(this, false,EcologicalMomentaryAssesmentActivity.this);
        }
        else{
            /* sensorAiMediator initialized*/
        }
        sensorAiMediator.synchronizeEventsBackground();
    }

    public void onPlayButtonClick(View v) {
        if (!isDetectionStarted) {
            isDetectionStarted = true;
            toggleDetection();
            if (PauseTime != null){
                PauseTime.cancel();
                PauseTime = null;
            }
        }
        else {
            playButton.setChecked(true);

            Intent intent = new Intent(this, ManualSmokeEvent.class);
            isPopupMode = true;
            Bundle b = new Bundle();
            b.putString("Task", "Pause");
            intent.putExtras(b);
            startActivityForResult(intent, requestCodePause);
        }
    }

    private void toggleDetection() {
        if(isDetectionStarted) {
            sensorServiceIntent = new Intent(EcologicalMomentaryAssesmentActivity.this, SensorReadoutService.class);
            startService(sensorServiceIntent);
            sensorServiceStarted = true;

            if (sensorAiMediator != null) {
                sensorAiMediator = null;
            }
            sensorAiMediator = new Mediator(this, true, EcologicalMomentaryAssesmentActivity.this);

            if (PauseTime == null){
                Toast.makeText(this, "Detection started", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "Detection resumed", Toast.LENGTH_SHORT).show();
            }
        } else {
            stopService(sensorServiceIntent);
            sensorServiceIntent = null;
            /* tell connected modules to unbind */
            sensorAiMediator.unbindFromServices();
            sensorServiceStarted = false;
            sensorAiMediator = null;
            if (PauseTime == null){
                Toast.makeText(this, "Detection stopped", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "Detection paused", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onLogButtonClick(View v){
        isPopupMode = true;
        Intent intent = new Intent(this, SmokingLogActivity.class);
        startActivityForResult(intent, requestCodeLog);
    }

    @Override
    public void modelEvaluatedCB(boolean smoking) {
        /* The measurement is completed*/
        if(sensorAiMediator != null) {
            SmokeDetector sd = sensorAiMediator.getSmokeDetector();

            if(Globals.getInstance().isDebugMode()) {
                detectorText.setText(sd.getCurrentProbability() + " (" + sd.getCurrentStartStopFrames() + ")");
                timingText.setText(sd.getCurrentTiming() + " ms");
                framesText.setText("" + sd.getCurrentFrame());
                String currentState = sd.getCurrentState();
                if (currentState == "Start") {
                    currentState += " (" + sd.getGestureCounter() + ")";
                }
                if (stateText.getText() != currentState) {
                    stateText.setText(currentState);
                }
            }

            if (smoking && !bNoDetection) {
                if(sd.getStartTime() == null || sd.getStopTime() == null) {
                    Log.i("ML", "no valid start and/or stop time available");
                } else {
                    showSmokingDetectedPopUp(sd.getStartTime(), sd.getStopTime());
                }
            }
        }
    }

    private void showSmokingDetectedPopUp(LocalDateTime startTime, LocalDateTime stopTime) {
        smokingStartTime = startTime;
        smokingEndTime = stopTime;
        isPopupMode = true;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd G 'at' HH:mm:ss z");
        Intent intent = new Intent(this, SmokeDetectedPopUpActivity.class);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setAction("de.uni_freiburg.iems.beatit");
        intent.putExtra("StartTime", startTime.atZone(ZoneId.of("UTC")).format(formatter));
        intent.putExtra("StopTime", stopTime.atZone(ZoneId.of("UTC")).format(formatter));
        intent.putExtra("SenderInfo", "Team1_Smoking_Event");
        sendOrderedBroadcast(intent,null, new NotificationResultReceiver(),null, Activity.RESULT_OK,null,null);
        startActivityForResult(intent, requestCodePopUp);
    }

    @Override
    protected void onActivityResult(int requestedCode, int resultCode, Intent intent) {

        isPopupMode = false;

        if(requestedCode == requestCodePopUp) {
            if(resultCode == 0) {
                // user timeout
                setSmokingDetectionNoUserAction();
                // 30 min Timeout starten
                this.SetPauseFunction(1800000);
            } else if (resultCode == 1) {
                // accepted event
                setSmokingIsDetectedCorrectly();
                // Nach Timeout fragen
                Intent intentPause = new Intent(this, ManualSmokeEvent.class);
                isPopupMode = true;
                Bundle b = new Bundle();
                b.putString("Task", "Pause");
                intentPause.putExtras(b);
                startActivityForResult(intentPause, requestCodePause);
            } else if (resultCode == 2) {
                // declined event
                Toast.makeText(this, "Event declined", Toast.LENGTH_SHORT).show();
                // no action needed. Event is not saved
            }
        }

        else if (requestedCode == requestCodeManual) {
            if (resultCode != 0) {

                Bundle b = intent.getExtras();
                String value ;
                if(b != null) {
                    value = b.getString("Time");
                    LocalDateTime ldt = LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
                    CreateNewManualSmokeEvent(ldt);
                }
            }
        }
        else if (requestedCode == requestCodeDAQ){
            bNoDetection = false;
        }
        else if (requestedCode == requestCodePause) {

            if (resultCode != 0) {
                Bundle b = intent.getExtras();
                String value;
                if (b != null) {
                    value = b.getString("Time");
                    long time = new Long(value);
                    time = time * 60000;

                    this.SetPauseFunction(time);
                }
            }
            else
            {
                PauseTime = null;
            }
        }
    }

    protected void SetPauseFunction(long Timeout){

        if (Timeout != 0) {

            // Timer mit angebebener Zeit fuer Wiederbeginn starten
            PauseTime = new CountDownTimer(Timeout, 1000) {

                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {

                    if (isPopupMode == false) {
                        Intent openMainActivity = new Intent(EcologicalMomentaryAssesmentActivity.this, EcologicalMomentaryAssesmentActivity.class);
                        openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivityIfNeeded(openMainActivity, 0);
                    }

                    isDetectionStarted = true;
                    playButton.setChecked(true);
                    toggleDetection();

                    PauseTime = null;
                }
            }.start();

            // Detektion beenden
            if (isDetectionStarted) {
                isDetectionStarted = false;
                toggleDetection();
                playButton.setChecked(false);
                startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME));
            }
        }
    }

    protected void  CreateNewManualSmokeEvent(LocalDateTime StartTime)
    {
        String startDate = StartTime.format(dateFormatter);
        String startTime = StartTime.format(timeFormatter);

        StartTime = StartTime.plus(5, (TemporalUnit) ChronoUnit.MINUTES);

        String stopDate = StartTime.format(dateFormatter);
        String stopTime = StartTime.format(timeFormatter);

        SmokingEvent event = new SmokingEvent("Smoking", startDate,
                startTime, stopDate, stopTime, true,
                false, false, UUID.randomUUID().toString());

        if (sensorAiMediator == null) {
            // sensorAiMediator not initialized
            sensorAiMediator = new Mediator(this, false,EcologicalMomentaryAssesmentActivity.this);
        }
        else{
            //sensorAiMediator initialized
        }

        sensorAiMediator.storeSmokingEvent(event);
    }

    public void setSmokingIsDetectedCorrectly()
    {
        String startDate = smokingStartTime.format(dateFormatter);
        String startTime = smokingStartTime.format(timeFormatter);
        String stopDate = smokingEndTime.format(dateFormatter);
        String stopTime = smokingEndTime.format(timeFormatter);

        Toast.makeText(this, "Event accepted", Toast.LENGTH_SHORT).show();

        SmokingEvent event = new SmokingEvent("Smoking", startDate,
                startTime, stopDate, stopTime, true,
                false, false, UUID.randomUUID().toString());

        sensorAiMediator.storeSmokingEvent(event);

    }

    public void setSmokingDetectionNoUserAction()
    {
        String startDate = smokingStartTime.format(dateFormatter);
        String startTime = smokingStartTime.format(timeFormatter);
        String stopDate = smokingEndTime.format(dateFormatter);
        String stopTime = smokingEndTime.format(timeFormatter);

        Toast.makeText(this, "No User Interaction", Toast.LENGTH_SHORT).show();

        SmokingEvent event = new SmokingEvent("Smoking", startDate,
                startTime, stopDate, stopTime, false,
                false, false, UUID.randomUUID().toString());

        sensorAiMediator.storeSmokingEvent(event);

    }
}
