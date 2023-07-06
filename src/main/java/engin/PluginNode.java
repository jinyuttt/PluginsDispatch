package engin;

import workplugins.IPlugin;
import workplugins.Policy;

import java.util.List;
import java.util.Map;

/**
 * 业务节点
 */
public class PluginNode {

    /**
     * 节点名称，与组件名称对应
     */
    public String name;

    /**
     * 节点唯一标识
     */
    public String flage;

    /**
     * 调用组件的条件
     */
    public String condition;

    /**
     * 组件本身需要参数，例如IP
     */
    public String  arg;


    /**
     * 下级节点
     */
    public List<PluginNode> nextNode;

    /**
     * 次节点设备ID
     */
    public String devid;

    /**
     * 本节点组件实例
     */
    public IPlugin plugin;

    /**
     * 本节点多实例
     */
    public List<PluginNode> pluginList;

    /**
     * 数据转换关系
     */
    public Map<String,String> map;

    /**
     * 多实例时调用策略
     */
    public Policy policy=Policy.Order;

    /**
     * 轮训策略
     */
    public int  index=0;

    /**
     * 订阅主题
     */
    public List<String> subTopic;

    /**
     * 最新一包数据
     */
    public  long msgno=0;

}
