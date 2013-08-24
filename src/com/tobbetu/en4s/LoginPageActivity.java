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
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
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

	private String TAG = "LoginPageActivity";

	private Button bLogin = null;
	private Button bRegister = null;
	private EditText etUsername, etPassword;
	private ProgressBar pbLogin = null;
	private User me = null;

	private LoginButton faceButton = null;
	private String faceAccessToken = null;

	private String sharedFileName = "loginInfo";
	protected static SharedPreferences loginPreferences;
	public static SharedPreferences firstTimeControlPref;

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

	private Handler myLocationHandler = null;
	private Runnable locationRunnable = null;

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

			// klavye otomatik olarak cikmayacak.
			getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

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
			}

			etUsername = (EditText) findViewById(R.id.etUsername);
			etPassword = (EditText) findViewById(R.id.etPassword);

			bLogin = (Button) findViewById(R.id.bLogin);
			bRegister = (Button) findViewById(R.id.bRegister);
			bLogin.setOnClickListener(this);
			bRegister.setOnClickListener(this);

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

					// burasi degisecek, facebook ile connection yazilinca iptal
					// ediyorum burayi.
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

				new LoginTask().execute("enforce", etUsername.getText()
						.toString(), etPassword.getText().toString());

				loginWithoutCurrentLocation();
			}
		} else if (arg0.getId() == R.id.bRegister) {
			Intent i = new Intent(this, RegisterPageActivity.class);
			startActivity(i);
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
						"[Login Failed] username: %s, passwd: %s", loginArg0,
						loginArg1), e);
				Log.e(getClass().getName(), "Login olamadik!");
			}
			return null;
		}

		@Override
		protected void onPostExecute(User result) {
			loginFlag = true;
			me = result;
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

			// after login, we need to stop location listener
			lManager.removeUpdates(mlocListener);

			Intent i = new Intent(LoginPageActivity.this, MainActivity.class);
			i.putExtra("latitude", latitude);
			i.putExtra("longitude", longitude);
			i.putExtra("user", me);

			startActivity(i);

			loginFlag = false;

		}
	}

	private void lockToComponents() {
		etUsername.setVisibility(EditText.INVISIBLE);
		etPassword.setVisibility(EditText.INVISIBLE);
		bLogin.setVisibility(Button.INVISIBLE);
		bRegister.setVisibility(Button.INVISIBLE);
		pbLogin.setVisibility(ProgressBar.VISIBLE);
		faceButton.setVisibility(Button.INVISIBLE);

	}

	private void createAlert() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Exit ?");
		builder.setMessage("Do you really want to quit ?");
		builder.setCancelable(true);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
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

	protected void loginWithoutCurrentLocation() {

		Log.i(TAG, "loginWithoutCurrentLocation is started");

		myLocationHandler = new Handler();

		locationRunnable = new Runnable() {

			@Override
			public void run() {
				Location lastLocation = lManager
						.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				latitude = lastLocation.getLatitude();
				longitude = lastLocation.getLongitude();
				locationFlag = true;
				Toast.makeText(getApplicationContext(),
						"We couldn't get your current location!",
						Toast.LENGTH_SHORT).show();
				startIntent();
			}
		};

		myLocationHandler.postDelayed(locationRunnable, 10000);
	}
}
