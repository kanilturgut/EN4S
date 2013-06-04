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

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ListView;

public class TabCreator extends Fragment {

	private static final String ARG_POSITION = "position";
	private ListView bugList = null;
	private int position;

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

		position = getArguments().getInt(ARG_POSITION);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(position == 0){
			return createHottestList();
		}
		else if(position == 1){
			return createNewestList();
		}
		else if(position == 2){
			return createNeariestList();
		} else {
			return createTopList();
		}
	}

	public View createHottestList(){
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		FrameLayout frameLayout = new FrameLayout(getActivity());
		final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());

		bugList = new ListView(getActivity());
		params.setMargins(margin, margin, margin, margin);
		bugList.setLayoutParams(params);

		ArrayList<Complaint> complaints = new ArrayList<Complaint>();

		complaints.add(new Complaint("Sorun1", "31.05.2013", "Burasi"));
		complaints.add(new Complaint("Polis", "31.05.2013", "Taksim & kugulu Park"));
		complaints.add(new Complaint("ED", "Always", "TOBB ETU"));

		bugList.setAdapter(new BugListAdapter(getActivity(), complaints));
		frameLayout.addView(bugList);

		initListener();

		return frameLayout;
	}

	public View createNewestList(){
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		FrameLayout frameLayout = new FrameLayout(getActivity());
		final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());

		bugList = new ListView(getActivity());
		params.setMargins(margin, margin, margin, margin);
		bugList.setLayoutParams(params);

		ArrayList<Complaint> complaints = new ArrayList<Complaint>();

		complaints.add(new Complaint("Sorun2", "31.05.2013", "Burasi"));
		complaints.add(new Complaint("Polis", "31.05.2013", "Taksim & kugulu Park"));
		complaints.add(new Complaint("Büyük Sorun", "12.11.2002", "TURKEY"));
		complaints.add(new Complaint("OE", "Often", "TOBB ETU"));

		bugList.setAdapter(new BugListAdapter(getActivity(), complaints));
		frameLayout.addView(bugList);

		initListener();
		
		return frameLayout;
	}

	public View createNeariestList(){
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		FrameLayout frameLayout = new FrameLayout(getActivity());
		final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());

		bugList = new ListView(getActivity());
		params.setMargins(margin, margin, margin, margin);
		bugList.setLayoutParams(params);

		ArrayList<Complaint> complaints = new ArrayList<Complaint>();

		complaints.add(new Complaint("Sorun3", "31.05.2013", "Burasi"));
		complaints.add(new Complaint("Polis", "31.05.2013", "Taksim & kugulu Park"));
		complaints.add(new Complaint("ED", "Always", "TOBB ETU"));

		bugList.setAdapter(new BugListAdapter(getActivity(), complaints));
		frameLayout.addView(bugList);

		initListener();
		
		return frameLayout;
	}

	public View createTopList(){
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		FrameLayout frameLayout = new FrameLayout(getActivity());
		final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());

		bugList = new ListView(getActivity());
		params.setMargins(margin, margin, margin, margin);
		bugList.setLayoutParams(params);

		ArrayList<Complaint> complaints = new ArrayList<Complaint>();

		complaints.add(new Complaint("Sorun4", "31.05.2013", "Burasi"));
		complaints.add(new Complaint("Polis", "31.05.2013", "Taksim & kugulu Park"));
		complaints.add(new Complaint("ED", "Always", "TOBB ETU"));

		bugList.setAdapter(new BugListAdapter(getActivity(), complaints));
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
				Intent anIntent = new Intent(getActivity(), DetailsActivity.class);
				anIntent.putExtra("class", temp);
				startActivity(anIntent);
			}

		});
		
	}
}