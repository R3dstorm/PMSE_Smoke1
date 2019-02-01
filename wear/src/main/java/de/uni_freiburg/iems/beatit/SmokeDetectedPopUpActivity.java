package de.uni_freiburg.iems.beatit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.wearable.activity.WearableActivity;
import android.view.View;

import java.lang.reflect.Array;

public class SmokeDetectedPopUpActivity extends WearableActivity {

    private int resultValue = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smoking_detected);
        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
    }


    public void saveDetectedSmokingEvent() {
        resultValue = 1;
    }

    public void onClickDeclineSmokeEvent(View v) {
        resultValue = 2;
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
        setResult(resultValue);
        this.finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        setResult(resultValue);
        this.finish();
    }

}
