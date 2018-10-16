package com.group10.comp90018.instagramviewer.Share;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.group10.comp90018.instagramviewer.R;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.PhotoEditorView;
import ja.burhanrashid52.photoeditor.PhotoFilter;

public class GalleryFragment extends Fragment {
    private static final String TAG = "GalleryFragment";
    private static final int PHOTO_FROM_GALLERY = 1;
    private static final int PHOTO_FROM_CAMERA = 2;

    //constants
    private static final int NUM_GRID_COLUMNS = 3;
    private static final String APPEND = "file:/";

    //widgets
//    private GridView gridView;
//    private ImageView galleryImages;
//    private ProgressBar mProgressBar;
    private Spinner directorySpinner;
    private Button choosePicButton;
    private Button cropButton;
    private Button blackWhiteButton;
    private Button sharpenButton;
    private Button documentaryButton;
    private PhotoEditorView mPhotoEditorView;
    private PhotoEditor mPhotoEditor;

    //vars
    private ArrayList<String> directories;
    private String mSelectedImage;

    private Uri imageUri;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState){
        View view = inflater.inflate(R.layout.fragment_gallery,container,false);
//        galleryImages = (ImageView) view.findViewById(R.id.galleryImageView);
//        gridView = (GridView) view.findViewById(R.id.gridView);
        mPhotoEditorView = view.findViewById(R.id.photoEditorView);
        directorySpinner = (Spinner) view.findViewById(R.id.spinnerDirectory);
//        mProgressBar = (ProgressBar) view.findViewById(R.id.progressbar);
//        mProgressBar.setVisibility(view.GONE);
        directories = new ArrayList<>();
        choosePicButton = (Button)view.findViewById(R.id.choose_pic_button);
        cropButton = (Button)view.findViewById(R.id.cropButton);
        blackWhiteButton = (Button)view.findViewById(R.id.blackWhiteButton);
        sharpenButton = (Button)view.findViewById(R.id.sharpenButton);
        documentaryButton = (Button)view.findViewById(R.id.documentaryButton);
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
        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startUCrop();
            }
        });
        blackWhiteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPhotoEditor != null){
                    mPhotoEditor.setFilterEffect(PhotoFilter.BLACK_WHITE);
                }
            }
        });
        sharpenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPhotoEditor != null){
                    mPhotoEditor.setFilterEffect(PhotoFilter.SHARPEN);
                }
            }
        });
        documentaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPhotoEditor != null){
                    mPhotoEditor.setFilterEffect(PhotoFilter.DOCUMENTARY);
//                    CustomEffect customEffect = new CustomEffect.Builder(EffectFactory.EFFECT_BRIGHTNESS)
//                            .setParameter("brightness", 0.5f)
//                            .build();
//                    mPhotoEditor.setFilterEffect(customEffect);
                }
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

    private String startUCrop(){
        File outDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }
        File outFile = new File(outDir, System.currentTimeMillis() + ".jpg");
        //裁剪后图片的绝对路径
        Log.e("111",outFile.getAbsolutePath());
        String cameraScalePath = outFile.getAbsolutePath();
        Uri destinationUri = Uri.fromFile(outFile);
        UCrop uCrop = UCrop.of(imageUri, destinationUri);
        UCrop.Options options = new UCrop.Options();
        //设置裁剪图片可操作的手势
        options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.ALL);
//        //是否隐藏底部容器，默认显示
//        options.setHideBottomControls(true);
        //是否能调整裁剪框
        options.setFreeStyleCropEnabled(true);
//        //设置toolbar颜色
//        options.setToolbarColor(ActivityCompat.getColor(getActivity(), R.color.white));
//        //设置状态栏颜色
//        options.setStatusBarColor(ActivityCompat.getColor(getActivity(), R.color.white));
        //是否能调整裁剪框
        // options.setFreeStyleCropEnabled(true);
        uCrop.withOptions(options);
        uCrop.start(getActivity(),this);
        return cameraScalePath;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1000:
                //super method removed
                if (resultCode == Activity.RESULT_OK) {
//                    choosePicButton.setVisibility(View.GONE);
                    imageUri = data.getData();
                    mPhotoEditorView.getSource().setImageURI(imageUri);
                    mPhotoEditor = new PhotoEditor.Builder(getActivity(), mPhotoEditorView).build();

//                try {
//                    Bitmap bitmapImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), returnUri);
//                    galleryImages.setImageBitmap(bitmapImage);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                setImage(returnUri.,galleryImages,APPEND);
                }
                break;
            case UCrop.REQUEST_CROP:
                if (resultCode == Activity.RESULT_OK) {
                    final Uri croppedUri = UCrop.getOutput(data);
                    imageUri = croppedUri;
                    mPhotoEditorView.getSource().setImageURI(imageUri);
                    getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, croppedUri));
                } else if (resultCode == UCrop.RESULT_ERROR) {
                    final Throwable cropError = UCrop.getError(data);
                }
                break;
                default:
                    break;
        }
    }

    public boolean saveImageToGallery(Context context, Bitmap bmp) {
        // 首先保存图片
        String storePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "dearxy";
        File appDir = new File(storePath);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            //通过io流的方式来压缩保存图片
            boolean isSuccess = bmp.compress(Bitmap.CompressFormat.JPEG, 60, fos);
            fos.flush();
            fos.close();

            //把文件插入到系统图库
            //MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);

            //保存图片后发送广播通知更新数据库
            Uri uri = Uri.fromFile(file);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            if (isSuccess) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
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

//    private void setImage(String imgURL, ImageView image, String append){
//        Log.d(TAG, "setImage: setting image");
//        ImageLoader imageLoader = ImageLoader.getInstance();
//        imageLoader.displayImage(append + imgURL, image, new ImageLoadingListener() {
//            @Override
//            public void onLoadingStarted(String imageUri, View view) {
//                mProgressBar.setVisibility(view.VISIBLE);
//            }
//
//            @Override
//            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
//                mProgressBar.setVisibility(view.INVISIBLE);
//            }
//
//            @Override
//            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//                mProgressBar.setVisibility(view.INVISIBLE);
//                view.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Log.e("1", "1111");
//                    }
//                });
//            }
//
//            @Override
//            public void onLoadingCancelled(String imageUri, View view) {
//                mProgressBar.setVisibility(view.INVISIBLE);
//            }
//        });
//    }



}
