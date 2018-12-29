package sensorReadoutModule;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.opencsv.CSVWriter;
import static com.opencsv.ICSVWriter.NO_ESCAPE_CHARACTER;
import static com.opencsv.ICSVWriter.NO_QUOTE_CHARACTER;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

import de.uni_freiburg.iems.beatit.R;
import sensorReadoutModule.SensorReadoutService.SensorReadoutBinder;

/* Callback interface for measurementCompleteEvent*/
interface MeasurementCompleteListener {
    void measurementCompleted();
}

public class dataAcquisitionActivity extends WearableActivity implements MeasurementCompleteListener {

    private float[] singleMeasurement;
    private float[][] dataStorage;
    private SensorReadout sensor;
    private Timer timer;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private TextView daqStatusText;
    private TextView dataOutputTextACCX, dataOutputTextACCY, dataOutputTextACCZ;
    private TextView dataOutputTextGYRX, dataOutputTextGYRY, dataOutputTextGYRZ;
    private ToggleButton idleToggleButton;
    private ToggleButton smokingToggleButton;
    private Button storeDataButton;
    private Switch startStopSensorsSwitch;
    private boolean measurementStarted = false;
    private LocalDateTime startOfMeasurement;

    private Intent sensorServiceIntent;
    private SensorReadoutService sensorService;
    private boolean sensorServiceBound = false;
    private boolean sensorServiceStarted = false;

    private ServiceConnection sensorServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to SensorReadoutService, cast the IBinder and get SensorReadoutService instance
            SensorReadoutBinder binder = (SensorReadoutBinder) service;
            sensorService = binder.getService();
            sensorServiceBound = true;

            if (SensorReadoutStatus.UNSUPPORTED_SENSORS.equals(sensorService.getSensorStatus())) {
                daqStatusText.setText("sensors unsupported");
                daqStatusText.setTextColor(Color.RED);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            sensorServiceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_acquisition);

        singleMeasurement = new float[10];

        daqStatusText = findViewById(R.id.textView);
        dataOutputTextACCX = findViewById(R.id.textView2);
        dataOutputTextACCY = findViewById(R.id.textView3);
        dataOutputTextACCZ = findViewById(R.id.textView4);
        dataOutputTextGYRX = findViewById(R.id.textView8);
        dataOutputTextGYRY = findViewById(R.id.textView9);
        dataOutputTextGYRZ = findViewById(R.id.textView10);

        idleToggleButton = findViewById(R.id.toggleButton);
        smokingToggleButton = findViewById(R.id.toggleButton2);
        storeDataButton = findViewById(R.id.button2);
        startStopSensorsSwitch = findViewById(R.id.switch1);

