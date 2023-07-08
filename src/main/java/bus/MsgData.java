package bus;

import java.util.Arrays;

/**
 * 主题
 */
public class MsgData {

    /**
     * 主题
     */
    public String topic;


    /**
     * 数据
     */
    public byte[] data;

    public String getMsg()
    {
        return  new String(Arrays.toString(data));
    }
}
