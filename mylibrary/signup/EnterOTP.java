package com.irestore.pdm.signup;

import static com.irestore.pdm.Global.Global.updateAppVersion;
import static com.irestore.pdm.Global.Global.updateDeviceConfiguration;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.irestore.pdm.Global.Global;
import com.irestore.pdm.Global.ShowLogs;
import com.irestore.pdm.GlobalActivities.SyncActivity;
import com.irestore.pdm.MainActivity2;
import com.irestore.pdm.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import retrofit2.http.POST;


public class EnterOTP extends AppCompatActivity {
    Button next;
    EditText enterOTP;
    String otpValue;
    boolean isOtpExpired;
    TextView timer ,resendOtp;
    private CountDownTimer countDownTimer;
    private boolean timerHasStarted = false;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    Typeface typeface;
    private static final String TAG = "EnterOTP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_enterotp);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        enterOTP = findViewById(R.id.enterOTP);
        next = findViewById(R.id.nextBtn);
        resendOtp = findViewById(R.id.resendBtn);
        typeface = Typeface.createFromAsset(getAssets(), "AvenirLTStd-Book.otf");

        ImageView clientLogo= findViewById(R.id.client_logo);

        sharedPref = getSharedPreferences(getString(
                R.string.preference_file_key), Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        Typeface typeFace = Typeface.createFromAsset(getAssets(), "AvenirLTStd-Book.otf");

        enterOTP.setTypeface(typeFace);
        next.setTypeface(typeFace);
        resendOtp.setTypeface(typeFace);

        nextOnClick();
        resendOTPClick();

        timer = findViewById(R.id.otptimer);
        timer.setTypeface(typeFace);
        TextView notification = findViewById(R.id.OtpText);
        notification.setText(sharedPref.getString("phoneNumber",""));

        Picasso.get().load(sharedPref.getString("imageURL","")).noFade().into(clientLogo);

        notification.setTypeface(typeFace);
        long startTime = 300 * 1000;
        long interval = 1000;
        countDownTimer = new MyCountDownTimer(startTime, interval);
        if (!timerHasStarted) {
            countDownTimer.start();
            timerHasStarted = true;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
     //   fetchLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateAppVersion(EnterOTP.this,"installed");
        //editor.putBoolean("otpEntered", true);
        editor.commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
       // editor.putBoolean("otpEntered", true);
        editor.commit();

    }



    public void nextOnClick(){
        next.setOnClickListener(v -> {
            // TODO Auto-generated method stub
            String s1 = enterOTP.getText().toString();
            otpValue = sharedPref.getString("otpValue", "");
            editor.putBoolean("chooseUtility",true);
            editor.commit();

                if (s1.length() == 0 ) {
                    showOfflineDialog(getString(
                            R.string.empty_otp));

                }else if (!s1.equals(otpValue)) {
                        if(isOtpExpired)
                            showOfflineDialog(getString(
                                    R.string.otp_expire));
                        else
                            showOfflineDialog(getString(
                                    R.string.incorrect_otp));

                }
                else
                    {
                        if(isOtpExpired)
                        {
                            showOfflineDialog(getString(
                                    R.string.otp_expire));
                        }
                        else {
                            if (sharedPref.getBoolean("adminApprovalRequired", false)) {
                                if (!sharedPref.getString("subscriptionStatus", "").equals("approved")) {

                                    if (sharedPref.getString("userID", "").equals("0")) {
                                        Intent i = new Intent(EnterOTP.this, MyProfile.class);
                                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        editor.putBoolean("userProfileCreated", false);
                                        editor.commit();
                                        startActivity(i);
                                    } else {
                                        if (!sharedPref.getString("subscriptionStatus", "").equals("")) {
                                            Intent i = new Intent(EnterOTP.this, AdminApprovalScreen.class);
                                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            editor.putBoolean("userProfileCreated", true);
                                            editor.commit();
                                            startActivity(i);
                                        } else {
                                            Intent i = new Intent(EnterOTP.this, MyProfile.class);
                                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            editor.putBoolean("userProfileCreated", false);
                                            editor.commit();
                                            startActivity(i);
                                        }
                                    }
                                } else {

                                    if (!sharedPref.getBoolean("termsAccepted", false)) {
                                        Intent loginIntent = new Intent();
                                        loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        loginIntent.setClass(EnterOTP.this, TermsConditions.class);
                                        editor.putBoolean("ACCEPTANCE", sharedPref.getBoolean("termsAccepted", false));
                                        editor.putBoolean("userProfileCreated", true);
                                        editor.commit();
                                        startActivity(loginIntent);

                                    } else {
                                        String deviceString = UUID.randomUUID().toString();
                                        SharedPreferences.Editor editor = sharedPref.edit();
                                        JSONObject jsonParam = null;
                                        JSONObject deviceParam = null;
                                        TimeZone tz = TimeZone.getTimeZone("UTC");
                                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ");
                                        df.setTimeZone(tz);


                                        try {
                                            jsonParam = new JSONObject();
                                            jsonParam.put("isAccepted", true);
                                            jsonParam.put("version", EnterOTP.this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
                                            jsonParam.put("time", df.format(new Date()));
                                            if (Global.currentLocation != null) {
                                                jsonParam.put("latitude", String.valueOf(
                                                        Global.currentLocation.getLatitude()));
                                                jsonParam.put("longitude", String.valueOf(
                                                        Global.currentLocation.getLongitude()));
                                            } else {
                                                jsonParam.put("latitude", 0.0);
                                                jsonParam.put("longitude", 0.0);
                                            }


                                            Field[] fields = Build.VERSION_CODES.class.getFields();
                                            String osName = fields[Build.VERSION.SDK_INT].getName();

                                            deviceParam = new JSONObject();
                                            deviceParam.put("type", "ANDROID");
                                            deviceParam.put("os", osName);
                                            deviceParam.put("make", Build.MANUFACTURER);
                                            deviceParam.put("model", Build.MODEL);
                                            deviceParam.put("deviceString", deviceString);
                                            deviceParam.put("version", EnterOTP.this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
                                            deviceParam.put("pushNotificationToken", sharedPref.getString("pushNotificationToken", ""));

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }


                                        HashMap<String, String> params = new HashMap<>();

                                        assert deviceParam != null;
                                        params.put("device", deviceParam.toString());
                                        params.put("terms", jsonParam.toString());
                                        try {
                                            params.put("version", EnterOTP.this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        params.put("phone", sharedPref.getString("phoneNumber", ""));
                                        params.put("email", sharedPref.getString("emailAddress", ""));


                                        if (Global.isNetworkAvailable(EnterOTP.this)) {
                                            updateDeviceConfiguration(EnterOTP.this, params);
                                         /*   STAsyncHttpConnectionUpadteDevice async = new STAsyncHttpConnectionUpadteDevice(EnterOTP.this, params);
                                            async.execute(Global.terms);*/
                                        } else {

                                            showOfflineDialog(getResources().getString(R.string.internet_error));
                                        }

                                        Intent i = new Intent();
                                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        i.setClass(EnterOTP.this, MainActivity2.class);
                                        editor.putBoolean("ACCEPTANCE", sharedPref.getBoolean("termsAccepted", false));
                                        editor.putBoolean("userProfileCreated", true);
                                        editor.putBoolean("adminApprovalStatus", true);
                                        editor.putString("deviceString", deviceString);
                                        editor.putString("newDeviceString", deviceString);
                                        editor.apply();
                                        startActivity(i);
                                    }
                                }
                            } else {
                                if (!sharedPref.getString("subscriptionStatus", "").equals("approved")) {


                                    if (sharedPref.getString("userID", "").equals("0")) {
                                        Intent i = new Intent(EnterOTP.this, MyProfile.class);
                                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        editor.putBoolean("userProfileCreated", false);
                                        editor.commit();
                                        startActivity(i);
                                    } else {
                                        if (!sharedPref.getString("subscriptionStatus", "").equals("")) {
                                            Intent i = new Intent(EnterOTP.this, AdminApprovalScreen.class);
                                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            editor.putBoolean("userProfileCreated", true);
                                            editor.commit();
                                            startActivity(i);
                                        } else {
                                            Intent i = new Intent(EnterOTP.this, MyProfile.class);
                                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            editor.putBoolean("userProfileCreated", false);
                                            editor.commit();
                                            startActivity(i);
                                        }
                                    }
                                } else {
                                    Intent loginIntent = new Intent();
                                    loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    loginIntent.setClass(EnterOTP.this, TermsConditions.class);
                                    editor.putBoolean("ACCEPTANCE", sharedPref.getBoolean("termsAccepted", false));
                                    editor.putBoolean("userProfileCreated", true);
                                    editor.commit();
                                    startActivity(loginIntent);
                                }
                            }
                        }
                    }



            });
    }

    private void resendOTPClick() {
        resendOtp.setOnClickListener(v -> {
            // TODO Auto-generated method stub

            enterOTP.setText("");
            otpValue = sharedPref.getString("otpValue", "");
            if(timerHasStarted) {
                countDownTimer.cancel();
                timerHasStarted = false;
                isOtpExpired = false;
                timer.setText(R.string.zero);
            }

               try {
                           if (Global.isNetworkAvailable(EnterOTP.this)) {
                                Global.createAndStartProgressBar(EnterOTP.this);
                                sendAndRequestResponse();
                            } else {
                                showOfflineDialog(getResources().getString(R.string.internet_error));
                            }
                    } catch (Exception e) {
                        ShowLogs.e(TAG,
                                "Error in http connection!!" + e);
                    }
            });
    }

    private void showOfflineDialog(String messageToShow) {
        final Dialog dialogT = new Dialog(EnterOTP.this, R.style.Theme_Dialog);
        View viewT = LayoutInflater.from(EnterOTP.this).inflate(R.layout.submit_alert, null);
        WindowManager.LayoutParams paramsT = dialogT.getWindow().getAttributes();
        paramsT.width = WindowManager.LayoutParams.MATCH_PARENT;
        paramsT.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialogT.setContentView(viewT);

        TextView message = viewT.findViewById(R.id.message);
        TextView alert = viewT.findViewById(R.id.alert);
        Button closeBtn =  viewT.findViewById(R.id.closeBtn);
        closeBtn.setText(R.string.ok);
        message.setText(messageToShow);
        message.setTypeface(typeface,Typeface.NORMAL);
        alert.setTypeface(typeface,Typeface.BOLD);
        closeBtn.setTypeface(typeface,Typeface.BOLD);
        closeBtn.setOnClickListener(v -> dialogT.dismiss());
        dialogT.show();

    }

    public class MyCountDownTimer extends CountDownTimer {
        public MyCountDownTimer(long startTime, long interval) {
            super(startTime, interval);
        }

        @Override
        public void onFinish() {
            isOtpExpired = true;
            timer.setText(R.string.zero);
            if(!EnterOTP.this.isFinishing()) {
                showOfflineDialog(getResources().getString(R.string.otp_expire));
            }
        }
        @Override
        public void onTick(long millisUntilFinished) {
            int seconds = (int) (millisUntilFinished / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            timer.setText("" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds));
        }

    }

    private void sendAndRequestResponse() {

        //RequestQueue initialized
        RequestQueue mRequestQueue = Volley.newRequestQueue(this);

        //String Request initialized
        StringRequest mStringRequest = new StringRequest(Request.Method.GET, Global.getOTP + sharedPref.getString("phoneNumber", ""), response -> {

            JSONObject responseObject;
            String message;
            boolean error;
            ShowLogs.i("shrinika", "response===" + response);

            try {
                if (response != null) {
                    responseObject = new JSONObject(response);
                    error = responseObject.getBoolean("Error");

                    if (!error) {
                        editor.putString("otpValue", responseObject.getString("OTP"));
                        editor.putString("emailAddress", sharedPref.getString("emailAddress", ""));
                        editor.commit();
                        if (!timerHasStarted) {
                            countDownTimer.start();
                            timerHasStarted = true;
                        }
                    } else {
                        message = responseObject.get("Message").toString();
                        showOfflineDialog(message);
                    }
                } else {
                    showOfflineDialog(EnterOTP.this.getResources().getString(R.string.server_error));
                }
                if (Global.progress.isShowing()) {
                    Global.stopProgressBar();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> {

            ShowLogs.i(TAG, "Error :" + error.toString());
            if (Global.progress.isShowing()) {
                Global.stopProgressBar();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("x-account-key", sharedPref.getString("accountKey", ""));
                headers.put("x-access-token", sharedPref.getString("token", ""));
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                headers.put("x-application", "PDM");
                headers.put("x-user", sharedPref.getString("phoneNumber", ""));

                return headers;
            }

        };

        mRequestQueue.add(mStringRequest);
    }

}
