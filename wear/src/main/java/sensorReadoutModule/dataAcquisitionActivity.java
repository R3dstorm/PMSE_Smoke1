package sensorReadoutModule;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;

import de.uni_freiburg.iems.beatit.R;

public class dataAcquisitionActivity extends WearableActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_acquisition);

        // Enables Always-on
        setAmbientEnabled();
    }
}
