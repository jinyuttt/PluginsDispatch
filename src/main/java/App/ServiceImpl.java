package App;

import Task.TaskEntity;
import Task.TaskModel;
import bus.*;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import engin.PluginEngine;
import engin.PluginNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import workplugins.PluginAnnotation;

/**
 * 接收外部请求
 */
public class ServiceImpl {
    static Log logger= LogFactory.getLog(ServiceImpl.class);
   MsgBus bus=new MsgBus();
   Gson gson=new Gson();

    /**
     * 所有订阅
     * @param addr
     * @param topic
     */
    public void ini(String[] addr,String[] topic)
    {
       bus.ini(addr);
        for (String str:topic
             ) {
            bus.subscribe(str);
        }

    }

    /**
     * 开始接收
     */
    public  void  start()
    {
        Thread s=new Thread(new Runnable() {
            @Override
            public void run() {
            bus.start();
            while (true)
            {
              MsgData msg= bus.getData();
              //根据主题转组件
                if(msg.topic.equals(TaskEntity.tasktopic)) {
                    //接收到了任务消息
                    var model = gson.fromJson(new String(msg.data), TaskModel.class);
                    if (model.taskopt.equals("stop")) {
                        logger.info("任务停止：" + model.taskid);
                        LogBus.getInstance().add(new LogMsg(model.taskid, "","停止任务"));
                        EnginCore.getInstance().stopTask(model.taskid);
                        continue;
                    }

                    var linkNode = PluginEngine.lst.parallelStream().filter(p -> p.name == model.name).findFirst();
                    if (linkNode == null) {
                        logger.error("流程对应错误，检查任务与流程的对应");
                        continue;
                    } else {
                        logger.info("任务开始：" + model.taskid);
                        LogBus.getInstance().add(new LogMsg(model.taskid, "","开始任务"));
                        EnginCore.getInstance().startTask(model);
                        //任务状态组件
                        String name = linkNode.get().taskComplete;
                        var t = PluginEngine.taskMap.get(name);
                        var map = AppCst.mapTask.get(name);
                        t.initArg(map);
                        t.init(model);
                        continue;
                    }
                }

                //查找组件
               var lst= PluginEngine.topic.getOrDefault(msg.topic,null);
               if(lst!=null)
               {
                   for (PluginNode node:lst
                        ) {
                       if(node.nextNode==null||node.nextNode.isEmpty())
                       {
                           String jon=new String(msg.data);
                           var obj= gson.fromJson(jon, JsonObject.class);
                          String taskid= obj.get("taskid").getAsString();
                           //没有下级节点返，则调度任务组件
                          var model= TaskEntity.mapTask.get(String.valueOf(taskid));
                           //找到对应的流程
                           if(model==null)
                           {
                               logger.error("流程没有对应数据");
                               continue;
                           }
                           var linkNode = PluginEngine.lst.parallelStream().filter(p->p.name==model.name).findFirst();
                           if(linkNode==null)
                           {
                               logger.error("流程对应错误，检查任务与流程的对应");
                               continue;
                           }
                           else
                           {
                             String name=  linkNode.get().taskComplete;
                            var t= PluginEngine.taskMap.get(name);
                            t.adddata(msg.data);
                            continue;
                           }
                       }
                       boolean issucess=false;
                       PluginAnnotation pluginAnnotation=   node.plugin.getClass().getAnnotation(PluginAnnotation.class);
                       if(pluginAnnotation.output().isEmpty())
                      {
                          //指定了结构,直接加入数据调度
                          try {
                           var cls=   Class.forName(pluginAnnotation.output());
                         //  var obj= cls.getConstructor().newInstance();
                           String json=new String(msg.data);
                          var obj= gson.fromJson(json,cls);
                           if(MsgData.class.isInstance(obj)) {
                               var rsp = (PluginEntity.MsgData) obj;
                               rsp.flage = node.flage;
                               rsp.msgno = node.msgno;
                               DataBus.getInstance().addData(rsp);
                               issucess = true;
                           }
                          } catch (Exception e) {
                              throw new RuntimeException(e);
                          }
                      }
                       if(!issucess)
                       {
                           /**
                            * 不能成功，则回给组件处理
                            */
                           node.plugin.response(msg.data);
                       }
                   }
               }
            }

            }
        });
        s.setDaemon(true);
        s.setName("ServiceImpl");
        s.start();
    }
}
