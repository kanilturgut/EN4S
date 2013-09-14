/*
 * Copyright (C) 2013 Andreas Stuetz <andreas.stuetz@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tobbetu.en4s;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ListView;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.tobbetu.en4s.backend.Complaint;
import com.tobbetu.en4s.helpers.BetterAsyncTask;

public class TabCreator extends Fragment {

    private static final String ARG_POSITION = "position";
    private ListView bugList = null;
    private int position;
    private String TAG = "TabCreator";

    private double latitude = 0;
    private double longitude = 0;

    public static TabCreator newInstance(int position) {
        TabCreator f = new TabCreator();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        latitude = getActivity().getIntent().getDoubleExtra("latitude", 0);
        longitude = getActivity().getIntent().getDoubleExtra("longitude", 0);

        Log.d(TAG, "latitude : " + latitude + ", longitude : " + longitude);

        position = getArguments().getInt(ARG_POSITION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        FrameLayout frameLayout = new FrameLayout(getActivity());
        final int margin = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8, getResources()
                        .getDisplayMetrics());

        bugList = new ListView(getActivity());
        params.setMargins(margin, margin, margin, margin);
        bugList.setLayoutParams(params);

        ComplaintListTask task = new ComplaintListTask();
        task.execute();
        frameLayout.addView(bugList);

        initListener();

        return frameLayout;
    }

    private void initListener() {

        bugList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {

                Complaint temp = (Complaint) bugList.getItemAtPosition(arg2);
                Intent anIntent = new Intent(getActivity(),
                        DetailsActivity.class);
                anIntent.putExtra("class", temp);
                anIntent.putExtra("latitude", latitude);
                anIntent.putExtra("longitude", longitude);
                startActivity(anIntent);
            }

        });

    }

    @Override
    public void onPause() {
        super.onPause();

        // TODO kanil will implement here
    }

    private class ComplaintListTask extends
            BetterAsyncTask<Void, List<Complaint>> {

        @Override
        protected List<Complaint> task(Void... arg0) throws Exception {
            switch (position) {
            case 0: // Hot
                return Complaint.getHotList();
            case 1: // New
                return Complaint.getNewList();
            case 2: // Near
                return Complaint.getNearList(latitude, longitude);
            case 3: // Top
                return Complaint.getTopList();
            default:
                throw new RuntimeException();
            }
        }

        @Override
        protected void onSuccess(List<Complaint> result) {
            bugList.setAdapter(new BugListAdapter(
                    TabCreator.this.getActivity(), result, position, latitude,
                    longitude));
        }

        @Override
        protected void onFailure(Exception error) {
            if (error instanceof IOException) {
                BugSenseHandler.sendException(error);
                Toast.makeText(TabCreator.this.getActivity(),
                        getResources().getString(R.string.network_failed_msg),
                        Toast.LENGTH_LONG).show();
            } else if (error instanceof JSONException
                    || error instanceof RuntimeException) {
                BugSenseHandler.sendException(error);
                Toast.makeText(TabCreator.this.getActivity(),
                        getResources().getString(R.string.api_changed),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

}