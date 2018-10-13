package com.group10.comp90018.instagramviewer.Share;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.design.widget.TabLayout;

import com.group10.comp90018.instagramviewer.R;
import com.group10.comp90018.instagramviewer.Utils.BottomNavigationViewHelper;
import com.group10.comp90018.instagramviewer.Utils.Permissions;
import com.group10.comp90018.instagramviewer.Utils.SectionPagerAdapter;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class ShareActivity extends AppCompatActivity {
    private static final String TAG =  "ShareActivity";
    private ViewPager mViewPaper;
    private Context mContext = ShareActivity.this;

    //constants
    private static final int ACTIVITY_NUM = 2;
    private static final int VERIFY_PERMISSIONS_REQUEST = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        Log.d(TAG,"onCreate: started");

        if(checkPermissionArray(Permissions.PERMISSIONS)){
            setupViewPager();

        }else {
            verifyPermissions(Permissions.PERMISSIONS);
        }

        //setupBottomNavigationView();
    }

    /**
     * return the current tab numbeer
     * 0 = GalleryFragment
     * 1 = PhotoFragment
     * @return
     */
    public int getCurrentTabNumber(){

        return mViewPaper.getCurrentItem();
    }

    /**
     * setup viewpaper for manager the tabs
     */
    private void setupViewPager(){
        SectionPagerAdapter adapter = new SectionPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new GalleryFragment());
        adapter.addFragment(new PhotoFragment());

        mViewPaper = (ViewPager) findViewById(R.id.container);
        mViewPaper.setAdapter(adapter);

        TabLayout tableLayout = (TabLayout) findViewById(R.id.tabsBottom);
        tableLayout.setupWithViewPager(mViewPaper);

        tableLayout.getTabAt(0).setText(getString(R.string.gallery));
        tableLayout.getTabAt(1).setText(getString(R.string.photo));
    }

    /**
     * verify all the permissions passed to the array
     * @param permissions
     */
    private void verifyPermissions(String[] permissions) {
        Log.d(TAG, "verifyPermissions: verifying permissions.");

        ActivityCompat.requestPermissions(
                ShareActivity.this, permissions,VERIFY_PERMISSIONS_REQUEST
        );

    }

    /**
     * Check an array of permissions
     * @param permissions
     * @return
     */
    public boolean checkPermissionArray(String[] permissions) {
        Log.d(TAG, "checkPermissionArray: checking permissions array. ");

        for(int i =0; i< permissions.length; i++){
            String check = permissions[i];
            if(!checkPermission(check)){
                return false;
            }
        }
        return true;
    }

    /**
     * Check a single permission is it has been verified
     * @param permission
     * @return
     */
    public boolean checkPermission(String permission) {
        Log.d(TAG, "checkPermission: checking permission: " + permission);

        int permissionRequest = ActivityCompat.checkSelfPermission(ShareActivity.this, permission);

        if(permissionRequest != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "checkPermission: checkPermissions: Permission was not granted for: ");
            return false;
        }else {
            Log.d(TAG, "checkPermission: checkPermissions: Permission was granted for: ");
            return true;
        }
    }


    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView(){
        Log.d(TAG,"setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavigationView);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
