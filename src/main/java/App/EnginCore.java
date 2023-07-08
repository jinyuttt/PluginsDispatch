package App;

import PluginEntity.RootMsgData;
import Task.TaskEntity;
import Task.TaskModel;
import Task.TaskStatus;
import bus.DataBus;
import bus.MsgBus;
import engin.LinkNode;
import engin.PluginEngine;
import engin.PluginNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import workplugins.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

    /**
     * 启动初始化组件
     */
    public void start()
    {
       String path= this.getClass().getClassLoader().getResource("").getPath();
        //读取xml
       path= path+"Plugin.xml";
       var lst= Util.getLinkNode(path);//读取xml结构
       var lstP=Util.getCurrenPlugin();//获取所有组件信息
        logger.info("读取业务流程:"+lst.size());
        logger.info("读取组件:"+lstP.size());
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
                        //防止组件阻塞
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

        //任务组件
        for (ITask t:Util.lstTask
             ) {
          var p=   t.getClass().getAnnotation(PluginAnnotation.class);
            PluginEngine.taskMap.put(p.name(), (ITask) t);
        }
       logger.info("加载组件完成");
        checkTaskStatus();
    }


    /**
     * 检查任务状态
     */
    private void  checkTaskStatus()
    {
        logger.info("开启任务状态检查");
        newCachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    Iterator<Map.Entry<String, TaskStatus>> entries = TaskEntity.mapStatus.entrySet().iterator();
                    while (entries.hasNext()) {
                        Map.Entry<String, TaskStatus> entry = entries.next();
                        if (entry.getValue() == TaskStatus.Start) {
                            var model = TaskEntity.mapTask.get(entry.getKey());//找到任务
                            var link = PluginEngine.lst.stream().filter(p -> p.name.equals(model.name)).findFirst().get();//找到流程
                            var t = PluginEngine.taskMap.get(link.taskComplete);//找到组件
                            var ret = t.isComplete(entry.getKey());//找到结果
                            if (ret && DataBus.getInstance().isEmpty(entry.getKey())) {
                                //任务自动完成
                                complateTask(entry.getKey());
                            }

                        }

                    }
                }
            }
        });
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
    private  static void getNodeStop(PluginNode node,String taskid )
    {
        if(node!=null)
        {
            node.plugin.stop(taskid);
            if(node.pluginList!=null)
            {
                for (PluginNode tmp:node.pluginList
                     ) {
                    if(tmp.plugin!=null)
                       tmp.plugin.stop(taskid);
                }
            }
            if(node.nextNode!=null)
            for (PluginNode child:node.nextNode
                 ) {
                getNodeStop(node,taskid);
            }
        }
    }

    /**
     * 停止任务
     * @param taskid
     */
    public   void stopTask(String taskid)
    {

        //清理处理的数据
        DataBus.getInstance().clear(taskid);
        var model= TaskEntity.mapTask.get(taskid);
        if(model!=null) {
            //停止所有组件
          var link=  PluginEngine.lst.stream().filter(p -> p.name.equals(model.name)).findFirst();
          if(link!=null)
          {
            LinkNode node=  link.get();
            getNodeStop(node.root,taskid);
          }
        }
        TaskEntity.mapTask.remove(taskid);
        TaskEntity.mapStatus.put(taskid, TaskStatus.Stop);
    }

    /**
     * 开始任务
     * @param model
     */
   public  void startTask(TaskModel model)
   {
       var task=new RootMsgData();
       task.taskModel=model;
       task.flage=TaskEntity.rootFlage;
       TaskEntity.mapTask.put(model.taskid,model);
       TaskEntity.mapStatus.put(model.taskid,TaskStatus.Start);
       DataBus.getInstance().addData(task);
       logger.info("开始任务:"+model.taskid);


   }

    /**
     * 完成任务
     * @param taskid
     */
    public  void  complateTask(String taskid)
    {
        logger.info("停止任务:"+taskid);
        TaskEntity.mapStatus.put(taskid,TaskStatus.Complete);
        MsgBus.send("taskstatus",taskid);

    }
}



