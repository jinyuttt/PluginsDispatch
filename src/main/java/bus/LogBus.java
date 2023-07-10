package bus;

import Task.TaskEntity;
import com.google.gson.Gson;
import engin.PluginEngine;
import engin.PluginNode;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 处理专门的日志信息
 */
public class LogBus {
    private static class LazyHolder {
        private static final LogBus INSTANCE = new LogBus();
    }

    public static final LogBus getInstance() {
        return LogBus.LazyHolder.INSTANCE;
    }

    private LogBus()
    {
        init();
    }

    private Gson gson=new Gson();

    /**
     * 所有返回的数据
     */
    private BlockingQueue<LogMsg> queue=new ArrayBlockingQueue<>(1000);


    /**
     * 查找组件
     * @param child
     * @param flage
     * @return
     */
    private PluginNode getFlageNode(PluginNode child, String flage)
    {
        if(child.flage.equals(flage))
        {
            return  child;
        }
        else
        {
            if(child.pluginList!=null)
            {
                for (PluginNode tmp :child.pluginList
                ) {
                    var node= getFlageNode(tmp,flage);
                    if(node!=null)
                    {
                        return  node;
                    }
                }
            }
        }

        return  null;
    }

    /**
     * 初始化
     */
    private void init()
    {
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
              while (true)
              {
                  try {
                    var log=  queue.take();
                    var model=  TaskEntity.mapTask.getOrDefault(log.taskid,null);
                      if(model==null)
                      {
                          continue;
                      }
                     var lst= PluginEngine.lst.stream().filter(p->p.name.equals(model.name));
                     var link=lst.findFirst();
                     var node=getFlageNode(link.get().root,log.flage);
                     if (log.msg!=null)
                     log.msg=String.format("接收到{2}数据{0},设备{1}",node.flage,node.devid,node.display);
                     var msg=gson.toJson(log);
                     MsgBus.send("processlog",msg);

                  } catch (InterruptedException e) {
                      throw new RuntimeException(e);
                  }
              }
            }
        });
        thread.setName("log");
        thread.setDaemon(true);
        thread.start();
    }
    public void  add(LogMsg obj)
    {
        queue.offer(obj);
    }
}
