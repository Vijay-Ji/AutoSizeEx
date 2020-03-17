/*
 * Copyright 2018 JessYan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.jessyan.autosize.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

/**
 * ================================================
 * Created by JessYan on 26/09/2016 16:59
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class ScreenUtils {

    private ScreenUtils() {
        throw new IllegalStateException("you can't instantiate me!");
    }

    public static int getStatusBarHeight() {
        int result = 0;
        try {
            int resourceId = Resources.getSystem().getIdentifier("status_bar_height", "dimen",
                    "android");
            if (resourceId > 0) {
                result = Resources.getSystem().getDimensionPixelSize(resourceId);
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static int getHeightOfNavigationBar(Context context) {
        // 如果小米手机开启了全面屏手势隐藏了导航栏则返回 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (Settings.Global.getInt(context.getContentResolver(), "force_fsg_nav_bar", 0) != 0) {
                return 0;
            }
        }

        int realHeight = getRealScreenSize(context)[1];
        int displayHeight = getScreenSize(context)[1];
        return realHeight - displayHeight;
    }

    /**
     * 获取原始的屏幕尺寸, includes window decorations (statusbar bar/menu bar)，默认使用该方法获取宽高
     * @param context {@link Context}
     * @return 屏幕尺寸
     */
    public static int[] getRealScreenSize(Context context) {
        WindowManager w = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display d = w.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        // since SDK_INT = 1;
        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels;

        // includes window decorations (statusbar bar/menu bar)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                widthPixels = (Integer) Display.class.getMethod("getRawWidth").invoke(d);
                heightPixels = (Integer) Display.class.getMethod("getRawHeight").invoke(d);
            } catch (Exception ignored) {
            }
        }
        // includes window decorations (statusbar bar/menu bar)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Point realSize = new Point();
            d.getRealSize(realSize);
            widthPixels = realSize.x;
            heightPixels = realSize.y;
        }
        return new int[] { widthPixels, heightPixels };
    }

    /**
     * 获取当前的屏幕尺寸
     * @param context {@link Context}
     * @return 屏幕尺寸
     */
    public static int[] getScreenSize(Context context) {
        DisplayMetrics metrics = getDisplayMetrics(context);
        return new int[] { metrics.widthPixels, metrics.heightPixels };
    }

    /**
     * 获取当前屏幕尺寸，不是屏幕的RealSize，获取实际尺寸使用{@link #getRealScreenSize(Context)}
     * excludes window decorations (statusbar bar/menu bar)
     * @param context
     * @return
     */
    public static DisplayMetrics getDisplayMetrics(Context context) {
        WindowManager w = (WindowManager) context.getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        Display d = w.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);
        return metrics;
    }

    /**
     * 获取当前屏幕DisplayMetrics，不是RealDisplayMetrics，获取实际尺寸使用{@link #getRealScreenSize(Context)}
     * excludes window decorations (statusbar bar/menu bar)
     * @param context
     * @return
     */
    @SuppressLint("NewApi")
    public static DisplayMetrics getRealDisplayMetrics(Context context) {
        WindowManager w = (WindowManager) context.getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        Display d = w.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getRealMetrics(metrics);
        return metrics;
    }

}
