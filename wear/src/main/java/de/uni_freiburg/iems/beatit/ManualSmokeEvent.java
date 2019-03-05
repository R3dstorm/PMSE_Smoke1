package de.uni_freiburg.iems.beatit;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.view.WindowManager;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_smoke_event);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        TextView textView = (TextView) findViewById(R.id.textView11);
        textView.setText("0 mintues ago");

        yourSeekBar=(SeekBar) findViewById(R.id.seekBar);
        yourSeekBar.setOnSeekBarChangeListener(new yourListener());
    }

    public void onClickCreateSmokeEvent(View v) {

        long lTimeOffset;
        LocalDateTime Time;

        yourSeekBar=(SeekBar) findViewById(R.id.seekBar);
        lTimeOffset =  ((yourSeekBar.getProgress() - yourSeekBar.getMax()) * -iMinPerTick);

        Time = LocalDateTime.now();
        Time = Time.minus(lTimeOffset, (TemporalUnit) ChronoUnit.MINUTES);
        Bundle b = new Bundle();
        b.putString("Time", Time.toString());
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

            iMinVal %= 60;

            TextView textView = (TextView) findViewById(R.id.textView11);

            if (iHourVal == 0) {
                textView.setText(iMinVal + " mintues ago");
            }
            else if (iHourVal == 1) {
                textView.setText(iHourVal+ " hour and " + iMinVal + " mintues ago");
            }
            else {
                textView.setText(iHourVal+ " hours and " + iMinVal + " mintues ago");
            }
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {}

    }
}

