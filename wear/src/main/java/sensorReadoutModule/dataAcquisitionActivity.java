package sensorReadoutModule;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.widget.TextView;
import java.util.Timer;
import java.util.TimerTask;
import de.uni_freiburg.iems.beatit.R;

public class dataAcquisitionActivity extends WearableActivity {

    private float[][] dataStorage;
    private float[] singleMeasurement;
    private SensorReadout sensor;
    private TextView dataOutputTextACCX, dataOutputTextACCY, dataOutputTextACCZ;
    private TextView dataOutputTextGYRX, dataOutputTextGYRY, dataOutputTextGYRZ;
    private TextView dataOutputTextMAGX, dataOutputTextMAGY, dataOutputTextMAGZ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_acquisition);

        singleMeasurement = new float[10];
        dataOutputTextACCX = (TextView) findViewById(R.id.textView2);
        dataOutputTextACCY = (TextView) findViewById(R.id.textView3);
        dataOutputTextACCZ = (TextView) findViewById(R.id.textView4);
        dataOutputTextGYRX = (TextView) findViewById(R.id.textView5);
        dataOutputTextGYRY = (TextView) findViewById(R.id.textView6);
        dataOutputTextGYRZ = (TextView) findViewById(R.id.textView7);
        dataOutputTextMAGX = (TextView) findViewById(R.id.textView8);
        dataOutputTextMAGY = (TextView) findViewById(R.id.textView9);
        dataOutputTextMAGZ = (TextView) findViewById(R.id.textView10);

        initDAQ();

        /* Create 250ms refresh timer*/
        startDataRefreshTimerTask(250);

        // Enables Always-on
        setAmbientEnabled();
    }

    /* TODO on delete? -> unsubscribe sensor listener*/

    /* Initialize and start the acquisition of sensor data*/
    /* TODO create separate function for starting the sensors?*/
    private void initDAQ (){
        dataStorage = new float[10][3000];
        sensor = new SensorReadout(this);
        sensor.initSensors();
    }

    /* Start a Timer Task to schedule the refresh of the data output text*/
    private void startDataRefreshTimerTask (int repeatDelay){
        Timer timer = new Timer();
        TimerTask refreshTimerTak = new TimerTask() {
            @Override
            public void run() {
                sensor.getValues(singleMeasurement);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dataOutputTextACCX.setText(String.format("%.3f",singleMeasurement[1]));
                        dataOutputTextACCY.setText(String.format("%.3f",singleMeasurement[2]));
                        dataOutputTextACCZ.setText(String.format("%.3f",singleMeasurement[3]));
                        dataOutputTextGYRX.setText(String.format("%.3f",singleMeasurement[4]));
                        dataOutputTextGYRY.setText(String.format("%.3f",singleMeasurement[5]));
                        dataOutputTextGYRZ.setText(String.format("%.3f",singleMeasurement[6]));
                        dataOutputTextMAGX.setText(String.format("%.3f",singleMeasurement[7]));
                        dataOutputTextMAGY.setText(String.format("%.3f",singleMeasurement[8]));
                        dataOutputTextMAGZ.setText(String.format("%.3f",singleMeasurement[9]));
                    }
                });
            }
        };

        timer.scheduleAtFixedRate(refreshTimerTak, 100, repeatDelay);
    }
    /* TODO cancel Timer?*/
}
