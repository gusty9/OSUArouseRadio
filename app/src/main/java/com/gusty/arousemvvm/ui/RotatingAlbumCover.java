package com.gusty.arousemvvm.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.gusty.arousemvvm.R;

public class RotatingAlbumCover {
    private ImageView viewHolder;
    private ImageView pausePlayOverlay;
    private Bitmap albumArt;
    private Animator diskAnimator;
    private Context context;

    /**
     * Create the 'rotation image art' object
     * @param viewHolder -> the image view to set the circular rotation image to
     * @param albumArt -> the image of the album art
     * @param context -> application context
     */
    public RotatingAlbumCover(ImageView viewHolder, ImageView pausePlayOverlay, Bitmap albumArt, Context context) {
        this.context = context;
        this.viewHolder = viewHolder;
        this.pausePlayOverlay = pausePlayOverlay;
        this.albumArt = getCircleBitmap(albumArt);
        this.viewHolder.setScaleType(ImageView.ScaleType.FIT_XY);
        Glide.with(context).load(this.albumArt).into(this.viewHolder);
        diskAnimator = ObjectAnimator.ofFloat(this.viewHolder, "rotation", 0f, 360.0f);
        diskAnimator.setDuration(3000);
        //Set rotation speed to be linear
        diskAnimator.setInterpolator(new LinearInterpolator());
        ((ObjectAnimator) diskAnimator).setRepeatCount(-1);
        ((ObjectAnimator) diskAnimator).setRepeatMode(ValueAnimator.RESTART);
        pausePlayOverlay.setVisibility(View.INVISIBLE);

    }

    /**
     * Crop the square bitmap into circle
     * @param bitmap
     * @return
     */
    private Bitmap getCircleBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        bitmap = cropBitmap(bitmap);
        try {
            Bitmap circleBitmap = Bitmap.createBitmap(bitmap.getWidth(),
                    bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(circleBitmap);
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(),
                    bitmap.getHeight());
            final RectF rectF = new RectF(new Rect(0, 0, bitmap.getWidth(),
                    bitmap.getHeight()));
            float roundPx = 0.0f;
            roundPx = bitmap.getWidth();
            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(Color.WHITE);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            final Rect src = new Rect(0, 0, bitmap.getWidth(),
                    bitmap.getHeight());
            canvas.drawBitmap(bitmap, src, rect, paint);
            return circleBitmap;
        } catch (Exception e) {
            return bitmap;
        }
    }

    /**
     * Crop the bitmap
     * @param bitmap
     * @return
     */
    private Bitmap cropBitmap(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int cropWidth = w >= h ? h : w;

        return Bitmap.createBitmap(bitmap, (bitmap.getWidth() - cropWidth) / 2,
                (bitmap.getHeight() - cropWidth) / 2, cropWidth, cropWidth);
    }

    /**
     *
     * @return whether or not rotation has started
     */
    public boolean isStarted() {
        return diskAnimator.isStarted();
    }

    /**
     * start the animation of the circle
     */
    public void startAnimation(int color) {
        diskAnimator.start();
        pausePlayOverlay.setImageResource(R.drawable.ic_pause_animator);
        pausePlayOverlay.setColorFilter(color);
        Animatable animation = (Animatable) pausePlayOverlay.getDrawable();
        animation.start();
    }

    /**
     * resume the animation (must have be started already)
     */
    public void resumeAnimation(int color) {
        diskAnimator.resume();
        pausePlayOverlay.setVisibility(View.VISIBLE);
        pausePlayOverlay.setImageResource(R.drawable.ic_pause_animator);
        pausePlayOverlay.setColorFilter(color);
        Animatable animation = (Animatable) pausePlayOverlay.getDrawable();
        animation.start();
        pausePlayOverlay.startAnimation(AnimationUtils.loadAnimation(this.context,R.anim.fade_out));
        pausePlayOverlay.setVisibility(View.GONE);
    }

    /**
     * pause the animation
     */
    public void pauseAnimation(int color) {
        diskAnimator.pause();
        pausePlayOverlay.setVisibility(View.VISIBLE);
        pausePlayOverlay.setImageResource(R.drawable.ic_play_animator);
        pausePlayOverlay.setColorFilter(color);
        Animatable animation = (Animatable) pausePlayOverlay.getDrawable();
        animation.start();
    }

    public void endAnimation() {
        diskAnimator.end();
    }
}
