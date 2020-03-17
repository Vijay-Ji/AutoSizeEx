package me.jessyan.autosize.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.Display;
import android.view.WindowManager;

/**
 * 应用组件级的工具，如获取AndroidManifest中的Application或Activity的metadata
 * @author jiwenjie
 */
public class AppUtils {
    private static final String KEY_AUTOSIZE_INTRINSIC = "autosize_intrinsic";
    private static Boolean sAppResizable;
    private static WindowParams sAppWindowParams;

    /**
     * 大屏应用适配参数(TNT...)
     * 参考：https://bytedance.feishu.cn/docs/doccnhSFwUzyrNDaT08bVDXTLxc?new_source=message#
     * "version,windowMode,resizeMode,forceResizeMode,width,height,minWidth,minHeight"
     * @param activity
     * @return new int[] {width, height}
     */
    public static WindowParams getActivityWindowParams(Activity activity) {
        WindowParams appWindowParams = getAppWindowParams(activity.getApplicationContext());
        Object obj = getActivityMetaData(activity, "windowParams");
        if (obj != null) {
            String windowParams = (String) obj;
            LogUtils.d("AppUtils-windowParams = " + windowParams + ", activity = "
                    + activity.getClass().getName());
            String[] params = windowParams.split(",");

            if (params.length > 0) {
                try {
                    int[] ret = new int[8];
                    for (int i = 0; i < params.length; i++) {
                        ret[i] = Integer.parseInt(params[i]);
                    }
                    return WindowParams.combineWindowParams(appWindowParams.getOriginParams(), ret);
                } catch (NumberFormatException ex) {
                    LogUtils.d("Exception in AppUtils - getActivityWindowParams()");
                }
            }
        }
        return appWindowParams;
    }

    /**
     * 大屏应用适配参数(TNT...)
     * 参考：https://bytedance.feishu.cn/docs/doccnhSFwUzyrNDaT08bVDXTLxc?new_source=message#
     * "version,windowMode,resizeMode,forceResizeMode,width,height,minWidth,minHeight"
     * @param appCtx
     * @return new int[] {width, height}
     */
    public static WindowParams getAppWindowParams(Context appCtx) {
        if (sAppWindowParams != null) {
            return sAppWindowParams;
        }

        Object obj = getAppMetaData(appCtx, "windowParams");
        if (obj != null) {
            String windowParams = (String) obj;
            LogUtils.d("AppUtils-windowParams=" + windowParams);
            String[] params = windowParams.split(",");

            if (params.length > 0) {
                try {
                    int[] tempParams = new int[8];
                    for (int i = 0; i < params.length; i++) {
                        tempParams[i] = Integer.parseInt(params[i]);
                    }

                    sAppWindowParams = WindowParams.createWindowParams(tempParams);
                    return sAppWindowParams;
                } catch (NumberFormatException ex) {
                    LogUtils.d("Exception in AppUtils - getAppWindowParams()");
                }
            }
        }
        return null;
    }

    public static Object getActivityMetaData(Activity activity, String key) {
        ActivityInfo info = null;
        try {
            info = activity.getPackageManager().getActivityInfo(activity.getComponentName(),
                    PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (info == null || info.metaData == null || !info.metaData.containsKey(key)) {
            return null;
        }
        return info.metaData.get(key);
    }

    public static Object getAppMetaData(Context appCtx, String key) {
        ApplicationInfo info = null;
        try {
            info = appCtx.getPackageManager().getApplicationInfo(appCtx.getPackageName(),
                    PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (info == null || info.metaData == null || !info.metaData.containsKey(key)) {
            return null;
        }
        return info.metaData.get(key);
    }

    /**
     * resizable == true, 说明此时处于大屏模式，获取大屏参数进行适配
     * @param activity
     * @return
     */
    public static boolean keepIntrinsic(Activity activity) {
        if (sAppResizable == null) {
            Object obj = getAppMetaData(activity, KEY_AUTOSIZE_INTRINSIC);
            sAppResizable = obj != null && (boolean) obj;
        }
        Object resizable = getActivityMetaData(activity, KEY_AUTOSIZE_INTRINSIC);
        if (resizable != null) {
            return resizable != null ? (boolean) resizable : sAppResizable;
        }
        return sAppResizable;
    }

    /**
     * 判断是不是手机+扩展屏模式
     * @param context
     * @return
     */
    public static boolean isExtDevice(Context context) {
        return getExtDisplayId(context) > 0;
    }

    /**
     * 通过自己的context获取display id来判断是否运行在大屏端
     * @param context
     * @return
     */
    public static int getExtDisplayId(Context context) {
        WindowManager w = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = w.getDefaultDisplay();
        // on pc display for phone + extend display usage only
        return display.getDisplayId();
    }
}
