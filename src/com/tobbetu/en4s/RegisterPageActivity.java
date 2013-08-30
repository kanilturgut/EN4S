package com.tobbetu.en4s;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.tobbetu.en4s.backend.Register;
import com.tobbetu.en4s.backend.Register.RegisterFailedException;
import com.tobbetu.en4s.backend.User;

public class RegisterPageActivity extends Activity {

	private final String TAG = "RegisterPageActivity";
	private EditText etRegisterName, etRegisterSurname, etRegisterEmail, etRegisterPassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_page);

		getActionBar().hide();
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		etRegisterName = (EditText) findViewById(R.id.etRegisterName);
		etRegisterSurname = (EditText) findViewById(R.id.etRegisterSurname);
		etRegisterEmail = (EditText) findViewById(R.id.etRegisterEmail);
		etRegisterPassword = (EditText) findViewById(R.id.etRegisterPassword);

		findViewById(R.id.bSignup).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				new RegisterTask().execute();
			}
		});
	}

	class RegisterTask extends AsyncTask<String, String, User> {

		String name = etRegisterName.getText().toString().trim();
		String surname = etRegisterSurname.getText().toString().trim();
		String email = etRegisterEmail.getText().toString().trim();
		String password = etRegisterPassword.getText().toString().trim();

		@Override
		protected User doInBackground(String... arg0) {

			Register reg = new Register(email, name, surname, password);
			try {
				reg.register();
			} catch (IOException e) {
				e.printStackTrace();
				Log.e(TAG, "failed because of IOException", e);
				//Need to stop registration task
				cancel(true);

			} catch (RegisterFailedException e) {
				e.printStackTrace();
				Log.e(TAG, "failed because of RegisterFailedException", e);
				//Need to stop registration task
				cancel(true);
			}

			return null;
		}

		@Override
		protected void onPostExecute(User result) {

			// to give permission to kill LauncherActivity
			LauncherActivity.shouldKillThisActivity = true;

			SharedPreferences sp = getSharedPreferences("loginInfo",
					MODE_PRIVATE);
			SharedPreferences.Editor editor = sp.edit();
			editor.putString("username", email);
			editor.putString("password", password);
			editor.apply();

			Intent i = new Intent(RegisterPageActivity.this,
					LoginPageActivity.class);
			startActivity(i);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			Toast.makeText(RegisterPageActivity.this, "Oops", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();

		finish();
	}
}
