package com.tobbetu.en4s;

import java.io.IOException;

import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.facebook.Session;
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

            if ((LauncherActivity.loginPreferences.getAll().size() != 0)) {
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
            }
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

                SharedPreferences.Editor preferencesEditor = LauncherActivity.loginPreferences
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

        finish();

        if (alertDialog != null)
            alertDialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, LauncherActivity.class));
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

                LauncherActivity.loginPreferences.edit().clear().commit();
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

        Intent i = new Intent(LoginPageActivity.this, MainActivity.class);
        startActivity(i);
    }

    private void lockToComponents() {

        findViewById(R.id.userInfoLayout).setVisibility(LinearLayout.INVISIBLE);
        etUsername.setVisibility(EditText.INVISIBLE);
        etPassword.setVisibility(EditText.INVISIBLE);
        bLogin.setVisibility(Button.INVISIBLE);
        pbLogin.setVisibility(ProgressBar.VISIBLE);

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
}