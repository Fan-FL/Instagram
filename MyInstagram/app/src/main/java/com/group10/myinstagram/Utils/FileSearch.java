package com.group10.myinstagram.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class FileSearch {

    /**
     * search a directory and return a list of all directories contained inside
     * @param directory
     * @return
     */
    public static ArrayList<String> getDirectoryPaths(String directory){
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        if(file.exists()){
            pathArray.add(directory);
            File[] listfiles = file.listFiles();
            for(int i = 0; i<listfiles.length; i++){
                if(listfiles[i].isDirectory()){
                    if(hasImage(listfiles[i].getAbsolutePath())){
                        pathArray.add(listfiles[i].getAbsolutePath());
                    }
                }
            }
        }
        return pathArray;
    }

    /**
     * search a directory and return a list of all files contained inside
     * @param directory
     * @return
     */
    public static ArrayList<String> getFilePaths(String directory){
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listfiles = file.listFiles();
        for(int i = 0; i<listfiles.length; i++){
            if(listfiles[i].isFile()){
                pathArray.add(listfiles[i].getAbsolutePath());
            }
        }
        return pathArray;
    }

    /**
     * search a directory and return a list of all files contained inside
     * @param directory
     * @return
     */
    public static ArrayList<String> getImageFilePaths(String directory){
        ImageFileNameFilter imageFileNameFilter = new ImageFileNameFilter();
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listfiles = file.listFiles();
        for(int i = 0; i<listfiles.length; i++){
            if(listfiles[i].isFile()){
                if (imageFileNameFilter.accept(listfiles[i].getAbsolutePath())){
                    pathArray.add(listfiles[i].getAbsolutePath());
                }
            }
        }
        if (!pathArray.isEmpty()){
            Collections.reverse(pathArray);
        }
        return pathArray;
    }

    private static boolean hasImage(String directory){
        ImageFileNameFilter imageFileNameFilter = new ImageFileNameFilter();
        File file = new File(directory);
        File[] listfiles = file.listFiles();
        for(int i = 0; i<listfiles.length; i++){
            if(listfiles[i].isFile()){
                if (imageFileNameFilter.accept(listfiles[i].getAbsolutePath())){
                    return true;
                }
            }
        }
        return false;
    }
}
