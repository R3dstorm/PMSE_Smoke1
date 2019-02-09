package SensorReadoutModule;

import android.content.Context;
import android.os.Handler;
import android.support.v4.util.CircularArray;
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

    private final int CIRCULAR_DATA_STORAGE_SIZE = 1000;
    private final int NUMBER_SENSOR_ELEMENTS = 6;

    private SensorReadoutConfig readOutConfig;
    private SensorManager mSensorManager;
    private Sensor AccSensor, GyroSensor;
    private SensorEventListener sensorListenerAcc, sensorListenerGyro;
    private boolean accCollected, gyrCollected;
    private float ACCX, ACCY, ACCZ, GYRX, GYRY, GYRZ, MAGX, MAGY, MAGZ;
    private float[][] dataStorage;
    private float[] sample;
    private CircularArray circularDataStorage;
    private int dataStoragePointer;
    private static final String TAG_ACC = "ACC";
    private static final String TAG_GYR = "GYR";
    private static final String TAG_CIRC = "CIRCULAR_ARRAY";
    private boolean singleMeasurementTriggered, continuousMeasurementTriggered;
    private MeasurementCompleteListener measurementCompleteListener;
    private SensorReadoutStatus sensorStatus = SensorReadoutStatus.NOT_INITIALIZED;
    private boolean isSmokingLabel;

    Context mContext;

    public SensorReadout(Context mContext, Handler handler) {
        /* Construct this class... */
        this.mContext = mContext;
        this.readOutConfig = new SensorReadoutConfig();
        singleMeasurementTriggered = false;
        continuousMeasurementTriggered = false;
        accCollected = gyrCollected = false;
        sensorListenerAcc = sensorListenerGyro = null;
        ACCX = ACCY = ACCZ = GYRX = GYRY = GYRZ = MAGX = MAGY = MAGZ = 0;
        dataStorage = new float[30000][10];
        sample = new float[6];
        circularDataStorage = new CircularArray(CIRCULAR_DATA_STORAGE_SIZE);
        dataStoragePointer = 0;
        isSmokingLabel = false;

        initSensors(handler);
    }

    public int initSensors(Handler handler) {
        int error = 0;
        AccSensor = GyroSensor = null;
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

        /* Init Event-Listeners:*/
        sensorListenerAcc = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float isSmokingLabelFloat = 0;
                if (isSmokingLabel){
                    isSmokingLabelFloat = 1;
                }
                ACCX = event.values[0];
                ACCY = event.values[1];
                ACCZ = event.values[2];
                if (readOutConfig.enableTerminalOutput == true){
                    Log.d(TAG_ACC, String.format("%f, %f, %f, %d", ACCX, ACCY, ACCZ, event.timestamp));
                }
                /* Make sure exactly one sample from each sensor is measured per data point.
                 * If there are unsupported sensors go on anyway.*/
                if (((gyrCollected == true) || (GyroSensor == null))) {
                    if (singleMeasurementTriggered || continuousMeasurementTriggered) {
                        accCollected = false;
                        gyrCollected = false;
                        /* TODO Create Class containing SesnorData (9x Float + Bool)*/
                        accCollected = true;
                    }
                    /* check for storing data for a single measurement */
                    if (singleMeasurementTriggered) {
                        dataStorage[dataStoragePointer][0] = isSmokingLabelFloat;
                        dataStorage[dataStoragePointer][1] = ACCX;
                        dataStorage[dataStoragePointer][2] = ACCY;
                        dataStorage[dataStoragePointer][3] = ACCZ;

                        if (dataStoragePointer < (dataStorage.length - 1)) {
                            dataStoragePointer++;
                        } else {
                            singleMeasurementTriggered = false;
                            measurementCompleteListener.measurementCompletedCB();
                        }
                    }
                    /* check for storing data for continuous measurement */
                    if (continuousMeasurementTriggered){
                        sample[0] = ACCX;
                        sample[1] = ACCY;
                        sample[2] = ACCZ;
                        /* Add data to circularArray (using clone because addLast seams to store only data identical references?*/
                        float localClone[];
                        localClone = sample.clone();
                        if (circularDataStorage.size() < CIRCULAR_DATA_STORAGE_SIZE) {
                            /* Buffer is not full ... fill data*/
                            circularDataStorage.addLast(localClone);
                        }
                        else { /* Buffer is full ... swap data */
                            circularDataStorage.popFirst();
                            circularDataStorage.addLast(localClone);
                        }

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
                if (gyrCollected == false) {

                    if (singleMeasurementTriggered || continuousMeasurementTriggered) {
                        gyrCollected = true;
                    }
                    if (singleMeasurementTriggered) {
                        dataStorage[dataStoragePointer][4] = GYRX;
                        dataStorage[dataStoragePointer][5] = GYRY;
                        dataStorage[dataStoragePointer][6] = GYRZ;
                    }
                    if (continuousMeasurementTriggered) {
                        sample[3] = GYRX;
                        sample[4] = GYRY;
                        sample[5] = GYRZ;
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        AccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (AccSensor == null){
            // No Sensor found on the device
            Log.e(TAG_ACC,"No accelerometer supported");
            error = -1;
        }
        else{
            mSensorManager.registerListener(sensorListenerAcc, AccSensor, 20000, handler);} /* FIXME sensor always outputs sampling period of 10ms ???*/

        GyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (GyroSensor == null){
            // No Sensor found on the device
            /* TODO handle ERRORS */
            Log.e(TAG_GYR,"No gyroscope supported");
            error = -1;
        }
        else{
            mSensorManager.registerListener(sensorListenerGyro, GyroSensor, 20000, handler);
        }

        if (error == 0) {
            sensorStatus = SensorReadoutStatus.INITIALIZED;
        }
        else {
            sensorStatus = SensorReadoutStatus.UNSUPPORTED_SENSORS;
        }
        return error;
    }

    public void stopSensors() {
        if (sensorListenerAcc != null){
            mSensorManager.unregisterListener(sensorListenerAcc, AccSensor);
            sensorListenerAcc = null;
        }
        if (sensorListenerGyro != null){
            mSensorManager.unregisterListener(sensorListenerGyro, GyroSensor);
            sensorListenerGyro = null;
        }
    }

    public void triggerSingleMeasurement(MeasurementCompleteListener _measurementCompleteListener) {
        singleMeasurementTriggered = true;
        dataStoragePointer = 0;
        measurementCompleteListener = _measurementCompleteListener;
    }

    public void stopSingleMeasurement() {
        singleMeasurementTriggered = false;
        dataStoragePointer = 0;
    }

    public void triggerContinuousMeasurement() {
        continuousMeasurementTriggered = true;
    }

    public void stopContinuousMeasurement() {
        continuousMeasurementTriggered = false;
    }

    public void getSample(float[] dataObject) {
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

    public float[][] getSingleMeasurementDataStorage()
    {
        return dataStorage;
    }

    public boolean isContMeasDataAvailable() {
        return (circularDataStorage.size() == CIRCULAR_DATA_STORAGE_SIZE) ? true : false;
    }

    /* Get Continuous Measurement Data from Buffer and convert to double */
    public double[][] getContinuousMeasurementDataStorage() {
        double[][] data = new double[NUMBER_SENSOR_ELEMENTS][CIRCULAR_DATA_STORAGE_SIZE];
        float[] dataElement;
        synchronized (this) {
            for (int i = 0; i < CIRCULAR_DATA_STORAGE_SIZE; i++) {
                dataElement = (float[]) circularDataStorage.get(i);
                for (int j = 0; j < NUMBER_SENSOR_ELEMENTS; j++) {
                    data[j][i] = dataElement[j];
                }
            }
        }
        return data;
    }

    public int getNumberOfSamples()
    {
        return (dataStoragePointer+1);
    }

    public void setSmokingLabel (boolean isSmoking){
        isSmokingLabel = isSmoking;
    }

    public SensorReadoutStatus getSensorStatus() {
        return sensorStatus;
    }
}
