package com.irestore.pdm.signup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.irestore.pdm.BuildConfig;
import com.irestore.pdm.Global.Global;
import com.irestore.pdm.Global.ShowLogs;
import com.irestore.pdm.GlobalActivities.BaseActivity;
import com.irestore.pdm.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


public class MyProfile extends BaseActivity {
    TransferUtility transferUtility;
    EditText email,phone,firstName,lastName,jobTitle,organization,city,state,county;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    TextView uploadPhotoLabel;
    Button nextBtn;
    ImageView profileImg;
    public static String profileImageUrl;
    String registeredEmail,registeredPhone,registeredfName,registeredlName,
            registeredJobTitle,registeredOrg,terms,userID,tenantName,registeredCity,rigisteredCounty,registeredState;
    Exception exception;
    String httpPostData,authToken;
    private static final char PARAMETER_DELIMITER = '&';
    private static final char PARAMETER_EQUALS_CHAR = '=';
    private static final int CONNECTION_TIMEOUT = 3000;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;

    File mypath;
    File storageDirectory;
    AmazonS3 s3;
    public static Bitmap bitmapNew;
    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile1);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Typeface typeFace = Typeface.createFromAsset(getAssets(), "AvenirLTStd-Book.otf");

        sharedPref = getSharedPreferences(getString(
                R.string.preference_file_key), Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        profileImg = findViewById(R.id.profileImg);

        registeredEmail = sharedPref.getString("emailAddress", "");
        registeredPhone = sharedPref.getString("phoneNumber", "");
        registeredfName = sharedPref.getString("firstName", "");
        registeredlName = sharedPref.getString("lastName", "");

        registeredJobTitle = sharedPref.getString("jobTitle", "");
        registeredOrg = sharedPref.getString("organization", "");
        registeredCity=  sharedPref.getString("city", "");
        registeredState=  sharedPref.getString("state", "");
        rigisteredCounty=  sharedPref.getString("county", "");

        authToken = sharedPref.getString("token","");
        tenantName = sharedPref.getString("accountKey","");

        uploadPhotoLabel = findViewById(R.id.uploadPhotoLabel);

        email = findViewById(R.id.registeredEmail);
        phone = findViewById(R.id.registeredNumber);
        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        jobTitle = findViewById(R.id.title);
        organization = findViewById(R.id.organization);
        city = findViewById(R.id.city);
        county = findViewById(R.id.county);
        state = findViewById(R.id.state);


        nextBtn = findViewById(R.id.signOutBtn);

        if(!registeredfName.isEmpty())
        {
            firstName.setEnabled(false);
            firstName.setFocusable(false);
        }
        if(!registeredlName.isEmpty())
        {
            lastName.setEnabled(false);
            lastName.setFocusable(false);
        }


        email.setText(registeredEmail);
        phone.setText(registeredPhone);

        firstName.setText(registeredfName);
        lastName.setText(registeredlName);
        jobTitle.setText(registeredJobTitle);
        organization.setText(registeredOrg);
        city.setText(registeredCity);
        county.setText(rigisteredCounty);
        state.setText(registeredState);

        email.setTypeface(typeFace);
        phone.setTypeface(typeFace);
        firstName.setTypeface(typeFace);
        lastName.setTypeface(typeFace);
        jobTitle.setTypeface(typeFace);
        organization.setTypeface(typeFace);
        city.setTypeface(typeFace);
        county.setTypeface(typeFace);
        state.setTypeface(typeFace);
        nextBtn.setTypeface(typeFace);
        amazonS3Setup(this);
        storageDirectory = new File(getExternalFilesDir(
                Environment.DIRECTORY_PICTURES), ".PDM");
        if (storageDirectory == null || !storageDirectory.mkdirs()) {
            Log.i("TreeInventory", "Directory not created");
        }
        File myDir = new File(storageDirectory ,  sharedPref.getString("userID","")+".png");

        Log.i("vidisha","dfsfdsfsdgsdg"+myDir.exists());
        if(myDir.exists()) {
            Bitmap b = null;
            try {
                b = BitmapFactory.decodeStream(new FileInputStream(myDir));
                profileImg.setImageBitmap(b);
                profileImg.setEnabled(true);
                uploadPhotoLabel.setText(R.string.edit_photo);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
        profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                   selectImage();

              //  checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);



            }
        });
       /* firstName.setFilters(new InputFilter[] {

                new RegexInputFilter("^[a-zA-Z]+[a-zA-Z.']*$"),
                new InputFilter.LengthFilter(50)
        });*/
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firstName.getText().toString().trim().isEmpty()) {
                    Toast.makeText(MyProfile.this, getResources().getString(R.string.empty_fname),
                            Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    if (!isValidName(firstName.getText().toString().trim())) {
                        Toast.makeText(MyProfile.this, getResources().getString(R.string.invalid_fname),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (firstName.getText().toString().trim().length() > 50) {
                        Toast.makeText(MyProfile.this, getResources().getString(R.string.first_name)+" "
                                        + getResources().getString(R.string.invalid_field_length1),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if (lastName.getText().toString().trim().isEmpty()) {
                    Toast.makeText(MyProfile.this, getResources().getString(R.string.empty_lname),
                            Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    if (!isValidName(lastName.getText().toString().trim())) {
                        Toast.makeText(MyProfile.this, getResources().getString(R.string.invalid_lname),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (lastName.getText().toString().trim().length() > 50) {
                        Toast.makeText(MyProfile.this, getResources().getString(R.string.last_name)+" "
                                        + getResources().getString(R.string.invalid_field_length1),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if (jobTitle.getText().toString().trim().isEmpty()) {
                    Toast.makeText(MyProfile.this, getResources().getString(R.string.empty_title),
                            Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    if (!isValidTitle(jobTitle.getText().toString().trim())) {
                        Toast.makeText(MyProfile.this, getResources().getString(R.string.invalid_title),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (jobTitle.getText().toString().trim().length() > 50) {
                        Toast.makeText(MyProfile.this, getResources().getString(R.string.title)+" "
                                        + getResources().getString(R.string.invalid_field_length1),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if (organization.getText().toString().isEmpty()) {
                    Toast.makeText(MyProfile.this, getResources().getString(R.string.empty_Organization),
                            Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    if (!isValidTitle(organization.getText().toString().trim())) {
                        Toast.makeText(MyProfile.this, getResources().getString(R.string.invalid_org),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (organization.getText().toString().trim().length() > 50) {
                        Toast.makeText(MyProfile.this, getResources().getString(R.string.organization_name)
                                        + getResources().getString(R.string.invalid_field_length1),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                if (!city.getText().toString().trim().isEmpty()) {
                    if (!isValidName(city.getText().toString().trim())) {
                        Toast.makeText(MyProfile.this, getResources().getString(R.string.valid_city),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (city.getText().toString().trim().length() > 50) {
                        Toast.makeText(MyProfile.this, getResources().getString(R.string.city)
                                        + getResources().getString(R.string.invalid_field_length1),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if (!state.getText().toString().trim().isEmpty()) {
                    if (!isValidName(state.getText().toString().trim())) {
                        Toast.makeText(MyProfile.this, getResources().getString(R.string.valid_state),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (state.getText().toString().trim().length() > 50) {
                        Toast.makeText(MyProfile.this, getResources().getString(R.string.state)
                                        + getResources().getString(R.string.invalid_field_length1),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if (!county.getText().toString().trim().isEmpty()) {
                    if (!isValidName(county.getText().toString().trim())) {
                        Toast.makeText(MyProfile.this, getResources().getString(R.string.valid_country),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (county.getText().toString().trim().length() > 50) {
                        Toast.makeText(MyProfile.this, getResources().getString(R.string.country)
                                        + getResources().getString(R.string.invalid_field_length1),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                if (Global.currentLocation != null)
                {
                    if(!sharedPref.getBoolean("userProfileCreated", false))
                    {
                        HashMap<String, String> params = new HashMap<String, String>();
                        params.put("email", email.getText().toString());
                        params.put("phone", phone.getText().toString());

                        if (!sharedPref.getString("primaryPhone", "").isEmpty())
                            params.put("primaryPhone", sharedPref.getString("primaryPhone", ""));
                        else
                            params.put("primaryPhone", phone.getText().toString());
                        params.put("firstName", firstName.getText().toString().trim());
                        params.put("lastName", lastName.getText().toString().trim());
                        if (sharedPref.getString("userType", "").equalsIgnoreCase("TOWN_USER"))
                            params.put("owner", "");
                        else
                            params.put("owner", sharedPref.getString("accountKey", ""));
                        params.put("organization", organization.getText().toString().trim());
                        params.put("job", jobTitle.getText().toString().trim());
                        params.put("city", city.getText().toString().trim());
                        params.put("state", state.getText().toString().trim());//CPManagerAndroid changed
                        params.put("county", county.getText().toString().trim());
                        params.put("userType", sharedPref.getString("userType", ""));

                        if (Global.isNetworkAvailable(MyProfile.this)) {

                            Global.createAndStartProgressBar(MyProfile.this);
                            sendAndRequestResponse(Global.createProfile,params);
                        } else {
                            Toast.makeText(MyProfile.this, getResources().getString(R.string.internet_error),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        if (!jobTitle.getText().toString().trim().equalsIgnoreCase(registeredJobTitle)
                                || !organization.getText().toString().trim().equalsIgnoreCase(registeredOrg)
                                || !city.getText().toString().trim().equalsIgnoreCase(registeredCity)
                                || !state.getText().toString().trim().equalsIgnoreCase(registeredCity)
                                || !county.getText().toString().trim().equalsIgnoreCase(rigisteredCounty)
                                || !firstName.getText().toString().trim().equalsIgnoreCase(registeredfName)
                                || !lastName.getText().toString().trim().equalsIgnoreCase(registeredlName)
                        ) {
                            HashMap<String, String> params = new HashMap<String, String>();
                            params.put("email", email.getText().toString());
                            params.put("phone", phone.getText().toString());


                            if (!sharedPref.getString("primaryPhone", "").isEmpty())
                                params.put("primaryPhone", sharedPref.getString("primaryPhone", ""));
                            else
                                params.put("primaryPhone", phone.getText().toString());
                            params.put("firstName", firstName.getText().toString().trim());
                            params.put("lastName", lastName.getText().toString().trim());
                            if (sharedPref.getString("userType", "").equalsIgnoreCase("TOWN_USER"))
                                params.put("owner", "");
                            else
                                params.put("owner", sharedPref.getString("accountKey", ""));
                            params.put("organization", organization.getText().toString().trim());
                            params.put("job", jobTitle.getText().toString().trim());
                            params.put("city", city.getText().toString());
                            params.put("state", state.getText().toString());//CPManagerAndroid changed
                            params.put("county", county.getText().toString().trim());
                            params.put("userType", sharedPref.getString("userType", ""));


                            if (Global.isNetworkAvailable(MyProfile.this)) {
                                Global.createAndStartProgressBar(MyProfile.this);

                                sendAndRequestResponse(Global.updateProfile,params);
                            } else {
                                Toast.makeText(MyProfile.this, getResources().getString(R.string.internet_error),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                        else
                        {
                            finish();
                            if(profileImageUrl!=null)
                                updateImageToUpload(sharedPref.getString("userID",""));

                        }
                    }

                }else {
                    Toast.makeText(MyProfile.this, getResources().getString(R.string.no_location),
                            Toast.LENGTH_SHORT).show();
                }

            }
        });

        LayoutInflater inflator = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.custom_titlebar_new, null);

        ActionBar actionBar = getSupportActionBar();
        TextView title = v.findViewById(R.id.title);
        Button nextBtn1 = v.findViewById(R.id.nextBtn);
        nextBtn1.setVisibility(View.INVISIBLE);
        title.setText(R.string.my_profile);

        title.setTypeface(typeFace);

        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled (false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setCustomView(v);


    }
    private void selectImage() {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(MyProfile.this);
        builder.setTitle("Add Profile Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo"))
                {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);


                    File f = new File(storageDirectory, sharedPref.getString("userID","")+".png");

                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Uri uri = FileProvider.getUriForFile(MyProfile.this, BuildConfig.APPLICATION_ID + ".fileprovider",f);

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(intent, 1);
                }
                else if (options[item].equals("Choose from Gallery"))
                {
                    Intent intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);
                }
               /* else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }*/
            }
        });
        builder.show();
    }
    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(MyProfile.this, permission) == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(MyProfile.this, new String[] { permission }, requestCode);
        }
        else {
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(MyProfile.this);
            // Toast.makeText(MyProfile.this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

    // This function is called when the user accepts or decline the permission.
    // Request Code is used to check which permission called this function.
    // This request code is provided when the user is prompt for permission.

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(MyProfile.this);
                //Toast.makeText(MyProfile.this, "Camera Permission Granted", Toast.LENGTH_SHORT) .show();
            }
            else {
                Toast.makeText(MyProfile.this, R.string.camera_permission, Toast.LENGTH_SHORT) .show();
            }
        }
        else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(MyProfile.this);
                // Toast.makeText(MyProfile.this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MyProfile.this, R.string.enable_storage, Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void amazonS3Setup(Context mContext) {
        credentialsProvider(mContext);
        setTransferUtility(mContext);
    }
    public void credentialsProvider(Context mContext) {
        // Initialize the Amazon Cognito credentials provider
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                mContext,
                Global.COGNITO_POOL_ID, // Identity Pool ID
                Global.COGNITO_REGION // Region
        );
        setAmazonS3Client(credentialsProvider);
    }

    public void setAmazonS3Client(CognitoCachingCredentialsProvider credentialsProvider) {
        // Create an S3 client
        s3 = new AmazonS3Client(credentialsProvider);
        // Set the region of your S3 bucket
        s3.setRegion(Region.getRegion(Global.BUCKET_REGION));
    }

    public void setTransferUtility(Context mContext) {
        transferUtility = new TransferUtility(s3, mContext);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            File s = null;

            for (File temp : storageDirectory.listFiles()) {
                if (temp.getName().equals(sharedPref.getString("userID", "") + ".png")) {
                    s = temp;
                    break;
                }
            }
            String realPath = s.getAbsolutePath();
            File f = new File(realPath);
            //  uri = Uri.fromFile(f);
            CropImage.activity(Uri.fromFile(f))
                    .start(this);
        }
        else if(requestCode==2)
        {
            Uri selectedImage = data.getData();
            CropImage.activity(selectedImage)
                    .start(this);
        }
        else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                Bitmap bitmap = BitmapFactory.decodeFile(resultUri.getPath());
                bitmapNew = bitmap;
                profileImg.setImageBitmap(bitmap);


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Log.i("shakti","onacivity===3333");
                Exception error = result.getError();
            }
        }
    }
    private void SaveImage(Bitmap finalBitmap , String name, JSONObject responseObj) {
        Boolean viewCards,isSupervisor,isInspector,isCrewLeader,isContractor,isAdmin;
        File file = new File (storageDirectory, name);
        profileImageUrl = file.toString();
        Log.i("vidisha","ddddddssss"+profileImageUrl);
        //  SaveImage/storage/emulated/0/Android/data/com.irestore.treeinventry.dev/files/Pictures/.TAI/55680.png
 /*       new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {*/
        try {

            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Global.progress.isShowing()) {
            Global.stopProgressBar();
        }
        try {
            if (!sharedPref.getBoolean("userProfileCreated", false)) {
                Integer userID = responseObj.getJSONArray("User").getJSONObject(0).getInt("userId");

                if (profileImageUrl != null)
                    updateImageToUpload(String.valueOf(userID));

                JSONArray permissionsArray_RVA = responseObj.getJSONArray("Permissions");

                if (permissionsArray_RVA.length() != 0) {
                    if (permissionsArray_RVA.getJSONObject(0).has("permissions")) {
                        if (permissionsArray_RVA.getJSONObject(0).getJSONObject("permissions").has("vcManager")) {
                            viewCards = permissionsArray_RVA.getJSONObject(0).getJSONObject("permissions").getJSONObject("vcManager").getBoolean("view");
                            isAdmin = permissionsArray_RVA.getJSONObject(0).getJSONObject("permissions").getJSONObject("vcManager").getBoolean("isAdmin");
                            isSupervisor = permissionsArray_RVA.getJSONObject(0).getJSONObject("permissions").getJSONObject("vcManager").getBoolean("isSupervisor");
                            isInspector = permissionsArray_RVA.getJSONObject(0).getJSONObject("permissions").getJSONObject("vcManager").getBoolean("isInspector");
                            isCrewLeader = permissionsArray_RVA.getJSONObject(0).getJSONObject("permissions").getJSONObject("vcManager").getBoolean("isCrewLeader");
                            isContractor = permissionsArray_RVA.getJSONObject(0).getJSONObject("permissions").getJSONObject("vcManager").getBoolean("isContractor");
                        } else {
                            viewCards = false;
                            isAdmin = false;
                            isSupervisor = false;
                            isInspector = false;
                            isCrewLeader = false;
                            isContractor = true;
                        }
                    } else {
                        viewCards = false;
                        isAdmin = false;
                        isSupervisor = false;
                        isInspector = false;
                        isCrewLeader = false;
                        isContractor = true;
                    }
                } else {
                    viewCards = false;
                    isAdmin = false;
                    isSupervisor = false;
                    isInspector = false;
                    isCrewLeader = false;
                    isContractor = false;
                }

                editor.putString("userID", String.valueOf(userID));
                editor.putBoolean("userProfileCreated", true);
                editor.putBoolean("viewCards", viewCards);
                editor.putBoolean("isAdmin", isAdmin);
                editor.putBoolean("isSupervisor", isSupervisor);
                editor.putBoolean("isInspector", isInspector);
                editor.putBoolean("isCrewLeader", isCrewLeader);
                editor.putBoolean("isContractor", isContractor);
                editor.putString("firstName", firstName.getText().toString().trim());
                editor.putString("lastName", lastName.getText().toString().trim());
                editor.putString("jobTitle", jobTitle.getText().toString().trim());
                editor.putString("organization", organization.getText().toString().trim());
                editor.putString("city", city.getText().toString().trim());
                editor.putString("state", state.getText().toString().trim());
                editor.putString("county", county.getText().toString().trim());
                editor.commit();

                if (sharedPref.getBoolean("adminApprovalRequired", false)) {
                    if (!sharedPref.getString("subscriptionStatus", "").equals("approved")) {
                        Intent intent = new Intent();
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setClass(MyProfile.this, AdminApprovalScreen.
                                class);
                        startActivity(intent);
                    } else {
                        editor.putBoolean("adminApprovalStatus", true);
                        Intent loginIntent = new Intent();
                        loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        loginIntent.setClass(MyProfile.this, TermsConditions.class);
                        editor.putBoolean("ACCEPTANCE", sharedPref.getBoolean("termsAccepted", false));
                        editor.commit();
                        startActivity(loginIntent);
                        // Global.reportSubmitted = true;
                    }
                } else {
                    editor.putBoolean("adminApprovalStatus", true);
                    Intent loginIntent = new Intent();
                    loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    loginIntent.setClass(MyProfile.this, TermsConditions.class);
                    editor.putBoolean("ACCEPTANCE", sharedPref.getBoolean("termsAccepted", false));
                    editor.commit();
                    startActivity(loginIntent);
                    // Global.reportSubmitted = true;
                }

            } else {

                Toast.makeText(MyProfile.this, responseObj.getString("Message"),
                        Toast.LENGTH_SHORT).show();

                editor.putString("firstName", firstName.getText().toString().trim());
                editor.putString("lastName", lastName.getText().toString().trim());
                editor.putString("jobTitle", jobTitle.getText().toString().trim());
                editor.putString("organization", organization.getText().toString().trim());
                editor.putString("city", city.getText().toString().trim());
                editor.putString("state", state.getText().toString().trim());
                editor.putString("county", county.getText().toString().trim());
                editor.commit();
                finish();
                if (profileImageUrl != null)
                    updateImageToUpload(sharedPref.getString("userID", ""));

            }
        }catch(Exception e)

        {

        }
        /*    }
        }, 100);*/


    }

    public void updateImageToUpload(String userId){
        // for(int i=0;i< images_array.size();i++) {

        Log.i("vidisha","fffffffff"+sharedPref.getString("profilePicBucket","")) ;
        TransferObserver transferObserverUser = transferUtility.upload(
                sharedPref.getString("profilePicBucket",""),     /* The bucket to upload to */
                (sharedPref.getString("phoneNumber","") + ".png").replace(sharedPref.getString("phoneNumber",""),userId), new File(profileImageUrl)      /* The key for the uploaded object */
                , CannedAccessControlList.PublicRead    /* The file where the data to upload exists */
        );


        transferObserverListenerUser(transferObserverUser/*,synProcess*/);
        // }
    }

    public void transferObserverListenerUser(final TransferObserver transferObserverUser/*,String progress*/) {
        // final String syncProgress  = progress;

        transferObserverUser.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {

            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                int percentage = (int) (bytesCurrent / bytesTotal * 100);
                Log.e("percentage", percentage + "");

            }

            @Override
            public void onError(int id, Exception ex) {

                Log.e("error", "error");

            }

        });
    }

    @Override
    protected void onResume() {
        super.onResume();


        //fetchLocation();
    }
    private boolean isValidName(String str) {
        // final Pattern pattern = Pattern.compile("[a-zA-z ]+[ '.]?([a-zA-Z ]+)*$");
        final Pattern pattern = Pattern.compile("^[a-zA-Z]+[a-zA-Z.']*$");

        return pattern.matcher(str).matches();
    }

    private boolean isValidTitle(String str) {
        final Pattern pattern = Pattern.compile("[a-zA-z .]+[a-zA-Z .]*");
        return pattern.matcher(str).matches();
    }




    private void sendAndRequestResponse(String updateProfile, HashMap<String, String> params) {

        //RequestQueue initialized
        RequestQueue mRequestQueue = Volley.newRequestQueue(this);

        //String Request initialized
        StringRequest mStringRequest = new StringRequest(!sharedPref.getBoolean("userProfileCreated",false)?Request.Method.POST:Request.Method.PUT, updateProfile, result -> {

            JSONObject responseObject;
            String message;
            boolean error;
            ShowLogs.i(TAG, "response===" + result);

            try {
                if (exception == null && result != null) {

                    try {
                        JSONObject responseObj = new JSONObject(result);

                        if (responseObj.getBoolean("Error")) {
                            Toast.makeText(MyProfile.this, responseObj.getString("Message"),
                                    Toast.LENGTH_SHORT).show();
                            if (Global.progress.isShowing()) {
                                Global.stopProgressBar();
                            }
                        } else {

                            if(bitmapNew!=null)
                                SaveImage(bitmapNew, sharedPref.getString("userID", "") + ".png",responseObj);
                            else
                            {
                                Boolean viewCards,isSupervisor,isInspector,isCrewLeader,isContractor,isAdmin;
                                if (!sharedPref.getBoolean("userProfileCreated", false)) {
                                    Integer userID = responseObj.getJSONArray("User").getJSONObject(0).getInt("userId");

                                    if (profileImageUrl != null)
                                        updateImageToUpload(String.valueOf(userID));

                                    JSONArray permissionsArray_RVA = responseObj.getJSONArray("Permissions");

                                    if (permissionsArray_RVA.length() != 0) {
                                        if (permissionsArray_RVA.getJSONObject(0).has("permissions")) {
                                            if (permissionsArray_RVA.getJSONObject(0).getJSONObject("permissions").has("vcManager")) {
                                                viewCards = permissionsArray_RVA.getJSONObject(0).getJSONObject("permissions").getJSONObject("vcManager").getBoolean("view");
                                                isAdmin = permissionsArray_RVA.getJSONObject(0).getJSONObject("permissions").getJSONObject("vcManager").getBoolean("isAdmin");
                                                isSupervisor = permissionsArray_RVA.getJSONObject(0).getJSONObject("permissions").getJSONObject("vcManager").getBoolean("isSupervisor");
                                                isInspector = permissionsArray_RVA.getJSONObject(0).getJSONObject("permissions").getJSONObject("vcManager").getBoolean("isInspector");
                                                isCrewLeader = permissionsArray_RVA.getJSONObject(0).getJSONObject("permissions").getJSONObject("vcManager").getBoolean("isCrewLeader");
                                                isContractor = permissionsArray_RVA.getJSONObject(0).getJSONObject("permissions").getJSONObject("vcManager").getBoolean("isContractor");
                                            } else {
                                                viewCards = false;
                                                isAdmin = false;
                                                isSupervisor = false;
                                                isInspector = false;
                                                isCrewLeader = false;
                                                isContractor = true;
                                            }
                                        } else {
                                            viewCards = false;
                                            isAdmin = false;
                                            isSupervisor = false;
                                            isInspector = false;
                                            isCrewLeader = false;
                                            isContractor = true;
                                        }
                                    } else {
                                        viewCards = false;
                                        isAdmin = false;
                                        isSupervisor = false;
                                        isInspector = false;
                                        isCrewLeader = false;
                                        isContractor = false;
                                    }
                                    editor.putString("userID", String.valueOf(userID));
                                    editor.putBoolean("userProfileCreated", true);
                                    editor.putBoolean("viewCards", viewCards);
                                    editor.putBoolean("isAdmin", isAdmin);
                                    editor.putBoolean("isSupervisor", isSupervisor);
                                    editor.putBoolean("isInspector", isInspector);
                                    editor.putBoolean("isCrewLeader", isCrewLeader);
                                    editor.putBoolean("isContractor", isContractor);
                                    editor.putString("firstName", firstName.getText().toString().trim());
                                    editor.putString("lastName", lastName.getText().toString().trim());
                                    editor.putString("jobTitle", jobTitle.getText().toString().trim());
                                    editor.putString("organization", organization.getText().toString().trim());
                                    editor.putString("city", city.getText().toString().trim());
                                    editor.putString("state", state.getText().toString().trim());
                                    editor.putString("county", county.getText().toString().trim());
                                    editor.commit();

                                    if (sharedPref.getBoolean("adminApprovalRequired", false)) {
                                        if (!sharedPref.getString("subscriptionStatus", "").equals("approved")) {
                                            Intent intent = new Intent();
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            intent.setClass(MyProfile.this, AdminApprovalScreen.
                                                    class);
                                            startActivity(intent);
                                        } else {
                                            editor.putBoolean("adminApprovalStatus", true);
                                            Intent loginIntent = new Intent();
                                            loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            loginIntent.setClass(MyProfile.this, TermsConditions.class);
                                            editor.putBoolean("ACCEPTANCE", sharedPref.getBoolean("termsAccepted", false));
                                            editor.commit();
                                            startActivity(loginIntent);
                                            //   Global.reportSubmitted = true;
                                        }
                                    } else {
                                        editor.putBoolean("adminApprovalStatus", true);
                                        Intent loginIntent = new Intent();
                                        loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        loginIntent.setClass(MyProfile.this, TermsConditions.class);
                                        editor.putBoolean("ACCEPTANCE", sharedPref.getBoolean("termsAccepted", false));
                                        editor.commit();
                                        startActivity(loginIntent);
                                        //   Global.reportSubmitted = true;
                                    }

                                } else {

                                    Toast.makeText(MyProfile.this, responseObj.getString("Message"),
                                            Toast.LENGTH_SHORT).show();

                                    editor.putString("firstName", firstName.getText().toString().trim());
                                    editor.putString("lastName", lastName.getText().toString().trim());
                                    editor.putString("jobTitle", jobTitle.getText().toString().trim());
                                    editor.putString("organization", organization.getText().toString().trim());
                                    editor.putString("city", city.getText().toString().trim());
                                    editor.putString("state", state.getText().toString().trim());
                                    editor.putString("county", county.getText().toString().trim());
                                    editor.commit();
                                    finish();
                                    if (profileImageUrl != null)
                                        updateImageToUpload(sharedPref.getString("userID", ""));

                                }
                                if (Global.progress.isShowing()) {
                                    Global.stopProgressBar();
                                }

                            }


                        }
                    }catch(Exception e)
                    {

                    }

                } else {
                    Toast.makeText(MyProfile.this, getResources().getString(R.string.server_error),
                            Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }, error -> {

            ShowLogs.i(TAG, "Error :" + error.toString());
            if (Global.progress.isShowing()) {
                Global.stopProgressBar();
            }
        })
        {
                @Override
                protected Map<String,String> getParams(){


                return params;
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