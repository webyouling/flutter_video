package com.test.player;

import android.content.Context;
import android.provider.Settings;

/**
 * 重力感应器开关
 * 围绕手机屏幕旋转的设置功能编写的方法
 *
 * @author Wilson
 */
public class SensorUtil {
    /**
     * 打开重力感应，即设置屏幕可旋转
     *
     * @param context
     */
    public static void openSensor(Context context) {
        Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1);
    }

    /**
     * 关闭重力感应，即设置屏幕不可旋转
     *
     * @param context
     */
    public static void closeSensor(Context context) {
        Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
    }

    /**
     * 获取屏幕旋转功能开启状态
     *
     * @param context
     * @return
     */
    public static int getSensorState(Context context) {
        int sensorState = 0;
        try {
            sensorState = Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION);
            return sensorState;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return sensorState;
    }

    /**
     * 判断屏幕旋转功能是否开启
     */
    public static boolean isOpenSensor(Context context) {
        boolean isOpen = false;
        if (getSensorState(context) == 1) {
            isOpen = true;
        } else if (getSensorState(context) == 0) {
            isOpen = false;
        }
        return isOpen;
    }
}  