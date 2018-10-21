package com.group10.myinstagram.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class Notification implements Parcelable {
    public static final Creator<Notification> CREATOR = new Creator<Notification>() {
        @Override
        public Notification createFromParcel(Parcel in) {
            return new Notification(in);
        }

        @Override
        public Notification[] newArray(int size) {
            return new Notification[size];
        }
    };
    private String user_id;
    private String create_time;
    private String action;
    private String image_path;

    public Notification() {
    }

    public Notification(String user_id, String create_time, String action, String image_path) {
        this.user_id = user_id;
        this.create_time = create_time;
        this.action = action;
        this.image_path = image_path;
    }

    protected Notification(Parcel in) {
        user_id = in.readString();
        create_time = in.readString();
        action = in.readString();
        image_path = in.readString();
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getImage_path() {
        return image_path;
    }

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }

    @Override
    public String toString() {
        return "Notification{" + "user_id='" + user_id + '\'' + ", create_time='" + create_time +
                '\'' + ", action='" + action + '\'' + ", image_path='" + image_path + '\'' + '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(user_id);
        dest.writeString(create_time);
        dest.writeString(action);
        dest.writeString(image_path);
    }
}
