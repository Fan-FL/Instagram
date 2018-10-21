package com.group10.myinstagram.Utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

public class LikeAnimation {

    private static final String TAG = "Heart";

    private static final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new
            DecelerateInterpolator();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new
            AccelerateInterpolator();

    public ImageView likeOutline, likeHeart;

    public LikeAnimation(ImageView likeOutline, ImageView likeHeart) {
        this.likeOutline = likeOutline;
        this.likeHeart = likeHeart;
    }

    public void toggleLike() {
        Log.d(TAG, "toggleLike: toggling heart.");

        AnimatorSet animationSet = new AnimatorSet();


        if (likeHeart.getVisibility() == View.VISIBLE) {
            Log.d(TAG, "toggleLike: toggling red heart off.");
            likeHeart.setScaleX(0.1f);
            likeHeart.setScaleY(0.1f);

            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(likeHeart, "scaleY", 1f, 0f);
            scaleDownY.setDuration(300);
            scaleDownY.setInterpolator(ACCELERATE_INTERPOLATOR);

            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(likeHeart, "scaleX", 1f, 0f);
            scaleDownX.setDuration(300);
            scaleDownX.setInterpolator(ACCELERATE_INTERPOLATOR);

            likeHeart.setVisibility(View.GONE);
            likeOutline.setVisibility(View.VISIBLE);

            animationSet.playTogether(scaleDownY, scaleDownX);
        } else if (likeHeart.getVisibility() == View.GONE) {
            Log.d(TAG, "toggleLike: toggling red heart on.");
            likeHeart.setScaleX(0.1f);
            likeHeart.setScaleY(0.1f);

            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(likeHeart, "scaleY", 0.1f, 1f);
            scaleDownY.setDuration(300);
            scaleDownY.setInterpolator(DECCELERATE_INTERPOLATOR);

            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(likeHeart, "scaleX", 0.1f, 1f);
            scaleDownX.setDuration(300);
            scaleDownX.setInterpolator(DECCELERATE_INTERPOLATOR);

            likeHeart.setVisibility(View.VISIBLE);
            likeOutline.setVisibility(View.GONE);

            animationSet.playTogether(scaleDownY, scaleDownX);
        }

        animationSet.start();

    }
}
