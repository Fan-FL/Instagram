package com.group10.comp90018.instagramviewer.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class PhotoManager {
    private static final String TAG = "PhotoManager";

    public static Bitmap getBitmap(String imgUrl){
        File imageFile = new File(imgUrl);
        FileInputStream fileInputStream = null;
        Bitmap bitmap = null;
        try{
            fileInputStream = new FileInputStream(imageFile);
            bitmap = BitmapFactory.decodeStream(fileInputStream);

        }catch (FileNotFoundException e){
            Log.d(TAG, "getBitmap: File not found");
        }finally {
            try {
                fileInputStream.close();
            }catch (IOException e){
                Log.d(TAG, "getBitmap:File not found ");
            }
        }
        return bitmap;
    }

    public static byte[] getBytesFromBitmap(Bitmap bitmap, int quality){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,quality,stream);

        return stream.toByteArray();
    }

}
