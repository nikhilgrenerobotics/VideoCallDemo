package com.example.videocalldemo.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.media.AudioManager;
import android.util.Log;



class Bluetooth {

    private final AudioManager audioManager;
    private static final String TAG=Bluetooth.class.getName();
    Bluetooth(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    /**
     * Check if there is a communication device currently connected that we could route
     * audio through.
     *
     * @return TRUE if there is a bluetooth communication device present, not necessarily if audio is being routed
     * over bluetooth otherwise FALSE.
     */
    boolean isBluetoothCommunicationDevicePresent() {
        Log.d(TAG,"hasBluetoothHeadset()");

        BluetoothAdapter bluetoothAdapter;
        try {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        } catch (Exception e) {
            Log.d(TAG,"BluetoothAdapter.getDefaultAdapter() exception: " + e.getMessage());
            return false;
        }

        if (bluetoothAdapter == null) {
            Log.d(TAG,"There is no bluetoothAdapter!?");
            return false;
        }

        int profileConnectionState;
        try {
            profileConnectionState = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET);
        } catch (Exception e) {
            Log.d(TAG,"BluetoothAdapter.getProfileConnectionState() exception: " + e.getMessage());
            profileConnectionState = BluetoothProfile.STATE_DISCONNECTED;
        }
        Log.d(TAG,"Bluetooth profile connection state: " + profileConnectionState);
        return bluetoothAdapter.isEnabled() && profileConnectionState == BluetoothProfile.STATE_CONNECTED;
    }

    boolean isOn() {
        return audioManager.isBluetoothScoOn();
    }

    void start() {
        audioManager.setBluetoothScoOn(true);
        audioManager.startBluetoothSco();
    }

    void stop() {
        Log.d(TAG,"stopBluetoothSco()");

        if (!audioManager.isBluetoothScoOn()) {
            Log.d(TAG,"==> Unable to stop Bluetooth sco since it is already disabled!");
            return;
        }

        Log.d(TAG,"==> turning Bluetooth sco off");
        audioManager.stopBluetoothSco();
        audioManager.setBluetoothScoOn(false);

        int retries = 0;
        while(audioManager.isBluetoothScoOn() && retries < 10) {
            audioManager.stopBluetoothSco();
            audioManager.setBluetoothScoOn(false);

            Log.d(TAG,"Retry of stopping bluetooth sco: " + retries);

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {}

            if (!audioManager.isBluetoothScoOn()) {
                return;
            }

            retries++;
        }
    }

}