        /* Start/Stop acquisition of data by sensors */
        startStopSensorsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    sensorServiceIntent = new Intent(dataAcquisitionActivity.this, SensorReadoutService.class);
                    sensorServiceIntent.putExtra("START_SENSOR_SERVICE", true); /* TODO is this necessary? */
                    startService(sensorServiceIntent);
                    sensorServiceStarted = true;
                    bindService(sensorServiceIntent, sensorServiceConnection, Context.BIND_AUTO_CREATE);
                }
                else{
                    unbindStopSensorService(false);
                }
            }
        });

        /* Update Button for Measurement (Idle/Recording) */
        idleToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (idleToggleButton.isChecked() == true){
                    if (sensorServiceBound) {
                        measurementStarted = true;
                        idleToggleButton.setTextOn("RECORDING");
                        idleToggleButton.setChecked(true);
                        startOfMeasurement = LocalDateTime.now();
                        if (wakeLock == null) {
                            powerManager = (PowerManager) getSystemService(POWER_SERVICE);
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                                    "MyApp::MyWakelockTag");
                        }
                        if (!wakeLock.isHeld()) {
                            wakeLock.acquire();
                        }
                        sensorService.startMeasurement(dataAcquisitionActivity.this);
                    }
                    else {
                        outputSensorsDisabledMessage();
                        idleToggleButton.setChecked(false);
                    }
                }
                else {
                    /* Do nothing*/
                }
            }
        });

        /* Update Button for Smoking-Label:*/
        smokingToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((sensorServiceBound)){
                    if(smokingToggleButton.isChecked()){
                        sensorService.setSmokingLabel(true);
                    }
                    else{
                        sensorService.setSmokingLabel(false);
                    }

                }
                /* If service not active -> reset button status (invert) */
                else{
                    outputSensorsDisabledMessage();
                    smokingToggleButton.setChecked(!smokingToggleButton.isChecked());
                }
            }
        });

        /* Operate store data button */
        storeDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int numberOfSamples;

                if ((sensorServiceBound) && (measurementStarted)) {

                    /* Get data*/
                    dataStorage = sensorService.getFullSampleStorage();
                    numberOfSamples = sensorService.getNumberOfSamples();

                    /* Store data to file*/
                    storeDataToFile(numberOfSamples);
                    /* Reset recording button*/
                    idleToggleButton.setTextOn("RECORDING");
                    idleToggleButton.setChecked(false);
                    measurementStarted = false;
                    sensorService.stopMeasurement();
                    if (wakeLock.isHeld()) {
                        wakeLock.release();
                    }
                }
                else {
                    outputSensorsDisabledMessage();
                }
            }
        });

        // Enables Always-on
        setAmbientEnabled();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, SensorReadoutService.class);
        if ((!sensorServiceBound) && (sensorServiceStarted)) {
            bindService(intent, sensorServiceConnection, Context.BIND_AUTO_CREATE);
        }
        /* Create 250ms refresh timer*/
        startDataRefreshTimerTask(250);
    }

    /* Stop sensors and timers */
    @Override
    protected void onStop() {
        super.onStop();
        timer.cancel();
        /* unbind sensorService*/
        if (sensorServiceBound) {
            unbindStopSensorService(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindStopSensorService(false);
    }

    protected void outputSensorsDisabledMessage() {
        Toast.makeText(dataAcquisitionActivity.this, "Sensors not initialized", Toast.LENGTH_SHORT).show();
    }

    /* Start a Timer Task to schedule the refresh of the data output text */
    private void startDataRefreshTimerTask(int repeatDelay) {
        timer = new Timer();
        TimerTask refreshTimerTak = new TimerTask() {
            @Override
            public void run() {
                if (sensorServiceBound) {
                    sensorService.getSingleSample(singleMeasurement);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dataOutputTextACCX.setText(String.format("%.3f",singleMeasurement[1]));
                            dataOutputTextACCY.setText(String.format("%.3f",singleMeasurement[2]));
                            dataOutputTextACCZ.setText(String.format("%.3f",singleMeasurement[3]));
                            dataOutputTextGYRX.setText(String.format("%.3f",singleMeasurement[4]));
                            dataOutputTextGYRY.setText(String.format("%.3f",singleMeasurement[5]));
                            dataOutputTextGYRZ.setText(String.format("%.3f",singleMeasurement[6]));
                            //TODO Debug output, dismiss in future release:
                            int numbers = sensorService.getNumberOfSamples();
                            daqStatusText.setText(String.format("%d",numbers));
                        }
                    });
                }
            }
        };
        timer.scheduleAtFixedRate(refreshTimerTak, 100, repeatDelay);
    }

    private void storeDataToFile(int numberOfSamples) {
        /* Checks if external storage is available for read and write */
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss_");
            String dateString;

            /* Build file name */
            File baseDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
            dateString = startOfMeasurement.format(formatter);
            File file = new File(baseDir,dateString + "AnalysisData.csv" );
            CSVWriter writer;

            /* Check for existing files */
            int fileCounter = 0;
            while(file.exists()) {
                fileCounter++;
                String fileName = dateString + "AnalysisData" + fileCounter+".csv";
                file = new File(baseDir,fileName );
            }
            try {
                writer = new CSVWriter(new FileWriter(file),' ',NO_QUOTE_CHARACTER, NO_ESCAPE_CHARACTER,"\n");//filePath));
                for (int i=0;i< numberOfSamples;i++){

                    String[] data = {String.format("%.0f",dataStorage[i][0]),
                            String.format("%.3f",dataStorage[i][1]),
                            String.format("%.6f",dataStorage[i][2]),
                            String.format("%.6f",dataStorage[i][3]),
                            String.format("%.6f",dataStorage[i][4]),
                            String.format("%.6f",dataStorage[i][5]),
                            String.format("%.6f",dataStorage[i][6])};
                    writer.writeNext(data);
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void measurementCompleted() {
        /* The measurement is completed*/
        wakeLock.release();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                idleToggleButton.setTextOn("RECORDED");
                idleToggleButton.setChecked(true);
            }
        });

    }

    private void unbindStopSensorService(boolean unbindOnly) {
        if (sensorServiceBound) {
            unbindService(sensorServiceConnection);
            sensorServiceBound = false;
        }
        if ((!unbindOnly) && (sensorServiceStarted)){
            stopService(sensorServiceIntent);
            sensorServiceStarted = false;
        }
    }
}

