package de.uni_freiburg.iems.beatit;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.wearable.activity.WearableActivity;
import android.view.View;

public class SmokeDetectedPopUpActivity extends WearableActivity {

    private int resultValue = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smoking_detected);
        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));

        new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                onDestroy();
            }
        }.start();
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