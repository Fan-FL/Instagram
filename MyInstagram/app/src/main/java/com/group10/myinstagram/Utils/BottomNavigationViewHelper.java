package com.group10.myinstagram.Utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.group10.myinstagram.Main.MainActivity;
import com.group10.myinstagram.Notification.NotificationActivity;
import com.group10.myinstagram.Profile.ProfileActivity;
import com.group10.myinstagram.R;
import com.group10.myinstagram.Search.SearchActivity;
import com.group10.myinstagram.Share.ShareActivity;

import androidx.annotation.NonNull;

public class BottomNavigationViewHelper {
    private static final String TAG = "BottomNavViewHelper";

    public static void enableNavigation(final Context context, BottomNavigationView view){
        view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.ic_home://ACTIVITY_NUM = 0
                        Intent intent1 = new Intent(context, MainActivity.class);
                        context.startActivity(intent1);
                        break;
                    case R.id.ic_search://ACTIVITY_NUM = 1
                        Intent intent2 = new Intent(context, SearchActivity.class);
                        context.startActivity(intent2);
                        break;
                    case R.id.ic_add://ACTIVITY_NUM = 2
                        Intent intent3 = new Intent(context, ShareActivity.class);
                        context.startActivity(intent3);
                        break;
                    case R.id.ic_notifications://ACTIVITY_NUM = 3
                        Intent intent4 = new Intent(context, NotificationActivity.class);
                        context.startActivity(intent4);
                        break;
                    case R.id.ic_profile://ACTIVITY_NUM = 4
                        Intent intent5 = new Intent(context, ProfileActivity.class);
                        context.startActivity(intent5);
                        break;
                }
                return false;
            }
        });
    }
}
