package com.example.videocalldemo.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Patterns;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.example.videocalldemo.MyApplication;
import com.example.videocalldemo.R;
import com.example.videocalldemo.utils.AppLogger;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;


/**
 * Created by Raviraja KV on 10/10/2016 5:55 PM.
 */
public class Globals {
    private static String TAG = "Globals";






    /**
     * Gets volley error.
     *
     * @param error   the error
     * @param context the context
     * @return the volley error
     */
    @Nullable
    public static String getVolleyError(VolleyError error, Context context) {

        AppLogger.getInstance().e("VOLLEY_ERROR", "Volley error: " + error.getMessage());

        AppLogger.getInstance().e(TAG, "Volley Error: ", error);

        String erroMessage = null;
        if (error instanceof NetworkError) {
            erroMessage = context.getString(R.string.cannot_conn_to_internet);
        } else if (error instanceof ServerError) {
            erroMessage = context.getString(R.string.conn_timed_out);
        } else if (error instanceof AuthFailureError) {
            erroMessage = context.getString(R.string.cannot_conn_to_internet);
        } else if (error instanceof ParseError) {
            erroMessage = context.getString(R.string.parsing_error);
        } else if (error instanceof NoConnectionError) {
            if (!isSSLError(error))
                erroMessage = context.getString(R.string.cannot_conn_to_internet);
        } else if (error instanceof TimeoutError) {
            erroMessage = context.getString(R.string.conn_timed_out);
        } else {
            boolean isSSLError = isSSLError(error);
        }
        return erroMessage;
    }

    private static boolean isSSLError(VolleyError error) {
        String errorMsg = error.getLocalizedMessage();
        return errorMsg.contains("SSLPeerUnverifiedException") || errorMsg.contains("Certificate pinning failure!");
    }

    /**
     * Show toast.
     *
     * @param message the message
     */
    public static void showToast(String message) {
        if (message.length() > 0) {
            Toast.makeText(MyApplication.getInstance(), message, Toast.LENGTH_SHORT).show();
        }
    }

    public static void showToastLong(String message) {
        if (message.length() > 0) {
            Toast.makeText(MyApplication.getInstance(), message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Show toast.
     *
     * @param mContext the m context
     * @param message  the message
     */
    public static void showToast(Context mContext, String message) {
        if (message.length() > 0) {
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Gets address.
     *
     * @param mContext the m context
     * @param lat      the lat
     * @param lng      the lng
     * @return the address
     */
    public static String getAddress(Context mContext, double lat, double lng) {
        if (lat != 0 && lng != 0) {
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(lat, lng, 1);

                String cityName = "";
                try {
                    cityName = addresses.get(0).getAddressLine(0);
                    String stateName = addresses.get(0).getAddressLine(1);
                    String countryName = addresses.get(0).getAddressLine(2);
                } catch (Exception ex) {
                    AppLogger.getInstance().e(TAG, "Exception: " + ex.getLocalizedMessage());
                }
                return cityName;
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
        } else {
            return "";
        }
    }


    /**
     * Generate 6 digit random number
     *
     * @return the int
     */
    public static int genSixDigitCode() {
        Random r = new Random();
        return (100000 + r.nextInt(900000));
    }



    /**
     * Gets battery level.
     *
     * @return the battery level
     */
    public static float getBatteryLevel() {
        Intent batteryIntent = MyApplication.getAppContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if (level == -1 || scale == -1) {
            return 50.0f;
        }

        return ((float) level / (float) scale) * 100.0f;
    }


    /**
     * Gets system external menory.
     *
     * @param mContext the m context
     * @return the system external menory
     */
// Getting available free space in SD card.
    @SuppressWarnings("deprecation")
    public static String getSystemExternalMenory(Context mContext) {
        String sdcard_space = "";

        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesAvailable = (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
        long megAvailable = bytesAvailable / (1024 * 1024);
        if (megAvailable == 0) {
            sdcard_space = "NA";
        }
        // putting phone time settings in auto time zone
        Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.AUTO_TIME, 1);
        sdcard_space = String.valueOf(megAvailable);
        return sdcard_space;

    }


    /**
     * Gets system internal memory.
     *
     * @param mContext the m context
     * @return the system internal memory
     */
// Getting available free space in internal memory
    public static String getSystemInternalMemory(Context mContext) {

        String internal_space = "";

        StatFs internalmem = new StatFs(Environment.getDataDirectory().getPath());
        @SuppressWarnings("deprecation")
        long ibyteavaulable = (long) internalmem.getBlockSize() * (long) internalmem.getAvailableBlocks();
        long imemavailble = ibyteavaulable / (1024 * 1024);

        internal_space = String.valueOf(imemavailble);
        return internal_space;

    }



    /**
     * hide soft keyboard
     *
     * @param activity the activity
     */
    public static void hideSoftKeyboard(Activity activity) {
        if (activity != null && activity.getWindow() != null && activity.getWindow().getDecorView() != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
        }
    }


    /**
     * Start video recording using intent.
     *
     * @param activity    the activity
     * @param requestCode the request code
     * @param videoUri    the video uri
     */
    public static void startVideoRecordingUsingIntent(Activity activity, int requestCode, Uri videoUri) {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);// quality
        intent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);//
        // intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT,
        // 5491520L);//5*1048*1048=5MB
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);// time limit up receiver_name
        // 30 seconds
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * Gets uri of recording video.
     *
     * @return the uri of recording video
     */
    public static Uri getUriOfRecordingVideo() {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        if (path.exists()) {
            File test1 = new File(path, "100MEDIA/");
            if (test1.exists()) {
                path = test1;
            } else {
                File test2 = new File(path, "100ANDRO/");
                if (test2.exists()) {
                    path = test2;
                } else {
                    File test3 = new File(path, "Camera/");
                    if (!test3.exists()) {
                        test3.mkdirs();
                    }
                    path = test3;
                }
            }
        } else {
            path = new File(path, "Camera/");
            path.mkdirs();
        }
        return Uri.fromFile(new File(path.getPath() + File.separator + getVideoAttachmentFileName()));
    }

    private static String getVideoAttachmentFileName() {
        return "VID_" + new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date()) + ".mp4";
    }

    /**
     * Is email address valid boolean.
     *
     * @param email the email
     * @return the boolean
     */
    public static boolean isEmailAddressValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Is connected to power boolean.
     *
     * @param context the context
     * @return the boolean
     */
    public static boolean isConnectedToPower(Context context) {
        Intent intent = MyApplication.getAppContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
    }

    /**
     * Is network connected boolean.
     *
     * @param context the context
     * @return the boolean
     */
    public static boolean isNetworkConnected(Context context) {
        return isNetworkConnected();
    }

    /**
     * Is network connected boolean.
     *
     * @return the boolean
     */
    public static boolean isNetworkConnected() {
        boolean isConnected;
        ConnectivityManager cm = (ConnectivityManager) MyApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        isConnected = cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
        /*if(!isConnected){
            AlertMessageToast.showNetworkToast();
        }*/
        return isConnected;
    }


}


