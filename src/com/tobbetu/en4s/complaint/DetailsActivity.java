package com.tobbetu.en4s.complaint;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import com.bugsense.trace.BugSenseHandler;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.tobbetu.en4s.R;
import com.tobbetu.en4s.Utils;
import com.tobbetu.en4s.backend.Image;
import com.tobbetu.en4s.backend.User;
import com.tobbetu.en4s.biggerImage.BiggerImage;
import com.tobbetu.en4s.cache.Cache;
import com.tobbetu.en4s.circularImageView.CircularImageView;
import com.tobbetu.en4s.comment.Comment;
import com.tobbetu.en4s.comment.CommentRejectedException;
import com.tobbetu.en4s.comment.MoreCommentsActivity;
import com.tobbetu.en4s.helpers.BetterAsyncTask;
import com.tobbetu.en4s.helpers.VoteRejectedException;
import com.tobbetu.en4s.login.Login;
import com.tobbetu.en4s.service.EnforceService;
import org.apache.http.client.HttpResponseException;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;

public class DetailsActivity extends Activity implements OnClickListener {

    final String TAG = "DetailsActivity";
    Context context;

    TextView tvComplaintAdress, tvComplaintTitle, tvComplaintCategory, tvReporter, tvReporterDate, tvUpVoteCount, tvDownVoteCount, tvCommentsCount, tvCommentsCountOnCommentButton;
    ImageView ivVoteUp, ivVoteDown, ivComments, ivProblemImage;
    LinearLayout detailsSocialNetworks;
    Button bMoreComment;

    GoogleMap myMap;

    Complaint comp = null;
    final User me = Login.getMe();
    LatLng compPos = null;

    // more comment activitysine gidilip gidilmedigini tutacak.
    boolean toMoreCommentActivity = false;

    CircularImageView ivAvatarImage = null;

    boolean afterCommentFlag = false;

    AlertDialog deleteDialog;

    EasyTracker easyTracker = null;

