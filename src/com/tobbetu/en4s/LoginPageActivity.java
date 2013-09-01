package com.tobbetu.en4s;

import java.io.IOException;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import com.tobbetu.en4s.backend.EnforceLogin;
import com.tobbetu.en4s.backend.FacebookLogin;
import com.tobbetu.en4s.backend.Login;
import com.tobbetu.en4s.backend.Login.LoginFailedException;
import com.tobbetu.en4s.backend.User;

public class LoginPageActivity extends Activity implements OnClickListener {

    private final String TAG = "LoginPageActivity";

    private Button bLogin = null;
    private EditText etUsername, etPassword;
    private ProgressBar pbLogin = null;

    private LoginButton faceButton = null;
    private String faceAccessToken = null;

    private final String sharedFileName = "loginInfo";
    protected static SharedPreferences loginPreferences;

    private LocationManager lManager = null;
    private LocationListener mlocListener = null;
    private double latitude = 0;
    private double longitude = 0;

    private boolean flag = false;
    private boolean loginFlag = false;
    private boolean locationFlag = false;

    protected static boolean intentCreated = false;

    private AlertDialog alertDialog = null;

    private Handler myLocationHandler = null;
    private Runnable locationRunnable = null;

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
            // startService(new Intent(this, EN4SService.class));

            lManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            mlocListener = new LoginPageLocationListener();
            lManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    0, 0, mlocListener);

            setContentView(R.layout.activity_login_page);
            getActionBar().hide();

            // klavye otomatik olarak cikmayacak.
            getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            etUsername = (EditText) findViewById(R.id.etUsername);
            etPassword = (EditText) findViewById(R.id.etPassword);

            bLogin = (Button) findViewById(R.id.bLogin);
            bLogin.setOnClickListener(this);

            pbLogin = (ProgressBar) findViewById(R.id.pbLogin);
            faceButton = (LoginButton) findViewById(R.id.faceButton);

            loginPreferences = getSharedPreferences(sharedFileName,
                    MODE_PRIVATE);
            if ((loginPreferences.getAll().size() != 0) && flag == false) {
                flag = true;
                if (!loginPreferences.getString("facebook_accessToken", "null")
                        .equals("null")) {
                    // facebook login
                    Log.d(TAG, "trying login with facebook");
                    new LoginTask().execute("facebook", loginPreferences
                            .getString("facebook_email", "NONE"),
                            loginPreferences.getString("facebook_accessToken",
                                    "NONE"));
                } else { // normal login
                    Log.d(TAG, "trying login with username");
                    Log.d(TAG, loginPreferences.getString("username", ""));
                    Log.d(TAG, loginPreferences.getString("password", ""));
                    new LoginTask().execute("enforce",
                            loginPreferences.getString("username", ""),
                            loginPreferences.getString("password", ""));
                }

                loginWithoutCurrentLocation();
            }

            faceButton.setOnErrorListener(new OnErrorListener() {

                @Override
                public void onError(FacebookException error) {
                    Log.i(TAG, "Error " + error.getMessage());
                }
            });

            // facebook izinlerini set ediyoruz.
            faceButton.setReadPermissions(Arrays.asList("basic_info", "email"));

            faceButton.setSessionStatusCallback(new StatusCallback() {

                @Override
                public void call(Session session, SessionState state,
                        Exception exception) {

                    if (session.isOpened()) {
                        faceAccessToken = session.getAccessToken();
                        Log.i(TAG, "Access Token" + session.getAccessToken());

                        Request.executeMeRequestAsync(session,
                                new GraphUserCallback() {

                                    @Override
                                    public void onCompleted(GraphUser user,
                                            Response response) {
                                        if (user != null) {

                                            SharedPreferences.Editor spEditor = loginPreferences
                                                    .edit();
                                            spEditor.putString("facebook_name",
                                                    user.asMap().get("name")
                                                            .toString());
                                            spEditor.putString(
                                                    "facebook_username",
                                                    user.asMap()
                                                            .get("username")
                                                            .toString());
                                            spEditor.putString(
                                                    "facebook_email",
                                                    user.asMap().get("email")
                                                            .toString());
                                            spEditor.putString(
                                                    "facebook_accessToken",
                                                    faceAccessToken);
                                            spEditor.apply();

                                            String userID = user.getId();
                                            String name = user.asMap()
                                                    .get("name").toString();
                                            String username = user.asMap()
                                                    .get("username").toString();
                                            String email = user.asMap()
                                                    .get("email").toString();

                                            Log.i(TAG, userID + "," + name
                                                    + "," + username + ","
                                                    + email);

                                            // loginFlag = true;

                                            new LoginTask()
                                                    .execute(
                                                            "facebook",
                                                            loginPreferences
                                                                    .getString(
                                                                            "facebook_email",
                                                                            "NONE"),
                                                            loginPreferences
                                                                    .getString(
                                                                            "facebook_accessToken",
                                                                            "NONE"));

                                            loginWithoutCurrentLocation();
                                            startIntent();
                                        }
                                    }
                                });

                    }

                }
            });
        }
    }

    @Override
    public void onClick(View arg0) {
        if (arg0.getId() == R.id.bLogin) {
            if (etUsername.getText().toString().equals("")
                    || etPassword.getText().toString().equals("")) {
                Toast.makeText(
                        getApplicationContext(),
                        getResources()
                                .getString(R.string.login_missing_content),
                        Toast.LENGTH_SHORT).show();
            } else {

                new LoginTask().execute("enforce", etUsername.getText()
                        .toString(), etPassword.getText().toString());

                loginWithoutCurrentLocation();
            }
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (intentCreated) {
            myLocationHandler.removeCallbacks(locationRunnable);
            locationRunnable = null;
            myLocationHandler = null;

            lManager.removeUpdates(mlocListener);
            lManager = null;
            mlocListener = null;

            finish();
        }

        if (alertDialog != null)
            alertDialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        Log.e(TAG, "in onBackPressed");

        if (LauncherActivity.firstTimeControlPref.getBoolean("didLogIn", false))
            createAlert();
        else
            super.onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode,
                resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login_page, menu);
        return true;
    }

    class LoginPageLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {

            latitude = loc.getLatitude();
            longitude = loc.getLongitude();

            locationFlag = true;
            startIntent();
        }

        @Override
        public void onProviderDisabled(String arg0) {
        }

        @Override
        public void onProviderEnabled(String arg0) {
        }

        @Override
        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        }

    }

    class LoginTask extends AsyncTask<String, String, User> {

        @Override
        protected void onPreExecute() {
            lockToComponents();
        }

        @Override
        protected User doInBackground(String... arg0) {
            String method = arg0[0];
            String loginArg0 = arg0[1];
            String loginArg1 = arg0[2];

            Log.d(getClass().getName(), "username: " + loginArg0);
            Log.d(getClass().getName(), "passwd: " + loginArg1);

            Login newLogin;
            if (method.equals("facebook"))
                newLogin = new FacebookLogin(loginArg0, loginArg1);
            else
                newLogin = new EnforceLogin(loginArg0, loginArg1);

            try {
                return newLogin.makeRequest();
            } catch (IOException e) {
                cancel(true);

                Log.e(getClass().getName(), "IOException", e);
            } catch (LoginFailedException e) {
                cancel(true);

                Log.e(getClass().getName(), String.format(
                        "[Login Failed] username: %s, passwd: %s", loginArg0,
                        loginArg1), e);
                Log.e(getClass().getName(), "Login olamadik!");
            }
            return null;
        }

        @Override
        protected void onPostExecute(User result) {
            loginFlag = true;
            // to give permission to kill LauncherActivity
            LauncherActivity.shouldKillThisActivity = true;

            startIntent();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            Toast.makeText(LoginPageActivity.this,
                    getResources().getString(R.string.login_failed_msg),
                    Toast.LENGTH_LONG).show();
        }

    }

    private void startIntent() {
        Log.d(getClass().getName(), String.format("Location: %s, Login: %s",
                locationFlag, loginFlag));

        if ((loginPreferences.getAll().size() == 0)) {

            SharedPreferences.Editor preferencesEditor = loginPreferences
                    .edit();
            preferencesEditor.putString("username", etUsername.getText()
                    .toString());
            preferencesEditor.putString("password", etPassword.getText()
                    .toString());
            preferencesEditor.apply();
        }

        if (locationFlag == true && loginFlag == true) {

            intentCreated = true;
            SharedPreferences.Editor editor = LauncherActivity.firstTimeControlPref
                    .edit();
            editor.putBoolean("didLogIn", true);
            editor.apply();

            // after login, we need to stop location listener
            lManager.removeUpdates(mlocListener);

            Intent i = new Intent(LoginPageActivity.this, MainActivity.class);
            i.putExtra("latitude", latitude);
            i.putExtra("longitude", longitude);

            startActivity(i);

            loginFlag = false;

        }
    }

    private void lockToComponents() {

        findViewById(R.id.userInfoLayout).setVisibility(LinearLayout.INVISIBLE);
        findViewById(R.id.breakLayout).setVisibility(LinearLayout.INVISIBLE);
        etUsername.setVisibility(EditText.INVISIBLE);
        etPassword.setVisibility(EditText.INVISIBLE);
        bLogin.setVisibility(Button.INVISIBLE);
        // bRegister.setVisibility(Button.INVISIBLE);
        pbLogin.setVisibility(ProgressBar.VISIBLE);
        faceButton.setVisibility(Button.INVISIBLE);

    }

    private void createAlert() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit ?");
        builder.setMessage("Do you really want to quit ?");
        builder.setCancelable(true);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                try {
                    System.exit(0);
                } catch (Throwable e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton("Cancel",
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

    protected void loginWithoutCurrentLocation() {

        Log.i(TAG, "loginWithoutCurrentLocation is started");

        myLocationHandler = new Handler();

        locationRunnable = new Runnable() {

            @Override
            public void run() {

                try {
                    Location lastLocation = lManager
                            .getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    latitude = lastLocation.getLatitude();
                    longitude = lastLocation.getLongitude();
                } catch (Exception e) {
                    try {
                        Location lastLocation = lManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        latitude = lastLocation.getLatitude();
                        longitude = lastLocation.getLongitude();
                    } catch (Exception ex) {
                        Log.e(TAG,
                                "lastknwon location bile yok (0,0) olarak yolluyorum");
                        latitude = 0;
                        longitude = 0;
                    }
                }

                locationFlag = true;
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.location_failed),
                        Toast.LENGTH_SHORT).show();
                startIntent();
            }
        };

        myLocationHandler.postDelayed(locationRunnable, 10000);
    }
}