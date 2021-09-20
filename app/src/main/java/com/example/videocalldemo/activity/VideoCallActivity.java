package com.example.videocalldemo.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.videocalldemo.R;
import com.example.videocalldemo.utils.AppPreferences;
import com.example.videocalldemo.utils.Globals;
import com.example.videocalldemo.utils.telebu.TelebuManager;
import com.telebu.joinsdk.core.Configuration;
import com.telebu.joinsdk.core.JoinPublishOptions;
import com.telebu.joinsdk.core.JoinSubscribeOptions;
import com.telebu.joinsdk.core.MediaStreamListener;
import com.telebu.joinsdk.core.MediaStreamOptions;
import com.telebu.joinsdk.core.PublishListener;
import com.telebu.joinsdk.core.Resolution;
import com.telebu.joinsdk.core.SubscribeListener;
import com.telebu.joinsdk.core.TelebuJoin;
import com.telebu.joinsdk.core.TelebuJoinListener;
import com.telebu.joinsdk.core.TelebuParticipant;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import telebu.base.LocalStream;
import telebu.base.TelebuError;
import telebu.conference.RemoteStream;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.example.videocalldemo.utils.telebu.TelebuManager.AUDIO_COMMUNICATION;
import static com.example.videocalldemo.utils.telebu.TelebuManager.AUDIO_LOUD_SPEAKER;
import static com.example.videocalldemo.utils.telebu.TelebuManager.AUDIO_NORMAL;


public class VideoCallActivity extends AppCompatActivity implements TelebuJoinListener, AudioManager.OnAudioFocusChangeListener {

