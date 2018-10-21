package com.group10.myinstagram.Models;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class InRangePhoto implements Parcelable {
    public static final Creator<InRangePhoto> CREATOR = new Creator<InRangePhoto>() {
        @Override
        public InRangePhoto createFromParcel(Parcel in) {
            return new InRangePhoto(in);
        }

        @Override
        public InRangePhoto[] newArray(int size) {
            return new InRangePhoto[size];
        }
    };
    Bitmap bitmap;

    public InRangePhoto() {
    }

    public InRangePhoto(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    protected InRangePhoto(Parcel in) {
        bitmap = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public String toString() {
        return "InRangePhoto{" + "bitmap=" + bitmap + '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(bitmap, flags);
    }
}
