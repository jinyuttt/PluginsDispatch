package bus;

import java.util.Arrays;

/**
 * 主题
 */
public class MsgData {
    public String topic;

    public byte[] data;

    public String getMsg()
    {
        return  new String(Arrays.toString(data));
    }
}
