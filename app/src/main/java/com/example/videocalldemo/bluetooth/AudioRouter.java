package com.example.videocalldemo.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.util.Log;

import com.example.videocalldemo.utils.telebu.TelebuManager;


public class AudioRouter {

    private final Bluetooth bluetooth;
    private final Context context;
    private static final String TAG=AudioRouter.class.getName();
    private final AudioManager audioManager;
    private BluetoothDevice connectedBluetoothHeadset;
    private final BroadcastReceiverManager broadcastReceiverManager;
    private final AudioManager.OnAudioFocusChangeListener sipService;
    /**
     * This boolean is set to TRUE when the audio is being routed around
     * bluetooth despite bluetooth being available, this will be when the
     * user selects a different audio source. This is so we can properly handle
     * a single button input from the bluetooth headset without ending a call.
     *
     */
    private boolean bluetoothManuallyDisabled = false;

    private BluetoothHeadsetBroadcastReceiver bluetoothHeadsetReceiver = new BluetoothHeadsetBroadcastReceiver();

    public AudioRouter(Context context, AudioManager audioManager, BroadcastReceiverManager broadcastReceiverManager, AudioManager.OnAudioFocusChangeListener sipService) {
        this.context = context;
        this.sipService=sipService;
        this.audioManager = audioManager;
        this.broadcastReceiverManager = broadcastReceiverManager;
        this.bluetooth = new Bluetooth(audioManager);

        broadcastReceiverManager.unregisterReceiver(bluetoothHeadsetReceiver);
        broadcastReceiverManager.registerReceiverViaGlobalBroadcastManager(bluetoothHeadsetReceiver, BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);

        BluetoothMediaSessionService.start(context);
    }

    /**
     * Destroy the audio router, tearing down all listeners and resetting services back to their
     * defaults. Should always be called when the audio router is used.
     *
     */
    public void destroy() {
        Log.d(TAG,"Destroying the audio router");
        bluetooth.stop();
        resetAndroidAudioManager();
        broadcastReceiverManager.unregisterReceiver(bluetoothHeadsetReceiver);
        context.stopService(new Intent(context, BluetoothMediaSessionService.class));
    }

    /**
     * Route the audio through the currently connected bluetooth device.
     *
     */
    public void routeAudioViaBluetooth() {
        logAudioRouteRequest("bluetooth");

        bluetoothManuallyDisabled = false;

        if (bluetooth.isOn()) {
            Log.e(TAG,"Aborting request to route call audio via BLUETOOTH as bluetooth is currently enabled");
            return;
        }

        bluetooth.start();

        logAudioRouteHandled("bluetooth");
    }

    /**
     * Route the audio through the phone's loud speaker.
     *
     */
    public void routeAudioViaSpeaker() {
        logAudioRouteRequest("speaker");

        bluetoothManuallyDisabled = true;

        if (isCurrentlyRoutingAudioViaBluetooth()) {
            Log.d(TAG,"Stopping bluetooth routing as speaker route was requested");
            bluetooth.stop();
        }

        audioManager.setSpeakerphoneOn(true);

        logAudioRouteHandled("speaker");
    }

    /**
     * Route the audio through the phone's earpiece, this is the standard method for making a call.
     *
     */
    public void routeAudioViaEarpiece() {
        logAudioRouteRequest("earpiece");

        bluetoothManuallyDisabled = true;

        if (!hasEarpiece()) {
            Log.e(TAG,"Unable to route audio via earpiece as the current device does not have one, is this not a phone?");
            return;
        }

        if (isCurrentlyRoutingAudioViaWiredHeadset() || isCurrentlyRoutingAudioViaEarpiece()) {
            Log.e(TAG,"Already routing audio via wired headset or earpiece");
            return;
        }

        if (isCurrentlyRoutingAudioViaBluetooth()) {
            Log.d(TAG,"Stopping bluetooth routing as earpiece route was requested");
            bluetooth.stop();
        }

        audioManager.setSpeakerphoneOn(false);

        logAudioRouteHandled("earpiece");
    }

    /**
     * Helper method for logging audio requests.
     *
     * @param method
     */
    private void logAudioRouteRequest(String method) {
        Log.d(TAG,"Received request to route the call audio via " + method.toUpperCase());
    }

    /**
     * Helper method for logging audio route completion.
     *
     * @param method
     */
    private void logAudioRouteHandled(String method) {
        Log.e(TAG,"Handled request to route the call audio via " + method.toUpperCase());
    }

    /**
     * Check if we are currently routing audio through a bluetooth device.
     *
     * @return TRUE if audio is being routed over bluetooth, otherwise FALSE.
     */
    public boolean isCurrentlyRoutingAudioViaBluetooth() {
        return getCurrentRoute() == Constants.ROUTE_BT;
    }

