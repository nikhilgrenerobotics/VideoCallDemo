package com.example.videocalldemo.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.videocalldemo.R;
import com.example.videocalldemo.utils.AppLogger;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class VideoPlayerActivity extends AppCompatActivity {
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 101;
    private String TAG = this.getClass().getSimpleName();
    private Context mContext = this;
    private Activity mActivity = this;

    private String videoUri;
    private String s3Url = "";
    private ImageView img_back;
    private VideoView videoView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video_player);
        videoView = (VideoView) findViewById(R.id.videoView);

        if (getIntent() != null) {
            videoUri = getIntent().getStringExtra("videoUrl");
            AppLogger.getInstance().e(TAG, "videoUri: " + videoUri);

            /*MediaController mediaController = new MediaController(mContext);
            mediaController.setAnchorView(videoView);

            //Setting MediaController and URI, then starting the videoView
            videoView.setMediaController(mediaController);
            videoView.setVideoURI(Uri.parse(videoUri));
            videoView.requestFocus();
            videoView.start();

            new Handler().postDelayed(() -> mediaController.show(0), 100);*/
        }

        if (videoUri.length() > 0 && !videoUri.startsWith("http")) {
            loadVideo();
        }

    }




    private void loadVideo() {
        AppLogger.getInstance().e(TAG, "videoFilePath: " + videoUri);
        try {
            //File videoFile = new File(videoUri.replace("file://", ""));
            if (videoUri.length() > 0) {
                MediaController mediaController = new MediaController(mContext);
                mediaController.setAnchorView(videoView);

                //Setting MediaController and URI, then starting the videoView
                videoView.setMediaController(mediaController);
                videoView.setVideoURI(Uri.parse(videoUri));
                videoView.requestFocus();
                videoView.start();
                //mediaController.show(0);

                videoView.setVisibility(View.VISIBLE);
            }


        } catch (Exception e) {
            e.printStackTrace();
            AppLogger.getInstance().e(TAG, "Exception: " + e.getLocalizedMessage());
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        supportFinishAfterTransition();
    }


}
