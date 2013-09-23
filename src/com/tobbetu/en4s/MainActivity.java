package com.tobbetu.en4s;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.tobbetu.en4s.service.EnforceService;

public class MainActivity extends FragmentActivity {

    private final Handler handler = new Handler();

    private PagerSlidingTabStrip tabs;
    private ViewPager pager;
    private MyPagerAdapter adapter;

    private Drawable oldBackground = null;
    private int currentColor = 0xFF666666;

    private AlertDialog alertDialog = null;

    private final String TAG = "MainActivity";

    protected boolean switchedFromGPSActivity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        pager = (ViewPager) findViewById(R.id.pager);
        adapter = new MyPagerAdapter(getSupportFragmentManager());

        pager.setAdapter(adapter);

        final int pageMargin = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                        .getDisplayMetrics());
        pager.setPageMargin(pageMargin);

        tabs.setViewPager(pager);

        changeColor(currentColor);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        Log.e(TAG, "in onBackPressed");
        createAlert();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (alertDialog != null)
            alertDialog.dismiss();
    }

    @Override
    protected void onResume() {
        if (switchedFromGPSActivity) {
            switchedFromGPSActivity = false;
            Intent i = new Intent(MainActivity.this, TakePhotoActivity.class);
            startActivity(i);
        }
        super.onResume();
    }

    private void createAlert() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.ma_quit_title);
        builder.setMessage(R.string.ma_quit_msg);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.ma_quit_ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Utils.turnGPSOff(MainActivity.this);
                        stopService(new Intent(MainActivity.this,
                                EnforceService.class));
                        System.exit(0);
                    }
                });
        builder.setNegativeButton(R.string.ma_quit_cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        alertDialog = builder.create();
        alertDialog.show();
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.login_gps_disabled)
                .setCancelable(false)
                .setPositiveButton(R.string.login_yes_openGPS,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                    final int id) {
                                switchedFromGPSActivity = true;
                                startActivity(new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }
                        })
                .setNegativeButton(R.string.login_no_closeGPS,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                    final int id) {
                                dialog.cancel();
                                Intent i = new Intent(MainActivity.this,
                                        TakePhotoActivity.class);
                                startActivity(i);
                            }
                        });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

        case R.id.action_contact:

            if (EnforceService.getLocation().getLatitude() == 0
                    && EnforceService.getLocation().getLongitude() == 0) {
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.ma_no_location),
                        Toast.LENGTH_LONG).show();
            } else {
                if (!EnforceService.getGPSStatus()) {
                    buildAlertMessageNoGps();
                } else {
                    Intent i = new Intent(MainActivity.this,
                            TakePhotoActivity.class);
                    startActivity(i);
                }
            }
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("NewApi")
    private void changeColor(int newColor) {

        tabs.setIndicatorColor(newColor);

        // change ActionBar color just if an ActionBar is available
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            Drawable colorDrawable = new ColorDrawable(newColor);
            Drawable bottomDrawable = getResources().getDrawable(
                    R.drawable.actionbar_bottom);
            LayerDrawable ld = new LayerDrawable(new Drawable[] {
                    colorDrawable, bottomDrawable });

            if (oldBackground == null) {

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    ld.setCallback(drawableCallback);
                } else {
                    // getActionBar().setBackgroundDrawable(ld);
                }

            } else {

                TransitionDrawable td = new TransitionDrawable(new Drawable[] {
                        oldBackground, ld });

                // workaround for broken ActionBarContainer drawable handling on
                // pre-API 17 builds
                // https://github.com/android/platform_frameworks_base/commit/a7cc06d82e45918c37429a59b14545c6a57db4e4
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    td.setCallback(drawableCallback);
                } else {
                    // getActionBar().setBackgroundDrawable(td);
                }

                td.startTransition(200);

            }

            oldBackground = ld;

            // http://stackoverflow.com/questions/11002691/actionbar-setbackgrounddrawable-nulling-background-from-thread-handler
            getActionBar().setDisplayShowTitleEnabled(false);
            getActionBar().setDisplayShowTitleEnabled(true);

        }

        currentColor = newColor;

    }

    public void onColorClicked(View v) {

        int color = Color.parseColor(v.getTag().toString());
        changeColor(color);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentColor", currentColor);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentColor = savedInstanceState.getInt("currentColor");
        changeColor(currentColor);
    }

    private final Drawable.Callback drawableCallback = new Drawable.Callback() {
        @SuppressLint("NewApi")
        @Override
        public void invalidateDrawable(Drawable who) {
            getActionBar().setBackgroundDrawable(who);
        }

        @Override
        public void scheduleDrawable(Drawable who, Runnable what, long when) {
            handler.postAtTime(what, when);
        }

        @Override
        public void unscheduleDrawable(Drawable who, Runnable what) {
            handler.removeCallbacks(what);
        }
    };

    public class MyPagerAdapter extends FragmentPagerAdapter {

        private final String[] TITLES = { getString(R.string.title_hot),
                getString(R.string.title_new), getString(R.string.title_near),
                getString(R.string.title_top) };

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public Fragment getItem(int position) {
            return TabCreator.newInstance(position);
        }

    }

}