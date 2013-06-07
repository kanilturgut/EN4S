package com.tobbetu.en4s;

import java.io.IOException;

import com.tobbetu.en4s.backend.Login;
import com.tobbetu.en4s.backend.Login.LoginFailedException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginPageActivity extends Activity {

    private Button bLogin = null;
    private EditText etUsername, etPassword;

    private String sharedFileName = "loginInfo";
    protected static SharedPreferences loginPreferences;

    private LocationManager lManager = null;
    private double latitude = 0;
    private double longitude = 0;

    private boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // startService(new Intent(this, EN4SService.class));

        lManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        LocationListener mlocListener = new LoginPageLocationListener();
        lManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
                mlocListener);

        // Burada sessionID ile HTTP POST yapilacak, olumlu donerse giris olacak
        // olumsuz donerse kullanici adi ve sifre ile yeni bir baglanti
        // gerceklestirilecek ve
        // yeni sessionID guncellenecek.

        setContentView(R.layout.activity_login_page);
        getActionBar().hide();
        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);

        bLogin = (Button) findViewById(R.id.bLogin);
        bLogin.setOnClickListener(loginButtonListener);

    }

    OnClickListener loginButtonListener = new OnClickListener() {
        public void onClick(View v) {

            if (etUsername.getText().toString().equals("")
                    || etPassword.getText().toString().equals("")) {
                Toast.makeText(getApplicationContext(),
                        "Missing content, please fill username field !",
                        Toast.LENGTH_SHORT).show();
            } else {

                /* Share Share Share Preferences */
                SharedPreferences.Editor preferencesEditor = loginPreferences
                        .edit();
                preferencesEditor.putString("username", etUsername.getText()
                        .toString());
                preferencesEditor.putString("password", etPassword.getText()
                        .toString());
                // =======Session ID
                // ==================================================================
                preferencesEditor.putString("sessionID", "sessionID");
                // =======Session ID
                // ==================================================================
                preferencesEditor.apply();
                /* Share Share Share Preferences */

                // unique bir kullanici mi ?
                Intent i = new Intent(LoginPageActivity.this,
                        MainActivity.class);
                i.putExtra("latitude", latitude);
                i.putExtra("longitude", longitude);
                startActivity(i);
            }
        }
    };

    protected void onPause() {
        super.onPause();
        finish();
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login_page, menu);
        return true;
    }

    class LoginPageLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location arg0) {

            latitude = arg0.getLatitude();
            longitude = arg0.getLongitude();

            loginPreferences = getSharedPreferences(sharedFileName,
                    MODE_PRIVATE);
            if ((loginPreferences.getAll().size() != 0) && flag == false) {

                flag = true;

                Intent i = new Intent(LoginPageActivity.this,
                        MainActivity.class);
                i.putExtra("latitude", latitude);
                i.putExtra("longitude", longitude);
                new LoginTask().execute(
                        loginPreferences.getString("username", ""),
                        loginPreferences.getString("password", ""));
                startActivity(i);
            }

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

    class LoginTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... arg0) {
            String username = arg0[0];
            String passwd = arg0[1];
            Login newLogin = new Login(username, passwd);
            try {
                newLogin.makeRequest();
            } catch (IOException e) {
                // TODO user have no internet connection
                Log.e(getClass().getName(), "IOException", e);

            } catch (LoginFailedException e) {
                // TODO inform user
                Log.e(getClass().getName(), String.format(
                        "[Login Failed] username: %s, passwd: %s", username,
                        passwd), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO kanil will implement here
        }

    }

}
