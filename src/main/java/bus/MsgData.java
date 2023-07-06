package bus;

import java.util.Arrays;

public class MsgData {
    public String topic;

    public byte[] data;

    public String getMsg()
    {
        return  new String(Arrays.toString(data));
    }
}
