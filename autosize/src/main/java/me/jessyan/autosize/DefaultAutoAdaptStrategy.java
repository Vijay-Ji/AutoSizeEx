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

import android.app.Activity;
import android.app.Application;

import me.jessyan.autosize.external.ExternalAdaptInfo;
import me.jessyan.autosize.external.ExternalAdaptManager;
import me.jessyan.autosize.internal.CancelAdapt;
import me.jessyan.autosize.internal.CustomAdapt;
import me.jessyan.autosize.utils.LogUtils;

/**
 * ================================================
 * 屏幕适配逻辑策略默认实现类, 可通过 {@link AutoSizeConfig#init(Application, boolean, AutoAdaptStrategy)}
 * 和 {@link AutoSizeConfig#setAutoAdaptStrategy(AutoAdaptStrategy)} 切换策略
 * Created by JessYan on 2018/8/9 15:57
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 * @see AutoAdaptStrategy
 */
public class DefaultAutoAdaptStrategy implements AutoAdaptStrategy {
    @Override
    public void applyAdapt(Object target, Activity activity) {
        // 检查是否开启了三方库的适配模式，只要不主动调用 ExternalAdaptManager 的方法，下面代码不会执行
        ExternalAdaptManager externalAdaptManager = AutoSizeConfig.getInstance()
                .getExternalAdaptManager();
        Class targetClass = target.getClass();
        if (externalAdaptManager.isRun()) {
            if (externalAdaptManager.isCancelAdapt(targetClass)) {
                LogUtils.w(String.format(Locale.ENGLISH, "%s canceled the adaptation!",
                        targetClass.getName()));
                AutoSize.cancelAdapt(activity);
                return;
            } else {
                ExternalAdaptInfo info = externalAdaptManager
                        .getExternalAdaptInfoOfActivity(targetClass);
                if (info != null) {
                    LogUtils.d(String.format(Locale.ENGLISH, "%s used %s for adaptation!",
                            targetClass.getName(), ExternalAdaptInfo.class.getName()));
                    AutoSize.autoConvertDensityOfExternalAdaptInfo(activity, info);
                    return;
                }
            }
        }

        // 如果 target 实现 CancelAdapt 接口表示放弃适配, 所有的适配效果都将失效
        if (target instanceof CancelAdapt) {
            LogUtils.w(String.format(Locale.ENGLISH, "%s canceled the adaptation!",
                    targetClass.getName()));
            AutoSize.cancelAdapt(activity);
            return;
        }

        // 如果 target 实现 CustomAdapt 接口表示该 target 想自定义一些用于适配的参数, 从而改变最终的适配效果
        if (target instanceof CustomAdapt) {
            AutoSize.autoConvertDensityOfCustomAdapt(activity, (CustomAdapt) target);
        } else {
            AutoSize.autoConvertDensityOfGlobal(activity);
        }
    }
}
