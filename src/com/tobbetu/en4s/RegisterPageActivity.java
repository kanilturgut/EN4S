package com.tobbetu.en4s;

import java.io.IOException;

import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.tobbetu.en4s.backend.Login;
import com.tobbetu.en4s.backend.Register;
import com.tobbetu.en4s.backend.Register.RegisterFailedException;
import com.tobbetu.en4s.backend.User;
import com.tobbetu.en4s.helpers.BetterAsyncTask;

public class RegisterPageActivity extends Activity {

    private final String TAG = "RegisterPageActivity";
    private EditText etRegisterName, etRegisterSurname, etRegisterEmail,
            etRegisterPassword;

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

                if (etRegisterName.getText().toString().equals("")
                        || etRegisterSurname.getText().toString().equals("")
                        || etRegisterEmail.getText().toString().equals("")
                        || etRegisterPassword.getText().toString().equals("")) {

                    Toast.makeText(
                            getApplicationContext(),
                            getResources().getString(
                                    R.string.login_missing_content),
                            Toast.LENGTH_SHORT).show();
                } else
                    new RegisterTask().execute();
            }
        });
    }

    class RegisterTask extends BetterAsyncTask<Void, User> {

        String name = etRegisterName.getText().toString().trim();
        String surname = etRegisterSurname.getText().toString().trim();
        String email = etRegisterEmail.getText().toString().trim();
        String password = etRegisterPassword.getText().toString().trim();

        @Override
        protected User task(Void... arg0) throws Exception {

            Register reg = new Register(email, name, surname, password);
            reg.register();
            return Login.getMe();
        }

        @Override
        protected void onSuccess(User result) {
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
        protected void onFailure(Exception error) {
            Log.e(TAG, "RegisterTask Failed", error);
            if (error instanceof IOException) {
                BugSenseHandler.sendException(error);
                Toast.makeText(RegisterPageActivity.this,
                        getResources().getString(R.string.network_failed_msg),
                        Toast.LENGTH_LONG).show();
            } else if (error instanceof RegisterFailedException) {
                Toast.makeText(RegisterPageActivity.this,
                        getResources().getString(R.string.reg_failed),
                        Toast.LENGTH_LONG).show();
            } else if (error instanceof JSONException) {
                BugSenseHandler.sendException(error);
                Toast.makeText(RegisterPageActivity.this,
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

    @Override
    protected void onStop() {
        super.onStop();

        finish();
    }
}
