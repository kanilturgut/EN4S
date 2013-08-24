package com.tobbetu.en4s;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;
import com.tobbetu.en4s.backend.Complaint;

public class MoreCommentsActivity extends Activity {

	private final String TAG = "MoreCommentActivity";
    Complaint complaint = null;
    LatLng userPosition = null;
    private ListView lvMoreComments;
    private EditText etNewComment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comment_list);

        getActionBar().hide();
        // klavye kendi kendine acilmayacak...Oh beeee :D
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        /*
         * Detay sayfasina geri dondugumuz zaman bu bilgileri inten icine
         * yerlestirmeliyiz.
         */
        complaint = (Complaint) getIntent().getSerializableExtra("class");
        userPosition = new LatLng(getIntent().getDoubleExtra("latitude", 0),
                getIntent().getDoubleExtra("longitude", 0));

        // Geri tusuna basinca gerceklesecek islemler
        /*findViewById(R.id.bBackToDetail).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        Intent i = new Intent(MoreCommentsActivity.this,
                                DetailsActivity.class);
                        i.putExtra("class", complaint);
                        i.putExtra("latitude", userPosition.latitude);
                        i.putExtra("longitude", userPosition.longitude);
                        startActivity(i);

                    }
                });*/

        lvMoreComments = (ListView) findViewById(R.id.commentList);
        lvMoreComments.setAdapter(new CommentListAdapter(this, complaint
                .getComments()));


    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(MoreCommentsActivity.this, DetailsActivity.class);
        i.putExtra("class", complaint);
        i.putExtra("latitude", userPosition.latitude);
        i.putExtra("longitude", userPosition.longitude);
        startActivity(i);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // kill this activity
        finish();
    }
    
}
