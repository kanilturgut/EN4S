package com.tobbetu.en4s.biggerImage;

import android.app.Activity;
import android.os.Bundle;

import com.tobbetu.en4s.R;
import com.tobbetu.en4s.backend.Image;
import com.tobbetu.en4s.complaint.Complaint;

public class BiggerImage extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bigger_image);
        getActionBar().hide();

        Complaint comp = (Complaint) getIntent().getSerializableExtra("class");

        TouchImageView image = (TouchImageView) findViewById(R.id.touchImageView);
        comp.getImage(0, Image.SIZE_512, image);
        image.setMaxZoom(5f);
    }
}
