package com.tobbetu.en4s;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.tobbetu.en4s.backend.User;

public class RegisterPageActivity extends Activity {

	private EditText etRegisterName, etRegisterEmail, etRegisterPassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_page);

		getActionBar().hide();

		etRegisterName = (EditText) findViewById(R.id.etRegisterName);
		etRegisterEmail = (EditText) findViewById(R.id.etRegisterEmail);
		etRegisterPassword = (EditText) findViewById(R.id.etRegisterPassword);

		findViewById(R.id.bRegisterSend).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Toast.makeText(getApplicationContext(),
						"Kayýt iþlemi yapýlacak", Toast.LENGTH_SHORT).show();
			}
		});
	}

	class RegisterTask extends AsyncTask<String, String, User> {

		String name = etRegisterName.getText().toString();
		String email = etRegisterEmail.getText().toString();
		String password = etRegisterPassword.getText().toString();
		
		@Override
		protected User doInBackground(String... arg0) {



			return null;
		}

		@Override
		protected void onPostExecute(User result) {

			SharedPreferences.Editor editor = LoginPageActivity.loginPreferences
					.edit();
			editor.putString("username", email);
			editor.putString("password", password);
			editor.apply();
			
			Intent i = new Intent(RegisterPageActivity.this, LoginPageActivity.class);
			startActivity(i);
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		finish();
	}
}
