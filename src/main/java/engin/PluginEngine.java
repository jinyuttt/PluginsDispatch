package engin;

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
}
