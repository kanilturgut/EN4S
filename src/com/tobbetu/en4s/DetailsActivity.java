package com.tobbetu.en4s;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.HttpResponseException;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.tobbetu.en4s.backend.Comment;
import com.tobbetu.en4s.backend.Complaint;
import com.tobbetu.en4s.backend.Image;
import com.tobbetu.en4s.backend.Login;
import com.tobbetu.en4s.backend.User;
import com.tobbetu.en4s.cache.Cache;
import com.tobbetu.en4s.helpers.BetterAsyncTask;
import com.tobbetu.en4s.helpers.CategoryI18n;
import com.tobbetu.en4s.helpers.CommentRejectedException;
import com.tobbetu.en4s.helpers.ComplaintRejectedException;
import com.tobbetu.en4s.helpers.VoteRejectedException;
import com.tobbetu.en4s.service.EnforceService;

public class DetailsActivity extends Activity implements OnClickListener {

    private final String TAG = "DetailsActivity";

    private TextView tvComplaintAdress, tvComplaintTitle, tvComplaintCategory,
            tvReporter, tvReporterDate, tvYouAreAlreadyVoted,
            tvYouAreNotAllowed;
    private Button bUpVote, bDownVote, bMoreComment;
    // private LinearLayout viewPagerLayout;

    private GoogleMap myMap;

    private Complaint comp = null;
    private final User me = Login.getMe();
    private LatLng compPos = null;

    // more comment activitysine gidilip gidilmedigini tutacak.
    private boolean toMoreCommentActivity = false;

    private ImageView ivProblemImage = null;
    private ImageView ivAvatarImage = null;

    private boolean afterCommentFlag = false;

