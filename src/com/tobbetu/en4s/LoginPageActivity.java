package com.tobbetu.en4s;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.tobbetu.en4s.backend.EnforceLogin;
import com.tobbetu.en4s.backend.FacebookLogin;
import com.tobbetu.en4s.backend.Login;
import com.tobbetu.en4s.backend.Login.LoginFailedException;
import com.tobbetu.en4s.backend.User;
import com.tobbetu.en4s.helpers.BetterAsyncTask;
import com.tobbetu.en4s.service.EnforceService;

public class LoginPageActivity extends Activity implements OnClickListener {

    private final String TAG = "LoginPageActivity";

    private Button bLogin = null;
    private EditText etUsername, etPassword;
    private ProgressBar pbLogin = null;

    private LoginButton faceButton = null;
    private String faceAccessToken = null;

    private final String sharedFileName = "loginInfo";
    protected static SharedPreferences loginPreferences;

    protected static boolean intentCreated = false;
    private LoginTask loginTask = null;

    private AlertDialog alertDialog = null;

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

            if (!EnforceService.getGPSStatus())
                buildAlertMessageNoGps();

            BugSenseHandler.initAndStartSession(LoginPageActivity.this,
                    getResources().getString(R.string.bugSense_API_KEY));

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
            if ((loginPreferences.getAll().size() != 0)) {
                if (!loginPreferences.getString("facebook_accessToken", "null")
                        .equals("null")) {
                    // facebook login
                    Log.d(TAG, "trying login with facebook");
                    Log.d(TAG, loginPreferences.getString("facebook_email",
                            "username@facebook.com"));
                    Log.d(TAG, loginPreferences.getString(
                            "facebook_accessToken", ""));

                    loginTask = new LoginTask();
                    loginTask.execute("facebook", loginPreferences.getString(
                            "facebook_email", "NONE"), loginPreferences
                            .getString("facebook_accessToken", "NONE"));

                } else { // normal login
                    Log.d(TAG, "trying login with username");
                    Log.d(TAG, loginPreferences.getString("username", ""));
                    Log.d(TAG, loginPreferences.getString("password", ""));

                    loginTask = new LoginTask();
                    loginTask.execute("enforce",
                            loginPreferences.getString("username", ""),
                            loginPreferences.getString("password", ""));
                }
            }

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

                SharedPreferences.Editor preferencesEditor = loginPreferences
                        .edit();
                preferencesEditor.putString("username", etUsername.getText()
                        .toString());
                preferencesEditor.putString("password", etPassword.getText()
                        .toString());
                preferencesEditor.apply();

                loginTask = new LoginTask();
                loginTask.execute("enforce", etUsername.getText().toString(),
                        etPassword.getText().toString());

            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (intentCreated) {

            if (EnforceService.getGPSStatus()) {
                turnGPSOff(this);
            }

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

    class LoginTask extends BetterAsyncTask<String, User> {

        @Override
        protected void onPreExecute() {
            lockToComponents();
        }

        @Override
        protected User task(String... arg0) throws Exception {
            String method = arg0[0];
            String loginArg0 = arg0[1];
            String loginArg1 = arg0[2];

            Log.d("LoginTask", "username: " + loginArg0);
            Log.d("LoginTask", "passwd: " + loginArg1);

            Login newLogin;
            if (method.equals("facebook"))
                newLogin = new FacebookLogin(loginArg0, loginArg1);
            else
                newLogin = new EnforceLogin(loginArg0, loginArg1);

            return newLogin.makeRequest();
        }

        @Override
        protected void onSuccess(User result) {
            // to give permission to kill LauncherActivity
            LauncherActivity.shouldKillThisActivity = true;

            startIntent();
        }

        @Override
        protected void onFailure(Exception error) {
            Log.e("LoginTask", "Failed to login: " + error.getMessage(), error);
            if (error instanceof IOException) {
                BugSenseHandler.sendException(error);
                Toast.makeText(LoginPageActivity.this,
                        getResources().getString(R.string.network_failed_msg),
                        Toast.LENGTH_LONG).show();
            } else if (error instanceof LoginFailedException) {
                Toast.makeText(LoginPageActivity.this,
                        getResources().getString(R.string.login_failed_msg),
                        Toast.LENGTH_LONG).show();

                loginPreferences.edit().clear().commit();
                intentCreated = true;
                Intent i = new Intent(LoginPageActivity.this,
                        LoginPageActivity.class);
                startActivity(i);
            } else if (error instanceof JSONException) {
                BugSenseHandler.sendException(error);
                Toast.makeText(LoginPageActivity.this,
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

    private void startIntent() {

        if (loginTask != null)
            loginTask.cancel(true);

        intentCreated = true;
        SharedPreferences.Editor editor = LauncherActivity.firstTimeControlPref
                .edit();
        editor.putBoolean("didLogIn", true);
        editor.apply();

        Intent i = new Intent(LoginPageActivity.this, MainActivity.class);
        startActivity(i);
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
        builder.setTitle(R.string.ma_quit_title);
        builder.setMessage(R.string.ma_quit_msg);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.ma_quit_ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            Utils.turnGPSOff(getApplicationContext());
                            System.exit(0);
                        } catch (Throwable e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });
        builder.setNegativeButton(R.string.ma_quit_cancel,
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

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(
                getResources().getString(R.string.login_gps_disabled))
                .setCancelable(false)
                .setPositiveButton(
                        getResources().getString(R.string.login_yes_openGPS),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                    final int id) {
                                startActivity(new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }
                        })
                .setNegativeButton(
                        getResources().getString(R.string.login_no_closeGPS),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                    final int id) {
                                dialog.cancel();
                            }
                        });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private static void turnGPSOff(Context c) {
        Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
        intent.putExtra("enabled", false);
        c.sendBroadcast(intent);

        String provider = Settings.Secure.getString(c.getContentResolver(),
                Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (provider.contains("gps")) { // if gps is enabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings",
                    "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            c.sendBroadcast(poke);
        }
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

                    // loginFlag = true;

                    Log.e(TAG, "fabutton");
                    loginTask = new LoginTask();
                    loginTask.execute("facebook", email, faceAccessToken);
                }
            }
        };
    };
}