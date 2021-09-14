package com.example.videocalldemo.adapter;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videocalldemo.R;
import com.example.videocalldemo.activity.VideoPlayerActivity;
import com.example.videocalldemo.utils.AppLogger;
import com.example.videocalldemo.utils.Globals;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;

import static org.webrtc.ContextUtils.getApplicationContext;

public class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder> {
    private JSONArray listdata;
    private Context mContext;
    private ProgressDialog pDialog;
    private int STORAGE_PERMISSION_REQUEST_CODE = 101;

    // RecyclerView recyclerView;
    public MyListAdapter(Context mContext,JSONArray listdata) {
        this.listdata = listdata;
        this.mContext = mContext;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.list_recorded_files, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        try {
            final JSONObject myListData = listdata.getJSONObject(position);
            if (myListData.has("fileName")) {
                holder.fileNameTextView.setText(myListData.optString("fileName"));
            }
            if (myListData.has("path")) {
                holder.filePathTextView.setText(myListData.optString("path"));
            }
            holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (myListData.has("path")) {
                        /*Intent intent = new Intent(mContext, VideoPlayerActivity.class);
                        intent.putExtra("videoUrl", myListData.optString("path"));
                        mContext.startActivity(intent);*/
                        /*String destinationDirectory = Environment.getExternalStorageDirectory() +File.separator+ "RecordedFiles" ;
                        File dir = new File(destinationDirectory);
                        if (!dir.exists()) {
                            dir.mkdir();
                            String filePath = destinationDirectory+File.separator+ "recording.mkv";
                            File outputFile = new File(filePath);

                            new Handler().postDelayed(() -> {
                                downloadFile(mContext,myListData,outputFile);
                            },50);
                        } else {
                            String filePath = destinationDirectory+File.separator+ "recording.mkv";
                            File outputFile = new File(filePath);

                            new Handler().postDelayed(() -> {
                                downloadFile(mContext,myListData,outputFile);
                            },50);
                        }*/
                            new DownloadFileFromURL().execute(myListData.optString("path"));



                    } else {
                        Globals.showToast("No Video Found");
                    }
                }
            });
        } catch (Exception e){
            AppLogger.getInstance().e("onBindVIewHolder","EXCEPTION: " + e.getLocalizedMessage());
        }
    }


    @Override
    public int getItemCount() {
        return listdata.length();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView fileNameTextView;
        public TextView filePathTextView;
        public RelativeLayout relativeLayout;
        public ViewHolder(View itemView) {
            super(itemView);
            this.fileNameTextView = (TextView) itemView.findViewById(R.id.fileNameTextView);
            this.filePathTextView = (TextView) itemView.findViewById(R.id.filePathTextView);
            relativeLayout = (RelativeLayout)itemView.findViewById(R.id.relativeLayout);
        }
    }

    private static void downloadFile(Context mContext,JSONObject myListData, File outputFile) {
        try {
            String url = myListData.optString("path");
            URL u = new URL(url);
            URLConnection conn = u.openConnection();
            int contentLength = conn.getContentLength();

            DataInputStream stream = new DataInputStream(u.openStream());

            byte[] buffer = new byte[contentLength];
            stream.readFully(buffer);
            stream.close();

            DataOutputStream fos = new DataOutputStream(new FileOutputStream(outputFile));
            fos.write(buffer);
            fos.flush();
            fos.close();


        } catch(FileNotFoundException e) {
            return; // swallow a 404
        } catch (IOException e) {
            return; // swallow a 404
        }
    }

    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            System.out.println("Starting download");

            pDialog = new ProgressDialog(mContext);
            pDialog.setMessage("Loading... Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                String root = Environment.getExternalStorageDirectory().toString();

                System.out.println("Downloading");
                URL url = new URL(f_url[0]);

                URLConnection conection = url.openConnection();
                conection.connect();
                // getting file length
                int lenghtOfFile = conection.getContentLength();

                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                // Output stream to write file

                OutputStream output = new FileOutputStream(root+"/recording.mkv");
                byte data[] = new byte[1024];

                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;

                    // writing data to file
                    output.write(data, 0, count);

                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }



        /**
         * After completing background task
         * **/
        @Override
        protected void onPostExecute(String file_url) {
            System.out.println("Downloaded");

            pDialog.dismiss();
            //File outputFile = new File(file_url);
            //Uri fileUri = FileProvider.getUriForFile(mContext, "com.example.videocalldemo.fileprovider", outputFile);
            String fileUrl = Environment.getExternalStorageDirectory().toString() + "/recording.mkv";
            Uri fileUri = Uri.parse(fileUrl);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, "video/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            mContext.startActivity(intent);
        }

    }

}
