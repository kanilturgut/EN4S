package com.tobbetu.en4s;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.tobbetu.en4s.backend.Complaint;
import com.tobbetu.en4s.backend.Image;
import com.tobbetu.en4s.helpers.CategoryI18n;
import com.tobbetu.en4s.helpers.Preview;

public class NewComplaint extends Activity implements OnClickListener {

    private Button bPush, bTakePhoto, bReTakePhoto, bImproveLocation;
    private ImageView ivTakenPhoto;
    private EditText etComplaintTitle;
    private TextView tvNewComplaintAdress;
    private Spinner categoriesSpinner;
    private ProgressDialog progressDialog = null;

    private GoogleMap myMap;
    private LatLng position = null;
    private double latitude = 0;
    private double longitude = 0;

    private Image img = null;
    private Bitmap bmp = null;
    private byte[] bitmapdata = null;

    private Complaint newComplaint = null;
    private String category;
    private int selectedCategoryIndex = 0;

    private String TAG = "NewComplaint";

    private Preview preview;
    ProgressDialog pg = null;

    private OrientationEventListener mOrientationListener = null;
    private int deviceOrientation, photoOrientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_complaint);
        getActionBar().hide();

        // klavye kendi kendine acilmayacak...Oh beeee :D
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int frameWidth = size.x;
        Log.e("frameWidth", frameWidth + "");
        double birsey = (frameWidth / 3.0);
        int frameHeight = (int) (birsey * 4.0);

        Log.i("ekran degerleri", frameWidth + "," + birsey + "," + frameHeight);

        preview = new Preview(this);

        if (Preview.pictureWidth == 0 || Preview.pictureHeight == 0) {

            // Create an alert

            finish();
        }

        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
                frameWidth, frameWidth);
        findViewById(R.id.photoButtonLL).setLayoutParams(llParams);

        ((FrameLayout) findViewById(R.id.fLPreview)).addView(preview);
        findViewById(R.id.fLPreview).setVisibility(View.VISIBLE);

        String savedComplainTitle = getIntent()
                .getStringExtra("complaintTitle");
        selectedCategoryIndex = getIntent().getIntExtra("complaintCategory", 0);

        latitude = getIntent().getDoubleExtra("user_lat", 0);
        longitude = getIntent().getDoubleExtra("user_lng", 0);

        final TextView tvCount = (TextView) findViewById(R.id.tvWordCount);
        etComplaintTitle = (EditText) findViewById(R.id.etNewComplaint);
        etComplaintTitle.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                tvCount.setText(100 - etComplaintTitle.getText().length() + "");
            }
        });

        tvNewComplaintAdress = (TextView) findViewById(R.id.tvNewComplaintAdress);
        ivTakenPhoto = (ImageView) findViewById(R.id.ivTakenPhoto);

        bTakePhoto = (Button) findViewById(R.id.bTakeIt);
        bReTakePhoto = (Button) findViewById(R.id.bReTake);
        bPush = (Button) findViewById(R.id.bPush);
        bImproveLocation = (Button) findViewById(R.id.bImproveLoc);

        bTakePhoto.setOnClickListener(this);
        bReTakePhoto.setOnClickListener(this);
        bPush.setOnClickListener(this);
        bImproveLocation.setOnClickListener(this);

        byte[] savedImage = getIntent().getByteArrayExtra("complaintImage");
        Bitmap savedBitmap = null;
        if (savedImage != null) {
            savedBitmap = BitmapFactory.decodeByteArray(savedImage, 0,
                    savedImage.length);

            img = new Image();
            img.setBmp(savedBitmap);

            // resim cekildikten sonra ikinci kez haritadan konum secilirse
            // mevcut foograf korunmali.
            bitmapdata = savedImage;

            ivTakenPhoto.setVisibility(ImageView.VISIBLE);
            findViewById(R.id.fLPreview).setVisibility(View.GONE);
            bTakePhoto.setVisibility(Button.GONE);
            bReTakePhoto.setVisibility(Button.VISIBLE);
            ivTakenPhoto.setImageBitmap(savedBitmap);

            savedBitmap = null;
        }

        myMap = ((MapFragment) getFragmentManager().findFragmentById(
                R.id.mapNewComplaint)).getMap();
        myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        position = new LatLng(latitude, longitude);

        Utils.addAMarker(myMap, position, false);
        Utils.centerAndZomm(myMap, position, 15);

        tvNewComplaintAdress.setText(Utils.getAddress(getBaseContext(),
                position));

        categoriesSpinner = (Spinner) findViewById(R.id.spinnerNewComplaintCategory);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter
                .createFromResource(this, R.array.categories,
                        android.R.layout.simple_spinner_item);
        spinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoriesSpinner.setAdapter(spinnerAdapter);
        categoriesSpinner
                .setOnItemSelectedListener(new OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1,
                            int arg2, long arg3) {
                        category = arg0.getItemAtPosition(arg2).toString();
                        selectedCategoryIndex = arg2;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                        category = getString(R.string.cat_select);
                        selectedCategoryIndex = 0;

                    }
                });

        etComplaintTitle.setText(savedComplainTitle);
        categoriesSpinner.setSelection(selectedCategoryIndex);

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
    protected void onStop() {
        super.onStop();
        mOrientationListener.disable();
        finish();
        Log.d(TAG, "in onStop");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_complaint, menu);
        return true;
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.bTakeIt) {
            pg = ProgressDialog.show(NewComplaint.this, null,
                    "Capturing Image..");
            pg.show();
            preview.camera.takePicture(null, null, jpegCallback);
        } else if (v.getId() == R.id.bReTake) {
            bmp = null;
            img = null;

            ivTakenPhoto.setVisibility(ImageView.GONE);
            findViewById(R.id.fLPreview).setVisibility(FrameLayout.VISIBLE);
            preview.camera.startPreview();

            bReTakePhoto.setVisibility(Button.GONE);
            bTakePhoto.setVisibility(Button.VISIBLE);

        } else if (v.getId() == R.id.bPush) {

            if (etComplaintTitle.getText().toString().equals(""))
                Toast.makeText(getApplicationContext(),
                        "You have to fill title!", Toast.LENGTH_SHORT).show();
            else if (selectedCategoryIndex == 0) {
                createAlertToChooseACategory();
            } else {

                progressDialog = ProgressDialog
                        .show(NewComplaint.this, "Loading",
                                "Your complaint is sending. Thank you for your patience");

                newComplaint = new Complaint();
                newComplaint.setTitle(etComplaintTitle.getText().toString());
                newComplaint.setAddress(Utils.getAddress(getBaseContext(),
                        position));
                newComplaint.setCity(Utils.getCity(getBaseContext(), position));
                newComplaint.setCategory(CategoryI18n
                        .getEnglishName(selectedCategoryIndex));
                newComplaint.setLatitude(latitude);
                newComplaint.setLongitude(longitude);

                Log.d("title", newComplaint.getTitle());
                Log.d("category", newComplaint.getCategory());
                Log.d("address", newComplaint.getAddress());
                Log.d("city", newComplaint.getCity());
                Log.d("location", newComplaint.getLatitude() + ","
                        + newComplaint.getLongitude());

                new SaveTask().execute();
            }
        } else { // bImroveLocation
            Intent biggerMapIntent = new Intent(NewComplaint.this,
                    BiggerMap.class);
            biggerMapIntent.putExtra("LatLng_Lat", latitude);
            biggerMapIntent.putExtra("LatLng_Lng", longitude);
            biggerMapIntent.putExtra("complaintTitle", etComplaintTitle
                    .getText().toString());
            biggerMapIntent
                    .putExtra("complaintCategory", selectedCategoryIndex);
            biggerMapIntent.putExtra("complaintImage", bitmapdata);
            Log.d(TAG, "onMapClick intent started");
            startActivity(biggerMapIntent);
        }

    }

    /** Handles data for jpeg picture */
    PictureCallback jpegCallback = new PictureCallback() {
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

                ivTakenPhoto.setVisibility(ImageView.VISIBLE);
                findViewById(R.id.fLPreview).setVisibility(View.GONE);
                bTakePhoto.setVisibility(Button.GONE);
                bReTakePhoto.setVisibility(Button.VISIBLE);
                ivTakenPhoto.setImageBitmap(bmp);
            }
        }
    };

    private void createAlertToChooseACategory() {

        AlertDialog.Builder alt_dlg_bld = new AlertDialog.Builder(this);
        alt_dlg_bld.setTitle(R.string.nc_cat_missing_title);
        alt_dlg_bld.setMessage(R.string.nc_cat_missing_msg).setCancelable(true);
        alt_dlg_bld.setPositiveButton(R.string.nc_cat_missing_ok,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alt_bld = alt_dlg_bld.create();
        alt_bld.show();

    }

    private class SaveTask extends AsyncTask<String, String, Complaint> {

        @Override
        protected Complaint doInBackground(String... params) {
            Complaint savedComplaint = null;
            try {
                savedComplaint = newComplaint.save();
                String url = img.upload(savedComplaint.getId());
                savedComplaint.addJustUploadedImage(url);
            } catch (IOException e) {
                // TODO: handle exception
                Log.e(TAG, "SaveTask doInBackground");
            }
            return savedComplaint;
        }

        @Override
        protected void onPostExecute(Complaint result) {
            super.onPostExecute(result);

            progressDialog.dismiss();

            Toast.makeText(getApplicationContext(),
                    "Your complaint is saved succesfully", Toast.LENGTH_SHORT)
                    .show();

            Intent anIntent = new Intent(NewComplaint.this,
                    DetailsActivity.class);
            anIntent.putExtra("latitude", latitude);
            anIntent.putExtra("longitude", longitude);
            anIntent.putExtra("class", result);
            startActivity(anIntent);
        }

    }

}
