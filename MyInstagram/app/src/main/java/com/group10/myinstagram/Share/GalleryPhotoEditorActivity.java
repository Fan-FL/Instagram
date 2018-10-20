package com.group10.myinstagram.Share;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.media.effect.EffectFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.group10.myinstagram.R;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import ja.burhanrashid52.photoeditor.CustomEffect;
import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.PhotoEditorView;
import ja.burhanrashid52.photoeditor.PhotoFilter;

public class GalleryPhotoEditorActivity extends AppCompatActivity {
    private static final String TAG = "GalleryPhotoEditor";
    //constants

    //widgets
    private Button cropButton;
    private Button blackWhiteButton;
    private Button sharpenButton;
    private Button documentaryButton;
    private Button bright_contrastButton;
    private PhotoEditorView mPhotoEditorView;
    private SeekBar contrastseekbar;
    private SeekBar brightnessseekbar;
    private LinearLayout seekbars;
    private PhotoEditor mPhotoEditor;

    //vars
    private String imagePath;
    private Uri imageUri;
    private String saveFilePath;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: GalleryPhotoEditorActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_editor);
        mPhotoEditorView = findViewById(R.id.photoEditorView);
        cropButton = (Button) findViewById(R.id.cropButton);
        blackWhiteButton = (Button) findViewById(R.id.blackWhiteButton);
        sharpenButton = (Button) findViewById(R.id.sharpenButton);
        documentaryButton = (Button) findViewById(R.id.documentaryButton);
        bright_contrastButton = (Button) findViewById(R.id.bright_contrastButton);
        brightnessseekbar = (SeekBar) findViewById(R.id.brightnessseekbar);
        contrastseekbar = (SeekBar) findViewById(R.id.contrastseekbar);
        seekbars = (LinearLayout) findViewById(R.id.seekbars);
        Log.d(TAG, "onCreateView: started");

        Intent intent = getIntent();
        imagePath = intent.getStringExtra(getString(R.string.selected_image));
        if (!imagePath.isEmpty()) {
            File picPath = new File(imagePath);
            imageUri = Uri.fromFile(picPath);
            mPhotoEditorView.getSource().setImageURI(imageUri);
            mPhotoEditor = new PhotoEditor.Builder(this, mPhotoEditorView).build();

        }

        File outDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }
        File outFile = new File(outDir, System.currentTimeMillis() + ".jpg");
        //裁剪后图片的绝对路径
        Log.e("111", outFile.getAbsolutePath());
        saveFilePath = outFile.getAbsolutePath();

        ImageView shareClose = (ImageView) findViewById(R.id.ivCloseShare);
        shareClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing the GalleryPhotoEditorActivity");
                finish();
            }
        });

        TextView nextScreen = (TextView) findViewById(R.id.tvNext);
        nextScreen.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                mPhotoEditor.saveAsFile(saveFilePath, new PhotoEditor.OnSaveListener() {
                    @Override
                    public void onSuccess(@NonNull String imagePath) {
                        Log.e("PhotoEditor", "Image Saved Successfully");
                        File picPath = new File(imagePath);
                        imageUri = Uri.fromFile(picPath);
                        mPhotoEditorView.getSource().setImageURI(imageUri);
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, imageUri));
                        Log.d(TAG, "onClick: navigating to the final share screen");
                        Intent intent = new Intent(GalleryPhotoEditorActivity.this, NextActivity.class);
                        intent.putExtra(getString(R.string.selected_image), imagePath);
                        startActivity(intent);
                        Log.d(TAG, "onClick: navigating share screen");
                    }

                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e("PhotoEditor", "Failed to save Image");
                    }
                });
            }
        });
        cropButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            public void onClick(View view) {
                seekbars.setVisibility(View.GONE);
                mPhotoEditor.saveAsFile(saveFilePath, new PhotoEditor.OnSaveListener() {
                    @Override
                    public void onSuccess(@NonNull String imagePath) {
                        File picPath = new File(imagePath);
                        imageUri = Uri.fromFile(picPath);
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, imageUri));
                        mPhotoEditorView.getSource().setImageURI(imageUri);
                        startUCrop();
                        Log.e("PhotoEditor", "Image Saved Successfully");
                    }

                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e("PhotoEditor", "Failed to save Image");
                        startUCrop();
                    }
                });
            }
        });
        blackWhiteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPhotoEditor != null){
                    seekbars.setVisibility(View.GONE);
                    mPhotoEditor.setFilterEffect(PhotoFilter.BLACK_WHITE);
                }
            }
        });
        sharpenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPhotoEditor != null){
                    seekbars.setVisibility(View.GONE);
                    mPhotoEditor.setFilterEffect(PhotoFilter.SHARPEN);
                }
            }
        });
        documentaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPhotoEditor != null){
                    seekbars.setVisibility(View.GONE);
                    mPhotoEditor.setFilterEffect(PhotoFilter.DOCUMENTARY);
                }
            }
        });
        bright_contrastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPhotoEditor != null){
                    seekbars.setVisibility(View.VISIBLE);
                }
            }
        });
        brightnessseekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // 当拖动条的滑块位置发生改变时触发该方法
            public void onProgressChanged(SeekBar arg0, int progress,
                                          boolean fromUser) {
                float brightness = progress/40.0f;
                CustomEffect customEffect = new CustomEffect.Builder(EffectFactory.EFFECT_BRIGHTNESS)
                        .setParameter("brightness", brightness)
                        .build();
                mPhotoEditor.setFilterEffect(customEffect);

            }

            public void onStartTrackingTouch(SeekBar bar) {
            }

            public void onStopTrackingTouch(SeekBar bar) {
            }
        });

        contrastseekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // 当拖动条的滑块位置发生改变时触发该方法
            public void onProgressChanged(SeekBar arg0, int progress,
                                          boolean fromUser) {
                float contrast = progress/50.0f;
                CustomEffect customEffect = new CustomEffect.Builder(EffectFactory.EFFECT_CONTRAST)
                        .setParameter("contrast", contrast)
                        .build();
                mPhotoEditor.setFilterEffect(customEffect);

            }

            public void onStartTrackingTouch(SeekBar bar) {
            }

            public void onStopTrackingTouch(SeekBar bar) {
            }
        });
    }

    private void startUCrop(){
        File outFile = new File(saveFilePath);
        //裁剪后图片的绝对路径
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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case UCrop.REQUEST_CROP:
                if (resultCode == Activity.RESULT_OK) {
                    final Uri croppedUri = UCrop.getOutput(data);
                    imageUri = croppedUri;
                    mPhotoEditorView.getSource().setImageURI(Uri.EMPTY);
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
