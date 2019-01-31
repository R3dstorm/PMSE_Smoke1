package de.uni_freiburg.iems.beatit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.wearable.activity.WearableActivity;
import android.view.View;

public class SmokeDetectedPopUpActivity extends WearableActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smoking_detected);
        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
    }


    public void saveDetectedSmokingEvent() {

    }

    public void onClickDeclineSmokeEvent(View v) {
        onDestroy();
    }

    public void onClickAcceptSmokeEvent(View v) {
        // save event in database and destroy Pop-Up
        saveDetectedSmokingEvent();
        onDestroy();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.finish();
    }

}
