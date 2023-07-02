package bus;
import App.ConvertArgs;
import PluginEntity.MsgData;
import cache.CacheUtil;
import engin.PluginEngine;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class DataBus {

        private static class LazyHolder {
            private static final DataBus INSTANCE = new DataBus();
        }

    public static final DataBus getInstance() {
        return LazyHolder.INSTANCE;
    }
    private  Queue<MsgData> queue=new ArrayBlockingQueue<MsgData>(1000);
    private ConcurrentHashMap<String,MsgData> cache=new ConcurrentHashMap<String, MsgData>();

    private BlockingQueue<Long> msgqueue=new ArrayBlockingQueue<>(10000);
        private DataBus (){
            removeMsg();
            start();
        }
        public void  addData(MsgData obj) {
            queue.add(obj);
            CacheUtil.getInstance().putmap(obj.flage,String.valueOf(obj.msgno),obj);
        }

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
                        var lstNode = PluginEngine.lst.parallelStream().findAny();
                        var next = lstNode.filter(p -> p.flage == msg.flage);
                        var node=next.get().nexNode;
                        try {
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
