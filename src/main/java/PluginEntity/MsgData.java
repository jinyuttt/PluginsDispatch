package PluginEntity;

/**
 * 数据 组件之间传递的数据
 */
public abstract class MsgData {

    /**
     * 任务ID
     */
    public String taskid;

    /**
     * 数据节点标识
     */
    public   String flage;

    /**
     * 一次流程的数据编号
     */
    public   long msgno=0;
}
