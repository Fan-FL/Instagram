package com.group10.myinstagram.Main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.group10.myinstagram.Bluetooth.ReceivePhotoActivity;
import com.group10.myinstagram.Bluetooth.SendPhotoActivity;
import com.group10.myinstagram.Login.LoginActivity;
import com.group10.myinstagram.Models.Comment;
import com.group10.myinstagram.Models.InRangePhoto;
import com.group10.myinstagram.Models.Like;
import com.group10.myinstagram.Models.Photo;
import com.group10.myinstagram.Models.User;
import com.group10.myinstagram.Models.UserAccountSettings;
import com.group10.myinstagram.R;
import com.group10.myinstagram.Share.ShareActivity;
import com.group10.myinstagram.Utils.BottomNavigationViewHelper;
import com.group10.myinstagram.Utils.InRangePhotoListAdapter;
import com.group10.myinstagram.Utils.LocationHelper;
import com.group10.myinstagram.Utils.Permissions;
import com.group10.myinstagram.Utils.UniversalImageLoader;
import com.group10.myinstagram.Utils.UserSuggest;
import com.group10.myinstagram.Utils.UserfeedListAdapter;
import com.group10.myinstagram.Utils.ViewCommentsFragment;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.List;
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Context mContext = MainActivity.this;
    private static final int ACTIVITY_NUM = 0;
    private static final double EARTH_RADIUS = 6378137.0;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    //vars
    private ArrayList<Photo> mPhotos;
    private ArrayList<String> mFollowing;
    private ArrayList<InRangePhoto> mInrangePhotos;
    private ArrayList<String> mfileNames;
    private ListView mListView;
    private UserfeedListAdapter mAdapter;
    private InRangePhotoListAdapter mInrangeAdapter;
    private double myLatitude;
    private double myLongitude;
    private ImageView btnLocation;
    private ImageView btnTime;

    private FrameLayout mFrameLayout;
    private RelativeLayout mRelativeLayout;
    private Intent intent;
    private Bitmap bitmap;

    private static final int VERIFY_PERMISSIONS_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG,"onCreate: starting.");

        mFrameLayout = (FrameLayout) findViewById(R.id.container);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.relLayoutParent);
        mListView = (ListView) findViewById(R.id.listView);

        setupBottomNavigationView();


        Log.d(TAG, "onCreate: without intent.");
        setupFirebaseAuth();
        initImageLoader();

        mFollowing = new ArrayList<>();
        mPhotos = new ArrayList<>();

        ImageView btnRecive = (ImageView) findViewById(R.id.btn_receive);

        btnRecive.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                Log.d(TAG, "onClick: receive photo via bluetooth");
                Intent intent = new Intent(MainActivity.this, ReceivePhotoActivity.class);
                startActivity(intent);
            }
        });

        ImageView btnInrange = (ImageView) findViewById(R.id.btn_inrange);

        btnInrange.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                Log.d(TAG, "onClick: show photo via bluetooth");
                getAllFilePath();

                mInrangePhotos = new ArrayList<>();
                for (String filepath:mfileNames) {
                    File imgFile = new  File(filepath);
                    if(imgFile.exists()) {
                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        InRangePhoto inRangePhoto = new InRangePhoto(myBitmap);
                        mInrangePhotos.add(inRangePhoto);
                    }
                }
                mInrangeAdapter = new InRangePhotoListAdapter(mContext, R.layout.layout_inrange_listitem, mInrangePhotos);
                mListView.setAdapter(mInrangeAdapter);
            }
        });

        btnLocation = (ImageView) findViewById(R.id.btn_location);

        btnLocation.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                Log.d(TAG, "onClick: sort by location");
                displayPhotos(1);

            }
        });

        btnTime = (ImageView) findViewById(R.id.btn_time);

        btnTime.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                Log.d(TAG, "onClick: sort by location");
                displayPhotos(0);

            }
        });

    }

    private void getAllFilePath() {
        mfileNames = new ArrayList<>();
        String path = Environment.getExternalStorageDirectory() + "/DCIM/MyInstagram/Bluetooth/";
        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        for (final File fileEntry : folder.listFiles()) {
            Log.d(TAG, "getAllFilePath: filename: " + fileEntry.getName());
            mfileNames.add(path+fileEntry.getName());
        }
    }

    public void onCommentThreadSelected(Photo photo, String callingActivity){
        Log.d(TAG, "onCommentThreadSelected: selected a comment thread");

        ViewCommentsFragment fragment  = new ViewCommentsFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        args.putString(getString(R.string.main_activity), getString(R.string.main_activity));
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_comments_fragment));
        transaction.commit();
    }

    public void hideLayout(){
        Log.d(TAG, "hideLayout: hiding layout");
        mRelativeLayout.setVisibility(View.GONE);
        mFrameLayout.setVisibility(View.VISIBLE);
    }


    public void showLayout(){
        Log.d(TAG, "hideLayout: showing layout");
        mRelativeLayout.setVisibility(View.VISIBLE);
        mFrameLayout.setVisibility(View.GONE);
    }

    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    private void getFollowing(){
        Log.d(TAG, "getFollowing: searching for following");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found user: " +
                            singleSnapshot.child(getString(R.string.field_user_id)).getValue());

                    mFollowing.add(singleSnapshot.child(getString(R.string.field_user_id)).getValue().toString());
                }
                //add current user to post view
                mFollowing.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
                //get the photos
                getPhotos();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getPhotos(){
        Log.d(TAG, "getPhotos: getting photos");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        for(int i = 0; i < mFollowing.size(); i++){
            final int count = i;
            Query query = reference
                    .child(getString(R.string.dbname_user_photos))
                    .child(mFollowing.get(i))
                    .orderByChild(getString(R.string.field_user_id))
                    .equalTo(mFollowing.get(i));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                        Photo photo = new Photo();
                        Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                        photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                        photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                        photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                        photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                        photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                        photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());
                        if (objectMap.get(getString(R.string.field_latitude)) != null &&
                                objectMap.get(getString(R.string.field_longitude)) != null) {
                            photo.setLatitude(Double.parseDouble(objectMap.get(getString(R.string.field_latitude)).toString()));
                            photo.setLongitude(Double.parseDouble(objectMap.get(getString(R.string.field_longitude)).toString()));
                        } else {
                            photo.setLatitude(0);
                            photo.setLongitude(0);
                        }

                        ArrayList<Like> likes = new ArrayList<Like>();
                        for (DataSnapshot dSnapshot : singleSnapshot
                                .child(getString(R.string.field_likes)).getChildren()){
                            Log.d(TAG, "onDataChange: main activity: load likes:" +
                                    dSnapshot.getValue(Like.class).getUsername());
                            Like like = new Like();
                            like.setUsername(dSnapshot.getValue(Like.class).getUsername());
                            likes.add(like);
                        }
                        photo.setLikes(likes);

                        ArrayList<Comment> comments = new ArrayList<Comment>();
                        for (DataSnapshot dSnapshot : singleSnapshot
                                .child(getString(R.string.field_comments)).getChildren()){
                            Comment comment = new Comment();
                            comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                            comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                            comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                            comments.add(comment);
                        }

                        photo.setComments(comments);
                        mPhotos.add(photo);
                    }
                    if(count >= mFollowing.size() -1){
                        //display our photos
                        displayPhotos(0);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void getLocation() {
        Log.d(TAG, "getUserLocation: get current user location.");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_users))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: get user location.");
                    myLongitude = singleSnapshot.getValue(User.class).getLongitude();
                    myLatitude = singleSnapshot.getValue(User.class).getLatitude();
                    getFollowing();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void displayPhotos(int sortMode){
        if (sortMode == 0) {
            // sort by time
            if(mPhotos != null){
                Collections.sort(mPhotos, new Comparator<Photo>() {
                    @Override
                    public int compare(Photo o1, Photo o2) {
                        return o2.getDate_created().compareTo(o1.getDate_created());
                    }
                });

                mAdapter = new UserfeedListAdapter(mContext, R.layout.layout_userfeed_listitem, mPhotos);
                mListView.setAdapter(mAdapter);
            }
        } else if (sortMode == 1) {
            Log.d(TAG, "displayPhotos: sort by location.");
            // sort by location
            if(mPhotos != null){
                Collections.sort(mPhotos, new Comparator<Photo>() {
                    @Override
                    public int compare(Photo o1, Photo o2) {
                        double dis1 = Math.abs(getDistance(myLongitude, myLatitude, o1.getLongitude(), o1.getLatitude()));
                        double dis2 = Math.abs(getDistance(myLongitude, myLatitude, o2.getLongitude(), o2.getLatitude()));
                        int i = (int) (dis1 - dis2);
                        return i;
                    }
                });

                mAdapter = new UserfeedListAdapter(mContext, R.layout.layout_userfeed_listitem, mPhotos);
                mListView.setAdapter(mAdapter);
            }
        }
    }

    public double getDistance(double longitude1, double latitude1,
                              double longitude2, double latitude2) {
        double Lat1 = rad(latitude1);
        double Lat2 = rad(latitude2);
        double a = Lat1 - Lat2;
        double b = rad(longitude1) - rad(longitude2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(Lat1) * Math.cos(Lat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }

    private double rad(double d) {
        return d * Math.PI / 180.0;
    }

    // navigation bar
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
                getLocation();
                updateLocation(user);
                if(user != null){
                    Log.d(TAG, "onAuthStateChanged: signed_in: "+ user.getUid());
                }else {
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                }
            }
        };
    }

    private void updateLocation(FirebaseUser user) {
        if(checkPermissionArray(Permissions.PERMISSIONS)){
            Location location = LocationHelper.getLocation(user, mContext);
            if(location == null){
                    DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();
                    mReference.child(getString(R.string.dbname_users))
                            .child(user.getUid())
                            .child(getString(R.string.field_longitude))
                            .setValue(37.8136);
                    mReference.child(getString(R.string.dbname_users))
                            .child(user.getUid())
                            .child(getString(R.string.field_latitude))
                            .setValue(144.9631);
                UserSuggest userSuggest = new UserSuggest(this, 37.8136, 144.9631);
            }else {
                DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();
                mReference.child(getString(R.string.dbname_users))
                        .child(user.getUid())
                        .child(getString(R.string.field_longitude))
                        .setValue(location.getLongitude());
                mReference.child(getString(R.string.dbname_users))
                        .child(user.getUid())
                        .child(getString(R.string.field_latitude))
                        .setValue(location.getLatitude());
                UserSuggest userSuggest = new UserSuggest(this, location.getLongitude(), location.getLatitude());
            }

        }else {
            verifyPermissions(Permissions.PERMISSIONS);
        }
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

        int permissionRequest = ActivityCompat.checkSelfPermission(MainActivity.this, permission);

        if(permissionRequest != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "checkPermission: checkPermissions: Permission was not granted for: ");
            return false;
        }else {
            Log.d(TAG, "checkPermission: checkPermissions: Permission was granted for: ");
            return true;
        }
    }

    /**
     * verify all the permissions passed to the array
     * @param permissions
     */
    private void verifyPermissions(String[] permissions) {
        Log.d(TAG, "verifyPermissions: verifying permissions.");

        ActivityCompat.requestPermissions(
                MainActivity.this, permissions,VERIFY_PERMISSIONS_REQUEST
        );

    }
}
