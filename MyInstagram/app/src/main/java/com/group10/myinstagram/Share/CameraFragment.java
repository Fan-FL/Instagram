package com.group10.myinstagram.Share;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.group10.myinstagram.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

public class CameraFragment extends Fragment {
    private static final SparseIntArray ORIENTATION = new SparseIntArray();
    /**
     * Max preview width that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_WIDTH = 1920;
    /**
     * Max preview height that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    static {
        ORIENTATION.append(Surface.ROTATION_0, 90);
        ORIENTATION.append(Surface.ROTATION_90, 0);
        ORIENTATION.append(Surface.ROTATION_180, 270);
        ORIENTATION.append(Surface.ROTATION_270, 180);
    }

    SurfaceView surfaceView;
    private String mCameraId;
    private Size mPreviewSize;
    private HandlerThread mCameraThread;
    private Handler mCameraHandler;
    private CameraDevice mCameraDevice;
    private ImageReader mImageReader;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private CaptureRequest mCaptureRequest;
    private CameraCaptureSession mCameraCaptureSession;
    private Button photoButton;
    private Button flashModeButton;
    private TextView flashModeTextView;
    private int flashMode = 2;
    private Paint paint = new Paint();
    private AutoFitTextureView mTextureView;
    /**
     * Orientation of the camera sensor
     */
    private int mSensorOrientation;
    /**
     * Whether the current camera device supports Flash or not.
     */
    private boolean mFlashSupported;
    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;
            startPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close();
            mCameraDevice = null;
        }
    };
    private TextureView.SurfaceTextureListener mTextureListener = new TextureView
            .SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //set parameters and open camera when  urefaceTexture is ready
            setupCamera(width, height);
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };
    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession
            .CaptureCallback() {
        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request,
                                        CaptureResult partialResult) {
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request,
                                       TotalCaptureResult result) {
            capture();
        }
    };

    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth, int
            textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight && option
                    .getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth && option.getHeight() >=
                        textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.e("CameraFragment", "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            saveInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_camera, container, false);

        mTextureView = (AutoFitTextureView) view.findViewById(R.id.textureView);
        photoButton = (Button) view.findViewById(R.id.photoButton);
        flashModeButton = (Button) view.findViewById(R.id.flashModeButton);
        flashModeTextView = (TextView) view.findViewById(R.id.flashModeTextView);

        surfaceView = (SurfaceView) view.findViewById(R.id.surfaceView);
        surfaceView.setZOrderOnTop(true);
        SurfaceHolder mHolder = surfaceView.getHolder();
        mHolder.setFormat(PixelFormat.TRANSPARENT);
        mHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Canvas canvas = holder.lockCanvas();
                if (canvas == null) {
                    Log.e("CameraFragment", "Cannot draw onto the canvas as it's null");
                } else {
                    int screenWidth = mTextureView.getWidth() / 2;
                    int screenHeight = mTextureView.getHeight() / 2;

                    //  Set paint options
                    paint.setAntiAlias(true);
                    paint.setStrokeWidth(3);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setColor(Color.WHITE);

                    int rectLen = 85;
                    int rectSpace = 25;
                    canvas.drawLine(screenWidth - rectLen, screenHeight - rectLen, screenWidth -
                            rectSpace, screenHeight - rectLen, paint);
                    canvas.drawLine(screenWidth - rectLen, screenHeight - rectLen, screenWidth -
                            rectLen, screenHeight - rectSpace, paint);
                    canvas.drawLine(screenWidth - rectLen, screenHeight + rectLen, screenWidth -
                            rectLen, screenHeight + rectSpace, paint);
                    canvas.drawLine(screenWidth - rectLen, screenHeight + rectLen, screenWidth -
                            rectSpace, screenHeight + rectLen, paint);
                    canvas.drawLine(screenWidth + rectLen, screenHeight - rectLen, screenWidth +
                            rectSpace, screenHeight - rectLen, paint);
                    canvas.drawLine(screenWidth + rectLen, screenHeight - rectLen, screenWidth +
                            rectLen, screenHeight - rectSpace, paint);
                    canvas.drawLine(screenWidth + rectLen, screenHeight + rectLen, screenWidth +
                            rectLen, screenHeight + rectSpace, paint);
                    canvas.drawLine(screenWidth + rectLen, screenHeight + rectLen, screenWidth +
                            rectSpace, screenHeight + rectLen, paint);

                    holder.unlockCanvasAndPost(canvas);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoButton.setEnabled(false);
                lockFocus();
            }
        });

        flashModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flashMode = (flashMode + 1) % 3;
                switch (flashMode) {
                    case 0:
                        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata
                                .CONTROL_AE_MODE_ON_ALWAYS_FLASH);
                        flashModeTextView.setText("Always");
                        break;
                    case 1:
                        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest
                                .CONTROL_AE_MODE_ON);
                        mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest
                                .FLASH_MODE_OFF);
                        flashModeTextView.setText("Off");
                        break;
                    case 2:
                        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata
                                .CONTROL_AE_MODE_ON_AUTO_FLASH);
                        flashModeTextView.setText("Auto");
                        break;

                }

            }
        });
        return view;
    }

    private void drawContent(Canvas canvas) {
        //画内容
        canvas.drawColor(Color.WHITE);
        paint.setColor(Color.RED);
        canvas.drawCircle(200, 300, 100, paint);
    }

    @Override
    public void onResume() {
        super.onResume();
        startCameraThread();
        if (!mTextureView.isAvailable()) {
            mTextureView.setSurfaceTextureListener(mTextureListener);
        } else {
            startPreview();
        }
    }

    private void startCameraThread() {
        mCameraThread = new HandlerThread("CameraThread");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());
    }

    private void setupCamera(int width, int height) {
        Activity activity = getActivity();
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

                // We don't use a front facing camera in this sample.
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }

                StreamConfigurationMap map = characteristics.get(CameraCharacteristics
                        .SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }

                // For still image captures, we use the largest available size.
                Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)
                ), new CompareSizesByArea());

                // Find out if we need to swap dimension to get the preview size relative to sensor
                // coordinate.
                int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
                //noinspection ConstantConditions
                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                boolean swappedDimensions = false;
                switch (displayRotation) {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                            swappedDimensions = true;
                        }
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                            swappedDimensions = true;
                        }
                        break;
                    default:
                        Log.e("CameraFragment", "Display rotation is invalid: " + displayRotation);
                }

                Point displaySize = new Point();
                activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
                int rotatedPreviewWidth = width;
                int rotatedPreviewHeight = height;
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;

                if (swappedDimensions) {
                    rotatedPreviewWidth = height;
                    rotatedPreviewHeight = width;
                    maxPreviewWidth = displaySize.y;
                    maxPreviewHeight = displaySize.x;
                }

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH;
                }

                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;
                }

                // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
                // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
                // garbage capture data.
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                        maxPreviewHeight, largest);

                // We fit the aspect ratio of TextureView to the size of preview we picked.
                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else {
                    mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }

                // Check if the flash is supported.
                Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                mFlashSupported = available == null ? false : available;

                setupImageReader();
                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            ErrorDialog.newInstance(getString(R.string.camera_error)).show
                    (getChildFragmentManager(), "dialog");
        }

    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getContext().getSystemService(Context
                .CAMERA_SERVICE);
        try {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) !=
                    PackageManager.PERMISSION_GRANTED) {
                return;
            }
            manager.openCamera(mCameraId, mStateCallback, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startPreview() {
        SurfaceTexture mSurfaceTexture = mTextureView.getSurfaceTexture();
        mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface previewSurface = new Surface(mSurfaceTexture);
        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice
                    .TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata
                    .CONTROL_AE_MODE_ON_AUTO_FLASH);
            mCaptureRequestBuilder.addTarget(previewSurface);
            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, mImageReader
                    .getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        mCaptureRequest = mCaptureRequestBuilder.build();
                        mCameraCaptureSession = session;
                        mCameraCaptureSession.setRepeatingRequest(mCaptureRequest, null,
                                mCameraHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {

                }
            }, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void lockFocus() {
        try {
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata
                    .CONTROL_AF_TRIGGER_START);
            mCameraCaptureSession.capture(mCaptureRequestBuilder.build(), mCaptureCallback,
                    mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void capture() {
        try {
            final CaptureRequest.Builder mCaptureBuilder = mCameraDevice.createCaptureRequest
                    (CameraDevice.TEMPLATE_STILL_CAPTURE);
            int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
            mCaptureBuilder.addTarget(mImageReader.getSurface());
            mCaptureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATION.get(rotation));
            CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession
                    .CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest
                        request, TotalCaptureResult result) {
                    Toast.makeText(getActivity().getApplicationContext(), "Image Saved!", Toast
                            .LENGTH_SHORT).show();
                    unLockFocus();
                    photoButton.setEnabled(true);
                }
            };
            mCameraCaptureSession.stopRepeating();
            mCameraCaptureSession.capture(mCaptureBuilder.build(), CaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void unLockFocus() {
        try {
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata
                    .CONTROL_AF_TRIGGER_CANCEL);
            mCameraCaptureSession.setRepeatingRequest(mCaptureRequest, null, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCameraCaptureSession != null) {
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }

        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }

        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
    }

    private void setupImageReader() {
        //2 represents ImageReader can get 2 image frame at maximum
        mImageReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(),
                ImageFormat.JPEG, 2);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                mCameraHandler.post(new imageSaver(reader.acquireNextImage()));
            }
        }, mCameraHandler);
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() *
                    rhs.getHeight());
        }

    }

    /**
     * Shows an error message dialog.
     */
    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity).setMessage(getArguments().getString
                    (ARG_MESSAGE)).setPositiveButton(android.R.string.ok, new DialogInterface
                    .OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    activity.finish();
                }
            }).create();
        }

    }

    public class imageSaver implements Runnable {

        private Image mImage;

        public imageSaver(Image image) {
            mImage = image;
        }

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            String path = Environment.getExternalStorageDirectory() + "/DCIM/Camera/";
            File mImageFile = new File(path);
            if (!mImageFile.exists()) {
                mImageFile.mkdir();
            }
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = path + "IMG_" + timeStamp + ".jpg";
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(fileName);
                fos.write(data, 0, data.length);
                File file = new File(fileName);
                Uri uri = Uri.fromFile(file);
                CameraFragment.this.getActivity().sendBroadcast(new Intent(Intent
                        .ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                Intent intent = new Intent(CameraFragment.this.getActivity(),
                        GalleryPhotoEditorActivity.class);
                intent.putExtra(getString(R.string.selected_image), fileName);
                startActivity(intent);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private class CustomView extends ImageView {

        private final Paint paint;

        public CustomView(CameraFragment context) {
            super(context.getActivity().getBaseContext());
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            canvas.drawColor(Color.TRANSPARENT);
            canvas.drawCircle(mTextureView.getX() + mTextureView.getWidth() / 2, mTextureView
                    .getY() + mTextureView.getHeight() / 2, 100, paint);
        }
    }
}
