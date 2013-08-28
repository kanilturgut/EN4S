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
import java.util.Arrays;

import uk.co.senab.photoview.PhotoView;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Request;
import com.facebook.Request.GraphUserCallback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.tobbetu.en4s.backend.Complaint;
import com.tobbetu.en4s.backend.Image;
import com.tobbetu.en4s.backend.Login;
import com.tobbetu.en4s.backend.User;
import com.tobbetu.en4s.helpers.CategoryI18n;
import com.tobbetu.en4s.helpers.VoteRejectedException;

public class DetailsActivity extends Activity implements OnClickListener {

    private final String TAG = "DetailsActivity";

    private TextView tvComplaintAdress, tvComplaintTitle, tvComplaintCategory,
            tvReporter, tvReporterDate, tvYouAreNotAllowed;
    private Button bUpVote, bDownVote, bMoreComment /* bShare */;
    // private LinearLayout viewPagerLayout;

    // private ViewPager mViewPager;
    private PhotoView photoView;

    private GoogleMap myMap;

    private Complaint comp = null;
    private User me = Login.getMe();
    private LatLng compPos = null;
    LatLng myPosition = null;

    private boolean isFetching;

    // more comment activitysine gidilip gidilmedigini tutacak.
    private boolean toMoreCommentActivity = false;

    private ImageView ivProblemImage = null;

    private ImageTask imageTask;

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
        bMoreComment = (Button) findViewById(R.id.bMoreComment);
        // bShare = (Button) findViewById(R.id.bShare);
        bUpVote.setOnClickListener(this);
        bDownVote.setOnClickListener(this);
        bMoreComment.setOnClickListener(this);
        // bShare.setOnClickListener(this);

        compPos = new LatLng(comp.getLatitude(), comp.getLongitude());
        myPosition = new LatLng(getIntent().getDoubleExtra("latitude", 0),
                getIntent().getDoubleExtra("longitude", 0));
        Log.i(TAG, "calisti");
        Log.d(TAG, "latitude = " + getIntent().getDoubleExtra("latitude", 0));
        Log.d(TAG, "longitude = " + getIntent().getDoubleExtra("longitude", 0));

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

        // String sporDallari[] = {"Basketbol", "Futbol", "Tenis", "Voleybol",
        // "Hentbol", "Yï¿½zme", "Golf"};
        // ListView lvComments = (ListView)
        // findViewById(R.id.lvCommentOnDetails);
        // lvComments.setAdapter(new ArrayAdapter<String>(this,
        // android.R.layout.simple_list_item_1,sporDallari));

        imageTask = new ImageTask();
        imageTask.execute(0);

    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.i(TAG, "onStop");

        // eger intent ile MoreCommentActivity e gidiliyorsa, artik bu activity
        // oldurulsun
        if (toMoreCommentActivity)
            finish();

