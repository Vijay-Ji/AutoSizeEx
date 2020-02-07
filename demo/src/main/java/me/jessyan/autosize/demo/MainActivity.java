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
package me.jessyan.autosize.demo;

import java.util.Locale;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import cat.ereza.customactivityoncrash.activity.DefaultErrorActivity;
import cat.ereza.customactivityoncrash.config.CaocConfig;
import me.jessyan.autosize.AutoSizeConfig;
import me.jessyan.autosize.internal.CustomAdapt;

/**
 * ================================================<p>
 * 本框架核心原理来自于 <a href="https://mp.weixin.qq.com/s/d9QCoBP6kV9VSWvVldVVwA">今日头条官方适配方案</a>，
 * 此方案不仅适配 {@link Activity}, Activity下所有 {@link Fragment}、{@link Dialog}、{@link View} 都会自动适配
 * <p>
 * {@link MainActivity} 是以屏幕宽度为基准进行适配的, 并且使用的是在 AndroidManifest 中填写的全局设计图尺寸 360 * 640
 * 不懂什么叫基准的话, 请看 {@link AutoSizeConfig#isBaseOnWidth()}) 的注释, AndroidAutoSize 默认全局以屏幕宽度为基准进行适配
 * 如果想更改为全局以屏幕高度为基准进行适配, 请在 {@link BaseApplication} 中按注释中更改, 为什么强调全局？
 * 因为 AndroidAutoSize 允许每个 {@link Activity} 可以自定义适配参数, 自定义适配参数通过实现 {@link CustomAdapt}
 * 如果不自定义适配参数就会使用全局的适配参数, 全局适配参数在 {@link BaseApplication} 中按注释设置
 * <p>
 * Created by JessYan on 2018/8/9 17:05
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
// 实现 CancelAdapt 即可取消当前 Activity 的屏幕适配, 并且这个 Activity 下的所有 Fragment 和 View 都会被取消适配
// public class MainActivity extends AppCompatActivity implements CancelAdapt {
public class MainActivity extends AppCompatActivity {
    private TextView mScreenSizeView, mScreenDensityView, mScreenInfoAutoSize;
    private Button mBtnStartStopAutoSize;

    /**
     * 跳转到 {@link CustomAdaptActivity}, 展示项目内部的 {@link Activity} 自定义适配参数的用法
     * @param view {@link View}
     */
    public void goCustomAdaptActivity(View view) {
        startActivity(new Intent(getApplicationContext(), CustomAdaptActivity.class));
    }

    /**
     * 跳转到三方库的 {@link Activity}, 展示项目外部某些三方库的 {@link Activity} 自定义适配参数的用法
     * 跳转前要先在 {@link BaseApplication#addExternalAdapt()} ()} 中给外部的三方库 {@link Activity} 自定义适配参数
     * @param view {@link View}
     */
    public void goThirdLibraryActivity(View view) {
        // 这里就是随便找个三方库的 Activity, 测试下适配三方库页面的功能是否可用
        // 以下代码就是为了启动这个三方库的 Activity, 不必在意
        Intent intent = new Intent(getApplicationContext(), DefaultErrorActivity.class);
        Bundle extras = new Bundle();
        extras.putSerializable("cat.ereza.customactivityoncrash.EXTRA_CONFIG",
                CaocConfig.Builder.create().get());
        intent.putExtras(extras);
        startActivity(intent);
    }

    /**
     * 需要注意的是停止或重启AndroidAutoSize，只是停止或重启后续要启动的适配工作；对已经启动并适配的，没有影响
     * @param view {@link View}
     */
    public void startOrStop(View view) {
        AutoSizeConfig config = AutoSizeConfig.getInstance();
        if (config.isStop()) {
            config.restart();
        } else {
            config.stop(this);
        }
        updateAutoSizeStatus();
        // AutoSize状态修改 Toast提示
        String text = !config.isStop() ? "AutoSize Started" : "AutoSize Stopped";
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();

        // 重绘界面
        recreate();
    }

    private void updateAutoSizeStatus() {
        String text = !AutoSizeConfig.getInstance().isStop() ? "AutoSize Started"
                : "AutoSize Stopped";
        mBtnStartStopAutoSize.setText(text);
    }

    /**
     * 点击view换图，判断当前DisplayMetrics的density
     * @param view
     */
    public void changeImage(View view) {
        String tag = (String) view.getTag();
        ImageView imageView = (ImageView) view;
        if ("0".equals(tag)) {
            imageView.setImageResource(R.mipmap.ic_launcher_new);
            imageView.setTag("1");
        } else {
            imageView.setImageResource(R.color.colorAccent);
            imageView.setTag("0");
        }
    }

    public void regainScreenInfo(View view) {
        updateScreenInfo();
        updateAutoSizeStatus();
    }

    private void updateScreenInfo() {
        WindowManager w = (WindowManager) getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        Display d = w.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getRealMetrics(metrics);

        mScreenSizeView.setText(String.format(Locale.getDefault(), "RealSize: %d x %d",
                metrics.widthPixels, metrics.heightPixels));
        mScreenDensityView.setText(String.format(Locale.getDefault(),
                "Real: density = %.2f, dpi = %d", metrics.density, metrics.densityDpi));

        // 展示已经autosize的屏幕信息
        DisplayMetrics autosizeMetrics = getResources().getDisplayMetrics();
        mScreenInfoAutoSize
                .setText(String.format(Locale.getDefault(), "AutoSize: density = %.2f, dpi = %d",
                        autosizeMetrics.density, autosizeMetrics.densityDpi));
    }

    @Override
    public Resources getResources() {
        // AutoSizeCompat.autoConvertDensityOfGlobal((super.getResources()));
        return super.getResources();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mScreenSizeView = findViewById(R.id.screen_size);
        mScreenDensityView = findViewById(R.id.screen_density);
        mScreenInfoAutoSize = findViewById(R.id.screen_info_autosize);

        mBtnStartStopAutoSize = findViewById(R.id.btn_start_stop);

        // 更新屏幕、AutoSize的状态信息
        updateScreenInfo();
        updateAutoSizeStatus();
    }
}
