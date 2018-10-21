package com.group10.myinstagram.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.group10.myinstagram.Models.User;
import com.group10.myinstagram.R;
import com.group10.myinstagram.Utils.BottomNavigationViewHelper;
import com.group10.myinstagram.Utils.GridImageAdapter;
import com.group10.myinstagram.Utils.UniversalImageLoader;
import com.group10.myinstagram.Utils.ViewProfileFragment;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    private static final int NUM_GRID_COLUMNS = 3;
    private static final int ACTIVITY_NUM = 4;
    private Context mContext = ProfileActivity.this;
    private ProgressBar mProgressBar;
    private ImageView profilePhoto;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.d(TAG, "onCreate: started");
        init();
        setupBottomNavigationView();
    }

    private void init() {
        Log.d(TAG, "init: inflating " + getString(R.string.profile_fragment));

        Intent intent = getIntent();
        if (intent.hasExtra(getString(R.string.calling_activity))) {
            Log.d(TAG, "init: searching for user object attached as intent extra");
            if (intent.hasExtra(getString(R.string.intent_user))) {
                User user = intent.getParcelableExtra(getString(R.string.intent_user));
                if (!user.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid
                        ())) {
                    Log.d(TAG, "init: inflating view profile");
                    ViewProfileFragment fragment = new ViewProfileFragment();
                    Bundle args = new Bundle();
                    args.putParcelable(getString(R.string.intent_user), intent.getParcelableExtra
                            (getString(R.string.intent_user)));
                    fragment.setArguments(args);

                    FragmentTransaction transaction = getSupportFragmentManager()
                            .beginTransaction();
                    transaction.replace(R.id.container, fragment);
                    transaction.addToBackStack(getString(R.string.view_profile_fragment));
                    transaction.commit();
                } else {
                    Log.d(TAG, "init: inflating Profile");
                    ProfileFragment fragment = new ProfileFragment();
                    FragmentTransaction transaction = ProfileActivity.this
                            .getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, fragment);
                    transaction.addToBackStack(getString(R.string.profile_fragment));
                    transaction.commit();
                }
            } else {
                Toast.makeText(mContext, "something went wrong", Toast.LENGTH_SHORT).show();
            }

        } else {
            Log.d(TAG, "init: inflating Profile");
            ProfileFragment fragment = new ProfileFragment();
            FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.container, fragment);
            transaction.addToBackStack(getString(R.string.profile_fragment));
            transaction.commit();
        }
    }


    private void setupImageGrid(ArrayList<String> imgURLs) {
        GridView gridView = findViewById(R.id.gridView);
        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth / NUM_GRID_COLUMNS;
        gridView.setColumnWidth(imageWidth);

        GridImageAdapter adapter = new GridImageAdapter(mContext, R.layout.layout_grid_imageview,
                "", imgURLs);
        gridView.setAdapter(adapter);

    }

    private void setProfileImage() {
        Log.d(TAG, "setProfileImage: setting profile photo. ");
        String imgURL = "d1cka1o15bmsqv.cloudfront" +
                ".net/images/items/36149577_285222235382199_3631438502815596544_n.jpg";
        UniversalImageLoader.setImage(imgURL, profilePhoto, mProgressBar, "http://");
    }

    private void setupActivityWidgets() {
        mProgressBar = (ProgressBar) findViewById(R.id.profileProgressBar);
        mProgressBar.setVisibility(View.GONE);
        profilePhoto = (ImageView) findViewById(R.id.profile_photo);
    }

    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id
                .bottom_navigation);
        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationView);

        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }


}
