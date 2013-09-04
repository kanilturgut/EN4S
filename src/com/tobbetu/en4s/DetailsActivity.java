/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.tobbetu.en4s;

import java.io.IOException;
import java.util.List;

import uk.co.senab.photoview.PhotoView;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.tobbetu.en4s.backend.Comment;
import com.tobbetu.en4s.backend.Complaint;
import com.tobbetu.en4s.backend.Image;
import com.tobbetu.en4s.backend.Login;
import com.tobbetu.en4s.backend.User;
import com.tobbetu.en4s.helpers.CategoryI18n;
import com.tobbetu.en4s.helpers.CommentRejectedException;
import com.tobbetu.en4s.helpers.VoteRejectedException;

public class DetailsActivity extends Activity implements OnClickListener {

    private final String TAG = "DetailsActivity";

    private TextView tvComplaintAdress, tvComplaintTitle, tvComplaintCategory,
            tvReporter, tvReporterDate, tvYouAreNotAllowed;
    private Button bUpVote, bDownVote, bMoreComment;
    // private LinearLayout viewPagerLayout;

    // private ViewPager mViewPager;
    private PhotoView photoView;

    private GoogleMap myMap;

    private Complaint comp = null;
    private final User me = Login.getMe();
    private LatLng compPos = null;
    LatLng myPosition = null;

    // more comment activitysine gidilip gidilmedigini tutacak.
    private boolean toMoreCommentActivity = false;

    private ImageView ivProblemImage = null;

