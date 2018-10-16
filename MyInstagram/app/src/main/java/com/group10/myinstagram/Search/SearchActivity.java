package com.group10.myinstagram.Search;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.group10.myinstagram.R;
import com.group10.myinstagram.Utils.BottomNavigationViewHelper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SearchActivity extends AppCompatActivity {
    private static final String TAG =  "SearchActivity";
    private Context mContext = SearchActivity.this;
    private static final int ACTIVITY_NUM = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG,"onCreate: started");

        setupBottomNavigationView();
    }

    private void setupBottomNavigationView(){
        Log.d(TAG,"setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationView);

        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
