package App;

import PluginEntity.RootMsgData;
import Task.TaskEntity;
import Task.TaskModel;
import bus.DataBus;
import engin.LinkNode;
import engin.PluginEngine;
import engin.PluginNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import workplugins.IInitPlugin;
import workplugins.IInputPlugin;
import workplugins.IPlugin;
import workplugins.PluginAnnotation;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 处理组件
 */
public class EnginCore {

    public  static class  SingleInstance
    {
        static  final  EnginCore INSTANCE= new EnginCore();
    }
    public static final EnginCore getInstance()
    {
        return  SingleInstance.INSTANCE;
    }
    Log logger= LogFactory.getLog(EnginCore.class);
    ExecutorService newCachedThreadPool = Executors.newCachedThreadPool();
    public void start()
    {
       String path= this.getClass().getClassLoader().getResource("").getPath();
        //读取xml
       path= path+"Plugin.xml";
       var lst= Util.getNode(path);//读取xml结构
       var lstP=Util.getCurrenPlugin();//获取所有组件信息
        PluginEngine.lst=lst;
        for (LinkNode link:lst
             ) {
            if(link.iniPlugin!=null)
            {
                //遍历所有配置的组件结构，创建组件实例
                for (PluginNode node:link.iniPlugin) {
                      //通过配置的名称查找对应的组件类
                        var tmpss=   lstP.stream().filter(p->{
                        PluginAnnotation name =  p.getClass().getAnnotation(PluginAnnotation.class);
                        if(IInitPlugin.class.isInstance(p))
                        {
                            if(name.name().toLowerCase().equals(node.name.toLowerCase()))
                            {
                                return  true;
                            }
                        }
                     return false;
                 });
                 if(tmpss==null)
                 {
                     continue;
                 }
                  var tmp=tmpss.findFirst();
                    try {
                        //创建对象实例
                        node.plugin = tmp.get().getClass().getConstructor().newInstance();
                        IInitPlugin cur= (IInitPlugin) node.plugin;
                        newCachedThreadPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                cur.init(node.arg);
                                //启动组件
                                cur.init();
                                cur.start();
                            }
                        });


                    }catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
                if(link.root!=null)
                {
                     PluginNode node=link.root;
                    getNode(node,lstP);
                }
            }
        }

        System.out.println("");
    }

    /**
     * 遍历创建所有节点子节点的组件实例
     * @param node
     * @param lstP
     */
   private  void  getNode(PluginNode node, List<IPlugin>lstP)
   {
           PluginNode finalNode = node;
           var tmp = lstP.stream().filter(p -> {
               PluginAnnotation name = p.getClass().getAnnotation(PluginAnnotation.class);
               String cur= finalNode.name;
               if (name.name().toLowerCase().equals(cur.toLowerCase())) {
                   return true;
               }
               return false;
           }).findFirst();
           try {
               //节点创建实例
               node.plugin = tmp.get().getClass().getConstructor().newInstance();
               if(node.pluginList!=null)
               {
                   //从根开始，遍历所有组件
                   for (PluginNode tmpnode: node.pluginList
                   ) {
                       tmpnode.plugin=tmp.get().getClass().getConstructor().newInstance();
                       if(IInputPlugin.class.isInstance(tmpnode.plugin)) {
                           //如果是输入插件则先启动
                           IInputPlugin inputPlugin = (IInputPlugin) tmpnode.plugin;
                           newCachedThreadPool.execute(new Runnable() {
                               @Override
                               public void run() {
                                   inputPlugin.init(tmpnode.arg);
                                   inputPlugin.start();

                               }
                           });
                       }

                   }
               }
               else
               {
                   node.plugin.init(node.arg);
                   //本节点单例时
                   if(IInputPlugin.class.isInstance(node.plugin))
                   {
                       IInputPlugin inputPlugin= (IInputPlugin) node.plugin;
                       inputPlugin.start();
                   }

               }
           }
           catch (Exception ex)
           {
               ex.printStackTrace();
           }
          //
       //本节点创建完成
       if(node.nextNode!=null) {
           for (PluginNode child : node.nextNode
           ) {
               getNode(child,lstP);
           }
       }
       }

    /**
     * 停止所有
     * @param node
     */
    private  static void getNodeStop(PluginNode node)
    {
        if(node!=null)
        {
            node.plugin.stop();
            if(node.pluginList!=null)
            {
                for (PluginNode tmp:node.pluginList
                     ) {
                    if(tmp.plugin!=null)
                       tmp.plugin.stop();
                }
            }
            if(node.nextNode!=null)
            for (PluginNode child:node.nextNode
                 ) {
                getNodeStop(node);
            }
        }
    }

    //停止任务
    public  static void stop(String taskid)
    {

        //清理处理的数据
        DataBus.getInstance().clear(taskid);
       var model= TaskEntity.map.getOrDefault(taskid,null);
        if(model!=null) {
          var link=  PluginEngine.lst.stream().filter(p -> p.name.equals(model.name)).findFirst();
          if(link!=null)
          {
            LinkNode node=  link.get();
            getNodeStop(node.root);
          }
        }


    }


   public  void  starttask(TaskModel model)
   {
       var task=new RootMsgData();
       task.taskModel=model;
       task.flage=TaskEntity.rootFlage;
       TaskEntity.map.put(model.taskid,model);
       DataBus.getInstance().addData(task);
   }
}



