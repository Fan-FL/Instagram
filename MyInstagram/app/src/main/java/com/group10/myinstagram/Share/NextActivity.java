package com.group10.myinstagram.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.group10.myinstagram.Bluetooth.SendPhotoActivity;
import com.group10.myinstagram.R;
import com.group10.myinstagram.Utils.FirebaseMethods;
import com.group10.myinstagram.Utils.UniversalImageLoader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class NextActivity extends AppCompatActivity {
    private static final String TAG = "NextActivity";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mReference;
    private FirebaseMethods mFirebaseMethods;

    private EditText mCaption;

    private String imgUrl;
    private String mAppend = "file:/";
    private int imageCount = 0;
    private Intent intent;
    private Bitmap bitmap;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: sharing image ?");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);
        mFirebaseMethods = new FirebaseMethods(NextActivity.this);
        mCaption = (EditText) findViewById(R.id.description);

        setupFirebaseAuth();

        ImageView backArrow = (ImageView) findViewById(R.id.ivCloseShare);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing the activity");
                finish();
            }
        });

        TextView shareScreen = (TextView) findViewById(R.id.tvShare);
        shareScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to the final share screen");
                //upload the image to firebase
                Toast.makeText(NextActivity.this, "Attempting to upload new photo.",Toast.LENGTH_SHORT).show();

                String caption = mCaption.getText().toString();
                if (intent.hasExtra(getString(R.string.selected_image))) {
                    imgUrl = intent.getStringExtra(getString(R.string.selected_image));
                    //UniversalImageLoader.setImage(imgUrl, imageView,null, mAppend);
                    mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo), caption, imageCount, imgUrl,null);
                }else if(intent.hasExtra(getString(R.string.selected_bitmap))){
                    bitmap = intent.getParcelableExtra(getString(R.string.selected_bitmap));
                    mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo), caption, imageCount, null,bitmap);
                    //UniversalImageLoader.setImage(imgUrl, imageView,null, mAppend);
                }
                //mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo), caption, imageCount, imgUrl,null);
            }
        });

        setImage();

        TextView bluetoothShare = (TextView) findViewById(R.id.bluetoothshare);
        bluetoothShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: share photo via bluetooth");
                Intent intent = new Intent(NextActivity.this, SendPhotoActivity.class);

                if (intent.hasExtra(getString(R.string.selected_image))) {
                    imgUrl = intent.getStringExtra(getString(R.string.selected_image));
                }
                Log.d(TAG, "send image via bluetooth:"+ imgUrl);
                intent.putExtra(getString(R.string.selected_image), imgUrl);

                startActivity(intent);
            }
        });
    }


    /**
     * get the image url from the incoming intent and displays the chosen image
     */
    private void setImage(){
        intent = getIntent();
        ImageView imageView = (ImageView) findViewById(R.id.imageShare);
        if (intent.hasExtra(getString(R.string.selected_image))) {
            imgUrl = intent.getStringExtra(getString(R.string.selected_image));
            UniversalImageLoader.setImage(imgUrl, imageView,null, mAppend);
        }else if(intent.hasExtra(getString(R.string.selected_bitmap))){
            bitmap = intent.getParcelableExtra(getString(R.string.selected_bitmap));
            imageView.setImageBitmap(bitmap);
        }

    }

    /*
    -------------------------------------Firebase ----------------------------------------------
     */

    /**
     * Setting up Firebase auth object."
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mReference = mFirebaseDatabase.getReference();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if(user != null){
                    Log.d(TAG, "onAuthStateChanged: signed_in: "+ user.getUid());
                }else {
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                }
            }
        };
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                //retrieve images for the user in question
//                imageCount = mFirebaseMethods.getImageCount(dataSnapshot);
//                Log.d(TAG, "onDataChange: "+imageCount);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mAuthStateListener != null){
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

}
