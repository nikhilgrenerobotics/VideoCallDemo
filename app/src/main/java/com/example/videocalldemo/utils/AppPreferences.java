package com.example.videocalldemo.utils;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferences {
    private SharedPreferences preference;
    private SharedPreferences.Editor editor;
    private Context context;

    public AppPreferences(Context context) {
        preference = context.getSharedPreferences("VIDEO-CALL-DEMO", Context.MODE_PRIVATE);
        editor = preference.edit();
        this.context = context;
    }



    public String getToken(){
        return preference.getString("token","");
    }

    public void setToken(String token){
        editor.putString("token", token);
        editor.commit();
    }

    public String getRoomId(){

        return preference.getString("roomId","");
    }

    public void setRoomId(String roomId){

        editor.putString("roomId",roomId);
        editor.commit();

    }


}
