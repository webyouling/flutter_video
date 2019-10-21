package com.test.player;

import android.content.Context;
import android.view.OrientationEventListener;


public abstract class SimpleOrientationEventListener extends OrientationEventListener {
    /**
     * 竖屏
     */
    public static final int ORIENTATION_PORTRAIT = 0;
    /**
     * 横屏
     */
    public static final int ORIENTATION_LANDSCAPE = 1;
    /**
     * 竖屏反向
     */
    public static final int ORIENTATION_PORTRAIT_REVERSE = 2;
    /**
     * 横屏反向
     */
    public static final int ORIENTATION_LANDSCAPE_REVERSE = 3;

    public int lastOrientation = 0;

    public SimpleOrientationEventListener(Context context) {
        super(context);
    }

    @Override
    public final void onOrientationChanged(int orientation) {
        if (orientation < 0) {
            return;
        }
//        Loger.e("orientation=" + orientation);
        int curOrientation;
        if (orientation <= 45) {
            curOrientation = ORIENTATION_PORTRAIT;
        } else if (orientation <= 135) {
            curOrientation = ORIENTATION_LANDSCAPE_REVERSE;
        } else if (orientation <= 225) {
            curOrientation = ORIENTATION_PORTRAIT_REVERSE;
        } else if (orientation <= 315) {
            curOrientation = ORIENTATION_LANDSCAPE;
        } else {
            curOrientation = ORIENTATION_PORTRAIT;
        }
//        Loger.e("lastOrientation=" + lastOrientation + "  curOrientation=" + curOrientation);
        if (curOrientation == 2)
            return;
        if (curOrientation != lastOrientation) {
            onChanged(lastOrientation, curOrientation);
            lastOrientation = curOrientation;
        }
    }

    public abstract void onChanged(int lastOrientation, int orientation);
}