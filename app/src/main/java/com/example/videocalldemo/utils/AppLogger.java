package com.example.videocalldemo.utils;

import android.util.Log;

import com.example.videocalldemo.BuildConfig;


/**
 * AppLogger is an extended custom class for defualt {@link Log} class
 * <p>This will automatically detect build variant and based on it log will be logged to console..</p>
 * Created by Raviraja KV on 10/1/2016.
 */
public class AppLogger {
    private boolean isLogEnabled = BuildConfig.DEBUG;

    /**
     * Default logging will be based on build variant
     */
    public AppLogger() {
    }

    private static AppLogger instance = new AppLogger();

    public static AppLogger getInstance() {
        return instance;
    }

    public void setLogState(boolean logState) {
        isLogEnabled = logState;
    }

    public boolean getLogState() {
        return isLogEnabled;
    }

    /**
     * DEBUG level of log
     *
     * @param a the tag
     * @param b the string for log in console
     */
    public void d(String a, String b) {
        if (isLogEnabled) Log.d(a, b);
    }

    /**
     * ERROR level of log
     *
     * @param a the tag
     * @param b the string for log in console
     */
    public void e(String a, String b) {
        if (isLogEnabled) Log.e(a, b);
    }

    public void e_debug_file(String a, String b) {
        if (isLogEnabled) Log.e(a, b);

        //new CustomExceptionHandler().writeToDebugLogFile("\n\n=========\n" + a + ":: " + b);
    }

    public void e(String a, String b, Throwable t) {
        if (isLogEnabled) Log.e(a, b, t);
    }

    public void i(String a, String b, Throwable t) {
        if (isLogEnabled) Log.e(a, b, t);
    }

    /**
     * INFO level of log
     *
     * @param a the tag
     * @param b the string for log in console
     */
    public void i(String a, String b) {
        if (isLogEnabled) Log.i(a, b);
    }

    /**
     * WARNING level of log
     *
     * @param a the tag
     * @param b the string for log in console
     */
    public void w(String a, String b) {
        if (isLogEnabled) Log.w(a, b);
    }

    /**
     * VERBOSE level of log
     *
     * @param a the tag
     * @param b the string for log in console
     */
    public void v(String a, String b) {
        if (isLogEnabled) Log.v(a, b);
    }

    public void e_long(String TAG, String message) {
        int maxLogSize = 2000;
        for (int i = 0; i <= message.length() / maxLogSize; i++) {
            int start = i * maxLogSize;
            int end = (i + 1) * maxLogSize;
            end = end > message.length() ? message.length() : end;
            e(TAG, message.substring(start, end));
        }
    }
}
