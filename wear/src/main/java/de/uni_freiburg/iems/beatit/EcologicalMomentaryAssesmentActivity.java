package de.uni_freiburg.iems.beatit;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.widget.TextView;
import sensorReadoutModule.sensorReadout;

public class EcologicalMomentaryAssesmentActivity extends WearableActivity {

    private TextView mTextView;
    private sensorReadout sensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecological_momentary_assesment);

        mTextView = (TextView) findViewById(R.id.text);
        sensor = new sensorReadout(this);
        sensor.initSensors();
        sensor.getValues();
        // Enables Always-on
        setAmbientEnabled();
    }
}
