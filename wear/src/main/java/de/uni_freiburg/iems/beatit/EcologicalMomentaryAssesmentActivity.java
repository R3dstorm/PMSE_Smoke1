package de.uni_freiburg.iems.beatit;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import MachineLearningModule.ModelHandler;
import sensorReadoutModule.dataAcquisitionActivity;
import sensorReadoutModule.SensorReadoutService;

public class EcologicalMomentaryAssesmentActivity extends WearableActivity implements View.OnClickListener {

    private TextView mTextView;
    private ToggleButton startButton;
    private Intent sensorServiceIntent;
    private Boolean sensorServiceStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecological_momentary_assesment);

        mTextView = (TextView) findViewById(R.id.text);

        // Enables Always-on
        setAmbientEnabled();
        Button daqButton = findViewById(R.id.startPageButtonLable);
        startButton = findViewById(R.id.startPageButtonPlay);
        daqButton.setOnClickListener(this);

        ModelHandler m = new ModelHandler();
        m.loadModel(getAssets());
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
        }
        else
        {
            stopService(sensorServiceIntent);
            sensorServiceStarted = false;
        }
    }

    public void onLogButtonClick(View v){
        Intent intent = new Intent(this, SmokeDetectedPopUpActivity.class);
        startActivity(intent);
    }
}