    static boolean isVotedUp, isVotedDown;

    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.details_layout);
        context = this;

        getActionBar().setHomeButtonEnabled(true);

        easyTracker = EasyTracker.getInstance(this);
        comp = (Complaint) getIntent().getSerializableExtra("class");

        ivProblemImage = (ImageView) findViewById(R.id.ivProblemImage);
        ivAvatarImage = (CircularImageView) findViewById(R.id.ivAvatar);
        ivProblemImage.setOnClickListener(this);

        tvComplaintAdress = (TextView) findViewById(R.id.tvComplaintAdress);
        tvComplaintTitle = (TextView) findViewById(R.id.tvComplaintTitle);
        tvComplaintCategory = (TextView) findViewById(R.id.tvComplaintCategory);

        tvReporter = (TextView) findViewById(R.id.tvReporter);
        tvReporterDate = (TextView) findViewById(R.id.tvReporterDate);

        ivVoteUp = (ImageView) findViewById(R.id.ivUp);
        ivVoteUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                changeUpVoteImage();
            }
        });
        ivVoteDown = (ImageView) findViewById(R.id.ivDown);
        ivVoteDown.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                changeDownVoteImage();
            }
        });

        ivComments = (ImageView) findViewById(R.id.ivComment);

        detailsSocialNetworks = (LinearLayout) findViewById(R.id.detailsSocialNetworks);
        detailsSocialNetworks.setOnClickListener(this);

        tvUpVoteCount = (TextView) findViewById(R.id.tvUpVoteCount);
        tvUpVoteCount.setText(String.valueOf(comp.getUpVote()));

        tvDownVoteCount = (TextView) findViewById(R.id.tvDownVoteCount);
        tvDownVoteCount.setText(String.valueOf(comp.getDownVote()));

        tvCommentsCountOnCommentButton = (TextView) findViewById(R.id.tvComment);
        tvCommentsCountOnCommentButton.setText(String.valueOf(comp.getCommentsCount()));

        tvCommentsCount = (TextView) findViewById(R.id.tvCommentCount);
        tvCommentsCount.setText(String.valueOf(comp.getCommentsCount()));


        if (comp.alreadyUpVoted(me)) {
            ivVoteUp.setImageResource(R.drawable.upvote_green);
            isVotedUp = true;
        } else {
            ivVoteUp.setImageResource(R.drawable.upvote);
            isVotedUp = false;
        }
        if (comp.alreadyDownVoted(me)) {
            ivVoteDown.setImageResource(R.drawable.downvote_red);
            isVotedDown = true;
        } else {
            ivVoteDown.setImageResource(R.drawable.downvote);
            isVotedDown = false;
        }

        bMoreComment = (Button) findViewById(R.id.bMoreComment);
        bMoreComment.setOnClickListener(this);

        if (comp.getCommentsCount() > 0) {
            bMoreComment.setText(getResources().getString(
                    R.string.detail_item_all_commments_button)
                    + " (" + comp.getCommentsCount() + ")");
        }

        compPos = new LatLng(comp.getLatitude(), comp.getLongitude());

        myMap = ((MapFragment) getFragmentManager().findFragmentById(
                R.id.mapDetails)).getMap();
        myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        myMap.getUiSettings().setAllGesturesEnabled(false);

        Utils.addAMarker(myMap, compPos, false);
        Utils.centerAndZomm(myMap, compPos, 15);
        tvComplaintAdress.setText(comp.getAddress());
        tvComplaintTitle.setText(comp.getTitle());
        tvComplaintCategory.setText(comp
                .getDateAsString(this.context) + " / " + comp.getDistance(this.context,
                EnforceService.getLocation().getLatitude(), EnforceService
                        .getLocation().getLongitude()));

        tvReporter.setText(comp.getReporter().getName());
        tvReporterDate.setText(comp.getDateAsString(this));

        Cache.getInstance().getImage(comp.getReporter().getAvatar(), ivAvatarImage);
        comp.getImage(0, Image.SIZE_512, ivProblemImage);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int tmpWidth = size.x;

        RelativeLayout.LayoutParams llParams = new RelativeLayout.LayoutParams(
                tmpWidth, tmpWidth);
        findViewById(R.id.complaintItemInfoLayout).setLayoutParams(llParams);
        ivProblemImage.setLayoutParams(llParams);

        new CommentGetTask().execute();
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this); // Add this method.
    }

    @Override
    protected void onStop() {
        super.onStop();

        EasyTracker.getInstance(this).activityStop(this); // Add this method.

        Log.i(TAG, "onStop");

        // eger intent ile MoreCommentActivity e gidiliyorsa, artik bu activity
        // oldurulsun
        if (toMoreCommentActivity || afterCommentFlag)
            finish();
    }

    // Buttons onClickListener
    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.bMoreComment) { // bMoreComment
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
                                            Toast.LENGTH_SHORT
                                    ).show();
                                else
                                    new CommentSaveTask().execute(et.getText()
                                            .toString());

                            }
                        }
                );

                alert.setNegativeButton(
                        getResources().getString(R.string.dialog_cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                dialog.cancel();
                            }
                        }
                );
                alert.show();
            }
        } else if (v.getId() == R.id.ivProblemImage) {

            easyTracker.send(MapBuilder.createEvent("bigger_image", "bigger_image_opened", "R.id.ivProblemImage", null).build());

            Intent i = new Intent(this, BiggerImage.class);
            i.putExtra("class", comp);
            startActivity(i);
        } else if (v == detailsSocialNetworks) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, comp.getTitle());
            shareIntent.putExtra(Intent.EXTRA_TEXT, comp.getTitle() + " @enforceapp " + comp.getSlug_URL());
            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share)));
        }

    }

    void createAlert(int i) {

        AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
        alt_bld.setTitle("Dikkat");
        alt_bld.setCancelable(true);

        alt_bld.setNegativeButton(R.string.da_dialog_neg,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                }
        );

        if (i == 0) { // up vote
            alt_bld.setMessage(R.string.da_upvote_msg);
            alt_bld.setPositiveButton(R.string.da_dialog_pos,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            new VoteTask("upvote").execute();
                        }
                    }
            );
        } else {
            alt_bld.setMessage(R.string.da_downvote_msg);
            alt_bld.setPositiveButton(R.string.da_dialog_pos,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            new VoteTask("downvote").execute();
                        }
                    }
            );
        }

        AlertDialog alt_dlg = alt_bld.create();
        alt_dlg.show();

    }

    class VoteTask extends BetterAsyncTask<Void, Void> {

        String type = "";

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
            Toast.makeText(context, getString(R.string.da_voting_accepted), Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onFailure(Exception error) {
            if (error instanceof IOException) {
                Toast.makeText(context, getString(R.string.network_failed_msg), Toast.LENGTH_SHORT).show();
            } else if (error instanceof VoteRejectedException) {
                Toast.makeText(context, getString(R.string.da_voting_rejected), Toast.LENGTH_LONG).show();

            }
        }

    }

    class CommentGetTask extends BetterAsyncTask<Void, List<Comment>> {

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

    class CommentSaveTask extends BetterAsyncTask<String, Void> {

        ProgressDialog pd = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pd = ProgressDialog.show(DetailsActivity.this, getResources()
                            .getString(R.string.dialog_comment_please_wait),
                    getResources()
                            .getString(R.string.dialog_comment_is_sending),
                    true, false
            );
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

    class ComplaintDeleteTask extends BetterAsyncTask<Void, Void> {

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

    void deleteDialog() {

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
                }
        );
        builder.setNegativeButton(R.string.ma_quit_cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                }
        );

        deleteDialog = builder.create();
        deleteDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                onBackPressed();
                return true;
            case R.id.da_action_delete:
                deleteDialog();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void changeUpVoteImage() {

        if (isVotedDown) {
            Toast.makeText(context, getString(R.string.da_already_voted), Toast.LENGTH_LONG).show();
        } else {

            if (isVotedUp) {
                isVotedUp = false;
                ivVoteUp.setImageResource(R.drawable.upvote);
                tvUpVoteCount.setText(String.valueOf(Integer.parseInt(tvUpVoteCount.getText().toString()) - 1));
            } else {
                isVotedUp = true;
                ivVoteUp.setImageResource(R.drawable.upvote_green);
                tvUpVoteCount.setText(String.valueOf(Integer.parseInt(tvUpVoteCount.getText().toString()) + 1));
            }

            new VoteTask("upvote").execute();
        }
    }

    void changeDownVoteImage() {

        if (isVotedUp) {
            Toast.makeText(context, getString(R.string.da_already_voted), Toast.LENGTH_LONG).show();
        } else {

            if (isVotedDown) {
                isVotedDown = false;
                ivVoteDown.setImageResource(R.drawable.downvote);
                tvDownVoteCount.setText(String.valueOf(Integer.parseInt(tvDownVoteCount.getText().toString()) - 1));
            } else {
                isVotedDown = true;
                ivVoteDown.setImageResource(R.drawable.downvote_red);
                tvDownVoteCount.setText(String.valueOf(Integer.parseInt(tvDownVoteCount.getText().toString()) + 1));
            }

            new VoteTask("downvote").execute();
        }
    }
}
