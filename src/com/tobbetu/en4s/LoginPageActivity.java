package com.tobbetu.en4s;

import com.example.tumsiniflar.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		startService(new Intent(this, EN4SService.class));
		
		//Burada sessionID ile HTTP POST yapilacak, olumlu donerse giris olacak
		//olumsuz donerse kullanici adi ve sifre ile yeni bir baglanti gerceklestirilecek ve 
		//yeni sessionID guncellenecek.
		
		loginPreferences = getSharedPreferences(sharedFileName, MODE_PRIVATE);
		if(loginPreferences.getAll().size() != 0){
			Log.e("Login", "1");
			Intent i = new Intent(LoginPageActivity.this, MainActivity.class);
			startActivity(i);
		}
		
		setContentView(R.layout.activity_login_page);
		getActionBar().hide();
		etUsername = (EditText) findViewById(R.id.etUsername);
		etPassword = (EditText) findViewById(R.id.etPassword);
		
		bLogin = (Button) findViewById(R.id.bLogin);
		bLogin.setOnClickListener(loginButtonListener);
		
	}
	
	OnClickListener loginButtonListener = new OnClickListener() {
		public void onClick(View v) {

			if(etUsername.getText().toString().equals("") || etPassword.getText().toString().equals("")) {
				Toast.makeText(getApplicationContext(), "Missing content, please fill username field !", Toast.LENGTH_SHORT).show();
			} else {
				
				/* Share Share Share Preferences */
				SharedPreferences.Editor preferencesEditor = loginPreferences.edit();
				preferencesEditor.putString("username", etUsername.getText().toString());
				preferencesEditor.putString("password", etPassword.getText().toString());
				//=======Session ID ==================================================================
				preferencesEditor.putString("sessionID", "sessionID");
				//=======Session ID ==================================================================				
				preferencesEditor.apply();
				/* Share Share Share Preferences */
				
				Log.e("Login", "2");

				
				//unique bir kullanici mi ?
				Intent i = new Intent(LoginPageActivity.this, MainActivity.class);
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

}
