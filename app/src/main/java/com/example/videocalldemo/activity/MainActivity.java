package com.example.videocalldemo.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.videocalldemo.adapter.MyListAdapter;
import com.example.videocalldemo.utils.AppPreferences;
import com.example.videocalldemo.utils.Globals;
import com.example.videocalldemo.R;
import com.example.videocalldemo.utils.AppLogger;
import com.example.videocalldemo.utils.WSUtilities;
import com.telebu.joinsdk.core.Configuration;
import com.telebu.joinsdk.core.MediaStreamListener;
import com.telebu.joinsdk.core.MediaStreamOptions;
import com.telebu.joinsdk.core.TelebuJoin;
import com.telebu.joinsdk.core.TelebuJoinListener;
import com.telebu.joinsdk.core.TelebuParticipant;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import telebu.base.LocalStream;
import telebu.base.TelebuError;
import telebu.conference.RemoteStream;

public class MainActivity extends AppCompatActivity {
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 101;
    private Context mContext = this;
    private String TAG = this.getClass().getSimpleName();
    private Button startCallButton, endCallButton, createRoomButton, joinRoomButton,recordedFilesButton;
    private TelebuJoinListener telebuJoinListener;
    private String accountId = "fd8561dfd4";
    private String endPoint = "123";
    private String accessKey = "adb139fd-58ea-47ca-80df-066938df76ee";
    private String secretKey = "aa1df5719f2037a299e1109e8fbbf4bb78e54abcbe8414f9baa1a42ccf107803";
    private AppPreferences appPreferences;
    private TextView roomIdLabelTextView,roomIdTextView;
    private EditText roomIdEditText,endPointEditText,userIdEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        startCallButton = (Button) findViewById(R.id.startCallButton);
        createRoomButton = (Button) findViewById(R.id.createRoomButton);
        joinRoomButton = (Button) findViewById(R.id.joinRoomButton);
        endCallButton = (Button) findViewById(R.id.endCallButton);
        recordedFilesButton = (Button) findViewById(R.id.recordedFilesButton);
        roomIdLabelTextView = (TextView) findViewById(R.id.roomIdLabelTextView);
        roomIdTextView = (TextView) findViewById(R.id.roomIdTextView);
        roomIdEditText = (EditText) findViewById(R.id.roomIdEditText);
        endPointEditText = (EditText) findViewById(R.id.endPointEditText);
        userIdEditText = (EditText) findViewById(R.id.userIdEditText);

        appPreferences = new AppPreferences(mContext);

        createRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Handler().postDelayed(() -> {
                    getRoom(10, true, 11, accessKey, secretKey);
                }, 50);
            }
        });

        joinRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String roomId = roomIdEditText.getText().toString();
                String endPointId = endPointEditText.getText().toString();
                accountId = userIdEditText.getText().toString();

                if (roomId.length() > 0 && endPointId.length() > 0 && accountId.length() > 0){
                    getToken(accountId,roomId,endPointId,accessKey,secretKey);
                }

            }
        });

        startCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mContext, VideoCallActivity.class);
                intent.putExtra("token",appPreferences.getToken());
                intent.putExtra("userId",accountId);
                startActivity(intent);

            }
        });

        recordedFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
                } else {
                    if (appPreferences.getRoomId().length() > 0) {
                        Intent intent = new Intent(mContext, RecordedFilesActivity.class);
                        intent.putExtra("roomId",appPreferences.getRoomId());
                        intent.putExtra("accessKey",accessKey);
                        intent.putExtra("secretKey",secretKey);
                        startActivity(intent);
                    }
                }


            }
        });

    }

    private void getRoom(int maxParticipants, boolean recording, int roomTimeOut, String accessKey, String secretKey) {
        WSUtilities.getRoom(mContext, maxParticipants, recording, roomTimeOut, accessKey, secretKey, new WSUtilities.WSResultCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    AppLogger.getInstance().e(TAG, "GET_ROOM_RESPONSE :" + result.toString());
                    JSONObject jsonObject = result.getJSONObject("response");
                    if (jsonObject.getBoolean("status")) {
                        Globals.showToast(jsonObject.getString("message"));
                        if (jsonObject.has("roomId")) {
                            appPreferences.setRoomId(jsonObject.getString("roomId"));
                            roomIdTextView.setText(jsonObject.getString("roomId"));
                            roomIdLabelTextView.setVisibility(View.VISIBLE);
                            roomIdTextView.setVisibility(View.VISIBLE);
                            getToken(accountId, appPreferences.getRoomId(), endPoint, accessKey, secretKey);
                        }
                    }
                } catch (Exception e) {
                    AppLogger.getInstance().e(TAG, "EXCEPTION :" + e.getLocalizedMessage());
                }
            }

            @Override
            public void onFailure(JSONObject error) {
                try {
                    Globals.showToast(mContext, error.getString("error_message"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void getToken(String accountId, String roomId, String endPoint, String accessKey, String secretKey) {
        WSUtilities.getToken(this, accountId, roomId, endPoint, accessKey, secretKey, new WSUtilities.WSResultCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    AppLogger.getInstance().e(TAG, "GET_TOKEN_RESPONSE :" + result.toString());
                    JSONObject jsonObject = result.getJSONObject("response");
                    if (jsonObject.getBoolean("status")) {
                        Globals.showToast(jsonObject.getString("message"));
                        if (jsonObject.has("token")) {
                            AppLogger.getInstance().e(TAG, "TOKEN :" + jsonObject.getString("token"));
                            appPreferences.setToken(jsonObject.getString("token"));
                            Intent intent = new Intent(mContext, VideoCallActivity.class);
                            intent.putExtra("token",appPreferences.getToken());
                            intent.putExtra("userId",accountId);
                            startActivity(intent);

                        }
                    }
                } catch (Exception e) {
                    AppLogger.getInstance().e(TAG, "EXCEPTION :" + e.getLocalizedMessage());
                }
            }

            @Override
            public void onFailure(JSONObject error) {
                try {
                    Globals.showToast(mContext, error.getString("error_message"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[]
            permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case STORAGE_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // check whether storage permission granted or not.
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // do what you want;
                        if (appPreferences.getRoomId().length() > 0) {
                            Intent intent = new Intent(mContext, RecordedFilesActivity.class);
                            intent.putExtra("roomId",appPreferences.getRoomId());
                            intent.putExtra("accessKey",accessKey);
                            intent.putExtra("secretKey",secretKey);
                            startActivity(intent);
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

}