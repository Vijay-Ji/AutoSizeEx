package me.jessyan.autosize.demo;

import android.support.v7.app.AppCompatActivity;

import me.jessyan.autosize.AutoSizeConfig;
import me.jessyan.autosize.internal.CustomAdapt;

/**
 * @author jiwenjie
 */
public class BaseActivity extends AppCompatActivity implements CustomAdapt {

    @Override
    public boolean isBaseOnWidth() {
        return true;
    }

    /**
     * 竖屏：phone <= 720, 720 < pad <= 900
     * 横屏：pad, TNT > 900
     * @return
     */
    @Override
    public float getSizeInDp() {
        AutoSizeConfig config = AutoSizeConfig.getInstance();
        // 按宽适配
        if (isBaseOnWidth()) {
            return config.isVertical() ? config.getDesignWidthInDp() : config.getDesignHeightInDp();
        }
        // 按高适配
        return !config.isVertical() ? config.getDesignWidthInDp() : config.getDesignHeightInDp();
    }
}
