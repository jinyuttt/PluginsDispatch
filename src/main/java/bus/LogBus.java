package bus;

import Task.TaskEntity;
import engin.PluginEngine;
import engin.PluginNode;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

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

    /**
     * 所有返回的数据
     */
    private BlockingQueue<LogProcess> queue=new ArrayBlockingQueue<>(1000);

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
    private void init()
    {
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
              while (true)
              {
                  try {
                    var log=  queue.take();
                    var model=  TaskEntity.map.getOrDefault(log.taskid,null);
                      if(model==null)
                      {
                          continue;
                      }
                     var lst= PluginEngine.lst.stream().filter(p->p.name.equals(model.name));
                     var link=lst.findFirst();
                     var node=getFlageNode(link.get().root,log.flage);

                     log.msg=String.format("接收到数据{0},设备{1}",node.flage,node.devid);;

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
    public void  add(LogProcess obj)
    {
        queue.offer(obj);
    }
}
