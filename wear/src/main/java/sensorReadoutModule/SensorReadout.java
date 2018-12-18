package sensorReadoutModule;

import android.content.Context;
import android.util.Log;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;

public class SensorReadout {

    static class SensorReadoutConfig{
        public boolean enableTerminalOutput;

        public SensorReadoutConfig (){
            enableTerminalOutput = false;
        }
    }
    private SensorReadoutConfig readOutConfig;
    private SensorManager mSensorManager;
    private Sensor AccSensor, GyroSensor, MagneticSensor;
    private SensorEventListener sensorListenerAcc, sensorListenerGyro, sensorListenerMagn;
    private boolean accCollected, gyrCollected, magCollected;
    private float ACCX, ACCY, ACCZ, GYRX, GYRY, GYRZ, MAGX, MAGY, MAGZ;
    private float[] dataObject;
    private float[][] dataStorage;
    private int dataStoragePointer;
    private static final String TAG_ACC = "ACC";
    private static final String TAG_GYR = "GYR";
    private static final String TAG_MAG = "MAG";
    private boolean measurementTriggered;
    private MeasurementCompleteListener measurementCompleteListener;

    Context mContext;

    public SensorReadout(Context mContext)
    {
        /* Construct this class... */
        this.mContext = mContext;
        this.readOutConfig = new SensorReadoutConfig();
        measurementTriggered = false;
        accCollected = gyrCollected = magCollected = false;
        dataStorage = new float[3000][10];
        dataStoragePointer = 0;
    }

    public void initSensors()
    {
        AccSensor = GyroSensor = MagneticSensor = null;
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

        AccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (AccSensor == null)
        {
            // No Sensor found on the device
            /* TODO handle ERRORS */
        }

        GyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (GyroSensor == null)
        {
            // No Sensor found on the device
            /* TODO handle ERRORS */
        }

        MagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (MagneticSensor == null)
        {
            // No Sensor found on the device
            /* TODO handle ERRORS */
        }

        /* Init Event-Listeners:*/
        sensorListenerAcc = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                ACCX = event.values[0];
                ACCY = event.values[1];
                ACCZ = event.values[2];
                if (readOutConfig.enableTerminalOutput == true){
                    Log.d(TAG_ACC, String.format("%f, %f, %f, %d", ACCX, ACCY, ACCZ, event.timestamp));
                }
                /* Make sure exactly one sample from each sensor is measured per data point */
                if ((measurementTriggered == true) &&
                        ((gyrCollected == true) && (magCollected == true))) {
                    accCollected = false;
                    gyrCollected = false;
                    magCollected = false;
                    dataStorage[dataStoragePointer][1] = ACCX;
                    dataStorage[dataStoragePointer][2] = ACCY;
                    dataStorage[dataStoragePointer][3] = ACCZ;
                    accCollected = true;
                    if (dataStoragePointer < (dataStorage.length-1)) {
                        dataStoragePointer++;
                    }
                    else
                    {
                        dataStoragePointer = 0;
                        measurementTriggered = false;
                        measurementCompleteListener.measurementCompleted();
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        sensorListenerGyro = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                GYRX = event.values[0];
                GYRY = event.values[1];
                GYRZ = event.values[2];
                if (readOutConfig.enableTerminalOutput == true){
                    Log.d(TAG_GYR, String.format("%f, %f, %f, %d", GYRX, GYRY, GYRZ, event.timestamp));
                }
                /* Make sure exactly one sample from each sensor is measured per data point */
                if ((measurementTriggered == true) && (gyrCollected == false)) {
                    dataStorage[dataStoragePointer][4] = GYRX;
                    dataStorage[dataStoragePointer][5] = GYRY;
                    dataStorage[dataStoragePointer][6] = GYRZ;
                    gyrCollected = true;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        sensorListenerMagn = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                MAGX = event.values[0];
                MAGY = event.values[1];
                MAGZ = event.values[2];
                if (readOutConfig.enableTerminalOutput == true){
                    Log.d(TAG_MAG, String.format("%f, %f, %f, %d", MAGX, MAGY, MAGZ, event.timestamp));
                }
                /* Make sure exactly one sample from each sensor is measured per data point */
                if ((measurementTriggered == true) && (magCollected == false)) {
                    dataStorage[dataStoragePointer][7] = MAGX;
                    dataStorage[dataStoragePointer][8] = MAGY;
                    dataStorage[dataStoragePointer][9] = MAGZ;
                    magCollected = true;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        mSensorManager.registerListener(sensorListenerAcc, AccSensor, 20000); /* FIXME sensor always outputs sampling period of 10ms ???*/
        mSensorManager.registerListener(sensorListenerGyro, GyroSensor, 20000);
        mSensorManager.registerListener(sensorListenerMagn, MagneticSensor, 20000);
    }

    public void stopSensors()
    {
        mSensorManager.unregisterListener(sensorListenerAcc, AccSensor);
        mSensorManager.unregisterListener(sensorListenerGyro, GyroSensor);
        mSensorManager.unregisterListener(sensorListenerMagn, MagneticSensor);
    }

    public void triggerMeasurement(MeasurementCompleteListener _measurementCompleteListener){
        measurementTriggered = true;
        dataStoragePointer = 0;
        measurementCompleteListener = _measurementCompleteListener;
    }

    public void getSample(float[] dataObject)
    {
        /* Format for captured data: LABEL ACCX ACCY ACCZ GYRX GYRY GYRZ MAGX MAGY MAGZ */
        /* Required sensors: Accelerometer; Gyroscope; Magnetic Field Sensor */
        /* TODO Check if synchronization between sensor data is correct. (Difference in timestamp < xy? */
        dataObject[1] = ACCX;
        dataObject[2] = ACCY;
        dataObject[3] = ACCZ;
        dataObject[4] = GYRX;
        dataObject[5] = GYRY;
        dataObject[6] = GYRZ;
        dataObject[7] = MAGX;
        dataObject[8] = MAGY;
        dataObject[9] = MAGZ;
    }

    public float[][] getDataStorage ()
    {
        return dataStorage;
    }
}
