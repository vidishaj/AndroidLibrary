package com.irestore.pdm.signup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.irestore.pdm.Global.Global;
import com.irestore.pdm.GlobalActivities.BaseActivity;
import com.irestore.pdm.MainActivity2;
import com.irestore.pdm.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import cz.msebera.android.httpclient.entity.StringEntity;



public class TermsConditions extends BaseActivity implements View.OnClickListener{

    Button agreeBtn,denyBtn;

    Boolean acceptance = false;

    SharedPreferences sharedPref;
    String deviceString;
    Exception exception;
    String httpPostData;
    SharedPreferences.Editor editor;
    private static final char PARAMETER_DELIMITER = '&';
    private static final char PARAMETER_EQUALS_CHAR = '=';
    private static final int CONNECTION_TIMEOUT = 3000;
    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_conditions);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Typeface typeFace = Typeface.createFromAsset(getAssets(), "AvenirLTStd-Book.otf");

        sharedPref = getSharedPreferences(getString(
                R.string.preference_file_key), Context.MODE_PRIVATE);

        String termsConditions = sharedPref.getString("termsUtility", "");

        deviceString = UUID.randomUUID().toString();

        editor = sharedPref.edit();


        agreeBtn = findViewById(R.id.agreeBtn);
        denyBtn = findViewById(R.id.denyBtn);

        agreeBtn.setOnClickListener(this);
        denyBtn.setOnClickListener(this);


        LayoutInflater inflator = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.custom_titlebar_new, null);
        v.setBackgroundColor(Color.WHITE);
        ActionBar actionBar = getSupportActionBar();
        TextView title = v.findViewById(R.id.title);
        Button nextBtn = v.findViewById(R.id.nextBtn);
        nextBtn.setVisibility(View.INVISIBLE);
        title.setText(R.string.terms_title);
        title.setTypeface(typeFace);

        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled (false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setCustomView(v);


       TextView tv= findViewById(R.id.conditionsText);
       tv.setText(Html.fromHtml(getString(R.string.tc_html)+termsConditions));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.agreeBtn:
                acceptance = true;

                JSONObject jsonParam =null;
                JSONObject deviceParam =null;
                TimeZone tz = TimeZone.getTimeZone("UTC");
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ");
                df.setTimeZone(tz);
               // loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);


                try
                {
                    jsonParam = new JSONObject();
                    jsonParam.put("isAccepted", acceptance);
                    jsonParam.put("version", this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
                    jsonParam.put("time", df.format(new Date()));
                    if(Global.currentLocation!=null) {
                        jsonParam.put("latitude", String.valueOf(
                                Global.currentLocation.getLatitude()));
                        jsonParam.put("longitude", String.valueOf(
                                Global.currentLocation.getLongitude()));
                    }else
                    {
                        jsonParam.put("latitude", 0.0);
                        jsonParam.put("longitude", 0.0);
                    }


                    Field[] fields = Build.VERSION_CODES.class.getFields();
                    String osName = fields[Build.VERSION.SDK_INT ].getName();

                    deviceParam = new JSONObject();
                    deviceParam.put("type", "ANDROID");
                    deviceParam.put("os", osName);
                    deviceParam.put("make", Build.MANUFACTURER);
                    deviceParam.put("model", Build.MODEL);
                    deviceParam.put("deviceString", deviceString);
                    deviceParam.put("version", this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
                    deviceParam.put("pushNotificationToken", sharedPref.getString("pushNotificationToken",""));





                }catch(Exception e)
                {
                    e.printStackTrace();
                }


                HashMap<String, String> params = new HashMap<String, String>();
                params.put("email",sharedPref.getString("emailAddress",""));
                params.put("device",deviceParam.toString());
                params.put("terms",jsonParam.toString());
                try {
                    params.put("version", this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
                }catch(Exception e)
                {

                }
                params.put("phone",sharedPref.getString("phoneNumber",""));
                try {
                    StringEntity entity = new StringEntity(params.toString());

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }


                if (Global.isNetworkAvailable(TermsConditions.this)) {
                    Global.createAndStartProgressBar(TermsConditions.this);
                   /* httpPostData = createQueryStringForParameters(params);
                    STAsyncHttpConnection async = new STAsyncHttpConnection();*/
                    updateDeviceConfiguration(TermsConditions.this,params);
                   // async.execute(Global.terms);
                }else
                {
                    Toast.makeText(TermsConditions.this, getResources().getString(R.string.internet_error),
                            Toast.LENGTH_SHORT).show();
                }

                editor.putString("deviceString",deviceString);
                editor.putString("newDeviceString",deviceString);
                editor.commit();


                editor.commit();

                break;
            case R.id.denyBtn:
                finishAffinity();
                //sharedPref.edit().clear().commit();
                sharedPref.edit().clear();
                editor.putString("pushNotificationToken",sharedPref.getString("pushNotificationToken",""));
                editor.commit();
                sharedPref.edit().commit();
                Intent i = new Intent();
                i.setClass(TermsConditions.this, LoginActivity.class);
                startActivity(i);

                break;

        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
       // sharedPref.edit().clear().commit();
        sharedPref.edit().clear();
        editor.putString("pushNotificationToken",sharedPref.getString("pushNotificationToken",""));
        editor.commit();
        sharedPref.edit().commit();
        Intent i = new Intent(TermsConditions.this, LoginActivity.class);
        startActivity(i);
    }
    @Override
    protected void onResume() {
        super.onResume();

    }


    public static String createQueryStringForParameters(HashMap<String, String> parameters) {
        StringBuilder parametersAsQueryString = new StringBuilder();
        if (parameters != null) {
            boolean firstParameter = true;

            for (String parameterName : parameters.keySet()) {
                if (!firstParameter) {
                    parametersAsQueryString.append(PARAMETER_DELIMITER);
                }
                parametersAsQueryString.append(parameterName)
                        .append(PARAMETER_EQUALS_CHAR);
                try {
                    parametersAsQueryString.append(URLEncoder.encode(
                            parameters.get(parameterName), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                firstParameter = false;
            }
        }
        return parametersAsQueryString.toString();
    }

    private void updateDeviceConfiguration(Context mContext,HashMap<String,String> deviceParams)
    {


        RequestQueue mRequestQueue = Volley.newRequestQueue(mContext);
        StringRequest mStringRequest = new StringRequest(Request.Method.PUT, Global.terms, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    if (response != null) {
                        JSONObject responseObj = new JSONObject(response);
                        if (responseObj.getBoolean("Error")) {
                            Toast.makeText(TermsConditions.this, responseObj.getString("Message"),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            SharedPreferences.Editor editor = sharedPref.edit();
                            Intent intent = new Intent();
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setClass(TermsConditions.this, MainActivity2.
                                    class);
                            startActivity(intent);
                            editor.putBoolean("termsAccepted",true);
                            //  Global.reportSubmitted = true;

                            editor.commit();

                        }

                    } else {
                        Toast.makeText(TermsConditions.this, getResources().getString(R.string.server_error),
                                Toast.LENGTH_SHORT).show();
                    }
                    if (Global.progress.isShowing()) {
                        Global.stopProgressBar();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("vidisha","TermsConditions fail===="+error.toString());
            }
        }){
            @Override
            protected Map<String,String> getParams(){


                return deviceParams;
            }
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
