package com.tobbetu.en4s;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;
import com.tobbetu.en4s.backend.Comment;
import com.tobbetu.en4s.backend.Complaint;

public class MoreCommentsActivity extends Activity {

    Complaint complaint = null;
    LatLng userPosition = null;
    private ListView lvMoreComments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_comments);

        getActionBar().hide();

        /*
         * Detay sayfasina geri dondugumuz zaman bu bilgileri inten icine
         * yerlestirmeliyiz.
         */
        complaint = (Complaint) getIntent().getSerializableExtra("class");
        userPosition = new LatLng(getIntent().getDoubleExtra("latitude", 0),
                getIntent().getDoubleExtra("longitude", 0));

        // Geri tusuna basinca gerceklesecek islemler
        findViewById(R.id.bBackToDetail).setOnClickListener(
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
                });

        lvMoreComments = (ListView) findViewById(R.id.lvCommentOnMoreComment);
        new CommentAsyncTask().execute();

        /*
         * Su anda commentler serverdan alinamadigi icin bu sekilde sacma
         * yorumlar getiriyorum. Fakat hem comment adapter hem comment layout
         * hem de comment sinifi yazildi. Server dan comment alinabildigi zaman
         * burasi update edilecek
         */
    }
    
    @Override
    public void onBackPressed() {
        Intent i = new Intent(MoreCommentsActivity.this,
                DetailsActivity.class);
        i.putExtra("class", complaint);
        i.putExtra("latitude", userPosition.latitude);
        i.putExtra("longitude", userPosition.longitude);
        startActivity(i);
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	
    	//kill this activity
    	finish();
    }

    private class CommentAsyncTask extends
            AsyncTask<String, String, List<Comment>> {

        @Override
        protected List<Comment> doInBackground(String... arg0) {
            try {
                return Comment.getComments(complaint);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Comment> result) {
            // FIXMEE: I have no idea which context to use, so selected randomly
            lvMoreComments.setAdapter(new CommentListAdapter(getBaseContext(),
                    result));
        }
    }

}
