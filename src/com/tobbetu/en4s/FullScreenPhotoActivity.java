package com.tobbetu.en4s;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class FullScreenPhotoActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	      setContentView(R.layout.full_screen_photo);
	      getActionBar().hide();
	      
	      Intent intent = getIntent();
	      int imageId = intent.getExtras().getInt("imageId");
	      
	      ImageView imageView = (ImageView) findViewById(R.id.fullScreenPhoto);
	      imageView.setScaleType(ScaleType.FIT_XY);
	      imageView.setImageResource(imageId);
	}

}