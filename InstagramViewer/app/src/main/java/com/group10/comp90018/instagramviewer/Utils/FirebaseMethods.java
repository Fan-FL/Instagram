package com.group10.comp90018.instagramviewer.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.group10.comp90018.instagramviewer.Main.MainActivity;
import com.group10.comp90018.instagramviewer.Models.Photo;
import com.group10.comp90018.instagramviewer.Models.User;
import com.group10.comp90018.instagramviewer.Models.UserAccountSettings;
import com.group10.comp90018.instagramviewer.Models.UserSettings;
import com.group10.comp90018.instagramviewer.Profile.AccountSettingsActivity;
import com.group10.comp90018.instagramviewer.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class FirebaseMethods {
    private static final String TAG = "FirebaseMethods";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myReference;
    private String userID;
    private StorageReference mStorageReference;

    private Context mContext;
    private double mPhotoUploadProgress = 0;

    public FirebaseMethods(Context context){
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myReference = mFirebaseDatabase.getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mContext = context;

        if(mAuth.getCurrentUser() != null){
            userID = mAuth.getCurrentUser().getUid();
        }
    }

    public int getImageCount(DataSnapshot dataSnapshot){
        int count = 0;
        for(DataSnapshot ds: dataSnapshot.child(mContext.getString(R.string.dbname_user_photos))
            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
            .getChildren()){
            count++;
        }
        return count;
    }

    /**
     * Update 'user_account_settings' node for the current user
     * @param displayName
     * @param website
     * @param description
     * @param phoneNumber
     */
    public void updateUserAccountSettings(String displayName, String website, String description, long phoneNumber){
        Log.d(TAG, "updateUserAccountSettings: updating user account settings. ");
        if(displayName != null){
            myReference.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_display_name))
                    .setValue(displayName);
        }
        if(website != null){
            myReference.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_website))
                    .setValue(website);
        }
        if(description != null){
            myReference.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_description))
                    .setValue(description);
        }
        if(phoneNumber != 0){
            myReference.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_phone_number))
                    .setValue(phoneNumber);
        }
    }