    /**
     * Check if we are currently routing audio through the phone's speaker.
     *
     * @return TRUE if audio is being routed over the speaker, otherwise FALSE.
     */
    public boolean isCurrentlyRoutingAudioViaSpeaker() {
        return getCurrentRoute() == Constants.ROUTE_SPEAKER;
    }

    /**
     * Check if we are currently routing audio through the phone's earpiece.
     *
     * @return TRUE if audio is being routed over the earpiece, otherwise FALSE.
     */
    public boolean isCurrentlyRoutingAudioViaEarpiece() {
        return getCurrentRoute() == Constants.ROUTE_EARPIECE;
    }

    /**
     * Check if we are currently routing audio through a wired headset.
     *
     * @return TRUE if audio is being routed through a wired headset, otherwise FALSE.
     */
    public boolean isCurrentlyRoutingAudioViaWiredHeadset() {
        return getCurrentRoute() == Constants.ROUTE_HEADSET;
    }

    /**
     * Check if bluetooth if it is currently possible to route audio via bluetooth,
     * essentially determining if a bluetooth device is connected that is capable of
     * handling phone calls.
     *
     * @return TRUE if we can route via bluetooth, otherwise FALSE.
     */
    public boolean isBluetoothRouteAvailable() {
        return bluetooth.isBluetoothCommunicationDevicePresent();
    }

    /**
     * Determine where the audio is currently being routed.
     *
     * @return
     */
    private int getCurrentRoute() {
        if (bluetooth.isOn()) {
            return Constants.ROUTE_BT;
        }

        if (audioManager.isSpeakerphoneOn()) {
            return Constants.ROUTE_SPEAKER;
        }

        if (audioManager.isWiredHeadsetOn()) {
            return Constants.ROUTE_HEADSET;
        }

        if (hasEarpiece()) {
            return Constants.ROUTE_EARPIECE;
        }

        return Constants.ROUTE_INVALID;
    }

    /**
     * Return the most recently connected bluetooth device, this may return a valid object
     * even if the device is no longer connected.
     *
     * @return The last connected BluetoothDevice
     */
    public BluetoothDevice getConnectedBluetoothHeadset() {
        return connectedBluetoothHeadset;
    }

    /**
     * Check if the device actually has an earpiece, this will only be relevant on Android
     * devices that are not phones.
     *
     * @return
     */
    private boolean hasEarpiece() {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    }

    /**
     * Resets the audio manager, setting back to default and stopping listening for events.
     *
     */
    private void resetAndroidAudioManager() {
        if(audioManager == null) return;
        TelebuManager.getInstance(context).setAudioManager(TelebuManager.AUDIO_NORMAL, sipService,this);
    }

    /**
     * Make sure we have audio focus.
     *
     */
    public void focus() {
        TelebuManager.getInstance(context).setAudioManager(TelebuManager.AUDIO_COMMUNICATION, sipService,this);
    }

    /**
     * This broadcast receiver handles events from bluetooth headsets, as these are the only
     * events we receive when a single button press is detected on a headset with a single button
     * (i.e. no designated call/end call buttons) we have to make some assumptions and perform
     * a call action if one occurs.
     *
     * This is a bit of a hack but there does not currently seem to be any alternative method
     * of implementing this functionality.
     *
     */
    private class BluetoothHeadsetBroadcastReceiver extends BroadcastReceiver {

        public BluetoothHeadsetBroadcastReceiver() {

        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            int state = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, -1);
            int previousState = intent.getIntExtra(BluetoothHeadset.EXTRA_PREVIOUS_STATE, -1);

            Log.d(TAG,"Received a bluetooth headset state update. Transitioned from state: " + previousState + " to: " + state);

            if (bluetoothDevice != null) {
                connectedBluetoothHeadset = bluetoothDevice;
                Log.d(TAG,"Bluetooth headset detected with name: " + bluetoothDevice.getName() + ", address: " + bluetoothDevice.getAddress() + ", and class: " + bluetoothDevice.getBluetoothClass().toString());
            }

            if (state == BluetoothHeadset.STATE_AUDIO_DISCONNECTED && !bluetoothManuallyDisabled && isBluetoothRouteAvailable()) {
                Log.d(TAG,"This state suggests the user has pressed a button, reconnecting bluetooth and performing a single button action on the current call");
               // SipServiceCommand.bluetoothAction(context,ACTION_BLUETOOTH_ACCEPT_OR_DECLINE_INCOMING_CALL);
                routeAudioViaBluetooth();
            }
        }
    }
}
