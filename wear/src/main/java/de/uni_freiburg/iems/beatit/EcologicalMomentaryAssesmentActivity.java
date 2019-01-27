package de.uni_freiburg.iems.beatit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.wear.ambient.AmbientModeSupport;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.ToggleButton;

import sensorReadoutModule.dataAcquisitionActivity;
import sensorReadoutModule.SensorReadoutService;

/* Callback interface for measurementCompleteEvent*/
interface ModelEvaluatedListener {
    void modelEvaluatedCB(boolean smoking);
}

public class EcologicalMomentaryAssesmentActivity extends AppCompatActivity implements View.OnClickListener, ModelEvaluatedListener, AmbientModeSupport.AmbientCallbackProvider {

    private TextView mTextView;
    private ToggleButton startButton;
    private Intent sensorServiceIntent;
    private Boolean sensorServiceStarted = false;
    private Mediator sensorAiMediator;
    private CheckBox smokingDetected;
    private AmbientModeSupport.AmbientController mAmbientController;

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

        mTextView = (TextView) findViewById(R.id.text);

        /* recyclerview */
//        RecyclerView recyclerView = findViewById(R.id.recyclerview);
//        final SmokingEventListAdapter adapter = new SmokingEventListAdapter(this);
//        recyclerView.setAdapter(adapter);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Enables Always-on
        mAmbientController = AmbientModeSupport.attach(this);
        //setAmbientEnabled();
        Button daqButton = findViewById(R.id.startPageButtonLable);
        startButton = findViewById(R.id.startPageButtonPlay);
        smokingDetected = findViewById(R.id.checkBox);
        daqButton.setOnClickListener(this);
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

    public void onAddEventButtonClick(View v){
        if (sensorAiMediator != null) {
            SmokingEvent event = new SmokingEvent("blub", "20190101",
                    "1125", "20190102", "1200", true);
            sensorAiMediator.storeSmokingEvent(event);
        }
    }

    public void onPlayButtonClick(View v){
        if(startButton.isChecked() == true) {
            sensorServiceIntent = new Intent(EcologicalMomentaryAssesmentActivity.this, SensorReadoutService.class);
            startService(sensorServiceIntent);
            sensorServiceStarted = true;

            sensorAiMediator = new Mediator(this, EcologicalMomentaryAssesmentActivity.this);
        }
        else
        {
            stopService(sensorServiceIntent);
            /* tell connected modules to unbind */
            sensorAiMediator.unbindFromServices();

            sensorServiceStarted = false;
        }
    }

    @Override
    public void modelEvaluatedCB(boolean smoking) {
        /* The measurement is completed*/
        if (smoking) {
            smokingDetected.setChecked(true);
        } else {
            smokingDetected.setChecked(false);
        }
    }
    public void onLogButtonClick(View v){
        Intent intent = new Intent(this, SmokingLogActivity.class);
        startActivity(intent);
    }
}