        /*
         * Bu sekilde detay sayfasina girilmis olan bir sikayetin, resmi
         * yuklenmeden detay sayfasindan cikinca arka plan da calisan resmi
         * indirme gorevi iptal ediliyor. Boylece kapanma hatasi almiyoruz.
         */
        imageTask.cancel(true);
    }

    // @Override
    // public void onBackPressed() {
    // super.onBackPressed();
    //
    // /**/
    // Intent i = new Intent(this, MainActivity.class);
    // i.putExtra("latitude", myPosition.latitude);
    // i.putExtra("longitude", myPosition.longitude);
    // startActivity(i);
    // }

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
            // Toast.makeText(container.getContext(), "tï¿½klandï¿½",
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

        // private Bitmap bmp = null;

        @Override
        protected String doInBackground(Integer... params) {

            Log.d("ImageTask", "istek yapildi1");
            comp.getImage(params[0], Image.SIZE_512, ivProblemImage);
            Log.d("ImageTask", "istek yapildi");
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

            // LinearLayout.LayoutParams ivParams = new
            // LinearLayout.LayoutParams(
            // tmpWidth, tmpWidth);

            ivProblemImage.setLayoutParams(llParams);

            // int tmpHeight = viewPagerLayout.getLayoutParams().height;
            //
            // if(tmpWidth > 600){
            // tmpWidth = bmp.getWidth();
            // tmpHeight = (int) ((double)(bmp.getWidth() / tmpWidth) *
            // tmpHeight);
            // }
            //
            // Log.e(getClass().getName(), "tmp.width:" + tmpWidth +
            // " , tmpheight : " + tmpHeight + "...." + "bmpW: " +
            // bmp.getWidth() + " , bmpH : " + bmp.getHeight());
            // cropped = Bitmap.createBitmap(bmp, 0, 150, tmpWidth, tmpHeight);
            // Log.e("burasi", "croppe width : " + cropped.getWidth() +
            // " cropped height : " + cropped.getHeight());
            // photoView.setImageBitmap(cropped);
        }
    }

    // Buttons onClickListener
    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.bUpVote) {
        	createAlert(1);          
        } else if (v.getId() == R.id.bDownVote) {
        	createAlert(0);
        } else if (v.getId() == R.id.bMoreComment) { // bMoreComment
            toMoreCommentActivity = true;
            Intent i = new Intent(this, MoreCommentsActivity.class);
            i.putExtra("class", comp);
            i.putExtra("latitude", myPosition.latitude);
            i.putExtra("longitude", myPosition.longitude);
            // ayrica burada liste icerigini de intent icine yerlestirmeliyiz
            // yada baska bir yontem
            startActivity(i);
        }

    }

	private void createAlert(int i) {

		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
		alt_bld.setTitle("Dikkat");
		alt_bld.setCancelable(true);

		alt_bld.setNegativeButton("Ýptal",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});

		if (i == 0) { // down vote
			alt_bld.setMessage("Aþaðý taþýdýðýnýz problemler daha alt sýralarda görünecektir");
			alt_bld.setPositiveButton("Kabul",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							new DownVoteTask().execute();
						}
					});
		} else {
			alt_bld.setMessage("Yukarý taþýdýðýnýz problemler daha üst sýralarda görünecektir");
			alt_bld.setPositiveButton("Kabul",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							new UpVoteTask().execute();
						}
					});
		}
		
		AlertDialog alt_dlg = alt_bld.create();
		alt_dlg.show();

	}
    
    private void startFacebookSession() {

        // if(Session.getActiveSession() == null) {
        isFetching = false;

        Log.d("FACEBOOK", "performFacebookLogin");
        final Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(
                this, Arrays.asList("basic_info", "email"));

        Session.openActiveSession(this, true, new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state,
                    Exception exception) {
                Log.d("FACEBOOK", "call");
                Log.i(TAG, session.isOpened() + "");
                if (session.isOpened() && !isFetching) {
                    Log.d("FACEBOOK", "if (session.isOpened() && !isFetching)");
                    isFetching = true;
                    session.requestNewReadPermissions(newPermissionsRequest);
                    Request getMe = Request.newMeRequest(session,
                            new GraphUserCallback() {
                                @Override
                                public void onCompleted(GraphUser user,
                                        Response response) {
                                    Log.d("FACEBOOK", "onCompleted");
                                    /*
                                     * Burada login icin yapilan shared
                                     * preferences dan farkli olarak yeni bir
                                     * SPref.. kullanilabilir. Bu sekilde
                                     * kullanici uygulamaya facebook ile login
                                     * olmasa bile daha sonraki sikayet
                                     * paylasimlarinda login olmak icin zaman
                                     * kaybetmez.
                                     */
                                }
                            });

                    getMe.executeAsync();
                } else {
                    /*
                     * Bu if else in bir ustunde yapilirsa yani diger if icinde
                     * o zaman baglanti inanimaz derecede uzadi. En az 3 deneme
                     * yaptim ve bu sekilde nedense daha hizli baglanti
                     * gerceklesti.
                     */
                    if (session.isOpened())
                        publishFeedDialog();
                }
            }
        });
        // } else {
        // /*Zaten daha onceden acilmis bir facebook session varsa, tekrar login
        // ile ugrasmadan direk olarak
        // * feed publish ediyoruz.*/
        // publishFeedDialog();
        // }
    }

    private void publishFeedDialog() {
        Bundle params = new Bundle();
        params.putString("name", "Enforce");
        params.putString("caption", "Let's Create Better Cities");
        params.putString("description",
                "Enforce is a mobile app for creating better cities.");
        params.putString("link", "http://enforceapp.com");
        params.putString("picture", "http://enforceapp.com/static/img/icon.png");

        WebDialog feedDialog = (new WebDialog.FeedDialogBuilder(this,
                Session.getActiveSession(), params)).setOnCompleteListener(
                new OnCompleteListener() {

                    @Override
                    public void onComplete(Bundle values,
                            FacebookException error) {
                        if (error == null) {
                            // When the story is posted, echo the success
                            // and the post Id.
                            final String postId = values.getString("post_id");
                            if (postId != null) {
                                Toast.makeText(DetailsActivity.this,
                                        "Posted story, id: " + postId,
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // User clicked the Cancel button
                                Toast.makeText(getApplicationContext(),
                                        "Publish cancelled", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        } else if (error instanceof FacebookOperationCanceledException) {
                            // User clicked the "x" button
                            Toast.makeText(getApplicationContext(),
                                    "Publish cancelled", Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            // Generic, ex: network error
                            Toast.makeText(getApplicationContext(),
                                    "Error posting story", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                }).build();
        feedDialog.show();
        Session.getActiveSession().closeAndClearTokenInformation();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode,
                resultCode, data);
    }

    class UpVoteTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                Log.d(getClass().getName(), "In UpVoteTask doInbackground");
                comp.upvote(me, Utils.locationToJSON(myPosition.latitude,
                        myPosition.longitude));
            } catch (IOException e) {
                Log.e(getClass().getName(), "UpVoteTask failed", e);
            } catch (VoteRejectedException e) {
                // TODO kullaniciya sorunu daha uygun bicimde goster
                Log.e(getClass().getName(), "UpVote rejected", e);
                // Toast.makeText(getApplicationContext(),
                // "Upvote Rejected: " + e.getMessage(),
                // Toast.LENGTH_SHORT).show();

                // UpVoteTask durduruluyor
                cancel(true);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(getClass().getName(), "In UpVoteTask onPostExecute");
            Toast.makeText(getApplicationContext(), "Thanks for your upvote",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            Utils.createAlert(DetailsActivity.this, "Hata",
                    "Oyunuz gecerli degil", true, "", "Tamam");

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
            Toast.makeText(getApplicationContext(), "Thanks for your downvote",
                    Toast.LENGTH_SHORT).show();
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

        if (item.getItemId() == R.id.shareOnFacebook)
            startFacebookSession();
        else if (item.getItemId() == R.id.shareOnTwitter)
            Toast.makeText(this, "It is not available", Toast.LENGTH_SHORT)
                    .show();

        return super.onOptionsItemSelected(item);
    }
}
