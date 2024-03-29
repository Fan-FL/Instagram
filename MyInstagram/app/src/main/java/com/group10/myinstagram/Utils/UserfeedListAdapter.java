package com.group10.myinstagram.Utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.group10.myinstagram.Main.MainActivity;
import com.group10.myinstagram.Models.Comment;
import com.group10.myinstagram.Models.Like;
import com.group10.myinstagram.Models.Notification;
import com.group10.myinstagram.Models.Photo;
import com.group10.myinstagram.Models.User;
import com.group10.myinstagram.Models.UserAccountSettings;
import com.group10.myinstagram.Profile.ProfileActivity;
import com.group10.myinstagram.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserfeedListAdapter extends ArrayAdapter<Photo> {
    private static final String TAG = "UserfeedListAdapter";
    private static final double EARTH_RADIUS = 6378137.0;
    private LayoutInflater mInflater;
    private int mLayoutResource;
    private Context mContext;
    private DatabaseReference mReference;
    private String currentUsername = "";
    private double longitude;
    private double latitude;

    public UserfeedListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull
            List<Photo> objects) {
        super(context, resource, objects);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayoutResource = resource;
        this.mContext = context;
        mReference = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Log.d(TAG, "getView: position: " + position);
        Log.d(TAG, "getView: photo id: " + getItem(position).getPhoto_id());

        final ViewHolder holder;

        //get the current users username (need for checking likes string)
        getCurrentUsername();

        if (convertView == null) {
            convertView = mInflater.inflate(mLayoutResource, parent, false);
            holder = new ViewHolder();

            holder.username = (TextView) convertView.findViewById(R.id.username);
            holder.image = (SquareImageView) convertView.findViewById(R.id.post_image);
            holder.heartRed = (ImageView) convertView.findViewById(R.id.image_heart_red);
            holder.heartWhite = (ImageView) convertView.findViewById(R.id.image_heart);
            holder.comment = (ImageView) convertView.findViewById(R.id.speech_bubble);
            holder.likes = (TextView) convertView.findViewById(R.id.image_likes);
            holder.comments = (TextView) convertView.findViewById(R.id.image_comments_link);
            holder.caption = (TextView) convertView.findViewById(R.id.image_caption);
            holder.timeDetla = (TextView) convertView.findViewById(R.id.image_time_posted);
            holder.mprofileImage = (CircleImageView) convertView.findViewById(R.id.profile_photo);
            holder.imageLocation = (TextView) convertView.findViewById(R.id.image_location);
            holder.detector = new GestureDetector(mContext, new GestureListener(holder, getItem
                    (position)));
            holder.heart = new LikeAnimation(holder.heartWhite, holder.heartRed);
            holder.photo = getItem(position);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();

        }

        holder.detector = new GestureDetector(mContext, new GestureListener(holder, getItem
                (position)));

        //get likes string
        getLikesString(holder, getItem(position));

        //set the caption
        holder.caption.setText(getItem(position).getCaption());

        //set the comment
        List<Comment> comments = getItem(position).getComments();
        holder.comments.setText("View all " + comments.size() + " comments");
        holder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: loading comment thread for " + getItem(position).getPhoto_id
                        ());
                ((MainActivity) mContext).onCommentThreadSelected(getItem(position), mContext
                        .getString(R.string.main_activity));

                //going to need to do something else?
                ((MainActivity) mContext).hideLayout();

            }
        });

        setCurrentDistance(getItem(position).getLongitude(), getItem(position).getLatitude(),
                holder);

        //set the time it was posted
        // TODO: show minutes difference
        String timestampDifference = getTimestampDifference(getItem(position));
        if (!timestampDifference.equals("0")) {
            holder.timeDetla.setText(timestampDifference + " DAYS AGO");
        } else {
            holder.timeDetla.setText("TODAY");
        }


        //set the profile image
        final ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(getItem(position).getImage_path(), holder.image);


        //get the profile image and username
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id)).equalTo(getItem
                        (position).getUser_id());
        Log.d(TAG, "position: " + position + " getView: user id: " + getItem(position).getUser_id
                ());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                    // currentUsername = singleSnapshot.getValue(UserAccountSettings.class)
                    // .getUsername();

                    Log.d(TAG, "onDataChange: user profile found user: " + singleSnapshot
                            .getValue(UserAccountSettings.class).getUsername());

                    holder.username.setText(singleSnapshot.getValue(UserAccountSettings.class)
                            .getUsername());
                    holder.username.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "onClick: navigating to profile of: " + holder.user
                                    .getUsername());

                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(mContext.getString(R.string.calling_activity),
                                    mContext.getString(R.string.main_activity));
                            intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
                            mContext.startActivity(intent);
                        }
                    });

                    imageLoader.displayImage(singleSnapshot.getValue(UserAccountSettings.class)
                            .getProfile_photo(), holder.mprofileImage);
                    holder.mprofileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "onClick: navigating to profile of: " + holder.user
                                    .getUsername());

                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(mContext.getString(R.string.calling_activity),
                                    mContext.getString(R.string.main_activity));
                            intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
                            mContext.startActivity(intent);
                        }
                    });


                    holder.settings = singleSnapshot.getValue(UserAccountSettings.class);
                    holder.comment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((MainActivity) mContext).onCommentThreadSelected(getItem(position),
                                    mContext.getString(R.string.main_activity));

                            //another thing?
                            ((MainActivity) mContext).hideLayout();
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //get the user object
        Query userQuery = mReference.child(mContext.getString(R.string.dbname_users))
                .orderByChild(mContext.getString(R.string.field_user_id)).equalTo(getItem
                        (position).getUser_id());
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: user object found user: " + singleSnapshot.getValue
                            (User.class).getUsername());

                    holder.user = singleSnapshot.getValue(User.class);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return convertView;
    }

    private void addNewLike(final ViewHolder holder, Photo photo) {
        Log.d(TAG, "addNewLike: adding new like");

        Notification notification = new Notification();
        notification.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        notification.setAction(mContext.getString(R.string.action_like));
        notification.setCreate_time(getTimestamp());
        notification.setImage_path(photo.getImage_path());
        String newNotificationID = FirebaseDatabase.getInstance().getReference().push().getKey();

        FirebaseDatabase.getInstance().getReference().child(mContext.getString(R.string
                .dbname_notification)).child(photo.getUser_id()).child(newNotificationID)
                .setValue(notification);

        String newLikeID = mReference.push().getKey();
        Like like = new Like();
        like.setUsername(currentUsername);

        mReference.child(mContext.getString(R.string.dbname_photos)).child(photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes)).child(newLikeID).setValue(like);

        mReference.child(mContext.getString(R.string.dbname_user_photos)).child(photo.getUser_id
                ()).child(photo.getPhoto_id()).child(mContext.getString(R.string.field_likes))
                .child(newLikeID).setValue(like);

        ArrayList<Like> likes = (ArrayList<Like>) photo.getLikes();
        Like currentLike = new Like(currentUsername);
        likes.add(currentLike);
        photo.setLikes(likes);

        holder.heart.toggleLike();
        getLikesString(holder, photo);
    }

    private String getTimestamp() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("Australia/Sydney"));
        return sdf.format(new java.util.Date());
    }

    private void getCurrentUsername() {
        Log.d(TAG, "getCurrentUsername: retrieving user account settings");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(mContext.getString(R.string.dbname_users)).orderByChild
                (mContext.getString(R.string.field_user_id)).equalTo(FirebaseAuth.getInstance()
                .getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    currentUsername = singleSnapshot.getValue(UserAccountSettings.class)
                            .getUsername();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getLikesString(final ViewHolder holder, Photo photo) {
        Log.d(TAG, "getLikesString: getting likes string");
        Log.d(TAG, "getLikesString: photo id:" + photo.getPhoto_id());
        Log.d(TAG, "getLikesString: likes number: " + photo.getLikes().size());

        try {
            holder.users = new StringBuilder();
            for (Like like : photo.getLikes()) {
                holder.users.append(like.getUsername());
                holder.users.append(",");
            }

            Log.d(TAG, "getLikesString: current string: " + holder.users.toString());
            String[] splitUsers = holder.users.toString().split(",");

            if (holder.users.toString().contains(currentUsername + ",")) {
                holder.likeByCurrentUser = true;
            } else {
                holder.likeByCurrentUser = false;
            }

            int length = photo.getLikes().size();
            Log.d(TAG, "getLikesString: likes length: " + length);
            if (length == 1) {
                holder.likesString = "Liked by " + splitUsers[0];
            } else if (length == 2) {
                holder.likesString = "Liked by " + splitUsers[0] + " and " + splitUsers[1];
            } else if (length == 3) {
                holder.likesString = "Liked by " + splitUsers[0] + ", " + splitUsers[1] + " and "
                        + splitUsers[2];

            } else if (length == 4) {
                holder.likesString = "Liked by " + splitUsers[0] + ", " + splitUsers[1] + ", " +
                        splitUsers[2] + " and " + splitUsers[3];
            } else if (length > 4) {
                holder.likesString = "Liked by " + splitUsers[0] + ", " + splitUsers[1] + ", " +
                        splitUsers[2] + " and " + (splitUsers.length - 3) + " others";
            } else if (length == 0) {
                holder.likesString = "";
            }
            Log.d(TAG, "onDataChange: likes string: " + holder.likesString);
            //setup likes string
            setupLikesString(holder, holder.likesString);
        } catch (NullPointerException e) {
            Log.e(TAG, "getLikesString: NullPointerException: " + e.getMessage());
            holder.likesString = "";
            holder.likeByCurrentUser = false;
            //setup likes string
            setupLikesString(holder, holder.likesString);
        }
    }

    private void setupLikesString(final ViewHolder holder, String likesString) {
        Log.d(TAG, "setupLikesString: likes string:" + holder.likesString);

        if (holder.likeByCurrentUser) {
            Log.d(TAG, "setupLikesString: photo is liked by current user");
            holder.heartWhite.setVisibility(View.GONE);
            holder.heartRed.setVisibility(View.VISIBLE);
            holder.heartRed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return holder.detector.onTouchEvent(event);
                }
            });
        } else {
            Log.d(TAG, "setupLikesString: photo is not liked by current user");
            holder.heartWhite.setVisibility(View.VISIBLE);
            holder.heartRed.setVisibility(View.GONE);
            holder.heartWhite.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return holder.detector.onTouchEvent(event);
                }
            });
        }
        holder.likes.setText(likesString);
    }

    private void setCurrentDistance(final double longitude, final double latitude, final
    ViewHolder holder) {
        Log.d(TAG, "getUserLocation: get current user location.");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(mContext.getString(R.string.dbname_users)).orderByChild
                (mContext.getString(R.string.field_user_id)).equalTo(FirebaseAuth.getInstance()
                .getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: get user location.");
                    double mLongitude = singleSnapshot.getValue(User.class).getLongitude();
                    double mLatitude = singleSnapshot.getValue(User.class).getLatitude();
                    Log.d(TAG, "getUserLocation: current longitude: " + mLongitude);
                    Log.d(TAG, "getUserLocation: current latitude: " + mLatitude);
                    double distance = getDistance(longitude, latitude, mLongitude, mLatitude);

                    if (distance < 10) {
                        holder.imageLocation.setText("nearby");
                    } else if (distance < 1000) {
                        int d = (int) Math.ceil(distance);
                        holder.imageLocation.setText("from " + d + "m away");
                    } else {
                        int d = (int) Math.ceil(distance) % 1000;
                        holder.imageLocation.setText("from " + d + "km away");
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public double getDistance(double longitude1, double latitude1, double longitude2, double
            latitude2) {
        double Lat1 = rad(latitude1);
        double Lat2 = rad(latitude2);
        double a = Lat1 - Lat2;
        double b = rad(longitude1) - rad(longitude2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(Lat1) * Math
                .cos(Lat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }

    private double rad(double d) {
        return d * Math.PI / 180.0;
    }

    /**
     * Returns a string representing the number of days ago the post was made
     *
     * @return
     */
    private String getTimestampDifference(Photo photo) {
        Log.d(TAG, "getTimestampDifference: getting timestamp difference.");

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        sdf.setTimeZone(TimeZone.getTimeZone("Australia/Sydney"));
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimestamp = photo.getDate_created();
        try {
            timestamp = sdf.parse(photoTimestamp);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) /
                    1000 / 60 / 60 / 24)));
        } catch (ParseException e) {
            Log.e(TAG, "getTimestampDifference: ParseException: " + e.getMessage());
            difference = "0";
        }
        return difference;
    }

    static class ViewHolder {
        CircleImageView mprofileImage;
        String likesString;
        TextView username, timeDetla, caption, likes, comments, imageLocation;
        SquareImageView image;
        ImageView heartRed, heartWhite, comment;

        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();
        StringBuilder users;
        boolean likeByCurrentUser;
        LikeAnimation heart;
        GestureDetector detector;
        Photo photo;
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener {

        ViewHolder mHolder;
        Photo mPhoto;

        public GestureListener(ViewHolder holder, Photo photo) {
            mHolder = holder;
            mPhoto = photo;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d(TAG, "onDoubleTap: double tap detected.");

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference.child(mContext.getString(R.string.dbname_photos)).child
                    (mPhoto.getPhoto_id()).child(mContext.getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                        String keyID = singleSnapshot.getKey();

                        //case1: Then user already liked the photo
                        if (mHolder.likeByCurrentUser && singleSnapshot.getValue(Like.class)
                                .getUsername().equals(currentUsername)) {

                            mReference.child(mContext.getString(R.string.dbname_photos)).child
                                    (mPhoto.getPhoto_id()).child(mContext.getString(R.string
                                    .field_likes)).child(keyID).removeValue();

                            mReference.child(mContext.getString(R.string.dbname_user_photos))
                                    .child(mPhoto.getUser_id()).child(mPhoto.getPhoto_id()).child
                                    (mContext.getString(R.string.field_likes)).child(keyID)
                                    .removeValue();

                            ArrayList<Like> likes = (ArrayList<Like>) mPhoto.getLikes();
                            Like currentLike = new Like(currentUsername);
                            likes.remove(currentLike);
                            mPhoto.setLikes(likes);

                            mHolder.heart.toggleLike();
                            getLikesString(mHolder, mPhoto);
                        }
                        //case2: The user has not liked the photo
                        else if (!mHolder.likeByCurrentUser) {
                            //add new like
                            addNewLike(mHolder, mPhoto);
                            break;
                        }
                    }
                    if (!dataSnapshot.exists()) {
                        //add new like
                        addNewLike(mHolder, mPhoto);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            return true;
        }
    }
}
