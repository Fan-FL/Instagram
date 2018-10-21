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
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

import androidx.fragment.app.Fragment;

public class GalleryFragment extends Fragment {
    private static final String TAG = "GalleryFragment";
    private static final int PHOTO_FROM_GALLERY = 1;
    private static final int PHOTO_FROM_CAMERA = 2;

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

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            saveInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
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
                Log.d(TAG, "onClick: navigating to edit screen");
                Intent intent = new Intent(getActivity(), GalleryPhotoEditorActivity.class);
                intent.putExtra(getString(R.string.selected_image), mSelectedImage);
                startActivity(intent);
                Log.d(TAG, "onClick: navigating edit screen");
            }
        });
        init();
        return view;
    }

    private void init() {
        FilePath filePath = new FilePath();

        //check for other folder inside "/storage/emulated/0/DCIM"
        ArrayList<String> pathArray = FileSearch.getDirectoryPaths(filePath.DCIM);
        if (pathArray != null) {
            directories.addAll(pathArray);
        }

        //check for other folder inside "/storage/emulated/0/picture"

        pathArray = FileSearch.getDirectoryPaths(filePath.PICTURES);
        if (pathArray != null) {
            directories.addAll(pathArray);
        }

        //check for other folder inside "/storage/emulated/0/bluetooth"

        pathArray = FileSearch.getDirectoryPaths(filePath.BLUETOOTH);
        if (pathArray != null) {
            directories.addAll(pathArray);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout
                .simple_spinner_item, directories) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // Cast the grid view current item as a text view
                TextView cell = (TextView) super.getView(position, convertView, parent);
                cell.setHeight(50);

                // Return the modified item
                return cell;
            }
        };
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

        directorySpinner.setSelection(0, true);

    }


    private void setupGridView(String selectedDirectory) {
        Log.d(TAG, "setupGridView: directory chosen: " + selectedDirectory);
        final ArrayList<String> imgURLs = FileSearch.getImageFilePaths(selectedDirectory);

        // //set the grid column width
        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth / NUM_GRID_COLUMNS;
        gridView.setColumnWidth(imageWidth);

        //use the grid adapter to adapt the images to gridview
        GridImageAdapter gridImageAdapter = new GridImageAdapter(getActivity(), R.layout
                .layout_grid_imageview, APPEND, imgURLs);
//        gridView.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, 5));
        gridView.setAdapter(gridImageAdapter);
        if (imgURLs.isEmpty()) {
            return;
        }

        //set the first image to be displayed
        setImage(imgURLs.get(0), galleryImages, APPEND);
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

    private void setImage(String imgURL, ImageView image, String append) {
        Log.d(TAG, "setImage: setting image");
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(getActivity()));
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
