package com.example.videocalldemo.utils;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.videocalldemo.MyApplication;
import com.example.videocalldemo.R;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class WSUtilities {

    private static String TAG = "WSUtilities";


    public static void getToken(final Context mContext, final String accountId, final String roomId, final String endPointId, final String accessKey, final String secretKey , final WSResultCallback callback) {
        final String TAG = "GET_TOKEN";
        final JSONObject result = new JSONObject();

        if (!Globals.isNetworkConnected(mContext)) {
            try {
                result.put("error_message", MyApplication.getAppContext().getResources().getString(R.string.no_internet));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            callback.onFailure(result);
            return;
        }

        AppLogger.getInstance().e_debug_file(TAG, "URL: " + new AppUrls().URL);
        /*StringRequest getToken = new StringRequest(Request.Method.POST, new AppUrls().URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                AppLogger.getInstance().e_debug_file(TAG, " Response: " + response);
                if (response != null && !response.isEmpty()) {
                    try {
                        JSONObject responseObj = new JSONObject(response);
                        result.put("response", responseObj);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Globals.showToast("Unable receiver_name process, please check your data connectivity and try again.");
                }

                callback.onSuccess(result);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMessage = Globals.getVolleyError(error, mContext);
                AppLogger.getInstance().e_debug_file(TAG, "Error in Volley: " + errorMessage);
                try {
                    result.put("error_message", errorMessage);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                callback.onFailure(result);
            }
        }) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.LinkedHashMap<String, String> params = new java.util.LinkedHashMap<>();
                //params.put("accountId ", accountId);
                params.put("roomId", String.valueOf(roomId));
                params.put("endPointId", String.valueOf(endPointId));
                AppLogger.getInstance().e_debug_file(TAG, " Params: " + params.toString());

                return params;
            }
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                //headers.put("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
                headers.put("x-access-key", accessKey);
                headers.put("x-access-secret", secretKey);
                //headers.put("Content-Type", "application/json");
                AppLogger.getInstance().e_debug_file(TAG, "HEADERS: " + headers.toString());
                return headers;
            }
        };*/

        JSONObject reqJson = new JSONObject();
        try {
            reqJson.put("roomId",roomId);
            reqJson.put("endPointId",endPointId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest getToken = new JsonObjectRequest(Request.Method.POST, new AppUrls().URL, reqJson, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                AppLogger.getInstance().e_debug_file(TAG, " Response: " + response);
                if (response != null) {
                    try {
                        //JSONObject responseObj = new JSONObject(response);
                        result.put("response", response);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Globals.showToast("Unable receiver_name process, please check your data connectivity and try again.");
                }

                callback.onSuccess(result);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMessage = Globals.getVolleyError(error, mContext);
                AppLogger.getInstance().e_debug_file(TAG, "Error in Volley: " + errorMessage);
                try {
                    result.put("error_message", errorMessage);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                callback.onFailure(result);
            }
        }) {
            /*@Override
            protected java.util.Map<String, String> getParams() {
                java.util.LinkedHashMap<String, String> params = new java.util.LinkedHashMap<>();
                //params.put("accountId ", accountId);
                params.put("roomId", String.valueOf(roomId));
                params.put("endPointId", String.valueOf(endPointId));
                AppLogger.getInstance().e_debug_file(TAG, " Params: " + params.toString());

                return params;
            }*/
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                //headers.put("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
                headers.put("x-access-key", accessKey);
                headers.put("x-access-secret", secretKey);
                //headers.put("Content-Type", "application/json");
                AppLogger.getInstance().e_debug_file(TAG, "HEADERS: " + headers.toString());
                return headers;
            }
        };




        MyApplication.getInstance().addToRequestQueue(getToken, "GET_TOKEN");

    }

    public static void getRoom(final Context mContext, final int maxParticipants, final boolean recording, final int roomTimeOut, final String accessKey, final String secretKey , final WSResultCallback callback) {
        final String TAG = "GET_ROOM_ID";
        final JSONObject result = new JSONObject();

        if (!Globals.isNetworkConnected(mContext)) {
            try {
                result.put("error_message", MyApplication.getAppContext().getResources().getString(R.string.no_internet));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            callback.onFailure(result);
            return;
        }

        AppLogger.getInstance().e_debug_file(TAG, "URL: " + new AppUrls().ROOM_URL);
        StringRequest getRoom = new StringRequest(Request.Method.POST, new AppUrls().ROOM_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                AppLogger.getInstance().e_debug_file(TAG, " Response: " + response);
                if (response != null && !response.isEmpty()) {
                    try {
                        JSONObject responseObj = new JSONObject(response);
                        result.put("response", responseObj);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Globals.showToast("Unable receiver_name process, please check your data connectivity and try again.");
                }

                callback.onSuccess(result);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMessage = Globals.getVolleyError(error, mContext);
                AppLogger.getInstance().e_debug_file(TAG, "Error in Volley: " + errorMessage);
                try {
                    result.put("error_message", errorMessage);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                callback.onFailure(result);
            }
        }) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.LinkedHashMap<String, String> params = new java.util.LinkedHashMap<>();
                params.put("maxParticipants", String.valueOf(maxParticipants));
                params.put("recording", Boolean.toString(recording));
                params.put("roomTimeOut", String.valueOf(roomTimeOut));
                AppLogger.getInstance().e_debug_file(TAG, " Params: " + params.toString());

                return params;
            }
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                //headers.put("Content-Type", "application/json");
                headers.put("x-access-key", accessKey);
                headers.put("x-access-secret", secretKey);
                AppLogger.getInstance().e_debug_file(TAG, "HEADERS: " + headers.toString());
                return headers;
            }
        };

        MyApplication.getInstance().addToRequestQueue(getRoom, TAG);

    }

    public static void getRoomRecordedFiles(final Context mContext, String roomId, final String accessKey, final String secretKey , final WSResultCallback callback) {
        final String TAG = "GET_RECORDED_ROOM_FILES";
        final JSONObject result = new JSONObject();

        if (!Globals.isNetworkConnected(mContext)) {
            try {
                result.put("error_message", MyApplication.getAppContext().getResources().getString(R.string.no_internet));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            callback.onFailure(result);
            return;
        }

        String url = new AppUrls().RECORDED_FILES_URL +roomId;
        AppLogger.getInstance().e_debug_file(TAG, "URL: " + url);

        JsonObjectRequest getRecordedFilesReq = new JsonObjectRequest(Request.Method.GET,url,
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                AppLogger.getInstance().e_debug_file(TAG, " Response: " + response);
                if (response != null) {
                    try {
                        result.put("response", response);
                    } catch (JSONException e) {
                        AppLogger.getInstance().e(TAG,"onResponseEXC: "+e.getLocalizedMessage());
                    }
                } else {
                    Globals.showToast("Unable receiver_name process, please check your data connectivity and try again.");
                }

                callback.onSuccess(result);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMessage = Globals.getVolleyError(error, mContext);
                AppLogger.getInstance().e_debug_file(TAG, "Error in Volley: " + errorMessage);
                try {
                    result.put("error_message", errorMessage);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                callback.onFailure(result);
            }
        }) {

            /**
             * Passing some request headers
             */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                //headers.put("Content-Type", "application/json");
                headers.put("x-access-key", accessKey);
                headers.put("x-access-secret", secretKey);
                AppLogger.getInstance().e_debug_file(TAG, "HEADERS: " + headers.toString());
                return headers;
            }
        };



        MyApplication.getInstance().addToRequestQueue(getRecordedFilesReq, TAG);

    }






    /**
     * Interface for result call back...
     */
    public interface WSResultCallback {
        void onSuccess(JSONObject result);

        void onFailure(JSONObject error);
    }
}
