package de.uni_freiburg.iems.beatit;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public class ManualSmokeEvent extends WearableActivity {

    private int resultValue = 0;
    private Intent returnIntent = null;
    private SeekBar yourSeekBar;
    private int iMinPerTick = 15;
    private boolean bManual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_smoke_event);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        TextView text2View = (TextView) findViewById(R.id.textView11);
        TextView text1View = (TextView) findViewById(R.id.textView7);
        Button MainButton = (Button) findViewById(R.id.ManualSmokeEventButton);

        yourSeekBar=(SeekBar) findViewById(R.id.seekBar);
        yourSeekBar.setOnSeekBarChangeListener(new yourListener());

        // Ermittlung fuer was die Activity aufgerufen wurde
        Bundle b = this.getIntent().getExtras();
        if(b != null) {
            if (b.getString("Task").equalsIgnoreCase("Pause")) {
                bManual = false;
            } else {
                bManual = true;
            }
        }

        if (bManual == false){
            MainButton.setText("Pause detection");
            text1View.setText("Pause detection for");
            text2View.setText("0 mintues");
            yourSeekBar.setMax(24);
            yourSeekBar.setProgress(24);
        }
        else {
            MainButton.setText("New cigarette");
            text1View.setText("Cigarette was smoked");
            text2View.setText("0 mintues ago");
            yourSeekBar.setMax(48);
            yourSeekBar.setProgress(48);
        }
    }

    public void onClickCreateSmokeEvent(View v) {

        long lTimeOffset;
        LocalDateTime Time;
        Bundle b = new Bundle();

        lTimeOffset = ((yourSeekBar.getProgress() - yourSeekBar.getMax()) * -iMinPerTick);

        if (bManual == false){
            b.putString("Time", Long.toString(lTimeOffset));
        }
        else {
            Time = LocalDateTime.now();
            Time = Time.minus(lTimeOffset, (TemporalUnit) ChronoUnit.MINUTES);
            b.putString("Time", Time.toString());
        }

        returnIntent = new Intent();
        returnIntent.putExtras(b);
        resultValue = 1;

        onDestroy();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (returnIntent == null) {
            setResult(resultValue);
        }
        else {
            setResult(resultValue, returnIntent);
        }
        this.finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        setResult(resultValue, null);
        this.finish();
    }

    private class yourListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            int iMinVal = ((progress - seekBar.getMax()) * -iMinPerTick);
            int iHourVal = iMinVal / 60;
            String strManual = "";

            iMinVal %= 60;

            TextView textView = (TextView) findViewById(R.id.textView11);

            if (bManual == true){
                strManual = " ago";
            }

            if (iHourVal == 0) {
                textView.setText(iMinVal + " mintues" + strManual);
            }
            else if (iHourVal == 1) {
                textView.setText(iHourVal+ " hour and " + iMinVal + " mintues" + strManual);
            }
            else {
                textView.setText(iHourVal+ " hours and " + iMinVal + " mintues" + strManual);
            }
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {}

    }
}

