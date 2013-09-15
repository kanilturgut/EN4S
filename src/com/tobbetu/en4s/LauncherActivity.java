package com.tobbetu.en4s;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.tobbetu.en4s.service.EnforceService;

public class LauncherActivity extends Activity implements OnClickListener {

    private final String TAG = "LauncherActivity";
    public static SharedPreferences firstTimeControlPref;
    private AlertDialog alertDialog = null;

    protected static boolean shouldKillThisActivity = false;

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
            setContentView(R.layout.activity_launcher);
            getActionBar().hide();

            startService(new Intent(this, EnforceService.class));
            /*
             * Bu blok, program cihaza yuklendikten sonra sadece 1 kere
             * calisacak ve yeni sikayet ekleme ekraninda kullanacagimiz,
             * cihazin hangi boyutta fotograf cekecegini (800x600, 1024x768 ...)
             * belirleyen bilgileri bulacak.
             */
            firstTimeControlPref = getSharedPreferences("firstTimeController",
                    MODE_PRIVATE);
            if (firstTimeControlPref.getBoolean("isThisFirstTime", true)
                    || !firstTimeControlPref.getBoolean("didLogIn", false)) {

                Log.i(TAG,
                        "Bir daha burayi gormeyeceksin. Eger gorursen yanlis birsey var demektir.");

                int[] sizes = Utils.deviceSupportedScreenSize();

                SharedPreferences.Editor firstTimeEditor = firstTimeControlPref
                        .edit();
                firstTimeEditor.putBoolean("isThisFirstTime", false);
                firstTimeEditor.putInt("deviceWidth", sizes[0]);
                firstTimeEditor.putInt("deviceHeight", sizes[1]);

                firstTimeEditor.apply();

                findViewById(R.id.bLauncherSignup).setOnClickListener(this);
                findViewById(R.id.bLauncherLogin).setOnClickListener(this);
            } else {
                Intent i = new Intent(this, LoginPageActivity.class);
                startActivity(i);
            }
        }
    }

    @Override
    public void onClick(View v) {
        Intent i = null;

        if (v.getId() == R.id.bLauncherLogin)
            i = new Intent(this, LoginPageActivity.class);
        else
            i = new Intent(this, RegisterPageActivity.class);

        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        createAlert();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // if (shouldKillThisActivity)
        finish();

        if (alertDialog != null)
            alertDialog.dismiss();
    }

    private void createAlert() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.ma_quit_title));
        builder.setMessage(getResources().getString(R.string.ma_quit_msg));
        builder.setCancelable(true);
        builder.setPositiveButton(
                getResources().getString(R.string.ma_quit_ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        try {
                            Utils.turnGPSOff(LauncherActivity.this);
                            stopService(new Intent(LauncherActivity.this,
                                    EnforceService.class));
                            System.exit(0);
                        } catch (Throwable e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });
        builder.setNegativeButton(
                getResources().getString(R.string.ma_quit_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        try {
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