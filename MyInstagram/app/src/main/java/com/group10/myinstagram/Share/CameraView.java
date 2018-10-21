package com.group10.myinstagram.Share;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.fragment.app.Fragment;

public class CameraView extends SurfaceView {
    private final Paint paint;
    private final SurfaceHolder mHolder;
    private final Context context;

    public CameraView(Fragment context) {
        super(context.getActivity().getBaseContext());
        mHolder = getHolder();
        mHolder.setFormat(PixelFormat.TRANSPARENT);
        this.context = context.getActivity().getBaseContext();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //  Find Screen size first
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int screenHeight = (int) (metrics.heightPixels * 0.9);

        //  Set paint options
        paint.setAntiAlias(true);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.argb(255, 255, 255, 255));

        canvas.drawLine((screenWidth / 3) * 2, 0, (screenWidth / 3) * 2, screenHeight, paint);
        canvas.drawLine((screenWidth / 3), 0, (screenWidth / 3), screenHeight, paint);
        canvas.drawLine(0, (screenHeight / 3) * 2, screenWidth, (screenHeight / 3) * 2, paint);
        canvas.drawLine(0, (screenHeight / 3), screenWidth, (screenHeight / 3), paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }
}
