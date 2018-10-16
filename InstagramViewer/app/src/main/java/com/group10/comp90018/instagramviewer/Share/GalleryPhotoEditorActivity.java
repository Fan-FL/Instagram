package com.group10.comp90018.instagramviewer.Share;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.group10.comp90018.instagramviewer.R;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.File;
import java.util.ArrayList;

import ja.burhanrashid52.photoeditor.PhotoEditorView;
import ja.burhanrashid52.photoeditor.PhotoFilter;

public class GalleryPhotoEditorActivity extends AppCompatActivity {
    private static final String TAG = "GalleryPhotoEditor";
    private static final int PHOTO_FROM_GALLERY = 1;
    private static final int PHOTO_FROM_CAMERA = 2;

    //constants
    private static final int NUM_GRID_COLUMNS = 3;
    private static final String APPEND = "file:/";

    //widgets
//    private GridView gridView;
//    private ImageView galleryImages;
//    private ProgressBar mProgressBar;
    private Button cropButton;
    private Button blackWhiteButton;
    private Button sharpenButton;
    private Button documentaryButton;
    private PhotoEditorView mPhotoEditorView;
    private ja.burhanrashid52.photoeditor.PhotoEditor mPhotoEditor;
    private String mSelectedImage;

    //vars
    private ArrayList<String> directories;
    private String imagePath;

    private Uri imageUri;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: GalleryPhotoEditorActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_editor);
        mPhotoEditorView = findViewById(R.id.photoEditorView);
        directories = new ArrayList<>();
        cropButton = (Button)findViewById(R.id.cropButton);
        blackWhiteButton = (Button)findViewById(R.id.blackWhiteButton);
        sharpenButton = (Button)findViewById(R.id.sharpenButton);
        documentaryButton = (Button)findViewById(R.id.documentaryButton);
        Log.d(TAG, "onCreateView: started");

        Intent intent = getIntent();
        mSelectedImage = intent.getStringExtra(getString(R.string.selected_image));
        if (!mSelectedImage.isEmpty()){
            File picPath = new File(mSelectedImage);
            imageUri = Uri.fromFile(picPath);
            mPhotoEditorView.getSource().setImageURI(imageUri);
            mPhotoEditor = new ja.burhanrashid52.photoeditor.PhotoEditor.Builder(this, mPhotoEditorView).build();

        }

        ImageView shareClose = (ImageView) findViewById(R.id.ivCloseShare);
        shareClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing the GalleryPhotoEditorActivity");
                finish();
            }
        });

        TextView nextScreen = (TextView)findViewById(R.id.tvNext);
        nextScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to the final share screen");
                Intent intent = new Intent(GalleryPhotoEditorActivity.this, NextActivity.class);
                intent.putExtra(getString(R.string.selected_image),mSelectedImage);
                startActivity(intent);
                Log.d(TAG, "onClick: navigating share screen");
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
    }

    private void startGallery() {
        Intent cameraIntent = new Intent(Intent.ACTION_GET_CONTENT);
        cameraIntent.setType("image/*");
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
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
        uCrop.start(this);
        return cameraScalePath;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case UCrop.REQUEST_CROP:
                if (resultCode == Activity.RESULT_OK) {
                    final Uri croppedUri = UCrop.getOutput(data);
                    imageUri = croppedUri;
                    mPhotoEditorView.getSource().setImageURI(imageUri);
                    this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, croppedUri));
                } else if (resultCode == UCrop.RESULT_ERROR) {
                    final Throwable cropError = UCrop.getError(data);
                }
                break;
            default:
                break;
        }
    }


}
