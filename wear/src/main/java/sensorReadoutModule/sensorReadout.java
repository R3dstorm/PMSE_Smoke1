package sensorReadoutModule;

import java.util.List;

import android.content.Context;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;

public class sensorReadout implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor AccSensor, GyroSensor, MagneticSensor;
    private SensorEventListener sensorListenerAcc, sensorListenerGyro, sensorListenerMagn;
    private float ACCX, ACCY, ACCZ, GYRX, GYRY, GYRZ, MAGX, MAGY, MAGZ;

    Context mContext;

    public sensorReadout (Context mContext)
    {
        /* Construct this class... */
        this.mContext = mContext;
    }

    public void initSensors()
    {
        /* TODO unregister from sensors after finishing useing them.*/
        AccSensor = GyroSensor = MagneticSensor = null;
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

        /* delete this:*/
        List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        List<Sensor> AccSensorList = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        List<Sensor> GyroSensorList = mSensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
        List<Sensor> MagneticSensorList = mSensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
        /* \delete this*/

        AccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (AccSensor == null)
        {
            // No Sensor found on the device
        }

        GyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (GyroSensor == null)
        {
            // No Sensor found on the device
        }

        MagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (MagneticSensor == null)
        {
            // No Sensor found on the device
        }

        /* Init Event-Listeners:*/

        sensorListenerAcc = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                ACCX = event.values[0];
                ACCY = event.values[1];
                ACCZ = event.values[2];
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
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };


        mSensorManager.registerListener(sensorListenerAcc, AccSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(sensorListenerGyro, GyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(sensorListenerMagn, MagneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void getValues ()
    {
        /* Format for captured data: ACCX ACCY ACCZ GYRX GYRY GYRZ MAGX MAGY MAGZ */
        /* Required sensors: Accelerometer; Gyroscope; Magnetic Field Sensor */
        /* Do something to get values... */


    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        // The light sensor returns a single value.
        // Many sensors return 3 values, one for each axis.
        float lux = event.values[0];
        // Do something with this sensor value.
    }

}
