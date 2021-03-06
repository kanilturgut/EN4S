package com.tobbetu.en4s.navigationDrawer;

/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.bugsense.trace.BugSenseHandler;
import com.google.analytics.tracking.android.EasyTracker;
import com.tobbetu.en4s.R;
import com.tobbetu.en4s.Utils;
import com.tobbetu.en4s.announcement.AnnouncementsActivity;
import com.tobbetu.en4s.backend.User;
import com.tobbetu.en4s.circularImageView.CircularImageView;
import com.tobbetu.en4s.complaint.BugListAdapter;
import com.tobbetu.en4s.complaint.Complaint;
import com.tobbetu.en4s.complaint.DetailsActivity;
import com.tobbetu.en4s.complaint.TakePhotoActivity;
import com.tobbetu.en4s.helpers.BetterAsyncTask;
import com.tobbetu.en4s.login.Login;
import com.tobbetu.en4s.service.EnforceService;
import com.tobbetu.en4s.settingsList.SettingsListActivity;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;

@SuppressLint({"NewApi", "ValidFragment"})
public class ListActivity extends Activity {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList, mDrawerMenuList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CircularImageView ivLeftDrawerUseravatar;

    private CharSequence mTitle = "Enforce";
    private static ListView bugList;
    private String[] TITLES = null;
    private String[] MENU_ITEMS = null;
    private int myPosition;
    private RelativeLayout relativeDrawerLayout = null;
    ImageView ivNewComplaintOnMainFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        getActionBar().setTitle(R.string.app_name);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        relativeDrawerLayout = (RelativeLayout) findViewById(R.id.relativeDrawerLayout);
        ivLeftDrawerUseravatar = (CircularImageView) findViewById(R.id.ivLeftDrawerUseravatar);

        // user avatar on the left drawer menu
        findViewById(R.id.relativeLayoLeftDrawerUseravatar).setOnClickListener(
                null);
        User me = Login.getMe();
        Utils.getImageWithoutDomain(this, me.getAvatar(), ivLeftDrawerUseravatar);
        ((TextView) findViewById(R.id.tvLeftMenuUsername))
                .setText(me.getName());
        ((TextView) findViewById(R.id.tvLeftMenuUserEmail)).setText(me
                .getEmail());

        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerMenuList = (ListView) findViewById(R.id.left_drawer_menu_item);

        TITLES = getResources().getStringArray(R.array.title_array);
        MENU_ITEMS = getResources().getStringArray(
                R.array.title_of_drawer_menu_items);

        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new MyDrawerListItemAdapter(this, TITLES));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerMenuList.setAdapter(new MyDrawerListMenuItemAdapter(this,
                MENU_ITEMS));
        mDrawerMenuList
                .setOnItemClickListener(new DrawerMenuItemClickListener());

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open, /* "open drawer" description for accessibility */
                R.string.drawer_close /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View view) {
                getActionBar().setSubtitle(mTitle);
                invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(0);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content
        // view

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {

            case R.id.action_contact:

                if (EnforceService.getLocation().getLatitude() == 0
                        && EnforceService.getLocation().getLongitude() == 0) {
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.ma_no_location),
                            Toast.LENGTH_LONG).show();
                } else {

                    Intent i = new Intent(ListActivity.this,
                            TakePhotoActivity.class);
                    startActivity(i);
                }
                break;
        }

        return super.onOptionsItemSelected(item);

    }

    /* The click listener for ListView in the navigation drawer */
    private class DrawerItemClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            selectItem(position);
            myPosition = position;
        }
    }

    // The click listener for ListView in the navigation drawer menu items, such
    // as new complaint and settings
    private class DrawerMenuItemClickListener implements
            ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {

            // start intents for new activities
            if (position == -1) { // normalde bu sifir olacak
                startActivity(new Intent(ListActivity.this,
                        AnnouncementsActivity.class));
            } else if (position == 0) { // new complaint

                // first close drawer
                mDrawerLayout.closeDrawer(relativeDrawerLayout);

                // need to add sleep or smthng

                // then start new photo activity
                Intent i = new Intent(ListActivity.this,
                        TakePhotoActivity.class);
                startActivity(i);
            } else if (position == -2) { // settings ---- normalde bu 2 olacak
                startActivity(new Intent(ListActivity.this,
                        SettingsListActivity.class));
            }
        }
    }

    private void selectItem(int position) {
        // update the main content by replacing fragments
        Fragment fragment = new ComplaintListFragment();
        Bundle args = new Bundle();
        args.putInt(ComplaintListFragment.ARG_COMPLAINT_NUMBER, position);
        fragment.setArguments(args);

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment).commit();

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(TITLES[position]);
        mDrawerLayout.closeDrawer(relativeDrawerLayout);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setSubtitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Fragment that appears in the "content_frame", shows a planet
     */
    public class ComplaintListFragment extends Fragment {
        public static final String ARG_COMPLAINT_NUMBER = "buglist_number";

        public ComplaintListFragment() {
            // Empty constructor required for fragment subclasses
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_bug_list,
                    container, false);
            int i = getArguments().getInt(ARG_COMPLAINT_NUMBER);
            bugList = (ListView) rootView.findViewById(R.id.lvBugs);
            ComplaintListTask task = new ComplaintListTask();
            task.execute();

            ivNewComplaintOnMainFrame = (ImageView) rootView.findViewById(R.id.ivNewComplaintOnMainFrame);
            ivNewComplaintOnMainFrame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(ListActivity.this, TakePhotoActivity.class);
                    startActivity(i);
                }
            });

            initListener();

            getActivity().setTitle(TITLES[i]);
            return rootView;
        }
    }

    private void initListener() {

        bugList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {

                Complaint temp = (Complaint) bugList.getItemAtPosition(arg2);
                  Intent anIntent = new Intent(ListActivity.this,
                        DetailsActivity.class);
                anIntent.putExtra("class", temp);
                startActivity(anIntent);
            }

        });

    }

    private class ComplaintListTask extends
            BetterAsyncTask<Void, List<Complaint>> {

        @Override
        protected List<Complaint> task(Void... arg0) throws Exception {
            switch (myPosition) {
                case 0: // Hot
                    return Complaint.getHotList(null);
                case 1: // New
                    return Complaint.getNewList(null);
                case 2: // Near
                    return Complaint.getNearList(null, EnforceService.getLocation()
                            .getLatitude(), EnforceService.getLocation()
                            .getLongitude());
                case 3: // Top
                    return Complaint.getTopList(null);
                default:
                    throw new RuntimeException();
            }
        }

        @Override
        protected void onSuccess(List<Complaint> result) {
            bugList.setAdapter(new BugListAdapter(ListActivity.this, result,
                    myPosition));
        }

        @Override
        protected void onFailure(Exception error) {
            if (error instanceof IOException) {
                BugSenseHandler.sendException(error);
                Toast.makeText(ListActivity.this,
                        getResources().getString(R.string.network_failed_msg),
                        Toast.LENGTH_LONG).show();
            } else if (error instanceof JSONException
                    || error instanceof RuntimeException) {
                BugSenseHandler.sendException(error);
                Toast.makeText(ListActivity.this,
                        getResources().getString(R.string.api_changed),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        stopService(new Intent(ListActivity.this, EnforceService.class));
        System.exit(0);
    }
}