package com.group10.myinstagram.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.group10.myinstagram.R;
import com.group10.myinstagram.Utils.BottomNavigationViewHelper;
import com.group10.myinstagram.Utils.FirebaseMethods;
import com.group10.myinstagram.Utils.SectionStatePagerAdapter;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

public class AccountSettingsActivity extends AppCompatActivity {
    private static final String TAG = "AccountSettingsActivity";
    private static final int ACTIVITY_NUM = 4;
    private Context mContext;
    private SectionStatePagerAdapter pagerAdapter;
    private ViewPager mViewPager;
    private RelativeLayout mRelativeLayout;
    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mReference;
    private FirebaseMethods mFirebaseMethods;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accountsetting);
        mContext = AccountSettingsActivity.this;
        Log.d(TAG,"onCreate: started. ");
        mViewPager = (ViewPager) findViewById(R.id.container);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.relLayout1);

        setupSettingsList();
        setupBottomNavigationView();
        setupFragments();
        getIncomingIntent();

        //setup the backarrow for navigating back to "ProfileActivity"
        ImageView backArrow = (ImageView) findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getIncomingIntent(){
        Intent intent = getIntent();
        if(intent.hasExtra(getString(R.string.calling_activity))){
            Log.d(TAG, "getIncomingIntent: received incoming intent from: " + getString(R.string.profile_activity));
            setViewPager(pagerAdapter.getFragmentNumber(getString(R.string.edit_profile_fragment)));
        }
    }

    private void setupFragments(){
        pagerAdapter = new SectionStatePagerAdapter(getSupportFragmentManager());
        //fragment 0
        pagerAdapter.addFragment(new EditProfileFragment(), getString(R.string.edit_profile_fragment));
        //fragment 1
        pagerAdapter.addFragment(new SignOutFragment(), getString(R.string.sign_out_fragment));
    }

    private void setViewPager(int fragmentNumber){
        mRelativeLayout.setVisibility(View.GONE);
        Log.d(TAG, "setViewPager: navigating to fragment: ");
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setCurrentItem(fragmentNumber);

    }
    private void setupSettingsList(){
        ListView listView = (ListView) findViewById(R.id.lvAccountSettings);

        ArrayList<String> options = new ArrayList<>();
        options.add(getString(R.string.edit_profile_fragment));//fragment 0
        options.add(getString(R.string.sign_out_fragment));//fragment 1

        ArrayAdapter adapter = new ArrayAdapter(mContext, android.R.layout.simple_list_item_1,options);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: navigating to fragment #: " + position);
                setViewPager(position);
            }
        });
    }

    /**
     * Bottom Navigation View setup
     */
    private void setupBottomNavigationView(){
        Log.d(TAG,"setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationView bottomNavigationViewEx = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationViewEx);

        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

}
