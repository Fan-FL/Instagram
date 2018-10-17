package com.group10.myinstagram.Utils;

public class ImageFileNameFilter {
    public boolean isGif(String file) {
        if (file.toLowerCase().endsWith(".gif")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isJpg(String file) {
        if (file.toLowerCase().endsWith(".jpg")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isPng(String file) {
        if (file.toLowerCase().endsWith(".png")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean accept(String fname) {
        return (isGif(fname) || isJpg(fname) || isPng(fname));

    }
}