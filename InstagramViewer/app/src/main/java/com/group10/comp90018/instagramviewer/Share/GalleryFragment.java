package com.group10.comp90018.instagramviewer.Share;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.group10.comp90018.instagramviewer.R;
import com.group10.comp90018.instagramviewer.Utils.FilePath;
import com.group10.comp90018.instagramviewer.Utils.FileSearch;
import com.group10.comp90018.instagramviewer.Utils.GridImageAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.IOException;
import java.util.ArrayList;

public class GalleryFragment extends Fragment {
    private static final String TAG = "GalleryFragment";
    private static final int PHOTO_FROM_GALLERY = 1;
    private static final int PHOTO_FROM_CAMERA = 2;

    //constants
    private static final int NUM_GRID_COLUMNS = 3;
    private static final String APPEND = "file:/";

    //widgets
//    private GridView gridView;
    private ImageView galleryImages;
    private ProgressBar mProgressBar;
    private Spinner directorySpinner;
    private Button choosePicButton;

    //vars
    private ArrayList<String> directories;
    private String mSelectedImage;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState){
        View view = inflater.inflate(R.layout.fragment_gallery,container,false);
        galleryImages = (ImageView) view.findViewById(R.id.galleryImageView);
//        gridView = (GridView) view.findViewById(R.id.gridView);
        directorySpinner = (Spinner) view.findViewById(R.id.spinnerDirectory);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressbar);
        mProgressBar.setVisibility(view.GONE);
        directories = new ArrayList<>();
        choosePicButton = (Button)view.findViewById(R.id.choose_pic_button);
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

        choosePicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGallery();
            }
        });

        init();
        return view;
    }

    private void startGallery() {
        Intent cameraIntent = new Intent(Intent.ACTION_GET_CONTENT);
        cameraIntent.setType("image/*");
        if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(cameraIntent, 1000);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super method removed
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1000) {
                Uri returnUri = data.getData();
                try {
                    Bitmap bitmapImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), returnUri);
                    galleryImages.setImageBitmap(bitmapImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
//
//                setImage(returnUri.,galleryImages,APPEND);
//
            }
        }
    }

    private void init(){
//        startGallery();

//        FilePath filePath = new FilePath();
//
//        //check for other folder inside "/storage/emulated/0/DCIM"
//        if (FileSearch.getDirectoryPaths(filePath.DCIM) != null){
//            directories.add(filePath.DCIM);
//        }
//
//        directories.add(filePath.CAMERA);
//
//        //check for other folder inside "/storage/emulated/0/picture"
//        if (FileSearch.getDirectoryPaths(filePath.PICTURES) != null){
//            directories.add(filePath.PICTURES);
//        }
//
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
//                android.R.layout.simple_spinner_item,directories);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        directorySpinner.setAdapter(adapter);
//
//        directorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                Log.d(TAG, "onItemClick: " + directories.get(position));
//                //setup our image grid for the directory chosen
//                setupGridView(directories.get(position));
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//
//        directorySpinner.setSelection(0, true);

    }


//    private void setupGridView(String selectedDirectory){
//        Log.d(TAG, "setupGridView: directory chosen: " + selectedDirectory);
//        final ArrayList<String> imgURLs = FileSearch.getImageFilePaths(selectedDirectory);
//
//        // //set the grid column width
//        int gridWidth = getResources().getDisplayMetrics().widthPixels;
//        int imageWidth = gridWidth/NUM_GRID_COLUMNS;
//        gridView.setColumnWidth(imageWidth);
//        gridView.set
//        //use the grid adapter to adapt the images to gridview
//        GridImageAdapter gridImageAdapter = new GridImageAdapter(getActivity(),R.layout.layout_grid_imageview,APPEND,imgURLs);
//        gridView.setAdapter(gridImageAdapter);
//
//        if (imgURLs.isEmpty()){
//            return;
//        }
//
//        //set the first image to be displayed
//        setImage(imgURLs.get(0),galleryImages,APPEND);
//        mSelectedImage = imgURLs.get(0);
//
//        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Log.d(TAG, "onItemClick: selected an image");
//                setImage(imgURLs.get(position), galleryImages, APPEND);
//                mSelectedImage = imgURLs.get(position);
//            }
//        });
//    }

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
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.e("1", "1111");
                    }
                });
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                mProgressBar.setVisibility(view.INVISIBLE);
            }
        });
    }



}
