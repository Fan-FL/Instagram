package com.group10.comp90018.instagramviewer.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.group10.comp90018.instagramviewer.R;
import com.group10.comp90018.instagramviewer.Utils.BottomNavigationViewHelper;
import com.group10.comp90018.instagramviewer.Utils.GridImageAdapter;
import com.group10.comp90018.instagramviewer.Utils.UniversalImageLoader;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG =  "ProfileActivity";
    private Context mContext = ProfileActivity.this;
    private ProgressBar mProgressBar;
    private ImageView profilePhoto;

    private static final int NUM_GRID_COLUMNS = 3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.d(TAG,"onCreate: started");
        init();

//        setupBottomNavigationView();
//        setupToolbar();
      // setupActivityWidgets();
        //setProfileImage();

        //tempGridSetup();
    }
    
    private void init(){
        Log.d(TAG, "init: inflating " + getString(R.string.profile_fragment));
        ProfileFragment fragment = new ProfileFragment();
        FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container,fragment);
        transaction.addToBackStack(getString(R.string.profile_fragment));
        transaction.commit();
    }
//
//    private void tempGridSetup(){
//        ArrayList<String> imgURLs = new ArrayList<>();
//        imgURLs.add("https://lonelyplanetimages.imgix.net/copilot/images/interest/food-and-drink.jpg?auto=compress&h=800");
//        imgURLs.add("http://www.parkdeanholidays.co.uk/resources/images/foodanddrink/foodMainImg.jpg");
//        imgURLs.add("https://www.crownmelbourne.com.au/getmedia/6f11fc96-32b9-4504-a2db-6b4af6652d60/1501-Melb-Restaurants-Foodcourt-Calatrava-490x346_1.jpg.aspx?width=490&height=346&ext=.jpg");
//        imgURLs.add("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS93VW8cMbmvv5o86SVZnTQZuzVXiffu4jCocnF2KTkEWD-ucH5Zg");
//        imgURLs.add("https://mindbodygreen-res.cloudinary.com/image/upload/w_767,q_auto:eco,f_auto,fl_lossy/org/stocksy_txp85d27985hya100_small_1244277.jpg");
//        imgURLs.add("https://cometlineconsulting.com.au/wp-content/uploads/2017/12/food-industry-trends-1.jpg");
//        imgURLs.add("http://harvestunion.ca/wp-content/uploads/2015/08/HARVEST-200-1024x683.jpg");
//        imgURLs.add("http://cheersbar.com.au/wp-content/uploads/2016/01/CheersBar-Food-76.jpg");
//        imgURLs.add("https://img.taste.com.au/a9tkfBF7/taste/2017/02/fruity-tingle-ice-cream-cones-121035-1.jpg");
//        imgURLs.add("https://img.delicious.com.au/hS5Kno3R/del/2015/10/no-churn-ice-cream-four-ways-15139-2.jpg");
//        imgURLs.add("https://www.newidea.com.au/media/22873/unicorn-icecream.jpg");
//        imgURLs.add("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQCFEWXW_AMSDJznxZ6Wjrao6L1xeFfFswI6v32tilK918HJRZM");
//        imgURLs.add("https://i2.wp.com/media.hungryforever.com/wp-content/uploads/2018/03/21170621/fcbc4511aae03318d4c664cc8712113d.jpg?ssl=1?w=356&strip=all&quality=80");
//        imgURLs.add("http://cdn-assets.indigenous.io/account_1233/4%20Scroller_1476392195637.png");
//        setupImageGrid(imgURLs);
//    }

    private void setupImageGrid(ArrayList<String> imgURLs){
        GridView gridView = findViewById(R.id.gridView);
        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth/NUM_GRID_COLUMNS;
        gridView.setColumnWidth(imageWidth);

        GridImageAdapter adapter = new GridImageAdapter(mContext,R.layout.layout_grid_imageview,"", imgURLs);
        gridView.setAdapter(adapter);

    }

    private void setProfileImage(){
        Log.d(TAG, "setProfileImage: setting profile photo. ");
        String imgURL = "d1cka1o15bmsqv.cloudfront.net/images/items/36149577_285222235382199_3631438502815596544_n.jpg";
        UniversalImageLoader.setImage(imgURL,profilePhoto,mProgressBar,"http://");
    }
    private void setupActivityWidgets(){
        mProgressBar = (ProgressBar) findViewById(R.id.profileProgressBar);
        mProgressBar.setVisibility(View.GONE);
        profilePhoto = (ImageView) findViewById(R.id.profile_photo);
    }


}
