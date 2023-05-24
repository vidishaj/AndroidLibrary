package com.example.mylibrary;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ToasterMessage {

    public static void s(Context c, String message){

        Toast.makeText(c,message, Toast.LENGTH_SHORT).show();

    }

    public static void launchActivity(Context c, Class c1){

        Intent profileIntent = new Intent();
        profileIntent.setClass(c, c1);
        c.startActivity(profileIntent);


    }
}