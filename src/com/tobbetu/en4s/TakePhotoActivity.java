package com.tobbetu.en4s;

import java.io.ByteArrayOutputStream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.tobbetu.en4s.backend.Image;
import com.tobbetu.en4s.helpers.Preview;
import com.tobbetu.en4s.service.EnforceService;

public class TakePhotoActivity extends Activity implements OnClickListener {

    private FrameLayout previewFrame;
    private LinearLayout previewLayout;
    private ImageView takenPhoto;
    private Button takeButton;
    private Button cancelButton;

    private Preview preview;
    private Handler previewHandler;
    private Runnable previewRunnable;
    private ProgressDialog pg;
    private Bitmap bmp;
    private Image img;
    private double longitude = 0, latitude = 0;

    private byte[] bitmapdata;
    private int deviceOrientation, photoOrientation;
    private OrientationEventListener mOrientationListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);
        getActionBar().hide();

        takeButton = (Button) findViewById(R.id.cameraButton);
        takeButton.setOnClickListener(this);
        cancelButton = (Button) findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(this);

        previewHandler = new Handler();
        previewRunnable = new Runnable() {

            @Override
            public void run() {

                // Log.i(TAG, "run started");
                preview = new Preview(TakePhotoActivity.this);
                if (Preview.pictureWidth == 0 || Preview.pictureHeight == 0) {
                    // Create an alert
                    finish();
                }

                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.x;
                int height = width; // Kare olaca�� i�in height = width

                previewLayout = (LinearLayout) findViewById(R.id.previewLayout);
                previewFrame = new FrameLayout(getBaseContext());

                FrameLayout.LayoutParams frameLayoutParams = new FrameLayout.LayoutParams(
                        width - 30, height - 30);
                previewFrame.setLayoutParams(frameLayoutParams);

                takenPhoto = new ImageView(getBaseContext());
                takenPhoto.setVisibility(ImageView.GONE);

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        width - 30, height - 30, Gravity.CENTER);
                previewLayout.addView(previewFrame, layoutParams);

                previewFrame.addView(preview);
                previewFrame.addView(takenPhoto);
            }
        };
        previewHandler.postDelayed(previewRunnable, 0);
        LinearLayout afterPhotoTakenLayout = (LinearLayout) findViewById(R.id.afterPhotoTakenLayout);
        afterPhotoTakenLayout.setVisibility(LinearLayout.GONE);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.take_photo, menu);
        return true;
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.cameraButton) {
            pg = ProgressDialog.show(TakePhotoActivity.this, null,
                    "Capturing Image..");
            pg.show();
            preview.camera.takePicture(null, null, jpegCallback);

        } else if (v.getId() == R.id.retakeButton) {
            bmp = null;
            img = null;

            takeButton.setVisibility(ImageButton.VISIBLE);
            findViewById(R.id.retakeButton).setVisibility(ImageButton.GONE);
            findViewById(R.id.doneButton).setVisibility(ImageButton.GONE);

            takenPhoto.setVisibility(ImageView.GONE);
            preview.setVisibility(FrameLayout.VISIBLE);
            preview.camera.startPreview();

            // bReTakePhoto.setVisibility(Button.GONE);
            // bTakePhoto.setVisibility(Button.VISIBLE);
        } else if (v.getId() == R.id.doneButton) {

            Intent i = new Intent(this, NewComplaint.class);
            i.putExtra("taken_photo", bitmapdata);
            i.putExtra("user_lat", latitude);
            i.putExtra("user_lng", longitude);
            startActivity(i);
        } else if (v.getId() == R.id.cancelButton) {
            bmp = null;
            img = null;

            cancelDialog();
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

                bmp = Bitmap.createBitmap(bmp, 0, 100, Preview.pictureHeight,
                        Preview.pictureHeight);

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

                takenPhoto.setVisibility(ImageView.VISIBLE);
                takenPhoto.setImageBitmap(bmp);
                preview.setVisibility(FrameLayout.GONE);

                LinearLayout afterPhotoTakenLayout = (LinearLayout) findViewById(R.id.afterPhotoTakenLayout);
                afterPhotoTakenLayout.setVisibility(LinearLayout.VISIBLE);
                findViewById(R.id.retakeButton).setVisibility(
                        ImageButton.VISIBLE);
                findViewById(R.id.doneButton)
                        .setVisibility(ImageButton.VISIBLE);

                takeButton.setVisibility(ImageButton.GONE);

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
                        Intent i = new Intent(TakePhotoActivity.this,
                                MainActivity.class);
                        startActivity(i);
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
        mOrientationListener.disable();
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        cancelButton.performClick();
    }
}