    private static final int OWT_REQUEST_CODE = 100;
    private SurfaceViewRenderer smallRenderer;
    LinearLayout container;
    TelebuJoin telebuJoin;
    EglBase eglBase;
    LocalStream mLocalStream;
    String userId;
    boolean isReleasedResources = false;
    TelebuManager  telebuManager;
    private Context mContext = this;
    private String TAG = this.getClass().getSimpleName();
    private Button recordBtn;
    private String selfStreamId;
    private AppPreferences appPreferences;
    TextView roomId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // FILL THE VIDEO CALL ACTIVITY WITH OUT STATUS BAR
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // set this flag so this activity will stay in front of the keyguard
        int flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
        getWindow().addFlags(flags);
        setContentView(R.layout.activity_call_video);
        roomId=findViewById(R.id.roomId);
        appPreferences = new AppPreferences(mContext);
        container = findViewById(R.id.container);
        smallRenderer = findViewById(R.id.small_renderer);
        recordBtn = findViewById(R.id.recordBtn);
        telebuManager= TelebuManager.getInstance(this);
        telebuManager.setAudioManager(AUDIO_COMMUNICATION,this,null);
        telebuManager.setAudioManager(AUDIO_LOUD_SPEAKER,this,null);
        roomId.setText("ROOM: "+appPreferences.getRoomId());
        /*Initialise telebu join */
        telebuJoin = TelebuJoin.getInstance(this);
        eglBase = telebuJoin.getEglBase();
        String JWTToken = getIntent().getStringExtra("token");
        userId = getIntent().getStringExtra("userId");
        /*init ConferenceClient */
        Configuration configuration = new Configuration();
        configuration.setToken(JWTToken);
        configuration.setUserId(userId);
        configuration.setUserName("Nikhil");
        configuration.setAttributes(new JSONObject());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userAgent", "Device:" + android.os.Build.DEVICE + ",MODEL:" + android.os.Build.MODEL + ",PRODUCT:" + android.os.Build.PRODUCT + ",OS:" + android.os.Build.VERSION.RELEASE);
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("id", userId);
            jsonObject1.put("name", "Nikhil");
            jsonObject.put("attributes", jsonObject1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        configuration.setAttributes(jsonObject);
        telebuJoin.init(configuration, this);

        /*Initialise SurfaceViewRenderer */
        smallRenderer.init(eglBase.getEglBaseContext(), null);
        smallRenderer.setMirror(true);
        //smallRenderer.setOnTouchListener(touchListener);
        smallRenderer.setEnableHardwareScaler(true);
        smallRenderer.setZOrderMediaOverlay(true);

        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recordBtn.getText().toString().equalsIgnoreCase("Start Recording")) {
                    Globals.showToast("Recording Started");
                    telebuJoin.startRecording();
                    recordBtn.setText("Stop Recording");
                } else {
                    telebuJoin.stopRecording();
                    recordBtn.setText("Start Recording");
                    Globals.showToast("Recording Stopped");
                }
            }
        });

    }

    @Override
    public void onStreamAdded(RemoteStream remoteStream) {
       /* Log.e(TAG,"onStreamAdded::"+remoteStream.id()+","+selfStreamId);
        if(selfStreamId==null||!selfStreamId.equals(remoteStream.id())){
            JoinSubscribeOptions joinSubscribeOptions = new JoinSubscribeOptions();
            ArrayList<String> videoCodec = new ArrayList<>();
            videoCodec.add("H264");
            joinSubscribeOptions.setVideoCodec(videoCodec);
            *//** Setting frame rate*//*
            List<Integer> supportedFrameRates =telebuJoin.getSupportedFrameRates(remoteStream);
            if(supportedFrameRates.size()>0){
                joinSubscribeOptions.setFps(supportedFrameRates.get(supportedFrameRates.size()-1));
            }

            *//** Setting resolution*//*
            List<Resolution> supportedResolutions =telebuJoin.getSupportedResolutions(remoteStream);
            if(supportedResolutions.size()>0){
                joinSubscribeOptions.setResolution(supportedResolutions.get(0));
            }

            telebuJoin.subscribe(remoteStream, joinSubscribeOptions, new SubscribeListener() {
                @Override
                public void subscribedSuccessfully(RemoteStream remoteStream, String subscriptionId) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("onStreamAdded", remoteStream.id());
                            addRemoteStreamToView(remoteStream);
                        }
                    });
                }

                @Override
                public void subscribedFailed(TelebuError telebuError) {

                }
            });
        }
*/
    }

    @Override
    public void onParticipantJoined(TelebuParticipant participant) {
        Log.e("onParticipantJoined", participant.getId() + "," + participant.getAttributes().toString());
    }

    @Override
    public void onMessageReceived(String message, String from, String to) {

    }

    @Override
    public void onServerDisconnected() {

    }

    @Override
    public void onLeft(String leftState) {

    }

    @Override
    public void joinFailed(TelebuError e) {
        Log.e("joinFailed", e.errorCode + "," + e.errorMessage);
    }

    @Override
    public void joinedSuccessfully(List<RemoteStream> remoteStreams, HashMap<String, TelebuParticipant> participants) {
       /* for (RemoteStream remoteStream : remoteStreams) {
            Log.e("joinedSuccessfully", "$$$:" + remoteStream.id());
            if (!remoteStream.id().contains("common")&&!remoteStream.id().contains("avd")) {
                JoinSubscribeOptions joinSubscribeOptions = new JoinSubscribeOptions();
                ArrayList<String> videoCodec = new ArrayList<>();
                videoCodec.add("H264");
                joinSubscribeOptions.setVideoCodec(videoCodec);
                telebuJoin.subscribe(remoteStream, joinSubscribeOptions, new SubscribeListener() {
                    @Override
                    public void subscribedSuccessfully(RemoteStream remoteStream, String subscriptionId) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e("joinedSuccessfully", remoteStream.id());
                                addRemoteStreamToView(remoteStream);
                            }
                        });
                    }

                    @Override
                    public void subscribedFailed(TelebuError telebuError) {

                    }
                });
            }
        }

        //telebuJoin.startRecording();
        requestPermission();*/
    }

    @Override
    public void onStreamEnded(RemoteStream remoteStream) {
        telebuJoin.unSubscribe(remoteStream);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                remoteStream.detach(container.findViewWithTag(remoteStream.id()));
                container.removeView(container.findViewWithTag(remoteStream.id()));
            }
        });
    }

    @Override
    public void onMute(String muteState) {

    }

    @Override
    public void onEnd(String endState) {

    }

    @Override
    public void onRecording(String s) {

    }

    /*@Override
    public void onRecording(String recordingAck) {

    }*/

    private void onConnectSucceed() {
        Log.e("onConnectSucceed", "onConnectSucceed");
        MediaStreamOptions mediaStreamOptions = new MediaStreamOptions();
        mediaStreamOptions.setHeight(720);
        mediaStreamOptions.setWidth(1280);
        mediaStreamOptions.setFps(30);
        mediaStreamOptions.setCaptureToTexture(true);
        mediaStreamOptions.setCameraFront(true);
        telebuJoin.getMediaStream(mediaStreamOptions, new MediaStreamListener() {
            @Override
            public void mediaStream(LocalStream localStream) {
                Log.e("localStream", "localStreamlocalStream");
                mLocalStream = localStream;
                localStream.attach(smallRenderer);
                JoinPublishOptions joinPublishOptions = new JoinPublishOptions();
                ArrayList<String> videoCodec = new ArrayList<>();
                joinPublishOptions.setVideoCodec(videoCodec);
                telebuJoin.publish(mLocalStream, joinPublishOptions, new PublishListener() {
                    @Override
                    public void publishSuccessfully(String publishId) {
                        selfStreamId=publishId;
                    }

                    @Override
                    public void publishFailed(TelebuError telebuError) {
                        telebuJoin.unPublish(mLocalStream);
                        onConnectSucceed();
                    }
                });
            }
        });
    }

    private void requestPermission() {
        String[] permissions = new String[]{Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE};

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(mContext,
                    permission) != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(VideoCallActivity.this,
                        permissions,
                        OWT_REQUEST_CODE);
                return;
            }
        }

        onConnectSucceed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case OWT_REQUEST_CODE: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PERMISSION_GRANTED) {
                    onConnectSucceed();

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(mContext, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    void addRemoteStreamToView(RemoteStream remoteStream) {
        SurfaceViewRenderer fullRenderer = new SurfaceViewRenderer(mContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 600);
        fullRenderer.setLayoutParams(layoutParams);
        fullRenderer.init(eglBase.getEglBaseContext(), null);
        fullRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        fullRenderer.setEnableHardwareScaler(true);
        fullRenderer.setZOrderMediaOverlay(true);
        fullRenderer.setTag(remoteStream.id());
        remoteStream.attach(fullRenderer);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                container.addView(fullRenderer);
                /*Log.e("addRemoteStreamToView",remoteStream.getAttributes().get("id")+","+userId);
                if(remoteStream.getAttributes().get("id").equals(userId)){
                    Button button=new Button(VideoCallActivity.this);
                    LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                    button.setLayoutParams(layoutParams);
                    button.setTag("btn_"+remoteStream.id());
                    button.setText("Leave");
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            isReleasedResources=true;
                            releaseResources();
                            telebuJoin.release();
                            finish();
                            *//*telebuJoin.switchCamera();*//*
                        }
                    });
                    container.addView(button);
                }*/
            }
        });
    }

    void releaseResources() {
        // eglBase.release();
        //telebuJoin.stopRecording();
        telebuJoin.unPublish(mLocalStream);
        if (mLocalStream != null) mLocalStream.detach(smallRenderer);
        smallRenderer.release();
        for (RemoteStream remoteStream : telebuJoin.getRemoteStreams()) {
            if (!remoteStream.id().contains("common")&&!remoteStream.id().contains("avd")) {
                remoteStream.detach(container.findViewWithTag(remoteStream.id()));
                if((container.findViewWithTag(remoteStream.id()))!=null)
                ((SurfaceViewRenderer) container.findViewWithTag(remoteStream.id())).release();
                telebuJoin.unSubscribe(remoteStream);
            }
        }
        telebuJoin.release();
        System.gc();
        System.runFinalization();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        telebuManager.setAudioManager(AUDIO_NORMAL,this,null);
        if (!isReleasedResources)
            releaseResources();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onAudioFocusChange(int i) {

    }


}