    private AlertDialog deleteDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.details_layout);
        // getActionBar().hide();

        comp = (Complaint) getIntent().getSerializableExtra("class");

        // viewPagerLayout = (LinearLayout) findViewById(R.id.viewPagerLayout);
        ivProblemImage = (ImageView) findViewById(R.id.ivProblemImage);
        ivAvatarImage = (ImageView) findViewById(R.id.ivAvatar);
        ivProblemImage.setOnClickListener(this);

        tvComplaintAdress = (TextView) findViewById(R.id.tvComplaintAdress);
        tvComplaintTitle = (TextView) findViewById(R.id.tvComplaintTitle);
        tvComplaintCategory = (TextView) findViewById(R.id.tvComplaintCategory);

        tvReporter = (TextView) findViewById(R.id.tvReporter);
        tvReporterDate = (TextView) findViewById(R.id.tvReporterDate);

        bUpVote = (Button) findViewById(R.id.bUpVote);
        bDownVote = (Button) findViewById(R.id.bDownVote);

        if (comp.alreadyVoted(me)) {
            bUpVote.setVisibility(Button.GONE);
            bDownVote.setVisibility(Button.GONE);
            tvYouAreAlreadyVoted = (TextView) findViewById(R.id.tvYouAreAlreadyVoted);
            tvYouAreAlreadyVoted.setVisibility(TextView.VISIBLE);
            tvYouAreAlreadyVoted.setText(getResources().getString(
                    R.string.da_already_voted));
        } else {
            bUpVote.setOnClickListener(this);
            bDownVote.setOnClickListener(this);
        }
        bMoreComment = (Button) findViewById(R.id.bMoreComment);
        bMoreComment.setOnClickListener(this);

        if (comp.getCommentsCount() > 0) {
            bMoreComment.setText(getResources().getString(
                    R.string.detail_item_all_commments_button)
                    + " (" + comp.getCommentsCount() + ")");
        }

        compPos = new LatLng(comp.getLatitude(), comp.getLongitude());

        // mViewPager = new HackyViewPager(this);
        // viewPagerLayout.addView(mViewPager);
        // mViewPager.setAdapter(new SamplePagerAdapter());

        myMap = ((MapFragment) getFragmentManager().findFragmentById(
                R.id.mapDetails)).getMap();
        myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        Utils.addAMarker(myMap, compPos, false);
        Utils.centerAndZomm(myMap, compPos, 15);
        tvComplaintAdress.setText(comp.getAddress());
        tvComplaintTitle.setText(comp.getTitle());
        tvComplaintCategory.setText(getString(CategoryI18n.getID(comp
                .getCategory())));

        tvReporter.setText(comp.getReporter().getName());
        tvReporterDate.setText(comp.getDateAsString(this));

        Cache.getInstance().getImage(comp.getReporter().getAvatar(),
                ivAvatarImage);
        comp.getImage(0, Image.SIZE_512, ivProblemImage);

        Log.d(TAG, "Avatar: " + comp.getReporter().getAvatar());
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int tmpWidth = size.x;

        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
                tmpWidth, tmpWidth);
        findViewById(R.id.complaintItemInfoLayout).setLayoutParams(llParams);
        ivProblemImage.setLayoutParams(llParams);

        new CommentGetTask().execute();
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.i(TAG, "onStop");

        // eger intent ile MoreCommentActivity e gidiliyorsa, artik bu activity
        // oldurulsun
        if (toMoreCommentActivity || afterCommentFlag)
            finish();
    }

    // Buttons onClickListener
    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.bUpVote) {
            createAlert(0);
        } else if (v.getId() == R.id.bDownVote) {
            createAlert(1);
        } else if (v.getId() == R.id.bMoreComment) { // bMoreComment
            toMoreCommentActivity = true;

            if (comp.getCommentsCount() != 0) {
                Intent i = new Intent(this, MoreCommentsActivity.class);
                i.putExtra("class", comp);
                startActivity(i);
            } else {
                final AlertDialog.Builder alert = new AlertDialog.Builder(this);
                LayoutInflater inflater = LayoutInflater.from(this);
                final View deneme = inflater.inflate(
                        R.layout.new_comment_from_dialog, null);
                alert.setView(deneme);
                alert.setTitle(getResources()
                        .getString(R.string.dialog_comment));
                alert.setPositiveButton(
                        getResources().getString(R.string.sendButton),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int whichButton) {

                                EditText et = (EditText) deneme
                                        .findViewById(R.id.etNewCommenFromDialog);

                                if (et.getText().toString().length() == 0)
                                    Toast.makeText(
                                            DetailsActivity.this,
                                            getResources()
                                                    .getString(
                                                            R.string.dialog_empty_comment),
                                            Toast.LENGTH_SHORT).show();
                                else
                                    new CommentSaveTask().execute(et.getText()
                                            .toString());

                            }
                        });

                alert.setNegativeButton(
                        getResources().getString(R.string.dialog_cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int whichButton) {
                                dialog.cancel();
                            }
                        });
                alert.show();
            }
        } else if (v.getId() == R.id.ivProblemImage) {
            Log.i(TAG, "tikla");
            Intent i = new Intent(this, BiggerImage.class);
            i.putExtra("class", comp);
            startActivity(i);
        }

    }

    private void createAlert(int i) {

        AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
        alt_bld.setTitle("Dikkat");
        alt_bld.setCancelable(true);

        alt_bld.setNegativeButton(R.string.da_dialog_neg,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        if (i == 0) { // up vote
            alt_bld.setMessage(R.string.da_upvote_msg);
            alt_bld.setPositiveButton(R.string.da_dialog_pos,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            new VoteTask("upvote").execute();
                        }
                    });
        } else {
            alt_bld.setMessage(R.string.da_downvote_msg);
            alt_bld.setPositiveButton(R.string.da_dialog_pos,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            new VoteTask("downvote").execute();
                        }
                    });
        }

        AlertDialog alt_dlg = alt_bld.create();
        alt_dlg.show();

    }

    class VoteTask extends BetterAsyncTask<Void, Void> {

        private String type = "";

        public VoteTask(String type) {
            this.type = type;
        }

        @Override
        protected Void task(Void... arg0) throws Exception {
            String location = Utils
                    .locationToJSON(EnforceService.getLocation().getLatitude(),
                            EnforceService.getLocation().getLongitude());

            if (type.equalsIgnoreCase("upvote")) {
                comp.upvote(me, location);
            } else if (type.equalsIgnoreCase("downvote")) {
                comp.downvote(me, location);
            }
            return null;
        }

        @Override
        protected void onSuccess(Void result) {
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.da_voting_accepted),
                    Toast.LENGTH_SHORT).show();

            bUpVote.setVisibility(Button.GONE);
            bDownVote.setVisibility(Button.GONE);

            tvYouAreAlreadyVoted = (TextView) findViewById(R.id.tvYouAreAlreadyVoted);
            tvYouAreAlreadyVoted.setVisibility(TextView.VISIBLE);
            tvYouAreAlreadyVoted.setText(getResources().getString(
                    R.string.da_already_voted));
        }

        @Override
        protected void onFailure(Exception error) {
            if (error instanceof IOException) {
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.network_failed_msg),
                        Toast.LENGTH_SHORT).show();
            } else if (error instanceof VoteRejectedException) {
                Utils.createAlert(DetailsActivity.this, "Hata",
                        getString(R.string.da_voting_rejected), true, "",
                        "Tamam");

                // Vote kabul edilmediyse hata goster
                bUpVote.setVisibility(Button.GONE);
                bDownVote.setVisibility(Button.GONE);

                tvYouAreAlreadyVoted = (TextView) findViewById(R.id.tvYouAreAlreadyVoted);
                tvYouAreAlreadyVoted.setVisibility(TextView.VISIBLE);
                tvYouAreAlreadyVoted.setText(getResources().getString(
                        R.string.da_voting_rejected));
            }
        }

    }

    private class CommentGetTask extends BetterAsyncTask<Void, List<Comment>> {

        @Override
        protected List<Comment> task(Void... arg0) throws Exception {
            return comp.getComments();
        }

        @Override
        protected void onSuccess(List<Comment> result) {
            // Make comments panel only visible after fetching
            LinearLayout commentsLayout = (LinearLayout) findViewById(R.id.newComplaintCommentLayout);
            commentsLayout.setVisibility(View.VISIBLE);

            TextView tvComment = (TextView) findViewById(R.id.tvComment);
            TextView tvCommentUser = (TextView) findViewById(R.id.tvCommentUser);
            ImageView ivAvatar = (ImageView) findViewById(R.id.ivCommentUserAvatar);

            TextView tvComment2 = (TextView) findViewById(R.id.tvComment2);
            TextView tvCommentUser2 = (TextView) findViewById(R.id.tvCommentUser2);
            ImageView ivAvatar2 = (ImageView) findViewById(R.id.ivCommentUserAvatar2);

            if (result != null) {
                if (result.size() >= 2) {
                    tvComment.setText(result.get(0).getText().toString());
                    tvCommentUser.setText(result.get(0).getAuthor().getName());
                    Cache.getInstance().getImage(
                            result.get(0).getAuthor().getAvatar(), ivAvatar);

                    tvComment2.setText(result.get(1).getText().toString());
                    tvCommentUser2.setText(result.get(1).getAuthor().getName());
                    Cache.getInstance().getImage(
                            result.get(1).getAuthor().getAvatar(), ivAvatar2);
                } else if (result.size() == 1) {
                    tvComment.setText(result.get(0).getText().toString());
                    tvCommentUser.setText(result.get(0).getAuthor().getName());
                    Cache.getInstance().getImage(
                            result.get(0).getAuthor().getAvatar(), ivAvatar);

                    ((LinearLayout) findViewById(R.id.newComplaintCommentTwo))
                            .setVisibility(LinearLayout.GONE);
                } else { // 0
                    ((LinearLayout) findViewById(R.id.newComplaintCommentInfoLayout))
                            .setVisibility(LinearLayout.GONE);

                    bMoreComment.setText(getResources().getString(
                            R.string.da_add_the_first_comment));
                }
            }
        }

        @Override
        protected void onFailure(Exception error) {
            if (error instanceof IOException) {
                Toast.makeText(DetailsActivity.this,
                        getResources().getString(R.string.network_failed_msg),
                        Toast.LENGTH_LONG).show();
            } else if (error instanceof JSONException
                    || error instanceof HttpResponseException) {
                BugSenseHandler
                        .sendEvent("CommentGetTask failed in DetailsActivity");
                BugSenseHandler.sendException(error);
                Toast.makeText(DetailsActivity.this,
                        getResources().getString(R.string.api_changed),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private class CommentSaveTask extends BetterAsyncTask<String, Void> {

        private ProgressDialog pd = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pd = ProgressDialog.show(DetailsActivity.this, getResources()
                    .getString(R.string.dialog_comment_please_wait),
                    getResources()
                            .getString(R.string.dialog_comment_is_sending),
                    true, false);
        }

        @Override
        protected Void task(String... arg0) throws Exception {
            comp.comment(arg0[0]);
            return null;
        }

        @Override
        protected void onSuccess(Void result) {
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.mca_comment_accepted),
                    Toast.LENGTH_SHORT).show();

            pd.dismiss();

            afterCommentFlag = true;
            Intent i = new Intent(DetailsActivity.this, DetailsActivity.class);
            i.putExtra("class", comp);
            startActivity(i);
        }

        @Override
        protected void onFailure(Exception error) {
            if (error instanceof IOException) {
                Toast.makeText(DetailsActivity.this,
                        getResources().getString(R.string.network_failed_msg),
                        Toast.LENGTH_LONG).show();
            } else if (error instanceof JSONException) {
                BugSenseHandler
                        .sendEvent("CommentSaveTask failed in DetailsActivity");
                BugSenseHandler.sendException(error);
                Toast.makeText(DetailsActivity.this,
                        getResources().getString(R.string.api_changed),
                        Toast.LENGTH_LONG).show();
            } else if (error instanceof CommentRejectedException) {
                Toast.makeText(
                        DetailsActivity.this,
                        getResources().getString(R.string.mca_comment_rejected),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private class ComplaintDeleteTask extends BetterAsyncTask<Void, Void> {

        @Override
        protected Void task(Void... arg0) throws Exception {
            comp.delete();
            return null;
        }

        @Override
        protected void onSuccess(Void result) {
            Toast.makeText(DetailsActivity.this, R.string.da_complaint_deleted,
                    Toast.LENGTH_LONG).show();
            finish();
        }

        @Override
        protected void onFailure(Exception error) {
            if (error instanceof IOException) {
                Toast.makeText(DetailsActivity.this,
                        getResources().getString(R.string.network_failed_msg),
                        Toast.LENGTH_LONG).show();
            } else if (error instanceof ComplaintRejectedException) {
                Toast.makeText(getApplicationContext(),
                        R.string.da_complaint_delete_rejected,
                        Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.details_menu, menu);
        if (comp.getReporter().getId().equals(Login.getMe().getId())) {
            menu.findItem(R.id.da_action_delete).setVisible(true);
        }
        return true;
    }

    private void deleteDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.da_delete_title);
        builder.setMessage(R.string.da_delete_msg);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.ma_quit_ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        new ComplaintDeleteTask().execute();
                    }
                });
        builder.setNegativeButton(R.string.ma_quit_cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        deleteDialog = builder.create();
        deleteDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String url = "";

        switch (item.getItemId()) {
        case R.id.da_action_delete:
            deleteDialog();
            return true;

        case R.id.shareOnFacebook:
            url = "https://www.facebook.com/sharer/sharer.php?u="
                    + comp.getSlug_URL();

            Intent facebookIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(url));
            startActivity(facebookIntent);
            return true;
        case R.id.shareOnTwitter:
            url = "https://twitter.com/intent/tweet?url=" + comp.getSlug_URL()
                    + "&text=" + comp.getTitle() + "&via=enforceapp";

            Intent twitterIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(url));
            startActivity(twitterIntent);
            return true;
        case R.id.shareOnGooglePlus:
            url = "https://plus.google.com/share?url=" + comp.getSlug_URL();

            Intent googlePlusIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(url));
            startActivity(googlePlusIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
