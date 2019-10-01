package xyz.lazycoders.dontshakeme;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.Toast;


import java.util.Locale;

public class MySensorService extends Service implements SensorEventListener {

    float xAccel,yAccel,zAccel;
    float xPreviousAccel,yPreviousAccel,zPreviousAccel;

    boolean firstUpdate = true;
    boolean shakeInitiated = false;
    float shakeThreshold = 8.5f;

    boolean playing = false;
    int counts;

    Sensor accelerometer;
    SensorManager sm;
    TextToSpeech t1;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_NORMAL);

        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    t1.setLanguage(Locale.UK);
                    t1.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                            Toast.makeText(MySensorService.this, "Start", Toast.LENGTH_SHORT).show();
                            playing = true;
                        }

                        @Override
                        public void onDone(String utteranceId) {
                            Toast.makeText(MySensorService.this, "Finished", Toast.LENGTH_SHORT).show();
                            playing = false;
                        }

                        @Override
                        public void onError(String utteranceId) {
                            Toast.makeText(MySensorService.this, "Error", Toast.LENGTH_SHORT).show();
                            playing = false;
                        }
                    });
                }
            }
        });



    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        t1.stop();
        t1.shutdown();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        updateAccelParameters(event.values[0],event.values[1],event.values[2]);

        if((!shakeInitiated) && isAccelerationChanged()){
            shakeInitiated = true;
        }
        else if ((shakeInitiated) && isAccelerationChanged()){
            executeShakeAction();
        }
        else if ((shakeInitiated) && !isAccelerationChanged()){
            shakeInitiated = false;
        }
    }

    private void executeShakeAction() {
        counts++;
//        Toast.makeText(this, "Shake Counts : "+counts, Toast.LENGTH_SHORT).show();
        String toSpeak="Do not shake me. You have shaken me "+counts+" times";
        if(!playing){
            t1.speak(toSpeak,TextToSpeech.QUEUE_FLUSH,null);
        }
    }

    private boolean isAccelerationChanged() {
        // Detect if Acceleration values are Changed
        float deltaX = Math.abs(xPreviousAccel - xAccel);
        float deltaY = Math.abs(yPreviousAccel - yAccel);
        float deltaZ = Math.abs(zPreviousAccel - zAccel);

        return (deltaX > shakeThreshold && deltaY > shakeThreshold)
                || (deltaX > shakeThreshold && deltaZ > shakeThreshold)
                || (deltaY > shakeThreshold && deltaZ > shakeThreshold);
    }


    private void updateAccelParameters(float xNewAccel, float yNewAccel, float zNewAccel) {
        if(firstUpdate){
            xPreviousAccel = xNewAccel;
            yPreviousAccel = yNewAccel;
            zPreviousAccel = zNewAccel;
            firstUpdate = false;
        }
        else{
            xPreviousAccel = xAccel;
            yPreviousAccel = yAccel;
            zPreviousAccel = zAccel;
        }
        xAccel = xNewAccel;
        yAccel = yNewAccel;
        zAccel = zNewAccel;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
