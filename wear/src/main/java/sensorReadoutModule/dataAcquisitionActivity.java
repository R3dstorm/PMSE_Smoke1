package sensorReadoutModule;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import de.uni_freiburg.iems.beatit.R;

import com.opencsv.CSVWriter;
import static com.opencsv.ICSVWriter.NO_ESCAPE_CHARACTER;
import static com.opencsv.ICSVWriter.NO_QUOTE_CHARACTER;

/* Callback interface for measurementCompleteEvent*/
interface MeasurementCompleteListener {
    void measurementCompleted();
}

public class dataAcquisitionActivity extends WearableActivity implements MeasurementCompleteListener{

    private float[] singleMeasurement;
    private float[][] dataStorage;
    private SensorReadout sensor;
    private Timer timer;
    private TextView daqStatusText;
    private TextView dataOutputTextACCX, dataOutputTextACCY, dataOutputTextACCZ;
    private TextView dataOutputTextGYRX, dataOutputTextGYRY, dataOutputTextGYRZ;
    private TextView dataOutputTextMAGX, dataOutputTextMAGY, dataOutputTextMAGZ;
    private ToggleButton idleToggleButton;
    private ToggleButton smokingToggleButton;
    private Button storeDataButton;
    private boolean labelIsSmoking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_acquisition);

        singleMeasurement = new float[10];

        daqStatusText = findViewById(R.id.textView);
        dataOutputTextACCX = findViewById(R.id.textView2);
        dataOutputTextACCY = findViewById(R.id.textView3);
        dataOutputTextACCZ = findViewById(R.id.textView4);
        dataOutputTextGYRX = findViewById(R.id.textView5);
        dataOutputTextGYRY = findViewById(R.id.textView6);
        dataOutputTextGYRZ = findViewById(R.id.textView7);
        dataOutputTextMAGX = findViewById(R.id.textView8);
        dataOutputTextMAGY = findViewById(R.id.textView9);
        dataOutputTextMAGZ = findViewById(R.id.textView10);

        idleToggleButton = findViewById(R.id.toggleButton);
        smokingToggleButton = findViewById(R.id.toggleButton2);
        storeDataButton = findViewById(R.id.button2);

        labelIsSmoking = smokingToggleButton.isChecked();

        initDAQ();

        /* Update Button for Measurement (Idle/Recording)*/
        idleToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (idleToggleButton.isChecked() == true){
                    idleToggleButton.setTextOn("RECORDING");
                    idleToggleButton.setChecked(true);
                    sensor.triggerMeasurement(dataAcquisitionActivity.this);
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
                labelIsSmoking = smokingToggleButton.isChecked();
            }
        });

        /* Operate store data button */
        storeDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float smokingValue = 0;

                /* Get data*/
                dataStorage = sensor.getDataStorage();
                /* Mark data as smoking / non-smoking*/
                if (labelIsSmoking){
                    smokingValue = 1;
                }
                for (int i = 0; i<dataStorage.length;i++){
                    dataStorage[i][0] = smokingValue;
                }
                /* Store data to file*/
                storeDataToFile();
                /* Reset recording button*/
                idleToggleButton.setTextOn("RECORDING");
                idleToggleButton.setChecked(false);
            }
        });

        /* Create 250ms refresh timer*/
        startDataRefreshTimerTask(250);

        // Enables Always-on
        setAmbientEnabled();
    }

    /* Stop sensors and timers */
    @Override
    protected void onStop()
    {
        super.onStop();
        stopDAQ();
        timer.cancel();
    }


    /* Initialize and start the acquisition of sensor data*/
    private void initDAQ (){
        int error = 0;
        sensor = new SensorReadout(this);
        error = sensor.initSensors();
        if (error != 0){
            daqStatusText.setText("sensors unsupported");
            daqStatusText.setTextColor(Color.RED);
        }

    }

    private void stopDAQ()
    {
        sensor.stopSensors();
    }

    /* Start a Timer Task to schedule the refresh of the data output text */
    private void startDataRefreshTimerTask (int repeatDelay){
        timer = new Timer();
        TimerTask refreshTimerTak = new TimerTask() {
            @Override
            public void run() {
                sensor.getSample(singleMeasurement);
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

    private void storeDataToFile ()
    {
        /* Checks if external storage is available for read and write */
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File baseDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
            File file = new File(baseDir,"AnalysisData.csv" );
            CSVWriter writer;

            /* Check for existing files*/
            int fileCounter = 0;
            while(file.exists()) {
                fileCounter++;
                String fileName = "AnalysisData"+fileCounter+".csv";
                file = new File(baseDir,fileName );
            }
            try {
                writer = new CSVWriter(new FileWriter(file),' ',NO_QUOTE_CHARACTER, NO_ESCAPE_CHARACTER,"\n");//filePath));
                for (int i=0;i<dataStorage.length;i++){

                    String smokeString;
                    if (dataStorage[i][0] == 1) {
                        smokeString = "smoking";
                    }
                    else{
                        smokeString = "NULL";
                    }

                    String[] data = {smokeString,String.format("%.3f",dataStorage[i][1]),
                            String.format("%.6f",dataStorage[i][2]),
                            String.format("%.6f",dataStorage[i][3]),
                            String.format("%.6f",dataStorage[i][4]),
                            String.format("%.6f",dataStorage[i][5]),
                            String.format("%.6f",dataStorage[i][6]),
                            String.format("%.6f",dataStorage[i][7]),
                            String.format("%.6f",dataStorage[i][8]),
                            String.format("%.6f",dataStorage[i][9])};
                    writer.writeNext(data);
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void measurementCompleted(){
        /* The measurement is completed*/
        idleToggleButton.setTextOn("RECORDED");
        idleToggleButton.setChecked(true);

    }
}
