package com.group10.comp90018.instagramviewer.Utils;

import android.os.Environment;

public class FilePath {

    /**
     * need the android phone to show
     */

    //"storage/emulated/0"
            ///storage/emulated/0/Pictures
    //Return the primary shared/external storage directory.
    public String ROOT_DIR = Environment.getExternalStorageDirectory().getPath();

    public String PICTURES = ROOT_DIR + "/Pictures";
    public String CAMERA = ROOT_DIR + "/DCIM/Camera";

    public String FIREBASE_IMAGE_STORAGE = "photos/users/";
}
