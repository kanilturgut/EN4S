package com.tobbetu.en4s;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.tobbetu.en4s.backend.Login;
import com.tobbetu.en4s.backend.Login.LoginFailedException;

public class LoginPageActivity extends Activity {

	private Button bLogin = null;
	private EditText etUsername, etPassword;
	private ProgressBar pbLogin = null;

	private String sharedFileName = "loginInfo";
	protected static SharedPreferences loginPreferences;

	private LocationManager lManager = null;
	private double latitude = 0;
	private double longitude = 0;

	private boolean flag = false;
	private boolean loginFlag = false;
	private boolean locationFlag = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!isNetworkAvailable()) {
			Toast.makeText(getApplicationContext(),
					"You have no internet access, please open your network",
					Toast.LENGTH_SHORT).show();
			finish();
		} else {

			// startService(new Intent(this, EN4SService.class));

			lManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			LocationListener mlocListener = new LoginPageLocationListener();
			lManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
					0, 0, mlocListener);

			// Burada sessionID ile HTTP POST yapilacak, olumlu donerse giris
			// olacak
			// olumsuz donerse kullanici adi ve sifre ile yeni bir baglanti
			// gerceklestirilecek ve
			// yeni sessionID guncellenecek.

			setContentView(R.layout.activity_login_page);
			getActionBar().hide();
			etUsername = (EditText) findViewById(R.id.etUsername);
			etPassword = (EditText) findViewById(R.id.etPassword);

			bLogin = (Button) findViewById(R.id.bLogin);
			bLogin.setOnClickListener(loginButtonListener);
			
			pbLogin = (ProgressBar) findViewById(R.id.pbLogin);

			loginPreferences = getSharedPreferences(sharedFileName,
					MODE_PRIVATE);
			if ((loginPreferences.getAll().size() != 0) && flag == false) {
				flag = true;
				
				new LoginTask().execute(
						loginPreferences.getString("username", ""),
						loginPreferences.getString("password", ""));
			}
		}
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
								
				new LoginTask().execute(etUsername.getText().toString(),
						etPassword.getText().toString());
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

	class LoginTask extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			lockToScreen();
		}
		
		@Override
		protected String doInBackground(String... arg0) {
			String username = arg0[0];
			String passwd = arg0[1];

            Log.d(getClass().getName(), "username: " + username);
            Log.d(getClass().getName(), "passwd: " + passwd);

			Login newLogin = new Login(username, passwd);
			try {
				newLogin.makeRequest();
			} catch (IOException e) {
				Toast.makeText(
						getApplicationContext(),
						"You have no internet access, please open your network !",
						Toast.LENGTH_SHORT).show();
				Log.e(getClass().getName(), "IOException", e);

			} catch (LoginFailedException e) {
				Toast.makeText(
						getApplicationContext(),
						"Your username or password is wrong, please check your information !",
						Toast.LENGTH_SHORT).show();
				Log.e(getClass().getName(), String.format(
						"[Login Failed] username: %s, passwd: %s", username,
						passwd), e);
				Log.e(getClass().getName(), "Login olamadik!");
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			loginFlag = true;
			startIntent();
		}

	}

	// class LocationTask extends AsyncTask<String, String, String> {
	//
	// @Override
	// protected String doInBackground(String... arg0) {
	// // TODO kanil will implement here
	// return null;
	// }
	//
	// @Override
	// protected void onPostExecute(String result) {
	// locationFlag = true;
	// startIntent();
	// }
	// }

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null;
	}

	private void startIntent() {
	    Log.d(getClass().getName(), String.format("Location: %s, Login: %s",
	            locationFlag, loginFlag));
		if (locationFlag == true && loginFlag == true) {

			Intent i = new Intent(LoginPageActivity.this, MainActivity.class);
			i.putExtra("latitude", latitude);
			i.putExtra("longitude", longitude);

			startActivity(i);

			loginFlag = false;

		} else {
			// ikisi birden false gelirse, biraz bekleyip yeniden bakmaliyiz.
		}
	}
	
	private void lockToScreen() {
		
		etUsername.setVisibility(View.INVISIBLE);
		etPassword.setVisibility(View.INVISIBLE);
		pbLogin.setVisibility(View.VISIBLE);
		bLogin.setVisibility(View.INVISIBLE);

		
	}

}
