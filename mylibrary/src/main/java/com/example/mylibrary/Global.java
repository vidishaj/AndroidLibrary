package com.example.mylibrary;

import static com.example.mylibrary.ToasterMessage.v1EndPoint;
import static com.example.mylibrary.ToasterMessage.v2EndPoint;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Global {

    public static String signUpURL = v1EndPoint + "/v1/signup/checkUser";

    public static String getOTP = v1EndPoint + "/v1/common/otp/?phone=";
    public static String createProfile = v1EndPoint + "/v1/common/users";//post
    public static String updateProfile = v1EndPoint + "/v1/common/users/profile";//put

    public static String getUserEmailByPhone= v2EndPoint + "/api/users/getUserEmailByPhoneNumber?phoneNumber=";


    public static Location currentLocation;
    public static String addressString="";
    public static String useraddressString;
    public static String street="";
    public static String streetNumber="";
    public static String city="";
    public static String state="";
    public static String stateAbbr="";
    public static String country="";
    public static String countryAbbr="";
    public static String zipCode="";
    public static ProgressDialog progress;

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void createAndStartProgressBar(final Context context)
    {
        progress = new ProgressDialog(context, R.style.ProgressDialogTheme);
        progress.setCancelable(true);//to stop the progress dialog from being cancelled
        progress.setCanceledOnTouchOutside(false);// when the user touches the screen when running
        progress.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                ((Activity)context).finish();
            }
        });
        startProgressBar();
    }
    public static void startProgressBar()
    {
        if (!progress.isShowing())
            progress.show();
    }
    public static void stopProgressBar()
    {
        if(progress.isShowing())
            progress.dismiss();
    }

}
