package com.tobbetu.en4s;

import java.io.IOException;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.TextView;
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
import com.tobbetu.en4s.backend.Login;
import com.tobbetu.en4s.backend.Login.LoginFailedException;

public class LoginPageActivity extends Activity {

	private String TAG = "LoginPageActivity";

	private Button bLogin = null;
	private EditText etUsername, etPassword;
	private ProgressBar pbLogin = null;

	private LoginButton faceButton = null;
	private String faceAccessToken = null;

	private String sharedFileName = "loginInfo";
	protected static SharedPreferences loginPreferences;

	private LocationManager lManager = null;
	private LocationListener mlocListener = null;
	private double latitude = 0;
	private double longitude = 0;

	private boolean flag = false;
	private boolean loginFlag = false;
	private boolean locationFlag = false;
	
	private boolean intentCreated = false;
	
    private AlertDialog alertDialog = null;
    private boolean closeFlag = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!isNetworkAvailable()) {
			Toast.makeText(getApplicationContext(),
					"You have no internet access, please open your network",
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
			etUsername = (EditText) findViewById(R.id.etUsername);
			etPassword = (EditText) findViewById(R.id.etPassword);

			bLogin = (Button) findViewById(R.id.bLogin);
			bLogin.setOnClickListener(loginButtonListener);

			pbLogin = (ProgressBar) findViewById(R.id.pbLogin);
			faceButton = (LoginButton) findViewById(R.id.faceButton);

			loginPreferences = getSharedPreferences(sharedFileName,
					MODE_PRIVATE);
			if ((loginPreferences.getAll().size() != 0) && flag == false) {
				flag = true;
//				if(!loginPreferences.getString("facebook_accessToken", "null").equals("null")) {
//					//facebook login
//					Log.d(TAG, "trying login with facebook");
//				}
//				else { //normal login
					Log.d(TAG, "trying login with username");
					Log.d(TAG, loginPreferences.getString("username", ""));
					Log.d(TAG, loginPreferences.getString("password", ""));
					new LoginTask().execute(
							loginPreferences.getString("username", ""),
							loginPreferences.getString("password", ""));
//				}
			}

			faceButton.setOnErrorListener(new OnErrorListener() {

				@Override
				public void onError(FacebookException error) {
					Log.i(TAG, "Error " + error.getMessage());					
				}
			});

			//facebook izinlerini set ediyoruz.
			faceButton.setReadPermissions(Arrays.asList("basic_info", "email"));

			faceButton.setSessionStatusCallback(new StatusCallback() {

				@Override
				public void call(Session session, SessionState state, Exception exception) {

					if (session.isOpened()) {
						faceAccessToken = session.getAccessToken();
						Log.i(TAG, "Access Token" + session.getAccessToken());

						Request.executeMeRequestAsync(session, new GraphUserCallback() {

							@Override
							public void onCompleted(GraphUser user, Response response) {
								if (user != null) {

									SharedPreferences.Editor spEditor = loginPreferences.edit();
									spEditor.putString("facebook_name", user.asMap().get("name").toString());
									spEditor.putString("facebook_username", user.asMap().get("username").toString());
									spEditor.putString("facebook_email", user.asMap().get("email").toString());
									spEditor.putString("facebook_accessToken", faceAccessToken);
									spEditor.apply();

									String userID = user.getId();
									String name = user.asMap().get("name").toString();
									String username = user.asMap().get("username").toString();
									String email = user.asMap().get("email").toString();

									Log.i(TAG, userID + "," + name + "," + username + "," + email);

								}
							}
						});

					}

				}
			});
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
				preferencesEditor.apply();
				/* Share Share Share Preferences */

				// unique bir kullanici mi ?

				new LoginTask().execute(etUsername.getText().toString(),
						etPassword.getText().toString());
			}
		}
	};

	@Override
	protected void onStop() {
		super.onStop();
		
		if (intentCreated)
			finish();
		
		Log.d(TAG, closeFlag + "");
		
		if (alertDialog != null)
			alertDialog.dismiss();
	}
	
	@Override
	public void onBackPressed() {
		Log.e(TAG, "in onBackPressed");
		createAlert();		
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

	class LoginTask extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			lockToComponents();
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

			intentCreated = true;
			
			//after login, we need to stop location listener
			lManager.removeUpdates(mlocListener);

			Intent i = new Intent(LoginPageActivity.this, MainActivity.class);
			i.putExtra("latitude", latitude);
			i.putExtra("longitude", longitude);

			startActivity(i);

			loginFlag = false;

		} 
	}

	private void lockToComponents() {
		etUsername.setVisibility(EditText.INVISIBLE);
		etPassword.setVisibility(EditText.INVISIBLE);
		bLogin.setVisibility(Button.INVISIBLE);
		pbLogin.setVisibility(ProgressBar.VISIBLE);
		faceButton.setVisibility(Button.INVISIBLE);

	}	
	
	private void createAlert() {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Exit ?");
		builder.setMessage("Do you really want to quit ?");
		builder.setCancelable(true);
		builder.setPositiveButton("OK", 
				new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                try {
                	closeFlag = true;
					System.exit(0);
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        });
		builder.setNegativeButton("Cancel", 
				new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                try {
                	closeFlag = true;
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
}
