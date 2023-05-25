package com.example.mylibrary;

import static com.example.mylibrary.ToasterMessage.sharedPreferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;


/**
 * Created by SecurAX on 5/25/2016.
 */
public class STAsyncHttpConnection extends AsyncTask<String, Void, String> {
    /**
     * Override this method to perform a computation on a background thread. The
     * specified parameters are the parameters passed to {@link #execute}
     * by the caller of this task.
     * <p>
     * This method can call {@link #publishProgress} to publish updates
     * on the UI thread.
     *
     * @param params The parameters of the task.
     * @return A result, defined by the subclass of this task.
     * @see #onPreExecute()
     * @see #onPostExecute
     * @see #publishProgress
     */
    private static final char PARAMETER_DELIMITER = '&';
    private static final char PARAMETER_EQUALS_CHAR = '=';
    private static final int CONNECTION_TIMEOUT = 3000;
    Context appContext;
    String requestType,safetyMessage,authToken;
    ImageView progressView;
    View bgView;
    String httpPostData,directURL="";
    Exception exception;

    public STAsyncHttpConnection(Context currentAppContext) {
        this.appContext = currentAppContext;
    }
   /* public STAsyncHttpConnection(Context currentAppContext, HashMap<String, String> postData) {
        this.appContext = currentAppContext;

        this.httpPostData = STAsyncHttpConnection.createQueryStringForParameters(postData);
    }*/

    @Override
    protected String doInBackground(String... params) {
        String result = null, urlStr = params[0];
        HttpURLConnection urlConnection = null;
        exception = null;

        try {
            URL url = new URL(urlStr);
            Log.i("STAsyncHttpConnection", "URL: " + urlStr);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);

            urlConnection.setRequestProperty ("x-account-key", "general");
            urlConnection.setRequestProperty ("x-access-token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJpUmVzdG9yZSIsImF1ZCI6ImdlbmVyYWwiLCJzdWIiOiJhdXRoZW50aWNhdGlvbiIsImlhdCI6MTQ2OTA4NzYwMX0.48fKAGieV0j9kQDu9wlaLi6a1B837nuCqthJmROSy-0");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setRequestProperty("x-application", "PDM");
            urlConnection.setRequestProperty("x-user", sharedPreferences.getString("phoneNumber",""));
            urlConnection.setRequestMethod("GET");


            int statusCode = urlConnection.getResponseCode();
            Log.i("STAsyncHttpConnection***", "URL: " + statusCode);
            if (statusCode == HttpURLConnection.HTTP_OK) {
                //Get Response
                InputStream is = urlConnection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer();
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                }
                rd.close();
                result = response.toString();

            } else if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                result = "{\"Error\" : true, \"Message\" : \"Unauthorized user\"}";
            }
            else if (statusCode == HttpURLConnection.HTTP_CLIENT_TIMEOUT||statusCode == HttpURLConnection.HTTP_GATEWAY_TIMEOUT) {
                result = "{\"Error\" : true, \"Message\" : \"Request Timed out. Please try again later\"}";
            }else {
                result = "{\"Error\" : true, \"Message\" : \"Server Error, please try again later\"}";
            }
        } catch (MalformedURLException ex) {
            exception = ex;
            Log.e("SocketTimeout exception", ex.toString());
        } catch (SocketTimeoutException ex) {
            exception = ex;
            Log.e("SocketTimeout exception", ex.toString());
        } catch (IOException ez) {
            exception = ez;
            ez.printStackTrace();
            Log.e("IO exception", ez.toString());
        } catch (Exception ez) {
            exception = ez;
            Log.e("exception", ez.toString());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return result;
    }

    @Override
    protected void onPreExecute() {
    }
    @Override
    protected void onPostExecute(String result) {
        JSONObject responseObject,responseData= null;
        String message;
        boolean error ;

        try {
            if (result != null) {

                responseObject = new JSONObject(result);
                error = responseObject.getBoolean("Error");
               // message = responseObject.get("Message").toString();

                 if(!error)
                 {


                     SharedPreferences.Editor editor = sharedPreferences.edit();
                     editor.putString( "fullResponseData", result.toString() );
                     editor.commit();

                     if (appContext.getClass().equals(MainActivity.class)) {
                         ((MainActivity) appContext).processWebServiceResponse();
                     }
                 }else
                {
                    message = responseObject.get("Message").toString();
                    Toast.makeText(appContext, message,
                            Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(appContext, appContext.getResources().getString(R.string.server_error),
                        Toast.LENGTH_SHORT).show();
            }
            if (Global.progress.isShowing()) {
                Global.stopProgressBar();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

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
}
