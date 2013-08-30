package com.tobbetu.en4s;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.tobbetu.en4s.backend.Comment;
import com.tobbetu.en4s.backend.Complaint;
import com.tobbetu.en4s.helpers.CommentRejectedException;

public class MoreCommentsActivity extends Activity {

	private final String TAG = "MoreCommentActivity";
	Complaint complaint = null;
	LatLng userPosition = null;
	private ListView lvMoreComments;
	private final EditText etNewComment = null;

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
		/*
		 * findViewById(R.id.bBackToDetail).setOnClickListener( new
		 * OnClickListener() {
		 * 
		 * @Override public void onClick(View v) {
		 * 
		 * Intent i = new Intent(MoreCommentsActivity.this,
		 * DetailsActivity.class); i.putExtra("class", complaint);
		 * i.putExtra("latitude", userPosition.latitude);
		 * i.putExtra("longitude", userPosition.longitude); startActivity(i);
		 * 
		 * } });
		 */

		lvMoreComments = (ListView) findViewById(R.id.commentList);
		new CommentAsyncTask().execute();

		final EditText etCommentListNew = (EditText) findViewById(R.id.etCommentListNew);

		findViewById(R.id.bCommentPush).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {

						new CommentSaveTask().execute(etCommentListNew
								.getText().toString());
					}
				});
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

	private class CommentAsyncTask extends
			AsyncTask<String, String, List<Comment>> {

		@Override
		protected List<Comment> doInBackground(String... arg0) {
			try {
				return complaint.getComments();
			} catch (IOException e) {
				cancel(true);

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

		@Override
		protected void onCancelled() {
			super.onCancelled();

			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.mca_comment_rejected),
					Toast.LENGTH_SHORT).show();
		}
	}

	private class CommentSaveTask extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... arg0) {

			try {
				complaint.comment(arg0[0]);
			} catch (IOException e) {
				e.printStackTrace();
				Log.e(TAG, "CommentSaveTask IOException ", e);
			} catch (CommentRejectedException e) {
				e.printStackTrace();
				Log.e(TAG, "CommentSaveTask CommentRejectedException ", e);
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.mca_comment_accepted),
					Toast.LENGTH_SHORT).show();
		}

	}

}
