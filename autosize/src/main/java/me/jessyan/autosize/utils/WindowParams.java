package me.jessyan.autosize.utils;

/**
 * 大屏应用窗口参数类，参考：https://bytedance.feishu.cn/docs/doccnhSFwUzyrNDaT08bVDXTLxc?new_source=message
 * @author jiwenjie
 */
public class WindowParams {
    public static final int WINDOW_MODE_NONE = -1;
    public static final int WINDOW_MODE_PORT = 0;
    public static final int WINDOW_MODE_LAND = 1;
    public static final int WINDOW_MODE_MAX = 2;
    public static final int WINDOW_MODE_MIN = 4;

    public static final int RESIZE_MODE__NONE = -1;
    public static final int RESIZE_MODE__NOT_STRETCH = 0;
    public static final int RESIZE_MODE__STRETCH__NOT_FULL = 1;
    public static final int RESIZE_MODE__PORT_STRETCH__NOT_FULL = 2;
    public static final int RESIZE_MODE__FULL = 4;
    public static final int RESIZE_MODE__RATIO_STRETCH = 8;

    private static final int PARAMS_NUM = 8;

    /**
     * 版本号，起始版本号从0开始，修改配置参数后请增加版本号使之生效。
     * 注意：版本号仅对application起作用，activity下无意义，可写任意值，当前主要是为了保持配置参数一致性，方便开发使用；
     * 如果同一应用在系统xml文件配置的版本号和应用自定义配置的版本号相同时则优先使用应用自定义配置
     */
    private int version;
    /**
     * -1-不指定；0-竖屏显示；1-横屏显示；2-最大化显示；4-全屏显示
     */
    private int windowMode;
    /**
     * -1-不指定；0-不能拉伸；1-可任意拉伸但不支持全屏；2-仅纵向拉伸且不支持全屏；4-支持全屏；8-按长宽比拉伸
     */
    private int resizeMode;
    /**
     * -1-不指定；0-不支持；1-支持restart后缩放；特殊用途，应用一般不用管这个参数，直接设置0或-1
     */
    private int forceResizeMode;
    /**
     * 窗口默认宽高，单位dp，0-不指定。注意：请确保长宽比跟windowMode一致
     */
    private int width;
    private int height;
    /**
     * 窗口最小宽高，单位dp，0-不指定，用于自由拉伸时最小窗口大小，如果不配置则使用窗口默认宽高。注意：请确保长宽比跟windowMode一致
     */
    private int minWidth;
    private int minHeight;
    private int[] originParams;

    public static WindowParams createWindowParams(int[] params) {
        int[] newParams = new int[PARAMS_NUM];
        System.arraycopy(params, 0, newParams, 0, Math.min(params.length, PARAMS_NUM));

        WindowParams windowParams = new WindowParams();
        windowParams.setOriginParams(newParams);

        windowParams.setVersion(newParams[0]);
        windowParams.setWindowMode(newParams[1]);
        windowParams.setResizeMode(newParams[2]);
        windowParams.setForceResizeMode(newParams[3]);
        windowParams.setWidth(newParams[4]);
        windowParams.setHeight(newParams[5]);
        windowParams.setMinWidth(newParams[6]);
        windowParams.setMinHeight(newParams[7]);
        return windowParams;
    }

    /**
     * subParams覆盖params，对应index没有值，不覆盖
     * @param params
     * @param subParams
     * @return
     */
    public static WindowParams combineWindowParams(int[] params, int[] subParams) {
        int[] newParams = new int[PARAMS_NUM];
        System.arraycopy(params, 0, newParams, 0, Math.min(params.length, PARAMS_NUM));
        System.arraycopy(subParams, 0, newParams, 0, Math.min(subParams.length, PARAMS_NUM));

        WindowParams windowParams = new WindowParams();
        windowParams.setOriginParams(newParams);

        windowParams.setVersion(newParams[0]);
        windowParams.setWindowMode(newParams[1]);
        windowParams.setResizeMode(newParams[2]);
        windowParams.setForceResizeMode(newParams[3]);
        windowParams.setWidth(newParams[4]);
        windowParams.setHeight(newParams[5]);
        windowParams.setMinWidth(newParams[6]);
        windowParams.setMinHeight(newParams[7]);
        return windowParams;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getWindowMode() {
        return windowMode;
    }

    public void setWindowMode(int windowMode) {
        this.windowMode = windowMode;
    }

    public int getResizeMode() {
        return resizeMode;
    }

    public void setResizeMode(int resizeMode) {
        this.resizeMode = resizeMode;
    }

    public int getForceResizeMode() {
        return forceResizeMode;
    }

    public void setForceResizeMode(int forceResizeMode) {
        this.forceResizeMode = forceResizeMode;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(int minWidth) {
        this.minWidth = minWidth;
    }

    public int getMinHeight() {
        return minHeight;
    }

    public void setMinHeight(int minHeight) {
        this.minHeight = minHeight;
    }

    @Override
    public String toString() {
        return "WindowParams{" + "version=" + version + ", windowMode=" + windowMode
                + ", resizeMode=" + resizeMode + ", forceResizeMode=" + forceResizeMode + ", width="
                + width + ", height=" + height + ", minWidth=" + minWidth + ", minHeight="
                + minHeight + '}';
    }

    public int[] getOriginParams() {
        return originParams;
    }

    public void setOriginParams(int[] originParams) {
        this.originParams = originParams;
    }
}
