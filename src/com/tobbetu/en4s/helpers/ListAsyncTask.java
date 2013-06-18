package com.tobbetu.en4s.helpers;

import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.ListView;

import com.tobbetu.en4s.BugListAdapter;
import com.tobbetu.en4s.backend.Complaint;

public class ListAsyncTask extends
        AsyncTask<String, String, List<Complaint>> {

    private ListView lw;
    private Activity activity;
    private ContentProviderFunction<Complaint> fn;
    private int position;

    public ListAsyncTask(Activity activity, ListView lw,
            ContentProviderFunction<Complaint> fn, int pos) {
        this.lw = lw;
        this.activity = activity;
        this.fn = fn;
        this.position = pos;
    }

    @Override
    protected List<Complaint> doInBackground(String... arg0) {
        return fn.getContent();
    }

    @Override
    protected void onPostExecute(List<Complaint> result) {
        super.onPostExecute(result);
        lw.setAdapter(new BugListAdapter(activity, result, position));
    }

}
