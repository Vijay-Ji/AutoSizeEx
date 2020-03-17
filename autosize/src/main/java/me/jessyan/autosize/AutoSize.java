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
package me.jessyan.autosize;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.View;

import me.jessyan.autosize.external.ExternalAdaptInfo;
import me.jessyan.autosize.external.ExternalAdaptManager;
import me.jessyan.autosize.internal.CancelAdapt;
import me.jessyan.autosize.internal.CustomAdapt;
import me.jessyan.autosize.unit.UnitsManager;
import me.jessyan.autosize.utils.AppUtils;
import me.jessyan.autosize.utils.LogUtils;
import me.jessyan.autosize.utils.Preconditions;
import me.jessyan.autosize.utils.ScreenUtils;
import me.jessyan.autosize.utils.WindowParams;

/**
 * ================================================
 * AndroidAutoSize 用于屏幕适配的核心方法都在这里, 核心原理来自于
 * <a href="https://mp.weixin.qq.com/s/d9QCoBP6kV9VSWvVldVVwA">今日头条官方适配方案</a>
 * 此方案只要应用到 {@link Activity} 上, 这个 {@link Activity} 下的所有
 * {@link android.support.v4.app.Fragment}、{@link Dialog}、
 * 自定义 {@link View} 都会达到适配的效果, 如果某个页面不想使用适配请让该 {@link Activity} 实现 {@link CancelAdapt}
 * <p>
 * 任何方案都不可能完美, 在成本和收益中做出取舍, 选择出最适合自己的方案即可, 在没有更好的方案出来之前, 只有继续忍耐它的不完美, 或者自己作出改变
 * 既然选择, 就不要抱怨, 感谢 今日头条技术团队 和 张鸿洋 等人对 Android 屏幕适配领域的的贡献
 * <p>
 * Created by JessYan on 2018/8/8 19:20
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public final class AutoSize {
    private static Map<String, DisplayMetricsInfo> mCache = new ConcurrentHashMap<>();

    private AutoSize() {
        throw new IllegalStateException("you can't instantiate me!");
    }

    /**
     * 使用 AndroidAutoSize 初始化时设置的默认适配参数进行适配 (AndroidManifest 的 Meta 属性)
     * @param activity {@link Activity}
     */
    public static void autoConvertDensityOfGlobal(Activity activity) {
        AutoSizeConfig config = AutoSizeConfig.getInstance();
        if (config.isBaseOnWidth()) {
            autoConvertDensityBaseOnWidth(activity, config.getDesignWidthInDp());
        } else {
            autoConvertDensityBaseOnHeight(activity, config.getDesignHeightInDp());
        }
    }

    /**
     * 使用 {@link Activity} 或 {@link android.support.v4.app.Fragment} 的自定义参数进行适配
     * @param activity {@link Activity}
     * @param customAdapt {@link Activity} 或 {@link android.support.v4.app.Fragment} 需实现
     *            {@link CustomAdapt}
     */
    public static void autoConvertDensityOfCustomAdapt(Activity activity, CustomAdapt customAdapt) {
        Preconditions.checkNotNull(customAdapt, "customAdapt == null");
        float sizeInDp = customAdapt.getSizeInDp();

        // 如果 CustomAdapt#getSizeInDp() 返回 0, 则使用在 AndroidManifest 上填写的设计图尺寸
        if (sizeInDp <= 0) {
            if (customAdapt.isBaseOnWidth()) {
                sizeInDp = AutoSizeConfig.getInstance().getDesignWidthInDp();
            } else {
                sizeInDp = AutoSizeConfig.getInstance().getDesignHeightInDp();
            }
        }
        autoConvertDensity(activity, sizeInDp, customAdapt.isBaseOnWidth());
    }

    /**
     * 这里是今日头条适配方案的核心代码, 核心在于根据当前设备的实际情况做自动计算并转换 {@link DisplayMetrics#density}、
     * {@link DisplayMetrics#scaledDensity}、{@link DisplayMetrics#densityDpi} 这三个值, 额外增加
     * {@link DisplayMetrics#xdpi}
     * 以支持单位 {@code pt}、{@code in}、{@code mm}
     * @param activity {@link Activity}
     * @param sizeInDp 设计图上的设计尺寸, 单位 dp, 如果 {@param isBaseOnWidth} 设置为 {@code true},
     *            {@param sizeInDp} 则应该填写设计图的总宽度, 如果 {@param isBaseOnWidth} 设置为 {@code false},
     *            {@param sizeInDp} 则应该填写设计图的总高度
     * @param isBaseOnWidth 是否按照宽度进行等比例适配, {@code true} 为以宽度进行等比例适配, {@code false} 为以高度进行等比例适配
     * @see <a href="https://mp.weixin.qq.com/s/d9QCoBP6kV9VSWvVldVVwA">今日头条官方适配方案</a>
     */
    public static void autoConvertDensity(Activity activity, float sizeInDp,
            boolean isBaseOnWidth) {
        Preconditions.checkNotNull(activity, "activity == null");

        AutoSizeConfig config = AutoSizeConfig.getInstance();
        float subunitsDesignSize = isBaseOnWidth ? config.getUnitsManager().getDesignWidth()
                : config.getUnitsManager().getDesignHeight();
        subunitsDesignSize = subunitsDesignSize > 0 ? subunitsDesignSize : sizeInDp;

        int baseSize = isBaseOnWidth ? config.getScreenWidth() : config.getScreenHeight();
        if (AppUtils.isExtDevice(activity) && AppUtils.keepIntrinsic(activity)) {
            DisplayMetrics metrics = ScreenUtils.getDisplayMetrics(activity);
            WindowParams params = AppUtils.getActivityWindowParams(activity);
            if (params != null) {
                baseSize = (int) ((isBaseOnWidth ? params.getWidth() : params.getHeight())
                        * metrics.density);
            }
        }
        String key = sizeInDp + "|" + subunitsDesignSize + "|" + isBaseOnWidth + "|"
                + config.isUseDeviceSize() + "|" + config.getInitScaledDensity() + "|" + baseSize;

        float targetDensity;
        int targetDensityDpi;
        float targetScaledDensity;
        float targetXdpi;
        int targetScreenWidthDp;
        int targetScreenHeightDp;
        DisplayMetricsInfo displayMetricsInfo = mCache.get(key);
        if (displayMetricsInfo == null) {
            // 根据设计图尺寸，重新计算density
            targetDensity = baseSize * 1.0f / sizeInDp;
            targetDensityDpi = (int) (targetDensity * 160);
            // 系统字体改变
            float scale = config.isExcludeFontScale() ? 1
                    : config.getInitScaledDensity() * 1.0f / config.getInitDensity();
            targetScaledDensity = targetDensity * scale;
            targetXdpi = baseSize * 1.0f / subunitsDesignSize;
            targetScreenWidthDp = (int) (config.getScreenWidth() / targetDensity);
            targetScreenHeightDp = (int) (config.getScreenHeight() / targetDensity);

            mCache.put(key, new DisplayMetricsInfo(targetDensity, targetDensityDpi,
                    targetScaledDensity, targetXdpi, targetScreenWidthDp, targetScreenHeightDp));
        } else {
            targetDensity = displayMetricsInfo.getDensity();
            targetDensityDpi = displayMetricsInfo.getDensityDpi();
            targetScaledDensity = displayMetricsInfo.getScaledDensity();
            targetXdpi = displayMetricsInfo.getXdpi();
            targetScreenWidthDp = displayMetricsInfo.getScreenWidthDp();
            targetScreenHeightDp = displayMetricsInfo.getScreenHeightDp();
        }

        setDensity(activity, targetDensity, targetDensityDpi, targetScaledDensity, targetXdpi);
        setScreenSizeDp(activity, targetScreenWidthDp, targetScreenHeightDp);

        LogUtils.d(String.format(Locale.ENGLISH,
                "%s has been adapted! \n%s Info: isBaseOnWidth = %s, %s = %.2f, %s = %.2f, targetDensity = %.2f, targetScaledDensity = %.2f, targetDensityDpi = %d, targetXdpi = %.2f, targetScreenWidthDp = %d, targetScreenHeightDp = %d",
                activity.getClass().getName(), activity.getClass().getSimpleName(), isBaseOnWidth,
                isBaseOnWidth ? "designWidthInDp" : "designHeightInDp", sizeInDp,
                isBaseOnWidth ? "designWidthInSubunits" : "designHeightInSubunits",
                subunitsDesignSize, targetDensity, targetScaledDensity, targetDensityDpi,
                targetXdpi, targetScreenWidthDp, targetScreenHeightDp));
    }

    /**
     * 给几大 {@link DisplayMetrics} 赋值
     * @param activity {@link Activity}
     * @param density {@link DisplayMetrics#density}
     * @param densityDpi {@link DisplayMetrics#densityDpi}
     * @param scaledDensity {@link DisplayMetrics#scaledDensity}
     * @param xdpi {@link DisplayMetrics#xdpi}
     */
    private static void setDensity(Activity activity, float density, int densityDpi,
            float scaledDensity, float xdpi) {
        // 兼容 MIUI
        Resources activityResources = activity.getResources();
        DisplayMetrics activityMetricsOnMiui = getMetricsOnMiui(activityResources);
        setDensity(
                activityMetricsOnMiui != null ? activityMetricsOnMiui
                        : activityResources.getDisplayMetrics(),
                density, densityDpi, scaledDensity, xdpi);

        // 兼容 MIUI
        Resources appResources = AutoSizeConfig.getInstance().getApplication().getResources();
        DisplayMetrics appMetricsOnMiui = getMetricsOnMiui(appResources);
        setDensity(appMetricsOnMiui != null ? appMetricsOnMiui : appResources.getDisplayMetrics(),
                density, densityDpi, scaledDensity, xdpi);
    }

    /**
     * 给 {@link Configuration} 赋值
     * @param activity {@link Activity}
     * @param screenWidthDp {@link Configuration#screenWidthDp}
     * @param screenHeightDp {@link Configuration#screenHeightDp}
     */
    private static void setScreenSizeDp(Activity activity, int screenWidthDp, int screenHeightDp) {
        UnitsManager unitsManager = AutoSizeConfig.getInstance().getUnitsManager();
        if (unitsManager.isSupportDp() && unitsManager.isSupportScreenSizeDp()) {
            setScreenSizeDp(activity.getResources().getConfiguration(), screenWidthDp,
                    screenHeightDp);

            Resources appResources = AutoSizeConfig.getInstance().getApplication().getResources();
            setScreenSizeDp(appResources.getConfiguration(), screenWidthDp, screenHeightDp);
        }
    }

    /**
     * 解决 MIUI 更改框架导致的 MIUI7 + Android5.1.1 上出现的失效问题 (以及极少数基于这部分 MIUI 去掉 ART 然后置入 XPosed 的手机)
     * 来源于:
     * https://github.com/Firedamp/Rudeness/blob/master/rudeness-sdk/src/main/java/com/bulong/rudeness/RudenessScreenHelper.java#L61:5
     * @param resources {@link Resources}
     * @return {@link DisplayMetrics}, 可能为 {@code null}
     */
    private static DisplayMetrics getMetricsOnMiui(Resources resources) {
        AutoSizeConfig config = AutoSizeConfig.getInstance();
        if (config.isMiui() && config.getTmpMetricsField() != null) {
            try {
                return (DisplayMetrics) config.getTmpMetricsField().get(resources);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 赋值
     * @param displayMetrics {@link DisplayMetrics}
     * @param density {@link DisplayMetrics#density}
     * @param densityDpi {@link DisplayMetrics#densityDpi}
     * @param scaledDensity {@link DisplayMetrics#scaledDensity}
     * @param xdpi {@link DisplayMetrics#xdpi}
     */
    static void setDensity(DisplayMetrics displayMetrics, float density, int densityDpi,
            float scaledDensity, float xdpi) {
        UnitsManager unitsManager = AutoSizeConfig.getInstance().getUnitsManager();
        if (unitsManager.isSupportDp()) {
            displayMetrics.density = density;
            displayMetrics.densityDpi = densityDpi;
        }
        if (unitsManager.isSupportSp()) {
            displayMetrics.scaledDensity = scaledDensity;
        }
        switch (unitsManager.getSupportSubunits()) {
        case PT:
            displayMetrics.xdpi = xdpi * 72f;
            break;
        case IN:
            displayMetrics.xdpi = xdpi;
            break;
        case MM:
            displayMetrics.xdpi = xdpi * 25.4f;
            break;
        case NONE:
        default:
            break;
        }
    }

    /**
     * Configuration赋值
     * @param configuration {@link Configuration}
     * @param screenWidthDp {@link Configuration#screenWidthDp}
     * @param screenHeightDp {@link Configuration#screenHeightDp}
     */
    private static void setScreenSizeDp(Configuration configuration, int screenWidthDp,
            int screenHeightDp) {
        configuration.screenWidthDp = screenWidthDp;
        configuration.screenHeightDp = screenHeightDp;
    }

    /**
     * 使用外部三方库的 {@link Activity} 或 {@link android.support.v4.app.Fragment} 的自定义适配参数进行适配
     * @param activity {@link Activity}
     * @param externalAdaptInfo 三方库的 {@link Activity} 或 {@link android.support.v4.app.Fragment}
     *            提供的适配参数, 需要配合
     *            {@link ExternalAdaptManager#addExternalAdaptInfoOfActivity(Class, ExternalAdaptInfo)}
     */
    public static void autoConvertDensityOfExternalAdaptInfo(Activity activity,
            ExternalAdaptInfo externalAdaptInfo) {
        Preconditions.checkNotNull(externalAdaptInfo, "externalAdaptInfo == null");
        float sizeInDp = externalAdaptInfo.getSizeInDp();

        // 如果 ExternalAdaptInfo#getSizeInDp() 返回 0, 则使用在 AndroidManifest 上填写的设计图尺寸
        if (sizeInDp <= 0) {
            if (externalAdaptInfo.isBaseOnWidth()) {
                sizeInDp = AutoSizeConfig.getInstance().getDesignWidthInDp();
            } else {
                sizeInDp = AutoSizeConfig.getInstance().getDesignHeightInDp();
            }
        }
        autoConvertDensity(activity, sizeInDp, externalAdaptInfo.isBaseOnWidth());
    }

    /**
     * 以宽度为基准进行适配
     * @param activity {@link Activity}
     * @param designWidthInDp 设计图的总宽度
     */
    public static void autoConvertDensityBaseOnWidth(Activity activity, float designWidthInDp) {
        autoConvertDensity(activity, designWidthInDp, true);
    }

    /**
     * 以高度为基准进行适配
     * @param activity {@link Activity}
     * @param designHeightInDp 设计图的总高度
     */
    public static void autoConvertDensityBaseOnHeight(Activity activity, float designHeightInDp) {
        autoConvertDensity(activity, designHeightInDp, false);
    }

    /**
     * 取消当前activity的适配
     * @param activity {@link Activity}
     */
    public static void cancelAdapt(Activity activity) {
        AutoSizeConfig config = AutoSizeConfig.getInstance();
        float initXdpi = config.getInitXdpi();
        switch (config.getUnitsManager().getSupportSubunits()) {
        case PT:
            initXdpi = initXdpi / 72f;
            break;
        case MM:
            initXdpi = initXdpi / 25.4f;
            break;
        default:
            break;
        }
        setDensity(activity, config.getInitDensity(), config.getInitDensityDpi(),
                config.getInitScaledDensity(), initXdpi);
        setScreenSizeDp(activity, config.getInitScreenWidthDp(), config.getInitScreenHeightDp());
    }

    /**
     * 当 App 中出现多进程，并且您需要适配所有的进程，就需要在 App 初始化时调用 {@link #initCompatMultiProcess}
     * 建议实现自定义 {@link Application} 并在 {@link Application#onCreate()} 中调用
     * {@link #initCompatMultiProcess}
     * @param context {@link Context}
     */
    public static void initCompatMultiProcess(Context context) {
        Cursor cursor = null;
        try {
            String uri = "content://" + context.getPackageName() + ".autosize-init-provider";
            cursor = context.getContentResolver().query(Uri.parse(uri), null, null, null, null);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
