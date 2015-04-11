package com.android.phoneassistant.sersor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;

import com.android.phoneassistant.util.Log;

public class FlipManager implements SensorEventListener {

    public static FlipManager sFlipManager;

    private Context mContext;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private AudioManager mAudioManager;
    private boolean shouldFlipMute = false;
    public static FlipManager getInstance(Context context) {
        if (sFlipManager == null) {
            sFlipManager = new FlipManager(context);
        }
        return sFlipManager;
    }

    private FlipManager(Context context) {
        mContext = context;
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }

    public void registerAccelerometerListener() {
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        shouldFlipMute = false;
        if (mSensor != null) {
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void unregisterAccelerometerListener() {
        if (mSensor != null) {
            mSensorManager.unregisterListener(this, mSensor);
            mSensor = null;
        }
    }

    private void unregisterListenerInternal() {
        if (mSensor != null) {
            mSensorManager.unregisterListener(this, mSensor);
            mSensor = null;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event == null) {
            return ;
        }
        if (event.sensor == null) {
            return ;
        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (event.values == null) {
                return ;
            }
            int len = event.values.length;
            if (len != 3) {
                return ;
            }
            
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            if (!shouldFlipMute && z > 9) {
                shouldFlipMute = true;
            }
            if (shouldFlipMute && z < -9) {
                Log.getLog(mContext).recordOperation("Flip mute make a call silent");
                unregisterListenerInternal();

                int ringerMode = mAudioManager.getRingerMode();
                if (mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                    mAudioManager.setRingerMode(ringerMode);
                }
                shouldFlipMute = false;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(Log.TAG, "sensor = " + sensor + " , accuracy = " + accuracy);
    }
}
