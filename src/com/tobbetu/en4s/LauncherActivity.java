package com.tobbetu.en4s;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.facebook.FacebookException;
import com.facebook.Request;
import com.facebook.Request.GraphUserCallback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.LoginButton.OnErrorListener;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.tobbetu.en4s.backend.EnforceLogin;
import com.tobbetu.en4s.backend.FacebookLogin;
import com.tobbetu.en4s.backend.Login;
import com.tobbetu.en4s.backend.Login.LoginFailedException;
import com.tobbetu.en4s.backend.Requests;
import com.tobbetu.en4s.backend.User;
import com.tobbetu.en4s.helpers.BetterAsyncTask;
import com.tobbetu.en4s.helpers.Geocoder;
import com.tobbetu.en4s.service.EnforceService;

public class LauncherActivity extends Activity implements OnClickListener {

    private final String TAG = "LauncherActivity";

    public static SharedPreferences firstTimeControlPref;
    protected static SharedPreferences loginPreferences;
    private final String sharedFileName = "loginInfo";

    private AlertDialog alertDialog = null;

    private String faceAccessToken = null;
    private LoginButton faceButton = null;

    private LoginTask loginTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!Utils.isNetworkAvailable(this)) {
            Toast.makeText(
                    getApplicationContext(),
                    getResources()
                            .getString(R.string.no_internet_access_dialog),
                    Toast.LENGTH_LONG).show();
            finish();
        } else {

            setContentView(R.layout.splash);
            getActionBar().hide();

            startService(new Intent(this, EnforceService.class));

            loginPreferences = getSharedPreferences(sharedFileName,
                    MODE_PRIVATE);
            /*
             * Bu blok, program cihaza yuklendikten sonra sadece 1 kere
             * calisacak ve yeni sikayet ekleme ekraninda kullanacagimiz,
             * cihazin hangi boyutta fotograf cekecegini (800x600, 1024x768 ...)
             * belirleyen bilgileri bulacak.
             */
            firstTimeControlPref = getSharedPreferences("firstTimeController",
                    MODE_PRIVATE);
            if (firstTimeControlPref.getBoolean("isThisFirstTime", true)) {

                Log.i(TAG,
                        "Bir daha burayi gormeyeceksin. Eger gorursen yanlis birsey var demektir.");

                int[] sizes = Utils.deviceSupportedScreenSize();

                SharedPreferences.Editor firstTimeEditor = firstTimeControlPref
                        .edit();
                firstTimeEditor.putBoolean("isThisFirstTime", false);
                firstTimeEditor.putInt("deviceWidth", sizes[0]);
                firstTimeEditor.putInt("deviceHeight", sizes[1]);
                firstTimeEditor.apply();

                setupComponents();

            } else {

                if (loginPreferences.getAll().size() != 0) {
                    if (!LauncherActivity.loginPreferences.getString(
                            "facebook_accessToken", "null").equals("null")) {
                        // facebook login
                        Log.d(TAG, "trying login with facebook");
                        Log.d(TAG, LauncherActivity.loginPreferences.getString(
                                "facebook_email", "username@facebook.com"));
                        Log.d(TAG, LauncherActivity.loginPreferences.getString(
                                "facebook_accessToken", ""));

                        loginTask = new LoginTask();
                        loginTask.execute("facebook",
                                LauncherActivity.loginPreferences.getString(
                                        "facebook_email", "NONE"),
                                LauncherActivity.loginPreferences.getString(
                                        "facebook_accessToken", "NONE"));

                    } else { // normal login
                        Log.d(TAG, "trying login with username");
                        Log.d(TAG, LauncherActivity.loginPreferences.getString(
                                "username", ""));
                        Log.d(TAG, LauncherActivity.loginPreferences.getString(
                                "password", ""));

                        loginTask = new LoginTask();
                        loginTask.execute("enforce",
                                LauncherActivity.loginPreferences.getString(
                                        "username", ""),
                                LauncherActivity.loginPreferences.getString(
                                        "password", ""));
                    }
                } else {
                    setupComponents();
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        Intent i = null;

        if (v.getId() == R.id.bLauncherLogin)
            i = new Intent(this, LoginPageActivity.class);
        else
            i = new Intent(this, RegisterPageActivity.class);

        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        createAlert();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (alertDialog != null)
            alertDialog.dismiss();

        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode,
                resultCode, data);
    }

    private void createAlert() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.ma_quit_title));
        builder.setMessage(getResources().getString(R.string.ma_quit_msg));
        builder.setCancelable(true);
        builder.setPositiveButton(
                getResources().getString(R.string.ma_quit_ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        try {
                            Utils.turnGPSOff(LauncherActivity.this);
                            stopService(new Intent(LauncherActivity.this,
                                    EnforceService.class));
                        } catch (Throwable e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });
        builder.setNegativeButton(
                getResources().getString(R.string.ma_quit_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        try {
                            dialog.dismiss();
                        } catch (Throwable e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });

        alertDialog = builder.create();
        alertDialog.show();
    }

    private final StatusCallback facebookCalback = new StatusCallback() {

        @Override
        public void call(Session session, SessionState state,
                Exception exception) {

            if (session.isOpened()
                    && loginPreferences.getString("facebook_accessToken",
                            "NONE").equals("NONE")) {
                faceAccessToken = session.getAccessToken();
                Log.i(TAG, "Access Token " + session.getAccessToken());
                Request.executeMeRequestAsync(session, userCallback);
            }
        }

        private final GraphUserCallback userCallback = new GraphUserCallback() {

            @Override
            public void onCompleted(GraphUser user, Response response) {
                if (user != null) {
                    Map<String, Object> userMap = user.asMap();
                    String userID = user.getId();
                    String name = user.getName();
                    String username = user.getUsername();
                    String email = null;

                    if (userMap.containsKey("email")) {
                        email = userMap.get("email").toString();
                    } else {
                        Log.d(TAG, "Facebook email was null");
                        email = username + "@facebook.com";
                        Log.d(TAG, "Facebook email -> " + email);
                    }

                    Log.i(TAG, userID + "," + name + "," + username + ","
                            + email);

                    SharedPreferences.Editor spEditor = loginPreferences.edit();
                    spEditor.putString("facebook_name", name);
                    spEditor.putString("facebook_username", username);
                    spEditor.putString("facebook_email", email);
                    spEditor.putString("facebook_accessToken", faceAccessToken);
                    spEditor.apply();

                    loginTask = new LoginTask();
                    loginTask.execute("facebook", email, faceAccessToken);
                }
            }
        };
    };

    class LoginTask extends BetterAsyncTask<String, User> {

        @Override
        protected void onPreExecute() {
            // lockToComponents(); bir turlu dogru duzgun calismadi.
        }

        @Override
        protected User task(String... arg0) throws Exception {
            String method = arg0[0];
            String loginArg0 = arg0[1];
            String loginArg1 = arg0[2];

            GoogleCloudMessaging gcm = GoogleCloudMessaging
                    .getInstance(getApplicationContext());

            String regId = null;

            try {
                regId = gcm.register(getApplicationContext().getResources()
                        .getString(R.string.gcm_SENDER_ID));
            } catch (IOException e) {
                Log.e("GCM", "IOException while registering", e);
            }

            Log.d("LoginTask", "username: " + loginArg0);
            Log.d("LoginTask", "passwd: " + loginArg1);

            Login newLogin;
            if (method.equals("facebook"))
                newLogin = new FacebookLogin(loginArg0, loginArg1, regId);
            else
                newLogin = new EnforceLogin(loginArg0, loginArg1, regId);

            User user = newLogin.makeRequest();
            try {
                JSONObject city = new JSONObject();
                Location location = EnforceService.getLocation();
                String cityName = Geocoder.getCity(location.getLatitude(),
                        location.getLongitude());
                Log.d(TAG, "Current City: " + cityName);
                city.put("current_city", Utils.slugify(cityName));
                Requests.put("/user/updatecity", city.toString());
            } catch (Exception e) {
                Log.e(TAG, "Current City", e);
            }

            return user;
        }

        @Override
        protected void onSuccess(User result) {

            if (loginTask != null)
                loginTask.cancel(true);

            Intent i = new Intent(LauncherActivity.this, ListActivity.class);
            startActivity(i);
        }

        @Override
        protected void onFailure(Exception error) {
            Log.e("LoginTask", "Failed to login: " + error.getMessage(), error);
            if (error instanceof IOException) {
                BugSenseHandler.sendException(error);
                Toast.makeText(LauncherActivity.this,
                        getResources().getString(R.string.network_failed_msg),
                        Toast.LENGTH_LONG).show();
            } else if (error instanceof LoginFailedException) {
                Toast.makeText(LauncherActivity.this,
                        getResources().getString(R.string.login_failed_msg),
                        Toast.LENGTH_LONG).show();

                loginPreferences.edit().clear().commit();
                Intent i = new Intent(LauncherActivity.this,
                        LauncherActivity.class);
                startActivity(i);
            } else if (error instanceof JSONException) {
                BugSenseHandler.sendException(error);
                Toast.makeText(LauncherActivity.this,
                        getResources().getString(R.string.api_changed),
                        Toast.LENGTH_LONG).show();
            } else {
                // Unexpected Failure
                BugSenseHandler
                        .sendEvent("Unexpected Failure in RegisterPageActivity");
                BugSenseHandler.sendException(error);
            }

        }
    }

    /*
     * Kullanici facebook ile login olmadan login page yada register page e
     * gider, yine herhangi bir login yada register islemi yapmadan launcher
     * activity e donerse, gerekli layout ve component duzenlemeleri yapilmali.
     */
    private void setupComponents() {

        setContentView(R.layout.activity_launcher);
        findViewById(R.id.bLauncherSignup).setOnClickListener(this);
        findViewById(R.id.bLauncherLogin).setOnClickListener(this);
        faceButton = (LoginButton) findViewById(R.id.faceButtonOnLauncherActivity);

        faceButton.setOnErrorListener(new OnErrorListener() {

            @Override
            public void onError(FacebookException error) {
                Log.i(TAG, "Error " + error.getMessage());
            }
        });

        // facebook izinlerini set ediyoruz.
        faceButton.setReadPermissions(Arrays.asList("basic_info", "email"));

        faceButton.setSessionStatusCallback(facebookCalback);
    }
}