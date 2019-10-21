package com.test.player;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;

/**
 * 播放器的背景色
 */
public class MyDrawable extends Drawable {
    private final Paint mRedPaint;
    private int bottomMargin;

    public MyDrawable(Context context) {
        bottomMargin = DensityUtil.dip2px(context, 8);
        mRedPaint = new Paint();
        mRedPaint.setARGB(255, 0, 0, 0);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        int width = getBounds().width();
        int height = getBounds().height();
        Rect rect = new Rect(0, 0, width, height - bottomMargin);
        canvas.drawRect(rect, mRedPaint);
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}