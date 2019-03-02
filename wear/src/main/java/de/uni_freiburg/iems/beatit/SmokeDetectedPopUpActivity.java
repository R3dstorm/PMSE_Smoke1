package de.uni_freiburg.iems.beatit;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.view.WindowManager;

public class SmokeDetectedPopUpActivity extends WearableActivity {

    private int resultValue = 0;
    private CountDownTimer timeout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smoking_detected);
        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        timeout = new CountDownTimer(30000, 1000) {

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
        timeout.cancel();
        resultValue = 2;
        onDestroy();
    }

    public void onClickAcceptSmokeEvent(View v) {
        // save event in database and destroy Pop-Up
        timeout.cancel();
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