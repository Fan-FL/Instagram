package com.group10.myinstagram.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class LocationHelper {
    @SuppressLint("MissingPermission")
    public static Location getLocation(FirebaseUser user, Context mContext) {
        String provider = "";
        //获取定位服务
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context
                .LOCATION_SERVICE);
        //获取当前可用的位置控制器
        List<String> list = locationManager.getProviders(true);

        if (list.contains(LocationManager.GPS_PROVIDER)) {
            //是否为GPS位置控制器
            provider = LocationManager.GPS_PROVIDER;
        } else if (list.contains(LocationManager.NETWORK_PROVIDER)) {
            //是否为网络位置控制器
            provider = LocationManager.NETWORK_PROVIDER;
        }

        if (!provider.isEmpty()) {
            return locationManager.getLastKnownLocation(provider);
        } else {
            return null;
        }

    }
}