//    public boolean checkIfUsernameExists(String username, DataSnapshot dataSnapshot){
//        Log.d(TAG, "checkIfUsernameExists: checking if "+ username + "already exists");
//
//        User user = new User();
//        for (DataSnapshot ds: dataSnapshot.child(userID).getChildren()){
//            Log.d(TAG, "checkIfUsernameExists: datasnapshot: " + ds);
//            user.setUsername(ds.getValue(User.class).getUsername());
//            Log.d(TAG, "checkIfUsernameExists: username");
//
//            if(StringManipulation.expandUsername(user.getUsername()).equals(username)){
//                    Log.d(TAG, "checkIfUsernameExists: FOUNED A MATCH");
//                    return true;
//            }
//        }
//        return false;
//    }

    /**
     * Register a new email and password to Firebase Authentication
     * @param email
     * @param password
     * @param username
     */
    public void registerNewEmail(final String email, String password, final String username){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail: onComplete: " + task.isSuccessful());

                        if(!task.isSuccessful()){
                            Toast.makeText(mContext, "Failed to authenticate", Toast.LENGTH_SHORT).show();
                        }else if(task.isSuccessful()){
                            //send verification email
                            sendVerficationEmail();
                            userID = mAuth.getCurrentUser().getUid();
                            Log.d(TAG, "onComplete: Authentication changed: "+ userID);
                        }
                    }
                });
    }

    /**
     * Send the email for verifying
     */
    public void sendVerficationEmail(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){

                    }else{
                        Toast.makeText(mContext,"could not sent verfication email.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    /**
     * Default user information setting
     * Add information to the users node
     * Add information to the user account settings
     * @param email
     * @param username
     * @param description
     * @param website
     * @param profile_photo
     */
    public void addNewUser(String email, String username, String description, String website, String profile_photo){
        User user = new User(userID, 1, email, StringManipulation.condenseUsername(username));

        myReference.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .setValue(user);

        UserAccountSettings settings = new UserAccountSettings(
                description,
                username,
                0,
                0,
                0,
                profile_photo,
                StringManipulation.condenseUsername(username),
                website
        );
        myReference.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .setValue(settings);
    }

    /**
     * Retrieves the account_settings for the user currently logged in
     * Database: user_account_settings node
     * @param dataSnapshot
     * @return
     */
    public UserSettings getUserSettings(DataSnapshot dataSnapshot){
        Log.d(TAG, "getUserAccountSettings: retrieving user account settings from firebase. ");

        User user = new User();
        UserAccountSettings settings = new UserAccountSettings();

        for(DataSnapshot ds: dataSnapshot.getChildren()){
            // user_account_settings node
            if(ds.getKey().equals(mContext.getString(R.string.dbname_user_account_settings))) {
                Log.d(TAG, "getUserAccountSettings: datasnapshot: " + ds);

                try {
                    settings.setDisplay_name(
                            ds.child(userID).getValue(UserAccountSettings.class).getDisplay_name()
                    );
                    settings.setUsername(
                            ds.child(userID).getValue(UserAccountSettings.class).getUsername()
                    );
                    settings.setWebsite(
                            ds.child(userID).getValue(UserAccountSettings.class).getWebsite()
                    );
                    settings.setDescription(
                            ds.child(userID).getValue(UserAccountSettings.class).getDescription()
                    );
                    settings.setPosts(
                            ds.child(userID).getValue(UserAccountSettings.class).getPosts()
                    );
                    settings.setProfile_photo(
                            ds.child(userID).getValue(UserAccountSettings.class).getProfile_photo()
                    );
                    settings.setFollowing(
                            ds.child(userID).getValue(UserAccountSettings.class).getFollowing()
                    );
                    settings.setFollowers(
                            ds.child(userID).getValue(UserAccountSettings.class).getFollowers()
                    );

                } catch (NullPointerException e) {
                    Log.e(TAG, "getUserAccountSettings: NullPointerException: " + e.getMessage());

                }
            }

                if(ds.getKey().equals(mContext.getString(R.string.dbname_users))) {
                    Log.d(TAG, "getUserAccountSettings: datasnapshot: " + ds);

                    user.setUsername(
                            ds.child(userID).getValue(User.class).getUsername()
                    );
                    user.setEmail(
                            ds.child(userID).getValue(User.class).getEmail()
                    );
                    user.setUser_id(
                            ds.child(userID).getValue(User.class).getUser_id()
                    );
                    user.setPhone_number(
                            ds.child(userID).getValue(User.class).getPhone_number()
                    );
                    Log.d(TAG, "getUserAccountSettings: retrieving user information. " + user.toString());
                }
            }
        return new UserSettings(user,settings);

    }

    /**
     * update user name to database
     * @param username
     */
    public void updateUsername(String username) {
        Log.d(TAG, "updateUsername: updating username" + username);
        myReference.child(mContext.getString(R.string.dbname_users))
                .child(userID).child(mContext.getString(R.string.field_username))
                .setValue(username);

        myReference.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID).child(mContext.getString(R.string.field_username))
                .setValue(username);
        
    }

    /**
     * update email to database
     * @param email
     */
    public void updateEmail(String email) {
        Log.d(TAG, "updateEmail: updating email" + email);
        myReference.child(mContext.getString(R.string.dbname_users))
                .child(userID).child(mContext.getString(R.string.field_email))
                .setValue(email);

    }

    /**
     * get time stamp for the photo updating
     * @return simpleDateFormat.format(new Date());
     */
    private String getTimeStamp(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz",Locale.ENGLISH);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Australia/Victoria"));
        return simpleDateFormat.format(new Date());
    }
    /**
     * add new photos to database
     * @param caption
     */
    private void addPhotoToDatabase(String caption, String url){
        Log.d(TAG, "addPhotoToDatabase: adding image to database");

        //set new photo information
        String tags = StringManipulation.getTags(caption);
        String newPhotoKey = myReference.child(mContext.getString(R.string.dbname_photos)).push().getKey();
        Photo photo = new Photo();
        photo.setCaption(caption);
        photo.setData_created(getTimeStamp());
        photo.setImage_path(url);
        photo.setPhoto_id(newPhotoKey);
        photo.setTags(tags);
        photo.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        //insert into database;
        myReference.child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser()
                .getUid()).child(newPhotoKey).setValue(photo);
        myReference.child(mContext.getString(R.string.dbname_photos)).child(newPhotoKey).setValue(photo);
    }

    public void uploadNewPhoto(String photoType, final String caption, int imageCount, String imgUrl,
            Bitmap bitmap) {
        Log.d(TAG, "uploadNewPhoto: attempting to upload image");
        FilePath filePath = new FilePath();
        if(photoType.equals(mContext.getString(R.string.new_photo))){
            Log.d(TAG, "uploadNewPhoto: upload new photo");
            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final StorageReference storageReference = mStorageReference
                    .child(filePath.FIREBASE_IMAGE_STORAGE+"/" + user_id + "/photo"+(imageCount+1));

            // use the bitmap to convert the image to bitmap
            if(bitmap == null){
                bitmap = PhotoManager.getBitmap(imgUrl);
            }

            byte[] bytes = PhotoManager.getBytesFromBitmap(bitmap,100);
            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri downloadUrl) {
                            Log.d("URL: ", downloadUrl.toString());
                            Toast.makeText(mContext,"photo upload success", Toast.LENGTH_SHORT).show();
                            // This is the complete uri, you can store it to real-time database
                            addPhotoToDatabase(caption, downloadUrl.toString());

                            //navigate to the main feed to the user
                            Intent intent = new Intent(mContext, MainActivity.class);
                            mContext.startActivity(intent);


                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: Photo upload failed");
                    Toast.makeText(mContext,"Photo upload failed", Toast.LENGTH_SHORT).show();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                    if(progress - 15 > mPhotoUploadProgress){
                        Toast.makeText(mContext, "photo upload progress" + String.format("%.0f", progress)+ "%", Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }
                    Log.d(TAG, "onProgress: upload progress" + progress +"done");
                }
            });

            // upload new profile photo
        }else if(photoType.equals(mContext.getString(R.string.profile_photo))){
            Log.d(TAG, "uploadNewPhoto: upload new profile photo");

            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final StorageReference storageReference = mStorageReference
                    .child(filePath.FIREBASE_IMAGE_STORAGE+"/" + user_id + "/profile_photo");

            // use the bitmap to convert the image to bitmap
            if(bitmap == null){
                bitmap = PhotoManager.getBitmap(imgUrl);
            }

            byte[] bytes = PhotoManager.getBytesFromBitmap(bitmap,100);
            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri downloadUrl) {
                            Log.d("URL: ", downloadUrl.toString());
                            Toast.makeText(mContext,"photo upload success", Toast.LENGTH_SHORT).show();
                            // This is the complete uri, you can store it to real-time database
                            // upload the profile photo data to the user_photos node
                            setProfilePhoto(downloadUrl.toString());
                            ((AccountSettingsActivity)mContext).setViewPager(
                                    ((AccountSettingsActivity)mContext).pagerAdapter
                                    .getFragmentNumber(mContext.getString(R.string.edit_profile_fragment))

                            );
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: Photo upload failed");
                    Toast.makeText(mContext,"Photo upload failed", Toast.LENGTH_SHORT).show();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                    if(progress - 15 > mPhotoUploadProgress){
                        Toast.makeText(mContext, "photo upload progress" + String.format("%.0f", progress)+ "%", Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }
                    Log.d(TAG, "onProgress: upload progress" + progress +"done");
                }
            });
        }
    
    }

    private void setProfilePhoto(String downloadUrl) {
        myReference.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mContext.getString(R.string.profile_photo))
                .setValue(downloadUrl);

    }
}