    private ImageTask imageTask;
    private boolean afterCommentFlag = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.details_layout);
        // getActionBar().hide();

        comp = (Complaint) getIntent().getSerializableExtra("class");

        // viewPagerLayout = (LinearLayout) findViewById(R.id.viewPagerLayout);
        ivProblemImage = (ImageView) findViewById(R.id.ivProblemImage);

        tvComplaintAdress = (TextView) findViewById(R.id.tvComplaintAdress);
        tvComplaintTitle = (TextView) findViewById(R.id.tvComplaintTitle);
        tvComplaintCategory = (TextView) findViewById(R.id.tvComplaintCategory);

        tvReporter = (TextView) findViewById(R.id.tvReporter);
        tvReporterDate = (TextView) findViewById(R.id.tvReporterDate);

        bUpVote = (Button) findViewById(R.id.bUpVote);
        bDownVote = (Button) findViewById(R.id.bDownVote);
        if (comp.alreadyUpVoted(me)) {
            bUpVote.setVisibility(Button.GONE);
            bDownVote.setVisibility(Button.GONE);
            tvYouAreNotAllowed = (TextView) findViewById(R.id.tvYouAreNotAllowed);
            tvYouAreNotAllowed.setVisibility(TextView.VISIBLE);
            tvYouAreNotAllowed.setText(getResources().getString(
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
        myPosition = new LatLng(getIntent().getDoubleExtra("latitude", 0),
                getIntent().getDoubleExtra("longitude", 0));

        // Ipneligine sildim, saygilarimla.
        // Mustafa

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

        imageTask = new ImageTask();
        imageTask.execute(0);

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

        /*
         * Bu sekilde detay sayfasina girilmis olan bir sikayetin, resmi
         * yuklenmeden detay sayfasindan cikinca arka plan da calisan resmi
         * indirme gorevi iptal ediliyor. Boylece kapanma hatasi almiyoruz.
         */
        imageTask.cancel(true);
    }

    class SamplePagerAdapter extends PagerAdapter {

        // private int[] sDrawables = { R.drawable.img1, R.drawable.img2,
        // R.drawable.img3 };

        @Override
        public int getCount() {
            return comp.imageCount();
        }

        @Override
        public View instantiateItem(final ViewGroup container,
                final int position) {
            photoView = new PhotoView(container.getContext());

            // photoView.setOnPhotoTapListener(new OnPhotoTapListener() {
            // @Override
            // public void onPhotoTap(View view, float x, float y) {
            // Toast.makeText(container.getContext(), "t�kland�",
            // Toast.LENGTH_SHORT).show();
            // //Log.e("image", "tik");
            // Intent intent = new Intent(getApplication(),
            // FullScreenPhotoActivity.class);
            //
            // ByteArrayOutputStream stream = new ByteArrayOutputStream();
            // bmp.compress(Bitmap.CompressFormat.PNG, 0, stream);
            // byte[] byteArray = stream.toByteArray();
            // intent.putExtra("image", byteArray);
            // startActivity(intent);
            // }
            // });

            // photoView.setOnTouchListener(new OnTouchListener() {
            //
            // @Override
            // public boolean onTouch(View v, MotionEvent event) {
            // ScrollView scrollView = (ScrollView) findViewById(R.id.scroller);
            // scrollView.setEnabled(false);
            // return false;
            // }
            // });

            // Now just add PhotoView to ViewPager and return it
            photoView.setScaleType(ScaleType.FIT_XY);
            container.addView(photoView, LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
            photoView.setImageResource(R.drawable.content_picture);
            return photoView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    }

    class ImageTask extends AsyncTask<Integer, String, String> {

        @Override
        protected String doInBackground(Integer... params) {
            try {
                comp.getImage(params[0], Image.SIZE_512, ivProblemImage);
            } catch (Exception e) {
                // Bazi durumlarda image cacheden yuklenemiyor. Mesela kucuk
                // sekilde geliyor, iste bu durumlarda exception atip activity i
                // olduruyorum
                cancel(true);
                Log.e(TAG, "ImageTask hatasi", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int tmpWidth = size.x;

            LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
                    tmpWidth, tmpWidth);
            findViewById(R.id.complaintItemInfoLayout)
                    .setLayoutParams(llParams);

            ivProblemImage.setLayoutParams(llParams);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            Toast.makeText(DetailsActivity.this,
                    getResources().getString(R.string.warning),
                    Toast.LENGTH_LONG).show();

            // Eger bu sorun olusursa activityi oldurup main acitivity e donelim
            finish();
        }
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
                i.putExtra("latitude", myPosition.latitude);
                i.putExtra("longitude", myPosition.longitude);
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
                            new UpVoteTask().execute();
                        }
                    });
        } else {
            alt_bld.setMessage(R.string.da_downvote_msg);
            alt_bld.setPositiveButton(R.string.da_dialog_pos,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            new DownVoteTask().execute();
                        }
                    });
        }

        AlertDialog alt_dlg = alt_bld.create();
        alt_dlg.show();

    }

    class UpVoteTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                Log.d(getClass().getName(), "In UpVoteTask doInbackground");
                comp.upvote(me, Utils.locationToJSON(myPosition.latitude,
                        myPosition.longitude));
            } catch (IOException e) {
                cancel(true);

                Log.e(getClass().getName(), "UpVoteTask failed", e);
            } catch (VoteRejectedException e) {
                cancel(true);

                Log.e(getClass().getName(), "UpVote rejected", e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(getClass().getName(), "In UpVoteTask onPostExecute");
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.da_voting_accepted),
                    Toast.LENGTH_SHORT).show();

            bUpVote.setVisibility(Button.GONE);
            bDownVote.setVisibility(Button.GONE);

            tvYouAreNotAllowed = (TextView) findViewById(R.id.tvYouAreNotAllowed);
            tvYouAreNotAllowed.setVisibility(TextView.VISIBLE);
            tvYouAreNotAllowed.setText(getResources().getString(
                    R.string.da_already_voted));
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            // oy verme sirasinda bir sikinti yasanirsa alert verilecek
            Utils.createAlert(DetailsActivity.this, "Hata", getResources()
                    .getString(R.string.da_voting_rejected), true, "", "Tamam");

        }

    }

    class DownVoteTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                Log.d(getClass().getName(), "In DownVoteTask doInbackground");
                comp.downvote(Utils.locationToJSON(myPosition.latitude,
                        myPosition.longitude));
            } catch (IOException e) {
                Log.e(getClass().getName(), "DownVoteTask failed", e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(getClass().getName(), "In DownVoteTask onPostExecute");
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.da_voting_accepted),
                    Toast.LENGTH_SHORT).show();

            bUpVote.setVisibility(Button.GONE);
            bDownVote.setVisibility(Button.GONE);

            tvYouAreNotAllowed = (TextView) findViewById(R.id.tvYouAreNotAllowed);
            tvYouAreNotAllowed.setVisibility(TextView.VISIBLE);
            tvYouAreNotAllowed.setText(getResources().getString(
                    R.string.da_already_voted));
        }

    }

    private class CommentGetTask extends
            AsyncTask<String, String, List<Comment>> {

        @Override
        protected List<Comment> doInBackground(String... arg0) {
            try {
                return comp.getComments();
            } catch (IOException e) {
                cancel(true);
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Comment> result) {

            TextView tvComment = (TextView) findViewById(R.id.tvComment);
            TextView tvCommentUser = (TextView) findViewById(R.id.tvCommentUser);

            TextView tvComment2 = (TextView) findViewById(R.id.tvComment2);
            TextView tvCommentUser2 = (TextView) findViewById(R.id.tvCommentUser2);

            if (result != null) {
                if (result.size() >= 2) {
                    tvComment.setText(result.get(0).getText().toString());
                    tvCommentUser.setText(result.get(0).getAuthor().getName());

                    tvComment2.setText(result.get(1).getText().toString());
                    tvCommentUser2.setText(result.get(1).getAuthor().getName());
                } else if (result.size() == 1) {
                    tvComment.setText(result.get(0).getText().toString());
                    tvCommentUser.setText(result.get(0).getAuthor().getName());

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
        protected void onCancelled() {
            super.onCancelled();

            Toast.makeText(DetailsActivity.this,
                    getResources().getString(R.string.warning),
                    Toast.LENGTH_LONG).show();

            // Eger bu sorun olusursa activityi oldurup main acitivity e donelim
            finish();
        }
    }

    private class CommentSaveTask extends AsyncTask<String, String, String> {

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
        protected String doInBackground(String... arg0) {

            try {
                comp.comment(arg0[0]);
            } catch (IOException e) {
                e.printStackTrace();
                cancel(true);
                Log.e(TAG, "CommentSaveTask IOException ", e);
            } catch (CommentRejectedException e) {
                e.printStackTrace();
                cancel(true);
                Log.e(TAG, "CommentSaveTask CommentRejectedException ", e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.mca_comment_accepted),
                    Toast.LENGTH_SHORT).show();

            pd.dismiss();

            afterCommentFlag = true;
            Intent i = new Intent(DetailsActivity.this, DetailsActivity.class);
            i.putExtra("class", comp);
            i.putExtra("latitude", myPosition.latitude);
            i.putExtra("longitude", myPosition.longitude);
            startActivity(i);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            pd.dismiss();

            Toast.makeText(DetailsActivity.this,
                    getResources().getString(R.string.mca_comment_rejected),
                    Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        String url = "";

        if (item.getItemId() == R.id.shareOnFacebook) {
            url = "https://www.facebook.com/sharer/sharer.php?u="
                    + comp.getPublicURL();

            Intent twitterIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(url));
            startActivity(twitterIntent);

        } else if (item.getItemId() == R.id.shareOnTwitter) {
            url = "https://twitter.com/intent/tweet?url=" + comp.getPublicURL()
                    + "&text=Problem%20Var!&via=enforceapp";

            Intent twitterIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(url));
            startActivity(twitterIntent);
        }

        return super.onOptionsItemSelected(item);
    }
}
