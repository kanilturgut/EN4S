package com.tobbetu.en4s.complaint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import com.bugsense.trace.BugSenseHandler;
import com.google.analytics.tracking.android.EasyTracker;
import com.tobbetu.en4s.LauncherActivity;
import com.tobbetu.en4s.R;
import com.tobbetu.en4s.backend.Image;
import com.tobbetu.en4s.service.EnforceService;

public class TakePhotoActivity extends Activity implements OnClickListener, SurfaceHolder.Callback {

    Context context;
    private ImageView takenPhoto;
    private ImageView takeButton;
    private Button cancelButton;
    ImageButton flashImage;
    public static boolean isLightOn = false;


    private ProgressDialog pg;
    private Bitmap bmp;
    private Image img;
    private double longitude = 0, latitude = 0;

    private byte[] bitmapdata;
    private int deviceOrientation, photoOrientation;
    private OrientationEventListener mOrientationListener = null;
    private Camera camera;

    public static int pictureWidth;
    public static int pictureHeight;
    SurfaceHolder mHolder;
    SurfaceView surfaceView;
    private Camera.Parameters parameters;
    LinearLayout llHeaderBack;
    RelativeLayout rlCameraOptionsLayout;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);
        context = this;
        getActionBar().hide();

        pictureWidth = LauncherActivity.firstTimeControlPref.getInt(
                "deviceWidth", 0);
        pictureHeight = LauncherActivity.firstTimeControlPref.getInt(
                "deviceHeight", 0);

        surfaceView = (SurfaceView) findViewById(R.id.surfaceview);
        mHolder = surfaceView.getHolder();

        if (mHolder != null) {
            mHolder.addCallback(this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        takenPhoto = (ImageView) findViewById(R.id.ivTakenPhoto);

        takeButton = (ImageView) findViewById(R.id.cameraButton);
        takeButton.setOnClickListener(this);
        //cancelButton = (Button) findViewById(R.id.cancelButton);
        //cancelButton.setOnClickListener(this);

        rlCameraOptionsLayout = (RelativeLayout) findViewById(R.id.rlCameraOptionsLayout);
        llHeaderBack = (LinearLayout) findViewById(R.id.llHeaderBack);
        llHeaderBack.setOnClickListener(this);

        flashImage = (ImageButton) findViewById(R.id.flashImage);
        flashImage.setOnClickListener(this);

        PackageManager packageManager = context.getPackageManager();
        if (packageManager != null) {
            if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                flashImage.setVisibility(View.INVISIBLE);
            }
        }

        if (pictureWidth == 0 || pictureHeight == 0) {
            // Create an alert
            Toast.makeText(context, R.string.nc_screen_size_doesnt_match, Toast.LENGTH_LONG).show();
            finish();
        }

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = width; // Kare olacagi icin height = width olmali.

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        surfaceView.setLayoutParams(layoutParams);

        takenPhoto.setLayoutParams(layoutParams);

        RelativeLayout afterPhotoTakenLayout = (RelativeLayout) findViewById(R.id.afterPhotoTakenLayout);
        afterPhotoTakenLayout.setVisibility(View.GONE);

        findViewById(R.id.retakeButton).setOnClickListener(this);
        findViewById(R.id.doneButton).setOnClickListener(this);

        mOrientationListener = new OrientationEventListener(this,
                SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                deviceOrientation = orientation;
            }
        };
        if (mOrientationListener.canDetectOrientation())
            mOrientationListener.enable();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.take_photo, menu);
        return true;
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.cameraButton) {
            pg = ProgressDialog.show(TakePhotoActivity.this, null,
                    getString(R.string.capturing));
            pg.show();
            camera.takePicture(null, null, jpegCallback);

        } else if (v.getId() == R.id.retakeButton) {
            bmp = null;
            img = null;

            takeButton.setVisibility(ImageButton.VISIBLE);
            rlCameraOptionsLayout.setVisibility(View.VISIBLE);
            findViewById(R.id.retakeButton).setVisibility(ImageButton.GONE);
            findViewById(R.id.doneButton).setVisibility(ImageButton.GONE);

            takenPhoto.setVisibility(ImageView.GONE);

            camera.startPreview();

        } else if (v.getId() == R.id.doneButton) {

            Intent i = new Intent(this, NewComplaint.class);
            i.putExtra("taken_photo", bitmapdata);
            i.putExtra("user_lat", latitude);
            i.putExtra("user_lng", longitude);
            startActivity(i);
        } else if (v.getId() == R.id.llHeaderBack) {
            bmp = null;
            img = null;

            cancelDialog();
        } else if (v == flashImage) {

            if (isLightOn) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(parameters);
                camera.startPreview();
                isLightOn = false;

                flashImage.setImageResource(R.drawable.ic_action_flash_off);
            } else {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(parameters);
                camera.startPreview();
                isLightOn = true;

                flashImage.setImageResource(R.drawable.ic_action_flash_on);
            }

        }
    }

    /** Handles data for jpeg picture */
    @SuppressLint("NewApi")
    PictureCallback jpegCallback = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            if (data != null) {

                // foto cekildigi anda cihazin dur
                photoOrientation = deviceOrientation;

                bmp = BitmapFactory.decodeByteArray(data, 0, data.length);

                Log.e("Bitmap1",
                        "width : " + bmp.getWidth() + ", height : "
                                + bmp.getHeight() + " ,bitmap.size : "
                                + (bmp.getByteCount() / 1000000) + " mb");

                if (bmp.getHeight() < bmp.getWidth()) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
                            bmp.getHeight(), matrix, true);
                }

                Log.e("Bitmap2",
                        "width : " + bmp.getWidth() + ", height : "
                                + bmp.getHeight() + " ,bitmap.size : "
                                + (bmp.getByteCount() / 1000000) + " mb");

                bmp = Bitmap.createBitmap(bmp, 0, 100, pictureHeight,
                        pictureHeight);

                if (pg != null)
                    pg.dismiss();

                // fotograf yatay cekildi ise tekrar ceviriyoruz
                if (260 < photoOrientation && photoOrientation < 280) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(-90);
                    bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
                            bmp.getHeight(), matrix, true);
                }

                ByteArrayOutputStream os = new ByteArrayOutputStream();
                bmp.compress(CompressFormat.JPEG, 90, os);

                img = new Image();
                img.setBmp(bmp);

                // silinecek
                bitmapdata = os.toByteArray();
                // bmp = BitmapFactory.decodeByteArray(array, 0, array.length);

                Log.e("Bitmap3",
                        "width : " + bmp.getWidth() + ", height : "
                                + bmp.getHeight() + " ,bitmap.size : "
                                + bmp.getByteCount() / 1000000.0 + " mb "
                                + bitmapdata.length / 1000000.0);

                takenPhoto.setVisibility(View.VISIBLE);
                takenPhoto.setImageBitmap(bmp);


                RelativeLayout afterPhotoTakenLayout = (RelativeLayout) findViewById(R.id.afterPhotoTakenLayout);
                afterPhotoTakenLayout.setVisibility(View.VISIBLE);
                findViewById(R.id.retakeButton).setVisibility(View.VISIBLE);
                findViewById(R.id.doneButton).setVisibility(View.VISIBLE);

                takeButton.setVisibility(View.GONE);
                rlCameraOptionsLayout.setVisibility(View.INVISIBLE);

                // Lokasyonu, kullanici fotografi cektigi anda alalim. Ornegin
                // seyahat halinde fotograf ceken bir kisi, hizli sekilde title
                // yazamazsa konum bilgisi degisip sikayetin yerini yanlis set
                // etmis olmayiz.
                latitude = EnforceService.getLocation().getLatitude();
                longitude = EnforceService.getLocation().getLongitude();
            }
        }
    };
    private AlertDialog alertDialog;

    private void cancelDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.tp_iptal);
        builder.setMessage(R.string.abort_new_complaint);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.ma_quit_ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        finish();
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

    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);

        mOrientationListener.disable();
        if (alertDialog != null) {
            alertDialog.dismiss();
        }

        finish();
    }

    @Override
    public void onBackPressed() {
        llHeaderBack.performClick();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (camera != null)
            camera.release();

        try {
            camera = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();

            BugSenseHandler.sendExceptionMessage(
                    "on Preview --> surfaceCreated", "Camera.open() failed", e);
            // calismayi bitirmeli yoksa uygulama crash eder.
            return;
        }

        camera.setDisplayOrientation(90);
        parameters = camera.getParameters();
        parameters.setPictureSize(pictureWidth, pictureHeight);

        List<String> focusMode = parameters.getSupportedFocusModes();
        for (String focus: focusMode) {
            if (focus.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            else if (focus.equals(Camera.Parameters.FOCUS_MODE_AUTO))
                parameters.setFocusMode(Camera.Parameters.FLASH_MODE_AUTO);
        }

        camera.setParameters(parameters);
        camera.startPreview();

        try {
            camera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            e.printStackTrace();

            BugSenseHandler.sendExceptionMessage(
                    "on Preview --> surfaceCreated",
                    "camera.setPreviewDisplay", e);
        }

        Log.i("Preview", "surfaceCreated");

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
        try {
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();

            BugSenseHandler.sendExceptionMessage(
                    "on Preview --> surfaceChanged", "camera.startPreview();",
                    e);
        }

        Log.i("Preview", "surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (camera != null) {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
        Log.i("Preview", "surfaceDestroyed");
    }
}
