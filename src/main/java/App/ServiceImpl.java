package App;

import Task.TaskEntity;
import Task.TaskModel;
import bus.DataBus;
import bus.MsgBus;
import bus.MsgData;
import com.google.gson.Gson;
import engin.PluginEngine;
import engin.PluginNode;
import workplugins.PluginAnnotation;

public class ServiceImpl {
   MsgBus bus=null;
   Gson gson=new Gson();
    public void ini(String[] addr,String[] topic)
    {
       bus.ini(addr);
        for (String str:topic
             ) {
            bus.subscribe(str);
        }

    }
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
                if(msg.topic.equals(TaskEntity.tasktopic))
                {
                    //接收到了任务消息
                  var model=  gson.fromJson(new String(msg.data),TaskModel.class);


                }
               var lst= PluginEngine.topic.getOrDefault(msg.topic,null);
               if(lst!=null)
               {
                   for (PluginNode node:lst
                        ) {
                       boolean issucess=false;
                       PluginAnnotation pluginAnnotation=   node.plugin.getClass().getAnnotation(PluginAnnotation.class);
                       if(pluginAnnotation.output().isEmpty())
                      {
                          //指定了结构
                          try {
                           var cls=   Class.forName(pluginAnnotation.output());
                         //  var obj= cls.getConstructor().newInstance();
                           String json=new String(msg.data);
                          var obj= gson.fromJson(json,cls);
                           if(MsgData.class.isInstance(obj))
                           {
                             var rsp=  (PluginEntity.MsgData) obj;
                             rsp.flage= node.flage;
                             rsp.msgno=node.msgno;
                               DataBus.getInstance().addData(rsp);
                               issucess=true;

                           }
                          } catch (Exception e) {
                              throw new RuntimeException(e);
                          }
                      }
                       if(!issucess)
                       {
                           node.plugin.responce(msg.data);
                       }
                   }
               }
            }

            }
        });
        s.start();
    }
}
