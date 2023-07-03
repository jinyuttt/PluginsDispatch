package bus;

import App.ConvertArgs;
import PluginEntity.MsgData;
import cache.CacheUtil;
import engin.PluginEngine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import workplugins.IInputPlugin;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 数据分发
 */
public class DataBus {
    private final static Log logger = LogFactory.getLog(DataBus.class);
        private static class LazyHolder {
            private static final DataBus INSTANCE = new DataBus();
        }

    public static final DataBus getInstance() {
        return LazyHolder.INSTANCE;
    }


   AtomicLong atomicLong=new AtomicLong(0);

    /**
     * 接收数据
     */
    private  Queue<MsgData> queue=new ArrayBlockingQueue<MsgData>(1000);



    /**
     * 执行完成准备移除
     */
    private BlockingQueue<Long> msgqueue=new ArrayBlockingQueue<>(10000);
        private DataBus (){
            removeMsg();
            start();
        }
        public void  addData(MsgData obj) {
            if(IInputPlugin.class.isInstance(obj))
            {
                obj.msgno=atomicLong.getAndIncrement();
            }
            logger.debug("接收"+obj.flage);
            queue.add(obj);
            CacheUtil.getInstance().putmap(obj.flage,String.valueOf(obj.msgno),obj);

        }

        public boolean isEmpty()
        {
          return   queue.isEmpty();
        }

        public  void  clear()
        {
            queue.clear();
        }

    /**
     * 删除缓存
     */
    private void removeMsg()
        {
            Thread thread=new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Long no=  msgqueue.take();
                        var list = PluginEngine.lst;
                        for (var t:list
                             ) {
                            CacheUtil.getInstance().remove(t.flage,String.valueOf(no));
                        }

                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                }
            });
            thread.setName("remove");
            thread.setDaemon(true);
            thread.start();
        }
        private  void  start() {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        MsgData msg = queue.poll();
                        if(PluginEngine.lst==null)
                        {
                            System.out.println("没有业务");
                            continue;
                        }
                        var lstNode = PluginEngine.lst.parallelStream().findAny();
                        var next = lstNode.filter(p -> p.flage == msg.flage);
                        var node=next.get().nexNode;
                        try {
                            //根据条件
                            if(ConvertArgs.convertCondition(node,msg))
                            {
                                MsgData rsp= null;
                                try {
                                    rsp = ConvertArgs.convertInput(node,msg);
                                    node.plugin.addData(rsp);
                                } catch (ClassNotFoundException e) {
                                    throw new RuntimeException(e);
                                }

                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                        if(node.nexNode==null)
                        {
                            msgqueue.add(msg.msgno);
                        }
                    }
                }
            });
            thread.setDaemon(true);
            thread.setName("databus");
            thread.start();
        }
}
