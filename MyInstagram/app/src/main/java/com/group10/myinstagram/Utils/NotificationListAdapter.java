package com.group10.myinstagram.Utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.group10.myinstagram.Models.Comment;
import com.group10.myinstagram.Models.Notification;
import com.group10.myinstagram.Models.User;
import com.group10.myinstagram.Models.UserAccountSettings;
import com.group10.myinstagram.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationListAdapter extends ArrayAdapter<Notification> {
    private static final String TAG = "NotificationListAdapter";


    private LayoutInflater mInflater;
    private List<Notification> mNotifications = null;
    private int layoutResource;
    private Context mContext;

    public NotificationListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Notification> objects) {
        super(context, resource, objects);
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;
        this.mNotifications = objects;
    }

    private static class ViewHolder{
        TextView username, notification, createTime;
        CircleImageView profileImage;
        ImageView image;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final NotificationListAdapter.ViewHolder holder;

        if(convertView == null){
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new NotificationListAdapter.ViewHolder();

            holder.notification = (TextView) convertView.findViewById(R.id.notification);
            holder.username = (TextView) convertView.findViewById(R.id.notification_username);
            holder.createTime = (TextView) convertView.findViewById(R.id.notification_time_posted);
            holder.profileImage = (CircleImageView) convertView.findViewById(R.id.notification_profile_image);
            holder.image = (ImageView) convertView.findViewById(R.id.notification_image);

            convertView.setTag(holder);
        }else{
            holder = (NotificationListAdapter.ViewHolder) convertView.getTag();
        }

        //set the notification
        String action = getItem(position).getAction();
        if (action.equals("like")) {
            holder.notification.setText("liked your photo.");
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(
                    getItem(position).getImage_path(),
                    holder.image);
        } else if (action.equals("follow")){
            holder.notification.setText("started following you.");
            holder.image.setVisibility(View.GONE);
        }

        //set the timestamp difference
        String timestampDifference = getTimestampDifference(getItem(position));
        if(!timestampDifference.equals("0")){
            holder.createTime.setText(timestampDifference + " d");
        }else{
            holder.createTime.setText("today");
        }

        //set the username and profile image
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());
        Log.d(TAG, "getView: user id:" + getItem(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for ( DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: username: " +
                            singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                    holder.username.setText(
                            singleSnapshot.getValue(UserAccountSettings.class).getUsername());

                    ImageLoader imageLoader = ImageLoader.getInstance();

                    imageLoader.displayImage(
                            singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),
                            holder.profileImage);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled.");
            }
        });

        return convertView;
    }

    /**
     * Returns a string representing the number of days ago the post was made
     * @return
     */
    private String getTimestampDifference(Notification notification){
        Log.d(TAG, "getTimestampDifference: getting timestamp difference.");

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        sdf.setTimeZone(TimeZone.getTimeZone("Australia/Sydney"));
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimestamp = notification.getCreate_time();
        try{
            timestamp = sdf.parse(photoTimestamp);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24 )));
        }catch (ParseException e){
            Log.e(TAG, "getTimestampDifference: ParseException: " + e.getMessage() );
            difference = "0";
        }
        return difference;
    }
}
