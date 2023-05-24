package com.irestore.pdm.signup;

import static com.irestore.pdm.Global.Global.updateDeviceConfiguration;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.irestore.pdm.Global.Global;
import com.irestore.pdm.GlobalActivities.BaseActivity;
import com.irestore.pdm.GlobalActivities.SyncActivity;
import com.irestore.pdm.MainActivity2;
import com.irestore.pdm.R;
import com.irestore.pdm.services.GPSTracker;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.UUID;



public class LoginActivity extends BaseActivity implements AdapterView.OnItemSelectedListener {


  GPSTracker gps;
    EditText phoneNumber,selectedUtility;
    public static EditText email;
    Button signIn;
    String phoneNo,responseJSON;
    String mUtility=null;


    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 123;
    Context context;
    SharedPreferences sharedPref;
    JSONObject responseData;

    Boolean termsAccepted,otpValidationRequired,adminApprovalRequired,viewCards,isSupervisor,isInspector,isCrewLeader,isContractor,isAdmin;
    JSONArray tenantsArray,userArray,subscriptionsArray,permissionsArray_RVA,roleMappingArray;
    String accountKey,logo,token,configuration,safetyMessage,s3Bucket,api,firestoreDb,firestoreCollection,firestoreUIData;

    public static String[] URLs ;
    Spinner spinner;
    ArrayAdapter<String> adapter;
    SharedPreferences.Editor editor;
    private String imageName;
    private boolean utilitySelected,utilitySelectedOne;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        gps = new GPSTracker(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        email = findViewById(R.id.userEmail);
        phoneNumber = findViewById(R.id.phoneNumber);
        selectedUtility = findViewById(R.id.selectedUtility);
        roleMappingArray = new JSONArray();
        signIn = findViewById(R.id.signInBtn);

        spinner = findViewById(R.id.selectUtility);
        spinner.setOnItemSelectedListener(this);

        context = getApplicationContext();
        signInClick();

        sharedPref = getSharedPreferences(getString(
                R.string.preference_file_key), Context.MODE_PRIVATE);
        editor = sharedPref.edit();


        email.setOnFocusChangeListener((view, b) -> {
            email.setTextColor(Color.WHITE);
        });

        Typeface typeFace = Typeface.createFromAsset(getAssets(), "AvenirLTStd-Book.otf");

        email.setTypeface(typeFace);
        phoneNumber.setTypeface(typeFace);
        selectedUtility.setTypeface(typeFace);

        signIn.setTypeface(typeFace);

        PhoneNumberFormatter2 addLineNumberFormatter = new PhoneNumberFormatter2(
                new WeakReference<>(phoneNumber),this,phoneNumber);
        phoneNumber.addTextChangedListener(addLineNumberFormatter);



    }
    public void fetchLocation() {
        gps = new GPSTracker(LoginActivity.this);

        Global.currentLocation = gps.getLocation();
        if (gps.canGetLocation() == false) {

        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        fetchLocation();
    }
    @Override
    public void onRestart() {
        super.onRestart();
        // When BACK BUTTON is pressed, the activity on the stack is restarted
        // Do what you want on the refresh procedure here
        //email.setText("");
    }

    @Override
    public void onStop() {
        super.onStop();

    }


    public void signInClick(){
        signIn.setOnClickListener(v -> {
            // TODO Auto-generated method stub

            phoneNo = phoneNumber.getText().toString().trim().replaceAll("[\\[\\]\\s(){}-]", "");
            editor.putString("phoneNumber", phoneNo);
            editor.putString("emailAddress", email.getText().toString());
            String util="";
            if(mUtility==null) {
                util = selectedUtility.getText().toString();

            }
            else
            {
                util=mUtility;
            }
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
            SharedPreferences.Editor editorstring;
            editorstring = preferences.edit();
            editorstring.putString("utility", util);
            editorstring.apply();


            editor.commit();//to skip otp
            if (!utilitySelectedOne) {


                if(email.getText().toString().isEmpty() && phoneNo.isEmpty())
                {
                    Toast.makeText(LoginActivity.this, "Email and Phone fields can not be left blank!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (email.getText().toString().isEmpty()) {
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.empty_email),
                            Toast.LENGTH_SHORT).show();
                    return;
                } else {

                    String email1 = email.getText().toString().trim();
                    String emailPattern = "[a-zA-Z0-9._'-]+@[a-z]+\\.+[a-z]+";

// onClick of button perform this simplest code.
                    if (!email1.matches(emailPattern))
                    {
                        Toast.makeText(LoginActivity.this, getResources().getString(R.string.invalid_email),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (email.getText().toString().length() > 150) {
                        Toast.makeText(LoginActivity.this, "Email "
                                        + getResources().getString(R.string.invalid_field_length1),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                }


                if (phoneNo.isEmpty()) {
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.empty_phone_number),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (phoneNo.length()!=10) {
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.empty_phone_number),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (Global.currentLocation != null) {

                    try {

                        InputMethodManager inputMethodManager = (InputMethodManager) LoginActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                        //Find the currently focused view, so we can grab the correct window token from it.
                        View view = LoginActivity.this.getCurrentFocus();
                        //If no view currently has focus, create a new one, just so we can grab a window token from it
                        if (view == null) {
                            view = new View(LoginActivity.this);
                        }
                        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);



                        if (Global.isNetworkAvailable(LoginActivity.this)) {
                            Global.createAndStartProgressBar(LoginActivity.this);
                            STAsyncHttpConnection async = new STAsyncHttpConnection(LoginActivity.this);
                            async.execute(Global.signUpURL+"/?email="+email.getText().toString().trim()+"&phone="+phoneNo);

                        } else {
                            Toast.makeText(LoginActivity.this, getResources().getString(R.string.internet_error),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("log_tag",
                                "Error in http connection!!" + e.toString());
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Toast.makeText(LoginActivity.this, getResources().getString(R.string.enable_location),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, getResources().getString(R.string.no_location),
                                Toast.LENGTH_SHORT).show();
                    }
                }

            }
            else
            {

                if (Global.currentLocation != null) {

                    try {

                        if(utilitySelected) {
                            if(sharedPref.getBoolean("otpValidationRequired",false)) {
                                if (Global.isNetworkAvailable(LoginActivity.this)) {
                                    Global.createAndStartProgressBar(LoginActivity.this);
                                    HttpGetRequest async = new HttpGetRequest();
                                    async.execute(Global.getOTP+sharedPref.getString("phoneNumber",""));
                                } else {
                                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.internet_error),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }else
                            {
                                if(sharedPref.getBoolean("adminApprovalRequired",false)) {
                                    if (!sharedPref.getString("subscriptionStatus", "").equals("approved")) {
                                        if (sharedPref.getString("userID", "").equals("0")) {
                                            Intent i = new Intent(LoginActivity.this, MyProfile.class);
                                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            //editor.putBoolean("OTPEntered", true);
                                            editor.putBoolean("userProfileCreated", false);
                                            editor.commit();
                                            startActivity(i);
                                        } else {
                                            if (!sharedPref.getString("subscriptionStatus", "").equals("")) {
                                                Intent i = new Intent(LoginActivity.this, AdminApprovalScreen.class);
                                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                // editor.putBoolean("OTPEntered", true);
                                                editor.putBoolean("userProfileCreated", true);
                                                editor.commit();
                                                startActivity(i);
                                            } else {
                                                Intent i = new Intent(LoginActivity.this, MyProfile.class);
                                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                // editor.putBoolean("OTPEntered", true);
                                                editor.putBoolean("userProfileCreated", false);
                                                editor.commit();
                                                startActivity(i);
                                            }
                                        }

                                    }
                                    else
                                    {
                                        if (!sharedPref.getBoolean("termsAccepted", false)) {
                                            Intent loginIntent = new Intent();
                                            loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            loginIntent.setClass(LoginActivity.this, TermsConditions.class);
                                            editor.putBoolean("ACCEPTANCE", sharedPref.getBoolean("termsAccepted", false));
                                            editor.putBoolean("userProfileCreated", true);
                                            editor.commit();
                                            startActivity(loginIntent);

                                        }
                                        else {
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
                                                jsonParam.put("version", LoginActivity.this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
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
                                                String osName = fields[Build.VERSION.SDK_INT].getName();

                                                deviceParam = new JSONObject();
                                                deviceParam.put("type", "ANDROID");
                                                deviceParam.put("os", osName);
                                                deviceParam.put("make", Build.MANUFACTURER);
                                                deviceParam.put("model", Build.MODEL);
                                                deviceParam.put("deviceString", deviceString);
                                                deviceParam.put("version", LoginActivity.this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
                                                deviceParam.put("pushNotificationToken", sharedPref.getString("pushNotificationToken",""));

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }


                                            HashMap<String, String> params = new HashMap<>();

                                            assert deviceParam != null;
                                            params.put("device", deviceParam.toString());
                                            params.put("terms", jsonParam.toString());
                                            try {
                                                params.put("version", LoginActivity.this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
                                            } catch (Exception e) {

                                            }
                                            params.put("phone", sharedPref.getString("phoneNumber", ""));
                                            params.put("email", sharedPref.getString("emailAddress", ""));


                                            if (Global.isNetworkAvailable(LoginActivity.this)) {
                                            /*    STAsyncHttpConnectionUpadteDevice async = new STAsyncHttpConnectionUpadteDevice(LoginActivity.this, params);
                                                async.execute(Global.terms);*/
                                                updateDeviceConfiguration(LoginActivity.this, params);
                                            } else {
                                                Toast.makeText(LoginActivity.this, getResources().getString(R.string.internet_error),
                                                        Toast.LENGTH_SHORT).show();
                                            }



                                            Intent i = new Intent();
                                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            i.setClass(LoginActivity.this, MainActivity2.class);
                                            editor.putBoolean("ACCEPTANCE", sharedPref.getBoolean("termsAccepted", false));
                                            editor.putBoolean("userProfileCreated", true);
                                            editor.putBoolean("adminApprovalStatus", true);
                                            editor.putString("deviceString",deviceString);
                                            editor.putString("newDeviceString",deviceString);
                                            editor.commit();
                                            startActivity(i);
                                        }
                                    }
                                }
                                else
                                {
                                    if (!sharedPref.getBoolean("termsAccepted", false)) {
                                        Intent loginIntent = new Intent();
                                        loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        loginIntent.setClass(LoginActivity.this, TermsConditions.class);
                                        editor.putBoolean("ACCEPTANCE", sharedPref.getBoolean("termsAccepted", false));
                                        editor.putBoolean("userProfileCreated", true);
                                        editor.commit();
                                        startActivity(loginIntent);

                                    }
                                    else {
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
                                            jsonParam.put("version", LoginActivity.this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
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
                                            String osName = fields[Build.VERSION.SDK_INT].getName();

                                            deviceParam = new JSONObject();
                                            deviceParam.put("type", "ANDROID");
                                            deviceParam.put("os", osName);
                                            deviceParam.put("make", Build.MANUFACTURER);
                                            deviceParam.put("model", Build.MODEL);
                                            deviceParam.put("deviceString", deviceString);
                                            deviceParam.put("version", LoginActivity.this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
                                            deviceParam.put("pushNotificationToken", sharedPref.getString("pushNotificationToken",""));

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }


                                        HashMap<String, String> params = new HashMap<>();

                                        assert deviceParam != null;
                                        params.put("device", deviceParam.toString());
                                        params.put("terms", jsonParam.toString());
                                        try {
                                            params.put("version", LoginActivity.this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
                                        } catch (Exception e) {

                                        }
                                        params.put("phone", sharedPref.getString("phoneNumber", ""));
                                        params.put("email", sharedPref.getString("emailAddress", ""));


                                        if (Global.isNetworkAvailable(LoginActivity.this)) {
                                            // Utils.createAndStartProgressBar(TermsConditions.this);
                                         /*   String httpPostData = STAsyncHttpConnectionUpadteDevice.createQueryStringForParameters(params);
                                            STAsyncHttpConnectionUpadteDevice async = new STAsyncHttpConnectionUpadteDevice(LoginActivity.this, params);
                                            async.execute(Global.terms);*/
                                            updateDeviceConfiguration(LoginActivity.this, params);
                                        } else {
                                            Toast.makeText(LoginActivity.this, getResources().getString(R.string.internet_error),
                                                    Toast.LENGTH_SHORT).show();
                                        }



                                        Intent i = new Intent();
                                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        i.setClass(LoginActivity.this, MainActivity2.class);
                                        editor.putBoolean("ACCEPTANCE", sharedPref.getBoolean("termsAccepted", false));
                                        editor.putBoolean("userProfileCreated", true);
                                        editor.putBoolean("adminApprovalStatus", true);
                                        editor.putString("deviceString",deviceString);
                                        editor.putString("newDeviceString",deviceString);
                                        editor.commit();
                                        startActivity(i);
                                    }
                                }
                            }
                        }
                        else
                        {
                            Toast.makeText(LoginActivity.this, getResources().getString(R.string.select_utility),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("log_tag",
                                "Error in http connection!!" + e.toString());
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Toast.makeText(LoginActivity.this, getResources().getString(R.string.enable_location),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, getResources().getString(R.string.no_location),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }


    public void processWebServiceResponse() throws JSONException {
        responseJSON = sharedPref.getString("fullResponseData", "");
        responseData = new JSONObject(responseJSON);
        email.setEnabled(false);
        phoneNumber.setEnabled(false);
        signIn.setText(R.string.next_btn);

        if(!responseData.getBoolean("Error")) {
            userArray = responseData.getJSONArray("User");
            tenantsArray = responseData.getJSONObject("Owners").getJSONArray("utilities");
            subscriptionsArray = responseData.getJSONArray("Subscription");

            if(responseData.has("RoleMapping"))
            {
                if(responseData.getJSONArray("RoleMapping").length()!=0)
                {
                    //Log.i("vidisha","roleeee=="+responseObject.getJSONArray("RoleMapping"));
                    roleMappingArray = responseData.getJSONArray("RoleMapping");
                }
            }
            permissionsArray_RVA = responseData.getJSONArray("Permissions");


           /* if(permissionsArray_RVA.length()!=0)
            {
                if(permissionsArray_RVA.getJSONObject(0).has("permissions")) {
                    viewCards = permissionsArray_RVA.getJSONObject(0).getJSONObject("permissions").getJSONObject("qaManager").getBoolean("view");
                    isAdmin = permissionsArray_RVA.getJSONObject(0).getJSONObject("permissions").getJSONObject("qaManager").getBoolean("isAdmin");
                    isSupervisor = permissionsArray_RVA.getJSONObject(0).getJSONObject("permissions").getJSONObject("qaManager").getBoolean("isSupervisor");
                    isInspector = permissionsArray_RVA.getJSONObject(0).getJSONObject("permissions").getJSONObject("qaManager").getBoolean("isInspector");
                    isCrewLeader = permissionsArray_RVA.getJSONObject(0).getJSONObject("permissions").getJSONObject("qaManager").getBoolean("isCrewLeader");
                    isContractor = permissionsArray_RVA.getJSONObject(0).getJSONObject("permissions").getJSONObject("qaManager").getBoolean("isContractor");
                }
                else {
                    viewCards = false;
                    isAdmin = false;
                    isSupervisor =false;
                    isInspector =false;
                    isCrewLeader =false;
                    isContractor =true;
                }
            }
            else {*/
                viewCards = false;
                isAdmin = false;
                isSupervisor =false;
                isInspector =false;
                isCrewLeader =false;
                isContractor =false;
         //   }


            URLs = new String[tenantsArray.length()];

            String[] spinnerArray = new String[tenantsArray.length()];
            String[] emailDomains = email.getText().toString().split("@");
            String emailC = emailDomains[1];
            int sel = 0;
            boolean emailDomainFound = false;

            if(subscriptionsArray.length()==0)
            {
                if(userArray.length()!=0)
                {
                    editor.putString("firstName", userArray.getJSONObject(0).getString("firstName"));
                    editor.putString("lastName", userArray.getJSONObject(0).getString("lastName"));
                    editor.putString("jobTitle", userArray.getJSONObject(0).getString("job"));
                    editor.putString("organization", userArray.getJSONObject(0).getString("organization"));
                    editor.putString("city", userArray.getJSONObject(0).getString("city"));
                    editor.putString("state", userArray.getJSONObject(0).getString("state"));
                    editor.putString("county", userArray.getJSONObject(0).getString("county"));
                    if(userArray.getJSONObject(0).has("departmentId"))
                        editor.putInt("departmentId", userArray.getJSONObject(0).getInt("departmentId"));

                    if(userArray.getJSONObject(0).has("primaryPhone"))
                        editor.putString("primaryPhone", userArray.getJSONObject(0).getString("primaryPhone"));
                    else
                        editor.putString("primaryPhone", "");
                    editor.commit();
                }
                JSONArray nt = new JSONArray();
                for(int i=0;i<tenantsArray.length();i++)
                {

                /*    if(tenantsArray.getJSONObject(i).getString("emailDomains").contains(emailC))
                    {

                        sel=i;
                        emailDomainFound = true;

                        break;
                    }*/
                    if(tenantsArray.getJSONObject(i).getString("emailDomains").contains(emailC))
                    {
                        nt.put(tenantsArray.getJSONObject(i));
                        //  tenantsArray =nt;
                        sel = i;



                    }

                }
                if(nt.length()>1) {

                    emailDomainFound = false;
                    tenantsArray=nt;
                    spinnerArray = new String[tenantsArray.length()];

                }else
                {
                    if(nt.length()!=0) {
                        emailDomainFound = true;
                        // sel= nt.length()-1;
                    }
                    //  tenantsArray =nt;
                }
                if(emailDomainFound)
                {

                    accountKey = tenantsArray.getJSONObject(sel).getString("accountKey");

                    logo = tenantsArray.getJSONObject(sel).getString("logo");
                    token = tenantsArray.getJSONObject(sel).getString("token");

                    configuration =  tenantsArray.getJSONObject(sel).getString("configuration");

                    JSONObject configuration = new JSONObject(tenantsArray.getJSONObject(sel).getString("configuration"));
                    if(configuration.has("safetyMessage"))
                        safetyMessage = configuration.getString("safetyMessage");
                    s3Bucket = configuration.getString("s3Bucket");
                    profilePicBucket = configuration.getString("profilePicBucket");
                    profilePicThumbnailsBucket = configuration.getString("profilePicThumbnailsBucket");
                    if(configuration.has("firestoreDb"))
                    firestoreDb = configuration.getString("firestoreDb");
                    if(configuration.has("firestoreCollection"))
                    firestoreCollection= configuration.getString("firestoreCollection");
                    if(configuration.has("firestoreUIData"))
                        firestoreUIData = configuration.getString("firestoreUIData");
                    api = configuration.getString("api");
                    otpValidationRequired = configuration.getBoolean("otpValidation");
                    adminApprovalRequired = configuration.getBoolean("adminApproval");
                    imageName = tenantsArray.getJSONObject(sel).getString("logo").
                            substring(tenantsArray.getJSONObject(sel).getString("logo").lastIndexOf("/") + 1);

                    JSONObject json = new JSONObject();
                    json.put("serviceEndpoint", accountKey+".irestore.info");
                    json.put("safetyMessage", safetyMessage);
                    json.put("s3BucketName", s3Bucket);
                    json.put("profilePicBucket", profilePicBucket);
                    json.put("profilePicThumbnailsBucket", profilePicThumbnailsBucket);
                    json.put("firestoreDb", firestoreDb);
                    json.put("firestoreCollection", firestoreCollection);
                    json.put("firestoreUIData", firestoreUIData);

                    json.put("authToken", token);
                    if(configuration.has("menuOptions"))
                        json.put("menuOptions", configuration.getString("menuOptions"));

                    editor.putString("accountKey",accountKey);
                    editor.putString("roleMappingArray", roleMappingArray.toString());

                    editor.putString("serviceEndpoint", accountKey+".irestore.info");
                    editor.putString("safetyMessage",safetyMessage);
                    editor.putString("s3Bucket",s3Bucket);
                    editor.putString("profilePicBucket", profilePicBucket);
                    editor.putString("profilePicThumbnailsBucket", profilePicThumbnailsBucket);
                    editor.putString("firestoreDb",firestoreDb);
                    editor.putString("firestoreCollection",firestoreCollection);
                    editor.putString("firestoreUIData",firestoreUIData);

                    editor.putString("api",api);
                    editor.putString("owner", accountKey);
                    editor.putBoolean("otpValidationRequired",otpValidationRequired);
                    editor.putBoolean("adminApprovalRequired",adminApprovalRequired);
                    editor.putString("token", token);
                    editor.putString("authToken",token);
                    editor.putString("utilityName", tenantsArray.getJSONObject(sel).getString("name"));
                    editor.putString("responseData", json.toString());
                    editor.putString("userType", "EMPLOYEE");

                    editor.putString("imageName", imageName);
                    editor.putString("imageURL",logo);
                    editor.putString("termsUtility",configuration.getString("terms"));
                    editor.putBoolean("viewCards", viewCards);
                    editor.putBoolean("isAdmin", isAdmin);
                    editor.putBoolean("isSupervisor", isSupervisor);
                    editor.putBoolean("isInspector", isInspector);
                    editor.putBoolean("isCrewLeader", isCrewLeader);
                    editor.putBoolean("isContractor", isContractor);
                    editor.commit();

                    selectedUtility.setVisibility(View.VISIBLE);
                    selectedUtility.setText(tenantsArray.getJSONObject(sel).getString("name"));
                    utilitySelected = true;
                    utilitySelectedOne = true;

                    // }


                }
                else
                {
                    editor.putString("userType", "TOWN_USER");
                    editor.commit();
                    for(int i=0;i<tenantsArray.length();i++) {
                        spinnerArray[i] = tenantsArray.getJSONObject(i).getString("name");
                        spinner.setVisibility(View.VISIBLE);
                        utilitySelected = false;
                        utilitySelectedOne = false;
                    }
                }

                ArrayList<String> positionList = new ArrayList<String>(Arrays.asList(spinnerArray));
                positionList.add(0,"--Select Utility--");
                String[] positions = positionList.toArray(new String[positionList.size()]);

                adapter = new ArrayAdapter<String>(LoginActivity.this, R.layout.spinner_item,
                        positions);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);


            }
            else
            {
                String fr_status;
                String termsConditions;
                if(subscriptionsArray.getJSONObject(0).has("subscriptionStatus"))
                {
                    fr_status = subscriptionsArray.getJSONObject(0).getString("subscriptionStatus");
                }
                else {
                    fr_status = "";
                }


                if(subscriptionsArray.getJSONObject(0).has("terms"))
                    termsConditions = subscriptionsArray.getJSONObject(0).getString("terms");
                else
                    termsConditions="";//QAManagerAndroid did


                termsAccepted = !termsConditions.isEmpty();

                editor.putBoolean("termsAccepted", termsAccepted);
//added
                editor.putString("firstName", userArray.getJSONObject(0).getString("firstName"));
                editor.putString("lastName", userArray.getJSONObject(0).getString("lastName"));
                editor.putString("emailAddress", userArray.getJSONObject(0).getString("email"));
                if(userArray.getJSONObject(0).has("primaryPhone"))
                    editor.putString("primaryPhone", userArray.getJSONObject(0).getString("primaryPhone"));
                else
                    editor.putString("primaryPhone", "");
                if(userArray.getJSONObject(0).has("departmentId"))
                    editor.putInt("departmentId", userArray.getJSONObject(0).getInt("departmentId"));


                editor.putString("phoneNumber", phoneNo);
                editor.putString("jobTitle", userArray.getJSONObject(0).getString("job"));
                editor.putString("organization", userArray.getJSONObject(0).getString("organization"));
                editor.putString("city", userArray.getJSONObject(0).getString("city"));
                editor.putString("state", userArray.getJSONObject(0).getString("state"));
                editor.putString("county", userArray.getJSONObject(0).getString("county"));
                if(!userArray.getJSONObject(0).getString("userType").equalsIgnoreCase("TOWN_USER"))
                    editor.putString("owner", userArray.getJSONObject(0).getString("owner"));
                else
                    editor.putString("owner", "");

                if(userArray.getJSONObject(0).has("departmentId"))
                    editor.putInt("departmentId", userArray.getJSONObject(0).getInt("departmentId"));


                editor.putString("userType", userArray.getJSONObject(0).getString("userType"));
                if(userArray.getJSONObject(0).has("userId"))
                    editor.putString("userID", String.valueOf(userArray.getJSONObject(0).getString("userId")));
                else
                    editor.putString("userID", "0");



                accountKey = tenantsArray.getJSONObject(0).getString("accountKey");

                logo = tenantsArray.getJSONObject(0).getString("logo");
                token = tenantsArray.getJSONObject(0).getString("token");


                new DownloadImage().execute(logo);

                imageName = tenantsArray.getJSONObject(0).getString("logo").
                        substring(tenantsArray.getJSONObject(0).getString("logo").lastIndexOf("/") + 1);


                configuration =  tenantsArray.getJSONObject(0).getString("configuration");

                JSONObject configuration = new JSONObject(tenantsArray.getJSONObject(0).getString("configuration"));
                if(configuration.has("safetyMessage"))
                    safetyMessage = configuration.getString("safetyMessage");
                s3Bucket = configuration.getString("s3Bucket");
                profilePicBucket = configuration.getString("profilePicBucket");
                profilePicThumbnailsBucket = configuration.getString("profilePicThumbnailsBucket");
                if(configuration.has("firestoreDb"))
                firestoreDb = configuration.getString("firestoreDb");
                if(configuration.has("firestoreCollection"))
                  firestoreCollection = configuration.getString("firestoreCollection");
                if(configuration.has("firestoreUIData"))
                  firestoreUIData = configuration.getString("firestoreUIData");
                api = configuration.getString("api");

                otpValidationRequired = configuration.getBoolean("otpValidation");
                adminApprovalRequired = configuration.getBoolean("adminApproval");

                editor.putString("accountKey",accountKey);
                editor.putString("roleMappingArray", roleMappingArray.toString());

                editor.putString("safetyMessage",safetyMessage);
                editor.putString("imageName",imageName);

                editor.putString("s3Bucket",s3Bucket);
                editor.putString("profilePicBucket", profilePicBucket);
                editor.putString("profilePicThumbnailsBucket", profilePicThumbnailsBucket);
                editor.putString("firestoreDb",firestoreDb);
                editor.putString("firestoreCollection",firestoreCollection);
                editor.putString("firestoreUIData",firestoreUIData);

                editor.putString("api",api);
                editor.putBoolean("otpValidationRequired",otpValidationRequired);
                editor.putBoolean("adminApprovalRequired",adminApprovalRequired);
                editor.putString("token", token);
                editor.putString("authToken",token);
                if(configuration.has("menuOptions"))
                    editor.putString("menuOptions", configuration.getString("menuOptions"));
                editor.putString("subscriptionStatus", fr_status);
                editor.putBoolean("termsAccepted", termsAccepted);
                editor.putString("imageName",imageName);
                editor.putString("serviceEndpoint", accountKey+".irestore.info");

                JSONObject json = new JSONObject();
                json.put("serviceEndpoint", accountKey+".irestore.info");
                json.put("safetyMessage", safetyMessage);
                json.put("s3BucketName", s3Bucket);
                json.put("profilePicBucket", profilePicBucket);
                json.put("profilePicThumbnailsBucket", profilePicThumbnailsBucket);
                json.put("firestoreDb", firestoreDb);
                json.put("firestoreCollection", firestoreCollection);
                json.put("firestoreUIData", firestoreUIData);


                json.put("authToken", token);
                if(configuration.has("menuOptions"))
                    json.put("menuOptions", configuration.getString("menuOptions"));

                editor.putString("responseData", json.toString());
                editor.putString("utilityName", tenantsArray.getJSONObject(0).getString("name"));
                editor.putString("imageURL",logo);
                editor.putString("termsUtility",configuration.getString("terms"));
                editor.putBoolean("viewCards", viewCards);
                editor.putBoolean("isAdmin", isAdmin);
                editor.putBoolean("isSupervisor", isSupervisor);
                editor.putBoolean("isInspector", isInspector);
                editor.putBoolean("isCrewLeader", isCrewLeader);
                editor.putBoolean("isContractor", isContractor);
                selectedUtility.setVisibility(View.VISIBLE);
                selectedUtility.setText(tenantsArray.getJSONObject(sel).getString("name"));
                utilitySelected = true;
                utilitySelectedOne = true;
                editor.commit();


            }


        }

    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String item = adapterView.getItemAtPosition(i).toString();
        mUtility=adapterView.getItemAtPosition(i).toString();
        // Showing selected spinner item
        //Toast.makeText(adapterView.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
        JSONObject selectedTenant ;
        try {
            if(i!=0) {

                selectedTenant = tenantsArray.getJSONObject(i-1);



                if(item.equalsIgnoreCase(selectedTenant.getString("name")))
                {
                    String logoURL;
                    accountKey = selectedTenant.getString("accountKey");
                    token = selectedTenant.getString("token");
                    configuration =  selectedTenant.getString("configuration");
                    JSONObject configuration = new JSONObject(selectedTenant.getString("configuration"));
                    if(configuration.has("safetyMessage"))
                        safetyMessage = configuration.getString("safetyMessage");
                    s3Bucket = configuration.getString("s3Bucket");
                    profilePicBucket = configuration.getString("profilePicBucket");
                    profilePicThumbnailsBucket = configuration.getString("profilePicThumbnailsBucket");
                    if(configuration.has("firestoreDb"))
                    firestoreDb = configuration.getString("firestoreDb");
                    if(configuration.has("firestoreCollection"))
                    firestoreCollection = configuration.getString("firestoreCollection");
                    if(configuration.has("firestoreUIData"))
                    firestoreUIData = configuration.getString("firestoreUIData");
                    api = configuration.getString("api");
                    otpValidationRequired = configuration.getBoolean("otpValidation");
                    adminApprovalRequired = configuration.getBoolean("adminApproval");
                    logoURL = selectedTenant.getString("logo");


                    imageName = selectedTenant.getString("logo").
                            substring(selectedTenant.getString("logo").lastIndexOf("/") + 1);

                    editor.putString("accountKey",accountKey);
                    editor.putString("roleMappingArray", roleMappingArray.toString());

                    editor.putString("safetyMessage",safetyMessage);
                    editor.putString("s3Bucket",s3Bucket);
                    editor.putString("profilePicBucket",profilePicBucket);
                    editor.putString("profilePicThumbnailsBucket",profilePicThumbnailsBucket);
                    editor.putString("firestoreDb",firestoreDb);
                    editor.putString("firestoreCollection",firestoreCollection);
                    editor.putString("firestoreUIData",firestoreUIData);

                    editor.putString("api",api);
                    editor.putBoolean("otpValidationRequired",otpValidationRequired);
                    editor.putBoolean("adminApprovalRequired",adminApprovalRequired);
                    editor.putString("imageName",imageName);
                    editor.putString("owner", "");//not found
                    editor.putString("token", token);
                    editor.putString("authToken",token);
                    if(configuration.has("menuOptions"))
                        editor.putString("menuOptions", configuration.getString("menuOptions"));
                    editor.putString("userID","0");
                    editor.putString("imageURL",logoURL);



                    JSONObject json = new JSONObject();
                    json.put("serviceEndpoint", accountKey+".irestore.info");
                    json.put("safetyMessage", safetyMessage);
                    json.put("s3BucketName", s3Bucket);
                    json.put("profilePicBucket", profilePicBucket);
                    json.put("profilePicThumbnailsBucket", profilePicThumbnailsBucket);
                    json.put("firestoreDb", firestoreDb);
                    json.put("firestoreCollection", firestoreCollection);
                    json.put("firestoreUIData", firestoreUIData);

                    json.put("authToken", token);
                    if(configuration.has("menuOptions"))
                        json.put("menuOptions", configuration.getString("menuOptions"));
                    editor.putString("responseData", json.toString());
                    editor.putString("termsUtility",configuration.getString("terms"));
                    editor.putString("utilityName", selectedTenant.getString("name"));
                    editor.putString("serviceEndpoint", accountKey+".irestore.info");
                    editor.putBoolean("viewCards", viewCards);
                    editor.putBoolean("isAdmin", isAdmin);
                    editor.putBoolean("isSupervisor", isSupervisor);
                    editor.putBoolean("isInspector", isInspector);
                    editor.putBoolean("isCrewLeader", isCrewLeader);
                    editor.putBoolean("isContractor", isContractor);
                    editor.putString("s3BucketName", s3Bucket);
                    editor.putString("profilePicBucket", profilePicBucket);

                    editor.commit();


                    utilitySelected = true;
                    utilitySelectedOne = true;
                    // editor.putString("tenantID",selectedTenant.getString("tenantID"));


                }
            }
            else
            {
                utilitySelected = false;
                utilitySelectedOne =true;

            }
            /*else
            {
                Toast.makeText(LoginScreen.this, getResources().getString(R.string.select_utility),
                        Toast.LENGTH_SHORT).show();
            }*/

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if (requestCode == REQUEST_ID_MULTIPLE_PERMISSIONS) {


            if (grantResults.length > 0) {
                for (int i = 0; i < permissions.length; i++) {


                    if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.e("msg", "storage granted");

                        }
                    }
                    else if (permissions[i].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.e("msg", "storage granted");

                        }
                        else if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                                Log.e("msg", "location granted");

                            }
                        }
                        else if (permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                                Log.e("msg", "location granted");

                            }
                        } else if (permissions[i].equals(Manifest.permission.CAMERA)) {
                            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                                Log.e("msg", "camerqa granted");

                            }
                        }
                        else if (permissions[i].equals(Manifest.permission.READ_PHONE_STATE)) {
                            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                                Log.e("msg", "read phone granted");

                            }
                        }

                    }

                }


            }}}

    // DownloadImage AsyncTask
    private class DownloadImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Bitmap doInBackground(String... URL) {

            String imageURL = URL[0];

            Bitmap bitmap = null;
            try {
                // Download Image from URL
                InputStream input = new URL(imageURL).openStream();
                // Decode Bitmap
                bitmap = BitmapFactory.decodeStream(input);

                SaveImage(bitmap,sharedPref.getString("imageName",imageName));

            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {


        }

        private void SaveImage(Bitmap finalBitmap, String imageName) {


            File internalStorage = new File(getExternalFilesDir(
                    Environment.DIRECTORY_PICTURES), ".PDM");
            if (!internalStorage.mkdirs()) {
                Log.i("PDM", "Directory not created");
            }

            File file = new File(internalStorage, imageName);
          //  if (file.exists ()) file.delete ();
            try {
                FileOutputStream out = new FileOutputStream(file);
                finalBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                out.flush();
                out.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class HttpGetRequest extends AsyncTask<String, Void, String> {
        public static final String REQUEST_METHOD = "GET";
        public static final int READ_TIMEOUT = 15000;
        public static final int CONNECTION_TIMEOUT = 15000;
        @Override
        protected String doInBackground(String... params){
            String stringUrl = params[0];
            String result;
            String inputLine;
            try {
                //Create a URL object holding our url
                URL myUrl = new URL(stringUrl);
                //Create a connection
                HttpURLConnection connection =(HttpURLConnection)
                        myUrl.openConnection();
                //Set methods and timeouts
                connection.setRequestMethod(REQUEST_METHOD);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                connection.setRequestProperty ("x-account-key", sharedPref.getString("accountKey",""));
                connection.setRequestProperty ("x-access-token", sharedPref.getString("token",""));
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("x-application", "PDM");
                connection.setRequestProperty("x-user", sharedPref.getString("phoneNumber",""));

                //Connect to our url
                connection.connect();
                //Create a new InputStreamReader
                InputStreamReader streamReader = new
                        InputStreamReader(connection.getInputStream());
                //Create a new buffered reader and String Builder
                BufferedReader reader = new BufferedReader(streamReader);
                StringBuilder stringBuilder = new StringBuilder();
                //Check if the line we are reading is not null
                while((inputLine = reader.readLine()) != null){
                    stringBuilder.append(inputLine);
                }
                //Close our InputStream and Buffered reader
                reader.close();
                streamReader.close();
                //Set our result equal to our stringBuilder
                result = stringBuilder.toString();
            }
            catch(IOException e){
                if (Global.progress.isShowing()) {
                    Global.stopProgressBar();
                }
                e.printStackTrace();
                result = null;
            }
            return result;
        }
        protected void onPostExecute(String result){
            // super.onPostExecute(result);
            Log.i("PDM","EnterOTP==="+result);
            JSONObject otpObject;
            try {
                otpObject = new JSONObject(result);
                editor.putString("otpValue", otpObject.getString("OTP"));
                editor.commit();
                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(LoginActivity.this, EnterOTP.class);
                startActivity(intent);
                if (Global.progress.isShowing()) {
                    Global.stopProgressBar();
                }

            }catch(Exception e)
            {
                if (Global.progress.isShowing()) {
                    Global.stopProgressBar();
                }
            }


        }
    }




}


class PhoneNumberFormatter2 implements TextWatcher {
    //This TextWatcher sub-class formats entered numbers as 1 (123) 456-7890
    private boolean mFormatting; // this is a flag which prevents the
    // stack(onTextChanged)
    private boolean clearFlag;
    private int mLastStartLocation;
    private String mLastBeforeText;
    private WeakReference<EditText> mWeakEditText;
    private Context context;
    private EditText ed;

    public PhoneNumberFormatter2(WeakReference<EditText> weakEditText,Context c,EditText ed) {
        this.mWeakEditText = weakEditText;
        this.context = c;
        this.ed = ed;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
        if (after == 0 && s.toString().equals("1 ")) {
            clearFlag = true;
        }
        mLastStartLocation = start;
        mLastBeforeText = s.toString();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before,
                              int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        // Make sure to ignore calls to afterTextChanged caused by the work
        // done below
        final InputMethodManager mgr = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(s.toString().replaceAll("[\\[\\]\\s(){}-]", "").length()==10) {
            mgr.hideSoftInputFromWindow(ed.getWindowToken(), 0);
            AsyncHttpClient client = new AsyncHttpClient();
            Global.createAndStartProgressBar(context);

            client.addHeader("x-account-key", "general");
            client.addHeader("x-access-token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJpUmVzdG9yZSIsImF1ZCI6ImdlbmVyYWwiLCJzdWIiOiJhdXRoZW50aWNhdGlvbiIsImlhdCI6MTQ2OTA4NzYwMX0.48fKAGieV0j9kQDu9wlaLi6a1B837nuCqthJmROSy-0");
            client.addHeader("Content-Type", "application/json");
            client.addHeader("x-application", "PDM");
            client.addHeader("x-user", s.toString().replaceAll("[\\[\\]\\s(){}-]", ""));

            Log.i("pushpa","request=="+Global.getUserEmailByPhone +s.toString().replaceAll("[\\[\\]\\s(){}-]", ""));
            client.get(Global.getUserEmailByPhone +s.toString().replaceAll("[\\[\\]\\s(){}-]", ""), new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {
                    if (Global.progress.isShowing()) {
                        Global.stopProgressBar();
                    }
                    Log.i("pushpa","failssssss=="+responseString);
                }

                @Override
                public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                    try{
                        Log.i("pushpa","passsss=="+responseString);
                        JSONObject result = new JSONObject(responseString);

                        if(result!=null)
                        {
                            if(result.has("email")) {
                                if (result.getJSONArray("email").length() != 0) {
                                    String emailValue = result.getJSONArray("email").getString(0);
                                    LoginActivity.email.setText(emailValue);
                                    LoginActivity.email.setTextColor(Color.parseColor("#5A5A5A"));
                                }else
                                {
                                    LoginActivity.email.setText("");
                                    if(result.has("message")) {
                                        if (!result.getString("message").isEmpty()) {

                                            AlertDialog.Builder alert = new AlertDialog.Builder(
                                                    context, R.style.MyAlertDialogStyle);
                                            alert.setMessage(result.getString("message"));
                                            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    dialog.dismiss();
                                                }
                                            });

                                            alert.setCancelable(false);
                                            alert.show();
                                        }
                                        else
                                            Toast.makeText(context, "Server Error",
                                                    Toast.LENGTH_SHORT).show();
                                    }else
                                    {
                                        Toast.makeText(context, "Server Error",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            if (Global.progress.isShowing()) {
                                Global.stopProgressBar();
                            }
                        }
                    }catch (Exception e)
                    {

                    }
                }

        });



            return;
        }
        if (!mFormatting) {
            mFormatting = true;
            int curPos = mLastStartLocation;
            String beforeValue = mLastBeforeText;
            String currentValue = s.toString();
            String formattedValue = formatUsNumber(s);
            if (currentValue.length() > beforeValue.length()) {
                int setCusorPos = formattedValue.length()
                        - (beforeValue.length() - curPos);
                mWeakEditText.get().setSelection(setCusorPos < 0 ? 0 : setCusorPos);
            } else {
                int setCusorPos = formattedValue.length()
                        - (currentValue.length() - curPos);
                if (setCusorPos > 0 && !Character.isDigit(formattedValue.charAt(setCusorPos - 1))) {
                    setCusorPos--;
                }
                mWeakEditText.get().setSelection(setCusorPos < 0 ? 0 : setCusorPos);
            }
                     mFormatting = false;
        }
    }

    private String formatUsNumber(Editable text) {
        StringBuilder formattedString = new StringBuilder();
        // Remove everything except digits
        int p = 0;
        while (p < text.length()) {
            char ch = text.charAt(p);
            if (!Character.isDigit(ch)) {
                text.delete(p, p + 1);
            } else {
                p++;
            }
        }
        // Now only digits are remaining
        String allDigitString = text.toString();

        int totalDigitCount = allDigitString.length();

        if (totalDigitCount == 0
                || (totalDigitCount > 10 && !allDigitString.startsWith("1"))
                || totalDigitCount > 11) {
            // May be the total length of input length is greater than the
            // expected value so we'll remove all formatting
            text.clear();
            text.append(allDigitString);
            return allDigitString;
        }
        int alreadyPlacedDigitCount = 0;
        // Only '1' is remaining and user pressed backspace and so we clear
        // the edit text.
        if (allDigitString.equals("1") && clearFlag) {
            text.clear();
            clearFlag = false;
            return "";
        }
        /*if (allDigitString.startsWith("1")) {
            formattedString.append("1 ");
            alreadyPlacedDigitCount++;
        }*/
        // The first 3 numbers beyond '1' must be enclosed in brackets "()"
        if (totalDigitCount - alreadyPlacedDigitCount > 3) {
            formattedString.append("("
                    + allDigitString.substring(alreadyPlacedDigitCount,
                    alreadyPlacedDigitCount + 3) + ") ");
            alreadyPlacedDigitCount += 3;
        }
        // There must be a '-' inserted after the next 3 numbers
        if (totalDigitCount - alreadyPlacedDigitCount > 3) {
            formattedString.append(allDigitString.substring(
                    alreadyPlacedDigitCount, alreadyPlacedDigitCount + 3)
                    + "-");
            alreadyPlacedDigitCount += 3;
        }
        // All the required formatting is done so we'll just copy the
        // remaining digits.
        if (totalDigitCount > alreadyPlacedDigitCount) {
            formattedString.append(allDigitString
                    .substring(alreadyPlacedDigitCount));
        }

        text.clear();
        text.append(formattedString.toString());
        return formattedString.toString();
    }


}




