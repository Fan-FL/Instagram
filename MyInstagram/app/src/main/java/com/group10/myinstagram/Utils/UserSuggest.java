package com.group10.myinstagram.Utils;

import android.content.Context;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.group10.myinstagram.Models.User;
import com.group10.myinstagram.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UserSuggest {
    private static final double EARTH_RADIUS = 6378137.0;
    public List<User> users = new ArrayList<>();
    private double myLongitude;
    private double myLatitude;
    private Context mContext;

    /**
     * @param context
     * @param myLongitude my position
     * @param myLatitude
     */
    public UserSuggest(Context context, double myLongitude, double myLatitude) {
        this.myLatitude = myLatitude;
        this.myLongitude = myLongitude;
        this.mContext = context;
        getAllUsersOrderByDistance();
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    /**
     * order users by distance between my position
     *
     * @param users
     * @return
     */
    private List<User> orderByDistance(List<User> users) {
        Collections.sort(users, new Comparator<User>() {

            @Override
            public int compare(User o1, User o2) {
                double dis1 = Math.abs(getDistance(myLongitude, myLatitude, o1.getLongitude(),
                        o1.getLatitude()));
                double dis2 = Math.abs(getDistance(myLongitude, myLatitude, o2.getLongitude(),
                        o2.getLatitude()));
                int i = (int) (dis1 - dis2);
                return i;
            }
        });
        return users;
    }

    /**
     * get distance between two position
     *
     * @param longitude1
     * @param latitude1
     * @param longitude2
     * @param latitude2
     * @return
     */
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
     * get all users from database and order users by distance
     */
    public void getAllUsersOrderByDistance() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child(mContext.getString(R.string.dbname_users)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot bookSnapshot : dataSnapshot.getChildren()) {
                    User user = bookSnapshot.getValue(User.class);
                    users.add(user);
                }
                users = orderByDistance(users);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
