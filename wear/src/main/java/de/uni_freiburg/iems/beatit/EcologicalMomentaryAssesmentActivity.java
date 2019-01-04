package de.uni_freiburg.iems.beatit;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import MachineLearningModule.ModelHandler;
import sensorReadoutModule.dataAcquisitionActivity;

public class EcologicalMomentaryAssesmentActivity extends WearableActivity implements View.OnClickListener {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecological_momentary_assesment);

        mTextView = (TextView) findViewById(R.id.text);

        // Enables Always-on
        setAmbientEnabled();
        Button daqButton = findViewById(R.id.button);
        daqButton.setOnClickListener(this);

        ModelHandler m = new ModelHandler();
        m.loadModel(getAssets());
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, dataAcquisitionActivity.class);
        startActivity(intent);
    }
}
