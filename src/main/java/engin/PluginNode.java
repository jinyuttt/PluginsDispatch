package engin;

import workplugins.IPlugin;

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
    public PluginNode nexNode;

    /**
     * 下级节点
     */
    public List<PluginNode> nextNode;

    /**
     * 次节点设备ID
     */
    public String devid;

    /**
     * 本节点组件
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

}
