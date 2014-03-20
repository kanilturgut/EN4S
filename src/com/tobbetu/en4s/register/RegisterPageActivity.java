package com.tobbetu.en4s.register;

import java.io.IOException;

import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.google.analytics.tracking.android.EasyTracker;
import com.tobbetu.en4s.LauncherActivity;
import com.tobbetu.en4s.R;
import com.tobbetu.en4s.backend.User;
import com.tobbetu.en4s.helpers.BetterAsyncTask;
import com.tobbetu.en4s.login.Login;
import com.tobbetu.en4s.login.LoginPageActivity;
import com.tobbetu.en4s.register.Register.RegisterFailedException;

public class RegisterPageActivity extends Activity implements
        OnEditorActionListener {

    private final String TAG = "RegisterPageActivity";
    private EditText etRegisterName, etRegisterSurname, etRegisterEmail,
            etRegisterPassword;

    private Button bSignup;

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
        etRegisterPassword.setOnEditorActionListener(this);

        bSignup = (Button) findViewById(R.id.bSignup);
        bSignup.setOnClickListener(new OnClickListener() {
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

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
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
        EasyTracker.getInstance(this).activityStop(this);

        finish();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, LauncherActivity.class));
    }

    @Override
    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
        if (arg0.getId() == etRegisterPassword.getId()
                && arg1 == EditorInfo.IME_ACTION_SEND) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etRegisterPassword.getWindowToken(), 0);
            bSignup.performClick();
            return true;
        }
        return false;
    }
}
