package SensorReadoutModule;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
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
import SensorReadoutModule.SensorReadoutService.SensorReadoutBinder;

/* Callback interface for measurementCompleteEvent*/
interface MeasurementCompleteListener {
    void measurementCompletedCB();
}

public class dataAcquisitionActivity extends WearableActivity implements MeasurementCompleteListener {

    private float[] singleMeasurement;
    private float[][] dataStorage;
    private Timer timer;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private TextView dataOutputTextACCX, dataOutputTextACCY, dataOutputTextACCZ;
    private TextView dataOutputTextGYRX, dataOutputTextGYRY, dataOutputTextGYRZ;
    private ToggleButton idleToggleButton;
    private ToggleButton smokingToggleButton;
    private Button storeDataButton, DaqButton;
    private boolean measurementStarted = false;
    private boolean DataStorageRequested = false;
    private LocalDateTime startOfMeasurement;
    private String strDaqLabel;

    private Intent sensorServiceIntent;
    private SensorReadoutService sensorService;
    private boolean sensorServiceBound = false;
    private boolean sensorServiceRunning = false;

    private ServiceConnection sensorServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to SensorReadoutService, cast the IBinder and get SensorReadoutService instance
            SensorReadoutBinder binder = (SensorReadoutBinder) service;
            sensorService = binder.getService();
            sensorServiceBound = true;
            sensorServiceRunning = sensorService.isSensorServiceRunning();
            if (sensorServiceRunning) {
                if (SensorReadoutStatus.UNSUPPORTED_SENSORS.equals(sensorService.getSensorStatus())) {
                    /* TODO only alert if none of the sensors is available */
                    //Toast.makeText(dataAcquisitionActivity.this, "Sensors unsupported", Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            sensorServiceBound = false;
            sensorServiceRunning = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_acquisition);

        singleMeasurement = new float[10];


        dataOutputTextACCX = findViewById(R.id.textView2);
        dataOutputTextACCY = findViewById(R.id.textView3);
        dataOutputTextACCZ = findViewById(R.id.textView4);
        dataOutputTextGYRX = findViewById(R.id.textView8);
        dataOutputTextGYRY = findViewById(R.id.textView9);
        dataOutputTextGYRZ = findViewById(R.id.textView10);

        idleToggleButton = findViewById(R.id.toggleButton);
        smokingToggleButton = findViewById(R.id.toggleButton2);
        storeDataButton = findViewById(R.id.button2);
        DaqButton = findViewById(R.id.DAQ_Button);

        /* Update Button for Measurement (Idle/Recording) */
        idleToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (idleToggleButton.isChecked() == true){
                    if (sensorServiceRunning) {
                        measurementStarted = true;
                        idleToggleButton.setTextOn("RECORD");
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
                        sensorService.triggerSingleMeasurement(dataAcquisitionActivity.this);
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
                if ((sensorServiceRunning)){
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
                if ((sensorServiceRunning) && (measurementStarted)) {

                    /* Store data to file -> Enable Storing -> Storing + button reset is executed by timer event*/
                    /* TODO solve this solution by job intent*/
                    DataStorageRequested = true;
                    Toast.makeText(dataAcquisitionActivity.this, "Storing started", Toast.LENGTH_SHORT).show();
                }
                else {
                    outputSensorsDisabledMessage();
                }
            }
        });

        // Enables Always-on
        setAmbientEnabled();

        strDaqLabel = "Smoking";
    }

    @Override
    protected void onStart() {
        super.onStart();

        /* Try to bind to server -> when bound, check if server is running */
        if (!sensorServiceBound) {
            sensorServiceIntent = new Intent(this, SensorReadoutService.class);
            sensorServiceIntent.putExtra("BIND_SENSOR_SERVICE", true); /* TODO is this necessary? */
            bindService(sensorServiceIntent, sensorServiceConnection, Context.BIND_AUTO_CREATE);
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
            unbindFromServices();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindFromServices();
    }

    protected void outputSensorsDisabledMessage() {
        Toast.makeText(dataAcquisitionActivity.this, "Sensors not initialized", Toast.LENGTH_SHORT).show();
    }

    /* Start a Timer Task to schedule the refresh of the data output text */
    private void startDataRefreshTimerTask(int repeatDelay) {
        timer = new Timer();
        TimerTask refreshTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (sensorServiceRunning) {
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
                            int numbers = sensorService.getNumberOfSamples();

                            if (!DataStorageRequested) {
                                //TODO Debug output, dismiss in future release:
                                //daqStatusText.setText(String.format("%d", numbers));
                                //daqStatusText.setTextColor(Color.WHITE);
                            }
                            else{
                                int numberOfSamples;

                                /* Get data */
                                dataStorage = sensorService.getFullSampleStorage();
                                numberOfSamples = sensorService.getNumberOfSamples();
                                /* Store data */
                                storeDataToFile(numberOfSamples);
                                DataStorageRequested = false;
                                /* Reset recording button*/
                                storingDataCompleted();
                            }
                        }
                    });
                }
            }
        };
        timer.scheduleAtFixedRate(refreshTimerTask, 100, repeatDelay);
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
            dateString = dateString + strDaqLabel + "_";
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
                Toast.makeText(dataAcquisitionActivity.this, "Storing finished", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void measurementCompletedCB() {
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

    private void storingDataCompleted() {
        idleToggleButton.setTextOn("RECORD");
        idleToggleButton.setChecked(false);
        measurementStarted = false;
        sensorService.stopSingleMeasurement();
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    public void unbindFromServices() {
        if (sensorServiceBound) {
            unbindService(sensorServiceConnection);
            sensorServiceBound = false;
        }
    }

    public void OnClickMainText(View v) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (strDaqLabel == "Smoking"){
                    strDaqLabel = "Washing";
                }
                else{
                    strDaqLabel = "Smoking";
                }
                smokingToggleButton.setTextOn(strDaqLabel);
                smokingToggleButton.setTextOff(strDaqLabel);
                smokingToggleButton.setText(strDaqLabel);
                DaqButton.setText(strDaqLabel + " DAQ");

                if ((sensorServiceRunning) && (measurementStarted)) {
                    storingDataCompleted();
                    Toast.makeText(dataAcquisitionActivity.this, "Recording data stopped", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

