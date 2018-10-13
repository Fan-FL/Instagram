package com.group10.comp90018.instagramviewer.Utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.group10.comp90018.instagramviewer.Models.User;
import com.group10.comp90018.instagramviewer.Models.UserAccountSettings;
import com.group10.comp90018.instagramviewer.Models.UserSettings;
import com.group10.comp90018.instagramviewer.R;

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

    /*
     * Register a new email and password to Firebase Authentication
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

    /*
     * Add information to the users node
     * Add information to the user account settings
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

    /*
     * Retrieves the account_settings for the user currently logged in
     * Database: user_account_settings node
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

    public void uploadNewPhoto(String photoType, String caption, int imageCount, String imgUrl) {
        Log.d(TAG, "uploadNewPhoto: attempting to upload image");
        FilePath filePath = new FilePath();
        if(photoType.equals(mContext.getString(R.string.new_photo))){
            Log.d(TAG, "uploadNewPhoto: upload new photo");
            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference storageReference = mStorageReference
                    .child(filePath.FIREBASE_IMAGE_STORAGE+"/" + user_id + "/photo"+(imageCount+1));


        }else if(photoType.equals(mContext.getString(R.string.profile_photo))){
            Log.d(TAG, "uploadNewPhoto: upload new profile photo");

        }
    
    }
}
