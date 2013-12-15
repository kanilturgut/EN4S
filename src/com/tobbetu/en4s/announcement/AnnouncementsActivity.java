package com.tobbetu.en4s.announcement;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.tobbetu.en4s.R;
import com.tobbetu.en4s.helpers.BetterAsyncTask;

public class AnnouncementsActivity extends Activity {

    private ListView lvAnnouncements;
    private AnnouncementListTask task = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcements);

        lvAnnouncements = (ListView) findViewById(R.id.lvAnnouncement);
        lvAnnouncements.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {

                // stop the async task
                if (task != null)
                    task.cancel(true);

                Announcement tmp = (Announcement) lvAnnouncements
                        .getItemAtPosition(arg2);
                Intent i = new Intent(AnnouncementsActivity.this,
                        AnnouncementDetailsActivity.class);
                i.putExtra("announcement_class", tmp);
                startActivity(i);
            }
        });

        task = new AnnouncementListTask();
        task.execute();

    }

    private class AnnouncementListTask extends
            BetterAsyncTask<Void, List<Announcement>> {

        @Override
        protected List<Announcement> task(Void... arg0) throws Exception {
            return Announcement.getList("/notification/ankara");
        }

        @Override
        protected void onSuccess(List<Announcement> result) {
            lvAnnouncements.setAdapter(new AnnouncementListAdapter(
                    AnnouncementsActivity.this, result));
        }

        @Override
        protected void onFailure(Exception error) {
            if (error instanceof IOException) {
                Toast.makeText(AnnouncementsActivity.this,
                        getResources().getString(R.string.network_failed_msg),
                        Toast.LENGTH_LONG).show();
            } else if (error instanceof JSONException
                    || error instanceof RuntimeException) {
                Toast.makeText(AnnouncementsActivity.this,
                        getResources().getString(R.string.api_changed),
                        Toast.LENGTH_LONG).show();
            }

            // we should kill activity
            AnnouncementsActivity.this.finish();
        }
    }
}
