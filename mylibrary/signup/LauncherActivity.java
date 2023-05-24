package com.irestore.pdm.signup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.irestore.pdm.Global.ShowLogs;
import com.irestore.pdm.GlobalActivities.BaseActivity;
import com.irestore.pdm.R;
import com.irestore.pdm.services.GPSTracker;

import org.checkerframework.checker.nullness.qual.NonNull;

public class LauncherActivity extends BaseActivity {
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    GPSTracker gps;
    private static final String TAG = "LauncherActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            sharedPref = getSharedPreferences(getString(
                    R.string.preference_file_key), Context.MODE_PRIVATE);
            editor = sharedPref.edit();
            gps = new GPSTracker(this);

            //To get FCM registration token
            getPushNotificationToken();

            Intent i = new Intent();
            i.setClass(LauncherActivity.this, SplashClient.class);
            startActivity(i);

    }

    private void getPushNotificationToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            ShowLogs.d(TAG, "Fetching FCM registration token failed");
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        ShowLogs.d(TAG, "Fetching FCM registration token passed=="+token);
                        editor.putString("pushNotificationToken",token);
                        editor.commit();
                    }
                });
        }
    }