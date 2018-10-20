package com.group10.myinstagram.Main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.group10.myinstagram.Bluetooth.ReceivePhotoActivity;
import com.group10.myinstagram.Bluetooth.SendPhotoActivity;
import com.group10.myinstagram.Login.LoginActivity;
import com.group10.myinstagram.R;
import com.group10.myinstagram.Utils.BottomNavigationViewHelper;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Context mContext = MainActivity.this;
    private static final int ACTIVITY_NUM = 0;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG,"onCreate: starting.");
        setupFirebaseAuth();
        setupBottomNavigationView();

        Button btnBluetooth = (Button) findViewById(R.id.btn_bluetooth);

        btnBluetooth.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                Log.d(TAG, "onClick: find pair.");
                Intent intent = new Intent(mContext, SendPhotoActivity.class);
                startActivity(intent);
            }
        });

        Button btnReceivePhoto = (Button) findViewById(R.id.btn_receive_photo);

        btnReceivePhoto.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                Log.d(TAG, "onClick: receive photo.");
                Intent intent = new Intent(mContext, ReceivePhotoActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupBottomNavigationView(){
        Log.d(TAG,"setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationView);

        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    // Firebase

    /**
     * checks to see if the user is logged in
     * @param user
     */
    private void checkCurrentUser(FirebaseUser user){
        Log.d(TAG, "checkCurrentUser: checking if user is logged in ");
        if(user == null){
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivity(intent);
        }

    }
    /**
     * Setting up Firebase auth object."
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");
        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //check if the user is logged in
                checkCurrentUser(user);
                if(user != null){
                    Log.d(TAG, "onAuthStateChanged: signed_in: "+ user.getUid());
                }else {
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
        checkCurrentUser(mAuth.getCurrentUser());
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mAuthStateListener != null){
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }
}
