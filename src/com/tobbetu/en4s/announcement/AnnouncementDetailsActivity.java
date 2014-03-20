package com.tobbetu.en4s.announcement;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.tobbetu.en4s.R;

public class AnnouncementDetailsActivity extends Activity implements
        OnClickListener {

    private Announcement ann;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement_details);
        getActionBar().hide();

        ann = (Announcement) getIntent().getSerializableExtra(
                "announcement_class");

        if (ann != null) {
            // set text views with announcement informations
            TextView tvAnnouncementTitle = (TextView) findViewById(R.id.tvAnnouncementTitle);
            TextView tvAnnouncementBaslangicTarihi = (TextView) findViewById(R.id.tvAnnouncementBaslangicTarihi);
            TextView tvAnnouncementBitisTarihi = (TextView) findViewById(R.id.tvAnnouncementBitisTarihi);
            TextView tvAnnouncementIlIlce = (TextView) findViewById(R.id.tvAnnouncementIlIlce);
            TextView tvAnnouncementAciklama = (TextView) findViewById(R.id.tvAnnouncementAciklama);

            tvAnnouncementTitle.setText(ann.getTitle());
            tvAnnouncementBaslangicTarihi.setText(millisToDate(ann
                    .getStartDate()));
            tvAnnouncementBitisTarihi.setText(millisToDate(ann.getEndDate()));
            tvAnnouncementIlIlce.setText(ann.getCity() + "/"
                    + ann.getDistrict());
            tvAnnouncementAciklama.setText(ann.getDescription());

            // to get affected areas
            LinearLayout llAnnouncementEtkilenecekBolgeler = (LinearLayout) findViewById(R.id.llAnnouncementEtkilenecekBolgeler);
            Set<String> areas = ann.getAreas();
            for (String s : areas) {
                TextView tvTemp = new TextView(this);
                tvTemp.setText(s);
                tvTemp.setPadding(5, 5, 5, 5);
                llAnnouncementEtkilenecekBolgeler.addView(tvTemp);
            }

            // set listener for share buttons
            findViewById(R.id.bAnnouncementFacebook).setOnClickListener(this);
            findViewById(R.id.bAnnouncementTwitter).setOnClickListener(this);
            findViewById(R.id.bAnnouncementGooglePlus).setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {

        String url = "";

        switch (v.getId()) {
        case R.id.bAnnouncementFacebook:
            url = "https://www.facebook.com/sharer/sharer.php?u="
                    + ann.getSlugURL();
            Intent facebookIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(url));
            startActivity(facebookIntent);
            break;
        case R.id.bAnnouncementTwitter:
            url = "https://twitter.com/intent/tweet?url=" + ann.getSlugURL()
                    + "&text=" + ann.getTitle() + "&via=enforceapp";

            Intent twitterIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(url));
            startActivity(twitterIntent);
            break;
        case R.id.bAnnouncementGooglePlus:
            url = "https://plus.google.com/share?url=" + ann.getSlugURL();

            Intent googlePlusIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(url));
            startActivity(googlePlusIntent);

            break;
        }
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
    }

    @SuppressLint("SimpleDateFormat")
    private String millisToDate(String str) {

        Date date = new Date(Long.parseLong(str));
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }
}
