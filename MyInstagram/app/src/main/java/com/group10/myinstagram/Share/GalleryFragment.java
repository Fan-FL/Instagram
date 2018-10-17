package com.group10.myinstagram.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.group10.myinstagram.R;
import com.group10.myinstagram.Utils.FilePath;
import com.group10.myinstagram.Utils.FileSearch;
import com.group10.myinstagram.Utils.GridImageAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

import androidx.fragment.app.Fragment;

public class GalleryFragment extends Fragment {
    private static final String TAG = "GalleryFragment";

    //constants
    private static final int NUM_GRID_COLUMNS = 3;
    private static final String APPEND = "file:/";

    //widgets
    private GridView gridView;
    private ImageView galleryImages;
    private ProgressBar mProgressBar;
    private Spinner directorySpinner;

    //vars
    private ArrayList<String> directories;
    private String mSelectedImage;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState){
        View view = inflater.inflate(R.layout.fragment_gallery,container,false);
        galleryImages = (ImageView) view.findViewById(R.id.galleryImageView);
        gridView = (GridView) view.findViewById(R.id.gridView);
        directorySpinner = (Spinner) view.findViewById(R.id.spinnerDirectory);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressbar);
        mProgressBar.setVisibility(view.GONE);
        directories = new ArrayList<>();
        Log.d(TAG, "onCreateView: started");

        ImageView shareClose = (ImageView) view.findViewById(R.id.ivCloseShare);
        shareClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing the gallery fragment");
                getActivity().finish();
            }
        });

        TextView nextScreen = (TextView) view.findViewById(R.id.tvNext);
        nextScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to the final share screen");
                Intent intent = new Intent(getActivity(), NextActivity.class);
                intent.putExtra(getString(R.string.selected_image),mSelectedImage);
                startActivity(intent);
                Log.d(TAG, "onClick: navigating share screen");
            }
        });

        init();
        return view;
    }

    private void init(){
        FilePath filePath = new FilePath();

        //check for other folder inside "/storage/emulated/0/picture"
        if (FileSearch.getDirectoryPaths(filePath.PICTURES) != null){
            directories.add(filePath.PICTURES);
        }

        ArrayList<String> directoryNames = new ArrayList<>();
        for(int i = 0; i < directories.size(); i++){
            int index = directories.get(i).lastIndexOf("/");
            String string = directories.get(i).substring(index);
            directoryNames.add(string);
        }

        directories.add(filePath.CAMERA);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item,directories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        directorySpinner.setAdapter(adapter);

        directorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: " + directories.get(position));
                //setup our image grid for the directory chosen
                setupGridView(directories.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }


    private void setupGridView(String selectedDirectory){
        Log.d(TAG, "setupGridView: directory chosen: " + selectedDirectory);
        final ArrayList<String> imgURLs = FileSearch.getFilePaths(selectedDirectory);

        // //set the grid column width
        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth/NUM_GRID_COLUMNS;
        gridView.setColumnWidth(imageWidth);

        //use the grid adapter to adapt the images to gridview
        GridImageAdapter gridImageAdapter = new GridImageAdapter(getActivity(),R.layout.layout_grid_imageview,APPEND,imgURLs);
        gridView.setAdapter(gridImageAdapter);

        //set the first image to be displayed
        setImage(imgURLs.get(0),galleryImages,APPEND);
        mSelectedImage = imgURLs.get(0);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: selected an image");
                setImage(imgURLs.get(position), galleryImages, APPEND);
                mSelectedImage = imgURLs.get(position);
            }
        });
    }

    private void setImage(String imgURL, ImageView image, String append){
        Log.d(TAG, "setImage: setting image");
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(append + imgURL, image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                mProgressBar.setVisibility(view.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                mProgressBar.setVisibility(view.INVISIBLE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                mProgressBar.setVisibility(view.INVISIBLE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                mProgressBar.setVisibility(view.INVISIBLE);
            }
        });
    }



}