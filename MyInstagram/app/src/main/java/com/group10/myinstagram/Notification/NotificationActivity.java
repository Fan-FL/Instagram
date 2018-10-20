package com.group10.myinstagram.Notification;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.group10.myinstagram.Models.Notification;
import com.group10.myinstagram.Models.User;
import com.group10.myinstagram.R;
import com.group10.myinstagram.Utils.BottomNavigationViewHelper;
import com.group10.myinstagram.Utils.NotificationListAdapter;
import com.group10.myinstagram.Utils.UserListAdapter;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class NotificationActivity extends AppCompatActivity {
    private static final String TAG =  "NotificationActivity";
    private Context mContext = NotificationActivity.this;
    private static final int ACTIVITY_NUM = 3;

    //widgets
    private ListView mListView;

    //vars
    private List<Notification> mNotificationList;
    private NotificationListAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: started");

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
