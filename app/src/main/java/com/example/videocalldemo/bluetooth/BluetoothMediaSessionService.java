package com.example.videocalldemo.bluetooth;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;


public class BluetoothMediaSessionService extends Service {

    public static final String SHOULD_NOT_START_IN_FOREGROUND_EXTRA = "com.telebu.sip.media.SHOULD_NOT_START_IN_FOREGROUND_EXTRA";

    //private MediaSessionCompat mSession;
    private static final String TAG=BluetoothMediaSessionService.class.getName();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
       /* MediaSessionCompat session = new MediaSessionCompat(this, getClass().getSimpleName());
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();
        stateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE |
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID | PlaybackStateCompat.ACTION_PAUSE |
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
        stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, 0, 1);
        session.setPlaybackState(stateBuilder.build());
        session.setCallback(new BluetoothButtonHandler(this));
        session.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS | MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);
        session.setPlaybackToLocal(AudioManager.STREAM_VOICE_CALL);
        mSession = session;*/
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        if (shouldBecomeForegroundService(intent)) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel("ping_voip_bluetooth", "PING", NotificationManager.IMPORTANCE_MIN);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notificationChannel.setSound(null,null);
            assert manager != null;
            manager.createNotificationChannel(notificationChannel);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, notificationChannel.getId());
            startForeground(1, notificationBuilder.build());
        }

   /*     mSession.setActive(true);

        MediaButtonReceiver.handleIntent(mSession, intent);*/

        return START_NOT_STICKY;
    }

    /**
     * Determines if this service should be run as a foreground service based on the passed intent and the OS version.
     *
     * @param intent
     * @return TRUE if the service should be run in foreground
     */
    private boolean shouldBecomeForegroundService(Intent intent) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && ! intent.getBooleanExtra(SHOULD_NOT_START_IN_FOREGROUND_EXTRA, false);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy");
      /*  mSession.setActive(false);

        mSession.release();*/
    }

    /**
     * Start this service.
     *
     * @param context
     */
    public static void start(Context context) {
        try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                Intent restartServiceIntent = new Intent(context, BluetoothMediaSessionService.class);
                restartServiceIntent.putExtra(SHOULD_NOT_START_IN_FOREGROUND_EXTRA, true);
                restartServiceIntent.setPackage(context.getPackageName());
                PendingIntent restartServicePendingIntent = PendingIntent.getService(context, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
                AlarmManager alarmService = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmService != null) {
                    alarmService.set(
                            AlarmManager.ELAPSED_REALTIME,
                            SystemClock.elapsedRealtime() + 500,
                            restartServicePendingIntent);
                }
            } else {
                Intent i = new Intent(context, BluetoothMediaSessionService.class);
                i.putExtra(SHOULD_NOT_START_IN_FOREGROUND_EXTRA, true);
                context.startService(i);
            }
        } catch (Exception e) {
           e.printStackTrace();
        }
    }

   /* private final class BluetoothButtonHandler extends MediaSessionCompat.Callback {
       private Context context;
        private BluetoothKeyNormalizer bluetoothKeyNormalizer;

        private BluetoothButtonHandler(Context context) {
            this.context = context;
            this.bluetoothKeyNormalizer = BluetoothKeyNormalizer.defaultAliases();
        }

        @Override
        public boolean onMediaButtonEvent(final Intent mediaButtonEvent) {
            KeyEvent keyEvent = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

            Log.d(TAG,"Received key event:" + keyEvent.getCharacters());

            if (keyEvent.getAction() != KeyEvent.ACTION_DOWN) return true;

            Integer code = bluetoothKeyNormalizer.normalize(keyEvent.getKeyCode());

            if (code == null) {
                Log.d(TAG,"Received a key code that we don't know how to handle: " + keyEvent.getKeyCode());
                stopSelf();
                return true;
            }

            Log.d(TAG,"Key event has been normalized from " + keyEvent.getKeyCode() + " to " + code);

            String action = convertKeycodeToSipAction(code);

            if (action != null) {
                SipServiceCommand.bluetoothAction(context,action);
            }

            stopSelf();

            return true;
        }

        *//**
         * Based on the provided key code, perform an action on the sip service.
         *
         * @param code
         * @return
         *//*
        private String convertKeycodeToSipAction(int code) {
            switch (code) {
                case KEYCODE_CALL:
                    Log.d(TAG,"ACTION_BLUETOOTH_ACCEPT_INCOMING_CALL:");
                   // return SipServiceConstants.ACTION_BLUETOOTH_ACCEPT_INCOMING_CALL;
                case KEYCODE_ENDCALL:
                    Log.d(TAG,"ACTION_BLUETOOTH_DECLINE_INCOMING_CALL");
                  //  return SipServiceConstants.ACTION_BLUETOOTH_DECLINE_INCOMING_CALL;
            }
            return null;
        }
    }*/
}
