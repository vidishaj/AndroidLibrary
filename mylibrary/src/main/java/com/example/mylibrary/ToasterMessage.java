package com.example.mylibrary;

import static com.example.mylibrary.Global.APIKey;
import static com.example.mylibrary.Global.v2EndPoint;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

public class ToasterMessage {

    public static SharedPreferences sharedPreferences;
    public static SharedPreferences.Editor editor;

    public static void s(Context c, String message){

        Toast.makeText(c,message, Toast.LENGTH_SHORT).show();

    }

    public static void init(Context c){

        Intent profileIntent = new Intent();
        profileIntent.setClass(c, MainActivity.class);
        c.startActivity(profileIntent);
    }

    public static void initData(Context c ,String tenant,String v1Url,String v2Url,String bucketPoolIdNew,String bucketPoolIdOld,String sharedPreference,String LoginStatus, Class landingClass,String googleKey){

        sharedPreferences = c.getSharedPreferences(sharedPreference, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        Global.v1EndPoint =v1Url;
        v2EndPoint = v2Url;
        APIKey = googleKey;
        Intent profileIntent = new Intent();
        profileIntent.setClass(c, MainActivity.class);
        c.startActivity(profileIntent);

    }
}