package engin;

import java.util.List;

/**
 * 表示一个业务流程
 */
public class LinkNode {

    /**
     * 所有初始组件
     */
    public List<PluginNode> iniPlugin=null;

    /**
     * 业务流程组件
     */
    public  PluginNode root=null;

    /**
     * 业务流名称
     */
    public  String name="";

    /**
     * 任务完成组件名称
     */
    public  String taskComplete="";
}
