package android.app;

public class SmtPCUtils {
    /**
     * 应用通过自己的context获取display id判断是否运行在大屏端，如果是手机+扩展屏模式，直接判断id >
     * 0就行，如果要支持大屏的Pad和TV，就只能用我们的私有接口isValidExtDisplayId()
     * @param id
     * @return true: on extend display false: on phone
     */
    public static boolean isValidExtDisplayId(int id) {
        return false;
    }
}
