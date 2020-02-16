package me.jessyan.autosize.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import me.jessyan.autosize.internal.CustomAdapt;
import me.jessyan.autosize.utils.LogUtils;

/**
 * @author jiwenjie
 */
public class CustomAdaptResActivity extends AppCompatActivity implements CustomAdapt {

    private ImageView mImgTestDisplay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_adapt_res);

        mImgTestDisplay = findViewById(R.id.image_test_display);
        mImgTestDisplay.post(new Runnable() {
            @Override
            public void run() {
                LogUtils.d("CustomAdaptResActivity.onCreate()-real(w="
                        + mImgTestDisplay.getMeasuredWidth() + ", h="
                        + mImgTestDisplay.getMeasuredHeight() + "), display(w="
                        + mImgTestDisplay.getWidth() + ", h=" + mImgTestDisplay.getHeight() + ")");
            }
        });
    }

    @Override
    public boolean isBaseOnWidth() {
        return true;
    }

    @Override
    public float getSizeInDp() {
        return 0;
    }
}
