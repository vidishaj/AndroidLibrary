package com.irestore.pdm.signup;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.irestore.pdm.MainActivity2;
import com.irestore.pdm.R;
import com.squareup.picasso.Picasso;


public class SplashClient extends AppCompatActivity {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 3000;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    String otpValue="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_client);
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);

        sharedPref = getSharedPreferences(getString(
                R.string.preference_file_key), Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        otpValue = sharedPref.getString("otpValue", "");
        LinearLayout logoHolder = findViewById(R.id.logoHolder);
        ImageView clientLogo= findViewById(R.id.client_logo);
        logoHolder.setVisibility(View.GONE);
        if (sharedPref.getBoolean("chooseUtility", false)) {
            logoHolder.setVisibility(View.VISIBLE);
            Picasso.get().load(sharedPref.getString("imageURL", "")).into(clientLogo);
        }


        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {

                if (sharedPref.getString("emailAddress", "").isEmpty()) {
                    Intent i = new Intent(SplashClient.this, LoginActivity.class);
                    startActivity(i);
                    finish();
                } else {
                    if (!sharedPref.getBoolean("chooseUtility", false)) {
                        if (!otpValue.isEmpty()) {
                            Intent intent = new Intent();
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setClass(SplashClient.this, EnterOTP.class);
                            startActivity(intent);
                        } else {
                            Intent i = new Intent();
                            i.setClass(SplashClient.this, LoginActivity.class);
                            startActivity(i);
                        }
                    } else {
                        if (sharedPref.getString("firstName", "").isEmpty()) {
                            Intent intent = new Intent();
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setClass(SplashClient.this, MyProfile.class);
                            startActivity(intent);
                        } else {
                            if (!sharedPref.getBoolean("adminApprovalStatus", false)) {
                                Intent intent = new Intent();
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.setClass(SplashClient.this, AdminApprovalScreen.class);
                                startActivity(intent);

                            } else {
                                if (sharedPref.getBoolean("termsAccepted", false)) {
                                    Intent i = new Intent();
                                    i.setClass(SplashClient.this, MainActivity2.class);
                                    startActivity(i);
                                } else {
                                    Intent i = new Intent();
                                    i.setClass(SplashClient.this, TermsConditions.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(i);
                                }
                            }
                        }
                    }
                }
                finish();
            }
        }, SPLASH_TIME_OUT);

    }



}