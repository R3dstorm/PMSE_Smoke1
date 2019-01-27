package de.uni_freiburg.iems.beatit;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.view.WindowManager;
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

public class EcologicalMomentaryAssesmentActivity extends WearableActivity implements View.OnClickListener, ModelEvaluatedListener {

    private TextView mTextView;
    private ToggleButton startButton;
    private Intent sensorServiceIntent;
    private Boolean sensorServiceStarted = false;
    private Mediator sensorAiMediator;
    private CheckBox smokingDetected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecological_momentary_assesment);

        mTextView = (TextView) findViewById(R.id.text);

        // Enables Always-on
        setAmbientEnabled();
        Button daqButton = findViewById(R.id.startPageButtonLable);
        startButton = findViewById(R.id.startPageButtonPlay);
        smokingDetected = findViewById(R.id.checkBox);
        daqButton.setOnClickListener(this);

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
        Intent intent = new Intent(this, SmokeDetectedPopUpActivity.class);
        startActivity(intent);
    }
}
