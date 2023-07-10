package bus;

/**
 * 日志结构
 */
public class LogMsg {
    public LogMsg()
    {

    }
    public LogMsg(String taskid, String flage, String msg)
    {
        this.msg=msg;
        this.taskid=taskid;
        this.flage=flage;
    }

    /**
     * 任务ID
     */
    public  String taskid;

    /**
     * 节点标识
     */

    public  String flage;

    /**
     * 消息
     */
    public String msg=null;
}
