package com.group10.myinstagram.Notification;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.group10.myinstagram.Models.Notification;
import com.group10.myinstagram.Models.User;
import com.group10.myinstagram.Profile.ProfileActivity;
import com.group10.myinstagram.R;
import com.group10.myinstagram.Search.SearchActivity;
import com.group10.myinstagram.Utils.BottomNavigationViewHelper;
import com.group10.myinstagram.Utils.NotificationListAdapter;
import com.group10.myinstagram.Utils.UserListAdapter;

import java.util.ArrayList;
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
        setContentView(R.layout.activity_activity_userfeed);
        mListView = (ListView) findViewById(R.id.listView);
        mNotificationList = new ArrayList<>();
        Log.d(TAG, "onCreate: started");

        setupBottomNavigationView();
        getNotification();
    }

    private void getNotification() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_notification))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found notification:" +
                            singleSnapshot.getValue(Notification.class).toString());

                    mNotificationList.add(singleSnapshot.getValue(Notification.class));
                    //update the users list view
                    updateNotificationList();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateNotificationList(){
        Log.d(TAG, "updateUsersList: updating users list");

        mAdapter = new NotificationListAdapter(NotificationActivity.this, R.layout.layout_user_listitem, mNotificationList);

        mListView.setAdapter(mAdapter);
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
