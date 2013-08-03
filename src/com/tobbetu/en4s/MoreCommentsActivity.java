package com.tobbetu.en4s;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;
import com.tobbetu.en4s.backend.Complaint;

public class MoreCommentsActivity extends Activity {

	Complaint complaint = null;
	LatLng userPosition = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_more_comments);
		
		getActionBar().hide();
		
		/*Detay sayfasina geri dondugumuz zaman bu bilgileri inten icine yerlestirmeliyiz.*/
		complaint = (Complaint) getIntent().getSerializableExtra("class");
		userPosition = new LatLng(getIntent().getDoubleExtra("latitude", 0), getIntent().getDoubleExtra("longitude", 0));
		
		//Geri tusuna basinca gerceklesecek islemler
		findViewById(R.id.bBackToDetail).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Intent i = new Intent(MoreCommentsActivity.this, DetailsActivity.class);
				i.putExtra("class", complaint);
                i.putExtra("latitude", userPosition.latitude);
                i.putExtra("longitude", userPosition.longitude);
                startActivity(i);				
				
			}
		});
		
		String sporDallari[] = {"Basketbol", "Futbol", "Tenis", "Voleybol",
	            "Hentbol", "Y�zme", "Golf"};
		ListView lvMoreComments = (ListView) findViewById(R.id.lvCommentOnMoreComment);
		lvMoreComments.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,sporDallari));
	}

}