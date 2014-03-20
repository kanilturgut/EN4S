package com.tobbetu.en4s.comment;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.google.analytics.tracking.android.EasyTracker;
import com.tobbetu.en4s.R;
import com.tobbetu.en4s.complaint.Complaint;
import com.tobbetu.en4s.complaint.DetailsActivity;
import com.tobbetu.en4s.helpers.BetterAsyncTask;

public class MoreCommentsActivity extends Activity {

    private final String TAG = "MoreCommentActivity";
    Complaint complaint = null;
    private ListView lvMoreComments;

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
        lvMoreComments = (ListView) findViewById(R.id.commentList);
        new CommentAsyncTask().execute();

        final EditText etCommentListNew = (EditText) findViewById(R.id.etCommentListNew);

        findViewById(R.id.bCommentPush).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        if (etCommentListNew.getText().toString().length() == 0)
                            Toast.makeText(
                                    MoreCommentsActivity.this,
                                    getResources().getString(
                                            R.string.dialog_empty_comment),
                                    Toast.LENGTH_SHORT).show();
                        else
                            new CommentSaveTask().execute(etCommentListNew
                                    .getText().toString());
                    }
                });
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(MoreCommentsActivity.this, DetailsActivity.class);
        i.putExtra("class", complaint);
        startActivity(i);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
        // kill this activity
        finish();
    }

    private class CommentAsyncTask extends BetterAsyncTask<Void, List<Comment>> {

        @Override
        protected List<Comment> task(Void... arg0) throws Exception {
            return complaint.getComments();
        }

        @Override
        protected void onSuccess(List<Comment> result) {
            // FIXMEE: I have no idea which context to use, so selected randomly
            lvMoreComments.setAdapter(new CommentListAdapter(getBaseContext(),
                    result));
        }

        @Override
        protected void onFailure(Exception error) {
            if (error instanceof IOException) {
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.network_failed_msg),
                        Toast.LENGTH_SHORT).show();
            } else if (error instanceof JSONException) {
                BugSenseHandler
                        .sendEvent("CommentAsyncTask failed because of JSONException");
                BugSenseHandler.sendException(error);
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.api_changed),
                        Toast.LENGTH_SHORT).show();
            }

        }
    }

    private class CommentSaveTask extends BetterAsyncTask<String, Void> {

        private ProgressDialog pd = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pd = ProgressDialog.show(MoreCommentsActivity.this, getResources()
                    .getString(R.string.dialog_comment_please_wait),
                    getResources()
                            .getString(R.string.dialog_comment_is_sending),
                    true, false);
        }

        @Override
        protected Void task(String... arg0) throws Exception {
            complaint.comment(arg0[0]);
            return null;
        }

        @Override
        protected void onSuccess(Void result) {
            pd.dismiss();
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.mca_comment_accepted),
                    Toast.LENGTH_SHORT).show();

            Intent i = new Intent(MoreCommentsActivity.this,
                    MoreCommentsActivity.class);
            i.putExtra("class", complaint);
            startActivity(i);
        }

        @Override
        protected void onFailure(Exception error) {
            pd.dismiss();
            Log.e(TAG, "CommentSaveTask Failed ", error);
            if (error instanceof IOException) {
                BugSenseHandler.sendException(error);
                Toast.makeText(MoreCommentsActivity.this,
                        getResources().getString(R.string.network_failed_msg),
                        Toast.LENGTH_LONG).show();
            } else if (error instanceof JSONException) {
                BugSenseHandler.sendException(error);
                Toast.makeText(MoreCommentsActivity.this,
                        getResources().getString(R.string.api_changed),
                        Toast.LENGTH_LONG).show();
            } else if (error instanceof CommentRejectedException) {
                Toast.makeText(
                        MoreCommentsActivity.this,
                        getResources().getString(R.string.mca_comment_rejected),
                        Toast.LENGTH_LONG).show();
            }
        }

    }

}
