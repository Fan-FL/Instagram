package com.group10.myinstagram.Utils;

import com.group10.myinstagram.Models.User;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UserSuggest {
    private static final double EARTH_RADIUS = 6378137.0;

    private double myLongitude;
    private double myLatitude;

    public List<User> orderByDistance(List<User> users, final double myLongitude, final double myLatitude){
        Collections.sort(users, new Comparator<User>() {

            @Override
            public int compare(User o1, User o2) {
                double dis1 = Math.abs(getDistance(myLongitude, myLatitude, o1.getLongitude(), o1.getLatitude()));
                double dis2 = Math.abs(getDistance(myLongitude, myLatitude, o2.getLongitude(), o2.getLatitude()));
                int i = (int) (dis1 - dis2);
                return i;
            }
        });
        return users;
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
}
