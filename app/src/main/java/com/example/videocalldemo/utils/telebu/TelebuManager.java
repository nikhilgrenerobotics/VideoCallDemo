package com.example.videocalldemo.utils.telebu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.Ringtone;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.PowerManager;
import android.telecom.TelecomManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;

import androidx.core.app.ActivityCompat;

import com.example.videocalldemo.bluetooth.AudioRouter;


public class TelebuManager implements SensorEventListener {

    private PowerManager mPowerManager;
    private Resources mR;
    private ConnectivityManager mConnectivityManager;
    private boolean mAudioFocused;
    private boolean isRinging;
    Ringtone currentRingtone;
    static Context context;
    static TelebuManager telebuManager;
    private SensorManager mSensorManager;
    private Sensor mProximity;
    private boolean mProximitySensingEnabled;
    private PowerManager.WakeLock mProximityWakelock;
    AudioManager audioManager;
    @SuppressLint("InvalidWakeLockTag")
    private TelebuManager(){
        audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        mR = context.getResources();
        /*Proximity Sensor Initialization*/
        mProximityWakelock = mPowerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "manager_proximity_sensor");
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
    }
    public static TelebuManager getInstance(Context mcontext){
        context=mcontext;
        if(telebuManager==null){
            telebuManager=new TelebuManager();
        }
        return telebuManager;
    }

    public boolean isRinging(){
        return isRinging;
    }

    public AudioManager getAudioManger(){
        return audioManager;
    }


    public boolean isCallActive(){
        try{
            if(ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
                TelecomManager tm = null;
                boolean isInCall=false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    tm = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
                    isInCall = tm.isInCall();
                }
                return isInCall;
            }else {
                AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
                Log.e("AudioManager",manager.getMode()+"..");
                if(manager.getMode()== AudioManager.MODE_IN_CALL){
                    return true;
                }
                else{
                    return false;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public Pair<Integer, Integer> getDisplay(Activity activity){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        return new Pair<>(width,height);
    }

    public void enableProximitySensing(boolean enable) {
        if (enable) {
            if (!mProximitySensingEnabled) {
                mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
                mProximitySensingEnabled = true;
            }
        } else {
            if (mProximitySensingEnabled) {
                mSensorManager.unregisterListener(this);
                mProximitySensingEnabled = false;
                // Don't forgeting to release wakelock if held
                if (mProximityWakelock.isHeld()) {
                    mProximityWakelock.release();
                }
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.timestamp == 0) return;
        if (isProximitySensorNearby(event)) {
            if (!mProximityWakelock.isHeld()) {
                mProximityWakelock.acquire();
            }
        } else {
            if (mProximityWakelock.isHeld()) {
                mProximityWakelock.release();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    public static Boolean isProximitySensorNearby(final SensorEvent event) {
        float threshold = 4.001f; // <= 4 cm is near

        final float distanceInCm = event.values[0];
        final float maxDistance = event.sensor.getMaximumRange();
        /*Log.d("Proximity sensor report [" + distanceInCm + "] , for max range [" + maxDistance + "]");*/
        if (maxDistance <= threshold) {
            // Case binary 0/1 and short sensors
            threshold = maxDistance;
        }
        return distanceInCm < threshold;
    }


    public void setAudioManager(String type, AudioManager.OnAudioFocusChangeListener  onAudioFocusChangeListener, AudioRouter audioRouter) {


        switch (type) {
            case AUDIO_COMMUNICATION:
                if(onAudioFocusChangeListener!=null){
                    try{
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Log.e("setAudioManager","AUDIO_COMMUNICATION");
                            audioManager.requestAudioFocus(new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                                    .setAudioAttributes(
                                            new AudioAttributes.Builder()
                                                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                                    .build()
                                    )
                                    .setAcceptsDelayedFocusGain(true)
                                    .setOnAudioFocusChangeListener(new AudioManager.OnAudioFocusChangeListener() {
                                        @Override
                                        public void onAudioFocusChange(int focusChange) {
                                            //Handle Focus Change
                                        }
                                    }).build()
                            );
                            audioManager.setStreamVolume(
                                    AudioManager.STREAM_VOICE_CALL,
                                    audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
                                    0);
                        }else {
                            audioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_VOICE_CALL,
                                    AudioManager.AUDIOFOCUS_GAIN);

                        }
                    }catch (Exception e){
                     e.printStackTrace();
                    }
                }
                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                if(audioRouter!=null){
                    if(audioRouter.isBluetoothRouteAvailable()){
                        audioRouter.routeAudioViaBluetooth();
                    }else {
                        audioRouter.routeAudioViaEarpiece();
                    }
                }
                break;
            case AUDIO_NORMAL:
                audioManager.setMicrophoneMute(false);
                audioManager.setMode(AudioManager.MODE_NORMAL);
                break;
            case AUDIO_LOUD_SPEAKER:
                if(audioRouter!=null){
                    audioRouter.routeAudioViaSpeaker();
                }
                audioManager.setSpeakerphoneOn(true);
                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                break;
            case AUDIO_NORMAL_SPEAKER:
                if(audioRouter==null){
                    audioManager.setSpeakerphoneOn(false);
                    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                }else {
                    if(audioRouter.isBluetoothRouteAvailable()){
                        audioRouter.routeAudioViaBluetooth();
                    }else {
                        audioRouter.routeAudioViaEarpiece();
                    }
                }
                break;
            case AUDIO_MUTE:
                audioManager.setMicrophoneMute(true);
                break;
            case AUDIO_UN_MUTE:
                audioManager.setMicrophoneMute(false);
                break;
        }

    }

    public void setAudioManager(String type, AudioManager.OnAudioFocusChangeListener  onAudioFocusChangeListener, AudioRouter audioRouter, boolean mutetype, String speakerstate) {
        switch (type) {
            case AUDIO_COMMUNICATION:
                if(onAudioFocusChangeListener!=null){
                    try{
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Log.e("setAudioManager","AUDIO_COMMUNICATION");
                            audioManager.requestAudioFocus(new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                                    .setAudioAttributes(
                                            new AudioAttributes.Builder()
                                                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                                    .build()
                                    )
                                    .setAcceptsDelayedFocusGain(true)
                                    .setOnAudioFocusChangeListener(new AudioManager.OnAudioFocusChangeListener() {
                                        @Override
                                        public void onAudioFocusChange(int focusChange) {
                                            //Handle Focus Change
                                        }
                                    }).build()
                            );
                            audioManager.setStreamVolume(
                                    AudioManager.STREAM_VOICE_CALL,
                                    audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
                                    0);
                        }else {
                            audioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_VOICE_CALL,
                                    AudioManager.AUDIOFOCUS_GAIN);

                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                if(audioRouter.isBluetoothRouteAvailable()){
                    audioRouter.routeAudioViaBluetooth();
                }else {
                    audioRouter.routeAudioViaEarpiece();
                }
                setAudioManager(speakerstate,onAudioFocusChangeListener,audioRouter);
                break;
        }

    }


    public static final String AUDIO_COMMUNICATION="communication";
    public static final String AUDIO_NORMAL="normal";
    public static final String AUDIO_LOUD_SPEAKER="loud_speaker";
    public static final String AUDIO_NORMAL_SPEAKER="normal_speaker";
    public static final String AUDIO_MUTE="mute";
    public static final String AUDIO_UN_MUTE="un_mute";

}
