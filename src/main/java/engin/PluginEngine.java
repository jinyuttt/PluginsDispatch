package engin;

import workplugins.ITask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 保持流程节点
 */
public class PluginEngine {

 /**
  * 业务流程
  */
 public  static List<LinkNode> lst;

 /**
  * 组件绑定主题
  */
 public  static Map<String,List<PluginNode>> topic=new HashMap<>();

 /**
  * 任务插件,用于判断任务状态,只有一个实例
  */
 public  static Map<String, ITask> taskMap=new HashMap<>();
}
