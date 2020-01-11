package com.gusty.arousemvvm.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.widget.ImageView;
import android.widget.TextView;

public class DynamicThemeFromAlbum {
    private static final float BITMAP_SCALE = 0.4f;
    private static final float BLUR_RADIUS = 25f;

    private Bitmap blurredBitmap;
    private int red;
    private int green;
    private int blue;
    private int textColor;
    private int textOutline;

    public DynamicThemeFromAlbum(Bitmap originalBitmap, Context context) {
        blurredBitmap = blur(context, originalBitmap);
        getAverageColor(originalBitmap);
        getTextColorFromBackground();
    }

    public Bitmap getBlurredBitmap() {
        return blurredBitmap;
    }

    private void getAverageColor(Bitmap album) {
        long redBucket = 0;
        long greenBucket = 0;
        long blueBucket = 0;
        long pixelCount = 0;
        for (int y = 0; y < album.getHeight(); y++) {
            for (int x = 0; x < album.getWidth(); x++) {
                int c = album.getPixel(x, y);
                pixelCount++;
                redBucket += Color.red(c);
                greenBucket += Color.green(c);
                blueBucket += Color.blue(c);
            }
        }
        red = (int) (redBucket / pixelCount);
        green = (int) (greenBucket / pixelCount);
        blue = (int) (blueBucket / pixelCount);
    }

    public int getStatusBarColorFromBackground() {
        return Color.rgb(red, green, blue);
    }

    private void getTextColorFromBackground() {

        if ((red * 0.299) + (green * 0.587) + (blue * 0.114) > 186) {
            textColor = Color.BLACK;
            textOutline = Color.WHITE;
        } else {
            textColor = Color.WHITE;
            textOutline = Color.BLACK;
        }
    }

    private Bitmap blur(Context context, Bitmap image) {
        int width = Math.round(image.getWidth() * BITMAP_SCALE);
        int height = Math.round(image.getHeight() * BITMAP_SCALE);

        Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

        RenderScript rs = RenderScript.create(context);

        ScriptIntrinsicBlur intrinsicBlur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);

        intrinsicBlur.setRadius(BLUR_RADIUS);
        intrinsicBlur.setInput(tmpIn);
        intrinsicBlur.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);

        return outputBitmap;
    }

    public void setTextviewStyles(TextView... views) {
        for(TextView view : views) {
            int comp = getComplementaryColor();
            view.setTextColor(textColor);
            view.setShadowLayer(1.6f, 0, 0,   comp);
        }
    }

    public void setIconColrs(ImageView... views) {
        for (ImageView view : views) {
            view.setColorFilter(textColor);
        }
    }

    /**
     *
     * @return the complementary of the average color
     */
    public int getComplementaryColor() {
        int maxPlusMin = max(red, green, blue) + min(red, green, blue);
        int rPrime = maxPlusMin - red;
        int bPrime = maxPlusMin - blue;
        int gPrime = maxPlusMin - green;
        return Color.rgb(rPrime, gPrime, bPrime);
    }

    public int getTriadicColor() {
        return Color.rgb(green, blue, red);
    }

    /**
     * the max of three ints
     * @param r
     * @param b
     * @param g
     * @return
     */
    private int max(int r, int b, int g) {
        return Math.max(Math.max(r, g), b);
    }

    /**
     * the min of three ints
     * @param r
     * @param b
     * @param g
     * @return
     */
    private int min(int r, int b, int g) {
        return Math.min(Math.min(r, g), b);
    }
}
