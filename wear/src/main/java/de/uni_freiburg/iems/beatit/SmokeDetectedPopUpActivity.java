package de.uni_freiburg.iems.beatit;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;

public class SmokeDetectedPopUpActivity extends WearableActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smoking_detected);

    }
}
