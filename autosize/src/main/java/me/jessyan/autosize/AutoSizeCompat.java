/*
 * Copyright 2019 JessYan
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import me.jessyan.autosize.external.ExternalAdaptInfo;
import me.jessyan.autosize.external.ExternalAdaptManager;
import me.jessyan.autosize.internal.CustomAdapt;
import me.jessyan.autosize.unit.UnitsManager;
import me.jessyan.autosize.utils.Preconditions;

/**
 * ================================================
 * 当遇到本来适配正常的布局突然出现适配失效，适配异常等问题, 重写当前 {@link Activity} 的 {@link Activity#getResources()} 并调用
 * {@link AutoSizeCompat} 的对应方法即可解决问题
 * <p>
 * Created by JessYan on 2018/8/8 19:20
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public final class AutoSizeCompat {
    private static Map<String, DisplayMetricsInfo> mCache = new ConcurrentHashMap<>();

    private AutoSizeCompat() {
        throw new IllegalStateException("you can't instantiate me!");
    }

    /**
     * 使用 AndroidAutoSize 初始化时设置的默认适配参数进行适配 (AndroidManifest 的 Meta 属性)
     * @param resources {@link Resources}
     */
    public static void autoConvertDensityOfGlobal(Resources resources) {
        AutoSizeConfig config = AutoSizeConfig.getInstance();
        if (config.isBaseOnWidth()) {
            autoConvertDensityBaseOnWidth(resources, config.getDesignWidthInDp());
        } else {
            autoConvertDensityBaseOnHeight(resources, config.getDesignHeightInDp());
        }
    }

    /**
     * 使用 {@link Activity} 或 {@link android.support.v4.app.Fragment} 的自定义参数进行适配
     * @param resources {@link Resources}
     * @param customAdapt {@link Activity} 或 {@link android.support.v4.app.Fragment} 需实现
     *            {@link CustomAdapt}
     */
    public static void autoConvertDensityOfCustomAdapt(Resources resources,
            CustomAdapt customAdapt) {
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
        autoConvertDensity(resources, sizeInDp, customAdapt.isBaseOnWidth());
    }

    /**
     * 这里是今日头条适配方案的核心代码, 核心在于根据当前设备的实际情况做自动计算并转换 {@link DisplayMetrics#density}、
     * {@link DisplayMetrics#scaledDensity}、{@link DisplayMetrics#densityDpi} 这三个值, 额外增加
     * {@link DisplayMetrics#xdpi}
     * 以支持单位 {@code pt}、{@code in}、{@code mm}
     * @param resources {@link Resources}
     * @param sizeInDp 设计图上的设计尺寸, 单位 dp, 如果 {@param isBaseOnWidth} 设置为 {@code true},
     *            {@param sizeInDp} 则应该填写设计图的总宽度, 如果 {@param isBaseOnWidth} 设置为 {@code false},
     *            {@param sizeInDp} 则应该填写设计图的总高度
     * @param isBaseOnWidth 是否按照宽度进行等比例适配, {@code true} 为以宽度进行等比例适配, {@code false} 为以高度进行等比例适配
     * @see <a href="https://mp.weixin.qq.com/s/d9QCoBP6kV9VSWvVldVVwA">今日头条官方适配方案</a>
     */
    public static void autoConvertDensity(Resources resources, float sizeInDp,
            boolean isBaseOnWidth) {
        Preconditions.checkNotNull(resources, "resources == null");

        AutoSizeConfig config = AutoSizeConfig.getInstance();
        float subunitsDesignSize = isBaseOnWidth ? config.getUnitsManager().getDesignWidth()
                : config.getUnitsManager().getDesignHeight();
        subunitsDesignSize = subunitsDesignSize > 0 ? subunitsDesignSize : sizeInDp;
        int baseSize = isBaseOnWidth ? config.getScreenWidth() : config.getScreenHeight();
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

        setDensity(resources, targetDensity, targetDensityDpi, targetScaledDensity, targetXdpi);
        setScreenSizeDp(resources, targetScreenWidthDp, targetScreenHeightDp);
    }

    /**
     * 给几大 {@link DisplayMetrics} 赋值
     * @param resources {@link Resources}
     * @param density {@link DisplayMetrics#density}
     * @param densityDpi {@link DisplayMetrics#densityDpi}
     * @param scaledDensity {@link DisplayMetrics#scaledDensity}
     * @param xdpi {@link DisplayMetrics#xdpi}
     */
    private static void setDensity(Resources resources, float density, int densityDpi,
            float scaledDensity, float xdpi) {
        // 兼容 MIUI
        DisplayMetrics activityMetricsOnMiui = getMetricsOnMiui(resources);
        AutoSize.setDensity(activityMetricsOnMiui != null ? activityMetricsOnMiui
                : resources.getDisplayMetrics(), density, densityDpi, scaledDensity, xdpi);

        // 兼容 MIUI
        Resources appResources = AutoSizeConfig.getInstance().getApplication().getResources();
        DisplayMetrics appMetricsOnMiui = getMetricsOnMiui(appResources);
        AutoSize.setDensity(
                appMetricsOnMiui != null ? appMetricsOnMiui : appResources.getDisplayMetrics(),
                density, densityDpi, scaledDensity, xdpi);
    }

    /**
     * 给 {@link Configuration} 赋值
     * @param resources {@link Resources}
     * @param screenWidthDp {@link Configuration#screenWidthDp}
     * @param screenHeightDp {@link Configuration#screenHeightDp}
     */
    private static void setScreenSizeDp(Resources resources, int screenWidthDp,
            int screenHeightDp) {
        UnitsManager unitsManager = AutoSizeConfig.getInstance().getUnitsManager();
        if (unitsManager.isSupportDp() && unitsManager.isSupportScreenSizeDp()) {
            setScreenSizeDp(resources.getConfiguration(), screenWidthDp, screenHeightDp);

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
     * @param resources {@link Resources}
     * @param externalAdaptInfo 三方库的 {@link Activity} 或 {@link android.support.v4.app.Fragment}
     *            提供的适配参数, 需要配合
     *            {@link ExternalAdaptManager#addExternalAdaptInfoOfActivity(Class, ExternalAdaptInfo)}
     */
    public static void autoConvertDensityOfExternalAdaptInfo(Resources resources,
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
        autoConvertDensity(resources, sizeInDp, externalAdaptInfo.isBaseOnWidth());
    }

    /**
     * 以宽度为基准进行适配
     * @param resources {@link Resources}
     * @param designWidthInDp 设计图的总宽度
     */
    public static void autoConvertDensityBaseOnWidth(Resources resources, float designWidthInDp) {
        autoConvertDensity(resources, designWidthInDp, true);
    }

    /**
     * 以高度为基准进行适配
     * @param resources {@link Resources}
     * @param designHeightInDp 设计图的总高度
     */
    public static void autoConvertDensityBaseOnHeight(Resources resources, float designHeightInDp) {
        autoConvertDensity(resources, designHeightInDp, false);
    }

    /**
     * 取消适配
     * @param resources {@link Resources}
     */
    public static void cancelAdapt(Resources resources) {
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
        setDensity(resources, config.getInitDensity(), config.getInitDensityDpi(),
                config.getInitScaledDensity(), initXdpi);
        setScreenSizeDp(resources, config.getInitScreenWidthDp(), config.getInitScreenHeightDp());
    }
}
