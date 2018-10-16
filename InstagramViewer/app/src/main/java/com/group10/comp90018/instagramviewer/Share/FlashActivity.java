package com.group10.comp90018.instagramviewer.Share;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.group10.comp90018.instagramviewer.R;

public class FlashActivity extends AppCompatActivity implements View.OnClickListener{

    private Camera camera;
    private Parameters parameters;
    private Button flashLightButton;
    boolean isFlashLightOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_photo);

        flashLightButton = (Button) findViewById(R.id.btnflash);
        flashLightButton.setOnClickListener(this);
//        flashLightButton.setOnClickListener(new FlashOnOffListener());
//
//        if (isFlashSupported()) {
//            camera = Camera.open();
//            parameters = camera.getParameters();
//        } else {
//            showNoFlashAlert();
//        }
    }

    private void getCamera() {
        if (camera == null) {
            try {
                camera = Camera.open();
                parameters = camera.getParameters();


            } catch (Exception e) {
                Toast.makeText(this,e.toString(),Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onClick(View view){
        if(view == flashLightButton){
            if(isFlashLightOn){
                trunOffFlash();
            }else {
                getCamera();
                trunOnFlash();
            }
        }

    }

    private void trunOnFlash(){
        try{
            parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
            camera.setParameters(parameters);
            camera.startPreview();
            isFlashLightOn = true;
            flashLightButton.setText("Off");

        }catch (Exception e){
            Toast.makeText(this,e.toString(),Toast.LENGTH_SHORT).show();
        }
    }

    private void trunOffFlash(){
        try{
            parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
            camera.setParameters(parameters);
            camera.stopPreview();
            isFlashLightOn = false;
            flashLightButton.setText("On");

        }catch (Exception e){
            Toast.makeText(this,e.toString(),Toast.LENGTH_SHORT).show();
        }

    }

//    private class FlashOnOffListener implements View.OnClickListener {
//
//        @Override
//        public void onClick(View v) {
//            if (isFlashLightOn) {
//                flashLightButton.setImageResource(R.drawable.flashlight_off);
//                parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
//                camera.setParameters(parameters);
//                camera.stopPreview();
//                isFlashLightOn = false;
//            } else {
//                flashLightButton.setImageResource(R.drawable.flashlight_on);
//                parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
//                camera.setParameters(parameters);
//                camera.startPreview();
//                isFlashLightOn = true;
//            }
//
//        }
//
//    }
//
//    private void showNoFlashAlert() {
//        new AlertDialog.Builder(this)
//                .setMessage("Your device hardware does not support flashlight!")
//                .setIcon(android.R.drawable.ic_dialog_alert).setTitle("Error")
//                .setPositiveButton("Ok", new OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                        finish();
//                    }
//                }).show();
//    }

//    private boolean isFlashSupported() {
//        PackageManager pm = getPackageManager();
//        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
//    }
//
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
            parameters = null;
        }
    }
}