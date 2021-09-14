package com.example.videocalldemo.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.TextView;

import com.example.videocalldemo.R;
import com.example.videocalldemo.adapter.MyListAdapter;
import com.example.videocalldemo.utils.AppLogger;
import com.example.videocalldemo.utils.Globals;
import com.example.videocalldemo.utils.WSUtilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RecordedFilesActivity extends AppCompatActivity {

    private Context mContext = this;
    private String TAG = this.getClass().getSimpleName();

    RecyclerView recordedFilesRecyclerView;
    TextView noDataFoundTextView;
    private String roomId  = "", accessKey = "",secretKey = "";
    private JSONArray recordingArray = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorded_files);

        recordedFilesRecyclerView = (RecyclerView) findViewById(R.id.recordedFilesRecyclerView);
        noDataFoundTextView = (TextView) findViewById(R.id.noDataFoundTextView);

        roomId = getIntent().getStringExtra("roomId");
        accessKey = getIntent().getStringExtra("accessKey");
        secretKey = getIntent().getStringExtra("secretKey");

        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if (roomId != null && roomId.length() > 0){
            getRoomRecordedFiles();
        }

    }

    private void getRoomRecordedFiles(){
        WSUtilities.getRoomRecordedFiles(mContext, roomId, accessKey, secretKey, new WSUtilities.WSResultCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    AppLogger.getInstance().e(TAG, "GET_ROOM_RECORDED_FILES_RESPONSE :" + result.toString());
                    JSONObject jsonObject = result.getJSONObject("response");
                    if (jsonObject.getBoolean("status")) {
                        Globals.showToast(jsonObject.getString("message"));
                        if (jsonObject.has("details")) {
                            AppLogger.getInstance().e(TAG, "details :" + jsonObject.getJSONArray("details"));
                            JSONArray detailJsonArray = jsonObject.getJSONArray("details");
                            for (int i =0; i<detailJsonArray.length();i++){
                                JSONObject detailJsonObject = detailJsonArray.getJSONObject(i);
                                recordingArray = detailJsonObject.getJSONArray("recordings");
                            }

                            if(recordingArray != null){
                                MyListAdapter adapter = new MyListAdapter(mContext,recordingArray);
                                recordedFilesRecyclerView.setHasFixedSize(true);
                                recordedFilesRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
                                recordedFilesRecyclerView.setAdapter(adapter);
                                noDataFoundTextView.setVisibility(View.GONE);
                                recordedFilesRecyclerView.setVisibility(View.VISIBLE);

                            }


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
}