package com.example.mylibrary;

import static com.example.mylibrary.ToasterMessage.APIKey;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GPSTracker extends Service implements LocationListener {

    private Context mContext = null;

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 1 meter

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000; // 1s

    // Declaring a Location Manager
    protected LocationManager locationManager;

    private static final int TWO_MINUTES = 1000;//1s
    public GPSTracker()
    {

    }

    public GPSTracker(Context context) {
        this.mContext = context;
        getLocation();
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @SuppressLint("MissingPermission")
    public Location getLocation() {
        if (location == null) {
            try {
                locationManager = (LocationManager) mContext
                        .getSystemService(LOCATION_SERVICE);

                // getting GPS status
                isGPSEnabled = locationManager
                        .isProviderEnabled(LocationManager.GPS_PROVIDER);

                // getting network status
                isNetworkEnabled = locationManager
                        .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (!isGPSEnabled && !isNetworkEnabled) {
                    // no network provider is enabled
                } else {
                    this.canGetLocation = true;
                    // if GPS Enabled get lat/long using GPS Services
                    if (isGPSEnabled) {
//                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            Global.currentLocation = location;

                            if (location != null) {
                                if (isBetterLocation(location, Global.currentLocation)) {
                                    latitude = Global.currentLocation.getLatitude();
                                    longitude =  Global.currentLocation.getLongitude();
                                    String url;



                                    url = "https://maps.googleapis.com/maps/api/geocode/json?latlng="
                                            + latitude + "," + longitude + "&sensor=false"+ "&key=" +APIKey;


                                    DownloadTask downloadTask = new DownloadTask();

                                    // Start downloading the geocoding places
                                    downloadTask.execute(url);
                                    // getAddressByGpsCoordinates(String.valueOf(longitude),String.valueOf(latitude));
                                    //fetchAddress();
                                    System.out.println("GPS lat long" + latitude + longitude
                                            + "Accuracy   " + location.getAccuracy());
                                    stopUsingGPS();
                                }
                            }
                        }
//                    }
                    }
                    // First get location from Network Provider
                    if (isNetworkEnabled) {
                        if (location == null) {
                            locationManager.requestLocationUpdates(
                                    LocationManager.NETWORK_PROVIDER,
                                    MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                            if (locationManager != null) {
                                location = locationManager
                                        .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                                Global.currentLocation = location;
                                Log.i("CPManagerAndroid","22222"+Global.currentLocation);
                                if (location != null) {
                                    if (isBetterLocation(location, Global.currentLocation)) {
                                        latitude = Global.currentLocation.getLatitude();
                                        longitude =  Global.currentLocation.getLongitude();

                                        //  Log.i("CPManagerAndroid","Utils.currentLocation"+Utils.currentLocation.getLatitude());
                                        //fetchAddress();
                                        String url;

                                        url = "https://maps.googleapis.com/maps/api/geocode/json?latlng="
                                                + latitude + "," + longitude + "&sensor=false"+ "&key=" +APIKey;


                                        DownloadTask downloadTask = new DownloadTask();

                                        // Start downloading the geocoding places
                                        downloadTask.execute(url);
                                        // getAddressByGpsCoordinates(String.valueOf(longitude),String.valueOf(latitude));

                                        stopUsingGPS();
                                    }
                                    else
                                    {
                                        latitude = Global.currentLocation.getLatitude();
                                        longitude =  Global.currentLocation.getLongitude();

                                        //  Log.i("CPManagerAndroid","Utils.currentLocation"+Utils.currentLocation.getLatitude());
                                        //fetchAddress();
                                        String url;

                                        url = "https://maps.googleapis.com/maps/api/geocode/json?latlng="
                                                + latitude + "," + longitude + "&sensor=false"+ "&key="+ APIKey;


                                        DownloadTask downloadTask = new DownloadTask();

                                        // Start downloading the geocoding places
                                        downloadTask.execute(url);
                                    }
                                }
                            }
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            stopUsingGPS();
        }


        return location;
    }



    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }

            data = sb.toString();
            br.close();

        }catch(Exception e){
            Log.i("Exception url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }

        return data;
    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {

        String data = null;


        // Invoked by execute() method of this object
        @Override
        protected String doInBackground(String... url) {
            try{
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String result){

            //  Log.i("CPManagerAndroid","onpost"+result);

            try {
                JSONObject responseObject = new JSONObject(result);
                String address = responseObject.getJSONArray("results").getJSONObject(0).getString("formatted_address");
                Global.addressString = address;




            }catch(Exception e)
            {
                Log.i("iRestore","onPostExecute"+e);
            }
        }
    }



    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else return isNewer && !isSignificantlyLessAccurate && isFromSameProvider;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     * */
    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(GPSTracker.this);
        }
    }

    /**
     * Function to get latitude
     * */
    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }

    /**
     * Function to get longitude
     * */
    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }

    /**
     * Function to check GPS/wifi enabled
     * @return boolean
     * */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     * */
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("Alert");

        // Setting Dialog Message
        alertDialog.setMessage("Please enable location services on your phone");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}