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

import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import me.jessyan.autosize.utils.AutoSizeUtils;

/**
 * ================================================
 * 通过声明 {@link ContentProvider} 自动完成初始化
 * Created by JessYan on 2018/8/19 11:55
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class InitProvider extends ContentProvider {
    /* 取消适配 */
    public static final String PATH_START = "adapt_start";
    public static final String PATH_STOP = "adapt_stop";
    public static final int CODE_START = 1;
    public static final int CODE_STOP = 2;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    @Override
    public boolean onCreate() {
        Context context = getContext();
        if (context != null) {
            String authority = "content://" + context.getPackageName() + ".autosize-init-provider";
            sURIMatcher.addURI(authority, PATH_START, CODE_START);
            sURIMatcher.addURI(authority, PATH_STOP, CODE_STOP);

            Application application = (Application) context.getApplicationContext();
            if (application == null) {
                application = AutoSizeUtils.getApplicationByReflect();
            }
            AutoSizeConfig.getInstance().setLog(true).init(application).setUseDeviceSize(false);
        }
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        // Context context = getContext();
        // if (context != null) {
        // int match = sURIMatcher.match(uri);
        // if (CODE_STOP == match) {
        // // AutoSizeConfig.getInstance().stop();
        // AutoSizeCompat.cancelAdapt(getContext().getResources());
        // }
        // LogUtils.d("InitProvider.query()-code=" + match);
        // } else {
        // LogUtils.d("InitProvider.query()-context=null");
        // }
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
